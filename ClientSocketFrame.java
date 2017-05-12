import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;

import javax.swing.JOptionPane;

import java.util.*;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;

import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable; 

import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import java.lang.Object;
import java.lang.Class;

import javax.sound.sampled.*;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JRootPane;

public class ClientSocketFrame implements ActionListener
{
    /* A variable that holds the dimensions of the screen size of the machine */
    public static Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    
    /**
     * PrivateMessageWindow 
     * Purpose: To handle private messaging window request both to and from the server dynamically
     */
    public static class PrivateMessageWindow implements ActionListener
    {
		/* A variable that holds an instance of the Client's Application*/
		ClientSocketFrame instanceOfClientFrame;

		/* A variable that holds the reciever's name */
		String recieverName;

		/* A variable that holds the reciever's name */
		String senderName;

		/* A JFrame container that holds J-Based GUI Objects*/
		JFrame pMessageWindowJFrame;

		/* A JTextField variable that handles all input typed in from the client */
		JTextField pMessagWindowText;

		/* A JButton variable that handles sending a client's message via button */
		JButton pMessageWindowSendButton;

		/* A JTextArea variable that holds information that is to be displayed to the client */
		public JTextArea pMessageWindowDisplayedText;

		boolean firstMessage;

		/**
	 	* Basic Constructor
	 	* @param winWidth The window width for this window
	 	* @param winHeight The window's height
	 	* @param sender The Name of the send (The local client's username)
	 	* @param reciever The name of the reciever who recieve these private messaging messages 
	 	* @param clientFrame The client's Socket Frame
	 	*/
		public PrivateMessageWindow(int winWidth, int winHeight, String sender, String reciever, ClientSocketFrame instance)
		{
	    	/* Initialzie GUI Based Components */
	    	initComponents(winWidth,winHeight,reciever, sender);

	    	/* Update variable that points to the client's instance of their program (ClientSocketFrame) */
	    	this.instanceOfClientFrame = instance;

	    	/* Update variable to hold the name of the reciever */
	    	this.recieverName = reciever;

	    	/* Update variable to hold the name of the sender */
	    	this.senderName = sender;

	    	firstMessage = true;
		}

	/**
	 * Purpose : To Initialize GUI Based Components
	 * @param winWidth The window width for this window
	 * @param winHeight The window's height
	 * @param reciever The name of the reciever who recieve these private messaging messages 
	 */
	public void initComponents(int _winWidth, int _winHeight, String _reciever, String _sender)
	{
		this.pMessageWindowJFrame = new JFrame("Private Message Frame");
		this.pMessageWindowJFrame.setTitle("Private Message Window : " + _sender + " and " + _reciever);
		this.pMessageWindowJFrame.setName("Private Message Window : " + _sender + " and " + _reciever);
		this.pMessageWindowJFrame.setSize(_winWidth, _winHeight);
		this.pMessageWindowJFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.pMessageWindowJFrame.getContentPane().setLayout(null);
		this.pMessageWindowJFrame.setLocation(dim.width/2-pMessageWindowJFrame.getSize().width/3, dim.height/2-pMessageWindowJFrame.getSize().height/3);
	
		this.pMessageWindowDisplayedText = new JTextArea();
		this.pMessageWindowDisplayedText.setBounds(10,10,480,400);
		this.pMessageWindowDisplayedText.setEditable(false);
		this.pMessageWindowJFrame.add(pMessageWindowDisplayedText);
	
		this.pMessagWindowText = new JTextField();
		this.pMessagWindowText.setBounds(7,410,370,50);
		this.pMessageWindowJFrame.add(pMessagWindowText);
	
		this.pMessageWindowSendButton = new JButton("Send Message");
		this.pMessageWindowSendButton.setName("Private Button");
		this.pMessageWindowSendButton.setBounds(375,410,120,50);
		this.pMessageWindowSendButton.addActionListener(this);
		this.pMessageWindowJFrame.add(pMessageWindowSendButton);
	
		this.pMessageWindowDisplayedText.setName("Private Window Displayed Text");
		this.pMessageWindowJFrame.setVisible(true);
	
		this.pMessageWindowJFrame.addWindowListener(new java.awt.event.WindowAdapter()
		{
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent)
			{
			    if (JOptionPane.showConfirmDialog(pMessageWindowJFrame,  "Are you sure to close this window?", "Really Closing?",  JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
			    {
			        instanceOfClientFrame.sendMessageToServer("PMessageClientClose-" + recieverName + "-" + senderName);
			    }
			}
		    });
		}
	
		@Override
		public void actionPerformed(ActionEvent e)
		{
		    if(e.getSource().equals(pMessageWindowSendButton))
		    {
		    	this.pMessageWindowDisplayedText.append("[You] : " + pMessagWindowText.getText() + "\n");
	
				if(firstMessage != false)
				{
				    this.instanceOfClientFrame.sendMessageToServer("PMESSAGECLIENT[T]-" + pMessagWindowText.getText() + "-" + recieverName);
				    firstMessage = false;
			    }
				else
				{	
				    this.instanceOfClientFrame.sendMessageToServer("PMESSAGECLIENT[F]-" + pMessagWindowText.getText() + "-" + recieverName);
			    }
		    }
		}
    } /* End of the PrivateMessagingWindow */
    
