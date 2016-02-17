package tests;

import org.junit.Test;
import updater.HtDownloadLink;
import util.Version;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class HtDownloadLinkTest {

    @Test
    public void htDownloadLinkCompareTo_sameVersionDiffDownloadLinkUrln_sameObject() throws MalformedURLException {
        Version version = new Version(1, 0, 0);
        HtDownloadLink a = new HtDownloadLink();
        a.setVersion(version);
        a.setDownloadLinkUrl(new URL("http://google.com"));

        HtDownloadLink b = new HtDownloadLink();
        b.setVersion(version);
        b.setDownloadLinkUrl(new URL("http://yahoo.com"));

        assertTrue(a.compareTo(b) == 0);
        assertTrue(a.equals(b));
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void htDownloadLinkCompareTo_diffVersionSameDownloadLinkUrl_diffObject() throws MalformedURLException {
        String fileLocationString = "http://google.com";
        HtDownloadLink a = new HtDownloadLink();
        a.setVersion(new Version(2, 0, 0));
        a.setDownloadLinkUrl(new URL(fileLocationString));

        HtDownloadLink b = new HtDownloadLink();
        b.setVersion(new Version(1, 2, 0));
        b.setDownloadLinkUrl(new URL(fileLocationString));

        assertFalse(a.compareTo(b) == 0);
        assertFalse(a.equals(b));
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void htDownloadLinkCompareTo_diffVersion_correctComparison() throws MalformedURLException {
        String fileLocationString = "http://google.com";
        HtDownloadLink a = new HtDownloadLink();
        a.setDownloadLinkUrl(new URL(fileLocationString));

        HtDownloadLink b = new HtDownloadLink();
        b.setDownloadLinkUrl(new URL(fileLocationString));

        a.setVersion(new Version(2, 0, 0));
        b.setVersion(new Version(1, 2, 0));
        assertTrue(a.compareTo(b) > 0);

        a.setVersion(new Version(1, 0, 0));
        b.setVersion(new Version(3, 0, 3));
        assertTrue(a.compareTo(b) < 0);
    }

    @Test
    public void htDownloadLinkGetVersion_setVersionByReflection_getCorrectValue()
            throws NoSuchFieldException, IllegalAccessException, MalformedURLException {
        HtDownloadLink a = new HtDownloadLink();

        a.setDownloadLinkUrl(new URL("http://google.com"));

        Version version = new Version(10, 11, 12);

        Class<?> htDownloadLinkClass = a.getClass();

        Field versionField = htDownloadLinkClass.getDeclaredField("version");
        versionField.setAccessible(true);

        versionField.set(a, version);

        assertEquals(version, a.getVersion());
    }

    @Test
    public void htDownloadLinkGetDownloadLinkUrl_setDownloadLinkUrlByReflection_getCorrectValue()
            throws NoSuchFieldException, IllegalAccessException, MalformedURLException {
        HtDownloadLink a = new HtDownloadLink();
        a.setVersion(new Version(10, 11, 12));

        URL fileLocation = new URL("http://google.com");

        Class<?> htDownloadLinkClass = a.getClass();

        Field downloadLinkUrlField = htDownloadLinkClass.getDeclaredField("downloadLinkUrl");
        downloadLinkUrlField.setAccessible(true);

        downloadLinkUrlField.set(a, fileLocation);

        assertEquals(fileLocation, a.getDownloadLinkUrl());
    }

    @Test
    public void htDownloadLinkSetVersion_getVersionByReflection_valueSetCorrectly()
            throws NoSuchFieldException, IllegalAccessException, MalformedURLException {
        HtDownloadLink a = new HtDownloadLink();
        a.setDownloadLinkUrl(new URL("http://google.com"));

        Version version = new Version(10, 11, 12);
        a.setVersion(version);

        Class<?> htDownloadLinkClass = a.getClass();

        Field versionField = htDownloadLinkClass.getDeclaredField("version");
        versionField.setAccessible(true);

        Version versionFromReflection = (Version) versionField.get(a);
        assertEquals(version, versionFromReflection);
    }

    @Test
    public void htDownloadLinkSetDownloadLinkUrl_getDownloadLinkUrlByReflection_valueSetCorrectly()
            throws NoSuchFieldException, IllegalAccessException, MalformedURLException {
        HtDownloadLink a = new HtDownloadLink();
        a.setVersion(new Version(10, 11, 12));

        URL fileLocation = new URL("http://google.com");

        a.setDownloadLinkUrl(fileLocation);

        Class<?> htDownloadLinkClass = a.getClass();

        Field downloadLinkUrlField = htDownloadLinkClass.getDeclaredField("downloadLinkUrl");
        downloadLinkUrlField.setAccessible(true);

        URL fileLocationFromReflection = (URL) downloadLinkUrlField.get(a);
        assertEquals(fileLocation, fileLocationFromReflection);
    }
}
