package mcjty.lostcities.varia;

import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraftforge.server.ServerLifecycleHooks;

import net.minecraft.server.world.ServerWorld;

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

}
