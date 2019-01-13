package com.example.inclass06_r20;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Messages extends AppCompatActivity {



    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String token,threadID,threadTitle,userID;
    TextView threadname;
    ArrayList<ThreadMessages> messageslist=new ArrayList<>();
    ListView listViewmessage;
    ImageView imgViewLogoutOnMessagesscreen,btnSend;
    MessagesAdapter messagesAdapter;
    EditText editTextAddMessageToThread;
    ThreadMessages threadMessages;
    Gson gson=new Gson();
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        listViewmessage=(ListView)findViewById(R.id.listViewMessages);
        btnSend=(ImageView) findViewById(R.id.btnSendMessage);
        editTextAddMessageToThread=(EditText)findViewById(R.id.editTextAddMessageToThread);
        sharedPreferences = getSharedPreferences("Chatroom", MODE_PRIVATE);
        editor=sharedPreferences.edit();

        threadname=(TextView)findViewById(R.id.txtViewThreadTitle);
        imgViewLogoutOnMessagesscreen=(ImageView)findViewById(R.id.imageView);
        token = sharedPreferences.getString("token", null);
        userID=sharedPreferences.getString("user_id",null);


        if(getIntent()!=null && getIntent().getExtras()!=null) {
            messageslist.clear();
            messageslist = (ArrayList<ThreadMessages>) getIntent().getExtras().getSerializable("messages");
            Collections.reverse(messageslist);
            threadTitle=getIntent().getExtras().getString("threadname");
            threadID=getIntent().getExtras().getString("threadID");
        }
        imgViewLogoutOnMessagesscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        threadname.setText(threadTitle);
        refreshListView();
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("message", "onClick: send btn");
                if(editTextAddMessageToThread.length() !=0){
                    sendMessage(editTextAddMessageToThread.getText().toString(),threadID,token);
                    editTextAddMessageToThread.setText("");
                }else{
                    Toast.makeText(getApplicationContext(),"Enter message",Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    public void sendMessage(String message, String threadID, final String token) {
        final RequestBody formBody = new FormBody.Builder()
                .add("message",message)
                .add("thread_id",threadID)
                .build();
        final Request request = new Request.Builder()
                .url("http://ec2-18-234-222-229.compute-1.amazonaws.com/api/message/add")
                .addHeader("Authorization","BEARER"+"  "+token)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseBody=response.body().string();
                Log.d("message add", "onResponse: "+responseBody);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            JSONObject root=new JSONObject(responseBody);
                            JSONObject messageObject=root.getJSONObject("message");
                            threadMessages = gson.fromJson(String.valueOf(messageObject), ThreadMessages.class);
                            messageslist.add(threadMessages);
                            messagesAdapter.notifyDataSetChanged();
                            messagesAdapter.enabledeleteButtonValue(userID,token,getApplicationContext(),messageslist);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        });

    }
    public void refreshListView() {
        messagesAdapter=new MessagesAdapter(this,R.layout.messageslayout,messageslist);
        messagesAdapter.enabledeleteButtonValue(userID,token,getApplicationContext(),messageslist);
        listViewmessage.setAdapter(messagesAdapter);
        messagesAdapter.notifyDataSetChanged();
    }


}
