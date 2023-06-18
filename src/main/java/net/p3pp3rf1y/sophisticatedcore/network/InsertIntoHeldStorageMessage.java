package net.p3pp3rf1y.sophisticatedcore.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.api.IStashStorageItem;

public class InsertIntoHeldStorageMessage extends SimplePacketBase {
	private final int slotIndex;

	public InsertIntoHeldStorageMessage(int slotIndex) {
		this.slotIndex = slotIndex;
	}

	public InsertIntoHeldStorageMessage(FriendlyByteBuf buffer) {
		this(buffer.readInt());
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeInt(this.slotIndex);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player == null) {
				return;
			}

			AbstractContainerMenu containerMenu = player.containerMenu;
			ItemStack storageStack = containerMenu.getCarried();
			if (storageStack.getItem() instanceof IStashStorageItem stashStorageItem) {
				Slot slot = containerMenu.getSlot(slotIndex);
				ItemStack stackToStash = slot.getItem();
				ItemStack stashResult = stashStorageItem.stash(storageStack, stackToStash);
				slot.set(stashResult);
				slot.onTake(player, stashResult);
			}
		});
		return true;
	}

}
