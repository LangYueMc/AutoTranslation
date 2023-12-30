package me.langyue.autotranslation.translate.google;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.langyue.autotranslation.AutoTranslation;
import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * HttpClient 连接池工具类
 */
public class HttpClientUtil {

    private static final int CONNECT_TIMEOUT = 5000;
    private static final int SOCKET_TIMEOUT = 3000;
    private static final int MAX_CONN = 100;
    private static final int HTTP_IDLE_TIMEOUT = 10 * 1000;
    private static final Gson GSON = new Gson();
    private static PoolingHttpClientConnectionManager manager;
    private static ScheduledExecutorService timer;

    private final static Object syncLock = new Object(); // 相当于线程锁,用于线程安全
    private static volatile CloseableHttpClient httpClient;

    /**
     * 对http请求进行基本设置
     *
     * @param httpRequestBase http请求
     */
    private static void setRequestConfig(HttpRequestBase httpRequestBase) {
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(CONNECT_TIMEOUT)
                .setConnectTimeout(CONNECT_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT).build();

        httpRequestBase.setConfig(requestConfig);
    }

    public static CloseableHttpClient getHttpClient(URI uri, String dns) {
        if (uri == null) return null;

        if (httpClient == null) {
            //多线程下多个线程同时调用getHttpClient容易导致重复创建httpClient对象的问题,所以加上了同步锁
            synchronized (syncLock) {
                if (httpClient == null) {
                    httpClient = createHttpClient(uri.getHost(), uri.getPort(), dns);
                    //开启监控线程,对异常和空闲线程进行关闭
                    timer = Executors.newSingleThreadScheduledExecutor();
                    timer.scheduleAtFixedRate(() -> {
                        //关闭异常连接
                        manager.closeExpiredConnections();
                        manager.closeIdleConnections(HTTP_IDLE_TIMEOUT, TimeUnit.MILLISECONDS);
                    }, HTTP_IDLE_TIMEOUT, HTTP_IDLE_TIMEOUT, TimeUnit.MILLISECONDS);
                }
            }
        }
        return httpClient;
    }

    /**
     * 根据host和port构建httpclient实例
     *
     * @param host 要访问的域名
     * @param port 要访问的端口
     * @return
     */
    public static CloseableHttpClient createHttpClient(String host, int port, String dns) {
        ConnectionSocketFactory plainSocketFactory = PlainConnectionSocketFactory.getSocketFactory();
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", plainSocketFactory)
                .register("https", getSslConnectionSocketFactory()).build();

        if (dns == null) {
            manager = new PoolingHttpClientConnectionManager(registry);
        } else {
            manager = new PoolingHttpClientConnectionManager(registry, new SystemDefaultDnsResolver() {
                @Override
                public InetAddress[] resolve(final String h) throws UnknownHostException {
                    if (h.equals(host)) {
                        return new InetAddress[]{InetAddress.getByName(dns)};
                    }
                    return super.resolve(h);
                }
            });
        }
        //设置连接参数
        manager.setMaxTotal(MAX_CONN); // 最大连接数
        manager.setDefaultMaxPerRoute(MAX_CONN); // 路由最大连接数

        HttpHost httpHost = new HttpHost(host, port);
        manager.setMaxPerRoute(new HttpRoute(httpHost), MAX_CONN);

        //请求失败时,进行请求重试
        HttpRequestRetryHandler handler = (e, i, httpContext) -> {
            if (i > 3) {
                //重试超过3次,放弃请求
                AutoTranslation.LOGGER.error("retry has more than 3 time, give up request");
                return false;
            }
            if (e instanceof ConnectTimeoutException) {
                // 连接超时
                AutoTranslation.LOGGER.error("Connection Time out");
                return false;
            }
            if (e instanceof NoHttpResponseException) {
                //服务器没有响应,可能是服务器断开了连接,应该重试
                AutoTranslation.LOGGER.error("receive no response from server, retry");
                return true;
            }
            if (e instanceof SSLHandshakeException) {
                // SSL握手异常
                AutoTranslation.LOGGER.error("SSL hand shake exception");
                return false;
            }
            if (e instanceof InterruptedIOException) {
                //超时
                AutoTranslation.LOGGER.error("InterruptedIOException");
                return false;
            }
            if (e instanceof UnknownHostException) {
                // 服务器不可达
                AutoTranslation.LOGGER.error("server host unknown");
                return false;
            }
            if (e instanceof SSLException) {
                AutoTranslation.LOGGER.error("SSLException", e);
                return false;
            }

            HttpClientContext context = HttpClientContext.adapt(httpContext);
            HttpRequest request = context.getRequest();
            return !(request instanceof HttpEntityEnclosingRequest);
        };
        return HttpClients.custom()
                .setConnectionManager(manager)
                .setRetryHandler(handler)
                .build();
    }

