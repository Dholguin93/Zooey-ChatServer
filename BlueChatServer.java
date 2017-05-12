/***************
 * BlueChatServer
 * Author: Diego Holguin & John Aromando
 *
 * This server tracks the dialog between various clients by connecting each client to the server,
 * which is handled by creating a seperate thread, and sends client dialog to a chat server
 * room. This was developed 
 **************/

import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.Date;
import java.lang.Thread.*;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.lang.String;

public class BlueChatServer implements Runnable
{   
    /**
     * BlueChatRoom
     * - This class tracks the dialog between specific clients that are allowed permission either within
     * specific rooms or as 
     */
    public class BlueChatRoom
    {
		/* The name of the BlueChatRoom */
		String roomName; 

		/* The conversations that will be displayed within the room */
		String[] chatRoomConversations;

		/* The BlueChatRoom's password, which will be needed to access this specific room */
		String password;

		/* The maximum number of conversations allowed within this room */
		int maxAmountOfConversations;

		/* The current amount of conversations being displayed within the room */
		int currentAmountOfConversations; 

		/* The Client's that are currently connected to this room */
		HashSet<ClientConnection> clientsWithinRoom;

		/* The maximum number of clients allowed within this room */
		int maxAmountOfClients;

		/* Is this room currently running */
		boolean isRoomActive;

		/* Is this the defualt main room */
		boolean isMainRoom; // Is this room the Main Chat Room

		/***
	 	* Basic Constructor
	 	* @param maxConversationAllowed The maximum amount of conversation within the room
	 	* @param nameOfRoom The name of the Room
	 	* @param isMain Is this room the main Room
	 	* @param passWord The password for the Room
	 	***/
        public BlueChatRoom(int maxConversationAllowed, String nameOfRoom, boolean isMain, String passWord)
		{	    
	    	/* Set the maximum amount of clients within the BlueChatRoom */
	    	clientsWithinRoom = new HashSet<ClientConnection>();

	    	maxAmountOfConversations = maxConversationAllowed;

	    	/* Set the maximum amount of conversations  within the BlueChatRoom */
	    	chatRoomConversations = new String[maxConversationAllowed];

	    	/* Set the name of the room */
	    	roomName = nameOfRoom;

	    	/* Set the Room to flag that is it no active */
	    	isRoomActive = true;

	    	password = passWord;

	    	/* Set the Main Room flag to determine is this  */
	    	isMainRoom = isMain;

	    	/* Set the current amount of conversations being displayed */
	    	currentAmountOfConversations = 0;
		}

		/**
	 	* Purpose: To add a ClientConnection to the room's container of clients
	 	* @param Cconnect The client that will monitored by the room
	 	*/
		public synchronized void addConnectionToRoom(ClientConnection Cconnect)
		{
	    	/* Add the client to the BlueChatRoom's HashSet of ClientConnections */
	    	this.getClientsWithinRoom().add(Cconnect);
		}

		public synchronized HashSet<ClientConnection> getClientsWithinRoom()
		{
	    	return clientsWithinRoom;
		} 

		/**
	 	* Purpose: To remove a specific client from the room
	 	* @param Cconnect The specific client that is requesting to leave the room
	 	*/
		public synchronized void removeConnectionToRoom(ClientConnection Cconnect) // CONCURRENTMODIFICATION ERROR HAPPENS HERE!!
		{
	    	for(ClientConnection connection : this.getClientsWithinRoom())
	    	{
	    		System.out.println("Sending something out to " + connection.clientName);
				connection.sendClientMessage("[" + Cconnect.clientName + "]" + " Has Left The Room", "M");

				/* If one of those clients are the clients requesting to leave the room*/
				if(connection.clientName.equals(Cconnect.clientName))
				{	    
		    		/* Let the BlueChatRoom know that they are no longer active within their room */
		    		this.clientsWithinRoom.remove(connection);
				}
	    	}
		}

		/**
	 	* Purpose: To set the BlueChatRoom's password
	 	* @param newPassword The new password for the specific room
	 	*/
		public void setPassword(String newPassword)
		{
	    	/* Set the password of the BlueChatRoom to be the paramerized String */
	    	this.password = newPassword;
		}

