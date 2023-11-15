package net.p3pp3rf1y.sophisticatedcore.mixin.common;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.level.block.Block;
import net.p3pp3rf1y.sophisticatedcore.extensions.block.SophisticatedBlock;

@Mixin(Block.class)
public class BlockMixin implements SophisticatedBlock {
}
