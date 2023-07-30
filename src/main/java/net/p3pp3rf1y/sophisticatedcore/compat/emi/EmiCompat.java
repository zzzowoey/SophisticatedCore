package net.p3pp3rf1y.sophisticatedcore.compat.emi;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import io.github.fabricators_of_create.porting_lib.util.NetworkDirection;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.p3pp3rf1y.sophisticatedcore.compat.ICompat;
import net.p3pp3rf1y.sophisticatedcore.compat.common.ClientRecipeHelper;
import net.p3pp3rf1y.sophisticatedcore.compat.common.SetGhostSlotMessage;
import net.p3pp3rf1y.sophisticatedcore.compat.common.SetMemorySlotMessage;
import net.p3pp3rf1y.sophisticatedcore.crafting.UpgradeNextTierRecipe;
import net.p3pp3rf1y.sophisticatedcore.network.PacketHandler;

public class EmiCompat implements EmiPlugin, ICompat {
    @Override
    public void register(EmiRegistry registry) {
        ClientRecipeHelper.getAndTransformAvailableRecipes(UpgradeNextTierRecipe.REGISTERED_RECIPES, ShapedRecipe.class, ClientRecipeHelper::copyShapedRecipe).forEach(r ->
            registry.addRecipe(new EmiCraftingRecipe(
                r.getIngredients().stream().map(EmiIngredient::of).toList(),
                EmiStack.of(r.getResultItem(null)),
                r.getId()
        )));
    }

    @Override
    public void setup() {
        PacketHandler.registerMessage(EmiFillRecipeC2SPacket.class, EmiFillRecipeC2SPacket::new, NetworkDirection.PLAY_TO_SERVER);
        PacketHandler.registerMessage(SetGhostSlotMessage.class, SetGhostSlotMessage::new, NetworkDirection.PLAY_TO_SERVER);
        PacketHandler.registerMessage(SetMemorySlotMessage.class, SetMemorySlotMessage::new, NetworkDirection.PLAY_TO_SERVER);
    }
}
