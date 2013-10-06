/*
 *
 *
 */

import java.net.*;
import java.io.*;

public class Client {
	//private variables
	private Socket socket              = null;
	private DataInputStream  console   = null;
	private DataOutputStream streamOut = null;

	// client constructor
	public Client(String server_address, int port_number) {
		System.out.println("Establishing connection. Please wait...");
		// create a socket for communication with the server
		try {
			socket = new Socket(server_address, port_number);
			System.out.println("Connected: " + socket);
			start();
		}
		catch(UnknownHostException e) {
			System.out.println("Unknown host exception: " + e.getMessage());
		}
		catch(IOException e) {
			System.out.println("IO exception: " + e.getMessage());
		}
		String line = "";
		while (!line.equals("logout")) {
			try {
				line = console.readLine();
				streamOut.writeUTF(line);
				streamOut.flush();
			}
			catch(IOException e) {
				System.out.println("IO exception: " + e.getMessage());
			}
		}
	}

	// setup a new ouput stream
	public void start() throws IOException{
		console   = new DataInputStream(System.in);
		streamOut = new DataOutputStream(socket.getOutputStream());
	}

	// 
	public void stop() {
		try {
			if (console   != null)  console.close();
			if (streamOut != null)  streamOut.close();
			if (socket    != null)  socket.close();
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
			System.out.println("e.g.   java Client localhost 10\n");
		}
		else
			client = new Client(args[0], Integer.parseInt(args[1]));
	}
}
