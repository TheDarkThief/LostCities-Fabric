package mcjty.lostcities.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.lostcities.editor.Editor;
import mcjty.lostcities.setup.Registration;
import mcjty.lostcities.varia.TextFactory;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import mcjty.lostcities.worldgen.lost.cityassets.BuildingPart;
import net.minecraft.util.Formatting;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.DefaultPosArgument;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;

public class CommandCreatePart implements Command<ServerCommandSource> {

    private static final CommandCreatePart CMD = new CommandCreatePart();

    public static ArgumentBuilder<ServerCommandSource, ?> register(CommandDispatcher<ServerCommandSource> dispatcher) {
        return CommandManager.literal("createpart")
                .requires(cs -> cs.hasPermissionLevel(1))
                .then(CommandManager.argument("name", IdentifierArgumentType.identifier())
                        .suggests(ModCommandManager.getPartSuggestionProvider())
                        .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                .executes(CMD)));
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Identifier name = context.getArgument("name", Identifier.class);
        BuildingPart part = null;
        try {
            part = AssetRegistries.PARTS.get(context.getSource().getWorld(), name);
        } catch (Exception e) {
            part = null;
        }
        if (part == null) {
            context.getSource().sendError(Text.literal("Error finding part '" + name + "'!").formatted(Formatting.RED));
            return 0;
        }

        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        DefaultPosArgument start = context.getArgument("pos", DefaultPosArgument.class);


        ServerWorld level = (ServerWorld) player.getServerWorld();
        IDimensionInfo dimInfo = Registration.LOSTCITY_FEATURE.get().getDimensionInfo(level);
        if (dimInfo == null) {
            context.getSource().sendError(TextFactory.literal("This dimension doesn't support Lost Cities!"));
            return 0;
        }

        Editor.startEditing(part, player, start.toAbsoluteBlockPos(context.getSource()), level, dimInfo, true);

        return 0;
    }

}
