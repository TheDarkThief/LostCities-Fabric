package mcjty.lostcities.varia;

import net.minecraft.text.Text;
import net.minecraft.text.MutableText;

public class TextFactory {

    public static MutableText translatable(String key) {
        return Text.translatable(key);
    }

    public static MutableText literal(String text) {
        return Text.literal(text);
    }

    public static MutableText keybind(String keybind) {
        return Text.keybind(keybind);
    }
}
