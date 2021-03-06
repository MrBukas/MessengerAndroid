package com.bukas.messenger;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class FrontPage extends AppCompatActivity {
    //private static Socket socket;
    static String[] usernames;
    static ListView listView;
    static ArrayAdapter<String> arrayAdapter;
    static String talkerName;

    @Override
    protected void onResume() {
        super.onResume();
        Intent i = getIntent();
        final String username = i.getStringExtra("username");
        final String password = i.getStringExtra("password");
        new AsyncGetTalkers(username,password).execute();
    }
//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        Toast.makeText(FrontPage.this,"There is no back action",Toast.LENGTH_LONG).show();
//        try {
//            User.socket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        //FrontPage.this.finish();
//       // return;
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //socket = MainActivity.socket;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_front_page);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent i = getIntent();
        final String username = i.getStringExtra("username");
        final String password = i.getStringExtra("password");
        new AsyncGetTalkers(username,password).execute();

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
        Button buttonNewTalker = findViewById(R.id.buttonNewTalker);

        AlertDialog.Builder adBuilder = new AlertDialog.Builder(this);
        adBuilder.setTitle("New Dialog");
        adBuilder.setMessage("Enter username");
        final EditText input = new EditText(this);
        adBuilder.setView(input);
        adBuilder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                talkerName = input.getText().toString();
                //Toast.makeText(FrontPage.this,"sad",Toast.LENGTH_SHORT).show();
                new AsyncNewDialog(talkerName).execute();

            }
        });
        adBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        final AlertDialog ad = adBuilder.create();


        buttonNewTalker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ad.show();

            }
        });





    }


    class AsyncGetTalkers extends AsyncTask<Void, Void, Void>
    {
        String username;
        String password;



        public AsyncGetTalkers(String username, String password) {
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

            toastHandler.obtainMessage(1,"Start");
            // socket = new Socket(getResources().getString(R.string.server_ip),5679);
            //PrintWriter printWriter = new PrintWriter(socket.getOutputStream(),true);
            //InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
            //BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            //Scanner scanner = new Scanner(bufferedReader);
            //printWriter.println(username);
            //printWriter.println((password));
            //int auth = Integer.parseInt(scanner.nextLine());//Сообщает о результате авторизации
            User.write("talkedUsers");
            int amountOfUsers = Integer.parseInt(User.read());
            List<Talker> talkers = new ArrayList<>();
            for (int i = 0; i < amountOfUsers; i++) {
                talkers.add(new Talker(Integer.parseInt(User.read()),User.read()));
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
    class AsyncNewDialog extends AsyncTask<Void, Void, Void>
    {


        String talkerName;
        public AsyncNewDialog(String talkerName) {
            this.talkerName = talkerName;
        }

        Handler toastHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                // This is where you do your work in the UI thread.
                // Your worker tells you in the message what to do.
                Toast.makeText(getApplicationContext(),message.obj.toString(),Toast.LENGTH_SHORT).show();
            }
        };

        @Override
        protected Void doInBackground(Void... voids)  {

            toastHandler.obtainMessage(1,"Start");
            User.write("isUserExists");
            User.write(talkerName);
            int id = Integer.parseInt(User.read());
            if (id != -1){
                Intent intent = new Intent(FrontPage.this,Chat.class);
                intent.putExtra("talkerName",talkerName);
                toastHandler.obtainMessage(1,"User found").sendToTarget();
                startActivity(intent);
            }else {
                toastHandler.obtainMessage(1,"User not found").sendToTarget();
            }



            return null;
        }
    }

}
