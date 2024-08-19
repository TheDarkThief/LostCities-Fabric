package mcjty.lostcities.worldgen;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.StructureVoidBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;

import net.minecraft.block.HorizontalConnectingBlock;

import net.minecraft.block.enums.StairShape;
import net.minecraft.block.enums.WallShape;
import net.minecraft.world.ChunkSectionCache;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.Heightmap;

import org.jetbrains.annotations.Nullable;
import java.util.function.Predicate;

import static net.minecraft.world.chunk.ChunkSection.*;

public class ChunkDriver {

    private WorldAccess region;
    private Chunk primer;
    private final BlockPos.Mutable current = new BlockPos.Mutable();
    private final BlockPos.Mutable pos = new BlockPos.Mutable();
//    private final Long2ObjectOpenHashMap<BlockState> cache = new Long2ObjectOpenHashMap<>();
    private SectionCache cache;
    private int cx;
    private int cz;

    public void setPrimer(WorldAccess region, Chunk primer) {
        this.region = region;
        this.primer = primer;
        if (primer != null) {
            cache = new SectionCache(region, primer.getPos().x << 4, primer.getPos().z << 4);
            this.cx = primer.getPos().x;
            this.cz = primer.getPos().z;
        }
    }

    public void actuallyGenerate(Chunk chunk) {
        ChunkSectionCache bulk = new ChunkSectionCache(region);
        cache.generate(bulk);
        bulk.close();

        BlockState bedrock = Blocks.BEDROCK.getDefaultState();
        for (int x = 0 ; x < 16 ; x++) {
            for (int z = 0 ; z < 16 ; z++) {
                int y = cache.heightmap[x][z];
                if (y > Integer.MIN_VALUE) {
                    chunk.getHeightmap(Heightmap.Type.MOTION_BLOCKING).trackUpdate(x, y, z, bedrock);
                    chunk.getHeightmap(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES).trackUpdate(x, y, z, bedrock);
                    chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR).trackUpdate(x, y, z, bedrock);
                    chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE).trackUpdate(x, y, z, bedrock);
                }
            }
        }

        cache.clear();
    }

    private void setBlockState(BlockPos p, BlockState state) {
        if (state != null) {
            cache.put(p, state);
        }
    }

    // This version of getBlock() is less optimal but it will work for different chunks
    private BlockState getBlockSafe(BlockPos p) {
        return isThisChunk(p) ? getBlock(p) : region.getBlockState(p);
    }

    private BlockState getBlock(BlockPos p) {
        BlockState state = cache.get(p);
        if (state == null) {
            state = region.getBlockState(p);
            cache.put(p, state);
        }
        return state;
    }

    public WorldAccess getRegion() {
        return region;
    }

    public Chunk getPrimer() {
        return primer;
    }

    public ChunkDriver current(int x, int y, int z) {
        current.set(x + (primer.getPos().x << 4), y, z + (primer.getPos().z << 4));
        return this;
    }

    public ChunkDriver currentAbsolute(BlockPos pos) {
        current.set(pos);
        return this;
    }

    public ChunkDriver currentRelative(BlockPos pos) {
        current(pos.getX(), pos.getY(), pos.getZ());
        return this;
    }

    public BlockPos getCurrentCopy() {
        return current.toImmutable();
    }

    public BlockPos.Mutable getCurrent() {
        return current;
    }

    public void incY() {
        current.setY(current.getY()+1);
    }

    public void decY() {
        current.setY(current.getY()-1);
    }

    public void incX() {
        current.setX(current.getX()+1);
    }

    public void incZ() {
        current.setZ(current.getZ()+1);
    }

    public int getX() {
        return current.getX();
    }

    public int getY() {
        return current.getY();
    }

    public int getZ() {
        return current.getZ();
    }

    public void setBlockRange(int x, int y, int z, int y2, BlockState state) {
        pos.set(x + (primer.getPos().x << 4), y, z + (primer.getPos().z << 4));
        while (y < y2) {
            setBlockState(pos, state);
            y++;
            pos.setY(y);
        }
    }

    public void setBlockRange(int x, int y, int z, int y2, BlockState state, Predicate<BlockState> test) {
        pos.set(x + (primer.getPos().x << 4), y, z + (primer.getPos().z << 4));
        while (y < y2) {
            BlockState st = getBlock(pos);
            if (st != state && test.test(st)) {
                setBlockState(pos, state);
            }
            y++;
            pos.setY(y);
        }
    }

    public void setBlockRangeToAir(int x, int y, int z, int y2) {
        BlockState air = Blocks.AIR.getDefaultState();
        pos.set(x + (primer.getPos().x << 4), y, z + (primer.getPos().z << 4));
        while (y < y2) {
            setBlockState(pos, air);
            y++;
            pos.setY(y);
        }
    }

    public void setBlockRangeToAir(int x, int y, int z, int y2, Predicate<BlockState> test) {
        BlockState air = Blocks.AIR.getDefaultState();
        pos.set(x + (primer.getPos().x << 4), y, z + (primer.getPos().z << 4));
        while (y < y2) {
            BlockState st = getBlock(pos);
            if (st != air && test.test(st)) {
                setBlockState(pos, air);
            }
            y++;
            pos.setY(y);
        }
    }

    private boolean isThisChunk(BlockPos pos) {
        int px = pos.getX() >> 4;
        int pz = pos.getZ() >> 4;
        return px == cx && pz == cz;
    }

    private BlockState updateAdjacent(BlockState state, Direction direction, BlockPos pos, Chunk thisChunk) {
        BlockState adjacent = getBlockSafe(pos);
        if (adjacent.getBlock() instanceof LadderBlock) {
            return adjacent;
        }
        BlockState newAdjacent = null;
        try {
            newAdjacent = adjacent.getStateForNeighborUpdate(direction, state, region, pos, pos.offset(direction));
        } catch (Exception e) {
            // We got an exception. For example for beehives there can potentially be a problem so in this case we just ignore it
            return adjacent;
        }
        if (newAdjacent != adjacent) {
            Chunk chunk = region.getChunk(pos);
            if (chunk == thisChunk) {
                setBlockState(pos, newAdjacent);
            } else if (chunk.getStatus().isAtLeast(ChunkStatus.FULL)) {
                region.setBlockState(pos, newAdjacent, Block.NOTIFY_LISTENERS);
            }
        }
        return newAdjacent;
    }

    public static boolean isBlockStairs(BlockState state) {
        return state.getBlock() instanceof StairsBlock;
    }

    private boolean isDifferentStairs(BlockState state, BlockPos pos, Direction face) {
        BlockPos relative = pos.offset(face);
        BlockState blockstate = getBlockSafe(relative);
        return !isBlockStairs(blockstate) || blockstate.get(StairsBlock.FACING) != state.get(StairsBlock.FACING) || blockstate.get(StairsBlock.HALF) != state.get(StairsBlock.HALF);
    }

    private StairShape getShapeProperty(BlockState state, BlockPos pos) {
        Direction direction = state.get(StairsBlock.FACING);
        BlockPos relative = pos.offset(direction);
        BlockState blockstate = getBlockSafe(relative);
        if (isBlockStairs(blockstate) && state.get(StairsBlock.HALF) == blockstate.get(StairsBlock.HALF)) {
            Direction direction1 = blockstate.get(StairsBlock.FACING);
            if (direction1.getAxis() != state.get(StairsBlock.FACING).getAxis() && isDifferentStairs(state, pos, direction1.getOpposite())) {
                if (direction1 == direction.rotateYCounterclockwise()) {
                    return StairShape.OUTER_LEFT;
                }

                return StairShape.OUTER_RIGHT;
            }
        }

        BlockPos relativeOpposite = pos.offset(direction.getOpposite());
        BlockState blockstate1 = getBlockSafe(relativeOpposite);
        if (isBlockStairs(blockstate1) && state.get(StairsBlock.HALF) == blockstate1.get(StairsBlock.HALF)) {
            Direction direction2 = blockstate1.get(StairsBlock.FACING);
            if (direction2.getAxis() != state.get(StairsBlock.FACING).getAxis() && isDifferentStairs(state, pos, direction2)) {
                if (direction2 == direction.rotateYCounterclockwise()) {
                    return StairShape.INNER_LEFT;
                }

                return StairShape.INNER_RIGHT;
            }
        }

        return StairShape.STRAIGHT;
    }

    private static WallShape canAttachWall(BlockState state) {
        return canAttach(state) ? WallShape.LOW : WallShape.NONE;
    }

    private static boolean canAttach(BlockState state) {
        if (state.isAir()) {
            return false;
        }
        if (state.isOpaque()) {
            return true;
        }
        return !Block.cannotConnect(state);
    }

    private BlockState correct(BlockState state) {
        int cx = current.getX();
        int cy = current.getY();
        int cz = current.getZ();

        Chunk thisChunk = region.getChunk(cx >> 4, cz >> 4);
        BlockState westState = updateAdjacent(state, Direction.EAST, pos.set(cx - 1, cy, cz), thisChunk);
        BlockState eastState = updateAdjacent(state, Direction.WEST, pos.set(cx + 1, cy, cz), thisChunk);
        BlockState northState = updateAdjacent(state, Direction.SOUTH, pos.set(cx, cy, cz - 1), thisChunk);
        BlockState southState = updateAdjacent(state, Direction.NORTH, pos.set(cx, cy, cz + 1), thisChunk);

        if (state.getBlock() instanceof HorizontalConnectingBlock) {
            state = state.with(HorizontalConnectingBlock.WEST, canAttach(westState));
            state = state.with(HorizontalConnectingBlock.EAST, canAttach(eastState));
            state = state.with(HorizontalConnectingBlock.NORTH, canAttach(northState));
            state = state.with(HorizontalConnectingBlock.SOUTH, canAttach(southState));
        } else if (state.getBlock() instanceof WallBlock) {
            state = state.with(WallBlock.WEST_SHAPE, canAttachWall(westState));
            state = state.with(WallBlock.EAST_SHAPE, canAttachWall(eastState));
            state = state.with(WallBlock.NORTH_SHAPE, canAttachWall(northState));
            state = state.with(WallBlock.SOUTH_SHAPE, canAttachWall(southState));
        } else if (state.getBlock() instanceof StairsBlock) {
            state = state.with(StairsBlock.SHAPE, getShapeProperty(state, pos.set(cx, cy, cz)));
        } else if (state.getBlock() instanceof StructureVoidBlock){
            //like an alpha channel - but for parts! Uses whatever block was previously there instead of changing it!
            return null;
        }
        return state;
    }

    public ChunkDriver blockImm(BlockState c) {
        setBlockState(pos, c);
        return this;
    }

