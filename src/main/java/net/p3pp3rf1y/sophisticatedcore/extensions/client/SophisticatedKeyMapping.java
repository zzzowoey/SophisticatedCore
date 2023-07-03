package net.p3pp3rf1y.sophisticatedcore.extensions.client;

import com.mojang.blaze3d.platform.InputConstants;

public interface SophisticatedKeyMapping {
    default InputConstants.Key getKey() {
        return null;
    }

    default boolean matches(InputConstants.Key keyCode) {
        return keyCode != InputConstants.UNKNOWN && keyCode.equals(getKey());
    }
}
