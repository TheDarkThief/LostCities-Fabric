package mcjty.lostcities.gui;

import mcjty.lostcities.api.LostChunkCharacteristics;
import mcjty.lostcities.api.RailChunkType;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.config.ProfileSetup;
import mcjty.lostcities.gui.elements.*;
import mcjty.lostcities.setup.Config;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.varia.TextFactory;
import mcjty.lostcities.worldgen.LostCityFeature;
import mcjty.lostcities.worldgen.lost.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GuiLCConfig extends Screen {

    private final Screen parent;

    private TextRenderer font;

    private ButtonWidget profileButton;
    private ButtonWidget worldstyleButton;
    private ButtonWidget customizeButton;
    private ButtonWidget modeButton;

    private static final int YOFFSET = 21;
    private String curpage;
    private int y;

    private static final List<String> MODES = Arrays.asList("Cities", "Buildings", "Damage", "Transport", "Various");
    private String mode = MODES.get(0);

    private long seed = 3439249320423L;
    private final Random random = new Random();

    private final List<GuiElement> elements = new ArrayList<>();
    private DoubleElement perlinScaleElement;
    private DoubleElement perlinOffsetElement;
    private DoubleElement perlinInnerScaleElement;

    private final LostCitySetup localSetup = new LostCitySetup(this::refreshPreview);

    public GuiLCConfig(Screen parent) {
        super(TextFactory.literal("Lost City Configuration"));
        this.parent = parent;
        localSetup.copyFrom(LostCitySetup.CLIENT_SETUP);
    }

    public static void selectProfile(String profileName, @Nullable LostCityProfile profile) {
        Config.profileFromClient = profileName;

        LostCityFeature.globalDimensionInfoDirtyCounter++;
        Config.resetProfileCache();

        if (profile != null) {
            ProfileSetup.STANDARD_PROFILES.get("customized").copyFrom(profile);
            Config.jsonFromClient = profile.toJson(false).toString();
        }
    }

    public LostCitySetup getLocalSetup() {
        return localSetup;
    }

    public TextRenderer getFont() {
        return this.font;
    }

    @Override
    public void tick() {
        elements.forEach(GuiElement::tick);
    }

    @Override
    protected void init() {
        super.init();
        // @todo 1.19.3
//        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);

        profileButtonWidget = addDrawableChild(new ButtonExt(55, 10, 80, 20, TextFactory.literal(localSetup.getProfileLabel()), p -> {
            localSetup.toggleProfile();
            updateValues();
        }).tooltip(TextFactory.literal("Select a standard profile for your Lost City worldgen")));

        worldstyleButtonWidget = addDrawableChild(new ButtonExt(145, 10, 120, 20, TextFactory.literal(localSetup.getWorldStyleLabel()), p -> {
            localSetup.toggleWorldStyle();
            updateValues();
        }).tooltip(TextFactory.literal("Select the worldstyle to use for this profile")));

        customizeButtonWidget = addDrawableChild(new ButtonExt(275, 10, 70, 20, TextFactory.literal("Customize"), p -> {
            localSetup.customize();
            updateValues();
        }).tooltip(TextFactory.literal("Create a customized version of the currently selected profile")));
        modeButtonWidget = addDrawableChild(new ButtonExt(355, 10, 70, 20, TextFactory.literal(mode), p -> toggleMode())
            .tooltip(TextFactory.literal("Switch between different configuration pages")));


        addDrawableChild(ButtonWidget.builder(TextFactory.literal("Done"), p -> done()).bounds(10, this.height - 22, 120, 20).build());
        addDrawableChild(ButtonWidget.builder(TextFactory.literal("Cancel"), p -> cancel()).bounds(this.width - 130, this.height - 22, 120, 20).build());
        addDrawableChild(new ButtonExt(this.width - 35, 35, 30, 20, TextFactory.literal("Rnd"), p -> randomizePreview())
                .tooltip(TextFactory.literal("Randomize the seed for the preview (does not affect the generated world)")));

        initCities(110);
        initBuildings(110);
        initDamage(70);
        initTransport(110);
        initVarious(110);

        updateValues();
    }

    private BooleanElement addBool(int left, String attribute) {
        BooleanElement el = new BooleanElement(this, curpage, left, y, attribute);
        add(el);
        return el;
    }

    private DoubleElement addDouble(int left, int w, String attribute) {
        DoubleElement el = new DoubleElement(this, curpage, left, y, w, attribute);
        add(el);
        return el;
    }

    private FloatElement addFloat(int left, String attribute) {
        FloatElement el = new FloatElement(this, curpage, left, y, attribute);
        add(el);
        return el;
    }

    private IntElement addInt(int left, String attribute) {
        IntElement el = new IntElement(this, curpage, left, y, attribute);
        add(el);
        return el;
    }

    private void start(String name) {
        curpage = name;
        y = 40;
    }

    private void nl() {
        y += YOFFSET;
    }

    private void initVarious(int left) {
        start("Various");
        addBool(left, "lostcity.generateSpawners").label("Spawners:"); nl();
        addBool(left, "lostcity.generateLighting").label("Lighting:"); nl();
        addBool(left, "lostcity.generateLoot").label("Loot:"); nl();
        addFloat(left, "lostcity.vineChance").label("Vines:"); nl();
        addFloat(left, "lostcity.randomLeafBlockChance").label("Leafs:"); nl();
        nl();
        addBool(left, "lostcity.generateNether").label("Nether:"); nl();
        addBool(left, "lostcity.editMode").label("Edit mode:");
    }

    private void initDamage(int left) {
        start("Damage");
        addBool(left, "lostcity.rubbleLayer").label("Rubble:"); nl();

        addFloat(left, "lostcity.ruinChance").label("Ruins:").prefix("%");
        addFloat(left + 80, "lostcity.ruinMinlevelPercent").prefix("-");
        addFloat(left + 140, "lostcity.ruinMaxlevelPercent").prefix("+");
        nl();

        addFloat(left, "explosions.explosionChance").label("Explosion:").prefix("%");
        addInt(left + 80, "explosions.explosionMinRadius").prefix("-");
        addInt(left + 140, "explosions.explosionMaxRadius").prefix("+");
        nl();
        addInt(left + 80, "explosions.explosionMinHeight").label("Height:");
        addInt(left + 140, "explosions.explosionMaxHeight");
        nl();

        addFloat(left, "explosions.miniExplosionChance").label("Min/exp:").prefix("%");
        addInt(left + 80, "explosions.miniExplosionMinRadius").prefix("-");
        addInt(left + 140, "explosions.miniExplosionMaxRadius").prefix("+");
        nl();
        addInt(left + 80, "explosions.miniExplosionMinHeight").label("Height:");
        addInt(left + 140, "explosions.miniExplosionMaxHeight");
        nl();
    }

    private void initBuildings(int left) {
        start("Buildings");
        addFloat(left,"lostcity.buildingChance").label("Buildings:"); nl();
        nl();
        addInt(left, "lostcity.buildingMinFloors").label("Floors:");
        addInt(left + 55, "lostcity.buildingMaxFloors");
        nl();
        addInt(left, "lostcity.buildingMinFloorsChance").label("Floor Chance:");
        addInt(left + 55, "lostcity.buildingMaxFloorsChance");
        nl();
        addInt(left, "lostcity.buildingMinCellars").label("Cellars:");
        addInt(left + 55, "lostcity.buildingMaxCellars");
    }

    private void initTransport(int left) {
        start("Transport");
        addFloat(left, "lostcity.highwayMainPerlinScale").label("1st perlin:"); nl();
        addFloat(left, "lostcity.highwaySecondaryPerlinScale").label("2nd perlin:"); nl();
        addFloat(left, "lostcity.highwayPerlinFactor").label("Perlin:"); nl();
        addInt(left, "lostcity.highwayDistanceMask").label("Distance mask:"); nl();
        addBool(left, "lostcity.railwaysEnabled").label("Railways:"); nl();
    }

    private void initCities(int left) {
        start("Cities");
        TextExt info = addDrawableChild(new TextExt(this, 10, 30, 230, 3, getFont(),
                TextFactory.literal("Cities are the main feature of Lost Cities. In this page you can control the rarity of them")));
        add(new WidgetElement(info, curpage, 10, 30));
        nl();
        addDouble(left,120, "cities.cityChance").label("Rarity:"); nl();
        perlinScaleElement = addDouble(left,45, "cities.cityPerlinScale").label("Scale/Offset:");
        perlinOffsetElement = addDouble(left + 55,45, "cities.cityPerlinOffset"); nl();
        perlinInnerScaleElement = addDouble(left,45, "cities.cityPerlinInnerScale").label("Inner scale"); nl();
        addFloat(left,"cities.cityThreshold").label("Threshold:"); nl();

        addInt(left,"cities.cityMinRadius").label("Radius:");
        addInt(left + 55, "cities.cityMaxRadius");
        nl();

        addFloat(left,"lostcity.parkChance").label("Parks:"); nl();
        addFloat(left,"lostcity.fountainChance").label("Fountains:"); nl();
    }

    private void toggleMode() {
        int idx = MODES.indexOf(mode);
        idx++;
        if (idx >= MODES.size()) {
            idx = 0;
        }
        mode = MODES.get(idx);
        modeButton.setMessage(TextFactory.literal(mode));
    }

    private GuiElement add(GuiElement el) {
        elements.add(el);
        return el;
    }

    public <T extends ClickableWidget> T addWidget(T widget) {
        return this.addDrawableChild(widget);
    }

    private void randomizePreview() {
        seed = random.nextLong();
        refreshPreview();
    }

    public void refreshPreview() {
        BuildingInfo.cleanCache();
        MultiChunk.cleanCache();
        Highway.cleanCache();
        Railway.cleanCache();
        City.cleanCache();
        CitySphere.cleanCache();
    }

    private void renderExtra(DrawContext graphics) {
        graphics.drawTextWithShadow(font, "Profile:", 10, 16, 0xffffffff);
        elements.forEach(el -> el.render(graphics));

        localSetup.get().ifPresent(profile -> {
            if ("Cities".equals(mode)) {
                renderPreviewMap(graphics, profile, false);
            } else if ("Buildings".equals(mode)) {
                renderPreviewCity(graphics, profile, false);
            } else if ("Damage".equals(mode)) {
                renderPreviewCity(graphics, profile, true);
            } else if ("Transport".equals(mode)) {
                renderPreviewTransports(graphics, profile);
            }
        });
    }

    private void renderPreviewTransports(DrawContext graphics, LostCityProfile profile) {
        renderPreviewMap(graphics, profile, true);
        NullDimensionInfo diminfo = new NullDimensionInfo(profile, seed);
        for (int z = 0; z < NullDimensionInfo.PREVIEW_HEIGHT; z++) {
            for (int x = 0; x < NullDimensionInfo.PREVIEW_WIDTH; x++) {
                int sx = x * 3 + this.width - 190;
                int sz = z * 3 + 32;
                int color = 0;
                ChunkCoord c = new ChunkCoord(diminfo.dimension(), x, z);
                Railway.RailChunkInfo type = Railway.getRailChunkType(c, diminfo, profile);
                if (type.getType() != RailChunkType.NONE) {
                    color = 0x99992222;
                }
                int levelX = Highway.getXHighwayLevel(c, diminfo, profile);
                int levelZ = Highway.getZHighwayLevel(c, diminfo, profile);
                if (levelX >= 0 || levelZ >= 0) {
                    if (color == 0) {
                        color = 0x99ffffff;
                    } else {
                        color = 0x99777777;
                    }
                }
                if (color != 0) {
                    graphics.fill(sx, sz, sx + 3, sz + 3, color);
                }
            }
        }
    }

    private void renderPreviewCity(DrawContext graphics, LostCityProfile profile, boolean showDamage) {
        int base = 50 + 120;
        int leftRender = this.width - 157;
        graphics.fill(leftRender, 50, leftRender + 150, base, 0xff0099bb);
        graphics.fill(leftRender, base, leftRender + 150, 50 + 150, 0xff996633);

        float radius = 190;
        int dimHor = 10;
        int dimVer = 4;

        Random rand = new Random(seed);

        for (int x = 0; x < 14; x++) {
            float factor = 0;
            float sqdist = (x * 16 - 7 * 16) * (x * 16 - 7 * 16);
            if (sqdist < radius * radius) {
                float dist = (float) Math.sqrt(sqdist);
                factor = (radius - dist) / radius;
            }
            if (factor > 0 && x > 0) {
                int maxfloors = profile.BUILDING_MAXFLOORS;
                int randdist = (int) (profile.BUILDING_MINFLOORS_CHANCE + (factor + .1f) * (profile.BUILDING_MAXFLOORS_CHANCE - profile.BUILDING_MINFLOORS_CHANCE));
                if (randdist < 1) {
                    randdist = 1;
                }
                int f = profile.BUILDING_MINFLOORS + rand.nextInt(randdist);
                f++;
                if (f > maxfloors+1) {
                    f = maxfloors+1;
                }
                int minfloors = profile.BUILDING_MINFLOORS + 1;
                if (f < minfloors) {
                    f = minfloors;
                }
                for (int i = 0; i < f; i++) {
                    graphics.fill(leftRender + dimHor * x, base - i * dimVer - dimVer, leftRender + dimHor * x + dimHor - 1, base - i * dimVer + dimVer - 1 - dimVer, 0xffffffff);
                }

                int maxcellars = profile.BUILDING_MAXCELLARS;
                int fb = profile.BUILDING_MINCELLARS + ((maxcellars <= 0) ? 0 : rand.nextInt(maxcellars + 1));
                for (int i = 0; i < fb; i++) {
                    graphics.fill(leftRender + dimHor * x, base + i * dimVer, leftRender + dimHor * x + dimHor - 1, base + i * dimVer + dimVer - 1, 0xff333333);
                }
            }
        }

//        profile.EXPLOSION_CHANCE
        if (showDamage) {
            float horFactor = 1.0f * dimHor / 16.0f;
            float verFactor = 1.0f * dimVer / 6.0f;
            int cx = leftRender + 75;
            int cz = (int) (base - (profile.EXPLOSION_MINHEIGHT-65) * verFactor);
            Random rnd = new Random(333);
            int explosionRadius = profile.EXPLOSION_MAXRADIUS;
            for (int x = (int) (cx - explosionRadius * horFactor); x <= cx + explosionRadius * horFactor; x++) {
                for (int z = (int) (cz - explosionRadius * verFactor); z <= cz + explosionRadius * verFactor; z++) {
                    double sqdist = (cx - x) * (cx - x) / horFactor / horFactor + (cz - z) * (cz - z) / verFactor / verFactor;
                    double dist = Math.sqrt(sqdist);
                    if (dist < explosionRadius - 3) {
                        double damage = 3.0f * (explosionRadius - dist) / explosionRadius;
                        if (rnd.nextFloat() < damage) {
                            graphics.fill(x, z, x + 1, z + 1, 0x66ff0000);
                        }
                    }
                }
            }
            cx = leftRender + 35;
            cz = (int) (base - (profile.MINI_EXPLOSION_MINHEIGHT-65) * verFactor);
            explosionRadius = profile.MINI_EXPLOSION_MAXRADIUS;
            for (int x = (int) (cx - explosionRadius * horFactor); x <= cx + explosionRadius * horFactor; x++) {
                for (int z = (int) (cz - explosionRadius * verFactor); z <= cz + explosionRadius * verFactor; z++) {
                    double sqdist = (cx - x) * (cx - x) / horFactor / horFactor + (cz - z) * (cz - z) / verFactor / verFactor;
                    double dist = Math.sqrt(sqdist);
                    if (dist < explosionRadius - 3) {
                        double damage = 3.0f * (explosionRadius - dist) / explosionRadius;
                        if (rnd.nextFloat() < damage) {
                            graphics.fill(x, z, x + 1, z + 1, 0x66ff0000);
                        }
                    }
                }
            }
        }
    }

    private static int soften(int color, boolean soft) {
        if (soft) {
            int r = (color & 0xff0000) >> 16;
            int g = (color & 0xff00) >> 8;
            int b = (color & 0xff);
            return (r / 3) << 16 | (g / 3) << 8 | (b / 3);
        }
        return color;
    }

    private void renderPreviewMap(DrawContext graphics, LostCityProfile profile, boolean soft) {
        NullDimensionInfo diminfo = new NullDimensionInfo(profile, seed);
        for (int z = 0; z < NullDimensionInfo.PREVIEW_HEIGHT; z++) {
            for (int x = 0; x < NullDimensionInfo.PREVIEW_WIDTH; x++) {
                int sx = x * 3 + this.width - 190;
                int sz = z * 3 + 32;
                char b = diminfo.getBiomeChar(x, z);
                int color = switch (b) {
                    case 'p' -> 0x005500;
                    case '-' -> 0x000066;
                    case '=' -> 0x000066;
                    case '#' -> 0x447744;
                    case '+' -> 0x335533;
                    case '*' -> 0xcccc55;
                    case 'd' -> 0xcccc55;
                    default -> 0x005500;
                };
                graphics.fill(sx, sz, sx + 3, sz + 3, 0xff000000 + soften(color, soft));
                ChunkCoord coord = new ChunkCoord(diminfo.dimension(), x, z);
                LostChunkCharacteristics characteristics = BuildingInfo.getChunkCharacteristicsGui(coord, diminfo);
                if (characteristics.isCity) {
                    color = 0x995555;
                    if (BuildingInfo.hasBuildingGui(x, z, diminfo, characteristics)) {
                        color = 0xffffff;
                    }
                    graphics.fill(sx, sz, sx + 2, sz + 2, 0xff000000 + soften(color, soft));
                }
            }
        }
    }

    private void updateValues() {
        elements.forEach(GuiElement::update);
        refreshPreview();
        profileButton.setTooltip(Tooltip.of(getLocalSetup().getProfileInfo()));
    }

    private void refreshButtons() {
        profileButton.setMessage(TextFactory.literal(localSetup.getProfileLabel()));
        worldstyleButton.setMessage(TextFactory.literal(localSetup.getWorldStyleLabel()));
        customizeButton.active = localSetup.isCustomizable();

        boolean isCustomized = "customized".equals(localSetup.getProfileLabel());
        worldstyleButton.active = isCustomized;
        modeButton.active = localSetup.isCustomizable() || isCustomized;
        elements.forEach(s -> {
            s.setEnabled(isCustomized);
            s.setBasedOnMode(mode);
        });

        localSetup.get().ifPresent(profile -> {
            boolean perlin = profile.CITY_CHANCE < 0;
            perlinScaleElement.setEnabled(perlin && isCustomized);
            perlinOffsetElement.setEnabled(perlin && isCustomized);
            perlinInnerScaleElement.setEnabled(perlin && isCustomized);
        });
    }


    private void cancel() {
        refreshPreview();
        MinecraftClient.getInstance().setScreen(parent);
    }

    private void done() {
        refreshPreview();
        LostCitySetup.CLIENT_SETUP.copyFrom(localSetup);
        LostCityProfile customizedProfile = localSetup.getCustomizedProfile();
        if ("customized".equals(localSetup.getProfile()) && customizedProfile != null) {
            ProfileSetup.STANDARD_PROFILES.get("customized").copyFrom(customizedProfile);
            selectProfile(localSetup.getProfile(), customizedProfile);
        } else {
            selectProfile(localSetup.getProfile(), null);
        }

        MinecraftClient.getInstance().setScreen(parent);
        LostCityFeature.globalDimensionInfoDirtyCounter++;
        Config.resetProfileCache();
    }

    @Override
    public void render(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics, mouseX, mouseY, partialTicks);
        refreshButtons();
        renderExtra(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        for(Element listener : this.children()) {
            if (listener instanceof ClickableWidget widget) {
                if (widget.isMouseOver(mouseX, mouseY) && widget.visible) {
                    Tooltip tooltip = widget.getTooltip();
                    if (tooltip != null) {
                        setTooltipForNextRenderPass(tooltip.toCharSequence(this.minecraft));
                    }
                    break;
                }
            }
        }
    }
}
