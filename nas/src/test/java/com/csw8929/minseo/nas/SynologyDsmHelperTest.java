package com.csw8929.minseo.nas;

import org.junit.Test;

import static org.junit.Assert.*;

public class SynologyDsmHelperTest {

    @Test
    public void normalizeDir_trailingSlash_isStripped() {
        assertEquals("/home/photos", SynologyDsmHelper.normalizeDir("/home/photos/"));
    }

    @Test
    public void normalizeDir_root_isPreserved() {
        assertEquals("/", SynologyDsmHelper.normalizeDir("/"));
    }

    @Test
    public void normalizeDir_null_returnsRoot() {
        assertEquals("/", SynologyDsmHelper.normalizeDir(null));
    }

    @Test
    public void normalizeDir_empty_returnsRoot() {
        assertEquals("/", SynologyDsmHelper.normalizeDir(""));
    }

    @Test
    public void normalizeDir_noTrailingSlash_unchanged() {
        assertEquals("/home/photos", SynologyDsmHelper.normalizeDir("/home/photos"));
    }

    @Test
    public void isSidExpiry_code105_returnsTrue() {
        assertTrue(SynologyDsmHelper.isSidExpiry(105));
    }

    @Test
    public void isSidExpiry_code106_returnsTrue() {
        assertTrue(SynologyDsmHelper.isSidExpiry(106));
    }

    @Test
    public void isSidExpiry_code107_returnsTrue() {
        assertTrue(SynologyDsmHelper.isSidExpiry(107));
    }

    @Test
    public void isSidExpiry_code119_returnsTrue() {
        assertTrue(SynologyDsmHelper.isSidExpiry(119));
    }

    @Test
    public void isSidExpiry_code408_returnsFalse() {
        assertFalse(SynologyDsmHelper.isSidExpiry(408));
    }

    @Test
    public void isSidExpiry_code100_returnsFalse() {
        assertFalse(SynologyDsmHelper.isSidExpiry(100));
    }

    @Test
    public void isSidExpiry_negativeOne_returnsFalse() {
        assertFalse(SynologyDsmHelper.isSidExpiry(-1));
    }
}
