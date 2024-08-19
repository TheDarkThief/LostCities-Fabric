package mcjty.lostcities.setup;


import mcjty.lostcities.LostCities;
import mcjty.lostcities.worldgen.LostCityFeature;
import mcjty.lostcities.worldgen.LostCitySphereFeature;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.SimpleRegistry;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;



public class Registration {

    // public static final SimpleRegistry FEATURES = FabricRegistryBuilder.createSimple(RegistryKey.ofRegistry(Identifier.of(LostCities.MODID, "lostcity"))).buildAndRegister(); 

    public static void init() {
        LostCities.LOGGER.info("SUP BITCHES");
    }

    // public static final Registry<LostCityFeature> LOSTCITY_FEATURE = FEATURES.createEntry("lostcity", LostCityFeature::new);
    // public static final Registry<LostCitySphereFeature> LOSTCITY_SPHERE_FEATURE = FEATURES.createEntry("spheres", LostCitySphereFeature::new);

    public static final Identifier LOSTCITY = Identifier.of(LostCities.MODID, "lostcity");

    
    // public static final RegistryKey<DimensionType> DIMENSION_TYPE = RegistryKey.of(RegistryKeys.DIMENSION_TYPE, LOSTCITY);
    // public static final RegistryKey<World> DIMENSION = RegistryKey.of(RegistryKeys.WORLD, LOSTCITY);


}
