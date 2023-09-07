package me.langyue.autotranslation.accessor;

public interface MutableComponentAccessor {

    void isLiteral(boolean isLiteral);

    boolean isLiteral();

    boolean isTranslated();

    void setTranslated(boolean translated);
}
