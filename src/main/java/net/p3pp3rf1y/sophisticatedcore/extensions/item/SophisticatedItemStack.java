package net.p3pp3rf1y.sophisticatedcore.extensions.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

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
     * @return the fuel burn time for this itemStack in a furnace. Return 0 to make
     *         it not act as a fuel. Return -1 to let the default vanilla logic
     *         decide.
     */
    default int getBurnTime(@Nullable RecipeType<?> recipeType) {
        return self().getItem().getBurnTime(self(), recipeType);
    }
}
