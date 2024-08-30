package mcjty.lostcities.commands;

import com.google.gson.*;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import mcjty.lostcities.editor.EditorInfo;
import mcjty.lostcities.setup.Registration;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.varia.TextFactory;
import mcjty.lostcities.varia.Tools;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import mcjty.lostcities.worldgen.lost.cityassets.BuildingPart;
import mcjty.lostcities.worldgen.lost.cityassets.CompiledPalette;
import mcjty.lostcities.worldgen.lost.cityassets.Palette;
import mcjty.lostcities.worldgen.lost.regassets.BuildingPartRE;
import mcjty.lostcities.worldgen.lost.regassets.PaletteRE;
import mcjty.lostcities.worldgen.lost.regassets.data.PaletteEntry;
import net.minecraft.util.Formatting;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.text.Text;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.block.BlockState;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CommandExportPart implements Command<ServerCommandSource> {

    private static final CommandExportPart CMD = new CommandExportPart();

    public static ArgumentBuilder<ServerCommandSource, ?> register(CommandDispatcher<ServerCommandSource> dispatcher) {
        return CommandManager.literal("exportpart")
                .requires(cs -> cs.hasPermissionLevel(1))
                .then(CommandManager.argument("name", StringArgumentType.word()).executes(CMD));
    }


    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String filename = context.getArgument("name", String.class);
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        EditorInfo editorInfo = EditorInfo.getEditorInfo(player.getUUID());
        if (editorInfo == null) {
            context.getSource().sendError(Text.literal("You are not editing anything!").formatted(Formatting.RED));
            return 0;
        }

        BuildingPart part = AssetRegistries.PARTS.get(context.getSource().getWorld(), editorInfo.getPartName());
        if (part == null) {
            context.getSource().sendError(Text.literal("Error finding part '" + editorInfo.getPartName() + "'!").formatted(Formatting.RED));
            return 0;
        }

        BlockPos start = editorInfo.getBottomLocation();

        ServerWorld level = (ServerWorld) player.getServerWorld();
        IDimensionInfo dimInfo = Registration.LOSTCITY_FEATURE.get().getDimensionInfo(level);
        if (dimInfo == null) {
            context.getSource().sendError(TextFactory.literal("This dimension doesn't support Lost Cities!"));
            return 0;
        }

        ChunkCoord coord = new ChunkCoord(dimInfo.getType(), start.getX() >> 4, start.getZ() >> 4);
        BuildingInfo info = BuildingInfo.getBuildingInfo(coord, dimInfo);
        CompiledPalette palette = info.getCompiledPalette();
        Palette partPalette = part.getLocalPalette(level);
        Palette buildingPalette = info.getBuilding().getLocalPalette(level);
        if (partPalette != null || buildingPalette != null) {
            palette = new CompiledPalette(palette, partPalette, buildingPalette);
        }

        Map<BlockState, Character> unknowns = new HashMap<>();

        List<List<String>> slices = new ArrayList<>();
        Set<Character> usedCharacters = new HashSet<>(palette.getCharacters());
        StringBuilder chars = new StringBuilder("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()_+-=[]{}|;:'<>,.?/`~");

        // Add various unicode characters
        for (int i = 0x0370; i < 0x0400; i++) {
            chars.append((char) i);
        }
        for (int i = 0x0400; i < 0x0500; i++) {
            chars.append((char) i);
        }
        String possibleChars = chars.toString();

        for (int y = 0 ; y < part.getSliceCount() ; y++) {
            List<String> yslice = new ArrayList<>();
            for (int z = 0; z < part.getZSize(); z++) {
                StringBuilder b = new StringBuilder();
                for (int x = 0; x < part.getXSize(); x++) {
                    BlockPos pos = info.getRelativePos(x, start.getY()+y, z);
                    BlockState state = level.getBlockState(pos);
                    Character c = editorInfo.getPaleteEntry(state);
                    if (c == null) {
                        c = unknowns.get(state);
                    }
                    if (c == null) {
                        // New state!
                        // Find a character that is not yet used
                        for (int i = 0 ; i < possibleChars.length() ; i++) {
                            char cc = possibleChars.charAt(i);
                            if (!usedCharacters.contains(cc)) {
                                c = cc;
                                break;
                            }
                        }
                        unknowns.put(state, c);
                        usedCharacters.add(c);
                    }
                    b.append(c);
                }
                yslice.add(b.toString());
            }
            slices.add(yslice);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        JsonObject root = new JsonObject();

        if (!unknowns.isEmpty()) {
            List<PaletteEntry> entries = new ArrayList<>();
            for (Map.Entry<BlockState, Character> entry : unknowns.entrySet()) {
                entries.add(new PaletteEntry(Character.toString(entry.getValue()), Optional.of(Tools.stateToString(entry.getKey())),
                        Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                        Optional.empty()));
            }
            PaletteRE paletteRE = new PaletteRE(entries);
            DataResult<JsonElement> result = PaletteRE.CODEC.encodeStart(JsonOps.INSTANCE, paletteRE);
            root.add("__comment__", new JsonPrimitive("'missingpalette' represents all blockstates that it couldn't find in the palette. These have to be put in a palette. " +
                    "'exportedpart' is the actual exported part"));
            root.add("missingpalette", result.result().get());
        } else {
            root.add("__comment__", new JsonPrimitive("'exportedpart' is the actual exported part"));
        }

        BuildingPartRE buildingPartRE = new BuildingPartRE(part.getXSize(), part.getZSize(), slices,
                Optional.ofNullable(part.getRefPaletteName()), Optional.empty(), Optional.empty());
        DataResult<JsonElement> result = BuildingPartRE.CODEC.encodeStart(JsonOps.INSTANCE, buildingPartRE);
        root.add("exportedpart", result.result().get());

        String json = gson.toJson(root);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename, StandardCharsets.UTF_8));
            writer.write(json);
            writer.close();
            context.getSource().sendFeedback(() -> TextFactory.literal("Exported part to '" + filename + "'!"), false);
        } catch (IOException e) {
            context.getSource().sendError(Text.literal("Error writing file '" + filename + "'!").formatted(Formatting.RED));
        }

        return 0;
    }
}