		/**
	 	* Purpose: To provide an interface of either automatically joining a room with no password, or reading in client input and matching it up with the 
	 	* SHA1 encrpytion of the room's password. Then based upon the user's input either let's the client join the room or aborts connecting them due to their attempt
	 	* not being the correct password. 
	 	* @param newRoom The room that was identified to be one with a password (from the function doesRoomHavePassword)
	 	* @param Coutput An output stream to the client used to print messages to the client
	 	* @param Cinput An input stream of the client used to read in input
	 	*/
		public synchronized BlueChatRoom joinByPassword(BlueChatRoom newRoom, String line, ClientConnection client)
		{
	    	/* Initialzie the return value of the function */
	    	BlueChatRoom clientsNewRoom = null;

	    	/* Parse the client's message to obtain only the relavent information (roomname-password) */
	    	String clientMessage = line.substring(9,line.length());
	    
	    	/* Format the string to take into account any unnessacary spaces */
	    	String formattedString = formatClientString(clientMessage);

	    	/* Calculate the dash location, needed in order to parse the client message's room name and password request */
	    	int dashLocation = formattedString.indexOf("-");
	    
	    	/* Parse the formatted string to contain only the password requested by the client */
	    	String password = formattedString.substring(dashLocation + 1, formattedString.length());

	    	/* If the password is essentially nothing, store it internally as "0" which represents a room with no password*/
	    	if(password.equals(""))
	    	{
				password = "0";
	    	}
	    
	    	/* Create an instance of John's SHA1 program, which encrypts the password attempt and stores it internally */
	    	sha1 passwordHash = new sha1(password);

	    	/* Grab this value from SHA1's internal storage for the encrypted text */
	    	String hashed = passwordHash.hashed;

	    	/* Set actual password attempt to be this SHA1 encryption, since room's password are stored in as a SHA1 encryption */
	    	password = hashed;

	    	/* If the password matches up with the room's password in terms of their SHA1 encryptions */
	    	if(password.equals(newRoom.password))
	    	{
				/* Set the return value to return the room requested */
	        	clientsNewRoom = newRoom;

				/* Print out that their attempt was a success */
	        	client.sendClientMessage(newRoom.roomName, "JRA");
	    	}
	    	/* If they fail in their attempt  */
	    	else
	    	{
				/* Print out that their password attempt was incorrect, and is aborting the command to join the room */
				client.sendClientMessage("JRF", "E");
	    	}

	    	/* Return either a null value, meaning it failed, or the room requested */
	    	return clientsNewRoom;
		}
	
		/***
	 	* Purpose: To add the user conversations into the char room to be displayed
	 	* @param clientConversation The conversation that will be displayed within the BlueChatRoom
	 	**/
		public synchronized void addConversationToRoom(String clientConversation, BlueChatRoom currentRoom)
		{
	    	/* If the max amount of conversations that can be displayed hasn't been reached */
	    	if(currentAmountOfConversations != maxAmountOfConversations)
	    	{
				chatRoomConversations[currentAmountOfConversations] = clientConversation;
				currentAmountOfConversations++;
	    	}
	    	/* If the max amount of conversations that can be displayed have been reached.. filter the old one's out */
	    	else
	    	{
				/* Shift every conversationsCycle through each conversation */
				for(int i = 0; i < maxAmountOfConversations; i++)
				{
		    		/* For each conversation besides the first one */
		    		if(i != 1)
		    		{
						/* Grab each Conversation at a given index */
						String currentConversation = chatRoomConversations[i];

						/* Decrement it's given index where it is displayed one unit  */
						chatRoomConversations[i-1] = currentConversation;
		    		}
				}

				/* After shifting all of the other conversations, then input the user's conversation */
				chatRoomConversations[maxAmountOfConversations -1] = clientConversation;
	    	}

	    	/* Display this newly added conversation to everyone within the room */
	    	displayToAllClients(clientConversation, currentRoom);
		}
	
		/**
	 	* Purpose: To display any new messages added to the room's and display them to each client within the room
	 	* @param message The message that will be displayed to everyone
	 	**/
		public synchronized void displayToAllClients(String message, BlueChatRoom currentRoom)
        {
	    	/* Create an interator to iterate through the HashSet of Client's within the room */
	    	Iterator iter = this.getClientsWithinRoom().iterator();

	    	while(iter.hasNext())
	    	{
				/* Get a singleBlueChatRoom wihtin the hashSet via iterator */
	        	ClientConnection singleClient = (ClientConnection)iter.next();

				/* If this client is in the same room as the person sending the message, display this new message to all of the parties memebers only */
				if(singleClient.currentChatRoom.roomName.equals(currentRoom.roomName))
				{
		    	singleClient.sendClientMessage(message, "M");
				}
	    	}	   
		}
    } /* End of the BlueChatRoom Class */

    /**
     * ClientConnection
     * - This tracks handles reading in transmitted data both to and from the user. Subsequentially, it handles the process of acquiring the clients username and message 
     * that he/she would like to send to any BlueChatrOOM
     */   
    public class ClientConnection implements Runnable
    {
		/* A pointer to the BlueChatServer */
		BlueChatServer server;
	
		/* * * * * * 
	 	* Socket: A socket is an endpoint for communication between two machines. 
	 	* - The client socket used for communicating between the Server and the Client.
	 	* * * * * */
        Socket clientSocket;

		/* * * * * * 
	 	* PrintWriter: Prints formatted representations of objects to a text-output stream (println,printf,.etc.)
	 	* - Prints out Client-related data into a text-output stream that the server uses when displaying dialog to the user.
	 	* * * * * */ 
	 	PrintWriter clientOutput;

	 	/* * * * * * 
	 	* BufferedReader :Reads text from a character-input stream, buffering characters so as to provide for 
	 	* the efficient reading of characters, arrays, and lines. 
	 	* - Used to read Client Input in a clean and effecient way.
	 	* * * * * */ 
		BufferedReader clientInput;

		/* The current Chat Room of the Client */
		BlueChatRoom currentChatRoom;

		/* Determines if the client is in the Chat Server or not */
		boolean isClientInChat;

