// A simple Client Server Protocol .. Client for Echo Server
import java.io.BufferedInputStream;
import java.io.BufferedReader;import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;import java.io.InputStream;import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Files;import java.nio.file.Paths;import java.security.DigestInputStream;import java.security.MessageDigest;import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class Client {
    private  InetAddress address;
    private  Socket s;
    private  String line;
    private  BufferedReader br;
    private  BufferedReader is;
    private  PrintWriter os;

    
	public static void main(String args[]) throws Exception	{

	  //  InetAddress address=InetAddress.getLocalHost();
		InetAddress address = InetAddress.getByName("127.0.0.1");
	    Socket s=null;
	    Socket dataSocket=null;
	    String line=null;
	    BufferedReader br=null;
	    BufferedReader is=null;
	    PrintWriter os=null;
	    ObjectOutputStream mapOutputStream = null;
	    BufferedInputStream in2 = null;
	    int portNumber = 1;	    	try		    {
	    				    	s=new Socket(address, 4445); // You can use static final constant PORT_NUM		        br= new BufferedReader(new InputStreamReader(System.in));		        is=new BufferedReader(new InputStreamReader(s.getInputStream()));		        os= new PrintWriter(s.getOutputStream());		    }		    catch (IOException e)		    {		    	e.printStackTrace();		    	System.err.print("IO Exception");		    }
	    	String number =is.readLine();	    	portNumber=Integer.parseInt(number);
	    	try{
	    		dataSocket = new Socket(address,portNumber);
	    	    mapOutputStream = new ObjectOutputStream(dataSocket.getOutputStream());
		        in2 = new BufferedInputStream(dataSocket.getInputStream(), 8096);
	    	}catch(IOException e){
	    		e.printStackTrace();
		    	System.err.print("IO Exception");
	    	}
		    System.out.println("Client Address : "+address);
		    System.out.println("Enter Data to echo Server ( Enter QUIT to end):");
		    
		    String response=null;
		    try		    {		     line=br.readLine(); 	     
		     while(line.compareTo("QUIT")!=0)		     {	 
		    	 if(line.compareTo("sync check")==0){    		 
		    		
		    		 os.println("sync check");
		    		 os.flush();
		    		 System.out.println("You have to update your storage with the following files:");
		    		 mapOutputStream.writeObject(hashAllFiles());
		    		 mapOutputStream.flush();
		    		 line=is.readLine();
		    		 while(line.compareTo("finished")!=0){
		    			 System.out.println(line);
		    			 line=is.readLine();
		    		 }
		    		 line=is.readLine();
		    		 System.out.println("The total size of updates is "+line);
		    		 System.out.println("Sync check finished");
		    		 line=br.readLine();
		     	 }
		    	 else if(line.compareTo("sync all")==0){  		 
		    		 String fileName="";
		    		 String operation="";
		    		 long fileSize=0;
		     		 os.println("sync all");
		    		 os.flush();
		    		 System.out.println("sync starting");
		    		 mapOutputStream.writeObject(hashAllFiles());
		    		 mapOutputStream.flush();
		    		 line=is.readLine();	
		    		 if(line.compareTo("finished")!=0){
		    		 fileName=is.readLine();
		    		 operation=is.readLine(); 
		    	     if(operation.compareTo("delete")!=0)fileSize=Long.valueOf(is.readLine()).longValue();
		    		
		    	     	while(line.compareTo("finished")!=0){
		    			 System.out.println(line);	   	
		    			 if(operation.compareTo("update")==0){
	    				        FileOutputStream inFile = new FileOutputStream(getFile(fileName));       
	    				        byte[] bytes = new byte[8096];
	    				        int count;
	    				        while (fileSize>0 && (count = in2.read(bytes)) > 0) {
	    				            inFile.write(bytes, 0, count);
	    				            fileSize-=count;
	    				            inFile.flush();
	    				        }
	    				        inFile.close();
	    				        os.println("done");
	    				        os.flush();
	    				        System.out.println(fileName+" updated");
		    			 }else if(operation.compareTo("delete")==0){
		    				 getFile(fileName).setWritable(true);
		    				 getFile(fileName).delete();
		    				 System.out.println(fileName+" deleted");
		    			 }else if(operation.compareTo("add")==0){
		    				    File thisfile = new File("./Local/"+fileName); 
	    				        
	    				         FileOutputStream inFile = new FileOutputStream(thisfile);
		    				     byte[] bytes = new byte[8096];
		    				     int count;
		    				     while (fileSize>0 && (count = in2.read(bytes)) > 0) {
		    				            inFile.write(bytes, 0, count);
		    				            fileSize-=count;
		    				            inFile.flush();
		    				     }
	    				        inFile.close();
	    				        os.println("done");
	    				        os.flush();
	    				        System.out.println(fileName+" added");
		    				 
		    			 }else{
		    				 System.out.print("Very interesting");
		    			 }
		    			
		    			line=is.readLine();
		    			if(line.compareTo("finished")!=0){
		    			fileName=is.readLine();
			    		operation=is.readLine(); 
			    		if(operation.compareTo("delete")!=0)fileSize=Long.valueOf(is.readLine()).longValue();
		    			}
		    		 }
		    		 }
		    		 line=is.readLine();
		    		 System.out.println("The total size of updates is "+line);
		    		 System.out.println("Syncing finished");
		    		 line=br.readLine();
		     	 } 
		    	 else if(line.length()>5 && line.substring(0,4).compareTo("sync")==0 && line.charAt(5)=='<' && line.charAt(line.length()-1)=='>'){
		    		 String fileName = line.substring(line.indexOf("<")+1,line.indexOf(">"));
		    		 String operation="";
		    		 long fileSize=0;
		    			if(isFileExists(fileName)){
		    				System.out.println("Start Syncing");	
		    				os.println("sync "+fileName);
		    				os.flush();
		    				response=is.readLine();
		    					
		    				 if(response.compareTo("200")==0){
		    				    os.println(getHash(fileName));
	    						os.flush();
		    			        response=is.readLine();  
		    			         if(response.compareTo("OK")==0){
		    				        System.out.println(fileName + " has already updated");
		    				     }else{
		    				        System.out.println(response); 
		    		
		    			    	    operation=is.readLine(); 
		    				        fileSize=Long.valueOf(is.readLine()).longValue();
		    				        FileOutputStream inFile = new FileOutputStream(getFile(fileName));       
		    				        
		    				        byte[] bytes = new byte[8096];
		    				        int count;
		    				        while (fileSize>0 && (count = in2.read(bytes)) > 0) {
		    				            inFile.write(bytes, 0, count);
		    				            fileSize-=count;
		    				            inFile.flush();
		    				        }
		    				        
		    				        inFile.close();
		    				        os.println("done");
		    				        os.flush();
		    				        System.out.println(fileName+" updated");
		    				 
		    				       
		    				     }
		    				   }else{
		    					   	File thisFile = getFile(fileName);
		    						System.out.println("deleting "+fileName+" " + thisFile.getTotalSpace());
		    						if(thisFile.delete()) System.out.println(fileName + " deleted succesfully");
		    						else System.out.println("Encountered a problem");
		    			    }
		    				
		    		     }else{
		    				System.out.println("Please make a sync request for existing file ");
		    		     }
		    			
		    	
		    	        line=br.readLine();
		    	 }else{		    	 	System.out.println("Please enter a valid command");	                line=br.readLine();
		    	 }
		      		     }
		    }catch(IOException e)
		    {
		        e.printStackTrace();
		        System.out.println("Socket read Error");
		    }
		    finally
		    {
		        is.close();os.close();br.close();
		        mapOutputStream.close();in2.close(); dataSocket.close();
		        s.close();
		        System.out.println("Connection Closed");
		    }

		}

	public static boolean isFileExists(String filename){
		 
		File folder = new File("./Local");
		File[] listOfFiles = folder.listFiles();
		boolean isFileExists = false;
		  for (int i = 0; i < listOfFiles.length; i++) {
		    	if(listOfFiles[i].getName().compareTo(filename)==0){
		    		isFileExists=true;
		    		break;
		    	}
		    }
		  return isFileExists;
	}
	public static File getFile(String filename){
		File file = new File("./Local/"+filename);
		return file;	
	}
	public static String getHash(String filename) throws Exception{
		
		        String path="./Local/"+filename;
		        String digest="";
		    	try {
		    	  digest=checkSum(path);
		    	  
		    	}catch(Exception e){
		    		System.out.println(e);
		    	}
		    
		    	return digest; 
	}
	
	public static HashMap<String,String> hashAllFiles() throws Exception{
		File folder = new File("./Local");
		File[] listOfFiles = folder.listFiles();
		HashMap ClientFiles= new HashMap<String,byte[]>();
		String path="";
		String digest = "";
		    for (int i = 0; i < listOfFiles.length; i++) {
		    	path="./Local/"+listOfFiles[i].getName();
		       	try{
		       		digest=checkSum(path);
		    		/*MessageDigest md = MessageDigest.getInstance("SHA1");
		    		FileInputStream is = new FileInputStream("./Local/"+listOfFiles[i].getName()); 		
		    	    DigestInputStream dis = new DigestInputStream(is, md);
		    	 	is.close();
		    	 	dis.close();
		    	    digest= md.digest().toString();
		    	*/
		    	}catch(Exception e){
		    			System.out.println("Error");
		    	}
		    	
		    	//System.out.println(digest);
		    	ClientFiles.put(listOfFiles[i].getName(), digest);
		    }
		   
	    return ClientFiles;
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
		fis.close();
}catch(Exception e){
		System.out.println(e);
}


return digest;

}
}