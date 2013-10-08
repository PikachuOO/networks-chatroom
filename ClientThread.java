/*
 * File: ClientThread.java
 * ------------
 * Name:       Nathan Hayes-Roth
 * UNI:        nbh2113
 * Class:      Computer Networks
 * Assignment: Programming Assignment #1
 * ------------
 * Description: once a Client has connected to the Server, the Server spawns a ClientThread to handle
 * all future communications. Each ClientThread has its own Socket and input/output streams and handles
 *     - user login
 *     - welcome/goodbye messages
 *     - all communication with the server/other clients
 */

import java.io.*;
import java.net.*;

class ClientThread extends Thread {

    /* class variables */
    private BufferedReader input_stream = null;
    private PrintStream   output_stream = null;
    private Socket        client_socket = null;
    private String             username = "";

    /* class constuctor */
    public ClientThread(Socket clientSocket) {
        this.client_socket = clientSocket;
    }
    
    /* 
     * Run method, called when the server calls start() on newly created ClientThreads
     */
    public void run() {
        openStreams();
        username = getUsername();
        // give the client 3 chances to enter the correct password
        if (login(username, 0,3)){
            welcome();
            listen();
        }
        cleanup();
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
     * Continue prompting the user for name until they provide one that is contained in the accepted list.
     */
    private String getUsername(){
        while (true) {
            output_stream.println("\nPlease enter your name:");
            String name = "";
            try {
                name = input_stream.readLine();
            } catch (IOException e) {
                System.err.println("Error reading username: " + e.getMessage());
            }
            if (Server.validUsername(name)){
                if (Server.active_threads.values().contains(name)){
                    output_stream.println("Sorry, that user is already logged in. Please try again.");
                } else return name;
            } else{
                output_stream.println("Sorry, there's no account associated with that username. Please try again.");                
            }
        }
    }
    
    /*
     * Give the user several opportunities to enter his/her password before being banned.
     */
    private boolean login(String name, int failed, int allowed){
        // ban after the allowed number of tries
        if (failed >= allowed) {
            output_stream.println("\nYou have been banned from the server. Better luck next time.");
            Server.active_threads.remove(this);
            Server.blocked_addresses.put(client_socket.getInetAddress(), System.nanoTime());
            return false;
        } else if (failed > 0) {
            output_stream.println("Password incorrect. Only " + (allowed-failed) + " more attempts...");
        }
        output_stream.println("\nPlease enter your password:");
        String pwd = "";
        try {
            pwd = input_stream.readLine();
        } catch (IOException e) {
            System.err.println("Error reading password: " + e.getMessage());
        }
        // if it checks out, add the thread to active threads and log the current time
        if (Server.login(name, pwd)){
            // make sure this account isn't being logged in on two client threads simultaneously
            if (Server.active_threads.containsValue(username)){
                output_stream.println("Sorry, this account has already logged in.");
                return false;
            }
            Server.active_threads.put(this, name);
            Server.login_times.put(username, System.nanoTime());
            return true;
        } else return login(name, failed+1, allowed);
    }
    
    /* 
     * Welcome the new the client. 
     */
    private void welcome(){
        // inform other users
        for (ClientThread thread : Server.active_threads.keySet()){
            if (!thread.equals(this)){
                thread.output_stream.println("\n*** " + username + " has joined the server. ***\n");
            }
        }
        // welcome the new user
        output_stream.println("\n*** Welcome to the server, " + username + 
                ". ***\n\tTo leave, type:     \"/exit\"" +
                "\n\tFor commands, type: \"/help\"\n");
    }
    
    private void listen(){
        while (true){
            String line = "";
            try {
                line = input_stream.readLine();
            } catch (IOException e) {
                System.err.println("Error listening to ClientThread: " + e.getMessage());
                break;
            }
            // quit
            if (line.equals("/quit")){
                break;
            }
            // help
            else if (line.equals("/help")){
                output_stream.println("\nCommand: \twhoelse\nUsage: \t\twhoelse\nDescription: \tDisplays the names of all other connected users.");
                output_stream.println("\nCommand: \twholasthr\nUsage: \t\twholasthr\nDescription: \tDisplays the name of users that connected within the last hour.");
                output_stream.println("\nCommand: \tbroadcast\nUsage: \t\tbroadcast message\nDescription: \tSend a message to all other connected users.");
                output_stream.println("\nCommand: \t/quit\nUsage: \t\t/quit\nDescription: \tDisconnects from the server.");
                output_stream.println("\nCommand: \t/help\nUsage: \t\t/help\nDescription: \tShow command descriptions.\n");
            }
            // whoelse
            else if (line.equals("whoelse")){
                for (ClientThread thread : Server.active_threads.keySet()){
                    if (!thread.equals(this)){
                        output_stream.println("<Online> " + Server.active_threads.get(thread));
                    }
                }
                output_stream.println();
            }
            // wholasthr
            else if (line.equals("wholasthr")){
                for (String other_name : Server.login_times.keySet()){
                    long elapsed_time = System.nanoTime() - Server.login_times.get(other_name);
                    long seconds = elapsed_time / 1000000000;
                    if (seconds <= 3600){
                        int minutes = (int)seconds / 60;
                        seconds = (int)seconds % 60;
                        output_stream.println("<" + minutes + ":" + seconds + " ago> " + other_name);
                    }
                }
                output_stream.println();
            }
            // broadcast
            else if (line.startsWith("broadcast ")){
                String message = line.substring(10);
                for (ClientThread thread : Server.active_threads.keySet()){
                    if (!thread.equals(this)){
                        thread.output_stream.println("<" + username +"> " + message);
                    }
                }
                output_stream.println();
            }
            // anything else
            else {
                output_stream.println("\n*** Sorry, I couldn't understand that command. Type \"/help\" for a list of commands. ***\n");
            }
        }
    }

    /*
     * Remove the thread from the Hashtable of currently running threads, inform others of this user's departure, 
     * and close this thread and its streams.
     */
    private void cleanup(){
        Server.active_threads.remove(this);
        for (ClientThread thread : Server.active_threads.keySet()){
            if (thread.username.equals(this.username)){
                // inform the logged in user of the attempt to login elsewhere
                thread.output_stream.println("\n*** Another user was prevented from logging into your account. ***\n");
            }
            else {
                thread.output_stream.println("\n*** " + username + " has left the server. ***\n");
            }
        }
        try{
           output_stream.close();
           input_stream.close();
           client_socket.close();
       } catch (IOException e) {
           System.err.println("Error during ClientThread cleanup:  " + e.getMessage());
       }
   }
}