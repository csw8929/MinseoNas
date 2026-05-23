# MinseoNas

Synology FileStation 기반 Android 앱을 위한 공용 NAS 어댑터 라이브러리.

- groupId: `com.csw8929.minseo`
- artifactId: `nas`
- version: `0.1.0`
- 자바 패키지: `com.csw8929.minseo.nas`

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
    implementation("com.csw8929.minseo:nas:0.1.0")
}
```

## 0.1.0 구성

- `DsHttp` — HTTP IO (GET, multipart upload, probeUrl)
- `DsAuth` — 자격증명 + SID 세션 + LAN/외부 base URL 해석
- `SynologyDsmHelper` — SID 재시도 (105/106), 에러 envelope, normalizeDir
- `NasCallback` — 공용 비동기 콜백 인터페이스
- `DsmException` — DSM API 호출 동기 에러

이후 0.2.0 에서 FileStation 동기 헬퍼, NasCredentials 인터페이스, NasConnectionTester, NasFieldsView 등 추가 예정.

설계 문서: 사용자 `~/.gstack/projects/csw8929-Minseo6/USER-worktree-feature+nas-library-design-*.md`
