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

import org.ocpsoft.prettytime.PrettyTime;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MessagesAdapter extends ArrayAdapter<ThreadMessages> {

    String token,userID;
    Gson gson=new Gson();
    DeleteMessage deleteMessage;
    Messages messages=new Messages();
    ArrayList<ThreadMessages> arrayList=new ArrayList<>();

    Context context;
    private final OkHttpClient client = new OkHttpClient();

    public MessagesAdapter(@NonNull Context context, int resource, @NonNull List<ThreadMessages> objects) {
        super(context, resource, objects);
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            final ThreadMessages threadMessages = getItem(position);
            MessagesAdapter.ViewHolder viewHolder;

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.messageslayout, parent, false);
                viewHolder = new MessagesAdapter.ViewHolder();
                viewHolder.txtViewMessageDescription = convertView.findViewById(R.id.txtViewMessageDescription);
                viewHolder.messagesPostedBy = convertView.findViewById(R.id.txtViewMessagePostedBy);
                viewHolder.messageDate = convertView.findViewById(R.id.txtViewMessagePostedDate);
                viewHolder.imgMessageDelete = convertView.findViewById(R.id.imgViewDeletemess);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (MessagesAdapter.ViewHolder) convertView.getTag();

            }
            viewHolder.txtViewMessageDescription.setText(threadMessages.message);
            viewHolder.messagesPostedBy.setText(threadMessages.user_fname + " " + threadMessages.user_lname);

            if (userID != null) {

                if (threadMessages.user_id.equals(userID)) {
                    viewHolder.imgMessageDelete.setVisibility(View.VISIBLE);

                } else {
                    viewHolder.imgMessageDelete.setVisibility(View.INVISIBLE);
                }
            }

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            try {
                Date date = simpleDateFormat.parse(threadMessages.created_at);


                PrettyTime prettyTime = new PrettyTime();
                viewHolder.messageDate.setText(prettyTime.format(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }


            viewHolder.imgMessageDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    final Request request = new Request.Builder()
                            .url("http://ec2-18-234-222-229.compute-1.amazonaws.com/api/message/delete/" + threadMessages.getId())
                            .addHeader("Authorization", "BEARER" + " " + token)
                            .get()
                            .build();

                    Log.d("Delete", "MessageID" + threadMessages.getId());
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {

                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {

                            final String responseBody = response.body().string();
                            Log.d("messadpt", "onResponse: delete message"+responseBody);
                            messages.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    deleteMessage = gson.fromJson(responseBody, DeleteMessage.class);
                                    if (deleteMessage.getStatus().equals("ok")) {
                                        Toast.makeText(context, "Message Deleted", Toast.LENGTH_SHORT).show();
                                        arrayList.remove(getPosition(threadMessages));
                                        notifyDataSetChanged();
                                    }
                                }
                            });


                        }
                    });

                }
            });

        return convertView;
    }


    public void enabledeleteButtonValue(String userID, String token, Context context,ArrayList<ThreadMessages> messagesArrayList) {
        this.userID=userID;
        this.token=token;
        this.context=context;
        this.arrayList=messagesArrayList;

    }

    public static class ViewHolder {
        TextView txtViewMessageDescription;
        TextView messagesPostedBy;
        TextView messageDate;
        ImageView imgMessageDelete;
    }
}
