package updater;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import org.apache.commons.io.output.CountingOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;

/**
 * This class counts the bytes that have been downloaded and tell its listener(s) about current percentage downloaded
 */
public class DownloadCountingOutputStream extends CountingOutputStream {
    private final long totalFileSizeInBytes;
    private final DoubleProperty percentageDownloaded; // 0.0 to 1.0

    /**
     *
     * @param out output stream
     * @param totalFileSizeInBytes total file size to be downloaded
     */
    public DownloadCountingOutputStream(OutputStream out, long totalFileSizeInBytes) {
        super(out);

        this.totalFileSizeInBytes = totalFileSizeInBytes;
        percentageDownloaded = new SimpleDoubleProperty(0);
    }

    public void addListener(Consumer<Double> listener) {
        percentageDownloaded.addListener(
                (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) ->
                        listener.accept(newValue.doubleValue()));
    }

    /**
     * {@inheritDoc}
     *
     * Updates percentage downloaded
     */
    @Override
    protected void afterWrite(int n) throws IOException {
        super.afterWrite(n);

        percentageDownloaded.set(1.0 * this.getByteCount() / this.totalFileSizeInBytes);
    }
}
