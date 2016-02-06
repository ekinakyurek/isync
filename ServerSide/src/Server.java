// echo server
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class Server
{
	public static void main(String args[])
	{
	
		
	    Socket s=null;
	    ServerSocket ss=null;
	    System.out.println("Server Listening......");
	    try
	    {
	        ss = new ServerSocket(4445); // can also use static final PORT_NUM , when define
	    }
	    catch(IOException e)
	    {
		    e.printStackTrace();
		    System.out.println("Server error");
	    }
	    int i=1;
	    while(true)
	    {
	        try
	        {
	         
	            s= ss.accept();
	            System.out.println("Connection established for client: "+i);
	            ServerThread st=new ServerThread(s,i);
	            st.start();
	            i=i+1;
	         
	        }
	
	        catch(Exception e)
	        {
		        e.printStackTrace();
		        System.out.println("Connection Error");
	        }
	    }
	
	}

}

class ServerThread extends Thread
{  
	int clientNumber=1;
	ServerSocket dataStream=null;
    String line=null;
    BufferedReader  is = null;
    PrintWriter os=null;
    Socket s=null;
    ObjectInputStream mapInputStream = null;
    BufferedOutputStream out = null;
    Socket dataPort=null;
    
    public ServerThread(Socket s,int clientNumber)
    
    {
        this.s=s;
        this.clientNumber=clientNumber;
    }

