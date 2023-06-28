package net.p3pp3rf1y.sophisticatedcore.extensions.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

import javax.annotation.Nullable;

public interface SophisticatedItem {
    // Helpers for accessing Item data
    private Item self()
    {
        return (Item)this;
    }

    /**
     * ItemStack sensitive version of {@link Item#getCraftingRemainingItem()}.
     * Returns a full ItemStack instance of the result.
     *
     * @param itemStack The current ItemStack
     * @return The resulting ItemStack
     */
    @SuppressWarnings("deprecation")
    default ItemStack getCraftingRemainingItem(ItemStack itemStack)
    {
        if (!hasCraftingRemainingItem(itemStack))
        {
            return ItemStack.EMPTY;
        }
        return new ItemStack(self().getCraftingRemainingItem());
    }

    /**
     * ItemStack sensitive version of {@link Item#hasCraftingRemainingItem()}.
     *
     * @param stack The current item stack
     * @return True if this item has a crafting remaining item
     */
    @SuppressWarnings("deprecation")
    default boolean hasCraftingRemainingItem(ItemStack stack)
    {
        return self().hasCraftingRemainingItem();
    }

    /**
     * @return the fuel burn time for this itemStack in a furnace. Return 0 to make
     *         it not act as a fuel. Return -1 to let the default vanilla logic
     *         decide.
     */
    default int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType)
    {
        return -1;
    }
}
