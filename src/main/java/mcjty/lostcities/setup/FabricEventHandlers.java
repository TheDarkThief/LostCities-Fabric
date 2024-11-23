package mcjty.lostcities.setup;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.commands.ModCommandManager;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.playerdata.PlayerProperties;
import mcjty.lostcities.playerdata.PropertiesDispatcher;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.varia.TextFactory;
import mcjty.lostcities.varia.CustomTeleporter;
import mcjty.lostcities.varia.WorldTools;
import mcjty.lostcities.worldgen.GlobalTodo;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.*;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import mcjty.lostcities.worldgen.lost.cityassets.PredefinedCity;
import mcjty.lostcities.worldgen.lost.cityassets.PredefinedSphere;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.Biome;

import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.world.level.ServerWorldProperties;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarting;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStopping;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;

import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandManagerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistryKeys;

import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;

import static mcjty.lostcities.setup.Registration.LOSTCITY;

public class FabricEventHandlers {

    private final Map<RegistryKey<World>, BlockPos> spawnPositions = new HashMap<>();

    public void bindEvets(){
        ServerLifecycleEvents.SERVER_STARTED.register(FabricEventHandlers::onServerStarting);
        ServerLifecycleEvents.SERVER_STOPPING.register(FabricEventHandlers::onServerStopping);
        EntitySleepEvents.START_SLEEPING.register(FabricEventHandlers::onPlayerSleepInBedEvent);
    }

    public 

