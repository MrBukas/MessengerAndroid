package com.bukas.messenger;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private static Socket socket;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button button_login  = (Button) findViewById(R.id.button_login);
        final EditText textView = findViewById(R.id.editText_username);
        final EditText pass = findViewById(R.id.editText_password);


       button_login.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               login(textView.getText().toString(),pass.getText().toString());
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
    void login(String username, String password){
       new AsyncCaller(username, password).execute();
        Toast.makeText(getApplicationContext(),"Sent",Toast.LENGTH_SHORT).show();

    }
    class AsyncCaller extends AsyncTask<Void, Void, Void>
    {
        String username;
        String password;

        public AsyncCaller(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                socket = new Socket("192.168.1.100",5678);
                PrintWriter printWriter = new PrintWriter(socket.getOutputStream(),true);
                printWriter.println("login");
                printWriter.println(username);
                printWriter.println(password);


            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}

