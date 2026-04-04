package com.flowingsun.rebornfov.client;

import com.flowingsun.rebornfov.menu.NameEditorMenu;
import com.flowingsun.rebornfov.network.ModNetwork;
import com.flowingsun.rebornfov.network.RenameTargetPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class NameEditorScreen extends AbstractContainerScreen<NameEditorMenu> {
    private EditBox nameInput;
    private Button confirmButton;

    public NameEditorScreen(NameEditorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 220;
        this.imageHeight = 120;
    }

    @Override
    protected void init() {
        super.init();
        int inputX = leftPos + 16;
        int inputY = topPos + 40;
        nameInput = new EditBox(font, inputX, inputY, imageWidth - 32, 20, Component.translatable("screen.rebornfov.name_editor_input"));
        nameInput.setValue(menu.getInitialName());
        nameInput.setMaxLength(64);
        nameInput.setCanLoseFocus(false);
        addRenderableWidget(nameInput);
        setInitialFocus(nameInput);

        confirmButton = addRenderableWidget(Button.builder(Component.translatable("screen.rebornfov.name_editor_confirm"), button -> {
            String newName = nameInput.getValue().trim();
            if (!newName.isEmpty()) {
                ModNetwork.CHANNEL.sendToServer(new RenameTargetPacket(menu.getBlockPos(), newName));
            }
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.closeContainer();
            }
        }).bounds(leftPos + 16, topPos + 72, imageWidth - 32, 20).build());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 || keyCode == 335) {
            confirmButton.onPress();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return nameInput.charTyped(codePoint, modifiers) || super.charTyped(codePoint, modifiers);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xCC202020);
        graphics.fill(leftPos + 8, topPos + 24, leftPos + imageWidth - 8, topPos + imageHeight - 8, 0xCC303030);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, Component.translatable("screen.rebornfov.name_editor", menu.getBlockLabel()), 10, 8, 0xFFFFFF, false);
        graphics.drawString(font, Component.translatable("screen.rebornfov.name_editor_hint"), 10, 26, 0xC0C0C0, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
