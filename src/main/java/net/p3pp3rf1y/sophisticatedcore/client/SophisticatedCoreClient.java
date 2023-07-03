package net.p3pp3rf1y.sophisticatedcore.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.fabricators_of_create.porting_lib.util.ScreenHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.api.IStashStorageItem;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.TranslationHelper;
import net.p3pp3rf1y.sophisticatedcore.client.init.ModFluids;
import net.p3pp3rf1y.sophisticatedcore.client.init.ModParticles;
import net.p3pp3rf1y.sophisticatedcore.network.PacketHandler;
import net.p3pp3rf1y.sophisticatedcore.upgrades.jukebox.StorageSoundHandler;
import net.p3pp3rf1y.sophisticatedcore.util.RecipeHelper;

import java.util.Collections;
import java.util.Optional;

@SuppressWarnings("unused")
public class SophisticatedCoreClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModParticles.registerFactories();
        ModFluids.registerFluids();

        ServerWorldEvents.UNLOAD.register(StorageSoundHandler::onWorldUnload);
        ClientTickEvents.START_CLIENT_TICK.register(StorageSoundHandler::tick);

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> RecipeHelper.setWorld(client.level));

        ScreenEvents.AFTER_INIT.register((Minecraft client, Screen screen, int windowWidth, int windowHeight) -> {
            if (!(screen instanceof AbstractContainerScreen<?> containerGui) || screen instanceof CreativeModeInventoryScreen || client.player == null) {
                return;
            }

            ScreenEvents.afterRender(screen).register(SophisticatedCoreClient::onDrawScreen);
        });

        PacketHandler.getChannel().initClientListener();
    }

    private static void onDrawScreen(Screen screen, PoseStack poseStack, int mouseX, int mouseY, float tickDelta) {
        Minecraft mc = ScreenHelper.getClient(screen);
        AbstractContainerScreen<?> containerGui = (AbstractContainerScreen<?>)screen;

        AbstractContainerMenu menu = containerGui.getMenu();
        ItemStack held = menu.getCarried();
        if (!held.isEmpty()) {
            Slot under = GuiHelper.getSlotUnderMouse(containerGui).orElse(null);

            for (Slot s : menu.slots) {
                ItemStack stack = s.getItem();
                if (!s.mayPickup(mc.player) || stack.isEmpty()) {
                    continue;
                }
                Optional<TooltipComponent> tooltip = getStashableAndTooltip(stack, held);
                if (tooltip.isEmpty()) {
                    continue;
                }

                if (s == under) {
                    renderSpecialTooltip(mc, containerGui, poseStack, mouseX, mouseY, tooltip);
                } else {
                    renderStashSign(mc, containerGui, poseStack, s, stack);
                }
            }
        }
    }

    private static void renderStashSign(Minecraft mc, AbstractContainerScreen<?> containerGui, PoseStack poseStack, Slot s, ItemStack stack) {
        int x = GuiHelper.getGuiLeft(containerGui) + s.x;
        int y = GuiHelper.getGuiTop(containerGui) + s.y;

        poseStack.pushPose();
        poseStack.translate(0, 0, containerGui instanceof StorageScreenBase ? 100 : 499);

        if (stack.getItem() instanceof IStashStorageItem) {
            mc.font.drawShadow(poseStack, "+", (float) x + 10, (float) y + 8, 0xFFFF00);
        } else {
            mc.font.drawShadow(poseStack, "-", x + 1, y, 0xFFFF00);
        }
        poseStack.popPose();
    }

    private static void renderSpecialTooltip(Minecraft mc, AbstractContainerScreen<?> containerGui, PoseStack poseStack, int mouseX, int mouseY, Optional<TooltipComponent> tooltip) {
        poseStack.pushPose();
        poseStack.translate(0, 0, containerGui instanceof StorageScreenBase ? -100 : 100);
        containerGui.renderTooltip(poseStack, Collections.singletonList(Component.translatable(TranslationHelper.INSTANCE.translItemTooltip("storage") + ".right_click_to_add_to_storage")), tooltip, mouseX, mouseY);
        poseStack.popPose();
    }

    private static Optional<TooltipComponent> getStashableAndTooltip(ItemStack inInventory, ItemStack held) {
        if (inInventory.getCount() == 1 && inInventory.getItem() instanceof IStashStorageItem stashStorageItem && stashStorageItem.isItemStashable(inInventory, held)) {
            return stashStorageItem.getInventoryTooltip(inInventory);
        }

        if (held.getItem() instanceof IStashStorageItem stashStorageItem && stashStorageItem.isItemStashable(held, inInventory)) {
            return stashStorageItem.getInventoryTooltip(held);
        }
        return Optional.empty();
    }
}