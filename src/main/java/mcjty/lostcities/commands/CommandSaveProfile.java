package mcjty.lostcities.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.lostcities.config.ProfileSetup;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.varia.TextFactory;
import net.minecraft.util.Formatting;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class CommandSaveProfile implements Command<ServerCommandSource> {

    private static final CommandSaveProfile CMD = new CommandSaveProfile();

    public static ArgumentBuilder<ServerCommandSource, ?> register(CommandDispatcher<ServerCommandSource> dispatcher) {
        return CommandManager.literal("saveprofile")
                .requires(cs -> cs.hasPermissionLevel(0))
                .then(CommandManager.argument("profile", StringArgumentType.word())
                    .executes(CMD));
    }


    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String name = context.getArgument("profile", String.class);
        LostCityProfile profile = ProfileSetup.STANDARD_PROFILES.get(name);
        if (profile == null) {
            context.getSource().sendFeedback(() -> TextFactory.literal(Formatting.RED + "Could not find profile '" + name + "'!"), true);
            return 0;
        }
        JsonObject jsonObject = profile.toJson(false);
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        try {
            try (PrintWriter writer = new PrintWriter(new File(name + ".json"))) {
                writer.print(gson.toJson(jsonObject));
                writer.flush();
            }
        } catch (FileNotFoundException e) {
            context.getSource().sendFeedback(() -> TextFactory.literal(Formatting.RED + "Error saving profile '" + name + "'!"), true);
            return 0;
        }
        context.getSource().sendFeedback(() -> TextFactory.literal(Formatting.GREEN + "Saved profile '" + name + "'!"), true);
        return 0;
    }
}
