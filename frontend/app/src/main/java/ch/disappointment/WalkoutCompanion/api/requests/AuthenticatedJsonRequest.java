package ch.disappointment.WalkoutCompanion.api.requests;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AuthenticatedJsonRequest extends JsonObjectRequest {
    String token;

    public AuthenticatedJsonRequest(String url, String tk, Response.Listener<JSONObject> listener, @Nullable Response.ErrorListener errorListener) {
        super(url, listener, errorListener);
        this.token = tk;
    }

    public AuthenticatedJsonRequest(int method, String url, String tk, @Nullable JSONObject jsonRequest, Response.Listener<JSONObject> listener, @Nullable Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
        this.token = tk;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>(super.getHeaders());
        headers.put("Authorization", "Bearer " + token);
        return headers;
    }
}
