package com.csw8929.minseo.nas;

/**
 * NAS 자격증명으로 LAN probe + login 시도. UI 없음 — 진행 상태/결과를 listener 로만 전달.
 * 콜백은 메인 스레드에서 실행된다 ({@link DsAuth#login} 내부 mainHandler 경유).
 *
 * 일반적 사용:
 * <pre>
 * NasConnectionTester.test(value, new NasConnectionTester.Listener() {
 *     public void onProgress(String msg) { tvResult.setText(msg); }
 *     public void onSuccess(String base) { ... save and dismiss ... }
 *     public void onFailure(String msg)  { tvResult.setText("✗ " + msg); }
 * });
 * </pre>
 */
public final class NasConnectionTester {

    public interface Listener {
        /** "LAN probe 중…" 같은 사용자 표시용 메시지. test() 직후 호출. */
        void onProgress(String message);

        /** 로그인 성공. {@code reachedBaseUrl} 은 LAN 폴백 후 실제 도달한 URL. */
        void onSuccess(String reachedBaseUrl);

        /** 실패 — DSM 에러 코드 메시지 또는 네트워크 오류. */
        void onFailure(String message);
    }

    private NasConnectionTester() {}

    /** 동기 init + 비동기 login. */
    public static void test(NasCredentials creds, Listener listener) {
        DsAuth.init(creds);

        String probeNote = creds.getLanUrl() == null || creds.getLanUrl().isEmpty()
                ? "DDNS 로 직접 연결 중…"
                : "LAN probe → 실패 시 DDNS 폴백…";
        listener.onProgress("연결 중… " + probeNote);

        DsAuth.login(new NasCallback<String>() {
            @Override public void onResult(String sid) { listener.onSuccess(DsAuth.apiBase()); }
            @Override public void onError(String msg)  { listener.onFailure(msg); }
        });
    }
}
