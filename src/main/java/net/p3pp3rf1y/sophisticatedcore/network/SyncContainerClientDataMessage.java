package net.p3pp3rf1y.sophisticatedcore.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.p3pp3rf1y.sophisticatedcore.common.gui.ISyncedContainer;

import javax.annotation.Nullable;

public class SyncContainerClientDataMessage extends SimplePacketBase {
	@Nullable
	private final CompoundTag data;

	public SyncContainerClientDataMessage(@Nullable CompoundTag data) {
		this.data = data;
	}

	public SyncContainerClientDataMessage(FriendlyByteBuf buffer) {
		this(buffer.readNbt());
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeNbt(data);
	}

	public boolean handle(Context context) {
		context.enqueueWork(() -> handleMessage(context.sender(), this));
		return true;
	}

	private static void handleMessage(@Nullable ServerPlayer sender, SyncContainerClientDataMessage message) {
		if (sender == null || message.data == null) {
			return;
		}

		if (sender.containerMenu instanceof ISyncedContainer container) {
			container.handleMessage(message.data);
		}
	}
}
