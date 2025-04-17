package cc.raynet.worldsharing.utils.model;

import net.labymod.api.util.I18n;

public enum WorldVisibility {

    INVITE,
    FRIENDS,
    PUBLIC;

    public static WorldVisibility fromValue(byte value) {
        if (value < 0 || value >= values().length) {
            return null;
        }
        return values()[value];
    }


    public byte get() {
        return (byte) ordinal();
    }

    @Override
    public String toString() {
        return I18n.translate("worldsharing.enums.visibility." + name().toLowerCase());
    }
}
