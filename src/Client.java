import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Client {
	Socket requestSocket;           //socket connect to the server
	ObjectOutputStream out;         //stream write to the socket
 	//ObjectInputStream in;          //stream read from the socket
	String message;                //message send to the server
	String MESSAGE;                //capitalized message read from the server

	public Client() {}

	void run(String name)
	{
		try{
			//create a socket to connect to the server
			requestSocket = new Socket("localhost", 8001);
			System.out.println("Connected to localhost in port 8001");
			//initialize inputStream and outputStream
			
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();
			//in = new ObjectInputStream(requestSocket.getInputStream());
			
			new Creader(requestSocket).start();
			
			//sending name first
			sendMessage(name);
			System.out.println("Thread started, Going ahead");
			//get Input from standard input
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			while(true)
			{
				System.out.print("Hello, please input a sentence: ");
				//read a sentence from the standard input
				message = bufferedReader.readLine();
				//Send the sentence to the server
				
				
        		String[] command = null;
        		
        		if(message.contains(" ")){
        		command = message.split(" ");}
               
                
        		if(command[1].equalsIgnoreCase("file"))
                {
                	//To do SendFile
        			String filePath = command[2].substring(1, command[2].length()-1);
                    
                	if(command[0].equalsIgnoreCase("broadcast"))
                	{
                		
                		sendMessage(command[0] + " "+command[1]+" " + filePath);
                    	sendFile(filePath);
                		
                	}else{
                		if(command[0].equalsIgnoreCase("unicast"))
                		{
                			//todo check if command[3] exists
                			sendMessage(command[0] + " "+command[1] +" "+ filePath +" "+ command[3]);
                        	sendFile(filePath);
                			
                		}else
                		{
                			if(command[0].equalsIgnoreCase("blockcast"))
                			{
                				sendMessage(command[0] + " "+command[1]+" " + filePath + " " + command[3]);
                            	sendFile(filePath);
                				
                			}
                		}
                	}
                
                	
                	
                	
                	
                	//File file = new File(filePath);
                	
                	
                	
                }else
                {
                	if(command[0].equalsIgnoreCase("blockcast")||command[0].equalsIgnoreCase("unicast")||command[0].equalsIgnoreCase("broadcast"))
                	{
                		sendMessage(message);
                	}else
                	{
                		System.out.println("There might be some error in what you typed.");
                		System.out.println("Your options are : \n1)Broadcast <..> \n2)Blockcast <..> \n3)Unicast <..> \nOptions are : <file> or <message>\n");
                	}
                	
                }
				//Receive the upperCase sentence from the server
				//MESSAGE = (String)in.readObject();
				//show the message to the user
				//System.out.println("Receive message: " + MESSAGE);
			}
		}
		catch (ConnectException e) {
    			System.err.println("Connection refused. You need to initiate a server first.");
		} 
		catch(UnknownHostException unknownHost){
			System.err.println("You are trying to connect to an unknown host!");
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
		finally{
			//Close connections
			try{
				//in.close();
				out.close();
				requestSocket.close();
				
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
	}


    //send a message to the output stream
	void sendMessage(String msg)
	{
		try{
			//stream write the message
			out.writeObject(msg);
			out.flush();
			System.out.println("Message Sent");
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}

	
	public void sendFile(String filePath) throws IOException
    {
    	FileInputStream fis = null;
    	BufferedInputStream bis = null;
    	
    	File myFile = new File(filePath);
        byte [] mybytearray  = new byte [(int)myFile.length()];
        try {
        	System.out.println((int)myFile.length());
			 fis = new FileInputStream(myFile);
			 bis = new BufferedInputStream(fis);
	         bis.read(mybytearray,0,mybytearray.length);
	         out.writeObject(mybytearray);
	         out.flush();
	         System.out.println("File Sent: " + filePath );
			
		} catch (FileNotFoundException e) {
			
			System.out.println("Path for the file is wrong or File do not exists");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Error in reading file from memory");
			e.printStackTrace();
		}finally{
			if (bis != null)
				bis.close();
			if (fis!=null)
				fis.close();
			
		}
       
    	
    	
    }
	
	
	
	
	
	


	//main method
    public static void main(String args[])
	{
		Client client = new Client();
		String name = args[0];
		//System.out.println("Client started with Name :" + name);
		client.run(name);
		
		
		
	}






    
    
    public static class Creader extends Thread
    {
        Socket connection;
		public ObjectInputStream in;
		public FileOutputStream fos;
		public BufferedOutputStream bos;
		public Creader(Socket con)
		{
			connection = con;
		}
		
        public void run()
        {
        		
        	try{
        		
        		in = new ObjectInputStream(connection.getInputStream());
        		
        		while(true)
        		{
        		
        		String MESSAGE = (String)in.readObject();
        		String[] command = null;
        		
        		if(MESSAGE.contains(" ")){
        		command = MESSAGE.split(" ");}
        		
        		if(command!=null&&command[1].equalsIgnoreCase("file_"))
        		{
        		   System.out.println("Recieving file from " + command[0]);
        		   
        		   byte[] content =(byte[])in.readObject();
        		   Path path = Paths.get(System.getProperty("user.dir")+"\\"+command[command.length-1]);
        		   Files.write(path, content);
        		   System.out.println("File Recieved");
        		           			
        		}else
        		{
        			System.out.println();
            		System.out.println(MESSAGE);
        		}
        		
        		
        		
        		}
        		
        	}catch(Exception e){
        		
        		System.out.println("Reader error ");
        		e.printStackTrace();
        		
        	}finally{
        		
        		try {
					in.close();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
        	}
        	
        	
        	
        }



    }

}

