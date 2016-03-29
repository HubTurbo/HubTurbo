package tests;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.FileHelper;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class FileHelperTest {

    private static final String TEST_FOLDER = "testfolder";
    private static final String TEST_FILE_NAME = "expected.txt";

    @Before
    public void initialiseTestFilesDirectory() throws IOException {
        FileUtils.deleteDirectory(new File(TEST_FOLDER));
        Files.createDirectories(Paths.get(TEST_FOLDER));
    }

    @After
    public void removeTestFilesDirectory() throws IOException {
        FileUtils.deleteDirectory(new File(TEST_FOLDER));
    }

    private void createTestFile(String content) throws IOException {
        Files.write(Paths.get(TEST_FOLDER, TEST_FILE_NAME), content.getBytes("UTF-8"));
    }

    private void createEmptyTestFile() throws IOException{
        Files.createFile(Paths.get(TEST_FOLDER, TEST_FILE_NAME));
    }

    @Test
    public void exists_fileExists_true() throws IOException {
        createEmptyTestFile();
        assertTrue(FileHelper.isFileExists(TEST_FOLDER, TEST_FILE_NAME));
    }

    @Test
    public void exists_fileDoesNotExist_false() {
        assertFalse(FileHelper.isFileExists(TEST_FOLDER, TEST_FILE_NAME));
    }

    @Test
    public void loadFileContents_fromEmptyFile_emptyString() throws IOException {
        createEmptyTestFile();
        assertEquals("", FileHelper.getFileContents(TEST_FOLDER, TEST_FILE_NAME));
    }

    @Test
    public void loadFileContents_fromConstructedFile_constructedString() throws IOException {
        String fileContents = "this is a test string 42 {}\nthis is a new line 44";
        createTestFile(fileContents);
        assertEquals(fileContents, FileHelper.getFileContents(TEST_FOLDER, TEST_FILE_NAME));
    }

    @Test
    public void writeFileContents_emptyString_emptyFileContents() throws IOException {
        FileHelper.writeFileContents(TEST_FOLDER, TEST_FILE_NAME, "");
        assertEquals("", FileUtils.readFileToString(new File(TEST_FOLDER, TEST_FILE_NAME)));
    }

    @Test
    public void writeFileContents_constructedString_constructedFileContents() throws IOException {
        String fileContents = "this is a test string 47 {}\nthis is a new line 49";
        FileHelper.writeFileContents(TEST_FOLDER, TEST_FILE_NAME, fileContents);
        assertEquals(fileContents, FileUtils.readFileToString(new File(TEST_FOLDER, TEST_FILE_NAME)));
    }
}
