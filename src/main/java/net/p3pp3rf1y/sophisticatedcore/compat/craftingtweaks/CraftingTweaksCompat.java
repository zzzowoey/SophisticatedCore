package net.p3pp3rf1y.sophisticatedcore.compat.craftingtweaks;

import net.blay09.mods.craftingtweaks.api.CraftingTweaksAPI;

import io.github.fabricators_of_create.porting_lib.util.EnvExecutor;
import net.fabricmc.api.EnvType;
import net.p3pp3rf1y.sophisticatedcore.compat.ICompat;

public class CraftingTweaksCompat implements ICompat {
	@Override
	public void setup() {
		CraftingTweaksAPI.registerCraftingGridProvider(new CraftingUpgradeTweakProvider());
        EnvExecutor.runWhenOn(EnvType.CLIENT, () -> CraftingUpgradeTweakUIPart::register);
	}
}
