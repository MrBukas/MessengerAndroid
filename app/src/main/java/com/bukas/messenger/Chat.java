package com.bukas.messenger;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Chat extends AppCompatActivity {
    private static Socket socket;
    static String[] messages;
    static String talkerName;
    static ArrayAdapter<String> arrayAdapter;
    static ListView listViewMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent i = getIntent();
        final String username = i.getStringExtra("username");
        final String password = i.getStringExtra("password");
        talkerName = i.getStringExtra("talkerName");
        new AsyncCaller(username,password).execute();

        listViewMessage = findViewById(R.id.listViewMessage);

    }
    class AsyncCaller extends AsyncTask<Void, Void, Void>
    {
        String username;
        String password;



        public AsyncCaller(String username, String password) {
            this.username = username;
            this.password = password;
            //makeToast(username+password);
        }
        void makeToast(String s){
            Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
        }
        Handler toastHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                // This is where you do your work in the UI thread.
                // Your worker tells you in the message what to do.
                Toast.makeText(getApplicationContext(),message.obj.toString(),Toast.LENGTH_SHORT).show();
            }
        };

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
                socket = new Socket("176.214.187.245",5679);
                PrintWriter printWriter = new PrintWriter(socket.getOutputStream(),true);
                InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                Scanner scanner = new Scanner(bufferedReader);
                printWriter.println(username);
                printWriter.println((password));
                int auth = Integer.parseInt(scanner.nextLine());//Сообщает о результате авторизации
                printWriter.println("dialog");
                printWriter.println(talkerName);
                List<String> messagesList = new ArrayList<>();
                String msg;
                do {
                    msg = scanner.nextLine();
                    messagesList.add(msg);
                }while (!msg.equals("endl"));
                messages = new String[messagesList.size()-1];
                for (int i = 0; i < messages.length; i++) {
                    messages[i] = messagesList.get(i);
                }
                arrayAdapter = new ArrayAdapter<String>(Chat.this,android.R.layout.simple_list_item_1,messages);
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        listViewMessage.setAdapter(arrayAdapter);

                    }
                });


            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
