package mcjty.lostcities.worldgen.lost.cityassets;

import mcjty.lostcities.api.ILostCityAsset;
import mcjty.lostcities.varia.Tools;
import mcjty.lostcities.worldgen.lost.regassets.VariantRE;
import mcjty.lostcities.worldgen.lost.regassets.data.BlockEntry;
import mcjty.lostcities.worldgen.lost.regassets.data.DataTools;
import net.minecraft.util.Identifier;
import net.minecraft.block.BlockState;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * A block variant
 */
public class Variant implements ILostCityAsset {

    private final Identifier name;
    private final List<Pair<Integer, BlockState>> blocks = new ArrayList<>();

    public Variant(VariantRE object) {
        name = object.getRegistryName();
        for (BlockEntry entry : object.getBlocks()) {
            BlockState state = Tools.stringToState(entry.block());
            blocks.add(Pair.of(entry.random(), state));
        }
    }

    @Override
    public String getName() {
        return DataTools.toName(name);
    }

    @Override
    public Identifier getId() {
        return name;
    }

    public List<Pair<Integer, BlockState>> getBlocks() {
        return blocks;
    }
}