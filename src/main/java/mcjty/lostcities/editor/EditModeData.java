package mcjty.lostcities.editor;

import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.varia.WorldTools;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.command.DataCommandStorage;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import org.jetbrains.annotations.NotNull;
import java.util.*;

/**
 * In a world created in editmode this structure will contain information about all generated parts
 */
public class EditModeData extends PersistentState {

    public static final String NAME = "LostCityEditData";

    public record PartData(String partName, int y) { }
    private final Map<ChunkCoord, List<PartData>> partData = new HashMap<>();


    @NotNull
    public static EditModeData getData() {
        ServerWorld overworld = WorldTools.getOverworld();
        PersistentStateManager storage = overworld.getPersistentStateManager();
        return storage.getOrCreate(type, NAME);
    }

    public EditModeData() {
    }

    public static EditModeData createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup){
        return new EditModeData(tag);
    }

    public EditModeData(NbtCompound nbt) {
        NbtList data = nbt.getList("data", NbtElement.COMPOUND_TYPE);
        for (NbtElement t : data) {
            NbtCompound pdTag = (NbtCompound) t;
            RegistryKey<World> level = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(pdTag.getString("level")));
            int chunkX = pdTag.getInt("x");
            int chunkZ = pdTag.getInt("z");
            ChunkCoord pos = new ChunkCoord(level, chunkX, chunkZ);
            String part = pdTag.getString("part");
            int y = pdTag.getInt("y");
            addPartData(pos, y, part);
        }
    }

    public void addPartData(ChunkCoord pos, int y, String partName) {
        partData.computeIfAbsent(pos, p -> new ArrayList<>()).add(new PartData(partName, y));
        markDirty();
    }

    public List<PartData> getPartData(ChunkCoord pos) {
        return partData.getOrDefault(pos, Collections.emptyList());
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        NbtList data = new NbtList();
        partData.forEach((pos, list) -> {
            for (PartData pd : list) {
                NbtCompound pdTag = new NbtCompound();
                pdTag.putString("level", pos.dimension().getValue().toString());
                pdTag.putInt("x", pos.chunkX());
                pdTag.putInt("z", pos.chunkZ());
                pdTag.putString("part", pd.partName());
                pdTag.putInt("y", pd.y());
                data.add(pdTag);
            }
        });
        tag.put("data", data);
        return tag;
    }

    private static Type<EditModeData> type = new Type<EditModeData>(
        EditModeData::new, // If there's no 'StateSaverAndLoader' yet create one
        EditModeData::createFromNbt, // If there is a 'StateSaverAndLoader' NBT, parse it with 'createFromNbt'
        null // Supposed to be an 'DataFixTypes' enum, but we can just pass null
    );
}
