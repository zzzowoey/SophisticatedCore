package net.p3pp3rf1y.sophisticatedcore.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.p3pp3rf1y.sophisticatedcore.settings.SettingsManager;

import java.util.function.BiConsumer;
import javax.annotation.Nullable;

public class SyncPlayerSettingsMessage extends SimplePacketBase {
	private final String playerTagName;
	@Nullable
	private final CompoundTag settingsNbt;

	public SyncPlayerSettingsMessage(String playerTagName, @Nullable CompoundTag settingsNbt) {
		this.playerTagName = playerTagName;
		this.settingsNbt = settingsNbt;
	}

	public SyncPlayerSettingsMessage(FriendlyByteBuf buffer) {
		this(buffer.readUtf(), buffer.readNbt());
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUtf(playerTagName);
		buffer.writeNbt(settingsNbt);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			Player player = context.getClientPlayer();
			if (player == null || settingsNbt == null) {
				return;
			}
			//need to call the static call indirectly otherwise this message class is class loaded during packethandler init and crashes on server due to missing ClientPlayerEntity
			BiConsumer<Player, CompoundTag> setSettings = (p, settingsNbt1) -> SettingsManager.setPlayerSettingsTag(p, playerTagName, settingsNbt1);
			setSettings.accept(player, settingsNbt);
		});
		return true;
	}

}
