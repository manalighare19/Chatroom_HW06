package com.example.inclass06_r20;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class ThreadListAdapter extends ArrayAdapter<ThreadFromResponse> {

    String token,userID;
    Context context;
    Threads threadsactivity=new Threads();
    DeleteThread deleteThread;
    Gson gson=new Gson();
    ArrayList<ThreadFromResponse> threadarrayList=new ArrayList<>();
    private final OkHttpClient client = new OkHttpClient();


    public ThreadListAdapter(@NonNull Context context, int resource, @NonNull List<ThreadFromResponse> objects) {
        super(context, resource, objects);

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ThreadFromResponse threadName=getItem(position);
        ViewHolder viewHolder;

        if(convertView==null) {
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.threadslayout,parent,false);
            viewHolder=new ViewHolder();
            viewHolder.txtViewThreadName=convertView.findViewById(R.id.txtViewThreadName);
            viewHolder.imgViewDelete=convertView.findViewById(R.id.imgViewDelete);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder=(ViewHolder)convertView.getTag();
        }
        viewHolder.txtViewThreadName.setText(threadName.title);
        if(threadName.user_id.equals(userID)) {
            viewHolder.imgViewDelete.setVisibility(View.VISIBLE);
        }
        else {
            viewHolder.imgViewDelete.setVisibility(View.INVISIBLE);
        }
        viewHolder.imgViewDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                deleteThread(token,threadName.id,threadName);
            }
        });

        return convertView;

    }


    public void enableDeleteButtonOnThreadsScreen(String userID,String token,Context context,ArrayList<ThreadFromResponse> threadarrayList) {
        this.userID=userID;
        this.token=token;
        this.context=context;
        this.threadarrayList=threadarrayList;
    }

    public static class ViewHolder {
        TextView txtViewThreadName;
        ImageView imgViewDelete;
    }

    public void deleteThread(String token,String threadID,final ThreadFromResponse threadName) {

        final Request request = new Request.Builder()
                .url("http://ec2-18-234-222-229.compute-1.amazonaws.com/api/thread/delete/" +threadID)
                .addHeader("Authorization", "BEARER" + " " + token)
                .get()
                .build();

       client.newCall(request).enqueue(new Callback() {
           @Override
           public void onFailure(Call call, IOException e) {

           }

           @Override
           public void onResponse(Call call, Response response) throws IOException {

               final String responseBody=response.body().string();
               Log.d("threadadpt", "onResponse: delete thread:"+responseBody);
               threadsactivity.runOnUiThread(new Runnable() {
                   @Override
                   public void run() {

                       deleteThread = gson.fromJson(responseBody, DeleteThread.class);

                       if (deleteThread.getStatus().equals("ok")) {
                           Toast.makeText(context, "Deleted Thread :"+threadName.title, Toast.LENGTH_SHORT).show();
                           threadarrayList.remove(getPosition(threadName));
                           notifyDataSetChanged();
                       }

                   }
               });

           }
       });

    }
}
