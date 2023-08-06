package net.p3pp3rf1y.sophisticatedcore.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.*;

import javax.annotation.Nullable;
import java.util.Objects;

public class ItemStackHelper {
	public static boolean areItemStackTagsEqualIgnoreDurability(ItemStack stackA, ItemStack stackB) {
		if (stackA.isEmpty() && stackB.isEmpty()) {
			return true;
		} else if (!stackA.isEmpty() && !stackB.isEmpty()) {
			if (stackA.getTag() == null && stackB.getTag() != null) {
				return false;
			} else {
				return (stackA.getTag() == null || areTagsEqualIgnoreDurability(stackA.getTag(), stackB.getTag()));
			}
		} else {
			return false;
		}
	}

	public static boolean areTagsEqualIgnoreDurability(CompoundTag tagA, @Nullable CompoundTag tagB) {
		if (tagA == tagB) {
			return true;
		}
		if (tagB == null || tagA.size() != tagB.size()) {
			return false;
		}

		for (String key : tagA.getAllKeys()) {
			if (!tagB.contains(key)) {
				return false;
			}
			if (key.equals("Damage")) {
				continue;
			}
			if (!Objects.equals(tagA.get(key), tagB.get(key))) {
				return false;
			}
		}
		return true;
	}

	public static ItemStack copyStackWithSize(ItemStack stack, int size) {
		if (size == 0) {
			return ItemStack.EMPTY;
		}

		ItemStack copy = stack.copy();
		copy.setCount(size);

		return copy;
	}

	public static boolean canItemStacksStack(ItemStack a, ItemStack b) {
		if (a.isEmpty() || !a.sameItem(b) || a.hasTag() != b.hasTag()) {
			return false;
		}

		//noinspection DataFlowIssue
		return (!a.hasTag() || a.getTag().equals(b.getTag())); /*&& Objects.equals(getCapNbt(a), getCapNbt(b));*/
	}
}
