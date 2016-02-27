package updater;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URI;
import java.net.URLConnection;
import java.util.function.Consumer;

/**
 * A class in charge of downloading file.
 */
public class FileDownloader {
    private static final Logger logger = LogManager.getLogger(FileDownloader.class.getName());

    public static final int CONNECTION_TIMEOUT = 15000;
    public static final int READ_CONNECTION_TIMEOUT = 30000;

    private final URI source;
    private final File dest;
    private long totalFileSizeInBytes;

    private final Consumer<Double> onDownload;

    /**
     * @param source URI of file source, expect to be a valid URL
     * @param dest local destination file
     * @param onDownload callback that listen to download stream
     */
    public FileDownloader(URI source, File dest, Consumer<Double> onDownload) {
        this.source = source;
        this.dest = dest;
        this.onDownload = onDownload;
    }

    /**
     * Starts download from source to destination as specified in constructor
     *
     * @return true if download is successful, else false
     */
    public boolean download() {
        URLConnection sourceConnection = null;

        try {
            sourceConnection = setupConnection(source);
        } catch (IOException e) {
            logger.error("URI of source file is not a well-formed URL");
            return false;
        }

        assert sourceConnection != null;

        totalFileSizeInBytes = sourceConnection.getContentLengthLong();

        try (
                InputStream inputStream = setupStreamFromSource(sourceConnection);
                OutputStream outputStream = setupStreamToDest(dest);
        ) {
            downloadStream(inputStream, outputStream);
        } catch (IOException e) {
            logger.error("Failed to create streams for download or socket timeout", e);
            return false;
        }

        return true;
    }

    private URLConnection setupConnection(URI source) throws IOException {
        URLConnection connection = source.toURL().openConnection();
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        connection.setReadTimeout(READ_CONNECTION_TIMEOUT);

        return connection;
    }

    private InputStream setupStreamFromSource(URLConnection source) throws IOException {
        InputStream inputStream = source.getInputStream();

        return inputStream;
    }

    private OutputStream setupStreamToDest(File dest) throws IOException {
        if (dest.exists() && !dest.delete()) {
            throw new IOException("Failed to delete old download file");
        }

        if (dest.createNewFile()) {
            return new FileOutputStream(dest);
        } else {
            throw new IOException("Failed to create new download file");
        }
    }

    private int downloadStream(InputStream inputStream, OutputStream outputStream) throws IOException {
        DownloadCountingOutputStream countingOutputStream =
                new DownloadCountingOutputStream(outputStream, totalFileSizeInBytes);

        countingOutputStream.addListener(onDownload);

        return IOUtils.copy(inputStream, countingOutputStream);
    }
}
