package me.langyue.autotranslation.mixin.compat.patchouli;

import me.langyue.autotranslation.accessor.MutableComponentAccessor;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.patchouli.client.book.text.Span;

@Mixin(Span.class)
public class SpanMixin {

    @Inject(method = "styledSubstring(I)Lnet/minecraft/network/chat/MutableComponent;", at = @At("RETURN"))
    private void styledSubstringIMixin(int start, CallbackInfoReturnable<MutableComponent> cir) {
        if (cir.getReturnValue() instanceof TextComponent textComponent) {
            ((MutableComponentAccessor) textComponent).at$shouldTranslate(false);
        }
    }

    @Inject(method = "styledSubstring(II)Lnet/minecraft/network/chat/MutableComponent;", at = @At("RETURN"))
    private void styledSubstringIIMixin(int start, int end, CallbackInfoReturnable<MutableComponent> cir) {
        if (cir.getReturnValue() instanceof TextComponent textComponent) {
            ((MutableComponentAccessor) textComponent).at$shouldTranslate(false);
        }
    }
}
