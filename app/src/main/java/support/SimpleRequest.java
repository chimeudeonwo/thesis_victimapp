package support;

import android.app.Activity;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class SimpleRequest{
    private final Activity activity;
    public static final String HOST = "http://192.168.2.122:8080/api/v1";
    private final Display display;
    private String requestResponse; // variable to return string request response
    private JSONObject serverResponse; // variable to return JSONObject request response

    public SimpleRequest(Activity activity) {
        this.activity = activity;
        this.display = new Display(getActivity());
        serverResponse = new JSONObject();
    }

    /**
     * Makes a POST request to the server.
     *
     * @param data - the request body payload.
     * @param url - the server endpoint for the request.
     * @param authToken - client authentication token.
     */
    public String postRequest(JSONObject data, String url, String authToken) throws JSONException {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        // String url = "http://192.168.2.122:8080/api/v1/register/user";
        // String url = "http://10.0.2.2:8080/api/v1/register/user";

        final String requestBody = String.valueOf(data);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // display.displayDialogMsg(response, "SimpleRequestResponseValue");
                        requestResponse = response;
                        setRequestResponse(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                display.displayDialogMsg("Error sending request: "+data + error, "PostRequestError");
                Log.d("PostRequestError", "did not send request", error);
            }
        }) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                int mStatusCode = response.statusCode;

                if (mStatusCode == 200) {
                    System.out.println("Response is successful SuccessResponse");
                }

                return super.parseNetworkResponse(response);
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public byte[] getBody() throws AuthFailureError {
                return requestBody.getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("Authorization", "Bearer " + authToken);

                return params;
            }

            /*@Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String responseString = "";
                if (response != null) {
                    // responseString = Arrays.toString(response.data);
                    responseString = String.valueOf(response.statusCode);
                    serverResponse = new JSONObject();
                    try {
                        serverResponse.put("statusCode", response.statusCode);
                        serverResponse.put("headers", response.headers);
                        serverResponse.put("body", response.data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    // can get more details such as response.headers
                }
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }*/
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
        return requestResponse;
    }

    public interface VolleyResponseListener {
        public void onSuccess(String response) throws JSONException;

        public void onFailure(Exception e);
    }

    public void getRequest(String url, String authToken, VolleyResponseListener volleyResponseListener) throws JSONException {
        String[] userIdResponse = {""};
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // display.displayDialogMsg("UserId", response);
                        try {
                            volleyResponseListener.onSuccess(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // display.displayDialogMsg("GetUser Id failed!", "RequestError");
                Log.d("GetUserIdRequestError", "did not send request", error);
                volleyResponseListener.onFailure(error);
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("Authorization", "Bearer " + authToken);

                return params;
            }

        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
        // display.displayDialogMsg("Result: " + getRequestResponse(), "ReturnedValue");
    }

    public JSONObject postJsonRequest(JSONObject data, String url) throws JSONException {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        // Request a string response from the provided URL.
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, data,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // display.displayDialogMsg("response: "+response, "ResponseResult");
                            setServerResponse(response.getString(ParamsRef.USER_TOKEN), response.getLong(ParamsRef.USER_ID));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), "Authentication error: username/password incorrect " + error, Toast.LENGTH_SHORT).show();
                // display.displayDialogMsg("SimpleRequest didn't work!", "RequestError");
                Log.d("RegisterRequestError", "did not send request", error.getCause());
            }
        }) {
            /*@Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                int mStatusCode = response.statusCode;

                if(mStatusCode == 200){
                    System.out.println("Response is successful SuccessResponse");
                }

                return super.parseNetworkResponse(response);
            } */

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public byte[] getBody() {
                return data.toString().getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                // params.put("Authorization", "Bearer " + authToken);

                return params;
            }

            /*@Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String responseString = "";
                if (response != null) {
                    // responseString = Arrays.toString(response.data);
                    responseString = String.valueOf(response.statusCode);
                    serverResponse = new JSONObject();
                    try {
                        serverResponse.put("statusCode", response.statusCode);
                        serverResponse.put("headers", response.headers);
                        serverResponse.put("body", response.data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    // can get more details such as response.headers
                }
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }*/
        };

        // Add the request to the RequestQueue.
        queue.add(jsonRequest);
        return serverResponse;
    }

    public JSONObject getServerResponse() throws JSONException {
        return serverResponse;
    }

    public void setServerResponse(String jwtToken, long userId) throws JSONException {
        serverResponse = this.getServerResponse();
        serverResponse.put(ParamsRef.USER_TOKEN, jwtToken);
        serverResponse.put(ParamsRef.USER_ID, userId);
    }

    public Activity getActivity() {
        return activity;
    }

    public String getRequestResponse() {
        return this.requestResponse;
    }

    public void setRequestResponse(String response) {
        this.requestResponse = response;
    }

    @Override
    public String toString() {
        return "SimpleRequest{" +
                "activity=" + activity +
                ", display=" + display +
                ", requestResponse='" + requestResponse + '\'' +
                ", serverResponse=" + serverResponse +
                '}';
    }
}
