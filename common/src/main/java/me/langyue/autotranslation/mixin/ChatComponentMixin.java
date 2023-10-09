package me.langyue.autotranslation.mixin;

import me.langyue.autotranslation.accessor.MutableComponentAccessor;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public class ChatComponentMixin {

    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"))
    private void addMessageMixin(Component component, CallbackInfo ci) {
        if (component instanceof TextComponent textComponent)
            // 聊天信息不直接翻译
            ((MutableComponentAccessor) textComponent).at$shouldTranslate(false);
    }
}
