package mcjty.lostcities.datagen;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.worldgen.LostTags;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistryKeys;

import org.jetbrains.annotations.NotNull;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class LCBlockTags extends BlockTagsProvider {

    public LCBlockTags(DataGenerator generator, CompletableFuture<RegistryEntryLookup.Provider> lookupProvider , ExistingFileHelper helper) {
        super(generator.getPackOutput(), lookupProvider, LostCities.MODID, helper);
    }

    private static final Set<TagKey<Block>> PLANT_TAGS = Set.of(
            BlockTags.CORAL_PLANTS,
            BlockTags.BAMBOO_BLOCKS,
            BlockTags.LOGS,
            BlockTags.LEAVES,
            BlockTags.SAPLINGS,
            BlockTags.FLOWERS
    );

    @Override
    protected void addTags(RegistryEntryLookup.Provider provider) {
        for (TagKey<Block> tag : PLANT_TAGS) {
            tag(LostTags.FOLIAGE_TAG).addTag(tag);
        }
        tag(LostTags.EASY_BREAKABLE_TAG).addTags(Tags.Blocks.GLASS);
        for (Block block : ForgeRegistryKeys.BLOCKS.getValues()) {
            if (block.getDefaultState().getLightEmission() > 0) {
                tag(LostTags.LIGHTS_TAG).add(block);
            }
        }

        tag(LostTags.ROTATABLE_TAG).addTag(net.minecraft.registry.tag.BlockTags.STAIRS);
        tag(LostTags.NOT_BREAKABLE_TAG).add(Blocks.BEDROCK, Blocks.END_PORTAL, Blocks.END_PORTAL_FRAME, Blocks.END_GATEWAY);

        tag(LostTags.NEEDSPOI_TAG).add(Blocks.BREWING_STAND, Blocks.CAULDRON, Blocks.BARREL, Blocks.BLAST_FURNACE, Blocks.SMOKER,
                Blocks.COMPOSTER, Blocks.FLETCHING_TABLE, Blocks.LECTERN, Blocks.STONECUTTER, Blocks.LOOM, Blocks.SMITHING_TABLE, Blocks.GRINDSTONE);
    }

    @Override
    @NotNull
    public String getName() {
        return "LostCity Tags";
    }
}
