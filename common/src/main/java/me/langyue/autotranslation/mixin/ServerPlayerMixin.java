package me.langyue.autotranslation.mixin;

import me.langyue.autotranslation.accessor.MutableComponentAccessor;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Inject(method = "sendMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V", at = @At("HEAD"))
    private void sendMessageMixin(Component component, ChatType chatType, UUID uUID, CallbackInfo ci) {
        if (component instanceof TextComponent textComponent) {
            ((MutableComponentAccessor) textComponent).at$shouldTranslate(false);
        }
    }
}
