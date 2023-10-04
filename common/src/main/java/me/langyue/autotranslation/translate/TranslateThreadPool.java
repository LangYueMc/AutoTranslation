package me.langyue.autotranslation.translate;

import me.langyue.autotranslation.AutoTranslation;
import me.langyue.autotranslation.TranslatorHelper;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class TranslateThreadPool {

    private record KeyValue(String key, String value) {
    }

    private static final Queue<KeyValue> QUEUE = new ConcurrentLinkedQueue<>();
    private static final Map<String, Consumer<String>> keys = new ConcurrentHashMap<>();
    private static ScheduledExecutorService timer = null;

    public static void init() {
        if (timer != null) return;
        AutoTranslation.LOGGER.info("Start the translation scheduled task");
        timer = Executors.newSingleThreadScheduledExecutor();

        timer.scheduleAtFixedRate(() -> {
            if (!TranslatorManager.getTranslator().ready()) return;
            if (QUEUE.isEmpty()) return;
            KeyValue keyValue = QUEUE.poll();
            if (keyValue == null) return;
            try {
                String translate = TranslatorHelper.translateSync(keyValue.key, keyValue.value);
                if (translate == null) {
                    QUEUE.offer(keyValue);
                } else {
                    Consumer<String> callback = keys.get(keyValue.key);
                    if (callback != null) {
                        callback.accept(translate);
                    }
                    keys.remove(keyValue.key);
                }
            } catch (Throwable e) {
                QUEUE.offer(keyValue);
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    /**
     * 往翻译队列添加 key value
     *
     * @param key      翻译 key
     * @param value    原文
     * @param callback 回调方法
     */
    public static void offer(String key, String value, Consumer<String> callback) {
        if (keys.containsKey(key)) return;
        keys.put(key, callback == null ? s -> {
        } : callback);
        QUEUE.offer(new KeyValue(key, value));
    }
}
