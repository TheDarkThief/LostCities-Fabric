package mcjty.lostcities.data;

import mcjty.lostcities.api.*;
import mcjty.lostcities.setup.CustomRegistries;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.varia.WorldTools;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import net.minecraft.registry.Registries;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import org.jetbrains.annotations.NotNull;
import java.util.*;

public class ChunkInfoData extends PersistentState {

    public static final String NAME = "LostCityChunkData";

    private final Map<ChunkCoord, List<LostChunkCharacteristics>> chunkData = new HashMap<>();

    @NotNull
    public static ChunkInfoData getData() {
        ServerWorld overworld = WorldTools.getOverworld();
        PersistentStateManager storage = overworld.getDataStorage();
        return storage.getOrCreate(ChunkInfoData::new, NAME);
    }

    public ChunkInfoData() {
    }

    public ChunkInfoData(NbtCompound nbt) {
        NbtList data = nbt.getList("data", NbtElement.COMPOUND_TYPE);
        for (NbtElement t : data) {
            NbtCompound pdTag = (NbtCompound) t;
            RegistryKey<World> level = RegistryKey.of(RegistryKeys.DIMENSION, new RegistryKey(pdTag.getString("level")));
            int chunkX = pdTag.getInt("x");
            int chunkZ = pdTag.getInt("z");
            ChunkCoord pos = new ChunkCoord(level, chunkX, chunkZ);
            String part = pdTag.getString("part");
            int y = pdTag.getInt("y");
            addPartData(pos, y, part);
        }
    }

    private LostChunkCharacteristics fromNbt(NbtCompound tag) {
        LostChunkCharacteristics characteristics = new LostChunkCharacteristics();
        characteristics.isCity = tag.getBoolean("city");
        characteristics.couldHaveBuilding = tag.getBoolean("couldHaveBuilding");
        characteristics.multiPos = new MultiPos(tag.getInt("multiX"), tag.getInt("multiZ"), tag.getInt("multiW"), tag.getInt("multiH"));
        characteristics.cityLevel = tag.getInt("cityLevel");
        characteristics.cityStyleId = new Identifier.of(tag.getString("cityStyle"));
        characteristics.multiBuildingId = new Identifier.of(tag.getString("multiBuilding"));
        characteristics.buildingTypeId = new Identifier.of(tag.getString("buildingType"));
        return characteristics;
    }

    private NbtCompound toNbt(LostChunkCharacteristics characteristics) {
        NbtCompound tag = new NbtCompound();
        tag.putBoolean("city", characteristics.isCity);
        tag.putBoolean("couldHaveBuilding", characteristics.couldHaveBuilding);
        tag.putInt("multiX", characteristics.multiPos.x());
        tag.putInt("multiZ", characteristics.multiPos.z());
        tag.putInt("multiW", characteristics.multiPos.w());
        tag.putInt("multiH", characteristics.multiPos.h());
        tag.putInt("cityLevel", characteristics.cityLevel);
        tag.putString("cityStyle", characteristics.cityStyleId.toString());
        tag.putString("multiBuilding", characteristics.multiBuildingId.toString());
        tag.putString("buildingType", characteristics.buildingTypeId.toString());
        return tag;
    }

    // @todo
    public void addPartData(ChunkCoord pos, int y, String partName) {
        chunkData.computeIfAbsent(pos, p -> new ArrayList<>()).add(new LostChunkCharacteristics());
        setDirty();
    }

    public List<LostChunkCharacteristics> getChunkData(ChunkCoord pos) {
        return chunkData.getOrDefault(pos, Collections.emptyList());
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        NbtList data = new NbtList();
        chunkData.forEach((pos, list) -> {
            for (LostChunkCharacteristics pd : list) {
                NbtCompound pdTag = new NbtCompound();
                pdTag.putString("level", pos.dimension().getValue().toString());
                pdTag.putInt("x", pos.chunkX());
                pdTag.putInt("z", pos.chunkZ());
//                pdTag.putString("part", pd.partName());
//                pdTag.putInt("y", pd.y());
                data.add(pdTag);
            }
        });
        tag.put("data", data);
        return tag;
    }
}