		/* Containts the name of the client within the Chat Server */
		String clientName;

		public boolean checkUsername, runClientGUI;

		String clientIP;

		/**** 
	 	* Constructor
	 	* @param Csocket The socket that is attached to the ClientConnection
	 	* @param Cname The IP Address of the client
	 	* @param blueServer The pointer to the blueServer, in order to request information from the server
	 	****/
		public ClientConnection(Socket Csocket, String IP, BlueChatServer blueServer)
		{
	    	/* Initalzie the appropriete variables for the Connection class based upon parameters */
	    	this.clientSocket = Csocket;
	    	this.clientIP = IP;
	    	this.isClientInChat = true;
	    	this.server = blueServer;

	    	this.checkUsername = false;
	    	this.runClientGUI = false;
	    
	    	try
	    	{
				/* Sets up the clientOutput stream to read in input from the clientSocket Output Stream */
				clientOutput = new PrintWriter(clientSocket.getOutputStream(),true);

				/* Sets up the clientInput stream to read in Input from the Socket Input Stream  */
				clientInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	    	}
	    	catch(IOException e)
	    	{
				System.out.println("Error Occured");
	    	}
		}

		/**
	 	* Purpose : To set the current chat room of the client to the requested room
	 	* @param room The room that the client will be joining
	 	* @param serverStartUp A flag that let's the function handle two different cases when the server is starting up and when this function is being called by a client
	 	*/
		public synchronized void setActiveRoom(BlueChatRoom room, boolean serverStartUp)
		{
	    	/* When the BlueChatServer is initally running... */
	    	if(serverStartUp)
	    	{		
				/* Let the client to know that it is starting in paramaterized room */
				this.currentChatRoom = room;

				/* Then let the paramaterized room know that some client has joined it's room */
				this.currentChatRoom.addConnectionToRoom(this);

				/* Upon server startup, add the clientConnection to the HashSet of ClientConnections within the server */
				this.server.clientConnections.add(this);

	   		}
	    	/* Whenever a client is changing their room's via runtime commands */
	    	else // CONCURRENTMODIFICATION ERROR HAPPENS HERE!!!
	    	{
				/* Detial to every user within the room that this user is leaving that this user has left */
				this.server.processClientInput("Has Left The " + this.currentChatRoom.roomName + " Room", this.clientName, this.currentChatRoom);
				
				/* Let the client's old room know that this client is leaving their room */ 
				this.currentChatRoom.removeConnectionToRoom(this);
				
				/* Let the parameterized room know that this client is joining it's room */
	        	room.addConnectionToRoom(this);
	        	
	        	/* Let the client to know that it is starting in paramaterized room */
				this.currentChatRoom = room;
				
				/* Detial to every user within the room that this user is leaving that this user has left */
				this.server.processClientInput("Has Joined The " + this.currentChatRoom.roomName + " Room", this.clientName, this.currentChatRoom);
	    	}
		}

		public void sendClientMessage(String message, String serverProtocal)
		{
	    	String serverMessage = "BLUESERVER[" + serverProtocal + "] " + message;
	    	this.clientOutput.println(serverMessage);
		}

		public void processClientGUIInput(BufferedReader clientInputStream, PrintWriter clientOutputStream)
		{   
	    	/* While the client is connected to the Chat Server, process their commands */
	    	while(this.isClientInChat)
	    	{
				try
				{
		    		String constantlyUpdatedInput = clientInputStream.readLine();
		    		System.out.println(constantlyUpdatedInput);

		    		if(!constantlyUpdatedInput.equals(""))
		    		{
						if(this.checkUsername == false)
						{
			    			checkUserNameProcess(constantlyUpdatedInput);
						}
						else
						{
			   				runClientGUIProcess(constantlyUpdatedInput);
						}
					}
				}
				catch (IOException e)
				{
		    		System.out.println("Error Occured in the processClientGUIInput Function");
		    		System.out.println("Error Message : " + e.getMessage());
				}
	    	}
		}

		public void checkUserNameProcess(String username)
		{
	   		/* Determine if the username is already in use or not */
	   		boolean doesUserNameExist  = this.server.checkIfAlreadyExist(username, this.clientOutput);

	    	/* If it is, print this out to the client */
	    	if(doesUserNameExist)
	    	{
				sendClientMessage("UAIU", "E");
	    	}
	    	/* If it isn't stop checking for the username, and add this ClientConnection to the server's container of ClientConnections */
	    	else
	    	{
				/* Add the client Connection to the main server */
				this.server.clientConnections.add(this);
		
				/* Set the client's username to indeed to be requested username */
				this.clientName = username;

				/*  Let the client know their username*/
				this.sendClientMessage(username, "UA");

				/* Inform the client and other clients within the main room that this client has joined the room */
				this.server.processClientInput("Has Just Joined The " + this.currentChatRoom.roomName + " Room", this.clientName, this.currentChatRoom);

				/* Log that this server joined this server */
				this.server.addToServerLog(this.clientName);

				/* Set flag to true, allowing user to proceed along the interface */
	       		this.checkUsername = true;
				this.runClientGUI = true;
	    	}
		}

