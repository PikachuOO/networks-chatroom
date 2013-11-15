# compiler
JCC = javac

# compilation flags
JFLAGS = -g

# default target
all: Client.class ClientThread.class Server.class

Client.class: Client.java
	$(JCC) Client.java

ClientThread.class: ClientThread.java
	$(JCC) ClientThread.java

Server.class: Server.java
	$(JCC) Server.java

clean: 
	rm -f *.class
