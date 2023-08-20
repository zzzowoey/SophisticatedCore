package net.p3pp3rf1y.sophisticatedcore.util;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import javax.annotation.Nullable;

public class MenuProviderHelper {
    public interface ContextProvider {
        void toBuffer(FriendlyByteBuf buffer);
    }

    public static <T extends ContextProvider> ExtendedScreenHandlerFactory createMenuProvider(MenuConstructor<T> menuConstructor, T context, Component name) {
        return new ExtendedScreenHandlerFactory() {
            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
                return menuConstructor.createMenu(i, context, player);
            }

            @Override
            public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
                context.toBuffer(buf);
            }

            @Override
            public Component getDisplayName() {
                return name;
            }
        };
    }

    @FunctionalInterface
    public interface MenuConstructor<T extends ContextProvider> {
        @Nullable
        AbstractContainerMenu createMenu(int i, T context, Player player);
    }
}
