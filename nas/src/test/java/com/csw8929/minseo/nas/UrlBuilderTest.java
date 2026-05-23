package com.csw8929.minseo.nas;

import org.junit.Test;

import static org.junit.Assert.*;

public class UrlBuilderTest {

    @Test
    public void buildBaseUrl_port5000_returnsHttp() {
        assertEquals("http://192.168.1.100:5000", Urls.buildBaseUrl("192.168.1.100", 5000));
    }

    @Test
    public void buildBaseUrl_port5001_returnsHttps() {
        assertEquals("https://my.ddns.net:5001", Urls.buildBaseUrl("my.ddns.net", 5001));
    }

    @Test
    public void buildBaseUrl_port443_returnsHttps() {
        assertEquals("https://nas.example.com:443", Urls.buildBaseUrl("nas.example.com", 443));
    }

    @Test
    public void buildBaseUrl_alreadyHasHttpPrefix_returnsAsIs() {
        assertEquals("http://192.168.1.1:5000", Urls.buildBaseUrl("http://192.168.1.1:5000", 9999));
    }

    @Test
    public void buildBaseUrl_alreadyHasHttpsWithTrailingSlash_stripsSlash() {
        assertEquals("https://nas.home", Urls.buildBaseUrl("https://nas.home/", 443));
    }

    @Test
    public void buildBaseUrl_emptyHost_returnsEmpty() {
        assertEquals("", Urls.buildBaseUrl("", 5000));
    }

    @Test
    public void buildBaseUrl_nullHost_returnsEmpty() {
        assertEquals("", Urls.buildBaseUrl(null, 5000));
    }
}
