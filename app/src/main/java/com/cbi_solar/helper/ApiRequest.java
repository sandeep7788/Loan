package com.cbi_solar.helper;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.cbi_solar.cbisolar.SplashScreen;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ApiRequest {

    public static JSONObject parseStringToJson(String response) {
        try {
            if (response == null || response.isEmpty()) {
                throw new Exception("Empty or null response");
            }

            // Convert to UTF-8 (fix encoding issues)
            String cleanResponse = new String(response.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8).trim();

            // Remove all non-JSON characters (symbols, spaces, unwanted tags)
            cleanResponse = cleanResponse.replaceAll("[^\\x20-\\x7E]", ""); // Remove non-printable ASCII characters

            // Ensure response is a valid JSON by trimming any leading or trailing non-JSON parts
            cleanResponse = cleanResponse.replaceAll("^[^\\{]+", "").replaceAll("[^\\}]+$", "");

            return new JSONObject(cleanResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Return null if parsing fails
        }
    }

    public static void verifierLogin(Context context, String a, String b, String deviceID, AlertDialog progressDialog) {
        String url = "https://vidhyalaya.co.in/MIS/API/Restapi/VerifierLogin?mobno="+a+"&password="+b+"&deviceID="+deviceID;

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        Log.e("@@TAG", "verifierLogin: "+url );

//        https://vidhyalaya.co.in/MIS/API/Restapi/VerifierLogin?mobno=1234&password=1234&deviceID=id

        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response1) {
                        progressDialog.dismiss();
                        try {

                            Log.e("@@TAG", "onResponse: "+response1 );

                            JSONObject response= parseStringToJson(response1);
                            Toast.makeText(context, response.getString("msg"), Toast.LENGTH_LONG).show();

                            if (response.getBoolean("status")) {
                                JSONObject responseBody = response.getJSONObject("responseBody");

                                MyApplication.writeStringPreference(ApiContants.id, responseBody.getString("id"));
                                MyApplication.writeStringPreference(ApiContants.PREF_F_name, responseBody.getString("verifier_name"));
                                MyApplication.writeStringPreference(ApiContants.login, "true");

                                context.startActivity(new Intent(context, SplashScreen.class));
                            } else {


//                                Toast.makeText(context, "Bad Response!", Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Log.e("@@TAG", "JSON Parsing Error: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Log.e("API_ERROR", error.toString()); // Log error
                        Toast.makeText(context, "Error: " + error.toString(), Toast.LENGTH_LONG).show();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=UTF-8"); // JSON format
                headers.put("User-Agent", "PostmanRuntime/7.29.4"); // Prevent blocks
                headers.put("Authorization", "Bearer YOUR_ACCESS_TOKEN"); // If API needs authentication
                headers.put("Accept-Encoding", "gzip, deflate, br"); // Maintain session
                headers.put("Cache-Control", "no-cache"); // Prevent caching
                return headers;
            }
        };

//        MyApplication.getInstance().addToRequestQueue(stringRequest);
        requestQueue.add(stringRequest);

    }
}
