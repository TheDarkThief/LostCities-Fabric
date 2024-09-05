package mcjty.lostcities.api;

import net.minecraft.util.Identifier;

public class LostChunkCharacteristics {
    public boolean isCity;
    public boolean couldHaveBuilding;   // True if this chunk could contain a building
    public MultiPos multiPos;           // Equal to SINGLE if a single building
    public int cityLevel;               // 0 is lowest city level
    public Identifier cityStyleId;
    public ILostCityCityStyle cityStyle;
    public Identifier multiBuildingId;
    public ILostCityMultiBuilding multiBuilding;
    public Identifier buildingTypeId;
    public ILostCityBuilding buildingType;
}
