// AI for gardener under normal control
package naclbot.units.AI.gardener;
import battlecode.common.*;
import naclbot.units.motion.Move;
import naclbot.variables.GlobalVars;

/* --------------------------------   Overview  ---------------------------------------
 * 
 * 		AI Controlling the functions of a useless testbot that does nothing
 * 			but stand there and move randomly (for testing purposes
 *
 *			          ~~ Coded by Illiyia (akimn@#mit.edu)
 *
 *			        Call the init() function to use the file...
 * 
 * 		  		Note: Debug statements all begin with SYSTEM CHECK 
 * 				  (Although none hre since this bot is retarded)
 * 
 * 		  Note: These scouts are called EmiliaBot because Emilia deserves
 * 					nothing for taking Subaru away from Rem >.>
 * 
 ------------------------------------------------------------------------------------- */

// Useless bot.. does nothing but move randomly

public class EmiliaBot extends GlobalVars {	
	
	public static void init(){
	
	// SYSTEM CHECK - Print out that the EmiliaBot has initiated...
	System.out.println("Hello, I'm an EmiliaBot and I'm retarded!");
	
	// Go on to the main function....
	main();
	}
	
	public static void main(){
	    // The code you want your robot to perform every round should be in this loop
	    while (true) {
	
	        // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
	        try {
	        	
	        	// SYSTEM CHECK - Print out why EmiliaBot sucks...
	        	System.out.println("I stole Subaru away from Rem and should totally rethink my life choices!");	
	        	System.out.println("Although to be honest he was a piece of shit until Rem raised him from the depths of hell");
	
	        	// EmiliaBot doesnt know what this does....
	        	boolean hasMoved = false;
	        	
	        	// In a retarded fashion, check for 30 different ways to move without breaking the loop even if one has been found
	        	for(int i = 0; i<=30; i ++){
	        		
		            // Generate a random direction
		            Direction dir = Move.randomDirection();
		            
		            float distance = (float) (Math.random() * battlecode.common.RobotType.GARDENER.strideRadius);
		            
		            // Check to see if the EmiliaBot can move in the given direction.....
		            if(rc.canMove(dir, distance) && !hasMoved){
		            	
		            	// Move in the generated distance and allow for no more moves.....
		            	rc.move(dir, distance);
		            	hasMoved = true;
		            }
	        	}	        	
	        	printOutWhyEmiliaIsNotBestGirl();
	        	
	            // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
	            Clock.yield();
	
	        } catch (Exception e) {
	            System.out.println("Retard Exception");
	            e.printStackTrace();
	        }
	    }
    }
	
	// Function to remind everyone why Emilia isn't best girl...
	
	private static void printOutWhyEmiliaIsNotBestGirl(){
		
		float randomize = (float) Math.random();
		
		if(randomize >= 0.5){			
			System.out.println("The only thing I have ever accomplished in life is getting Subaru-kun to sleep on my lap once");
		}
		else if(randomize >= 0.3){			
			System.out.println("I'm just a bland, boring, and underdeveloped character... Yay!");
		}
		else if(randomize >= 0.2){			
			System.out.println("Fuck Barusu");
		}
		else{			
			System.out.println("Felis is a better best girl than I will ever be!");			
		}
	}
}