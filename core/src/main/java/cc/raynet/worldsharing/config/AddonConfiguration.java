package cc.raynet.worldsharing.config;

import cc.raynet.worldsharing.config.NodeSelection.Selector;
import net.labymod.api.addon.AddonConfig;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget.ButtonSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.configuration.loader.annotation.ConfigName;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.util.MethodOrder;

@ConfigName("settings")
public class AddonConfiguration extends AddonConfig {

    @SwitchSetting
    private final ConfigProperty<Boolean> enabled = new ConfigProperty<>(true);

    @SwitchSetting
    private final ConfigProperty<Boolean> debug = new ConfigProperty<>(false);

    @Selector
    @MethodOrder(after = "debug")
    private final ConfigProperty<String> node = new ConfigProperty<>("");

    @Override
    public ConfigProperty<Boolean> enabled() {
        return this.enabled;
    }

    public ConfigProperty<Boolean> debug() {
        return this.debug;
    }

    @MethodOrder(after = "debug")
    @ButtonSetting
    public void clearCache(Setting setting) {
        // TODO
    }

}
