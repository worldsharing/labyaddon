package cc.raynet.worldsharing.v1_21_10;

import com.mojang.authlib.properties.Property;

public interface PropertyStorage {

    Property[] worldsharing$getProperties();

    void worldsharing$setProperties(Property[] profile);
}
