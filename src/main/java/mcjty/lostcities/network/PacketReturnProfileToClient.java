package mcjty.lostcities.network;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketReturnProfileToClient {

    private final RegistryKey<World> dimension;
    private final String profile;

    public PacketReturnProfileToClient(PacketByteBuf buf) {
        dimension = RegistryKey.of(RegistryKeys.WORLD, buf.readIdentifier());
        profile = buf.readString(32767);
    }

    public void toBytes(PacketByteBuf buf) {
        buf.writeIdentifier(dimension.getValue());
        buf.writeString(profile);
    }

    public PacketReturnProfileToClient(RegistryKey<World> dimension, String profileName) {
        this.dimension = dimension;
        this.profile = profileName;
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // @todo 1.14
//            WorldTypeTools.setProfileFromServer(dimension, profile);
        });
        ctx.get().setPacketHandled(true);
    }
}
