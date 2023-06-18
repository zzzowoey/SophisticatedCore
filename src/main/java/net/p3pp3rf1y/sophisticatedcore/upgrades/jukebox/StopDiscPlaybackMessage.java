package net.p3pp3rf1y.sophisticatedcore.upgrades.jukebox;

import net.minecraft.network.FriendlyByteBuf;
import net.p3pp3rf1y.sophisticatedcore.network.SimplePacketBase;

import java.util.UUID;

public class StopDiscPlaybackMessage extends SimplePacketBase {
	private final UUID storageUuid;

	public StopDiscPlaybackMessage(UUID storageUuid) {
		this.storageUuid = storageUuid;
	}

	public StopDiscPlaybackMessage(FriendlyByteBuf buffer) {
		this(buffer.readUUID());
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUUID(this.storageUuid);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> StorageSoundHandler.stopStorageSound(storageUuid));
		return true;
	}
}
