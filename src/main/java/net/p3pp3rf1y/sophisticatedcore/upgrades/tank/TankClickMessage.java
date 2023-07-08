package net.p3pp3rf1y.sophisticatedcore.upgrades.tank;

import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
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
			ContainerItemContext cic = ContainerItemContext.withInitial(cursorStack);
			Storage<FluidVariant> storage = cic.find(FluidStorage.ITEM);
			if (storage != null) {
				TankUpgradeWrapper tankWrapper = tankContainer.getUpgradeWrapper();
				FluidStack tankContents = tankWrapper.getContents();
				if (tankContents.isEmpty()) {
					drainHandler(sender, containerMenu, cic, storage, tankWrapper);
				} else {
					if (!tankWrapper.fillHandler(cic, storage, itemStackIn -> {
						containerMenu.setCarried(itemStackIn);
						sender.connection.send(new ClientboundContainerSetSlotPacket(-1, containerMenu.incrementStateId(), -1, containerMenu.getCarried()));
					})) {
						drainHandler(sender, containerMenu, cic, storage, tankWrapper);
					}
				}
			};
		});
		return true;
	}

	private static void drainHandler(ServerPlayer sender, AbstractContainerMenu containerMenu, ContainerItemContext cic, Storage<FluidVariant> storage, TankUpgradeWrapper tankWrapper) {
		tankWrapper.drainHandler(cic, storage, itemStackIn -> {
			containerMenu.setCarried(itemStackIn);
			sender.connection.send(new ClientboundContainerSetSlotPacket(-1, containerMenu.incrementStateId(), -1, containerMenu.getCarried()));
		});
	}
}