package mcjty.lostcities.api;

import net.minecraft.util.Identifier;
import net.minecraft.world.RegistryWorldView;

public interface ILostCityAssetRegistry<T extends ILostCityAsset> {

    T get(RegistryWorldView level, String name);

    T get(RegistryWorldView level, Identifier name);

    Iterable<T> getIterable();
}
