package me.langyue.autotranslation.accessor;

import net.minecraft.locale.Language;

public interface MutableComponentAccessor {
    boolean at$shouldTranslate();

    void at$shouldTranslate(boolean shouldTranslate);

    Language at$decomposedWith();
}
