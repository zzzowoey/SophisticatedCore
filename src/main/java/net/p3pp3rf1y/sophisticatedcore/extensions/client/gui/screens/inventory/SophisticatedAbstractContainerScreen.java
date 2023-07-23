package net.p3pp3rf1y.sophisticatedcore.extensions.client.gui.screens.inventory;

public interface SophisticatedAbstractContainerScreen {
    int slotColor = -2130706433;
    default int getSlotColor(int slotId) {
        return slotColor;
    }

    default int getGuiLeft() { return 0; }
    default int getGuiTop() { return 0; }
}
