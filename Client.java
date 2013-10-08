/*
 * File: Client.java
 * ------------
 * Name:    Nathan Hayes-Roth
 * UNI:    nbh2113
 * Class:    Computer Networks
 * Assignment: Programming Assignment #1
 * ------------
 * Description: The client application allows users to log onto the server using the
 * client terminal. The client application opens a TCP connection to the server and 
 * then runs two threads simultaneously, allowing the user to write to as well as 
 * read from the server.
 */

import java.io.*;
import java.net.*;

public class Client implements Runnable {

    /* Class Variables */
    private static Socket         client_socket = null;  // socket to connect to server
    private static BufferedReader  input_stream = null;  // input from server
    private static PrintStream    output_stream = null;  // output to server
    private static boolean               closed = false; // boolean signal if client has logged out
    private static BufferedReader  client_input = null;  // input from client

    /*
     * Check command line arguments for correct length. If necessary,
     * instruct the user how to correctly execute the program. Default values
     * provided.
     */
    private static void setupConnection(String[] args, String server_address, int port_number){
        // check command line arguments for correct length and select address/port
        if (args.length != 2){
            System.out.println("\nUsage: java Client <server_address> <port_number>");
            System.out.println("Defaulted to: java Client localhost 4119\n");
        } else {
            server_address = args[0];
            port_number = Integer.parseInt(args[1]);
        }
        // make connection
        try {
            client_socket = new Socket(server_address, port_number);
            client_input  = new BufferedReader(new InputStreamReader(System.in));
            output_stream = new PrintStream(client_socket.getOutputStream());
            input_stream  = new BufferedReader(new InputStreamReader(client_socket.getInputStream()));
            client_input  = new BufferedReader(new InputStreamReader(System.in));
        } catch (Exception e) {
            System.err.println("Error opening client socket: " + e.getMessage());
            System.exit(1);
        }
        
    }
    
    /*
     * Manage the sending and receiving of information once the client has connected.
     */
    private static void manageConnection(){
        // verify socket and socket streams were initialized
        if (client_socket == null || output_stream == null || input_stream == null){
            System.err.println("Socket object never initialized.");
        } else {
            try {
                // create a thread to read from the server (calls run())
                new Thread(new Client()).start();
                // write to the server until the connection is closed
                while (!closed) {
                    output_stream.println(client_input.readLine());
                }
                // cleanup
                output_stream.close();
                input_stream.close();
                client_socket.close();
                System.exit(1);
            } catch (IOException e) {
                System.err.println("IOException:  " + e);
            }
        }
    }
    
    /*
     * Create a thread to read from the server. Close the connection if the Server 
     * gives the signal.
     */
    public void run() {
        String server_message;
        try {
            while ((server_message = input_stream.readLine()) != null) {
                System.out.println(server_message);
                if (server_message.indexOf("*** Bye") != -1)
                    break;
            }
            closed = true;
        } catch (Exception e) {
            System.err.println("Error reading from server:  " + e.getMessage());
            System.exit(1);
        }
    }

    /* main */
    public static void main(String[] args) {
        setupConnection(args, "localhost", 4119);
        manageConnection();
    }
}