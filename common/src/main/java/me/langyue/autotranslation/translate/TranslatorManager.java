package me.langyue.autotranslation.translate;

import me.langyue.autotranslation.AutoTranslation;
import me.langyue.autotranslation.translate.google.Google;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class TranslatorManager {

    public static final String DEFAULT_TRANSLATOR = "Google";

    private static final Map<String, Supplier<ITranslator>> _TRANSLATOR_MAP = new LinkedHashMap<>() {{
        put(DEFAULT_TRANSLATOR, Google::getInstance);
    }};

    private static final Map<String, ITranslator> _TRANSLATOR_INSTANCES = new HashMap<>();


    public static void init() {
        setTranslator(AutoTranslation.CONFIG.translator);
        TranslateThreadPool.init();
    }

    public static void setTranslator(String name) {
        if (!_TRANSLATOR_INSTANCES.containsKey(name)) {
            ITranslator translator = _TRANSLATOR_MAP.get(name).get();
            if (translator == null) {
                AutoTranslation.LOGGER.error("Unknown translator: {}", name);
                setTranslator(DEFAULT_TRANSLATOR);
            } else {
                translator.init();
                _TRANSLATOR_INSTANCES.put(name, translator);
            }
        }
    }

    public static void registerTranslator(String name, Supplier<ITranslator> getInstance) {
        _TRANSLATOR_MAP.put(name, getInstance);
    }

    public static ITranslator getTranslator() {
        return getTranslator(AutoTranslation.CONFIG.translator);
    }

    public static ITranslator getTranslator(String name) {
        if (StringUtils.isBlank(name)) return null;
        return _TRANSLATOR_INSTANCES.get(name);
    }
}
