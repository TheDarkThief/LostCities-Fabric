package mcjty.lostcities.worldgen;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.api.LostChunkCharacteristics;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import net.minecraft.util.Formatting;
import net.minecraft.text.Text;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ErrorLogger {

    private static long lastReportTime = -1;

    public static void report(String message) {
        long time = System.currentTimeMillis();
        if (lastReportTime == -1 || lastReportTime < (time-10000)) {
            // Not reported before or too long ago
            lastReportTime = time;
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                player.sendMessage(Text.literal(message).formatted(Formatting.RED));
            }
        }
    }

    public static void logChunkInfo(int chunkX, int chunkZ, IDimensionInfo provider) {
        Logger logger = LostCities.getLogger();
        try {
            ChunkCoord coord = new ChunkCoord(provider.getType(), chunkX, chunkZ);
            logger.info("IsCity: " + BuildingInfo.isCityRaw(coord, provider, provider.getProfile()));
            LostChunkCharacteristics characteristics = BuildingInfo.getChunkCharacteristics(coord, provider);
            logger.info("    Level: " + characteristics.cityLevel);
            if (characteristics.multiBuilding != null) {
                logger.info("    Multibuilding: " + characteristics.multiBuilding.getName());
            }
            if (characteristics.buildingType != null) {
                logger.info("    Building: " + characteristics.buildingType.getName());
            }
            BuildingInfo info = BuildingInfo.getBuildingInfo(coord, provider);
            if (info.hasBuilding) {
                logger.info("        Floors: " + info.getNumFloors());
                logger.info("        Cellars: " + info.getNumCellars());
            }
        } catch (Exception e) {
            logger.warn("Error loging chunk info!", e);
        }
    }
}
