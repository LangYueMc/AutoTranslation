package me.langyue.autotranslation;

import com.mojang.realmsclient.RealmsMainScreen;
import me.langyue.autotranslation.accessor.ScreenAccessor;
import me.langyue.autotranslation.util.FileUtils;
import me.shedaniel.clothconfig2.gui.ClothConfigScreen;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.*;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScreenTranslationHelper {

    private static final Path file = AutoTranslation.ROOT.resolve("screen.whitelist");
    private static boolean ready = false;

    // MC 代码加密的，编译后包名就变了，所以只能这样
    private static final Set<String> MC_SCREEN = new LinkedHashSet<>() {{
        add(TitleScreen.class.getPackageName());
        add(RealmsMainScreen.class.getPackageName());
    }};

    private static final Set<String> WHITELIST = new LinkedHashSet<>();
    private static final Set<String> BLACKLIST = new LinkedHashSet<>() {{
        add(ChatScreen.class.getName());
        add(BookEditScreen.class.getName());
        add(SignEditScreen.class.getName());
        add(CommandBlockEditScreen.class.getName());
        add(StructureBlockEditScreen.class.getName());
        add(MinecartCommandBlockEditScreen.class.getName());
        add(JigsawBlockEditScreen.class.getName());
        add("dev.ftb.mods.ftblibrary.config.ui.");
        add("dev.ftb.mods.ftbquests.client.gui.SelectQuestObjectScreen");
        add("dev.ftb.mods.ftbquests.client.gui.MultilineTextEditorScreen");
        add("dev.ftb.mods.ftbquests.client.gui.RewardTablesScreen");
        add("dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen");
        add(ClothConfigScreen.class.getName());
    }};

    private static boolean needSave = false;
    private static ScheduledExecutorService timer = null;

    public static void init() {
        if (timer == null) {
            timer = Executors.newSingleThreadScheduledExecutor();
            timer.schedule(ScreenTranslationHelper::read, 0, TimeUnit.MINUTES);
            timer.scheduleAtFixedRate(ScreenTranslationHelper::write, 5, 5, TimeUnit.MINUTES);
        }
    }

    public static void addScreenBlacklist(Class screen) {
        if (screen == null) return;
        addScreenBlacklist(screen.getName());
    }

    public static void addScreenBlacklist(String screen) {
        if (StringUtils.isBlank(screen)) return;
        BLACKLIST.add(screen);
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
        boolean status = false;
        if (WHITELIST.contains(name)) {
            WHITELIST.remove(name);
        } else if (!isInBlacklist(screen)) {
            WHITELIST.add(name);
            status = true;
        }
        ((ScreenAccessor) screen).at$shouldTranslate(status);
        return status;
    }

    /**
     * 初始化屏幕翻译状态
     *
     * @param screen 屏幕
     */
    public static void initScreenStatus(Screen screen) {
        if (screen == null) return;
        boolean inBlacklist = isInBlacklist(screen);
        ScreenAccessor screenAccessor = (ScreenAccessor) screen;
        screenAccessor.at$shouldTranslate(!inBlacklist && WHITELIST.contains(getClassName(screen)));
        screenAccessor.at$showIcon(!inBlacklist);
    }

    /**
     * 获取屏幕翻译状态
     *
     * @param screen 屏幕
     */
    public static boolean getScreenStatus(Screen screen) {
        if (screen == null) return false;
        return ((ScreenAccessor) screen).at$shouldTranslate();
    }

    /**
     * 是否显示翻译图标
     *
     * @param screen 屏幕
     */
    public static boolean hideIcon(Screen screen) {
        if (screen == null) return true;
        return !((ScreenAccessor) screen).at$showIcon();
    }

    private static boolean isInBlacklist(Screen screen) {
        if (screen == null) return true;
        String screenName = getClassName(screen);
        if (AutoTranslation.CONFIG.ignoreOriginalScreen && MC_SCREEN.stream().anyMatch(screenName::startsWith)) {
            return true;
        }
        return BLACKLIST.stream().anyMatch(screenName::startsWith);
    }

    public static boolean shouldTranslate(Screen screen) {
        if (screen == null) return false;
        if (!ready) return false;
        return ((ScreenAccessor) screen).at$shouldTranslate();
    }

    private static String getClassName(Screen screen) {
        try {
            // FTB 兼容
            if (screen instanceof dev.ftb.mods.ftblibrary.ui.ScreenWrapper screenWrapper) {
                return screenWrapper.getGui().getClass().getName();
            }
        } catch (Throwable ignored) {
        }
        String className = screen.getClass().getName();
        if (className.startsWith("vazkii.patchouli.client.book.gui.")) {
            return "vazkii.patchouli.client.book.gui.*";
        }
        return className;
    }

    public static void ready() {
        ready = true;
    }

    public static void unready() {
        ready = false;
    }
}
