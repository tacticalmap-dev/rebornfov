package com.flowingsun.rebornfov.client;

import com.flowingsun.rebornfov.config.SupplyPresetManager;
import com.flowingsun.rebornfov.menu.FovPresetMenu;
import com.flowingsun.rebornfov.network.ModNetwork;
import com.flowingsun.rebornfov.network.SelectPresetPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class FovPresetScreen extends AbstractContainerScreen<FovPresetMenu> {
    private static final int ROW_HEIGHT = 20;

    public FovPresetScreen(FovPresetMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 220;
        this.imageHeight = 170;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xCC202020);
        graphics.fill(leftPos + 8, topPos + 18, leftPos + imageWidth - 8, topPos + imageHeight - 10, 0xCC303030);
        for (int i = 0; i < menu.getPresets().size(); i++) {
            int y = topPos + 24 + i * ROW_HEIGHT;
            SupplyPresetManager.SupplyPreset preset = menu.getPresets().get(i);
            int color = preset.id().equals(menu.getSelectedPreset()) ? 0xAA2F6F2F : 0x664A4A4A;
            if (isMouseOverRow(mouseX, mouseY, i)) {
                color = 0x887A7A22;
            }
            graphics.fill(leftPos + 12, y, leftPos + imageWidth - 12, y + ROW_HEIGHT - 2, color);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (int i = 0; i < menu.getPresets().size(); i++) {
                if (isMouseOverRow(mouseX, mouseY, i)) {
                    SupplyPresetManager.SupplyPreset preset = menu.getPresets().get(i);
                    ModNetwork.CHANNEL.sendToServer(new SelectPresetPacket(menu.getBlockPos(), preset.id()));
                    minecraft.player.closeContainer();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isMouseOverRow(double mouseX, double mouseY, int index) {
        int y = topPos + 24 + index * ROW_HEIGHT;
        return mouseX >= leftPos + 12 && mouseX <= leftPos + imageWidth - 12 && mouseY >= y && mouseY <= y + ROW_HEIGHT - 2;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 10, 6, 0xFFFFFF, false);
        graphics.drawString(font, Component.translatable("screen.rebornfov.preset_hint"), 10, imageHeight - 18, 0xC0C0C0, false);
        for (int i = 0; i < menu.getPresets().size(); i++) {
            SupplyPresetManager.SupplyPreset preset = menu.getPresets().get(i);
            graphics.drawString(font, preset.displayName(), 16, 28 + i * ROW_HEIGHT, 0xFFFFFF, false);
            graphics.drawString(font, preset.id(), 160, 28 + i * ROW_HEIGHT, 0xFFD27F, false);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
