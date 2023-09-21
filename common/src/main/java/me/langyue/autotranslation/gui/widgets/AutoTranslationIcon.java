package me.langyue.autotranslation.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import me.langyue.autotranslation.AutoTranslation;
import me.langyue.autotranslation.gui.ScreenManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class AutoTranslationIcon extends AbstractButton {
    private static final ResourceLocation TEXTURE = new ResourceLocation(AutoTranslation.MOD_ID, "textures/gui/icon.png");
    private boolean enabled;

    public AutoTranslationIcon(int x, int y, int width, int height, boolean enabled) {
        super(x, y, width, height, Component.empty());
        this.enabled = enabled;
    }

    @Override
    public void onPress() {
        this.enabled = ScreenManager.toggleScreenStatus(Minecraft.getInstance().screen);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float d) {
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(.5f, .5f, 1);
        guiGraphics.pose().translate(getX(), getY(), 0);
        guiGraphics.blit(TEXTURE, this.getX(), this.getY(), this.isFocused() ? 20.0f : 0.0f, this.enabled ? 20.0f : 0.0f, this.width, this.height, 64, 64);
        if (this.isHovered) {
            guiGraphics.renderTooltip(Minecraft.getInstance().font, Component.translatable("checkbox.autotranslation.tooltip"), mouseX, mouseY);
        }
        guiGraphics.pose().popPose();
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

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, Component.translatable("checkbox.autotranslation.tooltip"));
        if (this.active) {
            if (this.isFocused()) {
                narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.focused"));
            } else {
                narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.hovered"));
            }
        }
    }
}
