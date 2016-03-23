package tests;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.FileHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class FileHelperTest {

    private static final String TEST_FOLDER = "testfolder";
    private static final String TEST_FILE_NAME = "expected.txt";

    @Before
    public void initialiseTestFilesDirectory() {
        try {
            FileUtils.deleteDirectory(new File(TEST_FOLDER));
            Files.createDirectories(Paths.get(TEST_FOLDER));
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    @After
    public void removeTestFilesDirectory() {
        try {
            FileUtils.deleteDirectory(new File(TEST_FOLDER));
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    private void createTestFile(String content) {
        try {
            Files.write(Paths.get(TEST_FOLDER, TEST_FILE_NAME), content.getBytes("UTF-8"));
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    private void createEmptyTestFile() {
        try {
            Files.createFile(Paths.get(TEST_FOLDER, TEST_FILE_NAME));
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    @Test
    public void exists_fileExists_true() {
        createEmptyTestFile();
        FileHelper fileHelper = new FileHelper(TEST_FOLDER, TEST_FILE_NAME);
        assertTrue(fileHelper.exists());
    }

    @Test
    public void exists_fileDoesNotExist_false() {
        FileHelper fileHelper = new FileHelper(TEST_FOLDER, TEST_FILE_NAME);
        assertFalse(fileHelper.exists());
    }

    @Test
    public void loadFileContents_fromEmptyFile_emptyString() {
        createEmptyTestFile();
        FileHelper fileHelper = new FileHelper(TEST_FOLDER, TEST_FILE_NAME);
        try {
            assertEquals("", fileHelper.loadFileContents());
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    @Test
    public void loadFileContents_fromConstructedFile_constructedString() {
        String fileContents = "this is a test string 42 {}\nthis is a new line 44";
        createTestFile(fileContents);
        FileHelper fileHelper = new FileHelper(TEST_FOLDER, TEST_FILE_NAME);
        try {
            assertEquals(fileContents, fileHelper.loadFileContents());
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    @Test
    public void writeFileContents_emptyString_emptyFileContents() {
        FileHelper fileHelper = new FileHelper(TEST_FOLDER, TEST_FILE_NAME);
        try {
            fileHelper.writeFileContents("");
            assertEquals("", FileUtils.readFileToString(new File(TEST_FOLDER, TEST_FILE_NAME)));
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    @Test
    public void writeFileContents_constructedString_constructedFileContents() {
        String fileContents = "this is a test string 47 {}\nthis is a new line 49";
        FileHelper fileHelper = new FileHelper(TEST_FOLDER, TEST_FILE_NAME);
        try {
            fileHelper.writeFileContents(fileContents);
            assertEquals(fileContents, FileUtils.readFileToString(new File(TEST_FOLDER, TEST_FILE_NAME)));
        } catch (IOException e) {
            fail(e.toString());
        }
    }
}
