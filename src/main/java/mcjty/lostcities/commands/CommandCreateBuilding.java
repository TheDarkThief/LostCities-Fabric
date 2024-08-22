package mcjty.lostcities.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.lostcities.setup.Registration;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.varia.TextFactory;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import mcjty.lostcities.worldgen.lost.cityassets.*;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.DefaultPosArgument;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Identifier;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;

import static mcjty.lostcities.worldgen.LostCityTerrainFeature.FLOORHEIGHT;

public class CommandCreateBuilding implements Command<ServerCommandSource> {

    private static final CommandCreateBuilding CMD = new CommandCreateBuilding();

    public static ArgumentBuilder<ServerCommandSource, ?> register(CommandDispatcher<ServerCommandSource> dispatcher) {
        return CommandManager.literal("createbuilding")
                .requires(cs -> cs.hasPermissionLevel(1))
                .then(CommandManager.argument("name", IdentifierArgumentType.identifier())
                        .suggests(ModCommandManager.getBuildingSuggestionProvider())
                        .then(CommandManager.argument("floors", IntegerArgumentType.integer(1, 20))
                                .then(CommandManager.argument("cellars", IntegerArgumentType.integer(0, 10))
                                        .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                                .executes(CMD)))));
    }


    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Identifier name = context.getArgument("name", Identifier.class);
        Integer floors = context.getArgument("floors", Integer.class);
        Integer cellars = context.getArgument("cellars", Integer.class);
        Building building = AssetRegistries.BUILDINGS.get(context.getSource().getWorld(), name);
        if (building == null) {
            context.getSource().sendError(TextFactory.literal("Cannot find building: " + name + "!"));
            return 0;
        }

        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        ServerWorld level = (ServerWorld) player.getWorld();
        DefaultPosArgument pos = context.getArgument("pos", DefaultPosArgument.class);
        BlockPos bottom = pos.toAbsoluteBlockPos(context.getSource());

        IDimensionInfo dimInfo = Registration.LOSTCITY_FEATURE.get().getDimensionInfo(level);
        if (dimInfo == null) {
            context.getSource().sendError(TextFactory.literal("This dimension doesn't support Lost Cities!"));
            return 0;
        }
        ChunkCoord coord = new ChunkCoord(level.getRegistryKey(), bottom.getX() >> 4, bottom.getZ() >> 4);
        BuildingInfo info = BuildingInfo.getBuildingInfo(coord, dimInfo);
        info.setBuildingType(building, cellars, floors, bottom.getY());

        ChunkPos cp = new ChunkPos(bottom);

        int height = bottom.getY();
        for (int y = height ; y < level.getHeight() ; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    level.setBlockState(cp.getBlockPos(x, y, z), Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                }
            }
        }

        for (int f = -info.cellars; f <= info.getNumFloors(); f++) {
            BuildingPart part = info.getFloor(f);

            generatePart(level, cp, info, part, height);
            part = info.getFloorPart2(f);
            if (part != null) {
                generatePart(level, cp, info, part, height);
            }

            height += FLOORHEIGHT;    // We currently only support 6 here
        }

        return 0;
    }

    private static void generatePart(World level, ChunkPos cp, BuildingInfo info, IBuildingPart part, int oy) {
        CompiledPalette compiledPalette = info.getCompiledPalette();
        // Cache the combined palette?
        Palette partPalette = part.getLocalPalette(level);
        Palette buildingPalette = info.getBuilding().getLocalPalette(level);
        if (partPalette != null || buildingPalette != null) {
            compiledPalette = new CompiledPalette(compiledPalette, partPalette, buildingPalette);
        }

        boolean nowater = part.getMetaBoolean("nowater");
        BlockPos.Mutable current = new BlockPos.Mutable();

        for (int x = 0; x < part.getXSize(); x++) {
            for (int z = 0; z < part.getZSize(); z++) {
                char[] vs = part.getVSlice(x, z);
                if (vs != null) {
                    int rx = cp.getOffsetX(x);
                    int rz = cp.getOffsetZ(z);
                    current.set(rx, oy, rz);
                    for (char c : vs) {
                        BlockState b = compiledPalette.get(c);
                        if (b == null) {
                            throw new RuntimeException("Could not find entry '" + c + "' in the palette for part '" + part.getName() + "'!");
                        }
                        level.setBlockState(current, b, Block.NOTIFY_LISTENERS);
                        current.setY(current.getY() + 1);
                    }
                }
            }
        }
    }


}
