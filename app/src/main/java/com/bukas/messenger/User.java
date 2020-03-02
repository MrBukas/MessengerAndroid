package com.bukas.messenger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class User {
    static Socket socket = null;
    static PrintWriter printWriter;
    static InputStreamReader inputStreamReader;
    static BufferedReader bufferedReader;
    static Scanner scanner;

    public User(Socket s) throws IOException {
        socket = s;
        printWriter = new PrintWriter(socket.getOutputStream(),true);
        inputStreamReader = new InputStreamReader(socket.getInputStream());
        bufferedReader = new BufferedReader(inputStreamReader);
        scanner = new Scanner(bufferedReader);
    }

    public static void write(String s){
        printWriter.println(s);
    }

    public static String read(){
        return scanner.nextLine();
    }
    public static void closeSocket() throws IOException {
        socket.close();
        socket = null;
    }



}
