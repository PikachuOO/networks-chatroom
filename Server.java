/*
 *
 *
 */

import java.net.*;
import java.io.*;


public class Server {
	// private variables
	private Socket          socket   = null;
	private ServerSocket    server   = null;
	private DataInputStream streamIn =  null;

	// server constructor method
	public Server(int port_number) {  
		try {
			// create a soocket that listens to connection requests
			System.out.println("Binding to port " + port_number + ", please wait  ...");
			server = new ServerSocket(port_number);
			System.out.println("Server started: " + server);
			System.out.println("Waiting for a client..."); 
			// listen for a connection to be made and accept it
			socket = server.accept();
			System.out.println("Client accepted: " + socket);
			open();
			boolean done = false;
			while (!done){
				try {
					String line = streamIn.readUTF();
					System.out.println(line);
					done = line.equals(".bye");
				}
				catch(IOException ioe){
					done = true;
				}
			}
			close();
		}
		catch(Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
	}

	// open a connection
	public void open() throws IOException {
		streamIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
	}

	// close a connection
	public void close() throws IOException {
		if (socket != null)
			socket.close();
		if (streamIn != null)
			streamIn.close();
	}

	// main
	public static void main(String args[]) {
		Server server = null;
		if (args.length != 1){
			System.out.println("\nUsage: java Server port_number");
			System.out.println("e.g.   java Server 10\n");
		}
		else
			server = new Server(Integer.parseInt(args[0]));
	}
}
