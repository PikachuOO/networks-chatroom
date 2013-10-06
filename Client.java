/*
 * File: Client.java
 * ------------
 * Name : Nathan Hayes-Roth
 * UNI : nbh2113
 * Class: Computer Networks
 * Assignment: Programming Assignment #1
 * ------------
 * Description:
 * 
 */

import java.net.*;
import java.io.*;

public class Client implements Runnable {
	
	/* class variables */
	private Socket 			 client_socket = null;
	private DataInputStream  input_stream  = null;
	private DataOutputStream output_stream = null;
	private boolean			 closed		   = false;

	/* Client Constructor */
	public Client(String server_address, int port_number) {
		System.out.println("Establishing connection. Please wait...");
		// open a socket for communication with the server
		try {
			// port_number is the port number the server is listening on
			client_socket = new Socket(server_address, port_number);
			System.out.println("Connected: " + client_socket);
			start();
		}
		// catch exceptions
		catch(Exception e) {
			System.out.println("Error opening client socket: " + e.getMessage());
		}
		// read client input and send it to outpur stream
		try {
			// open a buffered reader
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			String line = "";
			// split each line and add the name/password pair to the hashtable
			while ((line = reader.readLine()) != null && !line.equals("logout")) {
				output_stream.writeUTF(line);
				output_stream.flush();
			}
		}
		catch (IOException e) {
			System.out.println("Error reading client input : " + e.getMessage());
		}
	}

	// setup a new ouput stream
	public void start() throws IOException{
		input_stream   = new DataInputStream(System.in);
		output_stream = new DataOutputStream(client_socket.getOutputStream());
	}

	// 
	public void stop() {
		try {
			if (input_stream   != null)  input_stream.close();
			if (output_stream != null)  output_stream.close();
			if (client_socket    != null)  client_socket.close();
		}
		catch(IOException ioe){
			System.out.println("Error closing ...");
		}
	}

	/*
	 * Create a thread to read data from the server.
	 */
	public void run(){
		try {
			// open a buffered reader
			BufferedReader reader = new BufferedReader(new InputStreamReader(input_stream));
			String line = "";
			// split each line and add the name/password pair to the hashtable
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
				if (!line.equals("logout")){
					break;
				}
			}
			closed = true;
		}
		catch (IOException e) {
			System.out.println("Error reading client input : " + e.getMessage());
		}
	}

	private static void instruct(){
		System.out.println("\nUsage: java Client <server_address> <port_number>");
		System.out.println("e.g.   java Client localhost 4119\n");
	}

	public static void main(String args[]) {
		// check cmd line arguments for correct length
		if (args.length != 2){
			instruct();
		}
		// create a client
		else {
			try{
				Client client = new Client(args[0], Integer.parseInt(args[1]));
			}
			// catch formatting exceptions and instruct the user
			catch (Exception e) {
				System.out.println("Command line error: " + e.getMessage());
				instruct();
			}
		}
	}
}
