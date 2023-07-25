package net.p3pp3rf1y.sophisticatedcore.network;

import io.github.fabricators_of_create.porting_lib.util.NetworkDirection;
import me.pepperbell.simplenetworking.C2SPacket;
import me.pepperbell.simplenetworking.S2CPacket;
import me.pepperbell.simplenetworking.SimpleChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import net.p3pp3rf1y.sophisticatedcore.compat.jei.TransferRecipeMessage;
import net.p3pp3rf1y.sophisticatedcore.upgrades.jukebox.PlayDiscMessage;
import net.p3pp3rf1y.sophisticatedcore.upgrades.jukebox.SoundStopNotificationMessage;
import net.p3pp3rf1y.sophisticatedcore.upgrades.jukebox.StopDiscPlaybackMessage;
import net.p3pp3rf1y.sophisticatedcore.upgrades.tank.TankClickMessage;

import java.util.function.Function;

import static io.github.fabricators_of_create.porting_lib.util.NetworkDirection.PLAY_TO_CLIENT;
import static io.github.fabricators_of_create.porting_lib.util.NetworkDirection.PLAY_TO_SERVER;

public class PacketHandler {
	public static final ResourceLocation CHANNEL_NAME = SophisticatedCore.getRL("channel");
	private static SimpleChannel channel;

	public static void init() {
		channel = new SimpleChannel(CHANNEL_NAME);
		channel.initServerListener();

		registerMessage(SyncContainerClientDataMessage.class, SyncContainerClientDataMessage::new, PLAY_TO_SERVER);
		registerMessage(TransferFullSlotMessage.class, TransferFullSlotMessage::new, PLAY_TO_SERVER);
		registerMessage(SyncContainerStacksMessage.class, SyncContainerStacksMessage::new, PLAY_TO_CLIENT);
		registerMessage(SyncSlotStackMessage.class, SyncSlotStackMessage::new, PLAY_TO_CLIENT);
		registerMessage(SyncPlayerSettingsMessage.class, SyncPlayerSettingsMessage::new, PLAY_TO_CLIENT);
		registerMessage(PlayDiscMessage.class, PlayDiscMessage::new, PLAY_TO_CLIENT);
		registerMessage(StopDiscPlaybackMessage.class, StopDiscPlaybackMessage::new, PLAY_TO_CLIENT);
		registerMessage(SoundStopNotificationMessage.class, SoundStopNotificationMessage::new, PLAY_TO_SERVER);
		registerMessage(TankClickMessage.class, TankClickMessage::new, PLAY_TO_SERVER);
		registerMessage(SyncTemplateSettingsMessage.class, SyncTemplateSettingsMessage::new, PLAY_TO_CLIENT);
		registerMessage(SyncAdditionalSlotInfoMessage.class, SyncAdditionalSlotInfoMessage::new, PLAY_TO_CLIENT);
		registerMessage(SyncEmptySlotIconsMessage.class, SyncEmptySlotIconsMessage::new, PLAY_TO_CLIENT);
		registerMessage(SyncSlotChangeErrorMessage.class, SyncSlotChangeErrorMessage::new, PLAY_TO_CLIENT);
		registerMessage(TransferRecipeMessage.class, TransferRecipeMessage::new, PLAY_TO_SERVER);
	}

	public static <T extends SimplePacketBase> void registerMessage(Class<T> type, Function<FriendlyByteBuf, T> factory, NetworkDirection direction) {
		PacketType<T> packet = new PacketType<>(type, factory, direction);
		packet.register();
	}

	public static SimpleChannel getChannel() {
		return channel;
	}

	public static void sendToServer(Object message) {
		getChannel().sendToServer((C2SPacket) message);
	}

	public static void sendToClient(ServerPlayer player, Object message) {
		getChannel().sendToClient((S2CPacket) message, player);
	}

	public static void sendToAllNear(ServerLevel world, BlockPos pos, int range, Object message) {
		getChannel().sendToClientsAround((S2CPacket) message, world, pos, range);
	}
	public static void sendToAllNear(ServerLevel world, Vec3 pos, int range, Object message) {
		getChannel().sendToClientsAround((S2CPacket) message, world, pos, range);
	}

	public static class PacketType<T extends SimplePacketBase> {
		private static int index = 0;

		private Function<FriendlyByteBuf, T> decoder;
		private Class<T> type;
		private NetworkDirection direction;

		public PacketType(Class<T> type, Function<FriendlyByteBuf, T> factory, NetworkDirection direction) {
			decoder = factory;
			this.type = type;
			this.direction = direction;
		}

		public void register() {
			switch (direction) {
				case PLAY_TO_CLIENT -> getChannel().registerS2CPacket(type, index++, decoder);
				case PLAY_TO_SERVER -> getChannel().registerC2SPacket(type, index++, decoder);
			}
		}
	}
}
