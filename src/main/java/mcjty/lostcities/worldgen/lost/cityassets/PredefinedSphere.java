package mcjty.lostcities.worldgen.lost.cityassets;

import mcjty.lostcities.api.ILostCityAsset;
import mcjty.lostcities.worldgen.lost.regassets.PredefinedSphereRE;
import mcjty.lostcities.worldgen.lost.regassets.data.DataTools;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class PredefinedSphere implements ILostCityAsset {

    private final Identifier name;
    private final RegistryKey<World> dimension;
    private final int chunkX;
    private final int chunkZ;
    private final int centerX;
    private final int centerZ;
    private final int radius;

    public PredefinedSphere(PredefinedSphereRE object) {
        name = object.getRegistryName();
        dimension = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(object.getDimension()));
        chunkX = object.getChunkX();
        chunkZ = object.getChunkZ();
        centerX = object.getCenterX();
        centerZ = object.getCenterZ();
        radius = object.getRadius();
    }

    @Override
    public String getName() {
        return DataTools.toName(name);
    }

    @Override
    public Identifier getId() {
        return name;
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

    public int getCenterX() {
        return centerX;
    }

    public int getCenterZ() {
        return centerZ;
    }

    public int getRadius() {
        return radius;
    }
}