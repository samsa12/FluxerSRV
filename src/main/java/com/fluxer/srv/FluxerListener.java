package com.fluxer.srv;

import com.google.gson.JsonObject;

public interface FluxerListener {
    void onMessageCreate(JsonObject message);
}
