package mcjty.lostcities.worldgen.lost.regassets;

import net.minecraft.util.Identifier;

public interface IAsset<T extends IAsset> {
    T setRegistryName(Identifier name);
}
