package mcjty.lostcities.worldgen.lost.cityassets;

import mcjty.lostcities.api.ILostCityAsset;
import mcjty.lostcities.worldgen.lost.regassets.StuffSettingsRE;
import mcjty.lostcities.worldgen.lost.regassets.data.DataTools;
import net.minecraft.util.Identifier;

public class StuffObject implements ILostCityAsset {

    private final Identifier name;
    private final StuffSettingsRE settings;

    public StuffObject(StuffSettingsRE settings) {
        this.settings = settings;
        this.name = settings.getRegistryName();
    }

    @Override
    public String getName() {
        return DataTools.toName(name);
    }

    @Override
    public Identifier getId() {
        return name;
    }

    public StuffSettingsRE getSettings() {
        return settings;
    }
}