    public static class ClientInvalidInputWindow implements ActionListener
    {
		JFrame clientNotificationJFrame;
		JLabel clientNotificationLabel;
		JButton closeButton;
	
		public ClientInvalidInputWindow(String textDisplayed)
		{
		    initComponents(textDisplayed);
		}
	
		public void initComponents(String _textDisplayed)
		{
		    clientNotificationJFrame = new JFrame("Invalid Input Frame");
		    clientNotificationJFrame.setTitle("Error : Invalid Input");
		    clientNotificationJFrame.setSize(250, 250);
		    clientNotificationJFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		    clientNotificationJFrame.getContentPane().setLayout(null);
		    clientNotificationJFrame.setLocation(dim.width/2-clientNotificationJFrame.getSize().width/3, dim.height/2-clientNotificationJFrame.getSize().height/3);
	
		    clientNotificationLabel = new JLabel(_textDisplayed);
		    clientNotificationLabel.setBounds(10,10, 150,100);
		    clientNotificationJFrame.add(clientNotificationLabel);
	
		    closeButton = new JButton("Close");
		    closeButton.setBounds(100,50,100,20);
		    closeButton.addActionListener(this);
		    clientNotificationJFrame.add(closeButton);
	
		    clientNotificationJFrame.setVisible(true);
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
		    System.out.println("ClientInvalidInputWindow Action Called");
		    clientNotificationJFrame.dispatchEvent(new WindowEvent(clientNotificationJFrame, WindowEvent.WINDOW_CLOSING));
		}
    }
    
    public static class ClientSubThread implements Runnable
    {
		public int port; // Port the user will connect at 
		public ClientSocketFrame clientGUI; // Reference Client's GUI
		public String host; // Hostname 
		public Socket clientSocket; // Socket object 
		
		// Buffers used for sending and recieving data from the server 
		public BufferedReader serverResponse;
		public static PrintWriter clientInput;
		
		
		public static String clientUsername;
		public File messageWav = null;
		public File privateWav = null;
		public boolean isThreadActive;
	
		public ClientSubThread(String hostName, int portNumber, ClientSocketFrame CGUI, String mWav, String pWav)
		{
		    this.host = hostName;
		    this.port = portNumber;
		    this.isThreadActive = true;
		    this.clientGUI = CGUI;
		    this.messageWav = new File(mWav);
		    this.privateWav = new File(pWav);
		}
	
