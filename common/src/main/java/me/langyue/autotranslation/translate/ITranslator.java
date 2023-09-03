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

    default String translate(String text, String tl) {
        return translate(text, tl, "auto");
    }

    String translate(String text, String tl, String sl);
}
