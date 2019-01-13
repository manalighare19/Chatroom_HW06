package com.example.inclass06_r20;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Signup extends AppCompatActivity {

    private final OkHttpClient client = new OkHttpClient();

     Gson gson=new Gson();

    EditText editTextFirstName,editTextLastName,editTextEmail,editTextChoosePassword,editTextRepeatPassword;
    Button btnSignUp,btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        editTextFirstName=(EditText)findViewById(R.id.editTextFirstName);
        editTextLastName=(EditText)findViewById(R.id.editTextLastName);
        editTextEmail=(EditText)findViewById(R.id.editTextEmail);
        editTextChoosePassword=(EditText)findViewById(R.id.editTextChoosePassword);
        editTextRepeatPassword=(EditText)findViewById(R.id.editTextRepeatPassword);
        btnCancel=(Button)findViewById(R.id.btnCancel);
        btnSignUp=(Button)findViewById(R.id.btnSignUpOnSignUpPage);

        Log.d("demo", " " + String.valueOf(Thread.currentThread().getId()));

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if(editTextFirstName.length()!=0 && editTextLastName.length()!=0 && editTextEmail.length()!=0 && editTextChoosePassword.length()!=0 && editTextRepeatPassword.length()!=0) {
                    if(editTextChoosePassword.getText().toString().equals(editTextRepeatPassword.getText().toString())) {
                        if(editTextChoosePassword.length() >5){
                            performSignUp(editTextEmail.getText().toString(),
                                    editTextRepeatPassword.getText().toString(),
                                    editTextFirstName.getText().toString(), editTextLastName.getText().toString());
                        }else {
                            Toast.makeText(getApplicationContext(),"Passwoord should be 6 0r more",Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Toast.makeText(getApplicationContext(),"Passwords not the same",Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(),"Fields not filled",Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void performSignUp(String email, String password,String fname,String lname) {
        final RequestBody formBody = new FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .add("fname",fname)
                .add("lname",lname)
                .build();
        final Request request = new Request.Builder()
                .url("http://ec2-18-234-222-229.compute-1.amazonaws.com/api/signup")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                Toast.makeText(getApplicationContext(),"error in api",Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        String  str = null;
                        try {
                            str = response.body().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.d("demo","  "+str);
                        UserSignUp userSignUp = gson.fromJson(str, UserSignUp.class);

                        if(userSignUp.getStatus().equals("ok")) {

                            SharedPreferences sharedPreferences = getSharedPreferences("Chatroom", MODE_PRIVATE);
                            SharedPreferences.Editor editor=sharedPreferences.edit();
                            editor.putString("token", userSignUp.getToken().toString());
                            editor.putString("user_email",userSignUp.getUser_email().toString());
                            editor.putString("user_fname",userSignUp.getUser_fname().toString());
                            editor.putString("user_lname",userSignUp.getUser_lname().toString());
                            editor.putString("user_id",userSignUp.getUser_id().toString());
                            editor.commit();

                            //Log.d("last name is : ",""+sharedPreferences.getString("user_lname","no String available"));

                            Toast.makeText(getApplicationContext(),"User created Successfully",Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent(Signup.this,MainActivity.class);
                            startActivity(intent);
                        }
                        else {
                            Toast.makeText(getApplicationContext(),"Please signup again",Toast.LENGTH_SHORT).show();
                        }


                    }
                });



            }
        });
    }
}
