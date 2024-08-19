package mcjty.lostcities.worldgen.lost.cityassets;

import mcjty.lostcities.api.ILostCityAsset;
import mcjty.lostcities.worldgen.lost.regassets.PredefinedCityRE;
import mcjty.lostcities.worldgen.lost.regassets.data.DataTools;
import mcjty.lostcities.worldgen.lost.regassets.data.PredefinedBuilding;
import mcjty.lostcities.worldgen.lost.regassets.data.PredefinedStreet;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class PredefinedCity implements ILostCityAsset {

    private final Identifier name;
    private final RegistryKey<World> dimension;
    private final int chunkX;
    private final int chunkZ;
    private final int radius;
    private final String cityStyle;
    private final List<PredefinedBuilding> predefinedBuildings = new ArrayList<>();
    private final List<PredefinedStreet> predefinedStreets = new ArrayList<>();

    public PredefinedCity(PredefinedCityRE object) {
        name = object.getRegistryName();
        dimension = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(object.getDimension()));
        chunkX = object.getChunkX();
        chunkZ = object.getChunkZ();
        radius = object.getRadius();
        cityStyle = object.getCityStyle();
        if (object.getPredefinedBuildings() != null) {
            predefinedBuildings.addAll(object.getPredefinedBuildings());
        }
        if (object.getPredefinedStreets() != null) {
            predefinedStreets.addAll(object.getPredefinedStreets());
        }
    }

    public RegistryKey<World> getDimension() {
        return dimension;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public int getRadius() {
        return radius;
    }

    public String getCityStyle() {
        return cityStyle;
    }

    public List<PredefinedBuilding> getPredefinedBuildings() {
        return predefinedBuildings;
    }

    public List<PredefinedStreet> getPredefinedStreets() {
        return predefinedStreets;
    }

    @Override
    public String getName() {
        return DataTools.toName(name);
    }

    @Override
    public Identifier getId() {
        return name;
    }
}
