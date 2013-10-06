/*
 *
 *
 */

import java.net.*;
import java.io.*;

public class Client {
	//private variables
	private Socket client_socket              = null;
	private DataInputStream  console   = null;
	private DataOutputStream streamOut = null;

	// client constructor
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
				streamOut.writeUTF(line);
				streamOut.flush();
			}
		}
		catch (IOException e) {
			System.out.println("Error reading client input : " + e.getMessage());
		}
	}

	// setup a new ouput stream
	public void start() throws IOException{
		console   = new DataInputStream(System.in);
		streamOut = new DataOutputStream(client_socket.getOutputStream());
	}

	// 
	public void stop() {
		try {
			if (console   != null)  console.close();
			if (streamOut != null)  streamOut.close();
			if (client_socket    != null)  client_socket.close();
		}
		catch(IOException ioe){
			System.out.println("Error closing ...");
		}
	}

	// main
	public static void main(String args[]) {
		Client client = null;
		if (args.length != 2){
			System.out.println("\nUsage: java Client port_number");
			System.out.println("e.g.   java Client localhost 4119\n");
		}
		else
			client = new Client(args[0], Integer.parseInt(args[1]));
	}
}
