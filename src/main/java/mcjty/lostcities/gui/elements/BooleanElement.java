package mcjty.lostcities.gui.elements;

import mcjty.lostcities.config.Configuration;
import mcjty.lostcities.gui.GuiLCConfig;
import mcjty.lostcities.varia.TextFactory;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class BooleanElement extends GuiElement {

    private final GuiLCConfig gui;
    private String label = null;
    private final ButtonExt field;
    private final String attribute;

    public BooleanElement(GuiLCConfig gui, String page, int x, int y, String attribute) {
        super(page, x, y);
        this.gui = gui;
        this.attribute = attribute;
        Boolean c = gui.getLocalSetup().get().map(h -> (Boolean) h.toConfiguration().get(attribute)).orElse(false);
        field = new ButtonExt(x, y, 60, 16, c ? TextFactory.literal("On") : TextFactory.literal("Off"), button -> {
            Text message = button.getMessage();
            if ("On".equals(message.getString())) { // @todo 1.16 getString() is ugly here!
                button.setMessage(TextFactory.literal("Off"));
            } else {
                button.setMessage(TextFactory.literal("On"));
            }
            gui.getLocalSetup().get().ifPresent(profile -> {
                Configuration configuration = profile.toConfiguration();
                configuration.set(attribute, "On".equals(button.getMessage().getString()));
                profile.copyFromConfiguration(configuration);
                gui.refreshPreview();
            });
        }) {
            // @todo 1.19.3
//            @Override
//            public void renderToolTip(PoseStack stack, int x, int y) {
//                gui.getLocalSetup().get().ifPresent(h -> {
//                    gui.renderTooltip(stack, h.toConfiguration().getValue(attribute).getComment(), x, y);
//                });
//            }
        };
        gui.addWidget(field);
    }

    public BooleanElement label(String label) {
        this.label = label;
        return this;
    }

    @Override
    public void render(DrawContext graphics) {
        if (label != null) {
            if (field.visible) {
                graphics.drawTextWithShadow(gui.getFont(), label, 10, y + 5, 0xffffffff);
            }
        }
    }

    @Override
    public void update() {
        gui.getLocalSetup().get().ifPresent(profile -> {
            Boolean result = profile.toConfiguration().get(attribute);
            field.setMessage(result ? TextFactory.literal("On") : TextFactory.literal("Off"));
        });
    }

    @Override
    public void setEnabled(boolean b) {
        field.active = b;
    }

    @Override
    public void setBasedOnMode(String mode) {
        field.visible = page.equalsIgnoreCase(mode);
    }
}
