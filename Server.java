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
import java.util.Hashtable;
import java.util.Vector;
import java.util.HashSet;


public class Server {

    /* Class Variables */
    private static ServerSocket                     server_socket = null;
    private static Socket                           client_socket = null;
    private static final int                      maxClientsCount = 10;
    private static final ClientThread[]                   threads = new ClientThread[maxClientsCount];
    public static Vector<ClientThread>       synchronized_threads = new Vector<ClientThread>();             // Synchronized storage prevents errors such as null pointers
    private static Hashtable<String, String>          credentials = new Hashtable<String,String>();
    private static Hashtable<InetAddress, Long> blocked_addresses = new Hashtable<InetAddress, Long>();     // Synchronized storage prevents errors such as null pointers
    
    /*
     * Check command line arguments for correct length. If necessary,
     * instruct the user how to correctly execute the program. Default values
     * provided.
     */
    private static void setupServer(String[] args, int port_number){
        // check command line arguments for correct length and select port
        if (args.length != 1){
            System.out.println("\n       Usage: java Server <port_number>");
            System.out.println("Defaulted to: java Server 4119\n");
        } else {
            port_number = Integer.parseInt(args[0]);
        }
        // open a server socket to listen for connections on
        try {
            server_socket = new ServerSocket(port_number);
        } catch (Exception e) {
            System.err.println("Error creating server socket: " + e.getMessage());
        }
        
    }
    
    /* 
     * Load credentials from text file into Hashtable
     */
    private static Hashtable<String, String> loadCredentials() {
        try {
            @SuppressWarnings("resource")
            BufferedReader reader = new BufferedReader(new FileReader("./credentials.txt"));
            String line = null;
            // split each line and add the name/password pair to the hashtable
            while ((line = reader.readLine()) != null) {
                String[] split = line.split("\\s");
                credentials.put(split[0], split[1]);
            }
        } catch (Exception e) {
            System.out.println("Error loading credentials: " + e.getMessage());
        }
        return credentials;
    }

    /*
     * Listen for new client connections and treat them accordingly.
     */
    private static void listen() {
        // loop indefinitely
        while (true) {
            try {
                // listen for a connection and accept it
                client_socket = server_socket.accept();
                // check if the address is banned
                if (!vetClient(client_socket, 10)){
                    // let the bad man know what he did
                    PrintStream os = new PrintStream(client_socket.getOutputStream());
                    os.println("\nYou have been temporarily banned from the server. Please try again later.\n");
                    os.close();
                    client_socket.close();
                } else {
                    //synchronized_threads.add(new ClientThread(client_socket, threads));
                    
                    
                    blocked_addresses.put(client_socket.getInetAddress(), System.nanoTime());
                    
                    int i = 0;
                    for (i = 0; i < maxClientsCount; i++) {
                        if (threads[i] == null) {
                            (threads[i] = new ClientThread(client_socket, threads))
                                    .start();
                            break;
                        }
                    }
                    if (i == maxClientsCount) {
                        PrintStream os = new PrintStream(
                                client_socket.getOutputStream());
                        os.println("Server too busy. Try later.");
                        os.close();
                        client_socket.close();
                    }
                    
                    
                }
            } catch (Exception e) {
                System.err.println("Error creating a ClientThread: " + e.getMessage());
            }
                
                /*
                
                blocked_addresses.put(client_socket.getInetAddress(), System.nanoTime());
                
                int i = 0;
                for (i = 0; i < maxClientsCount; i++) {
                    if (threads[i] == null) {
                        (threads[i] = new ClientThread(client_socket, threads))
                                .start();
                        break;
                    }
                }
                if (i == maxClientsCount) {
                    PrintStream os = new PrintStream(
                            client_socket.getOutputStream());
                    os.println("Server too busy. Try later.");
                    os.close();
                    client_socket.close();
                }
                
                
            } catch (IOException e) {
                System.out.println(e);
            }
            
                */
        }
        
    }
    
    /*
     * Check to see if a client socket is on the list of banned addresses.
     * If it is, check to see if enough time has passed.
     */
    private static boolean vetClient(Socket socket, int enough_time) {
        InetAddress address = socket.getInetAddress();
        // if the address is blocked
        if (blocked_addresses.containsKey(address)){
            // if enough time has passed
            long elapsed_time = System.nanoTime() - blocked_addresses.get(address);
            double seconds = (double)elapsed_time / 1000000000.0;
            if (seconds > enough_time){
                // remove it from the list and return true
                blocked_addresses.remove(address);
                return true;
            } else return false;
        } else return true;
    }

    public static void main(String args[]) {
        loadCredentials();
        setupServer(args, 4119);
        listen();
    }

}