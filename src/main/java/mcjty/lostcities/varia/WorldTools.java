package mcjty.lostcities.varia;

import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.ChunkRegion;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import net.minecraft.world.gen.structure.StructureKeys;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Map;
import java.util.Set;

public class WorldTools {

    public static boolean chunkLoaded(World world, BlockPos pos) {
        if (world == null || pos == null) {
            return false;
        }
        return world.isChunkLoaded(pos);
//        return world.getChunkProvider().getLoadedChunk(pos.getX() >> 4, pos.getZ() >> 4) != null && world.getChunkFromBlockCoords(pos).isLoaded();
    }

    public static ServerWorld getOverworld() {
        
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server.getWorld(World.OVERWORLD);
    }

    public static ServerWorld getOverworld(World world) {
        MinecraftServer server = world.getServer();
        return server.getWorld(World.OVERWORLD);
    }

    public static ServerWorld loadWorld(RegistryKey<World> type) {
        ServerWorld world = getWorld(type);
        if (world == null) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            return server.getWorld(type);
        }
        return world;
    }

    public static ServerWorld getWorld(RegistryKey<World> type) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server.getWorld(type);
    }

    public static ServerWorld getWorld(World world, RegistryKey<World> type) {
        MinecraftServer server = world.getServer();
        return server.getWorld(type);
    }

    public static Map<Structure, LongSet> checkStructures(ServerWorld level, ChunkCoord coord) {
        BlockPos center = new BlockPos(coord.chunkX() << 4 + 8, 60, coord.chunkZ() << 4 + 8);
        StructureAccessor structuremanager = level.structureManager();
//        Map<StructurePlacement, Set<RegistryEntry<Structure>>> map = new Object2ObjectArrayMap<>();
        Map<Structure, LongSet> structures = structuremanager.getAllStructuresAt(center);
        return structures;
//        RegistryEntryList<Structure> holderSet = RegistryEntryList.direct(StructureKeys.ANCIENT_CITY);
//        StructurePlacementCalculator chunkgeneratorstructurestate = level.getChunkSource().getGeneratorState();
//
//        for(RegistryEntry<Structure> holder : holderSet) {
//            for(StructurePlacement structureplacement : chunkgeneratorstructurestate.getPlacementsForStructure(holder)) {
//                map.computeIfAbsent(structureplacement, (_v) -> new ObjectArraySet<>()).add(holder);
//            }
//        }

    }
}
