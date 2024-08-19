package mcjty.lostcities.worldgen;

import mcjty.lostcities.LostCities;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.block.Block;

public class LostTags {

    public static final Identifier FOLIAGE = Identifier.of(LostCities.MODID, "foliage");
    public static final TagKey<Block> FOLIAGE_TAG = TagKey.of(RegistryKeys.BLOCK, FOLIAGE);

    public static final Identifier ROTATABLE = Identifier.of(LostCities.MODID, "rotatable");
    public static final TagKey<Block> ROTATABLE_TAG = TagKey.of(RegistryKeys.BLOCK, ROTATABLE);

    public static final Identifier EASY_BREAKABLE = Identifier.of(LostCities.MODID, "easybreakable");
    public static final TagKey<Block> EASY_BREAKABLE_TAG = TagKey.of(RegistryKeys.BLOCK, EASY_BREAKABLE);

    public static final Identifier NOT_BREAKABLE = Identifier.of(LostCities.MODID, "notbreakable");
    public static final TagKey<Block> NOT_BREAKABLE_TAG = TagKey.of(RegistryKeys.BLOCK, NOT_BREAKABLE);

    public static final Identifier LIGHTS = Identifier.of(LostCities.MODID, "lights");
    public static final TagKey<Block> LIGHTS_TAG = TagKey.of(RegistryKeys.BLOCK, LIGHTS);

    public static final Identifier NEEDSPOI = Identifier.of(LostCities.MODID, "needspoi");
    public static final TagKey<Block> NEEDSPOI_TAG = TagKey.of(RegistryKeys.BLOCK, NEEDSPOI);
}
