package net.p3pp3rf1y.sophisticatedcore.network;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.p3pp3rf1y.sophisticatedcore.common.gui.IAdditionalSlotInfoMenu;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class SyncEmptySlotIconsMessage extends SimplePacketBase {
	private final Map<ResourceLocation, Set<Integer>> emptySlotIcons;

	public SyncEmptySlotIconsMessage(Map<ResourceLocation, Set<Integer>> emptySlotIcons) {
		this.emptySlotIcons = emptySlotIcons;
	}

	public SyncEmptySlotIconsMessage(FriendlyByteBuf buffer) {
		Map<ResourceLocation, Set<Integer>> map = new HashMap<>();

		int size = buffer.readInt();
		for (int i = 0; i < size; i++) {
			ResourceLocation resourceLocation = buffer.readResourceLocation();
			map.put(resourceLocation, Arrays.stream(buffer.readVarIntArray()).boxed().collect(Collectors.toSet()));
		}

		Map<ResourceLocation, Set<Integer>> emptySlotIcons1 = map;
		this.emptySlotIcons = emptySlotIcons1;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeInt(emptySlotIcons.size());

		for (Map.Entry<ResourceLocation, Set<Integer>> entry : emptySlotIcons.entrySet()) {
			buffer.writeResourceLocation(entry.getKey());
			buffer.writeVarIntArray(entry.getValue().stream().mapToInt(i -> i).toArray());
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			Player player = context.getClientPlayer();
			if (player == null || !(player.containerMenu instanceof IAdditionalSlotInfoMenu menu)) {
				return;
			}
			menu.updateEmptySlotIcons(emptySlotIcons);
		});
		return true;
	}

}
