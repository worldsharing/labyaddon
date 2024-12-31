package cc.raynet.worldsharing.navigation;

import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.navigation.elements.ScreenNavigationElement;
import net.labymod.api.client.gui.screen.ScreenInstance;

public class NavigationElement extends ScreenNavigationElement {

    private final Component name;
    private final String widgetId;
    private final boolean show;

    public NavigationElement(Component name, String widgetId, boolean show, ScreenInstance instance) {
        super(instance);
        this.name = name;
        this.widgetId = widgetId;
        this.show = show;
    }

    @Override
    public Component getDisplayName() {
        return name;
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public boolean isVisible() {
        return show;
    }

    @Override
    public String getWidgetId() {
        return widgetId;
    }

}
