package mcjty.lostcities.worldgen;

import mcjty.lostcities.setup.Registration;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.world.ChunkRegion;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraftforge.common.Tags;

public class LostCitySphereFeature extends Feature<DefaultFeatureConfig> {

    public LostCitySphereFeature() {
        super(DefaultFeatureConfig.CODEC);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess level = context.getWorld();
        if (level instanceof ChunkRegion) {
            IDimensionInfo diminfo = Registration.LOSTCITY_FEATURE.get().getDimensionInfo(level);
            if (diminfo != null) {
                ChunkRegion region = (ChunkRegion) level;
                ChunkPos center = region.getCenterPos();
                RegistryEntry<Biome> biome = region.getBiome(center.getCenterAtY(60));
                if (biome.isIn(BiomeTags.IS_END)) {
                    return false;
                }

                int chunkX = center.x;
                int chunkZ = center.z;
                diminfo.setWorld(level);
                diminfo.getFeature().generateSpheres(region, region.getChunk(chunkX, chunkZ));
                return true;
            }
        }
        return false;
    }
}
