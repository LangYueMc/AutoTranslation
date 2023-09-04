package me.langyue.autotranslation.gui;

import me.langyue.autotranslation.AutoTranslation;
import net.minecraft.client.gui.screens.Screen;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScreenManager {

    private static final Path file = AutoTranslation.ROOT.resolve("screen.whitelist");

    private static final Set<String> WHITELIST = new LinkedHashSet<>();

    private static boolean needSave = false;
    private static ScheduledExecutorService timer = null;

    public static void init() {
        if (timer == null) {
            timer = Executors.newSingleThreadScheduledExecutor();
        }
        timer.schedule(ScreenManager::read, 0, TimeUnit.MINUTES);
        timer.scheduleAtFixedRate(ScreenManager::write, 5, 5, TimeUnit.MINUTES);
    }

    private static void read() {
        if (!Files.exists(file)) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file.toFile()))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                WHITELIST.add(line);
            }
        } catch (Throwable e) {
            AutoTranslation.LOGGER.error("Read file ({}) failed", file, e);
        }
    }

    private static void write() {
        if (!needSave) return;
        if (!Files.exists(file.getParent())) {
            try {
                Files.createDirectories(file.getParent());
            } catch (Throwable e) {
                AutoTranslation.LOGGER.error("Create directories ({}) failed", file.getParent(), e);
                return;
            }
        }
        if (!Files.exists(file)) {
            try {
                Files.createFile(file);
            } catch (Throwable e) {
                AutoTranslation.LOGGER.error("Create file ({}) failed", file, e);
                return;
            }
        }
        Writer writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(file.toFile()), StandardCharsets.UTF_8);
            writer.write(String.join("\n", WHITELIST));
        } catch (Throwable e) {
            AutoTranslation.LOGGER.error("Writing file failed", e);
        } finally {
            try {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * 切换屏幕翻译状态
     *
     * @param screen 屏幕
     * @return 切换后的状态
     */
    public static boolean toggleScreenStatus(Screen screen) {
        if (screen == null) return false;
        String name = screen.getClass().getName();
        needSave = true;
        if (shouldTranslate(name)) {
            WHITELIST.remove(name);
            return false;
        } else {
            WHITELIST.add(name);
            return true;
        }
    }

    public static boolean shouldTranslate(Screen screen) {
        if (screen == null) return false;
        return shouldTranslate(screen.getClass().getName());
    }

    public static boolean shouldTranslate(String screen) {
        if (screen == null) return false;
        return WHITELIST.stream().anyMatch(screen::equals);
    }
}
