/*
 *
 *
 */

import java.net.*;
import java.io.*;
import java.util.Hashtable;


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

	// load credentials from text file into Hashtable
	public static Hashtable<String, String> loadCredentials(){
		Hashtable<String, String> to_return = new Hashtable<String, String>();
		try {
			// open a buffered reader
			BufferedReader reader = new BufferedReader(new FileReader("./credentials.txt"));
			String line = null;
			// split each line and add the name/password pair to the hashtable
			while ((line = reader.readLine()) != null) {
				String[] split = line.split("\\s");
				to_return.put(split[0], split[1]);
			}
		}
		catch (Exception e) {
			System.out.println("Error loading credentials: " + e.getMessage());
		}
		return to_return;

	}

	// main
	public static void main(String args[]) {
		Server server = null;							// server
		Hashtable credentials = loadCredentials();		// read credentials into Hashtable
		System.out.println("DEBUG: " + credentials.size());

		if (args.length != 1){
			System.out.println("\nUsage: java Server port_number");
			System.out.println("e.g.   java Server 4119\n");
		}
		else
			server = new Server(Integer.parseInt(args[0]));
	}
}
