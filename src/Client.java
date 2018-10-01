import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


public class Client extends JFrame{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private JTextArea userText;
	private JScrollPane textPane;
	private JTextArea chatWindow;
	private ObjectOutputStream output;
	ObjectInputStream input;
	private String message = "";
	private String serverIP;
	private Socket connection;
	private EncryptionKeys keys;
	private boolean keysSent = false;
	private JButton send;
	private JScrollPane chatPane;


	//constructor
	public Client(String host){
		super("Instant Messenger - Client");
		serverIP = host;
		initComponents();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		setVisible(true);
		keys = new EncryptionKeys(1024);

	}

	private void initComponents() {

        chatPane = new javax.swing.JScrollPane();
        textPane = new javax.swing.JScrollPane();
        chatWindow = new javax.swing.JTextArea();
        send = new javax.swing.JButton();
        userText = new javax.swing.JTextArea();
        userText.setLineWrap(true);
        userText.setEditable(false);
        textPane.setViewportView(userText);
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        chatWindow.setEditable(false);
        chatWindow.setColumns(20);
        chatWindow.setRows(5);
        chatPane.setViewportView(chatWindow);

        send.setText("Send");
        send.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendMessage(userText.getText());
                userText.setText("");
            }
        });

        userText.addKeyListener(new KeyAdapter() {

        	public void keyPressed(KeyEvent e) {

        		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
        			send.doClick();


        		}

        	}

        });




        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(chatPane, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(textPane)
                .addGap(18, 18, 18)
                .addComponent(send)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(chatPane, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(textPane, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(send, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 23, Short.MAX_VALUE))
        );

        pack();
        send.setEnabled(false);
	}

	//connect to server
	public void startRunning(){

		try{
			connectToServer();
			setupStreams();
			exchangeKeys();
			whileChatting();
		}catch(EOFException eofEx){
			showMessage("\nClient Terminated connection");
		}catch(IOException ioEx){
			showMessage("\nCould not connect to server.");
		}finally{
			closeDown();
		}
	}
	//send keys
	private void exchangeKeys(){
		try{
			output.writeObject("n" + keys.nLocal);
			output.flush();
		}catch(IOException IOEx){
			chatWindow.append("\n ERROR - Message sending error.");
		}
		try{
			output.writeObject("e" + keys.eLocal);
			output.flush();
		}catch(IOException IOEx){
			chatWindow.append("\n ERROR - Message sending error.");
		}
	}

	private void connectToServer() throws IOException{
		showMessage("Attempting connection... ");
		connection = new Socket(InetAddress.getByName(serverIP), 6789);
		showMessage("\nConnected to "+ connection.getInetAddress().getHostName());
	}

	//setup streams to send/receive messages
	private void setupStreams() throws IOException{
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		input = new ObjectInputStream(connection.getInputStream());
		showMessage("\nOnline");
	}

	//while chatting with server
	private void whileChatting() throws IOException{
		ableToType(true);
		do{
			try{
				message = (String) input.readObject();
				if(!keysSent){
					if(message.substring(0, 1).equals("n")){
						try{
						keys.nForeign = new BigInteger(message.substring(1, message.length()));
						}catch(NumberFormatException nfEx){
							showMessage("Error sending keys");
						}
					}
					else if(message.substring(0, 1).equals("e")){
						try{
						keys.eForeign = new BigInteger(message.substring(1, message.length()));
						keysSent = true;

						}catch(NumberFormatException nfEx){
							showMessage("Error sending keys");
						}
					}
					continue;
				}
				showEncryptedMessage(message);
			}catch(ClassNotFoundException cnfEx){
				showMessage("\nUser Sending error");
			}
		}while(!message.equals("SERVER - END"));
	}

	//close streams/sockets
	private void closeDown(){
		showMessage("\nClosing down...");
		ableToType(false);
		try{
			output.close();
			input.close();
			connection.close();
		}catch(IOException ioEx){
			ioEx.printStackTrace();
		}catch(NullPointerException npEx){

		}
		showMessage("\nClosed.");

	}

	//send messages to server
	private void sendMessage(String message){
		if(message.equals("")){
			return;
		}
		try{
			String x = RSA.encrypt(message, keys.nForeign, keys.eForeign);
			output.writeObject(x);
			output.flush();
			showMessage("\nCLIENT - " + message);

		}catch(IOException ioEx){
			chatWindow.append("\nMessage sending error");
		}catch(Exception e){
			e.printStackTrace();
			chatWindow.append("\nEncryption error");
		}
	}

	//update chatwindow
	private void showMessage(final String text){
		SwingUtilities.invokeLater(
				new Runnable(){
					public void run(){

						chatWindow.append(text);


					}
				}
			);
	}
	private void showEncryptedMessage(final String text){
		SwingUtilities.invokeLater(
				new Runnable(){
					public void run(){
						try{
							chatWindow.append("\nSERVER - " + RSA.decrypt(text, keys.nLocal, keys.d));
						}catch(Exception e){
							e.printStackTrace();
							chatWindow.append("Decryption error");
						}
					}
				}
			);
	}



	//allows user to type into text box
	private void ableToType(final boolean b){
		SwingUtilities.invokeLater(
				new Runnable(){
					public void run(){
						send.setEnabled(b);
						userText.setEditable(b);

					}
				}
			);
	}

}
