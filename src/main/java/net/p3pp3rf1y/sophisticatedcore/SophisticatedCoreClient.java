package net.p3pp3rf1y.sophisticatedcore;

import net.fabricmc.api.ClientModInitializer;
import net.p3pp3rf1y.sophisticatedcore.client.ClientEventHandler;

public class SophisticatedCoreClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientEventHandler.registerHandlers();
    }
}
