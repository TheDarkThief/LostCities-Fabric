package mcjty.lostcities.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.lostcities.editor.EditModeData;
import mcjty.lostcities.setup.Registration;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.varia.TextFactory;
import mcjty.lostcities.worldgen.IDimensionInfo;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.ChunkPos;

import java.util.List;

public class CommandListParts implements Command<ServerCommandSource> {

    private static final CommandListParts CMD = new CommandListParts();

    public static ArgumentBuilder<ServerCommandSource, ?> register(CommandDispatcher<ServerCommandSource> dispatcher) {
        return CommandManager.literal("listparts")
                .requires(cs -> cs.hasPermissionLevel(1)).executes(CMD);
    }


    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        BlockPos start = player.getBlockPos();

        ServerWorld level = (ServerWorld) player.getServerWorld();
        IDimensionInfo dimInfo = Registration.LOSTCITY_FEATURE.get().getDimensionInfo(level);
        if (dimInfo == null) {
            context.getSource().sendError(TextFactory.literal("This dimension doesn't support Lost Cities!"));
            return 0;
        }
        if (!dimInfo.getProfile().EDITMODE) {
            context.getSource().sendError(TextFactory.literal("This world was not created with edit mode enabled. This command is not possible!"));
            return 0;
        }

        ChunkPos cp = new ChunkPos(start);
        List<EditModeData.PartData> data = EditModeData.getData().getPartData(new ChunkCoord(level.dimension(), cp.x, cp.z));
        for (EditModeData.PartData pd : data) {
            context.getSource().sendFeedback(() -> TextFactory.literal("Found '" + pd.partName() + "' at " + pd.y()), false);
        }
        return 0;
    }
}
