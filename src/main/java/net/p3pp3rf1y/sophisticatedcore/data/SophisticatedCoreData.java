package net.p3pp3rf1y.sophisticatedcore.data;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;

@SuppressWarnings("unused")
public class SophisticatedCoreData implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        SophisticatedCore.gatherData(generator);
    }
}
