package com.flowingsun.rebornfov.client;

import com.flowingsun.rebornfov.data.TeleportTarget;
import com.flowingsun.rebornfov.menu.BaseMenu;
import com.flowingsun.rebornfov.network.ModNetwork;
import com.flowingsun.rebornfov.network.TeleportRequestPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BaseScreen extends AbstractContainerScreen<BaseMenu> {
    private static final int ROW_HEIGHT = 18;
    private int lastClickIndex = -1;
    private long lastClickTime;

    public BaseScreen(BaseMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 220;
        this.imageHeight = 180;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xCC202020);
        graphics.fill(leftPos + 8, topPos + 18, leftPos + imageWidth - 8, topPos + imageHeight - 10, 0xCC303030);
        for (int i = 0; i < menu.getTargets().size(); i++) {
            int y = topPos + 22 + i * ROW_HEIGHT;
            int color = 0x664A4A4A;
            if (isMouseOverRow(mouseX, mouseY, i)) {
                color = 0x887A7A22;
            }
            graphics.fill(leftPos + 12, y, leftPos + imageWidth - 12, y + ROW_HEIGHT - 2, color);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (int i = 0; i < menu.getTargets().size(); i++) {
                if (isMouseOverRow(mouseX, mouseY, i)) {
                    long now = System.currentTimeMillis();
                    if (lastClickIndex == i && now - lastClickTime <= 300L) {
                        TeleportTarget target = menu.getTargets().get(i);
                        ModNetwork.CHANNEL.sendToServer(new TeleportRequestPacket(menu.getBasePos(), menu.getTeamId(), target.id()));
                        minecraft.player.closeContainer();
                    }
                    lastClickIndex = i;
                    lastClickTime = now;
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isMouseOverRow(double mouseX, double mouseY, int index) {
        int y = topPos + 22 + index * ROW_HEIGHT;
        return mouseX >= leftPos + 12 && mouseX <= leftPos + imageWidth - 12 && mouseY >= y && mouseY <= y + ROW_HEIGHT - 2;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 10, 6, 0xFFFFFF, false);
        graphics.drawString(font, Component.translatable("screen.rebornfov.base_hint"), 10, 18 + Math.max(menu.getTargets().size(), 1) * ROW_HEIGHT, 0xC0C0C0, false);
        for (int i = 0; i < menu.getTargets().size(); i++) {
            TeleportTarget target = menu.getTargets().get(i);
            int y = 26 + i * ROW_HEIGHT;
            graphics.drawString(font, target.name(), 16, y, 0xFFFFFF, false);
            graphics.drawString(font, target.type(), 150, y, 0xA0D0FF, false);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
