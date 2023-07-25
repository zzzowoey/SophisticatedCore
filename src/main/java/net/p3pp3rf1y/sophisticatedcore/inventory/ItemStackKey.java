package net.p3pp3rf1y.sophisticatedcore.inventory;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.world.item.ItemStack;

public record ItemStackKey(ItemStack stack) {
	public ItemStack getStack() {
		return stack;
	}

	public ItemStackKey(ItemStack stack) {
		this.stack = stack.copy();
		this.stack.setCount(1);
	}

	public ItemStackKey(ItemVariant resource) {
		this(resource.toStack());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {return true;}
		if (o == null || getClass() != o.getClass()) {return false;}
		ItemStackKey that = (ItemStackKey) o;
		return canItemStacksStack(stack, that.stack);
	}

	public static boolean canItemStacksStack(ItemStack a, ItemStack b) {
		if (a.isEmpty() || !a.sameItem(b) || a.hasTag() != b.hasTag()) {
			return false;
		}

		//noinspection DataFlowIssue
		return (!a.hasTag() || a.getTag().equals(b.getTag())); /*&& Objects.equals(getCapNbt(a), getCapNbt(b));*/
	}

	public boolean hashCodeNotEquals(ItemStack otherStack) {
		return hashCode() != getHashCode(otherStack);
	}

	@Override
	public int hashCode() {
		return getHashCode(stack);
	}

	public static int getHashCode(ItemStack stack) {
		int hash = stack.getItem().hashCode();
		if (stack.hasTag()) {
			//noinspection ConstantConditions - hasTag call makes sure getTag doesn't return null
			hash = hash * 31 + stack.getTag().hashCode();
		}
		return hash;
	}

	public static int getHashCode(ItemVariant resource) {
		return getHashCode(resource.toStack());
	}

	public boolean matches(ItemVariant resource) {
		return hashCode() == getHashCode(resource);
	}

	public boolean matches(ItemStack stack) {
		return hashCode() == getHashCode(stack);
	}
}
