package mcjty.lostcities.gui.elements;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;

public class ButtonExt extends ButtonWidget {

    public ButtonExt(int x, int y, int w, int h, Text message, PressAction action) {
        super(x, y, w, h, message, action, supplier -> Text.empty());
    }

    public ButtonExt tooltip(Text tooltip) {
        setTooltip(Tooltip.of(tooltip));
        return this;
    }
}