		/**
	 	* Purpose: To handle processing the input of the user once they have passed the the username phase of the application
	 	* @param clientMessage The message that is sent from the client
	 	*/
		public void runClientGUIProcess(String clientMessage)
		{
	    	/* Process the client's input */
	    	processLine(clientMessage, this);

	    	/* Determine if the user is trying to join a room that has a password */
	    	BlueChatRoom roomToJoin = doesRoomExist(clientMessage, this);

	    	/* If the user is trying to join a room with a password */
	    	if(roomToJoin != null)
	    	{
				/* Check if the password is correct */
				BlueChatRoom passwordedRoom = roomToJoin.joinByPassword(roomToJoin, clientMessage, this);

				/* If the user is able to join the room successfully */
				if(passwordedRoom != null)
				{
		    		/* Notify the user that they have successfully joined the room */
		    		this.sendClientMessage(passwordedRoom.roomName,"JRA");

		    		/* Set the active room of the client to be the room they have successfully joined */
		    		this.setActiveRoom(passwordedRoom,false);
				}
	    	}
		}
	

		/**
	 	* Start running the thread for this ClientConnection
	 	**/
		public void run()
		{
	    	/* Begin processing the client  */
	    	processClientGUIInput(this.clientInput, this.clientOutput);	      
		}

		/** 
	 	* Purpose: Prints out the message with the client Username bracketed in front of the message
	 	* @param Message The message that is to be printed out
	 	**/
		public void printMessage(String Message)
		{
	   		System.out.println("["+clientName+"]: " + Message);
		}
    } /* End of the ClientConnection Class */

    
    
    /* A container that holds the total number of clientConnections within the BlueChatServer */
    public static HashSet<ClientConnection> clientConnections;

    /* A container that holds all of the logs  within the BlueChatServer */
    public static HashSet<String> serverLogs;
    
    /*  The defualt port for the chat sever */
    public static final int Default_Port = 1518;

    /* The port to listen to for this server */
    public int serverPort;

    /*  Is the BlueChatServer currently running */
    public boolean isRunning;

    /* The collection of active BlueChatRooms within the BlueChatServer */
    public static HashSet<BlueChatRoom> chatRoomsActive;

    /**
     * Basic BlueChatServer Constructor
     * @param port The port to listen to
     **/
    public BlueChatServer(int port)
    {
		/* Initialize the server to listen to the specific Listening_Port */
		this.serverPort = port;
	
		/* Update isRunning to reflect that the server is active */
		this.isRunning = true;

		/* Initalize the HashSet Container of ClientConnections */
		this.clientConnections = new HashSet<ClientConnection>();

		/* Initalize the HashSet Container of BlueChatRoom */
		this.chatRoomsActive = new HashSet<BlueChatRoom>();

		/* Initalize the HashSet Container of Strings logged to the server */
		this.serverLogs = new HashSet<String>();
    }

    /**
    * Start running this thread for the BlueChatServer
    **/
    public void run()
    {
		/* Try to establish a connection with between the ServerSocket and any Client Sockets */
		try
		{	    
	    	/* Print out that the Blue Chat Server is starting up */
	    	System.out.println("Blue Chat Server : Welcome to the Blue Chat Server! Starting up...");

	    	/* Create an instance of John's SHA1 program, which encrypts the password attempt and stores it internally (The encryption of "0" essentially means it's a public room) */
	    	sha1 passwordHash = new sha1("0");

	    	/* Grab this value from SHA1's internal storage for the encrypted text */
	    	String hashed = passwordHash.hashed;

	    	/* Initalize a default main chat server */
	    	BlueChatRoom mainRoom = new BlueChatRoom(100,"Main", true, hashed);

	    	/* Add the main Room to BlueChatServer's collection of active rooms */
	    	chatRoomsActive.add(mainRoom);
	    
	    	/* Bound a server socket to the specified serverPort that we cant */
	    	ServerSocket serverSocket = new ServerSocket(serverPort);
		
	    	/* While the BlueChatServer is active.. */
	    	while(isRunning)
	    	{
				/* Initalize a Socket for the client that the server Socket accepts data to and from*/
				Socket CSocket = serverSocket.accept();
	
				/* Add the client's connection to this socket */
				addClientConnection(CSocket, mainRoom, this);
	    	}
	    	
	    	serverSocket.close();
		}
		catch (Exception e)
		{
	    	/* Error Messages */
	    	System.err.println("An error occured while creating the server socket. " + e.getMessage());
	    	System.err.println(e.getMessage());
		}
    }

