package com.example.stepapp.api;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.stepapp.BuildConfig;
import com.example.stepapp.api.exception.ApiException;
import com.example.stepapp.api.model.CredentialsDto;
import com.example.stepapp.api.model.TokenDto;
import com.example.stepapp.api.requests.AuthenticatedJsonRequest;
import com.example.stepapp.api.requests.AuthenticatedStringRequest;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.function.Consumer;

public class ApiService {
    private static final String baseUrl = BuildConfig.API_URL;
    private static ApiService instance;

    private final Gson gson = new Gson();
    private final RequestQueue queue;
    private boolean logged = false;
    private String accessToken;

    private ApiService(Context ctx) {
        queue = Volley.newRequestQueue(ctx);
    }

    public static ApiService getInstance(Context ctx) {
        if (instance == null) {
            instance = new ApiService(ctx);
        }

        return instance;
    }

    private void loginOrRegister(CredentialsDto credentials, String path, Consumer<TokenDto> onLogged, Consumer<ApiException> onError) {
        try {
            // Build the body
            JSONObject jsonObject = new JSONObject(gson.toJson(credentials));

            // Build the request
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, baseUrl + path,
                    jsonObject,
                    response -> {
                        // parse response body
                        TokenDto tokenDto =
                                gson.fromJson(response.toString(), TokenDto.class);
                        // save access token
                        this.accessToken = tokenDto.getAccessToken();
                        this.logged = true;

                        // execute callback
                        onLogged.accept(tokenDto);
                    },
                    error -> {
                        // execute error callback
                        onError.accept(new ApiException(
                                error.getMessage(),
                                error.networkResponse != null ? error.networkResponse.statusCode : null,
                                error));
                    }
            );

            // Send it
            queue.add(req);
        } catch (JSONException ex) {
            ex.printStackTrace();
            throw new ApiException("Error de-serializing body", 0, ex);
        }
    }

    public void login(CredentialsDto credentials, Consumer<TokenDto> onLogged, Consumer<ApiException> onError) {
        loginOrRegister(credentials, "login", onLogged, onError);
    }

    public void register(CredentialsDto credentials, Consumer<TokenDto> onLogged, Consumer<ApiException> onError) {
        loginOrRegister(credentials, "register", onLogged, onError);
    }

    public void getGreet(Consumer<String> onGreet, Consumer<ApiException> onError) {
        AuthenticatedStringRequest r = new AuthenticatedStringRequest(
                Request.Method.GET,
                baseUrl + "helloUser",
                accessToken,
                onGreet::accept,
                error -> {
                    onError.accept(new ApiException(error.getMessage(),
                            error.networkResponse != null ? error.networkResponse.statusCode : 0,
                            error));
                });

        queue.add(r);
    }

    public boolean isLogged() {
        return logged;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
