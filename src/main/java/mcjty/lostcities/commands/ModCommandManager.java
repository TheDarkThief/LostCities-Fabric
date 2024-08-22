package mcjty.lostcities.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import mcjty.lostcities.LostCities;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import mcjty.lostcities.worldgen.lost.cityassets.Building;
import mcjty.lostcities.worldgen.lost.cityassets.BuildingPart;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.command.CommandSource;

import org.jetbrains.annotations.NotNull;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ModCommandManager {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> commands = dispatcher.register(
                CommandManager.literal(LostCities.MODID)
                        .then(CommandCreateBuilding.register(dispatcher))
                        .then(CommandDebug.register(dispatcher))
                        .then(CommandStats.register(dispatcher))
                        .then(CommandMap.register(dispatcher))
                        .then(CommandSaveProfile.register(dispatcher))
                        .then(CommandCreatePart.register(dispatcher))
                        .then(CommandLocatePart.register(dispatcher))
                        .then(CommandLocate.register(dispatcher))
                        .then(CommandEditPart.register(dispatcher))
                        .then(CommandResumeEdit.register(dispatcher))
                        .then(CommandListParts.register(dispatcher))
                        .then(CommandExportPart.register(dispatcher))
                        .then(CommandTestFill.register(dispatcher))
        );

        dispatcher.register(CommandManager.literal("lost").redirect(commands));
        ResetChunksCommand.register(dispatcher);
    }

    @NotNull
    static SuggestionProvider<ServerCommandSource> getPartSuggestionProvider() {
        return (context, builder) -> {
            Stream<BuildingPart> stream = StreamSupport.stream(AssetRegistries.PARTS.getIterable().spliterator(), false);
            return CommandSource.suggestMatching.map(b -> b.getId().toString()), builder);
        };
    }

    @NotNull
    static SuggestionProvider<ServerCommandSource> getBuildingSuggestionProvider() {
        return (context, builder) -> {
            Stream<Building> stream = StreamSupport.stream(AssetRegistries.BUILDINGS.getIterable().spliterator(), false);
            return CommandSource.suggestMatching.map(b -> b.getId().toString()), builder);
        };
    }
}
