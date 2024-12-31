package cc.raynet.worldsharing.utils.model;

import net.labymod.api.util.I18n;

public enum WorldVisibility {

    INVITE_ONLY(0, "invite"),
    FRIENDS_ONLY(1, "friends"),
    PUBLIC(2, "public");

    public final byte value;
    private final String name;

    WorldVisibility(int value, String name) {
        this.value = (byte) value;
        this.name = name;
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
        return I18n.translate("worldsharing.enums.visibility." + name);
    }
}
