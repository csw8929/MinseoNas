package com.csw8929.minseo.nas;

/**
 * NAS 접속 자격증명 추상화. DsAuth / NasConnectionTester 가 이 인터페이스로 소비한다.
 *
 * 구현체:
 * <ul>
 *   <li>{@link NasCredentialsValue} — immutable POJO, 폼 입력값 / 테스트용</li>
 *   <li>{@link PlainPrefsCredentials} — SharedPreferences + assets/nas.local.properties</li>
 *   <li>{@link EncryptedPrefsCredentials} — EncryptedSharedPreferences (security-crypto)</li>
 * </ul>
 */
public interface NasCredentials {

    /** 외부(DDNS / 공인 IP) base URL. 예: "http://my.ddns.net:5000". trailing slash 없이. */
    String getBaseUrl();

    /** LAN base URL. 없으면 빈 문자열. 있으면 DsAuth 가 먼저 probe 후 폴백. */
    String getLanUrl();

    /** NAS 계정명. */
    String getUser();

    /** NAS 비밀번호. */
    String getPass();

    /** FileStation 기본 탐색 경로. 일반적으로 "/". */
    String getBasePath();

    /** 도메인 데이터 폴더. 예: 위치 공유 "/web/.minseo/location/", 비디오 "/video/.minseo". */
    String getPosDir();
}
