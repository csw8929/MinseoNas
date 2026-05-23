package com.csw8929.minseo.nas;

/**
 * NAS 자격증명 immutable POJO. {@link NasFieldsView} 폼 입력 결과로 만들거나,
 * 테스트에서 직접 만들어서 {@link DsAuth#init(NasCredentials)} 에 넘기는 용도.
 *
 * host + port 분리 형태로 보관하고 {@link #getBaseUrl()} 에서 {@link Urls#buildBaseUrl} 로 조립.
 */
public final class NasCredentialsValue implements NasCredentials {

    private final String host;
    private final int port;
    private final String lanHost;
    private final int lanPort;
    private final String user;
    private final String pass;
    private final String basePath;
    private final String posDir;

    private NasCredentialsValue(Builder b) {
        this.host     = b.host     == null ? "" : b.host;
        this.port     = b.port;
        this.lanHost  = b.lanHost  == null ? "" : b.lanHost;
        this.lanPort  = b.lanPort;
        this.user     = b.user     == null ? "" : b.user;
        this.pass     = b.pass     == null ? "" : b.pass;
        this.basePath = b.basePath == null || b.basePath.isEmpty() ? "/" : b.basePath;
        this.posDir   = b.posDir   == null || b.posDir.isEmpty()   ? "/" : b.posDir;
    }

    // ── NasCredentials ─────────────────────────────────────────────────────
    @Override public String getBaseUrl()  { return Urls.buildBaseUrl(host,    port); }
    @Override public String getLanUrl()   { return Urls.buildBaseUrl(lanHost, lanPort); }
    @Override public String getUser()     { return user; }
    @Override public String getPass()     { return pass; }
    @Override public String getBasePath() { return basePath; }
    @Override public String getPosDir()   { return posDir; }

    // ── 폼 binding 용 raw accessors ─────────────────────────────────────────
    public String getHost()    { return host; }
    public int    getPort()    { return port; }
    public String getLanHost() { return lanHost; }
    public int    getLanPort() { return lanPort; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String host;
        private int    port = 5000;
        private String lanHost;
        private int    lanPort = 5000;
        private String user;
        private String pass;
        private String basePath = "/";
        private String posDir   = "/";

        public Builder host(String v)     { this.host = v; return this; }
        public Builder port(int v)        { this.port = v; return this; }
        public Builder lanHost(String v)  { this.lanHost = v; return this; }
        public Builder lanPort(int v)     { this.lanPort = v; return this; }
        public Builder user(String v)     { this.user = v; return this; }
        public Builder pass(String v)     { this.pass = v; return this; }
        public Builder basePath(String v) { this.basePath = v; return this; }
        public Builder posDir(String v)   { this.posDir = v; return this; }

        public NasCredentialsValue build() { return new NasCredentialsValue(this); }
    }
}
