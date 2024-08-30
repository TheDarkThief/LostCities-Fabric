package mcjty.lostcities.editor;

import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import mcjty.lostcities.worldgen.lost.cityassets.BuildingPart;
import mcjty.lostcities.worldgen.lost.cityassets.CompiledPalette;
import mcjty.lostcities.worldgen.lost.cityassets.Palette;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.ServerTask;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;

public class Editor {

    public static void startEditing(BuildingPart part, ServerPlayerEntity player, BlockPos start, ServerWorld level, IDimensionInfo dimInfo, boolean clear) {
        ChunkCoord coord = new ChunkCoord(dimInfo.getType(), start.getX() >> 4, start.getZ() >> 4);
        BuildingInfo info = BuildingInfo.getBuildingInfo(coord, dimInfo);
        CompiledPalette palette = info.getCompiledPalette();
        Palette partPalette = part.getLocalPalette(level);
        Palette buildingPalette = info.getBuilding().getLocalPalette(level);
        if (partPalette != null || buildingPalette != null) {
            palette = new CompiledPalette(palette, partPalette, buildingPalette);
        }

        EditorInfo editorInfo = EditorInfo.createEditorInfo(player.getUuid(), part.getName(), start);

        CompiledPalette finalPalette = palette;

        player.getServer().execute(new ServerTask(3, () -> {
            if (clear) {
                for (int y = 0; y < part.getSliceCount(); y++) {
                    for (int x = 0; x < part.getXSize(); x++) {
                        for (int z = 0; z < part.getZSize(); z++) {
                            BlockPos pos = info.getRelativePos(x, start.getY() + y, z);
                            Character character = part.getC(x, y, z);
                            BlockState state = finalPalette.get(character);
                            if (state != null) {
                                level.setBlockState(pos, state, Block.NOTIFY_ALL);
                            } else {
                                level.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                            }
                        }
                    }
                }
            }
            for (int y = 0; y < part.getSliceCount(); y++) {
                for (int x = 0; x < part.getXSize(); x++) {
                    for (int z = 0; z < part.getZSize(); z++) {
                        BlockPos pos = info.getRelativePos(x, start.getY() + y, z);
                        Character character = part.getC(x, y, z);
                        if (finalPalette.get(character) != null) {
                            BlockState state = level.getBlockState(pos);
                            editorInfo.addPaletteEntry(character, state);
                        }
                    }
                }
            }
        }));
    }
}