    public void run() 
    {
    	
    try{
        is= new BufferedReader(new InputStreamReader(s.getInputStream()));
        os=new PrintWriter(s.getOutputStream());
       // mapInputStream = new ObjectInputStream(s.getInputStream())
    }
    catch(IOException e)
    {
        System.out.println("IO error in server thread");
    }
      
    try{
    	int portNumber=s.getLocalPort()+clientNumber;

        this.dataStream =new ServerSocket(portNumber);
        os.println(portNumber);
        os.flush();
		dataPort=dataStream.accept();
		mapInputStream = new ObjectInputStream(dataPort.getInputStream());
		out = new BufferedOutputStream(dataPort.getOutputStream(),8096);
		System.out.println("Data connection established for client: " + clientNumber);
    }catch(IOException e){
		    e.printStackTrace();
		    System.out.println("Server error");
    }
 
    try 
    {
        line=is.readLine();
        while(line.compareTo("QUIT")!=0)
        {

        	if(line.compareTo("sync check")==0) {
        	try {
				syncCheck();
			} catch (Exception e) {
				e.printStackTrace();}
        	}
        	else if(line.compareTo("sync all")==0){
        		try {
					syncAll();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        	else if(line.substring(0,4).compareTo("sync")==0){
        		try {
					syncFile(line.substring(5));
				} catch (Exception e) {
				}
        	}
        	else{
        		//Do nothing
        	}
        	line=is.readLine();
        }   
    } 
    catch (IOException e) 
    {
    	
        line=this.getName();
        int number=Integer.parseInt(line.substring(line.indexOf('-')+1))+1;
       //reused String line for getting thread name
        System.out.println("IO Error/ Client "+number+" terminated abruptly");
    }
    catch(NullPointerException e)
    {
    	
        line=this.getName();
        int number=Integer.parseInt(line.substring(line.indexOf('-')+1))+1;
        		//reused String line for getting thread name
        System.out.println("Client "+number+" Closed");
    }

    finally
    {    
    	String clientString =" for client: "+clientNumber;
	    try{
	        System.out.println("Connection closing "+clientString);
	        if (is!=null)
	        {
	            is.close(); 
	            System.out.println("Command Socket Input Stream closed"+clientString);
	        }
	
	        if(os!=null)
	        {
	            os.close();
	            System.out.println("Command Socket Out closed"+clientString);
	        }
	        if (out!=null)
	        {
		        out.close();
		        System.out.println("FileOutputStrem Closed"+clientString);
	        }
	        if (mapInputStream!=null)
	        {
		        out.close();
		        System.out.println("MapInputStream closed"+clientString);
	        }
	        if (dataPort!=null)
	        {
		        dataPort.close();
		        System.out.println("Dataport Socket closed"+clientString);
	        }
	        if (dataStream!=null)
	        {
		        dataStream.close();
		        System.out.println("DataStream Server Socket closed"+clientString);
	        }
	        if (s!=null)
	        {
		        s.close();
		        System.out.println("Command Socket Closed");
	        }
	       
	
	        }
	    catch(IOException ie)
	    {
	        System.out.println("Socket Close Error");
	    }
	 }//end finally
    }
    
    private void syncFile(String filename) throws Exception {
    
    	File folder = new File("./Storage");
		File[] listOfFiles = folder.listFiles();
		File ourFile = null;
    	boolean isFileExists = false;
    	
    	for (int i = 0; i < listOfFiles.length; i++) {
		    	if(listOfFiles[i].getName().compareTo(filename)==0){
		    		isFileExists =true;
		    		ourFile = listOfFiles[i];
		    		break;
		    	}
		    }
    
    	if(isFileExists){
    		  
    		  os.println("200");
              os.flush();          
              String hash="";
    		  hash=is.readLine();	
        	  if(hash.compareTo(getHash(filename))==0){
        		os.println("OK");
                os.flush(); 
        	  }else{
        		 os.println(filename+" "+ getAsString(ourFile.length())+" sending");
        		 os.flush();
        		 os.println("update");
        		 os.flush();
        		 os.println(ourFile.length());
        		 os.flush();
        		 sendFile(ourFile);
        	  }
    			  
    	  }else{  		
    		  os.println(404);
              os.flush();
              System.out.println("Response to Client  :  "+"404");
    	  }
     
	}
	
    	private boolean sendFile(File thisFile) throws IOException, ClassNotFoundException{
    			    FileInputStream in = new FileInputStream(thisFile);
    			    byte[] bytes = new byte[8096];
    		        int count;
    		        while ((count = in.read(bytes)) > 0) {
    		            out.write(bytes, 0, count);
    		            out.flush();
    		        }
    		      //  out.close();
    			    line=is.readLine();
    			    in.close();
    			    
    			    if(line.compareTo("done")==0){
    			    	return true;
    			    }else{
    			    	return false;
    			    }			  
    			    
    			    //System.out.println("Bytes Sent :" + bytecount);
		}

    	public static String getHash(String filename) throws Exception{
    		
	        String path="./Storage/"+filename;
	        String digest="";
	    	try {
	    	  digest=checkSum(path);
	    	  
	    	}catch(Exception e){
	    		System.out.println(e);
	    	}
	    
	    	return digest; 
}


	private void syncAll() throws Exception {
		 final Map<String,String> yourMap = (Map) mapInputStream.readObject();
		// System.out.println(yourMap.get("Presentation1.pptx"));
		 HashMap <String,String>serverFiles = hashAllFiles();
		 long updateSize=0;
		 if(yourMap.size()!=0){
			 for (String key : yourMap.keySet()) {
				 
			    if(serverFiles.get(key)!=null){
			    
			    	if(serverFiles.get(key).compareTo(yourMap.get(key))!=0){	
			    	
			    	os.println(key+" update " +getAsString(getFile(key).length()));
				    os.flush();
				    os.println(key);
				    os.flush();
				    os.println("update");
				    os.flush();
				    os.println(getFile(key).length());
				    os.flush();
				    sendFile(getFile(key));
				    updateSize+=getFile(key).length();
			    	}else{
			   		 
			    		//do nothing
			    	}
			    }else{
			    	os.println(key+" delete " +getAsString(getFile(key).length()));
			    	    os.flush();
			    	    os.println(key);
					    os.flush();
					    os.println("delete");
					    os.flush();
			    	    updateSize+=+getFile(key).length();
			    }
			    serverFiles.remove(key);
			 }
			 for(String key : serverFiles.keySet()){
				  os.println(key+" add " +getAsString(getFile(key).length()));
				  os.flush();
				   os.println(key);
				    os.flush();
				    os.println("add");
				    os.flush();
				    os.println(getFile(key).length());
				    os.flush();
				    sendFile(getFile(key));
				  updateSize+=+getFile(key).length();
			 }
		 }else{
			 if(serverFiles.size()!=0){
			 for(String key : serverFiles.keySet()){
				  os.println(key+" add " +getAsString(getFile(key).length()));
				  os.flush();
				  os.println(key);
				  os.flush();
				  os.println("add");
				  os.flush();
				  os.println(getFile(key).length());
				  os.flush();
				  sendFile(getFile(key));
				  updateSize+=+getFile(key).length();
			 }	
			 }else{
				
			 }
		 }
		 os.println("finished");
		 os.flush();
		
	
		 os.println(getAsString(updateSize));
		 os.flush();
		
	}
	public File getFile(String filename){
		File file = new File("./Storage/"+filename);
		return file;	
	}
	private void syncCheck() throws Exception{	
		
		final Map<String,String> yourMap = (Map) mapInputStream.readObject();
		
		// System.out.println(yourMap.get("Presentation1.pptx"));
		 HashMap <String,String>serverFiles = hashAllFiles();
		 long updateSize=0;
		 if(yourMap.size()!=0){
			 
			 for (String key : yourMap.keySet()) {
			    if(serverFiles.get(key)!=null){
			    
			    	if(serverFiles.get(key).compareTo(yourMap.get(key))!=0){	
			    	
			    	os.println(key+" update " +getAsString(getFile(key).length()));
				    os.flush();
				    updateSize+=getFile(key).length();
			    	}else{
			   		 
			    		//do nothing
			    	}
			    }else{
			    
			    	os.println(key+" delete " +getAsString(getFile(key).length()));
			    	os.flush();
			    	updateSize+=+getFile(key).length();
			    }
			    serverFiles.remove(key);
			 }
			 for(String key : serverFiles.keySet()){
				  os.println(key+" add " +getAsString(getFile(key).length()));
				  os.flush();
				  updateSize+=+getFile(key).length();
			 }
		 }else{
			 if(serverFiles.size()!=0){
			 for(String key : serverFiles.keySet()){
				  os.println(key+" add " +getAsString(getFile(key).length()));
				  os.flush();
				  updateSize+=+getFile(key).length();
			 }	
			 }else{
				//do nothing
			 }
		 }
		 os.println("finished");
		 os.flush();
		 os.println(getAsString(updateSize));
		 os.flush();
    }
	
	public static HashMap<String,String> hashAllFiles() throws Exception{
		File folder = new File("./Storage");
		File[] listOfFiles = folder.listFiles();
		HashMap ServerFiles= new HashMap<String,byte[]>();
		String path="";
		String digest = "";
		    for (int i = 0; i < listOfFiles.length; i++) {
		    	path="./Storage/"+listOfFiles[i].getName();
		    
		    	try{
		    		digest = checkSum(path);
		    		//MessageDigest md = MessageDigest.getInstance("SHA1");
		    		//FileInputStream is = new FileInputStream("./Storage/"+listOfFiles[i].getName()); 		
		    	   // DigestInputStream dis = new DigestInputStream(is, md);   
		    	  	//is.close();
		    	 	//dis.close();
		    	    //digest= md.digest().toString();	    	
		    	}catch(Exception e){
		    			System.out.println("Error");
		    	}
		    	
		    	//System.out.println(digest);
		    	ServerFiles.put(listOfFiles[i].getName(), digest);
		    }
	    return ServerFiles;
	}
	
    public static String checkSum(String path){
    	String digest="";
    try{
    	MessageDigest md = MessageDigest.getInstance("MD5");
        FileInputStream fis = new FileInputStream(path);
        
        byte[] dataBytes = new byte[1024];
     
        int nread = 0; 
        while ((nread = fis.read(dataBytes)) != -1) {
          md.update(dataBytes, 0, nread);
        };
        byte[] mdbytes = md.digest();
        
        StringBuffer hexString = new StringBuffer();
    	for (int i=0;i<mdbytes.length;i++) {
    		String hex=Integer.toHexString(0xff & mdbytes[i]);
    	     	if(hex.length()==1) hexString.append('0');
    	     	hexString.append(hex);
    	}
    		digest = hexString.toString();
    	
    }catch(Exception e){
    		System.out.println(e);
    }
    
   
	return digest;

    }
    

    private static final String[] Q = new String[]{"Bytes", "Kb", "Mb", "Gb", "T", "P", "E"};

    public String getAsString(long bytes)
    {
        for (int i = 6; i >= 0; i--)
        {
            double step = Math.pow(1024, i);
            
            if (bytes > step){
            	return String.format("%3.1f %s", bytes / step, Q[i]);
         
            }
           
        }
        
        return Long.toString(bytes);
    }




}