package mcjty.lostcities.gui;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.config.ProfileSetup;
import mcjty.lostcities.varia.TextFactory;
import net.minecraft.util.Formatting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.resource.LifecycledResourceManagerImpl;
import net.minecraft.resource.Resource;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class LostCitySetup {

    public static final LostCitySetup CLIENT_SETUP = new LostCitySetup(() -> {});

    private List<String> profiles = null;

    private String profile = null;
    private LostCityProfile customizedProfile = null;

    private final Runnable refreshPreview;

    public LostCitySetup(Runnable refreshPreview) {
        this.refreshPreview = refreshPreview;
    }

    public LostCityProfile getCustomizedProfile() {
        return customizedProfile;
    }

    public void reset() {
        profiles = null;
        profile = null;
        customizedProfile = null;
    }

    public boolean isCustomizable() {
        if (profile == null) {
            return false;
        }
        if ("customized".equals(profile)) {
            return false;
        }
        return true;
    }

    public String getProfile() {
        return profile;
    }

    public String getProfileLabel() {
        return profile == null ? "Disabled" : profile;
    }

    public Text getProfileInfo() {
        return get()
                .map(p -> TextFactory.literal(p.getDescription() + "\n").append(
                        TextFactory.literal(p.getExtraDescription() + "\n").formatted(Formatting.AQUA)).append(
                        TextFactory.literal(p.getWarning()).formatted(Formatting.RED)
                ))
                .orElse(TextFactory.literal("Click here to select a profile for your Lost Cities"));
    }

    public String getWorldStyleLabel() {
        return get().isEmpty() ? "n.a." : get().get().getWorldStyle();
    }

    public void setProfile(String profile) {
        this.profile = profile;
        refreshPreview.run();
    }

    public void copyFrom(LostCitySetup other) {
        this.profile = other.profile;
        this.customizedProfile = other.customizedProfile;
    }

    public void customize() {
        if (profile == null) {
            throw new IllegalStateException("Cannot happen!");
        }
        customizedProfile = new LostCityProfile("customized", false);
        LostCityProfile original = ProfileSetup.STANDARD_PROFILES.get(profile);
        ProfileSetup.STANDARD_PROFILES.put("customized", customizedProfile);
        profiles.add("customized");
        customizedProfile.copyFrom(original);
        profile = "customized";
        refreshPreview.run();
    }

    public Optional<LostCityProfile> get() {
        if (profile == null) {
            return Optional.empty();
        } else if ("customized".equals(profile)) {
            return Optional.ofNullable(customizedProfile);
        } else {
            return Optional.of(ProfileSetup.STANDARD_PROFILES.get(profile));
        }
    }

//    public Optional<Configuration> getConfig() {
//        if (profile == null) {
//            return Optional.empty();
//        } else if ("customized".equals(profile)) {
//
//        } else {
//
//        }
//    }


    private static String worldStyleToName(Identifier rl) {
        String path = rl.getPath();
        int idx = path.lastIndexOf('/');
        if (idx != -1) {
            path = path.substring(idx+1);
        }
        idx = path.lastIndexOf('.');
        if (idx != -1) {
            path = path.substring(0, idx);
        }
        if (!LostCities.MODID.equals(rl.getNamespace())) {
            path = rl.getNamespace() + ":" + path;
        }
        return path;
    }

    public void toggleWorldStyle() {
        ResourcePackManager repository = MinecraftClient.getInstance().getResourcePackManager();
        LifecycledResourceManager resourceManager = new LifecycledResourceManagerImpl(ResourceType.SERVER_DATA, repository.createResourcePacks());
        Map<Identifier, Resource> map = resourceManager.findResources("lostcities/worldstyles", s -> s.toString().endsWith(".json"));
        List<String> styles = map.keySet().stream().map(LostCitySetup::worldStyleToName).collect(Collectors.toList());
        String current = get().map(LostCityProfile::getWorldStyle).orElse("<none>");
        int idx = styles.indexOf(current);
        if (idx == -1) {
            idx = 0;
        } else {
            idx++;
            if (idx >= styles.size()) {
                idx = 0;
            }
        }
        if (get().isPresent()) {
            get().get().setWorldStyle(styles.get(idx));
        }
        refreshPreview.run();
        // If things break remove this line of code
        resourceManager.close();
    }

    public void toggleProfile() {
        if (profiles == null) {
            String preferedProfile = "default";
            profiles = ProfileSetup.STANDARD_PROFILES.entrySet().stream()
                    .filter(entry -> entry.getValue().isPublic())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            profiles.sort((o1, o2) -> {
                if (preferedProfile.equals(o1)) {
                    return -1;
                }
                if (preferedProfile.equals(o2)) {
                    return 1;
                }
                return o1.compareTo(o2);
            });
        }

        if (profile == null) {
            profile = profiles.get(0);
        } else {
            int i = profiles.indexOf(profile);
            if (i == -1 || i >= profiles.size()-1) {
                profile = null;
            } else {
                profile = profiles.get(i+1);
            }
        }
        refreshPreview.run();
    }
}
