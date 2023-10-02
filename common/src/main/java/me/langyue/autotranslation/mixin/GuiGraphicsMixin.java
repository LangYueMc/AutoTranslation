package me.langyue.autotranslation.mixin;

import me.langyue.autotranslation.gui.ScreenManager;
import me.langyue.autotranslation.translate.TranslatorManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin {

    @ModifyVariable(method = "drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private String drawStringMixin(String string) {
        return autoTranslation$getTranslate(string);
    }
//
//    @Redirect(method = "drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;getVisualOrderText()Lnet/minecraft/util/FormattedCharSequence;"))
//    private FormattedCharSequence drawComponentMixin(Component instance) {
//        return autoTranslation$translateComponent(instance).getVisualOrderText();
//    }

//    @Redirect(method = "drawCenteredString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;getVisualOrderText()Lnet/minecraft/util/FormattedCharSequence;"))
//    private FormattedCharSequence drawCenteredStringMixin(Component instance) {
//        return autoTranslation$translateComponent(instance).getVisualOrderText();
//    }
//
//    @Redirect(method = "renderTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;getVisualOrderText()Lnet/minecraft/util/FormattedCharSequence;"))
//    private FormattedCharSequence renderTooltipMixin(Component instance) {
//        return autoTranslation$translateComponent(instance).getVisualOrderText();
//    }
//
//    @ModifyArg(method = "renderComponentTooltip", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;transform(Ljava/util/List;Lcom/google/common/base/Function;)Ljava/util/List;"))
//    private List<Component> renderComponentTooltipMixin(List<Component> fromList) {
//        return fromList.stream().map(this::autoTranslation$translateComponent).toList();
//    }
//
//    @Redirect(method = "renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;II)V", at = @At(value = "INVOKE", target = "Ljava/util/List;stream()Ljava/util/stream/Stream;"))
//    private Stream<Component> renderTooltipMixin(List<Component> instance) {
//        return instance.stream().map(this::autoTranslation$translateComponent);
//    }
//
//    @Unique
//    private Component autoTranslation$translateComponent(Component instance) {
//        if (((MutableComponentAccessor) (Object) instance).isLiteral()) {
//            String content = instance.getString();
//            String translate = autoTranslation$getTranslate(content);
//            if (!content.equals(translate)) {
//                return Component.literal(content);
//            }
//        }
//        return instance;
//    }

    @Unique
    private String autoTranslation$getTranslate(String content) {
        if (ScreenManager.shouldTranslate(Minecraft.getInstance().screen)) {
            if (TranslatorManager.shouldTranslate(content)) {
                String t = TranslatorManager.translate(content, null);
                if (t != null) {
                    return t;
                }
            }
        }
        return content;
    }
}