    /**
     * Purpose: To transmit all of the clients who logged into this server in logged format, onto a file
     */
    public void sendLogsToFile()
    {    
		/* Creates a file object with a name that is paramterized within the constructor */
		File blueChatServerFile = new File("BlueServerLogs.txt");

        try
        {
	    	/* If the file already exists */
	    	if(!blueChatServerFile.exists())
	    	{
	        	/* Provide output that this is will be creating a new file */
				System.out.println("We had to make a new file.");

				blueChatServerFile.createNewFile();

				// Instantiate a Date object
				Date date = new Date();

				String loggedString = "BlueChatServer Started Up On  " + date.toString();

				/* Initialize a fileWriter object to append and not overwrite any data transmitted to BlueServerLogs.txt */
				FileWriter fileWriter = new FileWriter(blueChatServerFile, true);

				/* Initalize a bufferedWriter that will be used to write additional data to the file */
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
	    
				bufferedWriter.write("*-------- " + loggedString  +"--------* " + "\n");

				/* Initalized iterator which iterates through all of the active chat rooms within the server */
				Iterator<String> iter = serverLogs.iterator();

				while(iter.hasNext())
	        	{	    
		    		/* Get a singleBlueChatRoom wihtin the hashSet via iterator */
		    		String singleLog = (String)iter.next();

		    		bufferedWriter.write(singleLog + "\n");
				}

				bufferedWriter.write("\n*----------------------------------------------------------------------------*\n");
	    	}

	    	// Instantiate a Date object
	    	Date date = new Date();

	    	String loggedString = "BlueChatServer Started Up On  " + date.toString();

	    	/* Initialize a fileWriter object to append and not overwrite any data transmitted to BlueServerLogs.txt */
	    	FileWriter fileWriter = new FileWriter(blueChatServerFile, true);

	    	/* Initalize a bufferedWriter that will be used to write additional data to the file */
	    	BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

	    	bufferedWriter.write("\n");
	    
	    	bufferedWriter.write("*-------- " + loggedString  +" --------* " + "\n\n");

	    	/* Initalized iterator which iterates through all of the active chat rooms within the server */
	   		Iterator<String> iter = serverLogs.iterator();

	    	while(iter.hasNext())
	    	{	    
	        	/* Get a singleBlueChatRoom wihtin the hashSet via iterator */
				String singleLog = (String)iter.next();

				bufferedWriter.write(singleLog + "\n");
	    	}

	    	bufferedWriter.write("\n*----------------------------------------------------------------------------*\n");

	    	/* Close the buffered reader since it will no longer be used */
	    	bufferedWriter.close();

	    	/* Print out to the server that this was successful */
	    	System.out.println("File Was Succesfully Saved");

	    	/* Set the server to stop running and adding clients MY NEED TO CHANGE IN FUTURE */
	    	this.isRunning = false;
		}
		catch(IOException e)
		{
	    	/* Error Messaging */
	    	System.out.println("Could not successfully output BlueChatServer's logged data onto a file");
	    	System.out.println("Error Message : " + e.getMessage());
		}
    }

    /**
     * Purpose : To add a log into the server's container of logs
     * @param clientName The name of the client we are logging information about
     */
    public void addToServerLog(String clientName)
    {
		/* Instantiate a Date object */
       Date date = new Date();

       /* Format this log */
       String loggedString = "[" + clientName + "] Joined The Server On " + date.toString();

       /* Store this log within the serverLog HashSet */
       serverLogs.add(loggedString);

    }

    /**
     * Purpose: To determine if a username is already being used by another client or not
     * @param name The requested name that will be evalated
     * @param Coutput The output stream to the client
     */
    public boolean checkIfAlreadyExist(String name, PrintWriter Coutput)
    {	
		/* Initalized variable that will be returned to determine if this requested name is a duplicate AKA already in use */
		boolean duplicateName = false;
		
		for(ClientConnection client : clientConnections)
		{
	    	/* Temperary variable that determines if the any of the client's don't have a name at the moment */
	    	boolean isEmptyName = false;
	    	
	    	/* If the client hasn't had their names initialized yet */
	    	if(client.clientName == null)
	    	{
				/* Set the temperary variable to be true in this case */
				isEmptyName = true;
	    	}

	    	/* If there exist a name already in use that matches the requested text of the client */
	    	if(isEmptyName != true)
	    	{
				if(client.clientName.toUpperCase().equals(name.toUpperCase()))
				{
		    		/* Set flag to be true i.e this name is already in use */
		    		duplicateName = true;
				}
	   	 	} 
		}

		/* Return the result of whether this name is already in use or not */
		return duplicateName;
    }

    public void initializePrivateMessaging(String clientUsername, String clientMessage, ClientConnection clientRequestingPMessage)
    {
		/* Iterator variable that will be used to search for the specific client's username */
		Iterator<ClientConnection> iter = clientConnections.iterator();

		/* Variable that returns the actual client that was searched for, or null if the search failed */
		ClientConnection searchedClient = null;

		/* Iterator through the ClientConnections withi the server */
		while(iter.hasNext())
		{
	    	/* Grab a single instance for each cycle of the iteration */
	    	ClientConnection singleClient = (ClientConnection) iter.next();

	    	/* If the username that is being searched for equals one of the client's name */
	    	if(singleClient.clientName.toUpperCase().equals(clientUsername.toUpperCase()))
	    	{
				/* Update variable */
				searchedClient = singleClient;
        	}

        	if(searchedClient != null)
			{
	    		/* Tell the client to set up a window to to converse to the user */
	    		clientRequestingPMessage.sendClientMessage(clientUsername,"PMA");
			}
			else
			{
	    		clientRequestingPMessage.sendClientMessage(clientUsername,"PMD");
			}
    	}
    }