    @SubscribeEvent
    public void commandRegister(RegisterCommandManagerEvent event) {
        ModCommandManager.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onEntityConstructing(AttachCapabilitiesEvent<Entity> event){
        if (event.getObject() instanceof PlayerEntity) {
            if (!event.getObject().getCapability(PlayerProperties.PLAYER_SPAWN_SET).isPresent()) {
                event.addCapability(Identifier.of(LostCities.MODID, "spawnset"), new PropertiesDispatcher());
            }
        }
    }

    @SubscribeEvent
    public void onPlayerCloned(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            // We need to copyFrom the capabilities
            event.getOriginal().getCapability(PlayerProperties.PLAYER_SPAWN_SET).ifPresent(oldStore -> {
                event.getEntity().getCapability(PlayerProperties.PLAYER_SPAWN_SET).ifPresent(newStore -> {
                    newStore.copyFrom(oldStore);
                });
            });
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        event.getEntity().getCapability(PlayerProperties.PLAYER_SPAWN_SET).ifPresent(note -> {
            if (!note.isPlayerSpawnSet()) {
                note.setPlayerSpawnSet(true);
                for (Map.Entry<RegistryKey<World>, BlockPos> entry : spawnPositions.entrySet()) {
                    if (event.getEntity() instanceof ServerPlayerEntity serverPlayer) {
                        serverPlayer.setRespawnPosition(entry.getKey(), entry.getValue(), 0.0f, true, true);
                        serverPlayer.teleportTo(entry.getValue().getX(), entry.getValue().getY(), entry.getValue().getZ());
                    }
                }
            }
        });
    }



    @SubscribeEvent
    public void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.level instanceof ServerWorld serverLevel) {
            AssetRegistries.load(serverLevel);
            GlobalTodo.get(event.level).executeAndClearTodo(serverLevel);
        }
    }

    
    public static void onServerStarting(MinecraftServer event) {
        cleanUp();
    }

    public static void onServerStopping(MinecraftServer event) {
        cleanUp();
        Config.reset();
    }

    public static void cleanUp() {
        Config.resetProfileCache();
        BuildingInfo.cleanCache();
        MultiChunk.cleanCache();
        Highway.cleanCache();
        Railway.cleanCache();
        BiomeInfo.cleanCache();
        City.cleanCache();
        CitySphere.cleanCache();
    }

    @SubscribeEvent
    public void onCreateSpawnPoint(WorldEvent.CreateSpawnPosition event) {
        WorldAccess world = event.getWorld();
        if (world instanceof ServerWorld serverLevel) {
            IDimensionInfo dimensionInfo = Registration.LOSTCITY_FEATURE.get().getDimensionInfo(serverLevel);
            if (dimensionInfo == null) {
                return;
            }
            LostCityProfile profile = dimensionInfo.getProfile();

            Predicate<BlockPos> isSuitable = pos -> true;
            boolean needsCheck = false;

            if (!profile.SPAWN_BIOME.isEmpty()) {
                final Biome spawnBiome = ForgeRegistryKeys.BIOMES.getValue(Identifier.of(profile.SPAWN_BIOME));
                if (spawnBiome == null) {
                    ModSetup.getLogger().error("Cannot find biome '{}' for the player to spawn in !", profile.SPAWN_BIOME);
                } else {
                    isSuitable = blockPos -> world.getBiome(blockPos).value() == spawnBiome;
                    needsCheck = true;
                }
            } else if (!profile.SPAWN_CITY.isEmpty()) {
                final PredefinedCity city = AssetRegistries.PREDEFINED_CITIES.get(world, profile.SPAWN_CITY);
                if (city == null) {
                    ModSetup.getLogger().error("Cannot find city '{}' for the player to spawn in !", profile.SPAWN_CITY);
                } else {
                    float sqradius = getSqRadius(city.getRadius(), 0.8f);
                    isSuitable = blockPos -> city.getDimension() == serverLevel.getRegistryKey() &&
                            CitySphere.squaredDistance(city.getChunkX()*16+8, city.getChunkZ()*16+8, blockPos.getX(), blockPos.getZ()) < sqradius;
                    needsCheck = true;
                }
            } else if (!profile.SPAWN_SPHERE.isEmpty()) {
                if ("<in>".equals(profile.SPAWN_SPHERE)) {
                    isSuitable = blockPos -> {
                        ChunkCoord coord = new ChunkCoord(dimensionInfo.getType(), blockPos.getX() >> 4, blockPos.getZ() >> 4);
                        CitySphere sphere = CitySphere.getCitySphere(coord, dimensionInfo);
                        if (!sphere.isEnabled()) {
                            return false;
                        }
                        float sqradius = getSqRadius((int) sphere.getRadius(), 0.8f);
                        return sphere.getCenterPos().getSquaredDistance(blockPos.withY(sphere.getCenterPos().getY())) < sqradius;
                    };
                    needsCheck = true;
                } else if ("<out>".equals(profile.SPAWN_SPHERE)) {
                    isSuitable = blockPos -> {
                        ChunkCoord coord = new ChunkCoord(dimensionInfo.getType(), blockPos.getX() >> 4, blockPos.getZ() >> 4);
                        CitySphere sphere = CitySphere.getCitySphere(coord, dimensionInfo);
                        if (!sphere.isEnabled()) {
                            return true;
                        }
                        float sqradius = sphere.getRadius() * sphere.getRadius();
                        return sphere.getCenterPos().getSquaredDistance(blockPos.withY(sphere.getCenterPos().getY())) > sqradius;
                    };
                    needsCheck = true;
                } else {
                    final PredefinedSphere sphere = AssetRegistries.PREDEFINED_SPHERES.get(world, profile.SPAWN_SPHERE);
                    if (sphere == null) {
                        LostCities.setup.getLogger().error("Cannot find sphere '" + profile.SPAWN_SPHERE + "' for the player to spawn in !");
                    } else {
                        float sqradius = getSqRadius(sphere.getRadius(), 0.8f);
                        isSuitable = blockPos -> sphere.getDimension() == serverLevel.getRegistryKey() &&
                                CitySphere.squaredDistance((sphere.getChunkX() << 4) + 8, (sphere.getChunkZ() << 4) + 8, blockPos.getX(), blockPos.getZ()) < sqradius;
                        needsCheck = true;
                    }
                }
            }

            if (profile.SPAWN_NOT_IN_BUILDING) {
                isSuitable = isSuitable.and(blockPos -> isOutsideBuilding(dimensionInfo, blockPos));
                needsCheck = true;
            } else if (profile.FORCE_SPAWN_IN_BUILDING) {
                isSuitable = isSuitable.and(blockPos -> !isOutsideBuilding(dimensionInfo, blockPos));
                needsCheck = true;
            }

            // Potentially set the spawn point
            switch (profile.LANDSCAPE_TYPE) {
                case DEFAULT, SPHERES -> {
                    if (needsCheck) {
                        BlockPos pos = findSafeSpawnPoint(serverLevel, dimensionInfo, isSuitable, event.getSettings());
                        event.getSettings().setSpawn(pos, 0.0f);
                        spawnPositions.put(serverLevel.getRegistryKey(), pos);
                        event.setCanceled(true);
                    }
                }
                case FLOATING, SPACE, CAVERN -> {
                    BlockPos pos = findSafeSpawnPoint(serverLevel, dimensionInfo, isSuitable, event.getSettings());
                    event.getSettings().setSpawn(pos, 0.0f);
                    spawnPositions.put(serverLevel.getRegistryKey(), pos);
                    event.setCanceled(true);
                }
            }
        }
    }

    private boolean isOutsideBuilding(IDimensionInfo provider, BlockPos pos) {
        ChunkCoord coord = new ChunkCoord(provider.getType(), pos.getX() >> 4, pos.getZ() >> 4);
        BuildingInfo info = BuildingInfo.getBuildingInfo(coord, provider);
        return !(info.isCity() && info.hasBuilding);
    }

    private int getSqRadius(int radius, float pct) {
        return (int) ((radius * pct) * (radius * pct));
    }

    private BlockPos findSafeSpawnPoint(World world, IDimensionInfo provider, @NotNull Predicate<BlockPos> isSuitable,
                                    @NotNull ServerWorldProperties serverLevelData) {
        Random rand = new Random(provider.getSeed());
        int radius = 200;
        int attempts = 0;
//        int bottom = world.getWorldType().getMinimumSpawnHeight(world);
        while (true) {
            for (int i = 0 ; i < 200 ; i++) {
                int x = rand.nextInt(radius * 2) - radius;
                int z = rand.nextInt(radius * 2) - radius;
                attempts++;

                if (!isSuitable.test(new BlockPos(x, 128, z))) {
                    continue;
                }

                ChunkCoord coord = new ChunkCoord(provider.getType(), x >> 4, z >> 4);
                LostCityProfile profile = BuildingInfo.getProfile(coord, provider);

                for (int y = profile.GROUNDLEVEL-5 ; y < 125 ; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (isValidStandingPosition(world, pos)) {
//                        serverLevelData.setSpawn(pos.up(), 0.0f);
                        return pos.up();
                    }
                }
            }
            radius += 100;
            if (attempts > 20000) {
                LostCities.setup.getLogger().error("Can't find a valid spawn position!");
                throw new RuntimeException("Can't find a valid spawn position!");
            }
        }
    }

    private boolean isValidStandingPosition(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (!state.isSideSolidFullSquare(world, pos, Direction.UP)) {
            return false;
        }
        if (state.isOf(Blocks.BEDROCK)) {
            return false;
        }
        if (!world.getBlockState(pos.up()).isAir() || !world.getBlockState(pos.up(2)).isAir()) {
            return false;
        }
        return true;
//        return state.getBlock().isTopSolid(state) && state.getBlock().isFullCube(state) && state.getBlock().isOpaqueCube(state) && world.isAirBlock(pos.up()) && world.isAirBlock(pos.up(2));
//        return state.canOcclude();
    }

    private boolean isValidSpawnBed(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof BedBlock)) {
            return false;
        }
        Direction direction = ((BedBlock)Blocks.BLACK_BED).getBedDirection(state, world, pos);
        Block b1 = world.getBlockState(pos.down()).getBlock();
        Block b2 = world.getBlockState(pos.offset(direction.getOpposite()).down()).getBlock();
        Block b = ForgeRegistryKeys.BLOCKS.getValue(Identifier.of(Config.SPECIAL_BED_BLOCK.get()));
        if (b1 != b || b2 != b) {
            return false;
        }
        // Check if the bed is surrounded by 6 skulls
        if (!(world.getBlockState(pos.offset(direction)).getBlock() instanceof AbstractSkullBlock)) {   // @todo 1.14 other skulls!
            return false;
        }
        if (!(world.getBlockState(pos.offset(direction.rotateYClockwise())).getBlock() instanceof AbstractSkullBlock)) {
            return false;
        }
        if (!(world.getBlockState(pos.offset(direction.rotateYCounterclockwise())).getBlock() instanceof AbstractSkullBlock)) {
            return false;
        }
        if (!(world.getBlockState(pos.offset(direction.getOpposite(), 2)).getBlock() instanceof AbstractSkullBlock)) {
            return false;
        }
        if (!(world.getBlockState(pos.offset(direction.getOpposite()).offset(direction.getOpposite().rotateYClockwise())).getBlock() instanceof AbstractSkullBlock)) {
            return false;
        }
        if (!(world.getBlockState(pos.offset(direction.getOpposite()).offset(direction.getOpposite().rotateYCounterclockwise())).getBlock() instanceof AbstractSkullBlock)) {
            return false;
        }
        return true;
    }

    private BlockPos findValidTeleportLocation(World world, BlockPos start) {
        int chunkX = start.getX()>>4;
        int chunkZ = start.getZ()>>4;
        int y = start.getY();
        BlockPos pos = findValidTeleportLocation(world, chunkX, chunkZ, y);
        if (pos != null) {
            return pos;
        }
        for (int r = 1 ; r < 50 ; r++) {
            for (int i = -r ; i < r ; i++) {
                pos = findValidTeleportLocation(world, chunkX + i, chunkZ - r, y);
                if (pos != null) {
                    return pos;
                }
                pos = findValidTeleportLocation(world, chunkX + r, chunkZ + i, y);
                if (pos != null) {
                    return pos;
                }
                pos = findValidTeleportLocation(world, chunkX + r - i, chunkZ + r, y);
                if (pos != null) {
                    return pos;
                }
                pos = findValidTeleportLocation(world, chunkX - r, chunkZ + r - i, y);
                if (pos != null) {
                    return pos;
                }
            }
        }
        return null;
    }

    private BlockPos findValidTeleportLocation(World world, int chunkX, int chunkZ, int y) {
        BlockPos bestSpot = null;
        for (int dy = 0 ; dy < 255 ; dy++) {
            for (int x = 0 ; x < 16 ; x++) {
                for (int z = 0 ; z < 16 ; z++) {
                    if ((y + dy) < 250) {
                        BlockPos p = new BlockPos((chunkX << 4) + x, y + dy, (chunkZ << 4) + z);
                        if (isValidSpawnBed(world, p)) {
                            return p.up();
                        }
                        if (bestSpot == null && isValidStandingPosition(world, p)) {
                            bestSpot = p.up();
                        }
                    }
                    if ((y - dy) > 1) {
                        BlockPos p = new BlockPos((chunkX << 4) + x, y - dy, (chunkZ << 4) + z);
                        if (isValidSpawnBed(world, p)) {
                            return p.up();
                        }
                        if (bestSpot == null && isValidStandingPosition(world, p)) {
                            bestSpot = p.up();
                        }
                    }
                }
            }
        }
        return bestSpot;
    }


    public void onPlayerSleepInBedEvent(LivingEntity entitySleeping, BlockPos pos) {
//        if (LostCityConfiguration.DIMENSION_ID == null) {
//            return;
//        }
        if(entitySleeping instanceof PlayerEntity){
            World world = entitySleeping.getWorld();
            if (world.isClient) {
                return;
            }
            BlockPos bedLocation = pos;
            if (bedLocation == null || !isValidSpawnBed(world, bedLocation)) {
                return;
            }

            if (world.getRegistryKey() == Registration.DIMENSION) {
                //event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);
                ServerWorld destWorld = WorldTools.getOverworld(world);
                BlockPos location = findLocation(bedLocation, destWorld);
                CustomTeleporter.teleportToDimension((PlayerEntity)entitySleeping, destWorld, location);
            } else {
                //event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);
                ServerWorld destWorld = entitySleeping.getWorld().getServer().getWorld(Registration.DIMENSION);
                if (destWorld == null) {
                    ((PlayerEntity)entitySleeping).sendMessage(TextFactory.literal("Error finding Lost City dimension: " + LOSTCITY + "!").formatted(Formatting.RED));
                } else {
                    BlockPos location = findLocation(bedLocation, destWorld);
                    CustomTeleporter.teleportToDimension((PlayerEntity)entitySleeping, destWorld, location);
                }
            }
        }
    }

    private BlockPos findLocation(BlockPos bedLocation, ServerWorld destWorld) {
        BlockPos top = bedLocation.up(5);//destWorld.getHeight(Heightmap.Type.MOTION_BLOCKING, bedLocation).up(10);
        BlockPos location = top;
        while (top.getY() > 1 && destWorld.getBlockState(location).isAir()) {
            location = location.down();
        }
//        BlockPos location = findValidTeleportLocation(destWorld, top);
        if (destWorld.isAir(location.down())) {
            // No place to teleport
            destWorld.setBlockState(bedLocation, Blocks.COBBLESTONE.getDefaultState());
        }
        return location.up(1);
    }
}
