package me.langyue.autotranslation.mixin;

import me.langyue.autotranslation.accessor.MutableComponentAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Inject(method = "sendMessage", at = @At("HEAD"))
    private void sendMessageMixin(Component component, UUID uUID, CallbackInfo ci) {
        if (component instanceof TextComponent textComponent) {
            ((MutableComponentAccessor) textComponent).at$shouldTranslate(false);
        }
    }
}