    // String privateMessagingClient = clientMessage.substring(dashLocation+1,clientMessage.length());
    public void sendPrivateMessage(String clientUsername, String clientMessage, String usernameOfMessenger, boolean firstMessageSent)
    {
		/* Iterator variable that will be used to search for the specific client's username */
		Iterator<ClientConnection> iter = clientConnections.iterator();

		ClientConnection clientOfSender = null;
	
		/* Iterator through the ClientConnections withi the server */
		while(iter.hasNext())
		{
		    /* Grab a single instance for each cycle of the iteration */
		    ClientConnection singleClient = (ClientConnection) iter.next();
	
		    if(singleClient.clientName.toUpperCase().equals(clientUsername.toUpperCase()))
		    {
		    	clientOfSender = singleClient; 
		    }
	
		    /* If the username that is being searched for equals one of the client's name */
		    if(singleClient.clientName.toUpperCase().equals(clientUsername.toUpperCase()) && singleClient.clientName.toUpperCase().equals(usernameOfMessenger.toUpperCase()) != true)
		    {
		    	if(firstMessageSent)
		    	{
		    		singleClient.sendClientMessage(usernameOfMessenger + "-" + clientMessage + "@" + clientUsername, "PMU[T]");
		    	}
		    	else
		    	{
		    		singleClient.sendClientMessage(usernameOfMessenger + "-" + clientMessage + "@" + clientUsername, "PMU[F]");
		    	}
		    }
	     }
    }

    /**
     * Purpose: To create a new thread associated with the client connection, and store the thread within HashSet
     * @param Csocket The client socket to associate with the connection thread.
     * @param defaultRoom The default room which the client is automatically initialized in
     */
    public void addClientConnection(Socket Csocket, BlueChatRoom defaultRoom, BlueChatServer Bserver)
    {	
    	/* Store the Client socket's address*/
        String clientSocketInetAddress = Csocket.getInetAddress().toString();

        /* Reflect This Connection on the Server Side */
        System.out.println("Blue Chat Server : Connecting to client: " + clientSocketInetAddress);

        /* Initialize a new Connection for the Client */
        ClientConnection Cconnection = new ClientConnection(Csocket, clientSocketInetAddress, Bserver);

        /* Initialize the Client Connection to start in defualtRoom */
        Cconnection.setActiveRoom(defaultRoom, true);

        /* Start the client's thread */
        (new Thread(Cconnection)).start();
    }

    /**
     * Purpose: To close the server down and in turn, tell all of the other client's to close their respective applications down as well
     */
    public void closeServer()
    {
		/* If the sever is attempting to close when their are client within it.. */
		if(clientConnections.size() >= 1)
		{
		    /* Iterate through all active clients within the BlueChatServer */
		    for(Iterator<ClientConnection> I = clientConnections.iterator(); I.hasNext();)
		    {
			/* Get a singleClient via iterator */
			ClientConnection singleClient = (ClientConnection) I.next();
	
			/* Send a message to each client telling them to close their GUI applications */
			singleClient.sendClientMessage("Server Has Been Closed", "SD");
	
			/* Then handle the disconnection of each client on the server side */
			closeConnectionToClient(singleClient);
		    }
	
		    /* Clear the hashset with all of the active rooms within the BlueChatServer */ 
		    chatRoomsActive.clear();
		    
		    /* Close the BlueChatServer Application */
		    System.exit(1);
		}
		/* In the case where the server is closing itself without any clients */
		else
		{
		    /* Clear the hashset with all of the active rooms within the BlueChatServer */ 
		    chatRoomsActive.clear();
		    
		    /* Close the BlueChatServer Application */
		    System.exit(1);
		}
    }

    /**
     * Purpose: To close down the specified client's connection to the server
     * @param client The client who's connection you wish to have removed from the server
     */
    public void closeConnectionToClient(ClientConnection client)
    {
		client.currentChatRoom.clientsWithinRoom.remove(client);
		client.currentChatRoom = null;
		client.isClientInChat = false;
		
		try
		{	
		    client.clientSocket.close();
		    client.clientOutput.close();
		    client.clientInput.close();
		}
		catch (IOException e)
		{
		    System.out.println("Error When Closing Down Client Sockets From Server");
		    System.out.println("Error Message: " + e.getMessage());
		}
    }

    /**
     * Purpose: To process the user's input into chat room format and send this processedInput to be handled by the specifc room
     * @param clientMessage The client message that will be displayed
     * @param clientName The client's name
     * @param activeRoom The room that the client is currently being displayed
     **/
    public synchronized void processClientInput(String clientMessage, String clientName, BlueChatRoom activeRoom)
    {
		// Instantiate a Date object
		Date date = new Date();

		/* Set variables to hold the current number of seconds, minutes, and hour of when the client sends this message */
		String seconds = "" + date.getSeconds();
		String minutes = "" + date.getMinutes();
        String hours = "" + date.getHours();

		/* Handle the case of formatting the client's timestamp to always be in the format of 00::00::00 */
		if(date.getHours() < 10)
		{
		    hours = "0" + date.getHours();
		}

		if(date.getMinutes() < 10)
		{
		    minutes = "0" + date.getMinutes();
	        }

		if(date.getSeconds() < 10)
		{
		    seconds = "0" + date.getSeconds();
		}	
		
		/* Process the Input into chat room format */
		String processedInput = hours + "::" + minutes + "::" + seconds + " [" + clientName + "] : " + clientMessage;

		/* Process Input Into Chat Room */
		EnterInputIntoChat(processedInput, activeRoom);
    }

