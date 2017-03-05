import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Server {

    private static final int sPort = 8001; //The server will be listening on this port number
    static HashSet<Handler> handlers = new HashSet<Handler>();
    public static void main(String[] args) throws Exception {
        System.out.println("The server is running.");
        ServerSocket listener = new ServerSocket(sPort);

        int clientNum = 0;

        try {

            while (true) {
                new Handler(listener.accept(),clientNum).start();
                //handlers.get(clientNum).start();
                
                clientNum++;
            }
        } finally {
            listener.close();
        }




}

    //functions for the messages
    public static void broadcast(String msg,Handler avoid,String from) {
        System.out.println("broadcasting");

        for (Handler h : handlers) {
                //String MESSAGE = msg.toUpperCase();
        		if(h!=avoid)
                h.sendMessage("@"+from+": "+msg,h.cname);
            }
        }
    
    public static void unicast(String msg,String toSend,String from)
    {
    	for(Handler h : handlers)
    	{
    		if(h.cname.equals(toSend))
    		{
    			h.sendMessage("@"+from+": "+msg,h.cname);
    		}
    		
    	} 	
    }
    
    public static void blockcast(String msg, String toblock, String from) {
		
    	for(Handler h : handlers)
    	{
    		if(!h.cname.equals(toblock)&&(!h.cname.equalsIgnoreCase(from)))
    		{
    			h.sendMessage("@"+from+": "+msg,h.cname);
    		}
    		
    	}
		
	}
    
    
    
    //Functions for files
    public static void broadcastFile(byte[] file,Handler avoid,String from) {
        System.out.println("broadcasting function started");

        for (Handler h : handlers) {
                //String MESSAGE = msg.toUpperCase();
        		if(h!=avoid)
					try {
						h.sendFile(file,h.cname);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            }
        }
    
    public static void unicastFile(byte[] file,String toSend,String from) throws IOException
    {
    	for(Handler h : handlers)
    	{
    		if(h.cname.equals(toSend))
    		{
    			h.sendFile(file, h.cname);
    		}
    		
    	} 	
    }
    
    public static void blockcastFile(byte[] file, String toblock, String from) throws IOException {
		
    	for(Handler h : handlers)
    	{
    		if(!h.cname.equalsIgnoreCase(toblock)&&(!h.cname.equalsIgnoreCase(from)))
    		{
    			h.sendFile(file,h.cname);
    		}	
    	}	
	}
    
    
    
    private static class Handler extends Thread {
        private String message; //message received from the client
        //private String MESSAGE; //uppercase message send to the client
        Socket connection;
        private ObjectInputStream in;//stream read from the socket ????????
        private ObjectOutputStream out;//stream write to the socket
        
        private FileOutputStream fos;
        private BufferedOutputStream bos;
        private int no;				//The index number of the client
        String cname;
        
        public Handler(Socket connection, int no) {

            this.connection = connection;


            this.no = no;

        }
        
        public void changeName(String n){
        	cname = n;
        }

        public void run() {

            try{

                handlers.add(this);

                //initialize Input and Output streams 
                out = new ObjectOutputStream(connection.getOutputStream());
                out.flush();
                in = new ObjectInputStream(connection.getInputStream());
                
                try{
                	changeName((String)in.readObject());
                	System.out.println("Client " + cname + " isconnected!");
                    while(true)
                    { 

                        //receive the message sent from the client
                        message = (String)in.readObject();
                        //show the message to the user
                        System.out.println("Receive message: " + message + " from client " + cname);
                        String[] command = message.split(" ");	
                        
                        
                       
                       boolean error = true; 
                        
                       //broadcast
                       if(command[0].equalsIgnoreCase("broadcast"))
                        {
                    	 
                       		
                    	   if(command[1].equalsIgnoreCase("message"))
                        	{
                        		
                    		   String msg = "";
                          		for(int i=2;i<command.length;i++)
                          		{
                          		msg = msg + command[i] + " ";
                          		}
                    		   
                    		  
                    		   error = false;
                    		   Server.broadcast(msg,this,cname);
                        	}
                    	   
                    	   
                    	   if(command[1].equalsIgnoreCase("file"))
                       		{
                       			
                    		// receive file
                    		   System.out.println("....****Recieving file from " + cname);
                    		   byte[] mybytearray =(byte[])in.readObject();
                    		   //Path path = Paths.get(System.getProperty("user.dir")+command[command.length-1]);
                    		   //Files.write(path, content);
                    		   System.out.println("....****File Recieved from " + cname);

                    		  
                    		    Server.broadcast("File_ "+command[command.length-1], this, cname);
                    		    Server.broadcastFile(mybytearray,this,cname); 
                    		      
                    		     
                    		   error = false;
                       		}
                        	
                        }
                        
                        //Unicast
                        if(command[0].equalsIgnoreCase("unicast"))
                        {
                        	
                        	
                        	if(command[1].equalsIgnoreCase("message"))
                        	{
                        		String msg = "";
                        	
                        		for(int i=2;i<command.length-1;i++)
                        		{		
                        		msg = msg + command[i] + " ";
                        		}
                        		
                        		error = false;
                        		Server.unicast(msg, command[command.length-1], cname);
                        	}
                        	
                        	if(command[1].equalsIgnoreCase("file"))
                        	{
                        		// receive file
                     		   System.out.println("....****Recieving file from " + cname);
                     		   byte[] mybytearray =(byte[])in.readObject();
                     		   //Path path = Paths.get(System.getProperty("user.dir")+command[command.length-1]);
                     		   //Files.write(path, content);
                     		   System.out.println("....****File Recieved from " + cname);

                     		  
                     		    Server.unicast("File_ "+command[2],command[command.length-1], cname);
                     		    Server.unicastFile(mybytearray,command[command.length-1],cname); 
                     		      
                     		     
                     		   error = false;
                        	}
                        	
                        	
                        	
                        	
                        }
                        
                        
                        //bloackcast
                        if(command[0].equalsIgnoreCase("blockcast"))
                        {
                        	
                        	
                        	if(command[1].equalsIgnoreCase("message"))
                        	{
                        		String msg = "";
                        	
                        		for(int i=2;i<command.length-1;i++)
                        		{		
                        		msg = msg + command[i] + " ";
                        		}
                        		
                        		error = false;
                        		Server.blockcast(msg, command[command.length-1], cname);
                        	}
                        	
                        	if(command[1].equalsIgnoreCase("file"))
                        	{
                        		// receive file
                     		   System.out.println("....****Recieving file from " + cname);
                     		   byte[] mybytearray =(byte[])in.readObject();
                     		   //Path path = Paths.get(System.getProperty("user.dir")+command[command.length-1]);
                     		   //Files.write(path, content);
                     		   System.out.println("....****File Recieved from " + cname);

                     		  
                     		    Server.blockcast("File_ "+command[2],command[command.length-1], cname);
                     		    Server.blockcastFile(mybytearray,command[command.length-1],cname); 
                     		      
                     		     
                     		   error = false;
                        	}
                        	
                        	
                        	
                        	
                        }
                
                        if(error)
                        {
                        	System.out.println("Some error in what you typed. Type again");
                        	//System.out.println("Or recieved a file for now " + message);
                        	Server.unicast("There is some error in what you typed", cname, "Server");
                        }

                    }

                }
                catch(ClassNotFoundException classnot){

                    System.err.println("Data received in unknown format");

                }

            }
            catch(IOException ioException){

                System.out.println("Disconnect with Client -1" + cname);
                //ioException.printStackTrace();
                
                handlers.remove(this);

            }
            finally{

//Close connections 
                try{
                    in.close();
                    out.close();

                    connection.close();

                }
                catch(IOException ioException){

                    System.out.println("Disconnect with Client -2" + cname);
                    //ioException.printStackTrace();
                    
                    handlers.remove(this);

                }

            }

        }

        //send a message to the output stream 
        public void sendMessage(String msg,String name)
        {

            try{

                out.writeObject(msg);
                out.flush();
                System.out.println("Send message: " + msg + " to Client " +

                        name);

            }
            catch(IOException ioException){
                ioException.printStackTrace();
            }

        }
        
        public void sendFile(byte[] mybytearray,String name) throws IOException
        {
        	
            try {
		         out.writeObject(mybytearray);
		         out.flush();
		         System.out.println("File Sent : to " +name);
				
			} catch (FileNotFoundException e) {
				
				System.out.println("Path for the file is wrong or File do not exists");
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("Error in reading file from memory");
				e.printStackTrace();
			}finally{
				
				
			}
           
        	
        	
        }
        
        
        
        
    }









	
}


