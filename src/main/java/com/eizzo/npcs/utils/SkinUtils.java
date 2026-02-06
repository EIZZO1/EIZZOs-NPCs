package com.eizzo.npcs.utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
public class SkinUtils {
    private static final String MINESKIN_API = "https://api.mineskin.org/generate/upload";
    private static final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    public static String[] fetchSkinFromMojang(String username) {
        try {
            System.out.println("[Debug] Fetching UUID for: " + username);
            // 1. Get UUID from username using the newer reliable endpoint
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.minecraftservices.com/minecraft/profile/lookup/name/" + username))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                System.out.println("[Debug] Mojang lookup (minecraftservices) failed: " + response.statusCode() + ". Trying legacy api.mojang.com...");
                // Secondary attempt at legacy endpoint
                request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.mojang.com/users/profiles/minecraft/" + username))
                        .GET()
                        .build();
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
            }

            if (response.statusCode() != 200) {
                System.out.println("[Debug] All Mojang UUID lookups failed. Trying fallback...");
                return fetchSkinFromFallback(username);
            }

            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            String uuid = json.get("id").getAsString();
            System.out.println("[Debug] Got UUID: " + uuid + " for " + username);

            // 2. Get Profile (textures) from UUID using the .com endpoint
            request = HttpRequest.newBuilder()
                    .uri(URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false"))
                    .GET()
                    .build();
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.out.println("[Debug] Mojang Profile lookup failed: " + response.statusCode());
                return fetchSkinFromFallback(username);
            }
            json = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray properties = json.getAsJsonArray("properties");
            for (int i = 0; i < properties.size(); i++) {
                JsonObject prop = properties.get(i).getAsJsonObject();
                if (prop.get("name").getAsString().equals("textures")) {
                    String value = prop.get("value").getAsString();
                    String signature = prop.get("signature").getAsString();
                    System.out.println("[Debug] Successfully retrieved texture properties for " + username);
                    return new String[]{value, signature};
                }
            }
            System.out.println("[Debug] No 'textures' property found for " + username + ". Trying fallback...");
            return fetchSkinFromFallback(username);
        } catch (Exception e) {
            System.err.println("[Debug] Exception in Mojang lookup for " + username + ": " + e.getMessage());
            return fetchSkinFromFallback(username);
        }
    }

    private static String[] fetchSkinFromFallback(String username) {
        try {
            System.out.println("[Debug] Fetching skin from MC-Heads for: " + username);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://mc-heads.net/minecraft/profile/" + username))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return null;
            
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray properties = json.getAsJsonArray("properties");
            for (int i = 0; i < properties.size(); i++) {
                JsonObject prop = properties.get(i).getAsJsonObject();
                if (prop.get("name").getAsString().equals("textures")) {
                    return new String[]{prop.get("value").getAsString(), prop.get("signature").getAsString()};
                }
            }
        } catch (Exception e) {
            System.err.println("[Debug] Fallback skin lookup failed: " + e.getMessage());
        }
        return null;
    }

    public static String[] uploadSkin(File skinFile) {
        try {
            String boundary = "---" + System.currentTimeMillis() + "---";
            byte[] fileBytes = Files.readAllBytes(skinFile.toPath());
            StringBuilder header = new StringBuilder();
            header.append("--").append(boundary).append("\r\n");
            header.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(skinFile.getName()).append("\"\r\n");
            header.append("Content-Type: image/png\r\n\r\n");
            byte[] headerBytes = header.toString().getBytes(StandardCharsets.UTF_8);
            byte[] footerBytes = ("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8);
            byte[] body = new byte[headerBytes.length + fileBytes.length + footerBytes.length];
            System.arraycopy(headerBytes, 0, body, 0, headerBytes.length);
            System.arraycopy(fileBytes, 0, body, headerBytes.length, fileBytes.length);
            System.arraycopy(footerBytes, 0, body, headerBytes.length + fileBytes.length, footerBytes.length);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(MINESKIN_API))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return null;
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonObject data = json.getAsJsonObject("data");
            JsonObject texture = data.getAsJsonObject("texture");
            return new String[]{texture.get("value").getAsString(), texture.get("signature").getAsString()};
        } catch (Exception e) {
            return null;
        }
    }
}