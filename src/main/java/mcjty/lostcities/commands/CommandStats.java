package mcjty.lostcities.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.lostcities.setup.Registration;
import mcjty.lostcities.varia.Statistics;
import mcjty.lostcities.worldgen.IDimensionInfo;
import net.minecraft.util.Formatting;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.StructureWorldAccess;

public class CommandStats implements Command<ServerCommandSource> {

    private static final CommandStats CMD = new CommandStats();

    public static ArgumentBuilder<ServerCommandSource, ?> register(CommandDispatcher<ServerCommandSource> dispatcher) {
        return CommandManager.literal("stats")
                .requires(cs -> cs.hasPermissionLevel(0))
                .executes(CMD);
    }


    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        IDimensionInfo dimInfo = Registration.LOSTCITY_FEATURE.get().getDimensionInfo((StructureWorldAccess) player.getServerWorld());
        if (dimInfo != null) {
            Statistics statistics = dimInfo.getFeature().getStatistics();
            float averageTime = statistics.getAverageTime();
            long minTime = statistics.getMinTime();
            long maxTime = statistics.getMaxTime();
            context.getSource().sendFeedback(() -> Text.literal("Average time: " + averageTime + "ms").formatted(Formatting.YELLOW), false);
            context.getSource().sendFeedback(() -> Text.literal("Min time: " + minTime + "ms").formatted(Formatting.YELLOW), false);
            context.getSource().sendFeedback(() -> Text.literal("Max time: " + maxTime + "ms").formatted(Formatting.YELLOW), false);
        } else {
            context.getSource().sendError(Text.literal("No dimension info found!").formatted(Formatting.RED));
        }
        return 0;
    }
}
