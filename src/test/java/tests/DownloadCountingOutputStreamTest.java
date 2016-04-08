package tests;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.junit.Test;
import updater.DownloadCountingOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DownloadCountingOutputStreamTest {
    @Test
    public void downloadCountingOutputStream_downloading_updateListener() throws IOException {
        int totalByteSize = 20;
        DoubleProperty listenerValue = new SimpleDoubleProperty(0);
        listenerValue.setValue((int) 10);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DownloadCountingOutputStream dcos = new DownloadCountingOutputStream(baos, totalByteSize);
        dcos.addListener(val -> listenerValue.setValue(val));

        for (int i = 0; i < totalByteSize; i++) {
            dcos.write(i);
        }
        assertEquals(baos.size(), totalByteSize);
        // listener to dcos is set in percentage, hence expect 100% in decimal
        assertEquals(listenerValue.get(), 1.0, 0.0001);
    }
}
