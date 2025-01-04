package cc.raynet.worldsharing.config;

import cc.raynet.worldsharing.WorldsharingAddon;
import net.labymod.api.Textures.SpriteCommon;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.lss.property.annotation.AutoWidget;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.activity.Link;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.dropdown.DropdownWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.FlexibleContentWidget;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.configuration.settings.accessor.SettingAccessor;
import net.labymod.api.configuration.settings.annotation.SettingElement;
import net.labymod.api.configuration.settings.annotation.SettingFactory;
import net.labymod.api.configuration.settings.annotation.SettingWidget;
import net.labymod.api.configuration.settings.switchable.StringSwitchableHandler;
import net.labymod.api.configuration.settings.widget.WidgetFactory;
import net.labymod.api.util.Pair;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@AutoWidget
@SettingWidget
@Link("node-selection.lss")
public class NodeSelection extends FlexibleContentWidget {

    public void initialize(Parent parent) {
        super.initialize(parent);

        final var addon = WorldsharingAddon.INSTANCE;

        ButtonWidget refreshButton = ButtonWidget.icon(SpriteCommon.REFRESH);
        refreshButton.addId("refresh-button");
        refreshButton.setPressable(() -> Thread.ofVirtual().start(() -> addon.api.init()));
        refreshButton.setHoverComponent(Component.translatable("labymod.ui.button.refresh"));


        DropdownWidget<String> dropdownWidget = new DropdownWidget<>();
        dropdownWidget.add("auto");
        dropdownWidget.addAll(WorldsharingAddon.INSTANCE.nodes.keySet());

        dropdownWidget.setSelected(addon.api.selectedNode == null ? "auto" : addon.api.selectedNode.getFirst());
        dropdownWidget.setChangeListener(e -> {
            if ("auto".equals(e)) {
                addon.api.selectedNode = null;
                return;
            }

            for (var entry : addon.nodes.entrySet()) {
                if (entry.getKey().equals(e)) {
                    addon.api.selectedNode = Pair.of(entry.getKey(), entry.getValue());
                    break;
                }
            }

        });

        this.addContent(refreshButton);
        this.addFlexibleContent(dropdownWidget);

    }

    public void tick() {
        super.tick();
    }

    public void dispose() {
        super.dispose();
    }

    @SettingElement(switchable = StringSwitchableHandler.class)
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Selector {
    }

    @SettingFactory
    public static class Factory implements WidgetFactory<NodeSelection.Selector, NodeSelection> {

        @Override
        public NodeSelection[] create(Setting setting, Selector annotation, SettingAccessor accessor) {
            return new NodeSelection[]{new NodeSelection()};
        }

        @Override
        public Class<?>[] types() {
            return new Class[0];
        }
    }
}