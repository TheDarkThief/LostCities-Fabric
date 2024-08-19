package mcjty.lostcities.api;

import mcjty.lostcities.worldgen.lost.cityassets.Palette;
import net.minecraft.world.RegistryWorldView;

import org.jetbrains.annotations.Nullable;

public interface ILostCityBuilding extends ILostCityAsset {

    Palette getLocalPalette(RegistryWorldView level);

    /**
     * The chance this this building is alone. If 1.0f this building wants to be alone all the time. If 0.0f (default)
     * then the building does not care.
     */
    float getPrefersLonely();

    /**
     * Maximum number of floors for this type of building.
     */
    int getMaxFloors();

    /**
     * Maximum number of cellars for this type of building.
     */
    int getMaxCellars();

    int getMinFloors();

    int getMinCellars();

    /*
     * Allow the generation of doors.
     */
    public Boolean getAllowDoors();

    /*
     * Allow the generation of filler.
     */
    public Boolean getAllowFillers();
  
    /**
     * The filler block (from the palette) used to do procedural generation of extra
     * features (like the blocks around a door)
     */
    char getFillerBlock();

    /**
     * The rubble block (from the palette) used to generate debris to adjacent
     * chunks when this building is destroyed. If this is null getFillerBlock()
     * should be used
     */
    @Nullable
    Character getRubbleBlock();
}
