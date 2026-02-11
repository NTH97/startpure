package org.dreambot.scripts.startpure;

import org.dreambot.api.Client;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class DiscordNotifier {

    private DiscordNotifier() {}

    public static boolean sendNotification(String webhookUrl, String message) {
        try {
            byte[] imageBytes = captureScreenshot();
            if (imageBytes == null) {
                return false;
            }

            String boundary = "----DreamBotBoundary" + System.currentTimeMillis();

            HttpURLConnection connection = (HttpURLConnection) new URL(webhookUrl).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            connection.setRequestProperty("User-Agent", "DreamBot-StartPure/1.0");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            try (OutputStream os = connection.getOutputStream()) {
                String payloadJson = "{\"content\":\"" + escapeJson(message) + "\"}";

                // payload_json part
                writeMultipartField(os, boundary, "payload_json", payloadJson);

                // file part
                writeMultipartFile(os, boundary, "file", "screenshot.png", "image/png", imageBytes);

                // closing boundary
                String closing = "\r\n--" + boundary + "--\r\n";
                os.write(closing.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            int responseCode = connection.getResponseCode();
            connection.disconnect();
            return responseCode >= 200 && responseCode < 300;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static byte[] captureScreenshot() {
        try {
            BufferedImage image = Client.getCanvasImage();
            if (image == null) {
                return null;
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void writeMultipartField(OutputStream os, String boundary, String name, String value) throws IOException {
        String part = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"" + name + "\"\r\n"
                + "Content-Type: application/json\r\n\r\n"
                + value;
        os.write(part.getBytes(StandardCharsets.UTF_8));
    }

    private static void writeMultipartFile(OutputStream os, String boundary, String name, String filename, String contentType, byte[] data) throws IOException {
        String header = "\r\n--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + filename + "\"\r\n"
                + "Content-Type: " + contentType + "\r\n\r\n";
        os.write(header.getBytes(StandardCharsets.UTF_8));
        os.write(data);
    }

    private static String escapeJson(String input) {
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
