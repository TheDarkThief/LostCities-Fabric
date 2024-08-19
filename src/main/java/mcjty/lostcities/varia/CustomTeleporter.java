package mcjty.lostcities.varia;

import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.TeleportTarget.PostDimensionTransition;
import net.minecraft.world.dimension.PortalForcer;

import java.util.function.Function;

public class CustomTeleporter extends PortalForcer {
    
    private final ServerWorld worldServer;
    private final double x;
    private final double y;
    private final double z;

    public CustomTeleporter(ServerWorld world, double x, double y, double z) {
        super(world);
        this.worldServer = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }


    // I don't see any code using it and I don't know why it's here so, skaddadle skadoodle your dick is now a noodle :3

    // @Override
    // public Entity placeEntity(Entity entity, ServerWorld currentWorld, ServerWorld destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
    //     this.worldServer.getBlockState(new BlockPos((int) this.x, (int) this.y, (int) this.z));

    //     entity.setPos(this.x, this.y, this.z);
    //     entity.setVelocity(0, 0, 0);
    //     return entity;
    // }

    public static void teleportToDimension(PlayerEntity player, ServerWorld dimension, BlockPos pos){
        teleportToDimension(player, dimension, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
    }

    public static void teleportToDimension(PlayerEntity player, ServerWorld dimension, double x, double y, double z) {
        player.teleportTo(new TeleportTarget(dimension, player, new PostDimensionTransition() {
			public void onTransition(Entity entity){
                //nothing?
            }
        } ));
    }

}
