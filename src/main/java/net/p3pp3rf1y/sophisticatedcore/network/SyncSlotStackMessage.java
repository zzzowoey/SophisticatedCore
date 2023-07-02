package net.p3pp3rf1y.sophisticatedcore.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;

public class SyncSlotStackMessage  extends SimplePacketBase{
	private final int windowId;
	private final int stateId;
	private final int slotNumber;
	private final ItemStack stack;

	public SyncSlotStackMessage(int windowId, int stateId, int slotNumber, ItemStack stack) {
		this.windowId = windowId;
		this.stateId = stateId;
		this.slotNumber = slotNumber;
		this.stack = stack;
	}

	public SyncSlotStackMessage(FriendlyByteBuf buffer) {
		this(buffer.readInt(), buffer.readVarInt(), buffer.readShort(), PacketHelper.readItemStack(buffer));
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeInt(windowId);
		buffer.writeVarInt(stateId);
		buffer.writeShort(slotNumber);
		PacketHelper.writeItemStack(stack, buffer);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			LocalPlayer player = Minecraft.getInstance().player;
			if (player == null || !(player.containerMenu instanceof StorageContainerMenuBase || player.containerMenu instanceof SettingsContainerMenu) || player.containerMenu.containerId != windowId) {
				return;
			}
			player.containerMenu.setItem(slotNumber, stateId, stack);
		});
		return true;
	}
}