//    private void validate() {
//        if (current.getX() < 0 || current.getY() < 0 || current.getZ() < 0) {
//            throw new RuntimeException("current: " + current.getX() + "," + current.getY() + "," + current.getZ());
//        }
//        if (current.getX() > 15 || current.getY() > 255 || current.getZ() > 15) {
//            throw new RuntimeException("current: " + current.getX() + "," + current.getY() + "," + current.getZ());
//        }
//    }

    public ChunkDriver block(BlockState c) {
//        validate();
        setBlockState(current, correct(c));
        return this;
    }

    public ChunkDriver add(BlockState state) {
//        validate();
        setBlockState(current, correct(state));
        incY();
        return this;
    }

    public BlockState getBlock() {
        return getBlock(current);
    }

    public BlockState getBlockDown() {
        return getBlock(pos.set(current.getX(), current.getY()-1, current.getZ()));
    }

    public BlockState getBlockEast() {
        return getBlock(pos.set(current.getX()+1, current.getY(), current.getZ()));
    }

    public BlockState getBlockWest() {
        return getBlock(pos.set(current.getX()-1, current.getY(), current.getZ()));
    }

    public BlockState getBlockSouth() {
        return getBlock(pos.set(current.getX(), current.getY(), current.getZ()+1));
    }

    public BlockState getBlockNorth() {
        return getBlock(pos.set(current.getX(), current.getY(), current.getZ()-1));
    }


    public BlockState getBlock(int x, int y, int z) {
        return getBlock(pos.set(x + (primer.getPos().x << 4), y, z + (primer.getPos().z << 4)));
    }

    private static class S {
        private final BlockState[] section = new BlockState[field_31408];
        private boolean isEmpty = true;
    }

    private static class SectionCache {
        private final int minY;
        private final int maxY;
        private final int cx;
        private final int cz;
        private final S[] cache;
        private final int[][] heightmap = new int[16][16];

        private SectionCache(WorldAccess level, int cx, int cz) {
            minY = level.getTopY();
            maxY = level.getTopY();
            this.cx = cx;
            this.cz = cz;
            cache = new S[(maxY - minY) / field_31407];
            clear();
        }

        private void put(BlockPos pos, BlockState state) {
            int sectionIdx = (pos.getY() - minY) / field_31407;
            int px = pos.getX() & 0xf;
            int pz = pos.getZ() & 0xf;
            int idx = (px << 8) + ((pos.getY() & 0xf) << 4) + pz;
            cache[sectionIdx].section[idx] = state;
            cache[sectionIdx].isEmpty = false;
            if (!state.isAir()) {
                if (heightmap[px][pz] < pos.getY()) {
                    heightmap[px][pz] = pos.getY();
                }
            }
        }

        @Nullable
        private BlockState get(BlockPos pos) {
            int sectionIdx = (pos.getY() - minY) / field_31407;
            int idx = ((pos.getX() & 0xf) << 8) + ((pos.getY() & 0xf) << 4) + ((pos.getZ() & 0xf));
            return cache[sectionIdx].section[idx];
        }

        private void generate(ChunkSectionCache bulk) {
            for (int si = 0 ; si < (maxY - minY) / field_31407 ; si++) {
                S c = cache[si];
                if (!c.isEmpty) {
                    int cy = si * field_31407 + minY;
                    ChunkSection section = bulk.getSection(new BlockPos(cx, cy, cz));
                    if (section == null) {
                        throw new RuntimeException("This cannot happen: " + si);
                    }
                    int i = 0;
                    for (int x = 0 ; x < field_31406 ; x++) {
                        for (int y = 0 ; y < field_31407 ; y++) {
                            for (int z = 0 ; z < field_31406 ; z++) {
                                BlockState state = c.section[i++];
                                if (state != null) {
                                    section.setBlockState(x, y, z, state, false);
                                }
                            }
                        }
                    }
                }
            }
        }

        private void clear() {
            for (int si = 0 ; si < (maxY - minY) / field_31407 ; si++) {
                cache[si] = new S();
            }
            for (int x = 0 ; x < 16 ; x++) {
                for (int z = 0 ; z < 16 ; z++) {
                    heightmap[x][z] = Integer.MIN_VALUE;
                }
            }
        }
    }
}
