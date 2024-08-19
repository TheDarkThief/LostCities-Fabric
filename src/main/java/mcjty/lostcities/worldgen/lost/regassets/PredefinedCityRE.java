package mcjty.lostcities.worldgen.lost.regassets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lostcities.worldgen.lost.regassets.data.PredefinedBuilding;
import mcjty.lostcities.worldgen.lost.regassets.data.PredefinedStreet;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class PredefinedCityRE implements IAsset<PredefinedCityRE> {

    public static final Codec<PredefinedCityRE> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("dimension").forGetter(l -> l.dimension),
                    Codec.INT.fieldOf("chunkx").forGetter(l -> l.chunkX),
                    Codec.INT.fieldOf("chunkz").forGetter(l -> l.chunkZ),
                    Codec.INT.fieldOf("radius").forGetter(l -> l.radius),
                    Codec.STRING.fieldOf("citystyle").forGetter(l -> l.cityStyle),
                    Codec.list(PredefinedBuilding.CODEC).optionalFieldOf("buildings").forGetter(l -> Optional.ofNullable(l.predefinedBuildings)),
                    Codec.list(PredefinedStreet.CODEC).optionalFieldOf("streets").forGetter(l -> Optional.ofNullable(l.predefinedStreets))
            ).apply(instance, PredefinedCityRE::new));

    private Identifier name;

    private final String dimension;
    private final int chunkX;
    private final int chunkZ;
    private final int radius;
    private final String cityStyle;
    private final List<PredefinedBuilding> predefinedBuildings;
    private final List<PredefinedStreet> predefinedStreets;

    public PredefinedCityRE(
            String dimension,
            int chunkX, int chunkZ, int radius,
            String cityStyle,
            Optional<List<PredefinedBuilding>> predefinedBuildings,
            Optional<List<PredefinedStreet>> predefinedStreets) {
        this.dimension = dimension;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.radius = radius;
        this.cityStyle = cityStyle;
        this.predefinedBuildings = predefinedBuildings.orElse(null);
        this.predefinedStreets = predefinedStreets.orElse(null);
    }

    public String getDimension() {
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
    public PredefinedCityRE setRegistryName(Identifier name) {
        this.name = name;
        return this;
    }

    @Nullable
    public Identifier getRegistryName() {
        return name;
    }
}
