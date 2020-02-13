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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FrontPage extends AppCompatActivity {
    private static Socket socket;
    static String[] usernames;
    static ListView listView;
    static ArrayAdapter<String> arrayAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_front_page);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent i = getIntent();
        final String username = i.getStringExtra("username");
        final String password = i.getStringExtra("password");
        new AsyncCaller(username,password).execute();

        usernames = new String[]{"asd","asdd"};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,usernames);
        listView = findViewById(R.id.listView);
        //listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(FrontPage.this,Chat.class);
                intent.putExtra("username",username);
                intent.putExtra("password",password);
                intent.putExtra("talkerName",usernames[i]);
                startActivity(intent);
            }
        });





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
                toastHandler.obtainMessage(1,"Start");
                socket = new Socket("176.214.187.245",5679);
                PrintWriter printWriter = new PrintWriter(socket.getOutputStream(),true);
                InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                Scanner scanner = new Scanner(bufferedReader);
                printWriter.println(username);
                printWriter.println((password));
                int auth = Integer.parseInt(scanner.nextLine());//Сообщает о результате авторизации
                printWriter.println("talkedUsers");
                int amountOfUsers = Integer.parseInt(scanner.nextLine());
                List<Talker> talkers = new ArrayList<>();
                for (int i = 0; i < amountOfUsers; i++) {
                    talkers.add(new Talker(Integer.parseInt(scanner.nextLine()),scanner.nextLine()));
                }
                System.out.println(talkers);
                usernames = new String[talkers.size()];
                for (int i = 0; i <talkers.size() ; i++) {
                    usernames[i] = talkers.get(i).username;
                }
                arrayAdapter = new ArrayAdapter<String>(FrontPage.this,android.R.layout.simple_list_item_1,usernames);
                //listView = findViewById(R.id.listView);

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        listView.setAdapter(arrayAdapter);

                    }
                });







            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    class Talker {
        int id;
        String username;

        public Talker(int id, String username) {
            this.id = id;
            this.username = username;
        }

    }

}
