package cc.raynet.worldsharing.utils.model;

import net.labymod.api.util.I18n;

public enum WorldVisibility {

    INVITE(0),
    FRIENDS(1),
    PUBLIC(2);

    public final byte value;

    WorldVisibility(int value) {
        this.value = (byte) value;
    }

    public static WorldVisibility fromValue(byte value) {
        for (var v : WorldVisibility.values()) {
            if (v.value == value) {
                return v;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return I18n.translate("worldsharing.enums.visibility." + name().toLowerCase());
    }
}
