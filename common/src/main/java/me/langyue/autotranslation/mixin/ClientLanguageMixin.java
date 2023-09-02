package me.langyue.autotranslation.mixin;

import me.langyue.autotranslation.AutoTranslation;
import me.langyue.autotranslation.resource.ResourceManager;
import me.langyue.autotranslation.translate.TranslatorManager;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Mixin(ClientLanguage.class)
public class ClientLanguageMixin {

    @Invoker("appendFrom")
    private static void appendFrom(String string, List<Resource> list, Map<String, String> map) {
        throw new AssertionError();
    }

    @Unique
    private static final Pattern autoTranslation$en = Pattern.compile("[a-zA-Z]{2,}");

    @Unique
    private static String autoTranslation$namespace = "unknown";

    @Unique
    private static boolean autoTranslation$ready = false;

    @Inject(method = "loadFrom", at = @At(value = "HEAD"))
    private static void loadFromHeadMixin(net.minecraft.server.packs.resources.ResourceManager resourceManager,
                                          List<String> list, boolean bl, CallbackInfoReturnable<ClientLanguage> cir) {
        ResourceManager.UNKNOWN_KEYS.clear();
        autoTranslation$namespace = "unknown";
        autoTranslation$ready = false;
    }


    @Inject(method = "loadFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/language/ClientLanguage;appendFrom(Ljava/lang/String;Ljava/util/List;Ljava/util/Map;)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void loadFromRMixin(net.minecraft.server.packs.resources.ResourceManager resourceManager,
                                       List<String> list, boolean bl, CallbackInfoReturnable<ClientLanguage> cir,
                                       Map map, Iterator var4, String string, String string2, Iterator var7,
                                       String string3, ResourceLocation resourceLocation) {
        autoTranslation$namespace = string3;
    }

    @Redirect(method = "loadFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/language/ClientLanguage;appendFrom(Ljava/lang/String;Ljava/util/List;Ljava/util/Map;)V"))
    private static void appendFromMixin(String string, List<Resource> list, Map<String, String> map) {
        if (AutoTranslation.CONFIG.excludedNamespace.contains(autoTranslation$namespace)) {
            appendFrom(string, list, map);
            return;
        }
        Map<String, String> temp = new HashMap<>();
        appendFrom(string, list, temp);
        if (string.equals(Language.DEFAULT)) {
            // 默认语言
            temp.forEach((k, v) -> {
                map.put(k, v);
                ResourceManager.UNKNOWN_KEYS.put(autoTranslation$namespace, k);
            });
        } else if (AutoTranslation.getLanguage().equals(string)) {
            // 当前语言
            temp.forEach((k, v) -> {
                if (v.equals(map.get(k))) {
                    // 如果翻译文件的值跟 en 相同
                    String _t = v.replaceAll("(§[0-9a-rA-R])|(%[a-hsxont%]x?)|(\\\\\\S)|([^a-zA-z%§\\\\\\s]+)|([Ff][1-9][012]?)", "").toLowerCase();
                    for (String p : AutoTranslation.CONFIG.noNeedForTranslation) {
                        _t = _t.replaceAll(p.toLowerCase(), "");
                    }
                    if (!autoTranslation$en.matcher(_t.trim()).matches()) {
                        // 如果内容全是占位符，则不进行翻译
                        ResourceManager.UNKNOWN_KEYS.remove(autoTranslation$namespace, k);
                    }
                } else {
                    ResourceManager.UNKNOWN_KEYS.remove(autoTranslation$namespace, k);
                }
                map.put(k, v);
            });
        }
    }

    @Inject(method = "loadFrom", at = @At(value = "RETURN"))
    private static void loadFromReturnMixin(net.minecraft.server.packs.resources.ResourceManager resourceManager,
                                            List<String> list, boolean bl, CallbackInfoReturnable<ClientLanguage> cir) {
        AutoTranslation.LOGGER.info("{} keys obtained:", ResourceManager.UNKNOWN_KEYS.size());
        ResourceManager.UNKNOWN_KEYS.keySet().forEach(namespace -> {
            AutoTranslation.LOGGER.info("{} :", namespace);
            ResourceManager.UNKNOWN_KEYS.get(namespace).forEach(key -> AutoTranslation.LOGGER.info("\t{}", key));
        });
        ResourceManager.setLanguage(cir.getReturnValue());
        ResourceManager.initResource();
        autoTranslation$ready = true;
    }

    @Inject(method = "getOrDefault", at = @At(value = "RETURN"), cancellable = true)
    private void getOrDefaultMixin(String string, String string2, CallbackInfoReturnable<String> cir) {
        if (!autoTranslation$ready) return;
        if (!ResourceManager.UNKNOWN_KEYS.containsValue(string)) return;
        String returnValue = cir.getReturnValue();
        if (string.equals(returnValue)) return;
        String translate = TranslatorManager.translate(string, returnValue, null);
        if (translate == null) {
            return;
        }
        if (AutoTranslation.CONFIG.appendOriginal) {
            translate += " §7(" + returnValue + ")";
        }
        cir.setReturnValue(translate);
    }
}