		public void run()
		{
		    try
		    {		
		    	clientSocket = new Socket(host, port);
		    	clientInput = new PrintWriter(clientSocket.getOutputStream(), true);
		    	serverResponse = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		        unconnectedGUI.dispose();
		        usernameInquiryGUI.setVisible(true);
			
				while(isThreadActive)
				{
				    /* Response from the user program */
				    String SResponse = serverResponse.readLine();
		
				    if((SResponse.equals(null) || SResponse.equals("")) == false)
				    {
					   if(SResponse.startsWith("BLUESERVER[E]")) // Error
					   {			       
					       // Upload a notification window displaying the server's response
					       new ClientInvalidInputWindow(SResponse.substring(14, SResponse.length()));
					   }
					   else if (SResponse.startsWith("BLUESERVER[M]"))
					   {				   			       
					       // Upload a notification window displaying the server's response
					       recieveMessageText.append(SResponse.substring(14, SResponse.length()) + "\n");
					       playSound(messageWav);
					   }
					   else if (SResponse.startsWith("BLUESERVER[UA]")) // User Authenticated 
					   {
					       /* Initialize the clients username */
					       clientUsername  = SResponse.substring(15, SResponse.length());
		
					       /* Dispose of the Username Inquiry GUI */
					       usernameInquiryGUI.dispose();
		
					       /* Start displaying the connected GUI interface for the client to communicate to the server */
					       connectedGUI.setVisible(true);
				           }
					   else if (SResponse.startsWith("BLUESERVER[ARA]")) // Add Room Accepted
					   {
					       String serverResponseText = SResponse.substring(15,SResponse.length());
					       addRoomInquiryGUI.dispose();
					   }
					   else if (SResponse.startsWith("BLUESERVER[JRA]")) // Join Room Accepted
					   {
					       String newRoomText = SResponse.substring(15,SResponse.length());
					       connectedGUI.setTitle(newRoomText + " Room");
					       recieveMessageText.setText("");
					   }
					   else if (SResponse.startsWith("BLUESERVER[PMA]")) // Private Messege Accepted
					   {
					       String serverResponseText = SResponse.substring(16,SResponse.length());
					       privateMessageInquiryGUI.setVisible(false);
					       new PrivateMessageWindow(500,500, clientUsername, serverResponseText, clientGUI);
					       playSound(privateWav);
					   }
					   else if (SResponse.startsWith("BLUESERVER[PMU[T]]")) // Private Messege User (True)
					   {
					       // 
			        	       String serverResponseText = SResponse.substring(18, SResponse.length());
					       privateMessageInquiryGUI.setVisible(false); 
					       
					       /* Calculate the dash location */
					       int dashLocation = serverResponseText.indexOf("-");
					       int atLocation = serverResponseText.indexOf("@");
		
					       /* Initialize the variable to hold the client's message to the Private Messaged Client */
					       String sentClientUsername = serverResponseText.substring(1,dashLocation);
		
					       /* Initialize the variable to hold the person who this message is being sent to */
					       String sentClientContents = serverResponseText.substring(dashLocation+1, atLocation);
		
					       String recieverClientUsername = serverResponseText.substring(atLocation + 1, serverResponseText.length());
					       //System.out.println("Reciever Clinet Username : " + recieverClientUsername);
		
					       PrivateMessageWindow instance = new PrivateMessageWindow(500,500, recieverClientUsername, sentClientUsername, clientGUI);
		
					       playSound(privateWav);
		
					       instance.firstMessage = false;
		
					       instance.pMessageWindowDisplayedText.append("[" + sentClientUsername + "] : " + sentClientContents + "\n");
					   }
					   else if (SResponse.startsWith("BLUESERVER[PMU[F]]")) // Private Messege User (True)
					   {
			        	   String serverResponseText = SResponse.substring(18, SResponse.length());
					       privateMessageInquiryGUI.setVisible(false);
					       playSound(privateWav);
					       
					       /* Calculate the dash location */
					       int dashLocation = serverResponseText.indexOf("-");
		
					       /* Calculate the @ location */
					       int atLocation = serverResponseText.indexOf("@");
		
					       /* Initialize the variable to hold the client's message to the Private Messaged Client */
					       String sentClientUsername = serverResponseText.substring(1,dashLocation);
		
					       /* Initialize the variable to hold the person who this message is being sent to */
					       String sentClientContents = serverResponseText.substring(dashLocation+1, atLocation);
		
					       String recieverClientUsername = serverResponseText.substring(atLocation + 1, serverResponseText.length());
		
					       /* Grab all of the active frames within the application */
					       Frame[] activeFrames =  Frame.getFrames();
		
					       /* Iterate through these frames... */
					       for(int i = 0; i < activeFrames.length; i++)
					       {
							   /* If one of those frames is the Private Messaging Window of the sender and reciever */
							   if(activeFrames[i].getTitle().startsWith("Private Message Window : " + recieverClientUsername + " and " + sentClientUsername))
							   {
							       /* Find and typecase the component to be the JTextField within the frame */
							       JTextArea pDisplayedText = (JTextArea) findComponentByName(activeFrames[i], "Private Window Displayed Text");
							       
							       /* Safety Check - If it is able to find something...  */
							       if(pDisplayedText != null)
							       {
									   /* Append the text from the reciever into the sender's displayed Text field within the Private Messaging Window*/
									   pDisplayedText.append("[" + sentClientUsername + "] : " + sentClientContents + "\n");
									   playSound(messageWav);
							       }
						  		}
					       }
					   }
					   else if(SResponse.startsWith("BLUESERVER[PMC] " + clientUsername))
					   {
					       String sString = SResponse.substring(16, SResponse.length());
					       int dashLocation = sString.indexOf("-");
					       String senderName = sString.substring(0, dashLocation);
					       String recieverName = sString.substring(dashLocation+1, sString.length());
		
					       /* Grab all of the active frames within the application */
					       Frame[] activeFrames =  Frame.getFrames();
		
					       /* Iterate through these frames... */
					       for(int i = 0; i < activeFrames.length; i++)
					       {
						   /* If one of those frames is the Private Messaging Window of the sender and reciever */
						   if(activeFrames[i].getTitle().startsWith("Private Message Window : " + senderName  + " and " + recieverName))
						   {
						       /* Find and typecase the component to be the JTextField within the frame */
						       JTextArea pDisplayedText = (JTextArea) findComponentByName(activeFrames[i], "Private Window Displayed Text");
						       JButton pButton = (JButton) findComponentByName(activeFrames[i], "Private Button");
		
						       /* Safety Check - If it is able to find something...  */
						       if(pDisplayedText != null)
						       {
							   System.out.println("sdfsdfsd");
							   /* Append the text from the reciever into the sender's displayed Text field within the Private Messaging Window*/
							   pDisplayedText.append("[" + recieverName + "] : Is No Longer Private Messaging You \n");
						       }
		
						       if(pButton != null)
						       {
							   pButton.setEnabled(false);
							   System.out.println("Did shit too button");
						       }
						   }
					       }
					   }
				    }  
				}
		    }
		    catch (IOException e)
		    {
		    	System.out.println("An Error Occured via IOException");
		    	System.out.println(e.getMessage());
		    }
		}
	
