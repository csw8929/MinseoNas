package com.csw8929.minseo.nas;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 평문 SharedPreferences + assets/ 파일 fallback 로 자격증명 보관. Minseo6 패턴.
 *
 * <p>두 단계 우선순위 (SharedPreferences 값이 빈 문자열 / 미설정 → assets fallback):
 * <ol>
 *   <li>SharedPreferences (런타임 사용자 입력)</li>
 *   <li>assets/{assetFileName} (앱 빌드시 박힌 기본값, gitignored)</li>
 * </ol>
 *
 * <p>assets 파일 키 형식 (Minseo6 컨벤션):
 * <pre>
 * nas.host=...
 * nas.port=5000
 * nas.lan_host=...
 * nas.lan_port=5000
 * nas.user=...
 * nas.pass=...
 * nas.path=/web/.minseo/...
 * nas.base_path=/      # 선택, 기본 "/"
 * </pre>
 *
 * SharedPreferences 키: {@code nas_host}, {@code nas_port}, {@code nas_lan_host},
 * {@code nas_lan_port}, {@code nas_user}, {@code nas_pass}, {@code nas_path},
 * {@code nas_base_path}.
 */
public final class PlainPrefsCredentials implements NasCredentials {

    private static final String KEY_HOST      = "nas_host";
    private static final String KEY_PORT      = "nas_port";
    private static final String KEY_USER      = "nas_user";
    private static final String KEY_PASS      = "nas_pass";
    private static final String KEY_PATH      = "nas_path";
    private static final String KEY_BASE_PATH = "nas_base_path";
    private static final String KEY_LAN_HOST  = "nas_lan_host";
    private static final String KEY_LAN_PORT  = "nas_lan_port";

    private final SharedPreferences sp;
    private final Properties localDefaults = new Properties();
    private final String defaultPath;

    /**
     * @param ctx           Application context (또는 안에서 getApplicationContext() 호출)
     * @param prefsName     SharedPreferences 파일 이름 (예: "nas_prefs")
     * @param assetFileName assets 폴더의 fallback 파일명 (예: "nas.local.properties"). null 가능.
     * @param defaultPath   posDir 의 최종 fallback (assets 도 없을 때). 예: "/web/.minseo/location/"
     */
    public PlainPrefsCredentials(Context ctx, String prefsName,
                                 @Nullable String assetFileName, String defaultPath) {
        Context app = ctx.getApplicationContext();
        this.sp = app.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        this.defaultPath = defaultPath == null ? "/" : defaultPath;
        if (assetFileName != null) {
            try (InputStream is = app.getAssets().open(assetFileName)) {
                localDefaults.load(is);
            } catch (IOException ignored) {
                // assets 에 fallback 파일 없음 — 정상. 사용자가 런타임에 prefs 채움.
            }
        }
    }

    private String lp(String key, String fallback) {
        return localDefaults.getProperty(key, fallback);
    }

    private int lpInt(String key, int fallback) {
        try { return Integer.parseInt(lp(key, String.valueOf(fallback))); }
        catch (NumberFormatException e) { return fallback; }
    }

    public String getHost() {
        String v = sp.getString(KEY_HOST, "");
        return v.isEmpty() ? lp("nas.host", "") : v;
    }

    public int getPort() { return sp.getInt(KEY_PORT, lpInt("nas.port", 5000)); }

    public String getLanHost() {
        String v = sp.getString(KEY_LAN_HOST, "");
        return v.isEmpty() ? lp("nas.lan_host", "") : v;
    }

    public int getLanPort() { return sp.getInt(KEY_LAN_PORT, lpInt("nas.lan_port", 5000)); }

    @Override public String getUser() {
        String v = sp.getString(KEY_USER, "");
        return v.isEmpty() ? lp("nas.user", "") : v;
    }

    @Override public String getPass() {
        String v = sp.getString(KEY_PASS, "");
        return v.isEmpty() ? lp("nas.pass", "") : v;
    }

    @Override public String getBasePath() {
        String v = sp.getString(KEY_BASE_PATH, "");
        return v.isEmpty() ? lp("nas.base_path", "/") : v;
    }

    @Override public String getPosDir() {
        String v = sp.getString(KEY_PATH, "");
        return v.isEmpty() ? lp("nas.path", defaultPath) : v;
    }

    @Override public String getBaseUrl() { return Urls.buildBaseUrl(getHost(), getPort()); }
    @Override public String getLanUrl()  { return Urls.buildBaseUrl(getLanHost(), getLanPort()); }

    /** 폼 입력 결과를 prefs 에 통째로 저장. */
    public void save(NasCredentialsValue v) {
        sp.edit()
                .putString(KEY_HOST,      v.getHost())
                .putInt(KEY_PORT,         v.getPort())
                .putString(KEY_LAN_HOST,  v.getLanHost())
                .putInt(KEY_LAN_PORT,     v.getLanPort())
                .putString(KEY_USER,      v.getUser())
                .putString(KEY_PASS,      v.getPass())
                .putString(KEY_BASE_PATH, v.getBasePath())
                .putString(KEY_PATH,      v.getPosDir())
                .apply();
    }

    /** 현재 prefs 값을 폼 binding 용 POJO 로. {@link NasFieldsView#setFromValue}. */
    public NasCredentialsValue toValue() {
        return NasCredentialsValue.builder()
                .host(getHost())
                .port(getPort())
                .lanHost(getLanHost())
                .lanPort(getLanPort())
                .user(getUser())
                .pass(getPass())
                .basePath(getBasePath())
                .posDir(getPosDir())
                .build();
    }
}
