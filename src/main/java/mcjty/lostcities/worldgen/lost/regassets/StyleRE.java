package mcjty.lostcities.worldgen.lost.regassets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lostcities.worldgen.lost.regassets.data.PaletteSelector;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StyleRE implements IAsset<StyleRE> {

    public static final Codec<StyleRE> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(Codec.list(PaletteSelector.CODEC)).fieldOf("randompalettes").forGetter(l -> l.randomPaletteChoices)
            ).apply(instance, StyleRE::new));

    private Identifier name;

    private final List<List<PaletteSelector>> randomPaletteChoices;

    public StyleRE(List<List<PaletteSelector>> randomPaletteChoices) {
        this.randomPaletteChoices = randomPaletteChoices;
    }

    public List<List<PaletteSelector>> getRandomPaletteChoices() {
        return randomPaletteChoices;
    }

    @Override
    public StyleRE setRegistryName(Identifier name) {
        this.name = name;
        return this;
    }

    @Nullable
    public Identifier getRegistryName() {
        return name;
    }
}