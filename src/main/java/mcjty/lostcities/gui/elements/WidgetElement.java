package mcjty.lostcities.gui.elements;

import net.minecraft.client.gui.widget.ClickableWidget;

public class WidgetElement extends GuiElement {

    private final ClickableWidget widget;

    public WidgetElement(ClickableWidget widget, String page, int x, int y) {
        super(page, x, y);
        this.widget = widget;
    }

    @Override
    public void setBasedOnMode(String mode) {
        widget.visible = mode.equalsIgnoreCase(page);
    }
}
