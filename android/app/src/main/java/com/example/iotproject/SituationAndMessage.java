package com.example.iotproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashSet;
import java.util.Set;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

public class SituationAndMessage extends AppCompatActivity {
    class NewRunnable implements Runnable {
        @Override
        public void run() {
            /*try {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }*/
                while (!Thread.currentThread().isInterrupted()) {
                    if (Thread.interrupted()) {
                        //throw new InterruptedException();
                        Log.d("test9898","hihihihi");
                        break;
                    }
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    final RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                    final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                        //데이터 전달을 끝내고 이제 그 응답을 받을 차례입니다.
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONObject jsonResponse = new JSONObject(response.toString());
                                String res = jsonResponse.getString("body");
                                JSONObject jsonObj = new JSONObject(res);
                                Log.d("test0101",jsonObj.toString());

                                int gyro = (int) jsonObj.get("gyro");
                                int force = (int) jsonObj.get("force");
                                if (gyro > 70 && force > 900) {
                                    mHandler.sendEmptyMessage(CPRON);
                                } else {
                                    mHandler.sendEmptyMessage(CPROFF);
                                }
                            } catch (Exception e) {
                                Log.d("test", "error");
                                e.printStackTrace();
                            }
                        }
                        //서버로 데이터 전달 및 응답 받기에 실패한 경우 아래 코드가 실행됩니다.
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("test", error.toString());
                            error.printStackTrace();
                        }
                    });
                    jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    requestQueue.add(jsonObjectRequest);
                }
            /*}catch(InterruptedException e){
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }*/
        }
    }
    String url="https://52mpnxgee9.execute-api.us-east-2.amazonaws.com/default/iot_project";
    ImageView cprstat;
    Button sendbtn;
    ImageButton endbtn;
    private static Handler mHandler ;
    private static final int CPRON=0;
    private static final int CPROFF=1;
    Thread t;
    NewRunnable nr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_situation_and_message);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == CPRON){
                    cprstat=(ImageView)findViewById(R.id.cprstat);
                    cprstat.setImageResource(R.drawable.cpron);
                }
                else{
                    cprstat=(ImageView)findViewById(R.id.cprstat);
                    cprstat.setImageResource(R.drawable.cproff);
                }
            }
        };

        nr = new NewRunnable() ;
        t = new Thread(nr) ;
        t.start() ;

        sendbtn=(Button)findViewById(R.id.sendbtn);
        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText emgmessage=(EditText)findViewById(R.id.emgmessage);
                String emg = emgmessage.getText().toString();

                sendEmgMsg(emg);
                emgmessage.setText(null);
            }
        });
        endbtn = (ImageButton)findViewById(R.id.endbtn);
        endbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                t.interrupt();
                sendEndSig();
            }
        });
    }

    public void sendEmgMsg(String emg){
        JSONObject msg = new JSONObject();
        try{
            msg.put("emergency_msg", URLEncoder.encode(emg,"UTF-8"));
        }catch (JSONException e) {
            e.printStackTrace();
            Log.d("test", "json object error");
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
            Log.d("test", "json object error");
        }
        final RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, msg, new Response.Listener<JSONObject>() {
            //데이터 전달을 끝내고 이제 그 응답을 받을 차례입니다.
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String res = jsonResponse.getString("body");

                } catch (Exception e) {
                    Log.d("test", "error");
                    e.printStackTrace();
                }
            }
            //서버로 데이터 전달 및 응답 받기에 실패한 경우 아래 코드가 실행됩니다.
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("test", error.toString());
                error.printStackTrace();
            }
        });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(jsonObjectRequest);
    }
    public void sendEndSig(){
        String webserver="http://192.168.31.198:3000/api/current/getDone/";
        final RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, webserver, null, new Response.Listener<JSONObject>() {
            //데이터 전달을 끝내고 이제 그 응답을 받을 차례입니다.
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response.toString());

                } catch (Exception e) {
                    Log.d("test", "error");
                    e.printStackTrace();
                }
            }
            //서버로 데이터 전달 및 응답 받기에 실패한 경우 아래 코드가 실행됩니다.
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("test", error.toString());
                error.printStackTrace();
            }
        });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(jsonObjectRequest);
        t.interrupt();

        Intent intent=new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
}
