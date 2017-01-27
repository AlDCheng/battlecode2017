package naclbot.units.AI.scout;

import java.util.Arrays;
import battlecode.common.*;
import naclbot.units.motion.Yuurei;
import naclbot.units.motion.shoot.Korosenai;
import naclbot.units.interact.iFeed;
import naclbot.units.motion.Chirasou;
import naclbot.units.motion.Todoruno;
import naclbot.variables.GlobalVars;
import naclbot.variables.BroadcastChannels;	

/* ------------------   Overview ----------------------
 * 
 * AI Controlling the functions of the explorer scout....
 *
 * ~~ Coded by Illiyia (akimn@#mit.edu)
 * 
 * Debug statements all begin with SYSTEM CHECK 
 * 
 ---------------------------------------------------- */

// Remember.... the first scout gets its own special java file precisely because it is called RemBot!
// Anyone who sincerely doubts that Rem is best girl can look somewhere else - this code is only for those who are true believers.........

public class RemBot extends ScoutBot {
	
	public static void main(){
		
		while (true){
			
			try{
				
				System.out.println("I am the first scout O_o");
				
				System.out.println(unitNumber);
				System.out.println(myID);
				
			     Clock.yield();
			}
			catch (Exception exception){
				System.out.println("Explorer Scout Exception");
                exception.printStackTrace();
			}			
		}	
	}	
}
