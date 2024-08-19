package mcjty.lostcities.playerdata;

import net.minecraft.util.math.Direction;
import net.minecraft.nbt.NbtCompound;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PropertiesDispatcher implements ICapabilityProvider, INBTSerializable<NbtCompound> {

    private PlayerSpawnSet playerSpawnSet = null;
    private final LazyOptional<PlayerSpawnSet> opt = LazyOptional.of(this::createPlayerSpawnSet);

    @NotNull
    private PlayerSpawnSet createPlayerSpawnSet() {
        if (playerSpawnSet == null) {
            playerSpawnSet = new PlayerSpawnSet();
        }
        return playerSpawnSet;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        if (cap == PlayerProperties.PLAYER_SPAWN_SET) {
            return opt.cast();
        }
        return LazyOptional.empty();
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return getCapability(cap);
    }

    @Override
    public NbtCompound serializeNBT() {
        NbtCompound nbt = new NbtCompound();
        createPlayerSpawnSet().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(NbtCompound nbt) {
        createPlayerSpawnSet().loadNBTData(nbt);
    }
}
