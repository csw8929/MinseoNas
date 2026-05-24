package com.csw8929.minseo.nas;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * NAS 자격 증명 + 세션 + base URL 해석 소유. 단일 스레드 ExecutorService 공유.
 */
public final class DsAuth {
    private static final String TAG = "NAS";

    static volatile String cachedSid     = null;
    static volatile String resolvedBase  = null;

    private static final AtomicBoolean networkMonitorStarted = new AtomicBoolean(false);
    private static volatile Network lastKnownNetwork = null;

    public static final ExecutorService executor = Executors.newSingleThreadExecutor();
    public static final Handler mainHandler = new Handler(Looper.getMainLooper());

    // ── 런타임 인증 정보 (init()에서 주입) ─────────────────────────────────────
    public static volatile String cfgBaseUrl  = "";
    public static volatile String cfgLanUrl   = "";
    public static volatile String cfgUser     = "";
    public static volatile String cfgPass     = "";
    public static volatile String cfgBasePath = "/";
    public static volatile String cfgPosDir   = "/";

    private DsAuth() {}

    /** {@link NasCredentials} 받는 편의 오버로드. */
    public static void init(NasCredentials creds) {
        init(creds.getBaseUrl(), creds.getLanUrl(), creds.getUser(), creds.getPass(),
             creds.getBasePath(), creds.getPosDir());
    }

    /** NAS 인증 정보 적용. cachedSid/resolvedBase 캐시 무효화. */
    public static void init(String baseUrl, String lanUrl, String user, String pass,
                            String basePath, String posDir) {
        cfgBaseUrl  = baseUrl  == null ? "" : baseUrl;
        cfgLanUrl   = lanUrl   == null ? "" : lanUrl;
        cfgUser     = user     == null ? "" : user;
        cfgPass     = pass     == null ? "" : pass;
        cfgBasePath = basePath == null ? "/" : basePath;
        cfgPosDir   = posDir   == null ? "/" : posDir;
        cachedSid    = null;
        resolvedBase = null;
        Log.i(TAG, "DsAuth.init baseUrl=" + cfgBaseUrl + " lanUrl=" + cfgLanUrl);
    }

