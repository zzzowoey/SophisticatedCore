package net.p3pp3rf1y.sophisticatedcore.extensions.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

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
     * Called to tick armor in the armor slot. Override to do something
     */
    default void onArmorTick(ItemStack stack, Level level, Player player)
    {
    }

    /**
     * This is called when the item is used, before the block is activated.
     *
     * @return Return PASS to allow vanilla handling, any other to skip normal code.
     */
    default InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context)
    {
        return InteractionResult.PASS;
    }
}
