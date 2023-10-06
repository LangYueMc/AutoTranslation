package me.langyue.autotranslation.mixin;

import me.langyue.autotranslation.accessor.MutableComponentAccessor;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Inject(method = "sendSystemMessage", at = @At("HEAD"))
    private void sendSystemMessageMixin(Component component, CallbackInfo ci) {
        if (component instanceof MutableComponent mutableComponent) {
            ((MutableComponentAccessor) mutableComponent).at$shouldTranslate(false);
        }
    }

    @Inject(method = "logChatMessage", at = @At("HEAD"))
    private void logChatMessageMixin(Component component, ChatType.Bound bound, String string, CallbackInfo ci) {
        if (component instanceof MutableComponent mutableComponent) {
            ((MutableComponentAccessor) mutableComponent).at$shouldTranslate(false);
        }
    }
}