		public Component findComponentByName(Container container, String componentName)
		{
		    for (Component component: container.getComponents())
		    {
		        if(componentName.equals(component.getName()))
		        {
		        	return component;
		        }
		    
		        if (component instanceof JRootPane)
		        {
		        	JRootPane nestedJRootPane = (JRootPane)component;
		        	return findComponentByName(nestedJRootPane.getContentPane(), componentName);
		        }
		    }
		    return null;
		}
	
		public void playSound(File clipToPlay) 
		{
		    try 
		    {
		    	Clip soundClip = AudioSystem.getClip();
		    	soundClip.open(AudioSystem.getAudioInputStream(clipToPlay));
		    	soundClip.start();
		    	Thread.sleep(soundClip.getMicrosecondLength()/1000);	
		    }
		    catch(Exception exception) 
		    {
		    	System.out.println("Some exception occured when calling playSound function");
		    }
		}	
    }

    static JFrame unconnectedGUI, usernameInquiryGUI, connectedGUI, addRoomInquiryGUI, joinRoomInquiryGUI, privateMessageInquiryGUI;
    JLabel hostNameLabel, machinePortLabel, checkUsernameLabel, nameOfRoomLabel, passwordOfRoomLabel, joinRoomPasswordLabel, joinRoomNameLabel, privateMessageUsernameLabel;
    JTextField hostNameText,  machinePortText, sendMessageText, checkUsernameText, nameOfRoomText, passwordOfRoomText, joinRoomPasswordText, joinRoomNameText, privateMessageUsernameText;
    JButton joinServerButton, checkUsernameButton, sendMessageButton, addRoomButton, createRoomButton, joinRoomButton, tryToJoinRoomButton, sendPrivateMessageButton, tryToSendPrivateMessageButton;

