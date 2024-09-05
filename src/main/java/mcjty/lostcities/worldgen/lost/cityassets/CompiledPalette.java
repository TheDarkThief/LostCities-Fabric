package mcjty.lostcities.worldgen.lost.cityassets;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.worldgen.LostCityTerrainFeature;
import net.minecraft.block.BlockState;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;

import org.jetbrains.annotations.Nullable;
import java.util.*;

/**
 * More efficient representation of a palette useful for a single chunk
 */
public class CompiledPalette {

    private final Map<Character, Object> palette = new HashMap<>();
    private final Map<BlockState, BlockState> damagedToBlock = new HashMap<>();
    private final Map<Character, Palette.Info> information = new HashMap<>();

    public CompiledPalette(CompiledPalette other, Palette... palettes) {
        this.palette.putAll(other.palette);
        this.damagedToBlock.putAll(other.damagedToBlock);
        this.information.putAll(other.information);
        addPalettes(palettes);
    }

    public CompiledPalette(Palette... palettes) {
        addPalettes(palettes);
    }

    private int addEntries(BlockState[] randomBlocks, int idx, BlockState c, int cnt) {
        for (int i = 0 ; i < cnt ; i++) {
            if (idx >= randomBlocks.length) {
                return idx;
            }
            randomBlocks[idx++] = c;
        }
        return idx;
    }

    private void addPalettes(Palette[] palettes) {
        // First add the straight palette entries
        for (Palette p : palettes) {
            if (p != null) {
                for (Map.Entry<Character, Palette.PE> entry : p.getPalette().entrySet()) {
                    Palette.PE pe = entry.getValue();
                    if (pe.blocks() instanceof BlockState) {
                        palette.put(entry.getKey(), pe.blocks());
                    } else if (pe.blocks() instanceof Pair[]) {
                        Pair<Integer, BlockState>[] r = (Pair<Integer, BlockState>[]) pe.blocks();
                        BlockState[] randomBlocks = new BlockState[128];
                        int idx = 0;
                        for (Pair<Integer, BlockState> pair : r) {
                            idx = addEntries(randomBlocks, idx, pair.getRight(), pair.getLeft());
                            if (idx >= randomBlocks.length) {
                                break;
                            }
                        }
                        palette.put(entry.getKey(), randomBlocks);
                        if (idx < randomBlocks.length) {
                            throw new RuntimeException("Invalid palette entry for '" + entry.getKey() + "'! Not enough blocks in the random list (factor should go up to 128)");
                        }
                    } else if (!(pe.blocks() instanceof String)) {
                        if (pe.blocks() == null) {
                            throw new RuntimeException("Invalid palette entry for '" + entry.getKey() + "'!");
                        }
                        palette.put(entry.getKey(), pe.blocks());
                    }
                    // Remove information for this character here. If we need it again we will add it below
                    information.remove(entry.getKey());
                }
            }
        }

        boolean dirty = true;
        while (dirty) {
            dirty = false;

            // Now add the palette entries that refer to other palette entries
            for (Palette p : palettes) {
                if (p != null) {
                    for (Map.Entry<Character, Palette.PE> entry : p.getPalette().entrySet()) {
                        Palette.PE pe = entry.getValue();
                        if (pe.blocks() instanceof String blocks) {
                            char c = blocks.charAt(0);
                            if (palette.containsKey(c) && !palette.containsKey(entry.getKey())) {
                                palette.put(entry.getKey(), palette.get(c));
                                information.remove(entry.getKey());
                                dirty = true;
                            }
                        }
                    }
                }
            }
        }

        for (Palette p : palettes) {
            if (p != null) {
                for (Map.Entry<BlockState, BlockState> entry : p.getDamaged().entrySet()) {
                    BlockState c = entry.getKey();
                    damagedToBlock.put(c, entry.getValue());
                }
                for (Map.Entry<Character, Palette.PE> entry : p.getPalette().entrySet()) {
                    Palette.PE pe = entry.getValue();
                    if (pe.info().isSpecial()) {
                        information.put(entry.getKey(), pe.info());
                    }
                }
            }
        }
    }

    public Set<Character> getCharacters() {
        return palette.keySet();
    }

    /**
     * Return true if this palette entry exists
     */
    public boolean isDefined(Character c) {
        return c != null && palette.containsKey(c);
    }

    /**
     * Return true if this is a simple character that can have only one value in the palette
     */
    public boolean isSimple(char c) {
        Object o = palette.get(c);
        return o instanceof Character;
    }

    // Same as get(c) but with a predefined random generator that is predictable
    public BlockState get(char c, Random rand) {
        try {
            Object o = palette.get(c);
            if (o instanceof BlockState state) {
                return state;
            } else if (o == null) {
                return null;
            } else {
                BlockState[] randomBlocks = (BlockState[]) o;
                return randomBlocks[rand.nextInt(128)];
            }
        } catch (Exception e) {
            LostCities.LOGGER.log(Level.ERROR, e);
            return null;
        }

    }

    public Set<BlockState> getAll(char c) {
        try {
            Object o = palette.get(c);
            if (o instanceof BlockState state) {
                return Collections.singleton(state);
            } else if (o == null) {
                return Collections.emptySet();
            } else {
                BlockState[] randomBlocks = (BlockState[]) o;
                return Set.of(randomBlocks);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public BlockState get(char c) {
        try {
            Object o = palette.get(c);
            if (o instanceof BlockState state) {
                return state;
            } else if (o == null) {
                return null;
            } else {
                BlockState[] randomBlocks = (BlockState[]) o;
                return randomBlocks[LostCityTerrainFeature.fastrand128()];
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public BlockState canBeDamagedToIronBars(BlockState b) {
        return damagedToBlock.get(b);
    }

    public Palette.Info getInfo(Character c) { return information.get(c); }

    /**
     * For editor. Return the palette entry given a state
     */
    @Nullable
    public Character find(BlockState state) {
        for (Map.Entry<Character, Object> entry : palette.entrySet()) {
            Object o = entry.getValue();
            if (o instanceof BlockState s) {
                if (s == state) {
                    return entry.getKey();
                }
            } else {
                BlockState[] randomBlocks = (BlockState[]) o;
                for (BlockState randomBlock : randomBlocks) {
                    if (randomBlock == state) {
                        return entry.getKey();
                    }
                }
            }
        }
        return null;
    }

    /**
     * For editor. See if a state matches with a character
     */
    public boolean isMatch(char c, BlockState state) {
        Object o = palette.get(c);
        if (o instanceof BlockState s) {
            return s.getBlock() == state.getBlock();
        } else {
            BlockState[] randomBlocks = (BlockState[]) o;
            for (BlockState randomBlock : randomBlocks) {
                if (randomBlock.getBlock() == state.getBlock()) {
                    return true;
                }
            }
        }
        return false;
    }
}
