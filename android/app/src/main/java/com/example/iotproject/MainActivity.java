package com.example.iotproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

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
                        break;
                    }
                    try {
                        Thread.sleep(1000);
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

                                String res = jsonResponse.getString("where");
                                JSONArray jsonObj = new JSONArray(res);
                                for(int i=0;i<jsonObj.length();i++) {
                                    JSONObject Obj = (JSONObject)jsonObj.get(i);
                                    Log.d("test8",Integer.toString(i));
                                    Log.d("test8", Integer.toString((int)Obj.get("onoff")));
                                    if ((int)Obj.get("onoff") == 1) {
                                        X = (double) Obj.get("x");
                                        Y = (double) Obj.get("y");
                                        idx = (int)Obj.get("index");
                                        mHandler.sendEmptyMessage(0);
                                        t.interrupt();
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        //서버로 데이터 전달 및 응답 받기에 실패한 경우 아래 코드가 실행됩니다.
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                        }
                    });
                    jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    requestQueue.add(jsonObjectRequest);
                }
            }/*catch(InterruptedException e){
                e.printStackTrace();
               //Thread.currentThread().interrupt();
            }*/
        }
    //}

    String url="https://avgnoqx20f.execute-api.ap-northeast-2.amazonaws.com/default/IoT_Project";
    double X;
    double Y;
    int idx;
    ImageButton imgbtn;
    private static Handler mHandler ;
    Thread t;
    NewRunnable nr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgbtn = (ImageButton)findViewById(R.id.emgbtn);
        imgbtn.setVisibility(View.INVISIBLE);
        imgbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("test8123", Double.toString(X));
                Log.d("test8123", Double.toString(Y));
                Intent intent =new Intent(getApplicationContext(),MapActivity.class);
                intent.putExtra("X",X);
                intent.putExtra("Y",Y);
                intent.putExtra("index",idx);
                startActivity(intent);
                finish();
            }
        });
        nr = new NewRunnable() ;
        t = new Thread(nr) ;
        t.start() ;

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                imgbtn.setVisibility(View.VISIBLE);
                t.interrupt();
            }
        };
    }
}
