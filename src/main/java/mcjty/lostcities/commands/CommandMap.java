package mcjty.lostcities.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.lostcities.setup.Registration;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.StructureWorldAccess;

public class CommandMap implements Command<ServerCommandSource> {

    private static final CommandMap CMD = new CommandMap();

    public static ArgumentBuilder<ServerCommandSource, ?> register(CommandDispatcher<ServerCommandSource> dispatcher) {
        return CommandManager.literal("map")
                .requires(cs -> cs.hasPermissionLevel(0))
                .executes(CMD);
    }


    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        BlockPos position = player.getBlockPos();
        IDimensionInfo dimInfo = Registration.LOSTCITY_FEATURE.get().getDimensionInfo((StructureWorldAccess) player.getServerWorld());
        if (dimInfo != null) {
            ChunkPos pos = new ChunkPos(position);
            for (int z = pos.z - 20 ; z <= pos.z + 20 ; z++) {
                StringBuilder buf = new StringBuilder();
                for (int x = pos.x - 20 ; x <= pos.x + 20 ; x++) {
                    ChunkCoord coord = new ChunkCoord(dimInfo.getType(), x, z);
                    BuildingInfo info = BuildingInfo.getBuildingInfo(coord, dimInfo);
                    if (info.isCity && info.hasBuilding) {
                        buf.append("B");
                    } else if (info.isCity) {
                        buf.append("+");
                    } else if (info.highwayXLevel >= 0 || info.highwayZLevel >= 0) {
                        buf.append(".");
                    } else {
                        buf.append(" ");
                    }
                }
                //noinspection UseOfSystemOutOrSystemErr
                System.out.println(buf);
            }
        }
        return 0;
    }
}
