package me.langyue.autotranslation.mixin.compat.patchouli;

import me.langyue.autotranslation.accessor.MutableComponentAccessor;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import vazkii.patchouli.client.book.text.Span;

@Mixin(Span.class)
public class SpanMixin {

    @Redirect(method = "styledSubstring(I)Lnet/minecraft/network/chat/MutableComponent;", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/MutableComponent;setStyle(Lnet/minecraft/network/chat/Style;)Lnet/minecraft/network/chat/MutableComponent;"))
    private MutableComponent styledSubstringIMixin(MutableComponent instance, Style style) {
        MutableComponent component = instance.setStyle(style);
        ((MutableComponentAccessor) component).at$shouldTranslate(false);
        return component;
    }

    @Redirect(method = "styledSubstring(II)Lnet/minecraft/network/chat/MutableComponent;", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/MutableComponent;setStyle(Lnet/minecraft/network/chat/Style;)Lnet/minecraft/network/chat/MutableComponent;"))
    private MutableComponent styledSubstringIIMixin(MutableComponent instance, Style style) {
        MutableComponent component = instance.setStyle(style);
        ((MutableComponentAccessor) component).at$shouldTranslate(false);
        return component;
    }
}
