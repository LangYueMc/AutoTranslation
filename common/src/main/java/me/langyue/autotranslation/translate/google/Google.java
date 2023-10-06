package me.langyue.autotranslation.translate.google;

import com.google.gson.JsonArray;
import me.langyue.autotranslation.AutoTranslation;
import me.langyue.autotranslation.translate.ITranslator;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Google implements ITranslator {

    private static final Google instance = new Google();
    private static final ScheduledExecutorService timer = Executors.newScheduledThreadPool(5);
    private static final String default_domain = "translate.google.com";
    private final static Object syncLock = new Object();
    private static String client = "gtx";
    private boolean ready = false;

    private String domain = default_domain;
    private DNS dns = null;

    private Google() {
    }

    public static Google getInstance() {
        return instance;
    }

    @Override
    public void init() {
        timer.scheduleAtFixedRate(() -> {
            if (dns != null) return;
            if (!pingDomain(AutoTranslation.CONFIG.google.domain) && !pingDomain(default_domain)) {
                // 连接不到 Google 服务器，除去未联网因素，那就大概率是在中国了，开始扫 DNS
                chooseDNS();
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    private void chooseDNS() {
        this.dns = null;
        AutoTranslation.CONFIG.google.dns.forEach(ip -> timer.schedule(() -> {
            int latency = ping(ip);
            if (latency > -1) {
                ready = true;
                synchronized (syncLock) {
                    if (dns == null || dns.latency > latency) {
                        dns = new DNS(ip, latency);
                    }
                }
            }
        }, 10, TimeUnit.MILLISECONDS));
    }

    private boolean pingDomain(String domain) {
        long time = System.currentTimeMillis();
        int status = HttpClientUtil.status("https://" + domain + "?t=" + time, dns == null ? null : dns.ip);
        if (status < 400) {
            this.ready = true;
            this.domain = domain;
            AutoTranslation.LOGGER.info("Test {}: {}ms", domain, System.currentTimeMillis() - time);
            return true;
        } else if (status < 999) {
            AutoTranslation.LOGGER.warn("Test {} status: {}", domain, status);
        } else {
            AutoTranslation.LOGGER.warn("Test {}: unable to connect", domain);
        }
        return false;
    }

    private int ping(String address) {
        boolean reachable = false;
        InetAddress geek = null;
        long latency = -1;
        try {
            long time = System.currentTimeMillis();
            geek = InetAddress.getByName(address);
            reachable = geek.isReachable(3000);
            if (reachable) {
                latency = System.currentTimeMillis() - time;
            }
        } catch (IOException e) {
            if (geek == null) {
                AutoTranslation.LOGGER.warn("Ping {}: failed", geek);
            } else {
                AutoTranslation.LOGGER.warn("Ping {}: timeout", geek);
            }
        }
        if (reachable) {
            AutoTranslation.LOGGER.info("Ping {}: {}ms", geek, latency);
        } else {
            AutoTranslation.LOGGER.warn("Ping {}: unable to connect", geek);
        }
        this.ready = this.ready || reachable;
        return (int) latency;
    }

    @Override
    public boolean ready() {
        return this.ready;
    }

    public static void main(String[] args) {
        System.out.println(Google.getInstance().translate("测试test", "zh_cn", "en"));
    }

    @Override
    public int maxLength() {
        return 5000;
    }

    // [[["{\n  ","{\n  ",null,null,3,null,null,[[]],[[["6ffafab0da7e640be86ac09d0d5e539c","en_zh_2023q1.md"]],[null,true]]],["\"text.cloth-config.disabled_tooltip\": \"已禁用（未满足要求）\"\n","\"text.cloth-config.disabled_tooltip\": \"Disabled (requirements not met)\"\n",null,null,3,null,null,[[]],[[["6ffafab0da7e640be86ac09d0d5e539c","en_zh_2023q1.md"]],[null,true]]],["}","}",null,null,3,null,null,[[]],[[["6ffafab0da7e640be86ac09d0d5e539c","en_zh_2023q1.md"]]]]],null,"en",null,null,null,null,[]]
    // [[["谷歌","google",null,null,10]],null,"en",null,null,null,null,[]]
    @Override
    public String translate(String text, String tl, String sl) {
        JsonArray jsonArray = null;
        try {
            Map<String, String> params = new HashMap<>() {{
                put("client", client);
                put("sl", sl);
                put("tl", tl);
                put("dt", "t");
                put("ie", "UTF-8");
                put("oe", "UTF-8");
                put("q", text);
            }};
            jsonArray = HttpClientUtil.get("https://" + domain + "/translate_a/single", dns == null ? null : dns.ip, params, JsonArray.class);
            if (jsonArray == null) {
                // 读取失败，可能是调用受限了，改下 client
                client = "at";
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

    private record DNS(String ip, int latency) {
    }
}
