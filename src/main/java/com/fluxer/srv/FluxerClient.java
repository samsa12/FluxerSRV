package com.fluxer.srv;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.neovisionaries.ws.client.*;
import okhttp3.OkHttpClient;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class FluxerClient {
    private static final String GATEWAY_URL = "wss://gateway.fluxer.app/?v=1&encoding=json";
    private static final Gson GSON = new Gson();

    private final String token;
    private WebSocket ws;
    private final FluxerRestApi restApi;
    private int sequence = 0;
    private String sessionId;
    private Timer heartbeatTimer;
    private FluxerListener listener;
    private final Logger logger;

    public FluxerClient(String token, Logger logger, OkHttpClient httpClient) {
        this.token = token;
        this.logger = logger;
        this.restApi = new FluxerRestApi(token, httpClient);
    }

    public void setListener(FluxerListener listener) {
        this.listener = listener;
    }

    public void connect() throws Exception {
        ws = new WebSocketFactory()
                .setConnectionTimeout(5000)
                .createSocket(GATEWAY_URL)
                .addListener(new WebSocketAdapter() {
                    @Override
                    public void onTextMessage(WebSocket websocket, String text) throws Exception {
                        handleMessage(text);
                    }

                    @Override
                    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame,
                            WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
                        logger.warning("[Fluxer] Disconnected from gateway. Attempting to reconnect...");
                        stopHeartbeat();
                    }

                    @Override
                    public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
                        logger.severe("[Fluxer] Gateway error: " + cause.getMessage());
                    }
                })
                .connect();
    }

    private void handleMessage(String text) {
        JsonObject packet = GSON.fromJson(text, JsonObject.class);
        if (!packet.has("op"))
            return;

        int op = packet.get("op").getAsInt();

        if (packet.has("s") && !packet.get("s").isJsonNull()) {
            sequence = packet.get("s").getAsInt();
        }

        switch (op) {
            case 10: // Hello
                int heartbeatInterval = packet.getAsJsonObject("d").get("heartbeat_interval").getAsInt();
                startHeartbeat(heartbeatInterval);
                identify();
                break;
            case 0: // Dispatch
                String type = packet.get("t").getAsString();
                handleDispatch(type, packet.getAsJsonObject("d"));
                break;
            case 11: // Heartbeat ACK
                break;
        }
    }

    private void identify() {
        JsonObject payload = new JsonObject();
        payload.addProperty("op", 2);

        JsonObject d = new JsonObject();
        d.addProperty("token", token.replace("Bot ", ""));

        JsonObject properties = new JsonObject();
        properties.addProperty("os", "linux");
        properties.addProperty("browser", "fluxer-srv");
        properties.addProperty("device", "fluxer-srv");

        d.add("properties", properties);
        payload.add("d", d);

        ws.sendText(payload.toString());
    }

    private void startHeartbeat(int interval) {
        stopHeartbeat();
        heartbeatTimer = new Timer(true);
        heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                JsonObject heartbeat = new JsonObject();
                heartbeat.addProperty("op", 1);
                heartbeat.add("d", sequence == 0 ? null : GSON.toJsonTree(sequence));
                ws.sendText(heartbeat.toString());
            }
        }, interval, interval);
    }

    private void stopHeartbeat() {
        if (heartbeatTimer != null) {
            heartbeatTimer.cancel();
            heartbeatTimer = null;
        }
    }

    private void handleDispatch(String type, JsonObject data) {
        if (type.equals("READY")) {
            sessionId = data.get("session_id").getAsString();
            logger.info("[Fluxer] Logged in as " + data.getAsJsonObject("user").get("username").getAsString());
        } else if (type.equals("MESSAGE_CREATE")) {
            if (listener != null) {
                listener.onMessageCreate(data);
            }
        }
    }

    public FluxerRestApi getRestApi() {
        return restApi;
    }

    public void shutdown() {
        stopHeartbeat();
        if (ws != null) {
            ws.disconnect();
        }
    }
}
