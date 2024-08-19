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
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.Identifier;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.ChunkPos;

import java.util.List;

public class CommandLocatePart implements Command<ServerCommandSource> {

    private static final CommandLocatePart CMD = new CommandLocatePart();

    public static ArgumentBuilder<ServerCommandSource, ?> register(CommandDispatcher<ServerCommandSource> dispatcher) {
        return CommandManager.literal("locatepart")
                .requires(cs -> cs.hasPermissionLevel(1))
                .then(CommandManager.argument("name", IdentifierArgumentType.identifier()).suggests(
                        ModCommandManager.getPartSuggestionProvider()
                ).executes(CMD));
    }


    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Identifier name = context.getArgument("name", Identifier.class);

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
        // Abuse BlockPos as ChunkPos
        int cnt = 0;
        for (BlockPos.Mutable mpos : BlockPos.iterateInSquare(new BlockPos(cp.x, 0, cp.z), 15, Direction.EAST, Direction.SOUTH)) {
            List<EditModeData.PartData> data = EditModeData.getData().getPartData(new ChunkCoord(level.getRegistryKey(), mpos.getX(), mpos.getZ()));
            for (EditModeData.PartData pd : data) {
                if (pd.partName().equals(name.toString())) {
                    context.getSource().sendFeedback(() -> TextFactory.literal("Found at " + (mpos.getX() * 16 + 8) + "," + pd.y() + "," + (mpos.getZ() * 16 + 8)), false);
                    cnt++;
                    if (cnt > 6) {
                        break;
                    }
                }
            }
        }
        return 0;
    }
}
