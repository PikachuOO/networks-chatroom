/*
 * File: Server.java
 * ------------
 * Name:    Nathan Hayes-Roth
 * UNI:    nbh2113
 * Class:    Computer Networks
 * Assignment: Programming Assignment #1
 * ------------
 * Description:
 * 
 */

import java.io.*;
import java.net.*;

/*
 * The chat client thread. This client thread opens the input and the output
 * streams for a particular client, ask the client's name, informs all the
 * clients connected to the server about the fact that a new client has joined
 * the chat room, and as long as it receive data, echos that data back to all
 * other clients. The thread broadcast the incoming messages to all clients and
 * routes the private message to the particular client. When a client leaves the
 * chat room this thread informs also all the clients about that and terminates.
 */
class ClientThread extends Thread {

    private BufferedReader input_stream = null;
    private PrintStream output_stream = null;
    private Socket client_socket = null;
    private String username = "";

    public ClientThread(Socket clientSocket) {
        this.client_socket = clientSocket;
    }
    
    /*
     * Open input and output streams for this thread.
     */
    private void openStreams(){
        try {
            input_stream  = new BufferedReader(new InputStreamReader(client_socket.getInputStream()));
            output_stream = new PrintStream(client_socket.getOutputStream());
        } catch (Exception e) {
            System.err.println("Error opening ClientThread streams: " + e.getMessage());
        }
    }
    
    /*
     * Continue prompting the user for name until they provide one that contained in the accepted list.
     */
    private String getUsername(){
        while (true) {
            output_stream.println("Please enter your name:");
            String name = "";
            try {
                name = input_stream.readLine();
            } catch (IOException e) {
                System.err.println("Error reading username: " + e.getMessage());
            }
            if (Server.validUsername(name)){
                return name;
            }
        }
    }
    
    /*
     * Give the user several opportunities to enter his/her password before being banned.
     */
    private void login(String name, int failed, int allowed){
        // ban after the allowed number of tries
        if (failed >= allowed) {
            Server.synchronized_threads.remove(this);
            Server.blocked_addresses.put(client_socket.getInetAddress(), System.nanoTime());
            cleanup();
        } else if (failed > 0) {
            output_stream.println("Only " + (allowed-failed) + " more attempts...");
        }
        output_stream.println("Please enter your password:");
        String pwd = "";
        try {
            pwd = input_stream.readLine();
        } catch (IOException e) {
            System.err.println("Error reading password: " + e.getMessage());
        }
        if (Server.login(name, pwd)){
            Server.synchronized_threads.put(this, name);
        } else login(name, failed+1, allowed);
    }
    
    /* Welcome the new the client. */
    private void welcome(){
        // inform other users
        for (ClientThread thread : Server.synchronized_threads.keySet()){
            if (!thread.equals(this)){
                thread.output_stream.println(username + "has joined the server.");
            }
        }
        // welcome the new user
        output_stream.println("Welcome to the server, " + username + ".\nTo leave enter: /exit" +
        		"\nFor commands enter: /help");
    }
    
    private void cleanup(){
        try{
           output_stream.close();
           input_stream.close();
           client_socket.close();
       } catch (IOException e) {
           System.err.println("Error during ClientThread cleanup:  " + e.getMessage());
       }
   }
    
    private void listen(){
        while (true){
            String line = "";
            try {
                line = input_stream.readLine();
            } catch (IOException e) {
                System.err.println("Error listening to ClientThread: " + e.getMessage());
            }
            // quit
            if (line.equals("/quit")){
                break;
            }
            // help
            else if (line.equals("/help")){
                output_stream.println("\nCommand: whoelse\nUsage: whoelse\nDescription: displays name of other connected users");
                output_stream.println("\nCommand: wholasthr\nUsage: wholasthr\nDescription: displays name of users that connected within the last hour");
                output_stream.println("\nCommand: broadcast\nUsage: broadcast message\nDescription: broadcast message to all connected user");
                output_stream.println("\nCommand: /quit\nUsage: /quit\nDescription: disconnect from the server");
                output_stream.println("\nCommand: /help\nUsage: /help\nDescription: show command descriptions");
            }
            // whoelse
            else if (line.equals("whoelse")){
                for (ClientThread thread : Server.synchronized_threads.keySet()){
                    if (!thread.equals(this)){
                        output_stream.println("<Online> " + Server.synchronized_threads.get(thread));
                    }
                }
            }
            // wholasthr
            else if (line.equals("wholasthr")){
                
            }
            // broadcast
            else if (line.equals("broadcast")){
                
            }
            // anything else
            else {
                
            }
        }
    }
    

    public void run() {
        openStreams();
        username = getUsername();
        login(username, 0,3);
        welcome();
        listen();
        cleanup();
    }
}