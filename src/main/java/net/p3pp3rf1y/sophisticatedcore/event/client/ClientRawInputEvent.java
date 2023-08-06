package net.p3pp3rf1y.sophisticatedcore.event.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;


@Environment(EnvType.CLIENT)
public interface ClientRawInputEvent {
    Event<KeyPressed> KEY_PRESSED = EventFactory.createArrayBacked(KeyPressed.class, callbacks -> (client, keyCode, scanCode, action, modifiers) -> {
        for (var event : callbacks) {
            var result = event.keyPressed(client, keyCode, scanCode, action, modifiers);
            if (result != InteractionResult.PASS) {
                return result;
            }
        }
        return InteractionResult.PASS;
    });

    @FunctionalInterface
    interface KeyPressed {
        InteractionResult keyPressed(Minecraft client, int keyCode, int scanCode, int action, int modifiers);
    }
}
