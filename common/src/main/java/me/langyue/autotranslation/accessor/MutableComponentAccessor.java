package me.langyue.autotranslation.accessor;

public interface MutableComponentAccessor {
    boolean at$shouldTranslate();

    void at$shouldTranslate(boolean shouldTranslate);
}
