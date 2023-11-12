package net.p3pp3rf1y.sophisticatedcore.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedcore.settings.SettingsTemplateStorage;

import java.util.HashMap;
import java.util.Map;

public class SyncTemplateSettingsMessage extends SimplePacketBase {
	private final Map<Integer, CompoundTag> playerTemplates;
	private final Map<String, CompoundTag> playerNamedTemplates;

	public SyncTemplateSettingsMessage(Map<Integer, CompoundTag> playerTemplates, Map<String, CompoundTag> playerNamedTemplates) {
		this.playerTemplates = playerTemplates;
		this.playerNamedTemplates = playerNamedTemplates;
	}
	public SyncTemplateSettingsMessage(FriendlyByteBuf buffer) {
		int size = buffer.readInt();
		this.playerTemplates = new HashMap<>(size);
		for (int i = 0 ; i < size; i++) {
			this.playerTemplates.put(buffer.readInt(), buffer.readNbt());
		}
		size = buffer.readInt();
		this.playerNamedTemplates = new HashMap<>(size);
		for (int i = 0; i < size; i++) {
			this.playerNamedTemplates.put(buffer.readUtf(), buffer.readNbt());
		}
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeInt(this.playerTemplates.size());
		this.playerTemplates.forEach((k, v) -> {
			buffer.writeInt(k);
			buffer.writeNbt(v);
		});
		buffer.writeInt(this.playerNamedTemplates.size());
		this.playerNamedTemplates.forEach((k, v) -> {
			buffer.writeUtf(k);
			buffer.writeNbt(v);
		});
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			Player player = context.getClientPlayer();
			if (player == null) {
				return;
			}

			SettingsTemplateStorage settingsTemplateStorage = SettingsTemplateStorage.get();
			settingsTemplateStorage.clearPlayerTemplates(player);
			playerTemplates.forEach((k, v) -> settingsTemplateStorage.putPlayerTemplate(player, k, v));
			playerNamedTemplates.forEach((k, v) -> settingsTemplateStorage.putPlayerNamedTemplate(player, k, v));
			if (player.containerMenu instanceof SettingsContainerMenu<?> settingsContainerMenu) {
				settingsContainerMenu.refreshTemplateSlots();
			}
		});
		return true;
	}
}
