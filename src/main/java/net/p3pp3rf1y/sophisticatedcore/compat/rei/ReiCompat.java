package net.p3pp3rf1y.sophisticatedcore.compat.rei;

import io.github.fabricators_of_create.porting_lib.util.NetworkDirection;
import net.p3pp3rf1y.sophisticatedcore.compat.ICompat;
import net.p3pp3rf1y.sophisticatedcore.network.PacketHandler;

public class ReiCompat implements ICompat {
	@Override
	public void setup() {
		PacketHandler.registerMessage(TransferRecipeMessage.class, TransferRecipeMessage::new, NetworkDirection.PLAY_TO_SERVER);
		PacketHandler.registerMessage(SetGhostSlotMessage.class, SetGhostSlotMessage::new, NetworkDirection.PLAY_TO_SERVER);
		PacketHandler.registerMessage(SetMemorySlotMessage.class, SetMemorySlotMessage::new, NetworkDirection.PLAY_TO_SERVER);
	}
}