    /**
     * 支持SSL
     *
     * @return SSLConnectionSocketFactory
     */
    private static SSLConnectionSocketFactory getSslConnectionSocketFactory() {
        try {
            TrustStrategy acceptingTrustStrategy = (x509Certificates, s) -> true;
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
            return new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
        } catch (Throwable e) {
            AutoTranslation.LOGGER.warn("Create SSLConnectionSocketFactory failed", e);
        }
        return SSLConnectionSocketFactory.getSocketFactory();
    }

    private static <T> T execute(HttpRequestBase request, String dns, Class<T> classOfT) {
        long time = System.currentTimeMillis();
        setRequestConfig(request);
        CloseableHttpResponse response = null;
        InputStream in = null;
        T object = null;
        String result = null;
        try {
            response = getHttpClient(request.getURI(), dns).execute(request, HttpClientContext.create());
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                in = entity.getContent();
                result = IOUtils.toString(in, StandardCharsets.UTF_8);
                AutoTranslation.debug("{} {}ms: \n{}", request, System.currentTimeMillis() - time, result);
                if (classOfT == String.class) {
                    object = classOfT.cast(request);
                } else {
                    object = GSON.fromJson(result, classOfT);
                }
            }
        } catch (Throwable e) {
            AutoTranslation.LOGGER.error("{}: {}", request, result, e);
        } finally {
            try {
                if (in != null) in.close();
                if (response != null) response.close();
            } catch (IOException e) {
                AutoTranslation.LOGGER.error("error", e);
            }
        }
        return object;
    }

    public static JsonObject get(String url) {
        return get(url, null, null, JsonObject.class);
    }

    public static JsonObject get(String url, String dns) {
        return get(url, dns, null, JsonObject.class);
    }

    public static <T> T get(String url, String dns, Class<T> classOfT) {
        return get(url, dns, null, classOfT);
    }

    public static <T> T get(String url, String dns, Map<String, String> params, Class<T> classOfT) {
        HttpGet httpGet = new HttpGet(url);
        setRequestConfig(httpGet);
        if (params != null && !params.isEmpty()) {
            httpGet.setURI(URI.create(url + "?" + URLEncodedUtils.format(params.entrySet().stream().map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue())).toList(), StandardCharsets.UTF_8)));
        }
        return execute(httpGet, dns, classOfT);
    }

    public static JsonObject post(String url, Map<String, String> params) {
        return post(url, params, JsonObject.class);
    }

    public static <T> T post(String url, Map<String, String> params, Class<T> classOfT) {
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> nvps = new ArrayList<>();
        if (params != null && !params.isEmpty()) {
            Set<String> keys = params.keySet();
            for (String key : keys) {
                nvps.add(new BasicNameValuePair(key, params.get(key)));
            }
        }
        httpPost.setEntity(new UrlEncodedFormEntity(nvps, StandardCharsets.UTF_8));
        return execute(httpPost, null, classOfT);
    }

    public static int status(String url, String dns) {
        HttpGet httpGet = new HttpGet(url);
        setRequestConfig(httpGet);
        CloseableHttpResponse response = null;
        int status;
        try {
            response = getHttpClient(httpGet.getURI(), dns).execute(httpGet, HttpClientContext.create());
            status = response.getStatusLine().getStatusCode();
        } catch (Throwable e) {
            AutoTranslation.LOGGER.error(e.getMessage());
            status = 999;
        } finally {
            try {
                if (response != null) response.close();
            } catch (IOException e) {
                AutoTranslation.LOGGER.error("error", e);
            }
        }
        return status;
    }

    /**
     * 关闭连接池
     */
    public static void closeConnectionPool() {
        try {
            if (httpClient != null)
                httpClient.close();
            if (manager != null)
                manager.close();
            if (timer != null)
                timer.shutdown();
        } catch (IOException e) {
            AutoTranslation.LOGGER.error("", e);
        }
    }

    /**
     * 关闭连接池
     */
    public static void reset() {
        try {
            if (httpClient != null) {
                httpClient.close();
                httpClient = null;
            }
        } catch (IOException e) {
            AutoTranslation.LOGGER.error("", e);
        }
    }
}
