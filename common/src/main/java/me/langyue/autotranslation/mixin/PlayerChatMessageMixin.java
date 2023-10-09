package me.langyue.autotranslation.mixin;

import me.langyue.autotranslation.accessor.MutableComponentAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.PlayerChatMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerChatMessage.class)
public class PlayerChatMessageMixin {

    @Inject(method = "withUnsignedContent", at = @At("RETURN"))
    private void withUnsignedContentMixin(CallbackInfoReturnable<Component> cir) {
        if (cir.getReturnValue() instanceof MutableComponent mutableComponent) {
            ((MutableComponentAccessor) mutableComponent).at$shouldTranslate(false);
        }
    }

    @Inject(method = "signedContent", at = @At("RETURN"))
    private void signedContentMixin(CallbackInfoReturnable<Component> cir) {
        if (cir.getReturnValue() instanceof MutableComponent mutableComponent) {
            ((MutableComponentAccessor) mutableComponent).at$shouldTranslate(false);
        }
    }
}
