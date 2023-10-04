package me.langyue.autotranslation.gui;

import com.mojang.realmsclient.RealmsMainScreen;
import me.langyue.autotranslation.AutoTranslation;
import me.langyue.autotranslation.util.FileUtils;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.*;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScreenManager {

    private static final Path file = AutoTranslation.ROOT.resolve("screen.whitelist");

    // MC 代码加密的，编译后包名就变了，所以只能这样
    private static final Set<String> MC_SCREEN = new LinkedHashSet<>() {{
        add(TitleScreen.class.getPackageName());
        add(RealmsMainScreen.class.getPackageName());
    }};

    private static final Set<String> WHITELIST = new LinkedHashSet<>();
    public static final Set<String> BLACKLIST = new LinkedHashSet<>() {{
        add(ChatScreen.class.getName());
        add(BookEditScreen.class.getName());
        add(SignEditScreen.class.getName());
        add(HangingSignEditScreen.class.getName());
        add(CommandBlockEditScreen.class.getName());
        add(StructureBlockEditScreen.class.getName());
        add(MinecartCommandBlockEditScreen.class.getName());
        add(JigsawBlockEditScreen.class.getName());
    }};

    private static boolean needSave = false;
    private static ScheduledExecutorService timer = null;

    public static void init() {
        if (timer == null) {
            timer = Executors.newSingleThreadScheduledExecutor();
            timer.schedule(ScreenManager::read, 0, TimeUnit.MINUTES);
            timer.scheduleAtFixedRate(ScreenManager::write, 5, 5, TimeUnit.MINUTES);
        }
    }

    public static void saveConfig() {
        write();
    }

    private static void read() {
        List<String> lines = FileUtils.readToList(file);
        if (lines == null || lines.isEmpty()) return;
        WHITELIST.addAll(lines);
    }

    private static void write() {
        if (!needSave) return;
        FileUtils.write(file, WHITELIST);
    }

    /**
     * 切换屏幕翻译状态
     *
     * @param screen 屏幕
     */
    public static boolean toggleScreenStatus(Screen screen) {
        if (screen == null) return false;
        String name = getClassName(screen);
        needSave = true;
        if (shouldTranslate(name)) {
            WHITELIST.remove(name);
            return false;
        } else {
            WHITELIST.add(name);
            return true;
        }
    }

    /**
     * 获取屏幕翻译状态
     *
     * @param screen 屏幕
     */
    public static boolean getScreenStatus(Screen screen) {
        if (screen == null) return true;
        return WHITELIST.contains(getClassName(screen));
    }

    public static boolean isInBlacklist(Screen screen) {
        if (screen == null) return true;
        return isInBlacklist(getClassName(screen));
    }

    public static boolean isInBlacklist(String screen) {
        if (screen == null) return false;
        if (AutoTranslation.CONFIG.ignoreOriginalScreen && MC_SCREEN.stream().anyMatch(screen::startsWith)) {
            return true;
        }
        return BLACKLIST.contains(screen);
    }

    public static boolean shouldTranslate(Screen screen) {
        if (screen == null) return false;
        return shouldTranslate(getClassName(screen));
    }

    private static boolean shouldTranslate(String screen) {
        if (screen == null) return false;
        if (isInBlacklist(screen)) return false;
        return WHITELIST.contains(getClassName(screen));
    }

    private static String getClassName(Screen screen) {
        return getClassName(screen.getClass().getName());
    }

    private static String getClassName(String screen) {
        if (screen.startsWith("vazkii.patchouli.client.book.gui.")) {
            return "vazkii.patchouli.client.book.gui.*";
        }
        return screen;
    }
}
