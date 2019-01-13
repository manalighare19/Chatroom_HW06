package com.example.inclass06_r20;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
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

public class Threads extends AppCompatActivity {

    String useremail,user_fname,user_lname,user_id,token;
    TextView txtViewUserName;
    ImageView imgViewLogout,imageViewDeleteButton,imgViewAddNewThread;
    EditText editTextAddNewThread;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    ListView listView;

    ThreadListAdapter threadListAdapter;

    ArrayList<ThreadFromResponse> valuesOfthreadEnteredOnChildThread=new ArrayList<>();

    private final OkHttpClient client = new OkHttpClient();

    Gson gson=new Gson();
    ThreadFromResponse threadFromResponse;

    ThreadMessages messagesOfThreads=new ThreadMessages();
    ArrayList<ThreadMessages> threadMessagesArrayList=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_threads);

        setTitle(R.string.TitleOfThreadScreen);

        txtViewUserName=(TextView)findViewById(R.id.txtViewUserName);
        imgViewLogout=(ImageView)findViewById(R.id.imgViewLogoutOnThreadScreen);
        listView=(ListView)findViewById(R.id.listView);

        imgViewAddNewThread=(ImageView)findViewById(R.id.imgViewAddThread);
        imageViewDeleteButton=(ImageView)findViewById(R.id.imgViewDelete);

        editTextAddNewThread=(EditText)findViewById(R.id.editTextAddThread);

        sharedPreferences = getSharedPreferences("Chatroom", MODE_PRIVATE);
        editor=sharedPreferences.edit();

        token = sharedPreferences.getString("token", null);
        useremail=sharedPreferences.getString("user_email",null);
        user_fname=sharedPreferences.getString("user_fname",null);
        user_lname=sharedPreferences.getString("user_lname",null);
        user_id=sharedPreferences.getString("user_id",null);


        if(token!=null && useremail!=null) {
            txtViewUserName.setText(user_fname+" "+user_lname);
            getThreads(token);
            threadListAdapter=new ThreadListAdapter(this,R.layout.threadslayout,valuesOfthreadEnteredOnChildThread);
            listView.setAdapter(threadListAdapter);

        }
        imgViewAddNewThread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("thread", "onClick: add thread");
                if(editTextAddNewThread.length() != 0){
                    addNewThread(editTextAddNewThread.getText().toString(),token);
                    editTextAddNewThread.setText("");
                }else{
                    Toast.makeText(getApplicationContext(),"Enter Thread tiltle",Toast.LENGTH_SHORT).show();
                }

            }

        });


        imgViewLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.clear();
                editor.commit();
                Intent intent=new Intent(Threads.this,MainActivity.class);
                startActivity(intent);
            }
        });



       listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
               ThreadFromResponse itemname=threadListAdapter.getItem(i);
               Log.d("thread list", "onItemClick: "+itemname.title);
               getMessagesOnRowClick(token,i,itemname.title);

           }
       });



    }


    public void getThreads(final String token) {
        final Request request = new Request.Builder()
                .url("http://ec2-18-234-222-229.compute-1.amazonaws.com/api/thread")
                .addHeader("Authorization","BEARER"+" "+token)
                .get()
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String responsebody=response.body().string();
                Log.d("Thread get", "onResponse: "+responsebody);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            JSONObject root=new JSONObject(responsebody);
                            JSONArray threads=root.getJSONArray("threads");
                            for(int i=0;i<=threads.length()-1;i++) {
                                threadFromResponse = gson.fromJson(String.valueOf(threads.get(i)), ThreadFromResponse.class);
                                valuesOfthreadEnteredOnChildThread.add(threadFromResponse);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Collections.reverse(valuesOfthreadEnteredOnChildThread);
                        threadListAdapter.notifyDataSetChanged();
                        threadListAdapter.enableDeleteButtonOnThreadsScreen(user_id,token,getApplicationContext(),valuesOfthreadEnteredOnChildThread);

                    }
                });
            }
        });
    }



    public void addNewThread(String title, final String token) {
        Log.d("thread", "addNewThread: ");
        final RequestBody formBody = new FormBody.Builder()
                .add("title", title)
                .build();
        final Request request = new Request.Builder()
                .url("http://ec2-18-234-222-229.compute-1.amazonaws.com/api/thread/add")
                .addHeader("Authorization","BEARER"+" "+token)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if(response.isSuccessful()){
                    final String responseBody=response.body().string();
                    Log.d("thread add", "onResponse: "+responseBody);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject root=new JSONObject(responseBody);
                                JSONObject threadObject=root.getJSONObject("thread");
                                threadFromResponse = gson.fromJson(String.valueOf(threadObject), ThreadFromResponse.class);
                                valuesOfthreadEnteredOnChildThread.add(threadFromResponse);
                                threadListAdapter.notifyDataSetChanged();
                                threadListAdapter.enableDeleteButtonOnThreadsScreen(user_id,token,getApplicationContext(),valuesOfthreadEnteredOnChildThread);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

            }
        });
    }

    public void getMessagesOnRowClick(final String token, final int threadRow, final String title) {
        Log.d("thread", "getMessagesOnRowClick: ");
        final Request request = new Request.Builder()
                .url("http://ec2-18-234-222-229.compute-1.amazonaws.com/api/messages/"+valuesOfthreadEnteredOnChildThread.get(threadRow).id)
                .addHeader("Authorization","BEARER"+"  "+token)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String responseBody=response.body().string();
                Log.d("thread", "onResponse: message"+responseBody);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        JSONObject root= null;
                        try {
                            root = new JSONObject(responseBody);
                            JSONArray messages=root.getJSONArray("messages");
                            threadMessagesArrayList.clear();
                            for(int i=0;i<messages.length();i++)
                            {
                                messagesOfThreads=gson.fromJson(String.valueOf(messages.get(i)),ThreadMessages.class);
                                threadMessagesArrayList.add(messagesOfThreads);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        sharedPreferences = getSharedPreferences("Chatroom", MODE_PRIVATE);
                        editor = sharedPreferences.edit();
                        editor.putString("token", token);
                        editor.putString("user_id",user_id);
                        editor.commit();
                        Intent intent=new Intent(Threads.this,Messages.class);
                        intent.putExtra("messages",threadMessagesArrayList);
                        intent.putExtra("threadname",title);
                        intent.putExtra("threadID",valuesOfthreadEnteredOnChildThread.get(threadRow).id);
                        startActivity(intent);

                    }
                });

            }
        });
    }
}
