package ch.disappointment.WalkoutCompanion.api;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import ch.disappointment.WalkoutCompanion.BuildConfig;
import ch.disappointment.WalkoutCompanion.api.exception.ApiException;
import ch.disappointment.WalkoutCompanion.api.model.CredentialsDto;
import ch.disappointment.WalkoutCompanion.api.model.DailyGoal;
import ch.disappointment.WalkoutCompanion.api.model.DailyStepsListDto;
import ch.disappointment.WalkoutCompanion.api.model.TokenDto;
import ch.disappointment.WalkoutCompanion.api.requests.AuthenticatedJsonRequest;
import ch.disappointment.WalkoutCompanion.api.requests.AuthenticatedStringRequest;
import ch.disappointment.WalkoutCompanion.persistence.model.DailySteps;
import ch.disappointment.WalkoutCompanion.persistence.model.User;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.function.Consumer;

/**
 * This class is used to communicate with the Walkout REST API.
 * It uses Volley to manage http requests and avoid setting up request queues manually.
 */
public class ApiService {
    private static final String baseUrl = BuildConfig.API_URL;
    private static ApiService instance;

    private final Gson gson = new Gson();
    private final RequestQueue queue;

    private boolean isLocal = false;
    private boolean logged = false;
    private User loggedUser;
    private int currentGoal;

    private ApiService(Context ctx) {
        queue = Volley.newRequestQueue(ctx);
    }

    public static ApiService getInstance(Context ctx) {
        if (instance == null) {
            instance = new ApiService(ctx);
        }

        return instance;
    }

    private void forwardError(VolleyError error, Consumer<ApiException> onError) {
        int status = error.networkResponse != null ? error.networkResponse.statusCode : 0;
        if (error.getMessage() != null)
            onError.accept(new ApiException(error.getMessage(), status, error));
        else
            onError.accept(new ApiException(error.toString(), status, error));
    }

    private void loginOrRegister(CredentialsDto credentials, String path, Consumer<TokenDto> onLogged, Consumer<ApiException> onError) {
        try {
            // Build the body
            JSONObject jsonObject = new JSONObject(gson.toJson(credentials));

            // Build the request
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, baseUrl + path, jsonObject, response -> {
                // parse response body
                TokenDto tokenDto = gson.fromJson(response.toString(), TokenDto.class);
                // save access token
                this.logged = true;
                this.setLocal(false);
                loggedUser = new User(credentials.getUsername(), tokenDto.getAccessToken());

                // execute callback
                onLogged.accept(tokenDto);
            }, error -> {
                // execute error callback
                this.forwardError(error, onError);
            });

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
        AuthenticatedStringRequest r = new AuthenticatedStringRequest(Request.Method.GET, baseUrl + "helloUser", loggedUser.getToken(), onGreet::accept, error -> {
            this.forwardError(error, onError);
        });

        queue.add(r);
    }

    public User getLoggedUser() {
        return loggedUser;
    }

    public void logout() {
        logged = false;
        isLocal = false;
        loggedUser = null;
    }

    public boolean isLogged() {
        return logged;
    }

    public String getAccessToken() {
        return loggedUser.getToken();
    }

    public void setAccessToken(String accessToken) {
        this.loggedUser.setToken(accessToken);
    }

    public boolean isLocal() {
        return isLocal;
    }

    public void setLocal(boolean local) {
        isLocal = local;
    }

    public void setUser(User user) {
        loggedUser = user;
    }

    public void getDailySteps(String date, Consumer<DailySteps> onSuccess, Consumer<ApiException> onError) {
        AuthenticatedJsonRequest request = new AuthenticatedJsonRequest(Request.Method.GET, baseUrl + "daily-steps/" + date, loggedUser.getToken(), null, response -> {
            DailySteps steps = gson.fromJson(response.toString(), DailySteps.class);
            onSuccess.accept(steps);
        }, error -> this.forwardError(error, onError));

        queue.add(request);
    }

    public void getAllSteps(Consumer<DailySteps[]> onSuccess, Consumer<ApiException> onError) {
        AuthenticatedJsonRequest request = new AuthenticatedJsonRequest(Request.Method.GET, baseUrl + "daily-steps", loggedUser.getToken(), null, response -> {
            DailyStepsListDto steps = gson.fromJson(response.toString(), DailyStepsListDto.class);
            onSuccess.accept(steps.getItems());
        }, error -> this.forwardError(error, onError));

        queue.add(request);
    }

    public void getAllStepsExceptUser(Consumer<DailySteps[]> onSuccess, Consumer<ApiException> onError) {
        AuthenticatedJsonRequest request = new AuthenticatedJsonRequest(Request.Method.GET, baseUrl + "daily-steps/except-user", loggedUser.getToken(), null, response -> {
            DailyStepsListDto steps = gson.fromJson(response.toString(), DailyStepsListDto.class);
            onSuccess.accept(steps.getItems());
        }, error -> this.forwardError(error, onError));

        queue.add(request);
    }

    public void setDailySteps(DailySteps steps, Consumer<DailySteps> onSuccess, Consumer<ApiException> onError) throws JSONException {
        JSONObject body = new JSONObject(gson.toJson(steps));
        AuthenticatedJsonRequest request = new AuthenticatedJsonRequest(Request.Method.POST, baseUrl + "daily-steps", loggedUser.getToken(), body, response -> {
            DailySteps saved = gson.fromJson(response.toString(), DailySteps.class);
            onSuccess.accept(saved);
        }, error -> this.forwardError(error, onError));

        queue.add(request);
    }

    public void deleteDailySteps(String date, Runnable onSuccess, Consumer<ApiException> onError) {
        AuthenticatedJsonRequest request = new AuthenticatedJsonRequest(Request.Method.DELETE, baseUrl + "daily-steps/" + date, loggedUser.getToken(), null, response -> {
            onSuccess.run();
        }, error -> this.forwardError(error, onError));

        queue.add(request);
    }

    public void getDailyGoal(Consumer<Integer> onSuccess, Consumer<ApiException> onError) {
        AuthenticatedJsonRequest request = new AuthenticatedJsonRequest(Request.Method.GET, baseUrl + "goal", loggedUser.getToken(), null, response -> {
            DailyGoal saved = gson.fromJson(response.toString(), DailyGoal.class);
            onSuccess.accept(saved.getGoal());
        }, error -> this.forwardError(error, onError));

        queue.add(request);
    }

    public void setDailyGoal(Integer dailyGoal, Consumer<Integer> onSuccess, Consumer<ApiException> onError) {
        AuthenticatedStringRequest request = new AuthenticatedStringRequest(Request.Method.POST,
                baseUrl + "goal?goal=" + dailyGoal, loggedUser.getToken(), (response -> {
        }), null);

        queue.add(request);
    }

    public int getCurrentGoal() {
        return currentGoal;
    }

    public void setCurrentGoal(int currentGoal) {
        this.currentGoal = currentGoal;
    }
}
