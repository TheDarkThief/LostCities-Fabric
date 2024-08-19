package mcjty.lostcities.worldgen.lost.regassets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lostcities.worldgen.lost.regassets.data.BlockEntry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class VariantRE implements IAsset<VariantRE> {

    public static final Codec<VariantRE> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(BlockEntry.CODEC).fieldOf("blocks").forGetter(l -> l.blocks)
            ).apply(instance, VariantRE::new));

    private Identifier name;
    private final List<BlockEntry> blocks = new ArrayList<>();

    public VariantRE(List<BlockEntry> entries) {
        blocks.addAll(entries);
    }

    public List<BlockEntry> getBlocks() {
        return blocks;
    }

    @Override
    public VariantRE setRegistryName(Identifier name) {
        this.name = name;
        return this;
    }

    @Nullable
    public Identifier getRegistryName() {
        return name;
    }
}
