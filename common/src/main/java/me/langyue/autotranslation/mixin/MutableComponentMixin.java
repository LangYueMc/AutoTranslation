package me.langyue.autotranslation.mixin;

import me.langyue.autotranslation.accessor.MutableComponentAccessor;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MutableComponent.class)
public class MutableComponentMixin implements MutableComponentAccessor {

    @Unique
    public boolean autoTranslation$isLiteral = false;


    @Inject(method = "create", at = @At("RETURN"))
    private static void initMixin(ComponentContents componentContents, CallbackInfoReturnable<MutableComponent> cir) {
        MutableComponentAccessor accessor = (MutableComponentAccessor) (Object) cir.getReturnValue();
        accessor.isLiteral(componentContents instanceof LiteralContents);
    }

    @Override
    public void isLiteral(boolean isLiteral) {
        this.autoTranslation$isLiteral = isLiteral;
    }

    @Override
    public boolean isLiteral() {
        return autoTranslation$isLiteral;
    }
}
