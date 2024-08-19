package mcjty.lostcities.worldgen.lost.regassets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class PredefinedSphereRE implements IAsset<PredefinedSphereRE> {

    public static final Codec<PredefinedSphereRE> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("dimension").forGetter(l -> l.dimension),
                    Codec.INT.fieldOf("chunkx").forGetter(l -> l.chunkX),
                    Codec.INT.fieldOf("chunkz").forGetter(l -> l.chunkZ),
                    Codec.INT.fieldOf("centerx").forGetter(l -> l.centerX),
                    Codec.INT.fieldOf("centerz").forGetter(l -> l.centerZ),
                    Codec.INT.fieldOf("radius").forGetter(l -> l.radius)
            ).apply(instance, PredefinedSphereRE::new));

    private Identifier name;

    private final String dimension;
    private final int chunkX;
    private final int chunkZ;
    private final int centerX;
    private final int centerZ;
    private final int radius;

    public PredefinedSphereRE(
            String dimension,
            int chunkX, int chunkZ, int centerX, int centerZ, int radius) {
        this.dimension = dimension;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.radius = radius;
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

    public int getCenterX() {
        return centerX;
    }

    public int getCenterZ() {
        return centerZ;
    }

    public int getRadius() {
        return radius;
    }

    @Override
    public PredefinedSphereRE setRegistryName(Identifier name) {
        this.name = name;
        return this;
    }

    @Nullable
    public Identifier getRegistryName() {
        return name;
    }
}
