package ui.components.issue_creators;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Optional;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import util.HTLog;

public class UploadImageTask {

    private static final Logger logger = HTLog.get(UploadImageTask.class);
    private static final Base64.Encoder encoder = Base64.getEncoder();
    private static final String UPLOAD_URL = "https://api.imgur.com/3/image";
    private static final String CLIENT_ID = "42bc0de7b1ed93e";
    
    public final Optional<JSONObject> response;

    public UploadImageTask(Image fxImage) {
        String data = "";
        try {
            data = URLEncoder.encode("image", "UTF-8") + "="
                    + URLEncoder.encode(convertToBase64String(fxImage), "UTF-8");
            System.out.println("success encode");
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage());
        }
        response = setupConnection(data);
    }
    
    
    private Optional<JSONObject> setupConnection(String data) {
        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) new URL(UPLOAD_URL).openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Client-ID " + CLIENT_ID);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.connect();
            
            // Writing to connection
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();
            
            // Reading response
            StringBuilder stb = new StringBuilder();
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                stb.append(line).append("\n");
            }
            wr.close();
            rd.close();

            return getJSONResponse(stb.toString());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return Optional.empty();
    }

    private Optional<JSONObject> getJSONResponse(String response) {
        Optional<JSONObject> json = Optional.empty();
        try {
            json = Optional.of(new JSONObject(response));
        } catch (JSONException e) {
            logger.error(e.getMessage());
        }
        return json;
    }

    /**
     * Converts JavaFX Image to Base64 string
     * @param fxImage
     */
    private String convertToBase64String(Image fxImage) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(fxImage, null), "png", bos);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return encoder.encodeToString(bos.toByteArray());
    }
    
}
