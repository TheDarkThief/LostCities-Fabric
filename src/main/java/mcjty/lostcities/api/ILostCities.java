package mcjty.lostcities.api;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;


/**
 * Main interface for this mod. Use this to get city information
 * Get a reference to an implementation of this interface by calling:
 *         InterModComms.sendTo(ILostCities.LOSTCITIES, ILostCities.GET_LOST_CITIES, ModSetup.GetLostCities::new);
 */
public interface ILostCities {

    // MODID for Lost Cities
    String LOSTCITIES = "lostcities";

    // IMC message for getting ILostCities
    String GET_LOST_CITIES = "getLostCities";
    // IMC message for getting ILostCitiesPre
    String GET_LOST_CITIES_PRE = "getLostCitiesPre";

    // Meta values that you can use in assets
    String META_DONTCONNECT = "dontconnect";
    String META_SUPPORT = "support";
    String META_Z_1 = "z1";
    String META_Z_2 = "z2";
    String META_NOWATER = "nowater";

    /**
     * Get Lost City information for a given dimension. Returns null if the dimension doesn't support Lost Cities
     */
    @Nullable
    ILostCityInformation getLostInfo(World world);

    /**
     * Register a lost city profile with a dimension. Note that this is not remembered!
     * You need to do this again after loading your world. Preferably in the chunkGenerator
     * (for example in buildSurface)
     */
    void registerDimension(RegistryKey<World> key, String profile);

    /**
     * Call this client-side(!) before a world is created. It allows you to set the profile for the overworld
     */
    void setOverworldProfile(String profile);
}
