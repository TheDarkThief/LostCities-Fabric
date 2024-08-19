package mcjty.lostcities.playerdata;


import net.minecraft.nbt.NbtCompound;

public class PlayerSpawnSet {

    private boolean playerSpawnSet = false;

    public PlayerSpawnSet() {
    }

    public boolean isPlayerSpawnSet() {
        return playerSpawnSet;
    }

    public void setPlayerSpawnSet(boolean playerSpawnSet) {
        this.playerSpawnSet = playerSpawnSet;
    }

    public void copyFrom(PlayerSpawnSet source) {
        playerSpawnSet = source.playerSpawnSet;
    }


    public void saveNBTData(NbtCompound compound) {
        compound.putBoolean("spawnSet", playerSpawnSet);
    }

    public void loadNBTData(NbtCompound compound) {
        playerSpawnSet = compound.getBoolean("spawnSet");
    }
}
