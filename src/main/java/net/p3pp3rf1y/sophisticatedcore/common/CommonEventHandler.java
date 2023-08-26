package net.p3pp3rf1y.sophisticatedcore.common;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.p3pp3rf1y.sophisticatedcore.init.ModFluids;
import net.p3pp3rf1y.sophisticatedcore.init.ModParticles;
import net.p3pp3rf1y.sophisticatedcore.init.ModRecipes;

public class CommonEventHandler {
	public void registerHandlers() {
		ModFluids.registerHandlers();
		ModParticles.registerParticles();
		ModRecipes.registerHandlers();

		UseBlockCallback.EVENT.register(this::onUseBlock);
	}

	private InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
		if (player.isSpectator()) {
			return InteractionResult.PASS;
		}

		ItemStack stack = player.getItemInHand(hand);
		if (stack.isEmpty()) {
			return InteractionResult.PASS;
		}

		UseOnContext context = new UseOnContext(player, hand, hitResult);
		return stack.onItemUseFirst(context);
	}
}
