package com.csw8929.minseo.nas;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

/**
 * EncryptedSharedPreferences 기반 자격증명 보관. Minseo21 NasCredentialStore 일반화.
 *
 * <p><b>의존성</b>: 이 클래스를 사용하는 앱은 {@code androidx.security:security-crypto} 를
 * implementation 으로 추가해야 한다. (라이브러리 안에서는 compileOnly.)
 *
 * <p>EncryptedSharedPreferences 초기화 실패 시 평문 SharedPreferences 로 폴백
 * (앱이 동작은 하되 자격증명이 평문 저장됨 — Log.w 로 알림).
 */
public final class EncryptedPrefsCredentials implements NasCredentials {

    private static final String TAG = "NasCred";

    private static final String KEY_BASE_URL  = "base_url";
    private static final String KEY_LAN_URL   = "lan_url";
    private static final String KEY_USER      = "user";
    private static final String KEY_PASS      = "pass";
    private static final String KEY_BASE_PATH = "base_path";
    private static final String KEY_POS_DIR   = "pos_dir";

    private final SharedPreferences prefs;
    private final String defaultBaseUrl;
    private final String defaultLanUrl;
    private final String defaultBasePath;
    private final String defaultPosDir;

    /**
     * @param ctx             Application context
     * @param prefsName       EncryptedSharedPreferences 파일명 (예: "nas_credentials")
     * @param defaultBaseUrl  사용자가 prefs 안 채웠을 때의 fallback (앱에 박힌 기본 NAS 주소). "" 가능.
     * @param defaultLanUrl   LAN 주소 fallback. "" 가능.
     * @param defaultBasePath FileStation 기본 경로 fallback. 일반적으로 "/" 또는 도메인별 (Minseo21 "/video").
     * @param defaultPosDir   도메인 데이터 폴더 fallback (Minseo21 "/video/.minseo").
     */
    public EncryptedPrefsCredentials(Context ctx, String prefsName,
                                     String defaultBaseUrl, String defaultLanUrl,
                                     String defaultBasePath, String defaultPosDir) {
        this.defaultBaseUrl  = nz(defaultBaseUrl);
        this.defaultLanUrl   = nz(defaultLanUrl);
        this.defaultBasePath = defaultBasePath == null || defaultBasePath.isEmpty() ? "/" : defaultBasePath;
        this.defaultPosDir   = defaultPosDir   == null || defaultPosDir.isEmpty()   ? "/" : defaultPosDir;

        Context app = ctx.getApplicationContext();
        SharedPreferences p = null;
        try {
            MasterKey masterKey = new MasterKey.Builder(app)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            p = EncryptedSharedPreferences.create(
                    app, prefsName, masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (Exception e) {
            Log.w(TAG, "EncryptedSharedPreferences 초기화 실패 — 평문 prefs 로 폴백", e);
            p = app.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        }
        this.prefs = p;
    }

    private static String nz(String v) { return v == null ? "" : v; }

    /** 사용자가 자격증명을 직접 입력해서 저장한 적 있는지. */
    public boolean hasCredentials() {
        return !getUser().isEmpty() && !getPass().isEmpty();
    }

    @Override public String getBaseUrl()  { return prefs.getString(KEY_BASE_URL,  defaultBaseUrl); }
    @Override public String getLanUrl()   { return prefs.getString(KEY_LAN_URL,   defaultLanUrl); }
    @Override public String getUser()     { return prefs.getString(KEY_USER,      ""); }
    @Override public String getPass()     { return prefs.getString(KEY_PASS,      ""); }
    @Override public String getBasePath() { return prefs.getString(KEY_BASE_PATH, defaultBasePath); }
    @Override public String getPosDir()   { return prefs.getString(KEY_POS_DIR,   defaultPosDir); }

    /** 폼 입력값으로 통째 저장. */
    public void save(NasCredentialsValue v) {
        prefs.edit()
                .putString(KEY_BASE_URL,  v.getBaseUrl())
                .putString(KEY_LAN_URL,   v.getLanUrl())
                .putString(KEY_USER,      v.getUser())
                .putString(KEY_PASS,      v.getPass())
                .putString(KEY_BASE_PATH, v.getBasePath())
                .putString(KEY_POS_DIR,   v.getPosDir())
                .apply();
    }

    /** {@link NasFieldsView} 에 채우기 위한 POJO 변환. baseUrl 은 host+port 로 분해 불가하므로
     *  buildBaseUrl 형식("http(s)://host:port") 가정. 임의 URL 이면 host 필드에 baseUrl 통째.
     */
    public NasCredentialsValue toValue() {
        return NasCredentialsValue.builder()
                .host(getBaseUrl())   // 단순화 — 필요 시 앱이 별도 분해
                .port(5000)
                .lanHost(getLanUrl())
                .lanPort(5000)
                .user(getUser())
                .pass(getPass())
                .basePath(getBasePath())
                .posDir(getPosDir())
                .build();
    }

    public void clear() { prefs.edit().clear().apply(); }
}
