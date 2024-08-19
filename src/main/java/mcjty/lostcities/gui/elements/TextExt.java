package mcjty.lostcities.gui.elements;

import net.minecraft.client.font.TextRenderer ;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;


public class TextExt extends MultilineTextWidget {

    private final Screen parent;
    private Text tooltip = null;

    public TextExt(Screen parent, int x, int y, int w, int rows, TextRenderer font, Text message) {
        super(x, y, message, font);
        this.parent = parent;
        setMaxWidth(w);
        setMaxRows(rows);
    }

    public TextExt tooltip(Text tooltip) {
        this.tooltip = tooltip;
        return this;
    }


    // @todo 1.19.3
//    @Override
//    public void renderToolTip(PoseStack stack, int x, int y) {
//        if (tooltip != null) {
//            parent.renderTooltip(stack, tooltip, x, y);
//        }
//    }
}
