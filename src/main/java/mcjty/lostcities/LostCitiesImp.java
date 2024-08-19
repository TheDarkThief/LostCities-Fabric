package mcjty.lostcities;

import mcjty.lostcities.api.*;
import mcjty.lostcities.gui.GuiLCConfig;
import mcjty.lostcities.setup.Config;
import mcjty.lostcities.setup.Registration;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistryKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.StructureWorldAccess;

import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.Map;

public class LostCitiesImp implements ILostCities {

    private final Map<RegistryKey<World>, LostCityInformation> info = new HashMap<>();

    public void cleanUp() {
        info.clear();
    }

    @Nullable
    @Override
    public ILostCityInformation getLostInfo(World world) {
        IDimensionInfo dimensionInfo = Registration.LOSTCITY_FEATURE.get().getDimensionInfo((StructureWorldAccess) world);
        if (dimensionInfo != null) {
            if (!info.containsKey(world.getRegistryKey())) {
                LostCityInformation gen = new LostCityInformation(dimensionInfo);
                info.put(world.getRegistryKey(), gen);
            }
            return info.get(world.getRegistryKey());
        }
        return null;
    }

    @Override
    public void registerDimension(RegistryKey<World> key, String profile) {
        Config.registerLostCityDimension(key, profile);
    }

    @Override
    public void setOverworldProfile(String profile) {
        GuiLCConfig.selectProfile(profile, null);
    }

    public record LostCityInformation(IDimensionInfo dimensionInfo) implements ILostCityInformation {

        @Override
        public ILostChunkInfo getChunkInfo(int chunkX, int chunkZ) {
            ChunkCoord coord = new ChunkCoord(dimensionInfo.getType(), chunkX, chunkZ);
            return BuildingInfo.getBuildingInfo(coord, dimensionInfo);
        }

        @Override
        public ILostSphere getSphere(int x, int y, int z) {
            if (dimensionInfo.getProfile().isSpheres() || dimensionInfo.getProfile().isSpace()) {
                return BuildingInfo.getSphereInt(x, y, z, dimensionInfo);
            } else {
                return null;
            }
        }

        @Override
        public ILostSphere getSphere(int x, int z) {
            if (dimensionInfo.getProfile().isSpheres() || dimensionInfo.getProfile().isSpace()) {
                return BuildingInfo.getSphereInt(x, z, dimensionInfo);
            } else {
                return null;
            }
        }

        @Override
        public int getRealHeight(int level) {
            return dimensionInfo.getProfile().GROUNDLEVEL + level * 6;
        }

        @Override
        public ILostCityAssetRegistry<ILostCityBuilding> getBuildings() {
            return AssetRegistryKeys.BUILDINGS.cast();
        }

        @Override
        public ILostCityAssetRegistry<ILostCityMultiBuilding> getMultiBuildings() {
            return AssetRegistryKeys.MULTI_BUILDINGS.cast();
        }

        @Override
        public ILostCityAssetRegistry<ILostCityCityStyle> getCityStyles() {
            return AssetRegistryKeys.CITYSTYLES.cast();
        }
    }
}