    /**
     * Purpose : To format unformatted text (text with unnessacary spaces) 
     * @param unformattedText The text that will be formatted and returned
     */
    public String formatClientString(String unformattedText)
    {
		String formattedText = unformattedText.replaceAll(" ", "");
		return formattedText;
    }

    /**
     * Purpose: To process the client's command and then perform the client's command
     * @param line The string that the client's sends to the server
     * @param Croom The current room that the client is in
     * @param clientName The name of the client
     * @param Coutput The client's output stream (PrintWriter)
     * @param Cinput The clients input stream (BufferedReader)
     */
    private void processLine(String line, ClientConnection client)
    {
		/* If the user is types in the ADDROOM command */
        if (line.toUpperCase().startsWith("ADDROOM-"))
		{
		    /* Parse the client's message to obtain only the relavent information (roomname-password) */
		    String clientMessage = line.substring(8,line.length());

		    /* Format the string to take into account any unnessacary spaces */
		    String formattedString = formatClientString(clientMessage);

		    /* Calculate the dash location, needed in order to parse the client message's room name and password request */
		    int dashLocation = formattedString.indexOf("-");

		    /* Parse the formatted string to contain only the name of the room requested by the client */
		    String blueRoomName = formattedString.substring(0,dashLocation);
		    
		    /* Parse the formatted string to contain only the password requested by the client */
		    String password = formattedString.substring(dashLocation + 1);

		    /* Determines when the user has successfully been able to re-adjust their room */
		    boolean roomAlreadyExist = false;

		    /* Process passwords with no password in this defualt manner */
		    if(password.equals(""))
		    {
				password = "0";
		    }

		    for(BlueChatRoom oneRoom : chatRoomsActive)
		    {
		    	/* If this is the room that the user is trying to display their conversation at.. */
				if(oneRoom.roomName.equals(blueRoomName) && !roomAlreadyExist)
			    {
				    /* Set the boolean flag to false in order to short circuit this while loop, thus saving time */
				    roomAlreadyExist = true;
				}
		    }
		    
		    /* If no rooms contain this name, create it */
		    if(!roomAlreadyExist)
		    {
			    /* Create an instance of John's SHA1 program, which encrypts the password attempt and stores it internally */
				sha1 passwordHash = new sha1(password);

			    /* Grab this value from SHA1's internal storage for the encrypted text */
				String hashed = passwordHash.hashed;

			    /* Set actual password attempt to be this SHA1 encryption, since room's password are stored in as a SHA1 encryption */
				password = hashed;
				
				/* Create this newly BlueChatRoom and add it to BlueChatServer's container of activeRooms */
				chatRoomsActive.add(new BlueChatRoom(100,blueRoomName,false,password));

				client.sendClientMessage("The Room was successfully created", "ARA");
		    }
		    /* If there exist a room, inform the user that this room is already in us and DO NOT CREATE A ROOM */
		    else
		    {
				/* Send the client a error message */
		        client.sendClientMessage("RNAIU", "E");
		    }
		}
		/* If a user sends the server a message that they are quiting their connection to the server..  */
		else if (line.toUpperCase().startsWith("QUIT-"))
	        {
		    /* Close the connection of the client */
		    closeConnectionToClient(client);
	
		    /* Update Logs */
		    sendLogsToFile();
		    
		}
		/* If the user is attempting to send a private message to a specific user */
		else if (line.toUpperCase().startsWith("PMESSAGE-"))
		{
		    /* Parse the client's message to obtain only the relavent information (username to private message) */
		    String clientMessage = line.substring(9,line.length());
	
		    if(!client.clientName.toUpperCase().equals(clientMessage.toUpperCase()))
		    {
				/* Find the private user the client is trying to send a private message to */
				client.server.initializePrivateMessaging(clientMessage,"PMR",client);
		    }
		    else
		    {
				client.sendClientMessage("Why did you want to private message yourself?","E");
		    }
		}
		else if(line.toUpperCase().startsWith("PMESSAGECLIENT[T]-"))
		{
		    /* Initialize the variable to hold client related data sent to the server */
		    String clientMessage = line.substring(18,line.length());
	
		    /* Calculate the dash location */
		    int dashLocation = clientMessage.indexOf("-");
	
		    /* Initialize the variable to hold the client's message to the Private Messaged Client */
		    String privateMessagingContents = clientMessage.substring(0,dashLocation);
	
		    /* Initialize the variable to hold the person who this message is being sent to */
		    String privateMessagingClient = clientMessage.substring(dashLocation+1,clientMessage.length());
	
		    /* Send the private Message to the specific user */
		    client.server.sendPrivateMessage(privateMessagingClient, privateMessagingContents, client.clientName, true);
		}
		else if(line.toUpperCase().startsWith("PMESSAGECLIENT[F]-"))
		{
		    /* Initialize the variable to hold client related data sent to the server */
		    String clientMessage = line.substring(18,line.length());
	
		    /* Calculate the dash location */
		    int dashLocation = clientMessage.indexOf("-");
	
		    /* Initialize the variable to hold the client's message to the Private Messaged Client */
		    String privateMessagingContents = clientMessage.substring(0,dashLocation);
	
		    System.out.println("[SERVER] Private Messege Is : " + privateMessagingContents);
	
		    /* Initialize the variable to hold the person who this message is being sent to */
		    String privateMessagingClient = clientMessage.substring(dashLocation+1,clientMessage.length());
	
		    System.out.println("[SERVER] Client Private Messaging To : " + privateMessagingClient);
	
		    /* Send the private Message to the specific user */
		    client.server.sendPrivateMessage(privateMessagingClient, privateMessagingContents, client.clientName, false);
		    //client.server.sendPrivateMessage(client.clientName, privateMessagingContents, privateMessagingClient, false);
		}
		else if (line.toUpperCase().startsWith("PMESSAGECLIENTCLOSE-"))
		{
		    String sString = line.substring(20,line.length());
		    int dashLocation = sString.indexOf("-");
	
		    String clientOne = sString.substring(0,dashLocation);
		    String clientTwo = sString.substring(dashLocation+1, sString.length());	    
	
		    client.server.findClient(clientOne).sendClientMessage(clientOne + "-" + clientTwo,"PMC");
		}
		/* If the client types in anything besides the specified command, because it's handled in a different function, just process is as a simple message */
		else if (!line.toUpperCase().startsWith("JOINROOM-"))
		{
		    /* Send the client's message to the user specified room */
		    processClientInput(line, client.clientName, client.currentChatRoom);
		}
    } // End of the processLine function

