package net.p3pp3rf1y.sophisticatedcore.init;

import net.fabricmc.loader.api.FabricLoader;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import net.p3pp3rf1y.sophisticatedcore.compat.CompatModIds;
import net.p3pp3rf1y.sophisticatedcore.compat.ICompat;
import net.p3pp3rf1y.sophisticatedcore.compat.jei.JeiCompat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

public class ModCompat {
	private ModCompat() {}

	private static final Map<String, Supplier<Callable<ICompat>>> compatFactories = new HashMap<>();

	static {
		compatFactories.put(CompatModIds.JEI, () -> JeiCompat::new);
		/*compatFactories.put(CompatModIds.CRAFTING_TWEAKS, () -> CraftingTweaksCompat::new);
		compatFactories.put(CompatModIds.INVENTORY_SORTER, () -> InventorySorterCompat::new);*/
		//compatFactories.put(CompatModIds.QUARK, () -> QuarkCompat::new); //TODO readd quark compat
	}

	public static void initCompats() {
		for (Map.Entry<String, Supplier<Callable<ICompat>>> entry : compatFactories.entrySet()) {
			if (FabricLoader.getInstance().isModLoaded(entry.getKey())) {
				try {
					entry.getValue().get().call().setup();
				}
				catch (Exception e) {
					SophisticatedCore.LOGGER.error("Error instantiating compatibility ", e);
				}
			}
		}
	}
}
