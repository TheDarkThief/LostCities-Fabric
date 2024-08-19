package mcjty.lostcities.worldgen.lost.cityassets;

import mcjty.lostcities.api.ILostCityAsset;
import mcjty.lostcities.api.ILostCityAssetRegistry;
import mcjty.lostcities.worldgen.lost.regassets.IAsset;
import mcjty.lostcities.worldgen.lost.regassets.data.DataTools;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.RegistryWorldView;

import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class RegistryAssetRegistry<T extends ILostCityAsset, R> implements ILostCityAssetRegistry<T>  {

    private final Map<Identifier, T> assets = new HashMap<>();
    private final RegistryKey<Registry<R>> registryKey;
    private final Function<R, T> assetConstructor;

    public <S extends ILostCityAsset> ILostCityAssetRegistry<S> cast() {
        return (ILostCityAssetRegistry<S>) this;
    }

    public RegistryAssetRegistry(RegistryKey<Registry<R>> registryKey, Function<R, T> assetConstructor) {
        this.registryKey = registryKey;
        this.assetConstructor = assetConstructor;
    }

    @Override
    public T get(RegistryWorldView level, String name) {
        if (name == null) {
            return null;
        }
        return get(level, DataTools.fromName(name));
    }

    @NotNull
    public T getOrThrow(RegistryWorldView level, String name) {
        if (name == null) {
            throw new RuntimeException("Invalid name given to " + registryKey.getRegistry() + " getOrThrow!");
        }
        T result = get(level, DataTools.fromName(name));
        if (result == null) {
            throw new RuntimeException("Can't find '" + name + "' in " + registryKey.getRegistry() + "!");
        }
        return result;
    }

    @Override
    public T get(RegistryWorldView level, Identifier name) {
        if (name == null) {
            return null;
        }
        T t = assets.get(name);
        if (t == null) {
            try {
                Registry<R> registry = level.getRegistryManager().get(registryKey);
                R value = registry.get(RegistryKey.of(registryKey, name));
                if (value instanceof IAsset asset) {
                    asset.setRegistryName(name);
                }
                t = assetConstructor.apply(value);
            } catch (Exception e) {
                throw new RuntimeException("Error getting resource " + name + "!", e);
            }
            assets.put(name, t);
        }
        if (t != null) {
            t.init(level);
        }
        return t;
    }

    public void loadAll(RegistryWorldView level) {
        Registry<R> registry = level.getRegistryManager().get(registryKey);
        for (R r : registry) {
            Identifier name = registry.getId(r);
            if (!assets.containsKey(name)) {
                if (r instanceof IAsset asset) {
                    asset.setRegistryName(name);
                }
                T t = assetConstructor.apply(r);
                assets.put(name, t);
            }
        }
    }

    @Override
    public Iterable<T> getIterable() {
        return assets.values();
    }

    public int getNumAssets(RegistryWorldView level) {
        return level.getRegistryManager().get(registryKey).size();
    }

    public void reset() {
        assets.clear();
    }
}
