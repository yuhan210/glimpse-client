package mit.csail.glimpse.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Global {
	public static int CLASS_NUMBER;
	public static boolean sendFrame;
	public static int token; //how many numbers 
	public static int TOKENLIMIT = 1;
	
	//triggered transmission
	public static int MOTIONTHRESH = 640 * 480 / 2;
}
