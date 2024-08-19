package mcjty.lostcities.worldgen.lost.regassets.data;

import mcjty.lostcities.LostCities;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class DataTools {

    public static Optional<String> toNullable(Character c) {
        if (c == null) {
            return Optional.empty();
        } else {
            return Optional.of(Character.toString(c));
        }
    }

    public static Character getNullableChar(Optional<String> opt) {
        return opt.isPresent() ? opt.get().charAt(0) : null;
    }

    public static String toName(Identifier rl) {
        if (rl.getNamespace().equals(LostCities.MODID)) {
            return rl.getPath();
        } else {
            return rl.toString();
        }
    }

    public static Identifier fromName(String name) {
        if (name.contains(":")) {
            return Identifier.of(name);
        } else {
            return Identifier.of(LostCities.MODID, name);
        }
    }
}
