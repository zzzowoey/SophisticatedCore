package net.p3pp3rf1y.sophisticatedcore.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.p3pp3rf1y.sophisticatedcore.extensions.client.SophisticatedKeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(KeyMapping.class)
public class KeyMappingMixin implements SophisticatedKeyMapping{
    @Shadow private InputConstants.Key key;

    @Override
    public InputConstants.Key getKey() {
        return this.key;
    }
}
