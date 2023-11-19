package me.langyue.autotranslation.resource;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dev.architectury.platform.Platform;
import me.langyue.autotranslation.AutoTranslation;
import me.langyue.autotranslation.TranslatorHelper;
import me.langyue.autotranslation.command.ResourcePathArgument;
import me.langyue.autotranslation.translate.TranslatorManager;
import me.langyue.autotranslation.util.FileUtils;
import net.minecraft.ResourceLocationException;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import org.apache.commons.compress.archivers.zip.ParallelScatterZipCreator;
import org.apache.commons.compress.archivers.zip.UnixStat;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.parallel.InputStreamSupplier;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.*;

public class ResourceManager {
    private static final String ref = "_ref.json";
    private static final String packMcmeta = "pack.mcmeta";

    public static final String BATCH_TRANSLATION = "AutoTranslation:Res";
    public static final Multimap<String, String> UNLOAD_KEYS = LinkedListMultimap.create();
    public static final Multimap<String, String> UNKNOWN_KEYS = LinkedListMultimap.create();
    private final static Object syncLock = new Object();

    /**
     * 用于存放无 key 翻译
     */
    private static final Map<String, String> NO_KEY_TRANS_STORE = new ConcurrentHashMap<>();
    /**
     * 存放无 key 翻译文件的命名空间
     */
    public static final String NO_KEY_TRANS_STORE_NAMESPACE = "_at_store";

    /**
     * 翻译 json 必须格式化，不然可能出问题
     */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Language language;

    private static ScheduledExecutorService timer = null;

    public static void init() {
        if (timer == null) {
            timer = Executors.newSingleThreadScheduledExecutor();
            timer.scheduleAtFixedRate(ResourceManager::save, 5, 5, TimeUnit.MINUTES);
        }
    }

    public static void setLanguage(Language language) {
        ResourceManager.save();
        ResourceManager.language = language;
    }

