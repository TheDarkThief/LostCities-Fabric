package mcjty.lostcities.gui.elements;

import mcjty.lostcities.config.Configuration;
import mcjty.lostcities.gui.GuiLCConfig;
import mcjty.lostcities.varia.TextFactory;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;

public class IntElement extends GuiElement {

    private final GuiLCConfig gui;
    private String label = null;
    private String prefix = null;
    private final TextFieldWidget field;
    private final String attribute;

    public IntElement(GuiLCConfig gui, String page, int x, int y, String attribute) {
        super(page, x, y);
        this.gui = gui;
        this.attribute = attribute;
        Integer c = gui.getLocalSetup().get().map(h -> (Integer) h.toConfiguration().get(attribute)).orElse(0);
        field = new TextFieldWidget(gui.getFont(), x, y, 45, 16, TextFactory.literal(Integer.toString(c))) {
            // @todo 1.19.3
//            @Override
//            public void renderToolTip(PoseStack stack, int x, int y) {
//                gui.getLocalSetup().get().ifPresent(h -> {
//                    gui.renderTooltip(stack, h.toConfiguration().getValue(attribute).getComment(), x, y);
//                });
//            }
        };
        field.setChangedListener(s -> {
            gui.getLocalSetup().get().ifPresent(profile -> {
                Configuration configuration = profile.toConfiguration();

                int value = 0;
                try {
                    value = Integer.parseInt(s);
                } catch (NumberFormatException e) {
                    return;
                }
                Configuration.Value val = configuration.getValue(attribute);
                val.set(value);
                if (val.constrain()) {
                    // It was constraint to min/max. Restore the field
                    with(val.get());
                }
                profile.copyFromConfiguration(configuration);
                gui.refreshPreview();
            });
        });
        gui.addWidget(field);
    }

    public IntElement prefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public IntElement label(String label) {
        this.label = label;
        return this;
    }

    @Override
    public void tick() {
        field.tick();
    }

    @Override
    public void render(DrawContext graphics) {
        if (field.visible) {
            if (label != null) {
                graphics.drawTextWithShadow(gui.getFont(), label, 10, y + 5, 0xffffffff);
            }
            if (prefix != null) {
                graphics.drawTextWithShadow(gui.getFont(), prefix, x - 8, y + 5, 0xffffffff);
            }
        }
    }

    @Override
    public void update() {
        gui.getLocalSetup().get().ifPresent(profile -> {
            Object result = profile.toConfiguration().get(attribute);
            with(result);
        });
    }

    private void with(Object result) {
        if (result instanceof Float) {
            field.write(Float.toString((Float)result));
        } else if (result instanceof Integer) {
            field.write(Integer.toString((Integer)result));
        }
    }

    @Override
    public void setEnabled(boolean b) {
        field.setEditable(b);
    }

    @Override
    public void setBasedOnMode(String mode) {
        field.setVisible(page.equalsIgnoreCase(mode));
    }

}
