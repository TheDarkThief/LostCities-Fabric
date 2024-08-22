package mcjty.lostcities.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.lostcities.editor.EditModeData;
import mcjty.lostcities.editor.Editor;
import mcjty.lostcities.setup.Registration;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.varia.TextFactory;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import mcjty.lostcities.worldgen.lost.cityassets.BuildingPart;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.ChunkPos;

public class CommandEditPart implements Command<ServerCommandSource> {

    private static final CommandEditPart CMD = new CommandEditPart();

    public static ArgumentBuilder<ServerCommandSource, ?> register(CommandDispatcher<ServerCommandSource> dispatcher) {
        return CommandManager.literal("editpart")
                .requires(cs -> cs.hasPermissionLevel(1)).executes(CMD);
    }


    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        BlockPos start = player.getBlockPos();

        ServerWorld level = (ServerWorld) player.getWorld();
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
        for (EditModeData.PartData data : EditModeData.getData().getPartData(new ChunkCoord(level.dimension(), cp.x, cp.z))) {
            BuildingPart part = AssetRegistries.PARTS.get(level, data.partName());
            if (part == null) {
                context.getSource().sendError(TextFactory.literal("Unknown part '" + data.partName() + "' in this chunk!"));
                return 0;
            }
            if (data.y() <= start.getY() && start.getY() < data.y() + part.getSliceCount()) {
                context.getSource().sendFeedback(() -> TextFactory.literal("Start editing part '" + data.partName() + "'!"), false);
                Editor.startEditing(part, player, new BlockPos(start.getX(), data.y(), start.getZ()), level, dimInfo, true);
                return 0;
            }
        }

        context.getSource().sendError(TextFactory.literal("Could not find a part to edit in this chunk!"));
        return 0;
    }
}
