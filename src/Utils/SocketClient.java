package Utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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
  private DataInputStream dis = null;

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
      	dis = new DataInputStream(socket.getInputStream());      	
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
  		SendHelper sp = new SendHelper(act, byteArray);
    	sp.execute();
  }

  public class SendHelper extends AsyncTask<Void, Void, FrameClass>{

  	private CompleteListener callback;
  	private byte[] byteArray;
  	
  	public SendHelper(Activity act, byte[] byteArray){  		
  		this.callback = (CompleteListener)act;
  		this.byteArray = byteArray;
  	}
	
  	@Override
  	protected FrameClass doInBackground(Void... params) {
  		
  		FrameClass ffc = new FrameClass(0, System.nanoTime());
  		
  		if (out != null && byteArray.length > 0){  			
  			try {
				out.write(byteArray, 0, byteArray.length);
			} catch (IOException e) {
				
				e.printStackTrace();
				return ffc;
			}
  		}
  		  		
		int faceNum;		
		try {
			faceNum = dis.readInt();
			System.out.println(faceNum);
			
			for (int i = 0; i < faceNum; ++i){
				int x = dis.readInt();
				int y = dis.readInt();
				int w = dis.readInt();
				int h = dis.readInt();
				System.out.println(x + "," + y + "," + w + "," + h);
				   
				// feature points
				List<org.opencv.core.Point> pts = new ArrayList<org.opencv.core.Point>();
				for (int j = 0; j < 27; ++j){
					int f_x = dis.readInt();
					int f_y = dis.readInt();
					pts.add(new org.opencv.core.Point(f_x, f_y));
					System.out.println("featurePoint " + j + ":" + f_x + "," + f_y);
					
				}
				
				int pred_label = -1;   
				//confidence
				double[] all_conf = new double[Global.CLASS_NUMBER];
				for (int j = 0; j < 5; ++j){
					
					int label = dis.readInt();
					int conf = dis.readInt();					
					all_conf[label] = conf;
					
					if (j == 0){
						pred_label = label; 
					}
					System.out.println(label + "," + conf);
				}
				
				FaceClass fc = new FaceClass(pred_label, x, y, w, h, pts, all_conf);				
				ffc.push(fc);
			}
		} catch (IOException e) {
			
			e.printStackTrace();	
			
		} 
			
		return ffc;
  	}
  	
  	 protected void onPostExecute(FrameClass result) {
  		  callback.responseCallback(result);
  	 }

  }


	

  
}