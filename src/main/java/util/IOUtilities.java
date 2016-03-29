package util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class IOUtilities {

    public static ByteArrayOutputStream inputStreamToByteArrayOutputStream(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4096];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer;
    }

    private IOUtilities() {
    }
}
