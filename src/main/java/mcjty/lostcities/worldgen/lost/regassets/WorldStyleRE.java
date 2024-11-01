package mcjty.lostcities.worldgen.lost.regassets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lostcities.worldgen.lost.regassets.data.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

public class WorldStyleRE implements IAsset<WorldStyleRE> {

    public static final Codec<WorldStyleRE> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("outsidestyle").forGetter(l -> l.outsideStyle),
                    MultiSettings.CODEC.optionalFieldOf("multisettings").forGetter(l -> l.multiSettings.get()),
                    WorldSettings.CODEC.optionalFieldOf("settings").forGetter(l -> l.worldSettings.get()),
                    CitySphereSettings.CODEC.optionalFieldOf("cityspheres").forGetter(l -> Optional.ofNullable(l.citysphereSettings)),
                    ScatteredSettings.CODEC.optionalFieldOf("scattered").forGetter(l -> Optional.ofNullable(l.scatteredSettings)),
                    PartSelector.CODEC.optionalFieldOf("parts").forGetter(l -> l.partSelector.get()),
                    Codec.list(CityStyleSelector.CODEC).fieldOf("citystyles").forGetter(l -> l.cityStyleSelectors),
                    Codec.list(CityBiomeMultiplier.CODEC).optionalFieldOf("citybiomemultipliers").forGetter(l -> Optional.ofNullable(l.cityBiomeMultipliers))
            ).apply(instance, WorldStyleRE::new));

    private ResourceLocation name;
    private final String outsideStyle;
    private final MultiSettings multiSettings;
    private final WorldSettings worldSettings;
    private final ScatteredSettings scatteredSettings;
    private final CitySphereSettings citysphereSettings;
    @Nonnull private final PartSelector partSelector;
    private final List<CityStyleSelector> cityStyleSelectors;
    private final List<CityBiomeMultiplier> cityBiomeMultipliers;

    public WorldStyleRE(String outsideStyle,
                        Optional<MultiSettings> multiSettings,
                        Optional<WorldSettings> worldSettings,
                        Optional<CitySphereSettings> citysphereSettings,
                        Optional<ScatteredSettings> scatteredSettings,
                        Optional<PartSelector> partSelector,
                        List<CityStyleSelector> cityStyleSelector,
                        Optional<List<CityBiomeMultiplier>> cityBiomeMultipliers) {
        this.outsideStyle = outsideStyle;
        this.multiSettings = multiSettings.orElse(MultiSettings.DEFAULT);
        this.worldSettings = worldSettings.orElse(WorldSettings.DEFAULT);
        this.citysphereSettings = citysphereSettings.orElse(null);
        this.scatteredSettings = scatteredSettings.orElse(null);
        this.partSelector = partSelector.orElse(PartSelector.DEFAULT);
        this.cityStyleSelectors = cityStyleSelector;
        this.cityBiomeMultipliers = cityBiomeMultipliers.orElse(null);
    }

    public String getOutsideStyle() {
        return outsideStyle;
    }

    @Nonnull
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

    public List<CityStyleSelector> getCityStyleSelectors() {
        return cityStyleSelectors;
    }

    public List<CityBiomeMultiplier> getCityBiomeMultipliers() {
        return cityBiomeMultipliers;
    }

    @Nonnull
    public MultiSettings getMultiSettings() {
        return multiSettings;
    }

    @Nonnull
    public WorldSettings getWorldSettings() {
        return worldSettings;
    }

    @Override
    public WorldStyleRE setRegistryName(ResourceLocation name) {
        this.name = name;
        return this;
    }

    @Nullable
    public ResourceLocation getRegistryName() {
        return name;
    }
}
