package com.bukas.messenger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Starter extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starter);


        if (User.socket != null){
            startActivity( new Intent(Starter.this, FrontPage.class));
        }else {
            List<String> username_password = loadData();
            login(username_password.get(0),username_password.get(1));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Starter.this.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("RESUME");
        System.out.println("RESUME");




    }

    void login(String username, String password){
        new AsyncCaller(username, password).execute();
        //Toast.makeText(getApplicationContext(),"Sent",Toast.LENGTH_SHORT).show();

    }
    public List<String> loadData(){
        List<String> login_password = new ArrayList<>();
        SharedPreferences values = getApplicationContext()
                .getSharedPreferences(getString(R.string.preference_file_key), 0);
        login_password.add(values.getString("Username","Name"));
        login_password.add(values.getString("Password",""));
        return login_password;
    }
    class AsyncCaller extends AsyncTask<Void, Void, Void> implements Serializable
    {
        String username;
        String password;

        public AsyncCaller(String username, String password) {
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
                if (User.socket == null){
                    Socket socket = new Socket(getResources().getString(R.string.server_ip),5679);
                    new User(socket);
                }


                //PrintWriter printWriter = new PrintWriter(socket.getOutputStream(),true);
                //InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
                //  BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                // Scanner scanner = new Scanner(bufferedReader);

                //printWriter.println("login");
                int auth;

                User.write("login");
                User.write(username);
                User.write(hashPassword(password));
                auth = Integer.parseInt(User.read());

                switch (auth){
                    case 0:
                        toastHandler.obtainMessage(1,"Logged in").sendToTarget();
                        saveData(username,password);
                        //socket.close();
                        Intent intent = new Intent(Starter.this, FrontPage.class);
                        intent.putExtra("username",username);
                        intent.putExtra("password",hashPassword(password));
                        //MainActivity.this.finish();
                        startActivity(intent);
                        //onDestroy();
                        break;
                    case 1:
                        toastHandler.obtainMessage(1,"Wrong").sendToTarget();
                        startActivity(new Intent(Starter.this,MainActivity.class));
                        //onDestroy();
                        break;
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
