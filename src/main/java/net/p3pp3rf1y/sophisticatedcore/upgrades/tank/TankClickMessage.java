// TODO: Reimplement
/*
package net.p3pp3rf1y.sophisticatedcore.upgrades.tank;

import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerBase;
import net.p3pp3rf1y.sophisticatedcore.network.SimplePacketBase;

public class TankClickMessage extends SimplePacketBase {
	private final int upgradeSlot;

	public TankClickMessage(int upgradeSlot) {
		this.upgradeSlot = upgradeSlot;
	}

	public TankClickMessage(FriendlyByteBuf buffer) {
		this(buffer.readInt());
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeInt(this.upgradeSlot);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			ServerPlayer sender = context.getSender();
			if (sender == null || !(sender.containerMenu instanceof StorageContainerMenuBase)) {
				return;
			}
			AbstractContainerMenu containerMenu = sender.containerMenu;
			UpgradeContainerBase<?, ?> upgradeContainer = ((StorageContainerMenuBase<?>) containerMenu).getUpgradeContainers().get(upgradeSlot);
			if (!(upgradeContainer instanceof TankUpgradeContainer tankContainer)) {
				return;
			}
			ItemStack cursorStack = containerMenu.getCarried();
			cursorStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(fluidHandler -> {
				TankUpgradeWrapper tankWrapper = tankContainer.getUpgradeWrapper();
				FluidStack tankContents = tankWrapper.getContents();
				if (tankContents.isEmpty()) {
					drainHandler(sender, containerMenu, fluidHandler, tankWrapper);
				} else {
					if (!tankWrapper.fillHandler(fluidHandler, itemStackIn -> {
						containerMenu.setCarried(itemStackIn);
						sender.connection.send(new ClientboundContainerSetSlotPacket(-1, containerMenu.incrementStateId(), -1, containerMenu.getCarried()));
					})) {
						drainHandler(sender, containerMenu, fluidHandler, tankWrapper);
					}
				}
			});
		});
		return true;
	}

	private static void drainHandler(ServerPlayer sender, AbstractContainerMenu containerMenu, IFluidHandlerItem fluidHandler, TankUpgradeWrapper tankWrapper) {
		tankWrapper.drainHandler(fluidHandler, itemStackIn -> {
			containerMenu.setCarried(itemStackIn);
			sender.connection.send(new ClientboundContainerSetSlotPacket(-1, containerMenu.incrementStateId(), -1, containerMenu.getCarried()));
		});
	}
}
*/
