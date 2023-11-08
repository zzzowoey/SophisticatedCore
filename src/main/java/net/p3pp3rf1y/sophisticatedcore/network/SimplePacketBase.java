package net.p3pp3rf1y.sophisticatedcore.network;

import me.pepperbell.simplenetworking.C2SPacket;
import me.pepperbell.simplenetworking.S2CPacket;
import me.pepperbell.simplenetworking.SimpleChannel;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;

import java.util.concurrent.Executor;
import javax.annotation.Nullable;

public abstract class SimplePacketBase implements C2SPacket, S2CPacket {
    public abstract void write(FriendlyByteBuf buffer);

    public abstract boolean handle(Context context);

    @Override
    public final void encode(FriendlyByteBuf buffer) {
        write(buffer);
    }

    @Override
    public void handle(Minecraft client, ClientPacketListener listener, PacketSender responseSender, SimpleChannel channel) {
        handle(new Context(client, listener, null, client.player));
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl listener, PacketSender responseSender, SimpleChannel channel) {
        handle(new Context(server, listener, player, null));
    }

    public enum NetworkDirection {
        PLAY_TO_CLIENT,
        PLAY_TO_SERVER
    }

    public record Context(Executor exec, PacketListener listener, @Nullable ServerPlayer sender, @Nullable Player clientPlayer) {
        public void enqueueWork(Runnable runnable) {
            exec().execute(runnable);
        }

        @Nullable
        public ServerPlayer getSender() {
            return sender();
        }

        @Nullable
        public Player getClientPlayer() { return clientPlayer(); }

        public NetworkDirection getDirection() {
            return sender() == null ? NetworkDirection.PLAY_TO_SERVER : NetworkDirection.PLAY_TO_CLIENT;
        }
    }
}
