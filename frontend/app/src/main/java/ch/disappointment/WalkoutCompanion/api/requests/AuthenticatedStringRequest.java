package ch.disappointment.WalkoutCompanion.api.requests;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class AuthenticatedStringRequest extends StringRequest {
    String token;

    public AuthenticatedStringRequest(int method, String url, String tk, Response.Listener<String> listener, @Nullable Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
        this.token = tk;
    }

    public AuthenticatedStringRequest(String url, String tk, Response.Listener<String> listener, @Nullable Response.ErrorListener errorListener) {
        super(url, listener, errorListener);
        this.token = tk;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>(super.getHeaders());
        headers.put("Authorization", "Bearer " + token);
        return headers;
    }


}
