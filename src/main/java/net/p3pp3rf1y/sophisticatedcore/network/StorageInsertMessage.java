package net.p3pp3rf1y.sophisticatedcore.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.api.IStashStorageItem;

public class StorageInsertMessage extends SimplePacketBase {
	private final int slotIndex;

	public StorageInsertMessage(int slotIndex) {
		this.slotIndex = slotIndex;
	}

	public StorageInsertMessage(FriendlyByteBuf buffer) {
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
			ItemStack storageStack = containerMenu.getSlot(slotIndex).getItem();
			if (storageStack.getItem() instanceof IStashStorageItem stashStorageItem) {
				ItemStack heldItem = containerMenu.getCarried();
				containerMenu.setCarried(stashStorageItem.stash(storageStack, heldItem));
			}
		});
		return true;
	}

}
