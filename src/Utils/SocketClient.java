package Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import android.app.Activity;
import android.os.AsyncTask;

public class SocketClient{

	 /**
   * Maximum size of buffer
   */
  public static final int BUFFER_SIZE = 2048;
  private Socket socket = null;
  private OutputStream out = null;
  private BufferedReader in = null;

  private String host;
  private int port = 8888;
 

  /**
   * Constructor with Host, Port 
   * @param host
   * @param port
   */
  public SocketClient(String host, int port) {
      this.host = host;
      this.port = port;
     
  }

  public void connectWithServer() {
      try {
      	System.out.println("Connecting...");
      	socket = new Socket(this.host, this.port);
      	System.out.println("should be connected..");
      	
      	out = socket.getOutputStream();
      	in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      	System.out.println(in.readLine());
          
      } catch (IOException e) {
          e.printStackTrace();
      }
  }

  public void disConnectWithServer() {
      if (socket != null) {
          if (socket.isConnected()) {
              try {
                  in.close();
                  out.close();
                  socket.close();
              } catch (IOException e) {
                  e.printStackTrace();
              }
          }
      }
  }

  public void sendProcessFrameHeader(int command, int faceID, int imgSize, int imgWidth, int imgHeight) throws UnsupportedEncodingException, IOException{
  	
	    
	   	byte[] bSize = new byte[20];
	    bSize[0] = (byte) ((command & 0xff) );
	    bSize[1] = (byte) ((command >> 8) & 0xff);
	    bSize[2] = (byte) ((command >> 16) & 0xff);
	    bSize[3] = (byte) ((command >> 24) & 0xff);

	    bSize[4] = (byte) ((-1 & 0xff) );
	    bSize[5] = (byte) ((-1 >> 8) & 0xff);
	    bSize[6] = (byte) ((-1 >> 16) & 0xff);
	    bSize[7] = (byte)  ((-1 >> 24) & 0xff);

	    bSize[8] = (byte) ((imgSize & 0xff) );
	    bSize[9] = (byte) ((imgSize >> 8) & 0xff);
	    bSize[10] = (byte) ((imgSize >> 16) & 0xff);
	    bSize[11] = (byte)  ((imgSize >> 24) & 0xff);
	    
	    bSize[12] = (byte) ((imgWidth & 0xff) );
	    bSize[13] = (byte) ((imgWidth >> 8) & 0xff);
	    bSize[14] = (byte) ((imgWidth >> 16) & 0xff);
	    bSize[15] = (byte)  ((imgWidth >> 24) & 0xff);
	    
	    bSize[16] = (byte) ((imgHeight & 0xff) );
	    bSize[17] = (byte)((imgHeight >> 8) & 0xff);
	    bSize[18] = (byte) ((imgHeight >> 16) & 0xff);
	    bSize[19] = (byte)  ((imgHeight >> 24) & 0xff);

	    out.write( bSize );
  
  }
  
  public void goodBye() throws UnsupportedEncodingException, IOException{
	  
	   byte[] bSize = new byte[20];
	    bSize[0] = (byte) ((-1 & 0xff) );
	    bSize[1] = (byte) ((-1 >> 8) & 0xff);
	    bSize[2] = (byte) ((-1 >> 16) & 0xff);
	    bSize[3] = (byte) ((-1 >> 24) & 0xff);

	    bSize[4] = (byte) ((-1 & 0xff) );
	    bSize[5] = (byte) ((-1 >> 8) & 0xff);
	    bSize[6] = (byte) ((-1 >> 16) & 0xff);
	    bSize[7] = (byte)  ((-1 >> 24) & 0xff);

	    bSize[8] = (byte) ((-1 & 0xff) );
	    bSize[9] = (byte) ((-1 >> 8) & 0xff);
	    bSize[10] = (byte) ((-1 >> 16) & 0xff);
	    bSize[11] = (byte)  ((-1 >> 24) & 0xff);
	    
	    bSize[12] = (byte) ((-1 & 0xff) );
	    bSize[13] = (byte) ((-1 >> 8) & 0xff);
	    bSize[14] = (byte) ((-1 >> 16) & 0xff);
	    bSize[15] = (byte)  ((-1 >> 24) & 0xff);
	    
	    bSize[16] = (byte) ((-1 & 0xff) );
	    bSize[17] = (byte)((-1 >> 8) & 0xff);
	    bSize[18] = (byte) ((-1 >> 16) & 0xff);
	    bSize[19] = (byte)  ((-1 >> 24) & 0xff);

	    out.write( bSize );
	   
  }
  
  public void sendEntireFrame(byte[] byteArray, Activity act)throws IOException {
  		if (out != null && byteArray.length > 0){  			
  			out.write(byteArray, 0, byteArray.length);
  		}
  		
  		System.out.println("send entire frame");
  		
  		
  		SendHelper sp = new SendHelper(act);
    	sp.execute();
    	  	
  }

  public class SendHelper extends AsyncTask<Void, Void, String>{

  	private CompleteListener callback;
  	
  	public SendHelper(Activity act){
  		
  		this.callback = (CompleteListener)act;
  	
  	}
  	public void execute() {
		// TODO Auto-generated method stub
  		String message = "";
  	    int charsRead = 0;
  	    char[] buffer = new char[BUFFER_SIZE];
  	    try {
  	    	if ((charsRead = in.read(buffer)) != -1) {
  	    		message += new String(buffer).substring(0, charsRead);
  	    		System.out.println("msg:" + message);
  	    				
  	    		 callback.responseCallback(message); 	  		
  	        }
  	    	  	  	  
  	    } catch (IOException e) {
  	    	// TODO Auto-generated catch block
  	    	e.printStackTrace();
  	     }
	}
	
  	@Override
  	protected String doInBackground(Void... params) {
  		/**
  		System.out.println("running");
  		String message = "";
  	    int charsRead = 0;
  	    char[] buffer = new char[BUFFER_SIZE];
  	    try {
  	    	while ((charsRead = in.read(buffer)) != -1) {
  	    		message += new String(buffer).substring(0, charsRead);
  	    		System.out.println("msg" + message );
      	  		return message;
  	        }
  	    	  	  	  
  	    } catch (IOException e) {
  	    	// TODO Auto-generated catch block
  	    	e.printStackTrace();
  	     }**/
  	    return null;
  	}
  	
  	 protected void onPostExecute(String result) {
  		  //callback.responseCallback(result);
  	}

  }


	

  
}