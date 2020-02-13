package com.bukas.messenger;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private static Socket socket;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button button_login  = (Button) findViewById(R.id.button_login);
        final EditText edittext_username = findViewById(R.id.editText_username);
        final EditText edittext_pass = findViewById(R.id.editText_password);
        List<String> username_password = loadData();

        edittext_username.setText(username_password.get(0));
        edittext_pass.setText(username_password.get(1));

        if (!edittext_username.getText().toString().equals("Name")) {
            //Попытка войти с прошлым паролем
            login(edittext_username.getText().toString(), edittext_pass.getText().toString());
        }


       button_login.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               login(edittext_username.getText().toString(),edittext_pass.getText().toString());
           }
       });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public List<String> loadData(){
        List<String> login_password = new ArrayList<>();
        SharedPreferences values = getApplicationContext()
                .getSharedPreferences(getString(R.string.preference_file_key), 0);
        login_password.add(values.getString("Username","Name"));
        login_password.add(values.getString("Password",""));
        return login_password;
    }
    void login(String username, String password){
       new AsyncCaller(username, password).execute();
        //Toast.makeText(getApplicationContext(),"Sent",Toast.LENGTH_SHORT).show();

    }

    class AsyncCaller extends AsyncTask<Void, Void, Void> implements Serializable
    {
        String username;
        String password;

        public AsyncCaller(String username, String password) {
            this.username = username;
            this.password = password;
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
                socket = new Socket("176.214.187.245",5679);
                PrintWriter printWriter = new PrintWriter(socket.getOutputStream(),true);
                InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                Scanner scanner = new Scanner(bufferedReader);
                printWriter.println(username);
                printWriter.println(hashPassword(password));
                int auth = Integer.parseInt(scanner.nextLine());
                switch (auth){
                    case 0:
                        toastHandler.obtainMessage(1,"Logged in").sendToTarget();
                        saveData(username,password);
                        Intent intent = new Intent(MainActivity.this, FrontPage.class);
                        intent.putExtra("username",username);
                        intent.putExtra("password",hashPassword(password));
                        startActivity(intent);
                        break;
                    case 1:
                        toastHandler.obtainMessage(1,"Wrong").sendToTarget();
                        break;
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}

