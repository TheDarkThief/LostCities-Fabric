package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.function.Predicate;

public class IdentifierMatcher implements Predicate<Identifier> {
    private final Set<Identifier> ifAny;
    private final Set<Identifier> excluding;

    public static final Codec<IdentifierMatcher> CODEC = RecordCodecBuilder.create(codec -> codec.group(
            Codec.STRING.listOf().optionalFieldOf("if_any").forGetter(IdentifierMatcher::getIfAny),
            Codec.STRING.listOf().optionalFieldOf("excluding").forGetter(IdentifierMatcher::getExcluding)
    ).apply(codec, IdentifierMatcher::new));

    public IdentifierMatcher(Optional<List<String>> ifAny, Optional<List<String>> excluding) {
        this.ifAny = new HashSet<>();
        ifAny.ifPresent(strings -> strings.forEach(s -> this.ifAny.add(DataTools.fromName(s))));
        this.excluding = new HashSet<>();
        excluding.ifPresent(strings -> strings.forEach(s -> this.excluding.add(DataTools.fromName(s))));
    }

    public boolean isAny() {
        return ifAny.isEmpty() && excluding.isEmpty();
    }

    private Optional<List<String>> getIfAny() {
        if (ifAny == null || ifAny.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(ifAny.stream().map(Identifier::toString).toList());
        }
    }

    private Optional<List<String>> getExcluding() {
        if (excluding == null || excluding.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(excluding.stream().map(Identifier::toString).toList());
        }
    }

    public static final IdentifierMatcher ANY = new IdentifierMatcher(Optional.empty(), Optional.empty()) {
        @Override
        public boolean test(Identifier str) {
            return true;
        }
    };

    @Override
    public boolean test(Identifier rl) {
        if (rl == null) {
            return false;
        }
        if (!ifAny.isEmpty() && !ifAny.contains(rl)) {
            return false;
        }
        return excluding.isEmpty() || !excluding.contains(rl);
    }
}
