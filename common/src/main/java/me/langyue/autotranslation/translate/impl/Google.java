package me.langyue.autotranslation.translate.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.langyue.autotranslation.AutoTranslation;
import me.langyue.autotranslation.http.HttpClientUtil;
import me.langyue.autotranslation.translate.ITranslator;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class Google implements ITranslator {

    private static final String domain = "translate.google.com";
    private static String client = "at";

    @Override
    public boolean ready() {
        return true;
    }

    @Override
    public boolean check() {
        boolean reachable;
        InetAddress address;
        try {
            address = InetAddress.getByName(domain);
        } catch (IOException e) {
            AutoTranslation.LOGGER.info("Ping {}: failed", domain);
            return false;
        }
        long time = System.currentTimeMillis();
        try {
            reachable = address.isReachable(3000);
        } catch (IOException e) {
            AutoTranslation.LOGGER.info("Ping {}: timeout", address);
            return false;
        }
        if (reachable) {
            AutoTranslation.LOGGER.info("Ping {}: {}ms", address, System.currentTimeMillis() - time);
        } else {
            AutoTranslation.LOGGER.info("Ping {}: unable to connect", address);
        }
        return reachable;
    }

    @Override
    public int maxLength() {
        return 5000;
    }

    @Override
    public String translate(String text, String language) {
        JsonArray jsonArray = null;
        try {
            jsonArray = HttpClientUtil.get("https://%s/translate_a/single?client=%s&sl=en&tl=%s&dt=t&q=%s".formatted(domain, client, URLEncoder.encode(language, StandardCharsets.UTF_8), URLEncoder.encode(text, StandardCharsets.UTF_8)), JsonArray.class);
            if (jsonArray == null) {
                // 读取失败，可能是调用受限了，改下 client
                client = "gtx";
            }
        } catch (Throwable e) {
            AutoTranslation.LOGGER.error("Google Translate encountered an exception", e);
            return null;
        }
        if (jsonArray == null) {
            return null;
        }
        try {
            JsonArray elements = jsonArray.getAsJsonArray().get(0).getAsJsonArray();
            StringBuilder result = new StringBuilder();
            elements.forEach(jsonElement -> {
                result.append(jsonElement.getAsJsonArray().get(0).getAsString());
            });
            return result.toString();
        } catch (Throwable e) {
            return null;
        }
    }

    public static void main(String[] args) {
        // [[["{\n  ","{\n  ",null,null,3,null,null,[[]],[[["6ffafab0da7e640be86ac09d0d5e539c","en_zh_2023q1.md"]],[null,true]]],["\"option.modmenu.quick_configure\": \"快速配置\",\n  ","\"option.modmenu.quick_configure\": \"Quick Configure\",\n  ",null,null,3,null,null,[[]],[[["6ffafab0da7e640be86ac09d0d5e539c","en_zh_2023q1.md"]],[null,true]]],["\"option.modmenu.quick_configure.true\": \"启用\",\n  ","\"option.modmenu.quick_configure.true\": \"Enabled\",\n  ",null,null,3,null,null,[[]],[[["6ffafab0da7e640be86ac09d0d5e539c","en_zh_2023q1.md"]],[null,true]]],["\"option.modmenu.game_menu_button_style.icon\": \"图标\",\n  ","\"option.modmenu.game_menu_button_style.icon\": \"Icon\",\n  ",null,null,3,null,null,[[]],[[["6ffafab0da7e640be86ac09d0d5e539c","en_zh_2023q1.md"]],[null,true]]],["\"option.modmenu.quick_configure.false\": \"禁用\",\n  ","\"option.modmenu.quick_configure.false\": \"Disabled\",\n  ",null,null,3,null,null,[[]],[[["6ffafab0da7e640be86ac09d0d5e539c","en_zh_2023q1.md"]],[null,true]]],["\"option.modmenu.game_menu_button_style.below_bugs\": \"低于错误\",\n  ","\"option.modmenu.game_menu_button_style.below_bugs\": \"Below Bugs\",\n  ",null,null,3,null,null,[[]],[[["6ffafab0da7e640be86ac09d0d5e539c","en_zh_2023q1.md"]],[null,true]]],["\"modmenu.kofi\": \"Ko-fi\",\n  ","\"modmenu.kofi\": \"Ko-fi\",\n  ",null,null,3,null,null,[[]],[[["6ffafab0da7e640be86ac09d0d5e539c","en_zh_2023q1.md"]],[null,true]]],["\"option.modmenu.sorting.descending\": \"Z-A\",\n  ","\"option.modmenu.sorting.descending\": \"Z-A\",\n  ",null,null,3,null,null,[[]],[[["6ffafab0da7e640be86ac09d0d5e539c","en_zh_2023q1.md"]],[null,true]]],["\"modmenu.reddit\": \"Reddit\",\n  ","\"modmenu.reddit\": \"Reddit\",\n  ",null,null,3,null,null,[[]],[[["6ffafab0da7e640be86ac09d0d5e539c","en_zh_2023q1.md"]],[null,true]]],["\"option.modmenu.game_menu_button_style.replace_bugs\": \"替换错误\",\n  ","\"option.modmenu.game_menu_button_style.replace_bugs\": \"Replace Bugs\",\n  ",null,null,3,null,null,[[]],[[["6ffafab0da7e640be86ac09d0d5e539c","en_zh_2023q1.md"]],[null,true]]],["\"option.modmenu.game_menu_button_style\": \"游戏菜单\",\n  ","\"option.modmenu.game_menu_button_style\": \"Game Menu\",\n  ",null,null,3,null,null,[[]],[[["6ffafab0da7e640be86ac09d0d5e539c","en_zh_2023q1.md"]],[null,true]]],["\"modmenu.crowdin\": \"Crowdin\",\n  ","\"modmenu.crowdin\": \"Crowdin\",\n  ",null,null,3,null,null,[[]],[[["6ffafab0da7e640be86ac09d0d5e539c","en_zh_2023q1.md"]],[null,true]]],["\"modmenu.coindrop\": \"硬币掉落\",\n  ","\"modmenu.coindrop\": \"Coindrop\",\n  ",null,null,3,null,null,[[]],[[["6ffafab0da7e640be86ac09d0d5e539c","en_zh_2023q1.md"]],[null,true]]],["\"modmenu.experimental\": \"(Mod 菜单更新检查器是实验性的！)\",\n  ","\"modmenu.experimental\": \"(Mod Menu update checker is experimental!)\",\n  ",null,null,3,null,null,[[]],[[["6ffafab0da7e640be86ac09d0d5e539c","en_zh_2023q1.md"]],[null,true]]],["\"modmenu.liberapay\": \"Liberapay\",\n  ","\"modmenu.liberapay\": \"Liberapay\",\n  ",null,null,3,null,null,[[]],[[["6ffafab0da7e640be86ac09d0d5e539c","en_zh_2023q1.md"]],[null,true]]],["\"option.modmenu.sorting.ascending\": \"A-Z\"\n","\"option.modmenu.sorting.ascending\": \"A-Z\"\n",null,null,3,null,null,[[]],[[["6ffafab0da7e640be86ac09d0d5e539c","en_zh_2023q1.md"]],[null,true]]],["}","}",null,null,3,null,null,[[]],[[["6ffafab0da7e640be86ac09d0d5e539c","en_zh_2023q1.md"]]]]],null,"en",null,null,null,null,[]]
        // [[["{\n  ","{\n  ",null,null,3,null,null,[[]],[[["6ffafab0da7e640be86ac09d0d5e539c","en_zh_2023q1.md"]],[null,true]]],["\"text.cloth-config.disabled_tooltip\": \"已禁用（未满足要求）\"\n","\"text.cloth-config.disabled_tooltip\": \"Disabled (requirements not met)\"\n",null,null,3,null,null,[[]],[[["6ffafab0da7e640be86ac09d0d5e539c","en_zh_2023q1.md"]],[null,true]]],["}","}",null,null,3,null,null,[[]],[[["6ffafab0da7e640be86ac09d0d5e539c","en_zh_2023q1.md"]]]]],null,"en",null,null,null,null,[]]
        // [[["谷歌","google",null,null,10]],null,"en",null,null,null,null,[]]
        JsonArray jsonArray = new Gson().fromJson("[[[\"{\\n  \",\"{\\n  \",null,null,3,null,null,[[]],[[[\"6ffafab0da7e640be86ac09d0d5e539c\",\"en_zh_2023q1.md\"]],[null,true]]],[\"\\\"text.cloth-config.disabled_tooltip\\\": \\\"已禁用（未满足要求）\\\"\\n\",\"\\\"text.cloth-config.disabled_tooltip\\\": \\\"Disabled (requirements not met)\\\"\\n\",null,null,3,null,null,[[]],[[[\"6ffafab0da7e640be86ac09d0d5e539c\",\"en_zh_2023q1.md\"]],[null,true]]],[\"}\",\"}\",null,null,3,null,null,[[]],[[[\"6ffafab0da7e640be86ac09d0d5e539c\",\"en_zh_2023q1.md\"]]]]],null,\"en\",null,null,null,null,[]]", JsonArray.class);

        JsonArray elements = jsonArray.getAsJsonArray().get(0).getAsJsonArray();
        StringBuilder result = new StringBuilder();
        elements.forEach(jsonElement -> {
            result.append(jsonElement.getAsJsonArray().get(0).getAsString());
        });
        System.out.println(result);
    }
}