    /** 네트워크 전환(5G↔WiFi) 감지하여 resolvedBase / SID 캐시 무효화. */
    public static void startNetworkMonitoring(Context ctx) {
        if (!networkMonitorStarted.compareAndSet(false, true)) return;
        ConnectivityManager cm = (ConnectivityManager) ctx.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            Log.w(TAG, "ConnectivityManager null — 네트워크 모니터링 비활성");
            return;
        }
        try {
            cm.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                @Override public void onAvailable(Network network) {
                    if (lastKnownNetwork != null && !lastKnownNetwork.equals(network)) {
                        Log.i(TAG, "네트워크 전환 감지 → resolvedBase/SID 캐시 초기화");
                        resolvedBase = null;
                        cachedSid = null;
                    }
                    lastKnownNetwork = network;
                }
                @Override public void onLost(Network network) {
                    Log.i(TAG, "네트워크 끊김 → 캐시 초기화");
                    resolvedBase = null;
                    cachedSid = null;
                    lastKnownNetwork = null;
                }
            });
        } catch (Exception e) {
            networkMonitorStarted.set(false);
            Log.w(TAG, "startNetworkMonitoring 실패: " + e.getMessage());
        }
    }

    /** 로그인 (비동기). 첫 URL 실패 시 나머지 URL 도 시도. 콜백은 메인 스레드에서 실행. */
    public static void login(NasCallback<String> cb) {
        executor.execute(() -> {
            try {
                if (resolvedBase == null) {
                    resolvedBase = resolveBase();
                    Log.i(TAG, "resolvedBase=" + resolvedBase);
                }
                String sid = tryLoginSync(resolvedBase);
                if (sid == null) {
                    String alt = resolvedBase.equals(cfgBaseUrl) ? cfgLanUrl : cfgBaseUrl;
                    if (alt != null && !alt.isEmpty() && !alt.equals(resolvedBase)) {
                        Log.i(TAG, "첫 URL 실패, 대체 URL 시도: " + alt);
                        sid = tryLoginSync(alt);
                        if (sid != null) resolvedBase = alt;
                    }
                }
                if (sid != null) {
                    cachedSid = sid;
                    final String finalSid = sid;
                    mainHandler.post(() -> cb.onResult(finalSid));
                } else {
                    mainHandler.post(() -> cb.onError("NAS 로그인 실패 (외부/LAN 모두 실패)"));
                }
            } catch (Exception e) {
                Log.e(TAG, "login exception", e);
                mainHandler.post(() -> cb.onError("NAS 연결 오류: " + e.getMessage()));
            }
        });
    }

    private static String tryLoginSync(String base) {
        try {
            String url = base + "/webapi/auth.cgi"
                    + "?api=SYNO.API.Auth&version=6&method=login"
                    + "&account=" + URLEncoder.encode(cfgUser, "UTF-8")
                    + "&passwd=" + URLEncoder.encode(cfgPass, "UTF-8")
                    + "&session=FileStation&format=sid";
            String body = DsHttp.httpGet(url);
            JSONObject json = new JSONObject(body);
            if (json.optBoolean("success", false)) {
                String sid = json.getJSONObject("data").getString("sid");
                Log.i(TAG, "login OK base=" + base + " SID=" + sid.substring(0, Math.min(8, sid.length())) + "…");
                return sid;
            }
            int code = json.optJSONObject("error") != null
                    ? json.getJSONObject("error").optInt("code", -1) : -1;
            Log.w(TAG, "login 실패 base=" + base + " code=" + code);
        } catch (Exception e) {
            Log.w(TAG, "login 예외 base=" + base + ": " + e.getMessage());
        }
        return null;
    }

    /** API 호출에 사용할 base URL — LAN 성공 시 LAN, 아니면 cfgBaseUrl. */
    public static String apiBase() {
        return resolvedBase != null ? resolvedBase : cfgBaseUrl;
    }

    /** executor 스레드에서만 호출 (probeUrl 이 네트워크 호출). 외부 먼저, LAN 폴백. */
    private static String resolveBase() {
        if (cfgBaseUrl != null && !cfgBaseUrl.isEmpty() && DsHttp.probeUrl(cfgBaseUrl)) {
            Log.i(TAG, "외부 연결: " + cfgBaseUrl);
            return cfgBaseUrl;
        }
        if (cfgLanUrl != null && !cfgLanUrl.isEmpty() && DsHttp.probeUrl(cfgLanUrl)) {
            Log.i(TAG, "LAN 연결: " + cfgLanUrl);
            return cfgLanUrl;
        }
        Log.w(TAG, "Probe 모두 실패, 외부 URL 사용: " + cfgBaseUrl);
        return cfgBaseUrl;
    }

    /** 동기 재로그인 — 성공 시 새 SID 반환, 실패 시 null (cachedSid 도 클리어). */
    static String reLoginSync() {
        String base = resolvedBase != null ? resolvedBase : cfgBaseUrl;
        if (base == null || base.isEmpty()) { cachedSid = null; return null; }
        String sid = tryLoginSync(base);
        if (sid == null) {
            String alt = base.equals(cfgBaseUrl) ? cfgLanUrl : cfgBaseUrl;
            if (alt != null && !alt.isEmpty() && !alt.equals(base)) {
                sid = tryLoginSync(alt);
                if (sid != null) resolvedBase = alt;
            }
        }
        cachedSid = sid;
        if (sid != null) Log.i(TAG, "reLoginSync OK");
        return sid;
    }

    /** 동기 SID 보장 — 캐시된 SID 있으면 반환, 없으면 로그인 시도. 실패 시 null. */
    static String ensureSidSync() {
        if (cachedSid != null) return cachedSid;
        if (resolvedBase == null) resolvedBase = resolveBase();
        return reLoginSync();
    }
}
