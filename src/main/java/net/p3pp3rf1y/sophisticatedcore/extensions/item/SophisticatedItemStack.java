package net.p3pp3rf1y.sophisticatedcore.extensions.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public interface SophisticatedItemStack {
    // Helpers for accessing Item data
    private ItemStack self()
    {
        return (ItemStack)this;
    }

    /**
     * ItemStack sensitive version of {@link Item#getCraftingRemainingItem()}.
     * Returns a full ItemStack instance of the result.
     *
     * @return The resulting ItemStack
     */
    default ItemStack getCraftingRemainingItem()
    {
        return self().getItem().getCraftingRemainingItem(self());
    }

    /**
     * ItemStack sensitive version of {@link Item#hasCraftingRemainingItem()}.
     *
     * @return True if this item has a crafting remaining item
     */
    default boolean hasCraftingRemainingItem()
    {
        return self().getItem().hasCraftingRemainingItem(self());
    }

    /**
     * Called to tick armor in the armor slot. Override to do something
     */
    default void onArmorTick(Level level, Player player)
    {
        self().getItem().onArmorTick(self(), level, player);
    }
}
