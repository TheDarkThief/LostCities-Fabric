package mcjty.lostcities.worldgen.lost.cityassets;

import mcjty.lostcities.api.ILostCityAsset;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.varia.Tools;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.BiomeInfo;
import mcjty.lostcities.worldgen.lost.regassets.WorldStyleRE;
import mcjty.lostcities.worldgen.lost.regassets.data.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class WorldStyle implements ILostCityAsset {

    private final Identifier name;
    private final String outsideStyle;

    private final CitySphereSettings citysphereSettings;
    private final ScatteredSettings scatteredSettings;
    @NotNull private final PartSelector partSelector;
    private final List<Pair<Predicate<RegistryEntry<Biome>>, Pair<Float, String>>> cityStyleSelector = new ArrayList<>();
    private final List<Pair<Predicate<RegistryEntry<Biome>>, Float>> cityBiomeMultiplier = new ArrayList<>();
    private final MultiSettings multiSettings;

    public WorldStyle(WorldStyleRE object) {
        name = object.getRegistryName();
        this.citysphereSettings = object.getCitysphereSettings();
        this.scatteredSettings = object.getScatteredSettings();
        this.partSelector = object.getPartSelector();
        this.multiSettings = object.getMultiSettings();
        outsideStyle = object.getOutsideStyle();
        for (CityStyleSelector selector : object.getCityStyleSelectors()) {
            Predicate<RegistryEntry<Biome>> predicate = biomeRegistryEntry -> true;
            if (selector.biomeMatcher() != null) {
                predicate = selector.biomeMatcher();
            }
            cityStyleSelector.add(Pair.of(predicate, Pair.of(selector.factor(), selector.citystyle())));
        }
        if (object.getCityBiomeMultipliers() != null) {
            for (CityBiomeMultiplier multiplier : object.getCityBiomeMultipliers()) {
                cityBiomeMultiplier.add(Pair.of(multiplier.biomeMatcher(), multiplier.multiplier()));
            }
        }
    }

    @Override
    public String getName() {
        return DataTools.toName(name);
    }

    @Override
    public Identifier getId() {
        return name;
    }

    public String getOutsideStyle() {
        return outsideStyle;
    }

    @NotNull
    public PartSelector getPartSelector() {
        return partSelector;
    }

    public CitySphereSettings getCitysphereSettings() {
        return citysphereSettings;
    }

    @Nullable
    public ScatteredSettings getScatteredSettings() {
        return scatteredSettings;
    }

    public MultiSettings getMultiSettings() {
        return multiSettings;
    }

    public float getCityChanceMultiplier(IDimensionInfo provider, ChunkCoord coord) {
        RegistryEntry<Biome> biome = BiomeInfo.getBiomeInfo(provider, coord).getMainBiome();
        for (Pair<Predicate<RegistryEntry<Biome>>, Float> pair : cityBiomeMultiplier) {
            if (pair.getLeft().test(biome)) {
                return pair.getRight();
            }
        }
        return 1.0f;
    }

    public String getRandomCityStyle(IDimensionInfo provider, ChunkCoord coord, Random random) {
        RegistryEntry<Biome> biome = BiomeInfo.getBiomeInfo(provider, coord).getMainBiome();
        List<Pair<Float, String>> ct = new ArrayList<>();
        for (Pair<Predicate<RegistryEntry<Biome>>, Pair<Float, String>> pair : cityStyleSelector) {
            if (pair.getKey().test(biome)) {
                ct.add(pair.getValue());
            }
        }

        Pair<Float, String> randomFromList = Tools.getRandomFromList(random, ct, Pair::getLeft);
        if (randomFromList == null) {
            return null;
        } else {
            return randomFromList.getRight();
        }
    }
}
