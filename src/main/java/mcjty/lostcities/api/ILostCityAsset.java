package mcjty.lostcities.api;

import net.minecraft.util.Identifier;
import net.minecraft.world.RegistryWorldView;

public interface ILostCityAsset {

    // Called after the asset is fetched from the registry
    default void init(RegistryWorldView world) {}

    String getName();

    Identifier getId();
}
