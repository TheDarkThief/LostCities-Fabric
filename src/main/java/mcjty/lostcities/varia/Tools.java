package mcjty.lostcities.varia;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.registry.DefaultedRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.random.Random;
import net.minecraft.datafixer.fix.BlockStateFlattening;
import net.minecraft.world.WorldView;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;

import org.jetbrains.annotations.Nullable;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import java.util.function.Function;
import java.util.stream.Collectors;

public class Tools {

    private static final Set<String> DONE = new HashSet<>();

    private static final Function<Map.Entry<Property<?>, Comparable<?>>, String> PROPERTY_MAPPER = new Function<Map.Entry<Property<?>, Comparable<?>>, String>() {
        @Override
        public String apply(@Nullable Map.Entry<Property<?>, Comparable<?>> entry) {
            if (entry == null) {
                return "<NULL>";
            } else {
                Property<?> property = entry.getKey();
                return property.getName() + "=" + this.getName(property, entry.getValue());
            }
        }

        private <T extends Comparable<T>> String getName(Property<T> property, Comparable<?> comparable) {
            return property.name((T)comparable);
        }
    };

    public static String stateToString(BlockState state) {
        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append(Registries.BLOCK.getKey(state.getBlock()));
        if (!state.getEntries().isEmpty()) {
            stringbuilder.append('[');
            stringbuilder.append(state.getEntries().entrySet().stream().map(PROPERTY_MAPPER).collect(Collectors.joining(",")));
            stringbuilder.append(']');
        }

        return stringbuilder.toString();
    }

    public static BlockState stringToState(String s) {
        if (s.contains("[")) {
            try {
                BlockArgumentParser.BlockResult parser = BlockArgumentParser.block(WorldTools.getOverworld().createCommandRegistryWrapper(RegistryKeys.BLOCK), new StringReader(s), false);
                return parser.blockState();
            } catch (CommandSyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        String converted = BlockStateFlattening.lookupBlock(s);
        Block value = Registries.BLOCK.get(Identifier.of(converted));
        if (value == null) {
            throw new RuntimeException("Cannot find block: '" + s + "'!");
        }
        return value.getDefaultState();
    }

    // public static <T> T getRandomFromList(Random random, List<T> list, Function<T, Float> weightGetter) {
    //     if (list.isEmpty()) {
    //         return null;
    //     }
    //     List<T> elements = new ArrayList<>();
    //     float totalweight = 0;
    //     for (T pair : list) {
    //         elements.add(pair);
    //         totalweight += weightGetter.apply(pair);
    //     }
    //     float r = random.nextFloat() * totalweight;
    //     for (T pair : elements) {
    //         r -= weightGetter.apply(pair);
    //         if (r <= 0) {
    //             return pair;
    //         }
    //     }
    //     return elements.get(elements.size() - 1);
    // }

    public static <T> T getRandomFromList(Random random, List<T> list, Function<T, Float> weightGetter) {
        if (list.isEmpty()) {
            return null;
        }
        List<T> elements = new ArrayList<>();
        float totalweight = 0;
        for (T pair : list) {
            elements.add(pair);
            totalweight += weightGetter.apply(pair);
        }
        float r = random.nextFloat() * totalweight;
        for (T pair : elements) {
            r -= weightGetter.apply(pair);
            if (r <= 0) {
                return pair;
            }
        }
        return null;
    }

    //java.util.Random version
    public static <T> T getRandomFromList(java.util.Random random, List<T> list, Function<T, Float> weightGetter) {
        if (list.isEmpty()) {
            return null;
        }
        List<T> elements = new ArrayList<>();
        float totalweight = 0;
        for (T pair : list) {
            elements.add(pair);
            totalweight += weightGetter.apply(pair);
        }
        float r = random.nextFloat() * totalweight;
        for (T pair : elements) {
            r -= weightGetter.apply(pair);
            if (r <= 0) {
                return pair;
            }
        }
        return null;
    }

    public static Iterable<RegistryEntry<Block>> getBlocksForTag(TagKey<Block> rl) {
        @SuppressWarnings("deprecation") DefaultedRegistry<Block> registry = Registries.BLOCK;
        return registry.iterateEntries(rl);
    }

    public static boolean hasTag(Block block, TagKey<Block> tag) {
        //modern tag solution I think
        return block.getDefaultState().isIn(tag);
    }

    public static int getSeaLevel(WorldView level) {
        if (level instanceof StructureWorldAccess wgLevel) {
            if (wgLevel.getChunkManager() instanceof ServerChunkManager scc) {
                return scc.getChunkGenerator().getSeaLevel();
            }
        }
        //noinspection deprecation
        return level.getSeaLevel();
    }

    public static <T> RecordCodecBuilder<T, List<String>> listOrStringList(String fieldName, String defaultVal, Function<T, List<String>> getter) {
        return Codec.either(Codec.STRING, Codec.STRING.listOf())
                .optionalFieldOf(fieldName, Either.left(defaultVal))
                .xmap(either -> either.map(List::of, Function.identity()), list -> list.size() == 1 ? Either.left(list.get(0)) : Either.right(list))
                .forGetter(getter);
    }
}
