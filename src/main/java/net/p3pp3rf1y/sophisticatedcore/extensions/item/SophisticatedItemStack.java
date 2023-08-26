package net.p3pp3rf1y.sophisticatedcore.extensions.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

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

    default InteractionResult onItemUseFirst(UseOnContext context) {
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        Registry<Block> registry = player.level.registryAccess().registryOrThrow(Registries.BLOCK);
        if (player != null && !player.getAbilities().mayBuild && !self().hasAdventureModePlaceTagForBlock(registry, new BlockInWorld(context.getLevel(), pos, false))) {
            return InteractionResult.PASS;
        } else {
            Item item = self().getItem();
            InteractionResult result = item.onItemUseFirst(self(), context);
            if (player != null && result == InteractionResult.SUCCESS) {
                player.awardStat(Stats.ITEM_USED.get(item));
            }

            return result;
        }
    }
}
