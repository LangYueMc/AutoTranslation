package me.langyue.autotranslation.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class AutoTranslationCheckbox extends Checkbox {

    public AutoTranslationCheckbox(int x, int y, int width, int height, boolean checked) {
        super(x, y, width, height, Component.empty(), checked);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float d) {
        super.renderWidget(guiGraphics, mouseX, mouseY, d);
        if (this.isHovered) {
            guiGraphics.renderTooltip(Minecraft.getInstance().font, Component.translatable("checkbox.autotranslation.tooltip"), mouseX, mouseY);
        }
    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
        if (focusNavigationEvent instanceof FocusNavigationEvent.ArrowNavigation) {
            // 禁用导航
            return null;
        }
        return super.nextFocusPath(focusNavigationEvent);
    }
}
