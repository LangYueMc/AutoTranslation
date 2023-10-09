package me.langyue.autotranslation.gui.widgets;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.langyue.autotranslation.AutoTranslation;
import me.langyue.autotranslation.ScreenTranslationHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class AutoTranslationIcon extends AbstractButton {
    static AutoTranslationIcon instance;
    private static final ResourceLocation TEXTURE = new ResourceLocation(AutoTranslation.MOD_ID, "textures/gui/icon.png");
    private boolean enabled;

    public AutoTranslationIcon() {
        super(0, 0, 12, 12, Component.empty());
    }

    private int getX() {
        if (Minecraft.getInstance().screen == null) return 10;
        int screenWidth = Minecraft.getInstance().screen.width;
        switch (AutoTranslation.CONFIG.icon.displayArea) {
            case TOP_LEFT, MIDDLE_LEFT, BOTTOM_LEFT -> {
                return Math.abs(AutoTranslation.CONFIG.icon.offsetX) + 10;
            }
            case TOP_CENTER, MIDDLE_CENTER, BOTTOM_CENTER -> {
                return (screenWidth - this.width) / 2 + AutoTranslation.CONFIG.icon.offsetX;
            }
            case TOP_RIGHT, MIDDLE_RIGHT, BOTTOM_RIGHT -> {
                return screenWidth - this.width - Math.abs(AutoTranslation.CONFIG.icon.offsetX) - 10;
            }
        }
        return 10;
    }

    private int getY() {
        if (Minecraft.getInstance().screen == null) return 10;
        int screenHeight = Minecraft.getInstance().screen.height;
        switch (AutoTranslation.CONFIG.icon.displayArea) {
            case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> {
                return Math.abs(AutoTranslation.CONFIG.icon.offsetX) + 10;
            }
            case MIDDLE_LEFT, MIDDLE_CENTER, MIDDLE_RIGHT -> {
                return (screenHeight - this.height) / 2 + AutoTranslation.CONFIG.icon.offsetY;
            }
            case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> {
                return screenHeight - this.height - Math.abs(AutoTranslation.CONFIG.icon.offsetY) - 10;
            }
        }
        return 10;
    }

    @Override
    public void onPress() {
        this.enabled = ScreenTranslationHelper.toggleScreenStatus(Minecraft.getInstance().screen);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.x = this.getX();
        this.y = this.getY();
        super.render(poseStack, i, j, f);
    }

    @Override
    public void renderButton(PoseStack poseStack, int i, int j, float f) {
        Screen screen = Minecraft.getInstance().screen;
        if (ScreenTranslationHelper.hideIcon(screen)) return;
        this.enabled = ScreenTranslationHelper.getScreenStatus(screen);
        if (!AutoTranslation.CONFIG.icon.alwaysDisplay && !this.enabled) return;
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        AutoTranslationIcon.blit(poseStack, this.x, this.y, this.isFocused() ? 12.0f : 0.0f, this.enabled ? 12.0f : 0.0f, this.width, this.height, 64, 64);
        if (this.isHovered && Minecraft.getInstance().screen != null) {
            Minecraft.getInstance().screen.renderTooltip(poseStack, Component.translatable("checkbox.autotranslation.tooltip"), i, j);
        }
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, Component.translatable("checkbox.autotranslation.tooltip"));
        if (this.active) {
            if (this.isFocused()) {
                narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.focused"));
            } else {
                narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.hovered"));
            }
        }
    }

    public static AutoTranslationIcon getInstance() {
        if (instance == null) {
            instance = new AutoTranslationIcon();
        }
        return instance;
    }
}
