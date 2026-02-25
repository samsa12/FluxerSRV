package com.fluxer.srv;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;

public class FluxerRestApi {
    private static final String BASE_URL = "https://api.fluxer.app/v1";
    private static final Gson GSON = new Gson();
    private final String token;
    private final OkHttpClient httpClient;

    public FluxerRestApi(String token, OkHttpClient httpClient) {
        this.token = token;
        this.httpClient = httpClient;
    }

    public void sendMessage(String channelId, String content, boolean announcement) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("content", content);
        if (announcement) {
            body.addProperty("announcement", true);
        }

        post("/channels/" + channelId + "/messages", body);
    }

    public void sendMessage(String channelId, String content) throws IOException {
        sendMessage(channelId, content, false);
    }

    private void post(String route, JsonObject body) throws IOException {
        post(route, body, true);
    }

    private void post(String route, JsonObject body, boolean authorize) throws IOException {
        Request.Builder builder = new Request.Builder()
                .url(BASE_URL + route)
                .post(RequestBody.create(MediaType.parse("application/json"), body.toString()));

        if (authorize) {
            builder.header("Authorization", "Bot " + token);
        }

        try (Response response = httpClient.newCall(builder.build()).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "null";
                throw new IOException("Unexpected code " + response + " | Body: " + errorBody);
            }
        }
    }
}
