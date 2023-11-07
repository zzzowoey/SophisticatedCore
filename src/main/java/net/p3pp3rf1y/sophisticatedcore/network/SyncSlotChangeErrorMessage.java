package net.p3pp3rf1y.sophisticatedcore.network;

import java.util.Arrays;
import java.util.stream.Collectors;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeSlotChangeResult;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class SyncSlotChangeErrorMessage extends SimplePacketBase {
	private final UpgradeSlotChangeResult slotChangeError;

	public SyncSlotChangeErrorMessage(UpgradeSlotChangeResult slotChangeError) {
		this.slotChangeError = slotChangeError;
	}

	public SyncSlotChangeErrorMessage(FriendlyByteBuf packetBuffer) {
		this(new UpgradeSlotChangeResult.Fail(packetBuffer.readComponent(),
				Arrays.stream(packetBuffer.readVarIntArray()).boxed().collect(Collectors.toSet()),
				Arrays.stream(packetBuffer.readVarIntArray()).boxed().collect(Collectors.toSet()),
				Arrays.stream(packetBuffer.readVarIntArray()).boxed().collect(Collectors.toSet())));
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		writeSlotChangeResult(buffer, slotChangeError);
	}

	private static void writeSlotChangeResult(FriendlyByteBuf packetBuffer, UpgradeSlotChangeResult slotChangeResult) {
		packetBuffer.writeComponent(slotChangeResult.getErrorMessage().orElse(Component.empty()));
		packetBuffer.writeVarIntArray(slotChangeResult.getErrorUpgradeSlots().stream().mapToInt(i -> i).toArray());
		packetBuffer.writeVarIntArray(slotChangeResult.getErrorInventorySlots().stream().mapToInt(i -> i).toArray());
		packetBuffer.writeVarIntArray(slotChangeResult.getErrorInventoryParts().stream().mapToInt(i -> i).toArray());
	}


	@Override
	@Environment(EnvType.CLIENT)
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			Player player = context.getClientPlayer();
			if (player == null || !(player.containerMenu instanceof StorageContainerMenuBase<?> menu)) {
				return;
			}
			menu.updateSlotChangeError(slotChangeError);
		});
		return true;
	}

}
