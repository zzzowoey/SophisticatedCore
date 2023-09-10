package net.p3pp3rf1y.sophisticatedcore;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedcore.common.CapabilityWrapper;
import net.p3pp3rf1y.sophisticatedcore.common.CommonEventHandler;
import net.p3pp3rf1y.sophisticatedcore.data.SCFluidTagsProvider;
import net.p3pp3rf1y.sophisticatedcore.data.SCRecipeProvider;
import net.p3pp3rf1y.sophisticatedcore.init.ModCompat;
import net.p3pp3rf1y.sophisticatedcore.network.PacketHandler;
import net.p3pp3rf1y.sophisticatedcore.util.RecipeHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SophisticatedCore implements ModInitializer {
	public static final String ID = "sophisticatedcore";
	public static final Logger LOGGER = LogManager.getLogger(ID);
	public static boolean SERVER_STARTED = false;

	public final CommonEventHandler commonEventHandler = new CommonEventHandler();

	public SophisticatedCore() {
	}

	@Override
	public void onInitialize() {
		Config.register();

		commonEventHandler.registerHandlers();

		PacketHandler.init();
		ModCompat.initCompats();

		CapabilityWrapper.register();

		ServerLifecycleEvents.SERVER_STARTED.register(server -> RecipeHelper.setWorld(server.getLevel(Level.OVERWORLD)));

		PacketHandler.getChannel().initServerListener();
	}

	public static ResourceLocation getRL(String path) {
		return new ResourceLocation(ID, path);
	}

	public static void gatherData(FabricDataGenerator gen) {
		FabricDataGenerator.Pack pack = gen.createPack();
		pack.addProvider(SCFluidTagsProvider::new);
		pack.addProvider(SCRecipeProvider::new);
	}
}
