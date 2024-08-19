package mcjty.lostcities.network;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketRequestProfile {

    private final RegistryKey<World> dimension;

    public PacketRequestProfile(PacketByteBuf buf) {
        dimension = RegistryKey.of(RegistryKeys.WORLD, buf.readIdentifier());
    }

    public void toBytes(PacketByteBuf buf) {
        buf.writeIdentifier(dimension.getValue());
    }

    public PacketRequestProfile(RegistryKey<World> dimension) {
        this.dimension = dimension;
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // @todo 1.14
//            ServerPlayerEntity player = ctx.get().getSender();
//            LostCityProfile profile = WorldTypeTools.getProfile(WorldTools.getWorld(dimension));
//            PacketHandler.INSTANCE.sendTo(new PacketReturnProfileToClient(dimension, profile.getName()), player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        });
        ctx.get().setPacketHandled(true);
    }
}
