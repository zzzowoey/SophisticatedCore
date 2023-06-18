package net.p3pp3rf1y.sophisticatedcore.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.p3pp3rf1y.sophisticatedcore.settings.SettingsManager;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;

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
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			LocalPlayer localPlayer = Minecraft.getInstance().player;
			if (localPlayer == null || settingsNbt == null) {
				return;
			}
			//need to call the static call indirectly otherwise this message class is class loaded during packethandler init and crashes on server due to missing ClientPlayerEntity
			BiConsumer<Player, CompoundTag> setSettings = (player, settingsNbt1) -> SettingsManager.setPlayerSettingsTag(player, playerTagName, settingsNbt1);
			setSettings.accept(localPlayer, settingsNbt);
		});
		return true;
	}

}