    /**
     * Purpose: To determine if the room the client has requested has a password or not
     * @param line The user's command line
     * @param client The client that we're processing input from
     */
    public synchronized BlueChatRoom doesRoomExist(String line, ClientConnection client)
    {
		/* Pointer that is returned if the room can be found or not */
		BlueChatRoom roomToJoin = null;
		
		/* If the user types in a command to join a specific room */
        if(line.toUpperCase().startsWith("JOINROOM-"))
		{
		    /* Parse the client's message to obtain only the relavent information (roomname-password) */
		    String clientMessage = line.substring(9,line.length());

		    /* Format the string to take into account any unnessacary spaces */
		    String formattedString = formatClientString(clientMessage);

		    /* Calculate the dash location, needed in order to parse the client message's room name and password request */
		    int dashLocation = formattedString.indexOf("-");

		    /* Parse the formatted string to contain only the name of the room requested by the client */
		    String blueRoomName = formattedString.substring(0,dashLocation);
		    
		    /* Parse the formatted string to contain only the password requested by the client */
		    String password = formattedString.substring(dashLocation + 1);
		    
		    for(BlueChatRoom singleRoom : chatRoomsActive)
		    {
				/* If there exist a room that matches the user's requested room's name */
				if(blueRoomName.toUpperCase().equals(singleRoom.roomName.toUpperCase()))
				{
			    	roomToJoin = singleRoom;
				}
		    }
	    
		    /* If the user tries to join a room that was never creeated */
		    if(roomToJoin == null)
		    {
				/* Inform that user via Error Messeging that this room isn't an active room */
				client.sendClientMessage("The room isn't active", "JRF");
		    }

	    	/* Return whether there exist a room or not in the case the user types in the correct command */
	    	return roomToJoin;
		}
		/* In case the room doesn't have a password.. */
		else
    	{	    
		    /* Return null in the case the user didn't type in the correct command, handled where function is called */
		    return roomToJoin;
		}
	}

    /**
     * Purpose: To identify which room's contents must this message be displayed in, and then display send their conversation to everyone in the room
     * @param processedInput The formated client message
     * @param activeRoom The room that the client is currently being displayed
     **/
    public synchronized void EnterInputIntoChat(String proccesedInput, BlueChatRoom currentRoom)
    {	
		for(BlueChatRoom singleRoom : chatRoomsActive)
		{
		    /* If this is the room that the user is trying to display their conversation at.. */
		    if(singleRoom.roomName.equals(currentRoom.roomName))
		    {
				/* Pass along the user's conversation to be handled by the specific room  */
				currentRoom.addConversationToRoom(proccesedInput, currentRoom);
		    }
		}
    }

    /**
     * Purpose: To return the specified string's ClientConnection 
     * @param clientName The name of the client to find 
     **/
    public ClientConnection findClient(String clientName)
    {
		ClientConnection searchedClient = null;
		for(ClientConnection client : clientConnections)
		{
	    	if(client.clientName.toUpperCase().equals(clientName.toUpperCase()))
	    	{
				searchedClient = client;
	   		}
		}

		return searchedClient;
    }
    
    /**
     * The main thread of the server. It processes command line arguments and starts
     * an instance of the BlueChatServer and runs it
     **/
    public static void main(String[] args)
    {
		int port = Default_Port;
	
		BlueChatServer server = new BlueChatServer(port);
		(new Thread(server)).start();
    }
}
