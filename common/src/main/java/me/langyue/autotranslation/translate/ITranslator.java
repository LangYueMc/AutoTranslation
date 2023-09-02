package me.langyue.autotranslation.translate;

public interface ITranslator {

    void init();

    default boolean ready() {
        return true;
    }

    /**
     * 单次可翻译的最大长度
     */
    int maxLength();

    String translate(String text, String language);
}
