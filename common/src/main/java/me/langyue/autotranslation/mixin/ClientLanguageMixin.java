package me.langyue.autotranslation.mixin;

import me.langyue.autotranslation.AutoTranslation;
import me.langyue.autotranslation.TranslatorHelper;
import me.langyue.autotranslation.config.Config;
import me.langyue.autotranslation.resource.ResourceManager;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Mixin(ClientLanguage.class)
public class ClientLanguageMixin {
    @Final
    @Shadow
    private static Logger LOGGER;

    @Invoker("appendFrom")
    private static void appendFrom(String string, List<Resource> list, Map<String, String> map) {
        throw new AssertionError();
    }

    @Unique
    private static boolean autoTranslation$ready = false;

    @Inject(method = "loadFrom", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;", ordinal = 0), locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void loadFromRMixin(net.minecraft.server.packs.resources.ResourceManager resourceManager,
                                       List<String> list, boolean bl, CallbackInfoReturnable<ClientLanguage> cir,
                                       Map<String, String> map) {
        if (AutoTranslation.getLanguage().equals(Language.DEFAULT)) return;
        if (!list.contains(AutoTranslation.getLanguage())) {
            return;
        }
        ResourceManager.UNKNOWN_KEYS.clear();
        autoTranslation$ready = false;
        // 自定义循环
        for (String lang : list) {
            String file = String.format(Locale.ROOT, "lang/%s.json", lang);
            for (String namespace : resourceManager.getNamespaces()) {
                try {
                    ResourceLocation resourceLocation = new ResourceLocation(namespace, file);
                    autoTranslation$appendFrom(lang, namespace, resourceManager.getResourceStack(resourceLocation), map);
                } catch (Exception exception) {
                    LOGGER.warn("Skipped language file: {}:{} ({})", namespace, file, exception.toString());
                }
            }
        }
        // 不走原循环
        list.clear();
    }

    @Unique
    private static void autoTranslation$appendFrom(String lang, String namespace, List<Resource> list, Map<String, String> map) {
        if (AutoTranslation.CONFIG.excludedNamespace.stream().anyMatch(s -> Pattern.matches(s, namespace))) {
            appendFrom(lang, list, map);
            return;
        }
        Map<String, String> temp = new HashMap<>();
        appendFrom(lang, list, temp);
        if (lang.equals(Language.DEFAULT)) {
            // 默认语言
            temp.forEach((k, v) -> {
                map.put(k, v);
                if (!ResourceManager.UNKNOWN_KEYS.containsValue(k) && TranslatorHelper.shouldTranslate(k, v)) {
                    ResourceManager.UNKNOWN_KEYS.put(namespace, k);
                }
            });
        } else if (AutoTranslation.getLanguage().equals(lang)) {
            // 当前语言
            temp.forEach((k, v) -> {
                if (AutoTranslation.CONFIG.mode == Config.FilterMode.CORRECTION && v.equals(map.get(k))) {
                    if (!TranslatorHelper.shouldTranslate(k, v)) {
                        ResourceManager.UNKNOWN_KEYS.remove(namespace, k);
                    }
                } else {
                    ResourceManager.UNKNOWN_KEYS.remove(namespace, k);
                }
                map.put(k, v);
            });
        }
        temp.clear();
    }

    @Inject(method = "loadFrom", at = @At("RETURN"))
    private static void loadFromReturnMixin(net.minecraft.server.packs.resources.ResourceManager resourceManager,
                                            List<String> list, boolean bl, CallbackInfoReturnable<ClientLanguage> cir) {
        if (AutoTranslation.getLanguage().equals(Language.DEFAULT)) return;
        ResourceManager.setLanguage(cir.getReturnValue());
        if (ResourceManager.UNKNOWN_KEYS.isEmpty()) {
            autoTranslation$ready = true;
            return;
        }
        AutoTranslation.LOGGER.info("{} keys obtained", ResourceManager.UNKNOWN_KEYS.size());
        ResourceManager.UNKNOWN_KEYS.keySet().forEach(namespace -> {
            AutoTranslation.debug("{} :", namespace);
            ResourceManager.UNKNOWN_KEYS.get(namespace).forEach(key -> AutoTranslation.debug("\t{}", key));
        });
        ResourceManager.initResource();
        autoTranslation$ready = true;
    }

    @Unique
    private static final Pattern autoTranslation$idPattern = Pattern.compile("([^\\s.]+\\.)+[^\\s.]+");

    @Inject(method = "getOrDefault", at = @At(value = "RETURN"), cancellable = true)
    private void getOrDefaultMixin(String string, String string2, CallbackInfoReturnable<String> cir) {
        if (!autoTranslation$ready) return;
        if (!ResourceManager.UNKNOWN_KEYS.containsValue(string) && !TranslatorHelper.hasCache(string)) return;
        String returnValue = cir.getReturnValue();
        if (string.equals(returnValue) && autoTranslation$idPattern.matcher(string).matches()) {
            // TODO 记录这种英文语言文件里都没有的 key
            return;
        }
        String translate = TranslatorHelper.translate(string, returnValue, null);
        if (translate == null) {
            return;
        }
        cir.setReturnValue(translate);
    }
}
