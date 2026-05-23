# MinseoNas

Synology FileStation 기반 Android 앱을 위한 공용 NAS 어댑터 라이브러리.

- groupId: `com.csw8929.minseo`
- artifactId: `nas`
- version: `0.2.0`
- 자바 패키지: `com.csw8929.minseo.nas` (UI 컴포넌트는 `.ui` 서브패키지)

## 빌드 / 로컬 publish

```bash
./gradlew :nas:publishToMavenLocal
# → ~/.m2/repository/com/csw8929/minseo/nas/0.1.0/nas-0.1.0.aar
```

## 사용 (consumer 앱)

```kotlin
// settings.gradle.kts 또는 app/build.gradle.kts repositories 에 mavenLocal() 추가
repositories { mavenLocal(); google(); mavenCentral() }

// app/build.gradle.kts
dependencies {
    implementation("com.csw8929.minseo:nas:0.2.0")
    // EncryptedPrefsCredentials 사용 시에만 추가 (Minseo21 같은 케이스)
    // implementation("androidx.security:security-crypto:1.1.0-alpha06")
}
```

## 0.2.0 구성

### 저수준 (0.1.0 부터)
- `DsHttp` — HTTP IO (GET, multipart upload, probeUrl)
- `DsAuth` — 자격증명 + SID 세션 + LAN/외부 base URL 해석. `init(NasCredentials)` 오버로드
- `SynologyDsmHelper` — SID 재시도 (105/106/107/119), 에러 envelope, normalizeDir
- `NasCallback` — 공용 비동기 콜백 인터페이스
- `DsmException` — DSM API 호출 동기 에러
- `Urls` — host+port → http(s) URL 조립

### FileStation (0.2.0)
- `FileStation` — 동기 헬퍼: `uploadJson` / `downloadJson` / `listFiles` / `delete`. 내부에서 SID 재시도 + envelope 처리.

### 자격증명 추상화 (0.2.0)
- `NasCredentials` — 6필드 인터페이스 (baseUrl / lanUrl / user / pass / basePath / posDir)
- `NasCredentialsValue` — immutable POJO + Builder. 폼 입력 / 테스트용.
- `PlainPrefsCredentials` — SharedPreferences + `assets/nas.local.properties` fallback (Minseo6 패턴)
- `EncryptedPrefsCredentials` — EncryptedSharedPreferences (Minseo21 패턴, `security-crypto` 의존성 필요)

### UI / 흐름 (0.2.0)
- `NasConnectionTester` — UI 없는 비동기 연결 테스트. listener 로 progress/success/failure.
- `ui.NasFieldsView` — 공용 7필드 CustomView (LinearLayout 서브클래스). `setFromValue` / `getFormValues`.

## 단위 테스트

- `SynologyDsmHelperTest` — normalizeDir, isSidExpiry 계약 (12 case)
- `UrlBuilderTest` — buildBaseUrl scheme / 폴백 (7 case)

설계 문서: 사용자 `~/.gstack/projects/csw8929-Minseo6/USER-worktree-feature+nas-library-design-*.md`