    public static JTextArea recieveMessageText;
    public static String currentRoomName;

    public ClientSocketFrame()
    {
    	/******************** [SUB-GUI] Unconncted GUI  *********************/
    	unconnectedGUI = new JFrame ("Server Login Frame");
        unconnectedGUI.setTitle("Join A Server");
        unconnectedGUI.setSize(250, 250);
        unconnectedGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        unconnectedGUI.getContentPane().setLayout(null);
        unconnectedGUI.setLocation(dim.width/2-unconnectedGUI.getSize().width/2, dim.height/2-unconnectedGUI.getSize().height/2);

        hostNameLabel = new JLabel("Host Name: ");
        hostNameLabel.setBounds(10, 10, 90, 21);
        unconnectedGUI.add(hostNameLabel);

        hostNameText = new JTextField();
        hostNameText.setBounds(105, 10, 90, 21);
        unconnectedGUI.add(hostNameText);

        machinePortLabel = new JLabel("Port#: ");
        machinePortLabel.setBounds(10, 35, 90, 21);
        unconnectedGUI.add(machinePortLabel);

        machinePortText = new JTextField();
        machinePortText.setBounds(105, 35, 90, 21);
        unconnectedGUI.add(machinePortText);

        joinServerButton = new JButton("Join");
        joinServerButton.setBounds(35, 60, 150, 21);
        joinServerButton.addActionListener(this);
        unconnectedGUI.add(joinServerButton);

        unconnectedGUI.setVisible(true);
	
        /*******************  [MAIN GUI] Connected GUI  ***********************/

        connectedGUI = new JFrame ("Chat Room Frame");
        connectedGUI.setTitle("Main Room");
        connectedGUI.setSize(500,500);
        connectedGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        connectedGUI.getContentPane().setLayout(null);
        connectedGUI.setLocation(dim.width/2-connectedGUI.getSize().width/2, dim.height/2-connectedGUI.getSize().height/2);

        sendMessageText = new JTextField();
        sendMessageText.setBounds(10,350,390,50);
        connectedGUI.add(sendMessageText);

        recieveMessageText = new JTextArea();
        recieveMessageText.setBounds(10, 10, 480,335);
        recieveMessageText.setEditable(false);
        connectedGUI.add(recieveMessageText);

        sendMessageButton = new JButton("Send Message");
        sendMessageButton.setBounds(400,350,100,50);
        sendMessageButton.addActionListener(this);
        connectedGUI.add(sendMessageButton);

		addRoomButton = new JButton("AddRoom");
		addRoomButton.setBounds(10,400,100,50);
		addRoomButton.addActionListener(this);
		connectedGUI.add(addRoomButton);
	
		joinRoomButton = new JButton("Join Room");
		joinRoomButton.setBounds(110,400,100,50);
		joinRoomButton.addActionListener(this);
		connectedGUI.add(joinRoomButton);
	
		sendPrivateMessageButton = new JButton("Private Message");
		sendPrivateMessageButton.setBounds(210,400,120,50);
		sendPrivateMessageButton.addActionListener(this);
		connectedGUI.add(sendPrivateMessageButton);
		
		connectedGUI.setVisible(false);

		/*******************  [SUB-GUI] Adding Room GUI  ***********************/
	
		addRoomInquiryGUI = new JFrame("Adding Room Inquiry");
		addRoomInquiryGUI.setTitle("Add Room");
		addRoomInquiryGUI.setSize(250,250);
		addRoomInquiryGUI.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		addRoomInquiryGUI.getContentPane().setLayout(null);
	
		nameOfRoomLabel = new JLabel("Room Name: ");
		nameOfRoomLabel.setBounds(10,10,100,40);
		addRoomInquiryGUI.add(nameOfRoomLabel);
	
		nameOfRoomText = new JTextField();
		nameOfRoomText.setBounds(90,20,140,20);
		addRoomInquiryGUI.add(nameOfRoomText);
	
		passwordOfRoomLabel = new JLabel("Password: ");
		passwordOfRoomLabel.setBounds(10,30,100,40);
		addRoomInquiryGUI.add(passwordOfRoomLabel);
	
		passwordOfRoomText = new JTextField();
		passwordOfRoomText.setBounds(70,40,100,20);
		addRoomInquiryGUI.add(passwordOfRoomText);
	
		createRoomButton = new JButton("Create Room");
		createRoomButton.setBounds(75,100,120,40);
		createRoomButton.addActionListener(this);
		addRoomInquiryGUI.add(createRoomButton);
	
		addRoomInquiryGUI.setVisible(false);

		/*******************  [SUB-GUI] JoinRoom GUI  ***********************/
	
		joinRoomInquiryGUI = new JFrame("Join Room Frame");
		joinRoomInquiryGUI.setTitle("Join Room");
		joinRoomInquiryGUI.setSize(250,250);
		joinRoomInquiryGUI.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		joinRoomInquiryGUI.getContentPane().setLayout(null);
	
		joinRoomPasswordLabel = new JLabel("Room's Password: ");
		joinRoomPasswordLabel.setBounds(10,30,150,20);
		joinRoomInquiryGUI.add(joinRoomPasswordLabel);
	
		joinRoomPasswordText = new JTextField();
		joinRoomPasswordText.setBounds(130,30,100,20);
		joinRoomInquiryGUI.add(joinRoomPasswordText);
	
		joinRoomNameLabel = new JLabel("Room's Name: ");
		joinRoomNameLabel.setBounds(10,10,150,20);
		joinRoomInquiryGUI.add(joinRoomNameLabel);
	
		joinRoomNameText = new JTextField();
		joinRoomNameText.setBounds(100,10,100,20);
		joinRoomInquiryGUI.add(joinRoomNameText);
	
		tryToJoinRoomButton = new JButton("Join Room");
		tryToJoinRoomButton.setBounds(100,125,100,30);
		tryToJoinRoomButton.addActionListener(this);
		joinRoomInquiryGUI.add(tryToJoinRoomButton);
	
		joinRoomInquiryGUI.setVisible(false);

		/*******************  [SUB-GUI] Request Username GUI  ***********************/
	
		usernameInquiryGUI = new JFrame ("Username Inquiry Frame");
		usernameInquiryGUI.setTitle("Request Username Window");
		usernameInquiryGUI.setSize(250,250);
		usernameInquiryGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		usernameInquiryGUI.getContentPane().setLayout(null);
	
		checkUsernameLabel = new JLabel("Username: ");
		checkUsernameLabel.setBounds(10,40,100,30);
		usernameInquiryGUI.add(checkUsernameLabel);
	
		checkUsernameText = new JTextField();
		checkUsernameText.setBounds(80,40, 100,50);
		usernameInquiryGUI.add(checkUsernameText);
	
		checkUsernameButton = new JButton("Request Username");
		checkUsernameButton.setBounds(10,80,80,50);
		checkUsernameButton.addActionListener(this);
		usernameInquiryGUI.add(checkUsernameButton);
		usernameInquiryGUI.setLocation(dim.width/2-usernameInquiryGUI.getSize().width/2, dim.height/2-usernameInquiryGUI.getSize().height/2);

		/*******************  [SUB-GUI] Private Message GUI  ***********************/
		
		privateMessageInquiryGUI = new JFrame("Private Message Inquiry Frame");
		privateMessageInquiryGUI.setTitle("Private Message");
		privateMessageInquiryGUI.setSize(250,250);
		privateMessageInquiryGUI.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		privateMessageInquiryGUI.getContentPane().setLayout(null);
	
		privateMessageUsernameLabel = new JLabel("Username: ");
		privateMessageUsernameLabel.setBounds(10,40,100,30);
		privateMessageInquiryGUI.add(privateMessageUsernameLabel);
	
		privateMessageUsernameText = new JTextField();
		privateMessageUsernameText.setBounds(80,40,100,50);
		privateMessageInquiryGUI.add(privateMessageUsernameText);
	
		tryToSendPrivateMessageButton = new JButton("Private Message User");
		tryToSendPrivateMessageButton.setBounds(10,80,80,50);
		tryToSendPrivateMessageButton.addActionListener(this);
		privateMessageInquiryGUI.add(tryToSendPrivateMessageButton);
	
		privateMessageInquiryGUI.setLocation(dim.width/2-usernameInquiryGUI.getSize().width/2, dim.height/2-usernameInquiryGUI.getSize().height/2);
    }


