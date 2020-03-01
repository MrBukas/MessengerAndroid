package com.bukas.messenger;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class Registration extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button button_register = findViewById(R.id.button_register);
        final EditText edittext_username = findViewById(R.id.editText_username_reg);
        final EditText edittext_pass = findViewById(R.id.editText_password_reg);

        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    class AsyncRegister extends AsyncTask<Void, Void, Void> implements Serializable
    {
        String username;
        String password;

        public AsyncRegister(String username, String password) {
            this.username = username;
            this.password = password;
        }

        Handler toastHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                // This is where you do your work in the UI thread.
                // Your worker tells you in the message what to do.
                Toast.makeText(getApplicationContext(),message.obj.toString(),Toast.LENGTH_SHORT).show();
            }
        };
        String hashPassword(String password){
            MessageDigest md = null;
            try {
                md = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            // Change this to UTF-16 if needed
            md.update(password.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();

            String hex = String.format("%064x", new BigInteger(1, digest));
            return hex;
        }
        public void saveData(String username, String password){
            SharedPreferences values = getApplicationContext()
                    .getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = values.edit();
            editor.putString("Username", username);
            editor.putString("Password", password);
            editor.commit();
        }


        @Override
        protected Void doInBackground(Void... voids)  {
            try {
                Socket socket = new Socket(getResources().getString(R.string.server_ip),5679);
                PrintWriter printWriter = new PrintWriter(socket.getOutputStream(),true);
                InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                Scanner scanner = new Scanner(bufferedReader);
                printWriter.println("reg");
                printWriter.println(username);
                printWriter.println(hashPassword(password));
                int reg = Integer.parseInt(scanner.nextLine());
                switch (reg){
                    case 0:
                        toastHandler.obtainMessage(1,"Registered").sendToTarget();
                        saveData(username,password);
                        Intent intent = new Intent(Registration.this, FrontPage.class);
                        intent.putExtra("username",username);
                        intent.putExtra("password",hashPassword(password));
                        startActivity(intent);
                        break;
                    case 1:
                        toastHandler.obtainMessage(1,"This username is taken").sendToTarget();
                        break;
                    case -1:
                        toastHandler.obtainMessage(1,"Error").sendToTarget();
                        break;
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


}
