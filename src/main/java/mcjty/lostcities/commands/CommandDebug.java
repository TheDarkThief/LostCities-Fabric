package mcjty.lostcities.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.lostcities.setup.Registration;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.worldgen.ChunkHeightmap;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import mcjty.lostcities.worldgen.lost.CitySphere;
import mcjty.lostcities.worldgen.lost.Railway;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;

public class CommandDebug implements Command<ServerCommandSource> {

    private static final CommandDebug CMD = new CommandDebug();

    public static ArgumentBuilder<ServerCommandSource, ?> register(CommandDispatcher<ServerCommandSource> dispatcher) {
        return CommandManager.literal("debug")
                .requires(cs -> cs.hasPermissionLevel(0))
                .executes(CMD);
    }


    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        BlockPos position = player.getBlockPos();
        IDimensionInfo dimInfo = Registration.LOSTCITY_FEATURE.get().getDimensionInfo((StructureWorldAccess) player.getWorld());
        if (dimInfo != null) {
            ChunkCoord coord = new ChunkCoord(dimInfo.getType(), position.getX() >> 4, position.getZ() >> 4);
            BuildingInfo info = BuildingInfo.getBuildingInfo(coord, dimInfo);
            System.out.println("profile = " + info.profile.getName());
//            System.out.println("provider.hasMansion = " + info.provider.hasMansion(info.chunkX, info.chunkZ));
            System.out.println("buildingType = " + info.buildingType.getName());
            System.out.println("floors = " + info.getNumFloors());
            System.out.println("floorsBelowGround = " + info.cellars);
            System.out.println("cityLevel = " + info.cityLevel);
            System.out.println("cityGroundLevel = " + info.getCityGroundLevel());
            System.out.println("isCity = " + info.isCity);
            System.out.println("chunkX = " + info.chunkX);
            System.out.println("chunkZ = " + info.chunkZ);
            System.out.println("getCityStyle() = " + BuildingInfo.getChunkCharacteristics(info.coord, info.provider).cityStyle.getName());
            System.out.println("streetType = " + info.streetType);
            System.out.println("ruinHeight = " + info.ruinHeight);
            System.out.println("tunnel0 = " + info.isTunnel(0));
            System.out.println("tunnel1 = " + info.isTunnel(1));
            System.out.println("getHighwayXLevel() = " + info.getHighwayXLevel());
            System.out.println("getHighwayZLevel() = " + info.getHighwayZLevel());

            float reldist = CitySphere.getRelativeDistanceToCityCenter(info.coord, dimInfo);
            System.out.println("reldist = " + reldist);

            Railway.RailChunkInfo railInfo = Railway.getRailChunkType(info.coord, info.provider, info.profile);
            System.out.println("railInfo.getType() = " + railInfo.getType());
            System.out.println("railInfo.getWorld() = " + railInfo.getWorld());
            System.out.println("railInfo.getDirection() = " + railInfo.getDirection());
            System.out.println("railInfo.getRails() = " + railInfo.getRails());

            CitySphere sphere = CitySphere.getCitySphere(info.coord, dimInfo);
            System.out.println("sphere.cityCenter = " + sphere.getCenter());
            System.out.println("sphere.isEnabled() = " + sphere.isEnabled());
            System.out.println("sphere.radius = " + sphere.getRadius());

            int explosions = info.getExplosions().size();
            System.out.println("explosions = " + explosions);

            ChunkHeightmap heightmap = dimInfo.getFeature().getHeightmap(info.coord, (StructureWorldAccess) player.getServerWorld());
            System.out.println("Chunk height (heightmap): " + heightmap.getHeight());

            System.out.println("dimInfo.getProfile().BUILDING_MINFLOORS = " + dimInfo.getProfile().BUILDING_MINFLOORS);
            System.out.println("dimInfo.getProfile().BUILDING_MAXFLOORS = " + dimInfo.getProfile().BUILDING_MAXFLOORS);
            System.out.println("dimInfo.getProfile().CITY_CHANCE = " + dimInfo.getProfile().CITY_CHANCE);
            System.out.println("info.isOcean() = " + info.isOcean());
        }
        return 0;
    }
}
