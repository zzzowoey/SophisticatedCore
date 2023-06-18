package net.p3pp3rf1y.sophisticatedcore.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.p3pp3rf1y.sophisticatedcore.settings.SettingsTemplateStorage;

import java.util.HashMap;
import java.util.Map;

public class SyncTemplateSettingsMessage extends SimplePacketBase {
	private final Map<Integer, CompoundTag> playerTemplates;

	public SyncTemplateSettingsMessage(Map<Integer, CompoundTag> playerTemplates) {
		this.playerTemplates = playerTemplates;
	}
	public SyncTemplateSettingsMessage(FriendlyByteBuf buffer) {
		int size = buffer.readInt();
		playerTemplates = new HashMap<>(size);
		for (int i = 0 ; i < size; i++) {
			playerTemplates.put(buffer.readInt(), buffer.readNbt());
		}
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeInt(playerTemplates.size());
		playerTemplates.forEach((k, v) -> {
			buffer.writeInt(k);
			buffer.writeNbt(v);
		});
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			LocalPlayer player = Minecraft.getInstance().player;
			if (player == null) {
				return;
			}

			SettingsTemplateStorage settingsTemplateStorage = SettingsTemplateStorage.get();
			playerTemplates.forEach((k, v) -> settingsTemplateStorage.putPlayerTemplate(player, k, v));
		});
		return true;
	}

}
