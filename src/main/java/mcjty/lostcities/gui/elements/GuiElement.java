package mcjty.lostcities.gui.elements;

import net.minecraft.client.gui.DrawContext;

public class GuiElement {

    protected final int x;
    protected final int y;

    protected final String page;

    public GuiElement(String page, int x, int y) {
        this.page = page;
        this.x = x;
        this.y = y;
    }

    public void tick() {

    }

    public void render(DrawContext graphics) {

    }

    public void update() {

    }

    public void setEnabled(boolean b) {

    }

    public void setBasedOnMode(String mode) {

    }
}
