package me.langyue.autotranslation.mixin;

import me.langyue.autotranslation.accessor.MutableComponentAccessor;
import me.langyue.autotranslation.gui.ScreenManager;
import me.langyue.autotranslation.translate.TranslatorManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin {

    @Redirect(method = "drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawInBatch(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;IIZ)I"))
    private int drawStringMixin(Font instance, String string, float f, float g, int i, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, Font.DisplayMode displayMode, int j, int k, boolean bl2) {
        return instance.drawInBatch(autoTranslation$getTranslate(string), f, g, i, bl, matrix4f, multiBufferSource, displayMode, j, k, bl2);
    }

    @Redirect(method = "drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;getVisualOrderText()Lnet/minecraft/util/FormattedCharSequence;"))
    private FormattedCharSequence drawComponentMixin(Component instance) {
        return autoTranslation$translateComponent(instance).getVisualOrderText();
    }

    @Redirect(method = "drawCenteredString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;getVisualOrderText()Lnet/minecraft/util/FormattedCharSequence;"))
    private FormattedCharSequence drawCenteredStringMixin(Component instance) {
        return autoTranslation$translateComponent(instance).getVisualOrderText();
    }

    @Redirect(method = "renderTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;getVisualOrderText()Lnet/minecraft/util/FormattedCharSequence;"))
    private FormattedCharSequence renderTooltipMixin(Component instance) {
        return autoTranslation$translateComponent(instance).getVisualOrderText();
    }

    @Inject(method = "renderComponentTooltip", at = @At(value = "HEAD"))
    private void renderComponentTooltipMixin(Font font, List<Component> list, int i, int j, CallbackInfo ci) {
        List<Component> temp = list.stream().map(this::autoTranslation$translateComponent).toList();
        list.clear();
        list.addAll(temp);
    }

    @Inject(method = "renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;II)V", at = @At(value = "HEAD"))
    private void renderTooltipMixin(Font font, List<Component> list, Optional<TooltipComponent> optional, int i, int j, CallbackInfo ci) {
        List<Component> temp = list.stream().map(this::autoTranslation$translateComponent).toList();
        list.clear();
        list.addAll(temp);
    }

    @Unique
    private Component autoTranslation$translateComponent(Component instance) {
        if (((MutableComponentAccessor) (Object) instance).isLiteral()) {
            String content = instance.getString();
            String translate = autoTranslation$getTranslate(content);
            if (!content.equals(translate)) {
                return Component.literal(content);
            }
        }
        return instance;
    }

    @Unique
    private String autoTranslation$getTranslate(String content) {
        if (ScreenManager.shouldTranslate(Minecraft.getInstance().screen)) {
            if (TranslatorManager.shouldTranslate(content, content)) {
                String t = TranslatorManager.translate(content, null);
                if (t != null) {
                    return t;
                }
            }
        }
        return content;
    }
}
