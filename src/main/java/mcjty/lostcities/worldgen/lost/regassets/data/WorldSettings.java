package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;

import java.util.Optional;

public record WorldSettings(RailwayAvoidance railwayAvoidance) {

    public enum RailwayAvoidance implements StringRepresentable {
        IGNORE("ignore"),
        BLOCK_RAILWAY("block_railway");

        private final String name;

        RailwayAvoidance(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public static final Codec<WorldSettings> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    StringRepresentable.fromEnum(RailwayAvoidance::values).fieldOf("railwayavoidance").forGetter(l -> l.railwayAvoidance)
            ).apply(instance, WorldSettings::new));

    public static final WorldSettings DEFAULT = new WorldSettings(RailwayAvoidance.IGNORE);

    public Optional<WorldSettings> get() {
        if (this == DEFAULT) {
            return Optional.empty();
        } else {
            return Optional.of(this);
        }
    }

}