    public static void createPackMeta() {
        JsonObject pack = new JsonObject();
        pack.addProperty("pack_format", SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES));
        pack.addProperty("description", language.getOrDefault("pack.mcmeta.description") + "\n" + DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.format(System.currentTimeMillis()));
        JsonObject mcmeta = new JsonObject();
        mcmeta.add("pack", pack);
        write(AutoTranslation.ROOT.resolve(packMcmeta).toFile(), GSON.toJson(mcmeta));
    }

    public static void initResource() {
        if (language == null) {
            throw new RuntimeException("Unready!");
        }
        Multimap<String, String> remove = LinkedListMultimap.create();
        UNKNOWN_KEYS.forEach((namespace, key) -> {
            // 去除各种各样原因（比如其他mod）到这里已经翻译过了的
            if (!TranslatorHelper.shouldTranslate(language.getOrDefault(key))) {
                remove.put(namespace, key);
            } else {
                UNLOAD_KEYS.put(namespace, key);
            }
        });
        remove.forEach(UNKNOWN_KEYS::remove);
        ResourcePathArgument.addExamples(UNKNOWN_KEYS.keySet());
        loadResource();
        UNLOAD_KEYS.keySet().forEach(namespace -> {
            Collection<String> keys = UNLOAD_KEYS.get(namespace);
            if (keys.isEmpty()) return;
            Map<String, String> refMap = new LinkedHashMap<>();
            keys.forEach(key -> refMap.put(key, language.getOrDefault(key)));
            // 写入参考文件
            write(namespace, ref, refMap);
            // 写入翻译文件
            int maxLength = TranslatorManager.getTranslator().maxLength();
            List<String> chunkKeys = new ArrayList<>();
            List<String> chunkValues = new ArrayList<>();
            int length = BATCH_TRANSLATION.length();
            for (String key : keys) {
                String value = refMap.get(key);
                var valueLength = value.length();
                length += valueLength;
                if (length + chunkKeys.size() * 2 > maxLength) {
                    // 达到分片大小
                    translatorAndAppendJson(namespace, chunkKeys, chunkValues);
                    length = BATCH_TRANSLATION.length() + valueLength;
                    chunkKeys = new ArrayList<>();
                    chunkValues = new ArrayList<>();
                }
                chunkKeys.add(key);
                chunkValues.add(language.getOrDefault(key).replaceAll("\n", "\\\\n"));
            }
            if (!chunkKeys.isEmpty()) {
                translatorAndAppendJson(namespace, chunkKeys, chunkValues);
            }
        });
    }

    public static void loadResource(String namespace) {
        Path file = AutoTranslation.ROOT.resolve(namespace).resolve(AutoTranslation.getLanguage() + ".json");
        if (Files.exists(file)) {
            JsonObject jsonObject = read(file);
            jsonObject.asMap().forEach((k, v) -> {
                String t = v.getAsString();
                if (namespace.equals(NO_KEY_TRANS_STORE_NAMESPACE) || UNKNOWN_KEYS.containsValue(k)) {
                    // NO_KEY_TRANS_STORE_NAMESPACE 全存，其他的只有在 UNKNOWN_KEYS 里存在的才缓存
                    TranslatorHelper.setCache(k, t);
                }
                NO_KEY_TRANS_STORE.remove(k);
                UNLOAD_KEYS.remove(namespace, k);
            });
            AutoTranslation.LOGGER.info("Resource loaded: " + file);
        } else {
            throw new ResourceLocationException("Invalid resource path: " + file);
        }
    }

    public static void loadResource(String... namespaces) {
        if (namespaces == null || namespaces.length == 0) {
            namespaces = new HashSet<>(UNKNOWN_KEYS.keySet()) {{
                add(NO_KEY_TRANS_STORE_NAMESPACE);
            }}.toArray(new String[]{});
        }
        for (String ns : namespaces) {
            try {
                loadResource(ns);
            } catch (ResourceLocationException e) {
                AutoTranslation.LOGGER.warn(e.getMessage());
            }
        }
    }

    public static void noKeyTranslate(String en, String translation) {
        if (StringUtils.isNotBlank(en) && StringUtils.isNotBlank(translation)) {
            NO_KEY_TRANS_STORE.put(en, translation);
        }
    }

    public static void save() {
        if (language == null) return;
        if (!NO_KEY_TRANS_STORE.isEmpty()) {
            AutoTranslation.debug("Saving no_key_translation");
            write(NO_KEY_TRANS_STORE_NAMESPACE, AutoTranslation.getLanguage() + ".json", NO_KEY_TRANS_STORE);
            loadResource(NO_KEY_TRANS_STORE_NAMESPACE);
        }
    }

    private static void translatorAndAppendJson(String namespace, List<String> chunkKeys, List<String> chunkValues) {
        TranslatorHelper.translate(BATCH_TRANSLATION + "\n" + String.join("\n", chunkValues), false, s -> {
            String[] result = s.split("\n");
            Map<String, String> map = new LinkedHashMap<>();
            for (int i = 0; i < chunkKeys.size(); i++) {
                try {
                    map.put(chunkKeys.get(i), result[i + 1].replaceAll("\\\\n", "\n"));
                } catch (Throwable e) {
                    AutoTranslation.LOGGER.warn("{}:{} load failed", namespace, chunkKeys.get(i), e);
                }
            }
            write(namespace, AutoTranslation.getLanguage() + ".json", map);
            loadResource(namespace);
        });
    }

    /**
     * 写入文件
     *
     * @param dir      目录
     * @param fileName 文件名
     * @param map      键值对
     */
    private static void write(String dir, String fileName, Map<String, String> map) {
        if (map.isEmpty()) return;
        synchronized (syncLock) {
            Path file = AutoTranslation.ROOT.resolve(dir).resolve(fileName);
            JsonObject json = read(file);
            for (Map.Entry<String, String> entry : map.entrySet()) {
                json.addProperty(entry.getKey(), entry.getValue());
            }
            write(file.toFile(), GSON.toJson(json));
        }
    }

    private static void write(File file, String content) {
        synchronized (syncLock) {
            FileUtils.write(file.toPath(), content);
        }
    }

    @NotNull
    private static JsonObject read(Path path) {
        synchronized (syncLock) {
            if (!Files.exists(path)) {
                return new JsonObject();
            }
            String readString = null;
            try {
                readString = Files.readString(path);
            } catch (Exception e) {
                AutoTranslation.LOGGER.error("Read file ({}) failed", path, e);
            }
            JsonObject jsonObject = null;
            if (StringUtils.isNotBlank(readString)) {
                try {
                    jsonObject = GSON.fromJson(readString, JsonObject.class);
                } catch (JsonSyntaxException e) {
                    AutoTranslation.LOGGER.error("The original file ({}) format is incorrect", path, e);
                }
            }
            if (jsonObject == null) {
                return new JsonObject();
            }
            return jsonObject;
        }
    }

    public static void packResource(boolean increment) throws IOException {
        createPackMeta();
        List<String> namespaces = new ArrayList<>();
        if (increment) {
            namespaces.addAll(UNKNOWN_KEYS.keySet());
        } else {
            for (File f : Objects.requireNonNull(AutoTranslation.ROOT.toFile().listFiles())) {
                if (f.isDirectory() && !f.getName().equals(NO_KEY_TRANS_STORE_NAMESPACE)) {
                    namespaces.add(f.getName());
                }
            }
        }
        if (namespaces.isEmpty()) return;
        String resourcePack = "AutoTranslation." + AutoTranslation.getLanguage() + (increment ? ".Increment" : ".Full") + DateFormatUtils.format(System.currentTimeMillis(), ".yyMMdd_HHmm") + ".zip";
        Path zipOutName = AutoTranslation.ROOT.resolve(resourcePack);
        compressAssets(zipOutName, namespaces);
        AutoTranslation.LOGGER.info("Packaged resource pack: {}", zipOutName);
        Files.copy(zipOutName, Minecraft.getInstance().getResourcePackDirectory().resolve(resourcePack), StandardCopyOption.REPLACE_EXISTING);
        PackRepository packRepository = Minecraft.getInstance().getResourcePackRepository();
        packRepository.reload();
        String resourceId = "file/" + resourcePack;
        Pack pack = packRepository.getPack(resourceId);
        Collection<String> selectedIds = packRepository.getSelectedIds();
        if (pack != null && !selectedIds.contains(resourceId)) {
            ArrayList<String> list = Lists.newArrayList();
            list.add(resourceId);
            list.addAll(selectedIds);
            packRepository.setSelected(list);
            Minecraft.getInstance().options.updateResourcePacks(packRepository);
        }
    }

    private static void compressAssets(Path zipOutName, List<String> namespaces) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                5,
                10,
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(500),
                new ThreadPoolExecutor.CallerRunsPolicy());
        ParallelScatterZipCreator zipCreator = new ParallelScatterZipCreator(executor);
        try (
                OutputStream outputStream = new FileOutputStream(zipOutName.toFile());
                ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(outputStream)
        ) {
            zipArchiveOutputStream.setEncoding("UTF-8");
            String fileName = AutoTranslation.getLanguage() + ".json";
            for (String namespace : namespaces) {
                File inFile = AutoTranslation.ROOT.resolve(namespace).resolve(fileName).toFile();
                String zipFileName = "assets/" + namespace + "/lang/" + fileName;
                try {
                    addArchiveEntry(zipCreator, zipFileName, new FileInputStream(inFile));
                } catch (Throwable e) {
                    AutoTranslation.debug("zip \"{}\" failed", inFile, e);
                }
            }
            addArchiveEntry(zipCreator, packMcmeta, new FileInputStream(AutoTranslation.ROOT.resolve(packMcmeta).toFile()));
            try {
                Platform.getMod(AutoTranslation.MOD_ID).findResource("icon.png").ifPresent(icon -> {
                    try {
                        addArchiveEntry(zipCreator, "pack.png", Files.newInputStream(icon));
                    } catch (Throwable ignored) {
                    }
                });
            } catch (Throwable ignored) {
            }
            zipCreator.writeTo(zipArchiveOutputStream);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static void addArchiveEntry(ParallelScatterZipCreator zipCreator, String name, InputStream fileInputStream) {
        final InputStreamSupplier inputStreamSupplier = () -> fileInputStream;
        ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(name);
        zipArchiveEntry.setMethod(ZipArchiveEntry.DEFLATED);
        zipArchiveEntry.setUnixMode(UnixStat.FILE_FLAG | 436);
        zipCreator.addArchiveEntry(zipArchiveEntry, inputStreamSupplier);
    }
}
