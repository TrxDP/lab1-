
package com.duellite.api;

import com.duellite.model.Card;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;


public class YgoApiClient {

    private static final String RANDOM_CARD_URL = "https://db.ygoprodeck.com/api/v7/randomcard.php";
    private static final int MAX_ATTEMPTS = 40;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    /**
     * Pide cartas aleatorias hasta obtener una carta Monster valida
     */
    public Card fetchRandomMonster() throws IOException, InterruptedException {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            String body = get(RANDOM_CARD_URL);
            Card card = parseMonster(body);
            if (card != null) {
                return card;
            }
        }
        throw new IllegalStateException("No se pudo cargar la carta");
    }

    /** Descarga una imagen desde su URL. Devuelve null si no se pudo leer. */
    public BufferedImage fetchImage(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("User-Agent", "YuGiOhDuelLite/1.0")
                .GET().build();
        HttpResponse<byte[]> response = http.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + " al descargar la imagen");
        }
        return ImageIO.read(new ByteArrayInputStream(response.body()));
    }

    private String get(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("User-Agent", "YuGiOhDuelLite/1.0")
                .header("Accept", "application/json")
                .GET().build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode());
        }
        return response.body();
    }

    /** Convierte el JSON en Card */
    private Card parseMonster(String body) {
        try {
            JSONObject json = new JSONObject(body);
            if (json.has("data")) {
                JSONArray data = json.getJSONArray("data");
                if (data.isEmpty()) return null;
                json = data.getJSONObject(0);
            }
            String type = json.optString("type", "");
            if (!type.contains("Monster")) return null;
            if (!json.has("atk") || !json.has("def")) return null;

            JSONArray images = json.optJSONArray("card_images");
            if (images == null || images.isEmpty()) return null;
            String image = images.getJSONObject(0).optString("image_url_small", "");
            if (image.isEmpty()) image = images.getJSONObject(0).optString("image_url", "");
            if (image.isEmpty()) return null;

            return new Card(json.getString("name"), json.getInt("atk"), json.getInt("def"), image);
        } catch (Exception e) {
            return null;
        }
    }
}
