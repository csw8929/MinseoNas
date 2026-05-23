package com.csw8929.minseo.nas;

/** NAS 접속 URL 조립 유틸. host + port 를 http(s) base URL로. */
public final class Urls {

    private Urls() {}

    /**
     * "host" + port 를 http/https scheme 으로 조립.
     * - port 5001 / 443 → https
     * - 그 외 → http
     * - host 가 이미 http(s):// 로 시작하면 trailing slash만 정리해서 반환
     * - host 가 null / 빈 문자열이면 "" 반환
     */
    public static String buildBaseUrl(String host, int port) {
        if (host == null || host.isEmpty()) return "";
        String h = host.trim();
        if (h.startsWith("http://") || h.startsWith("https://")) {
            while (h.endsWith("/")) h = h.substring(0, h.length() - 1);
            return h;
        }
        String scheme = (port == 5001 || port == 443) ? "https" : "http";
        return scheme + "://" + h + ":" + port;
    }
}