    public static void main(String[] args)
    {
        BlueChatServer.main(new String[0]);
		new ClientSocketFrame();
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource().equals(joinServerButton))
		{
	    	joinServer();
        }
		else if (e.getSource().equals(sendMessageButton))
		{
	    	sendMessageToServer(sendMessageText.getText());
	    	sendMessageText.setText("");
	    	
		}
		else if (e.getSource().equals(checkUsernameButton))
		{
	    	sendMessageToServer((checkUsernameText.getText()));
		}
		else if (e.getSource().equals(addRoomButton))
		{
	    	if(addRoomInquiryGUI.isShowing() != true)
	    	{
			addRoomInquiryGUI.setVisible(true);
	    	}
		}
		else if (e.getSource().equals(createRoomButton))
		{
	    	sendMessageToServer("ADDROOM-" + nameOfRoomText.getText() + "-" + passwordOfRoomText.getText());
        }
		else if (e.getSource().equals(joinRoomButton))
		{
	    	joinRoomInquiryGUI.setVisible(true);
        }
		else if (e.getSource().equals(tryToJoinRoomButton))
		{
	    	sendMessageToServer("JOINROOM-" + joinRoomNameText.getText() + "-" + joinRoomPasswordText.getText());
	    	recieveMessageText.setText("");
	    	joinRoomInquiryGUI.setVisible(false);
        }
		else if (e.getSource().equals(sendPrivateMessageButton))
		{
	    	privateMessageInquiryGUI.setVisible(true);
		}
		else if(e.getSource().equals(tryToSendPrivateMessageButton))
		{
	    	sendMessageToServer("PMESSAGE-" + privateMessageUsernameText.getText());
		}
    }

    public void joinServer()
    {
		(new Thread(new ClientSubThread(hostNameText.getText(), Integer.parseInt(machinePortText.getText()),this, "test.WAV", "test2.WAV"))).start();
    }

    public void sendMessageToServer(String message)
    {
		ClientSubThread.clientInput.println(message);
		//System.out.println("Sent over the following message to the server : " + message);
    }
}
