package com.bukas.messenger;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;

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
    static PrintWriter printWriter;
    static InputStreamReader inputStreamReader;
    static BufferedReader bufferedReader;
    //static Scanner scanner;
    static String[] messages;
    static String talkerName;
    static ArrayAdapter<String> arrayAdapter;
    static ListView listViewMessage;
    static List<MessageFromDatabase> messagesList;
    static CustomMessageAdapter cma;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        Intent i = getIntent();
        talkerName = i.getStringExtra("talkerName");
        getSupportActionBar().setTitle(talkerName);
        messagesList = new ArrayList<>();




        new AsyncGetDialog().execute();

        listViewMessage = findViewById(R.id.listViewMessage);

        final EditText editTextMsg = findViewById(R.id.editTextMsg);
        Button buttonSend = findViewById(R.id.buttonSendMsg);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AsyncSendMessage(editTextMsg.getText().toString()).execute();
                editTextMsg.setText("");
            }
        });

    }



    class AsyncSendMessage extends AsyncTask<Void, Void, Void>{
        String msg;
        public AsyncSendMessage(String msg) {
            this.msg = msg;
        }

        @Override
        protected Void doInBackground(Void... voids) {
           User.write("message");
           User.write(talkerName);
           User.write(msg);
            messagesList.add(new MessageFromDatabase(msg,true));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cma = new CustomMessageAdapter();
                    listViewMessage.setAdapter(cma);
                    scrollListViewToBottom();
                }
            });
            return null;
        }
    }
    class AsyncGetDialog extends AsyncTask<Void, Void, Void>
    {

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

//                socket = new Socket(getResources().getString(R.string.server_ip),5679);
//                printWriter = new PrintWriter(socket.getOutputStream(),true);
//                inputStreamReader = new InputStreamReader(socket.getInputStream());
//                bufferedReader = new BufferedReader(inputStreamReader);
//                scanner = new Scanner(bufferedReader);
//                printWriter.println(username);
//                printWriter.println((password));
                //int auth = Integer.parseInt(User.read());//Сообщает о результате авторизации
                User.write("dialog");
                User.write(talkerName);
                String msg;
                int user_id = Integer.parseInt(User.read());
                msg = User.read();
                if (!msg.equals("endl"))
                do {
                    boolean sentByUser = Integer.parseInt(User.read()) == user_id;
                    messagesList.add(new MessageFromDatabase(msg,sentByUser));
                    msg = User.read();

                    System.out.println("liNe GoTt");
                }while (!msg.equals("endl"));
                System.out.println(messagesList.size());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cma = new CustomMessageAdapter();
                        listViewMessage.setAdapter(cma);
                        scrollListViewToBottom();
                    }
                });
                //Старый способ получения сообщений
                /*do {
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
                });*/

                return null;


        }
    }
    class CustomMessageAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return messagesList.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.custom_message_adapter_layout,null);
            TextView messageTextUser = view.findViewById(R.id.messageUserTextView);
            TextView messageTextTalker = view.findViewById(R.id.messageTalkerTextView);
            if (messagesList.get(i).sentByUser){
                messageTextUser.setText(messagesList.get(i).text);
                messageTextTalker.setText("");
            }else {
                messageTextUser.setText("");
                messageTextTalker.setText(messagesList.get(i).text);
            }
            return view;
        }
    }
    class MessageFromDatabase{
        String text;
        boolean sentByUser;
        public MessageFromDatabase(String text, boolean sentByUser) {
            this.text = text;
            this.sentByUser = sentByUser;
        }


    }
    private void scrollListViewToBottom() {
        listViewMessage.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                listViewMessage.setSelection(cma.getCount() - 1);
            }
        });
    }

}

