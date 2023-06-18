package net.p3pp3rf1y.sophisticatedcore;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedcore.common.CommonEventHandler;
import net.p3pp3rf1y.sophisticatedcore.init.ModCompat;
import net.p3pp3rf1y.sophisticatedcore.network.PacketHandler;
import net.p3pp3rf1y.sophisticatedcore.util.RecipeHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SophisticatedCore implements ModInitializer {
	public static final String ID = "sophisticatedcore";
	public static final Logger LOGGER = LogManager.getLogger(ID);

	public final CommonEventHandler commonEventHandler = new CommonEventHandler();

	@SuppressWarnings("java:S1118") //needs to be public for mod to work
	public SophisticatedCore() {
	}

	@Override
	public void onInitialize() {
		Config.register();

		commonEventHandler.registerHandlers();

		init();
	}

	public static void init() {
		PacketHandler.init();
		ModCompat.initCompats();

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			ServerLevel world = server.getLevel(Level.OVERWORLD);
			if (world != null) {
				RecipeHelper.setWorld(world);
			}
		});
	}

	public static ResourceLocation getRL(String path) {
		return new ResourceLocation(ID, path);
	}
}
