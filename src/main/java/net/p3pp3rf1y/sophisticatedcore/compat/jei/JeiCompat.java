package net.p3pp3rf1y.sophisticatedcore.compat.jei;

import io.github.fabricators_of_create.porting_lib.util.NetworkDirection;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import net.p3pp3rf1y.sophisticatedcore.compat.ICompat;
import net.p3pp3rf1y.sophisticatedcore.compat.common.ClientRecipeHelper;
import net.p3pp3rf1y.sophisticatedcore.compat.common.SetGhostSlotMessage;
import net.p3pp3rf1y.sophisticatedcore.compat.common.SetMemorySlotMessage;
import net.p3pp3rf1y.sophisticatedcore.crafting.UpgradeNextTierRecipe;
import net.p3pp3rf1y.sophisticatedcore.network.PacketHandler;

@SuppressWarnings("unused")
@JeiPlugin
public class JeiCompat implements IModPlugin, ICompat {
	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(SophisticatedCore.ID, "default");
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		registration.addRecipes(RecipeTypes.CRAFTING, ClientRecipeHelper.getAndTransformAvailableRecipes(UpgradeNextTierRecipe.REGISTERED_RECIPES, ShapedRecipe.class, ClientRecipeHelper::copyShapedRecipe));
	}

	@Override
	public void setup() {
		PacketHandler.registerMessage(TransferRecipeMessage.class, TransferRecipeMessage::new, NetworkDirection.PLAY_TO_SERVER);
		PacketHandler.registerMessage(SetGhostSlotMessage.class, SetGhostSlotMessage::new, NetworkDirection.PLAY_TO_SERVER);
		PacketHandler.registerMessage(SetMemorySlotMessage.class, SetMemorySlotMessage::new, NetworkDirection.PLAY_TO_SERVER);
	}
}
