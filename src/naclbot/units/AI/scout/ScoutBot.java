// AI for soldier under normal control
package naclbot;
import battlecode.common.*;

/* Values to define
 * 
 * SCOUT_CHANNEL offset 0 -> channel for number of scouts
 * message_offset -> offset of a report from a scout
 * 
 */


/* Scout Report Format
 * 
 * 
 */

public class ScoutBot extends GlobalVars {
	public static void entry() throws GameActionException {
		System.out.println("I'm a scout!");
		
        // Important parameters for self
        Team enemy = rc.getTeam().opponent();
        int id = rc.getID();
        int message_offset = 10;
        int SCOUT_CHANNEL = 150;
        int scout_number = rc.readBroadcast(SCOUT_CHANNEL)+1;
        rc.broadcast(SCOUT_NUMBER, scout_number);

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	
            	
                MapLocation myLocation = rc.getLocation();

                // See if there are any nearby enemy robots
                RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);
                BulletInfo[] bullets = rc.senseNearbyBullets(-1);

                // If there are some...
                if (robots.length > 0) {
                	
                	// IF there are many - scout must choose to go around
                	if(robots.length> 3){
                		//TODO
                		;
                	}
                	// Send current coordinates
                	rc.broadcast(SCOUT_CHANNEL + scout_number * message_offset, (int)myLocation.x);
                	rc.broadcast(SCOUT_CHANNEL + scout_number * message_offset, (int)myLocation.y);                 
                                       
                }
                
                

                // Move randomly
                Move.tryMove(Move.randomDirection());

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Scout Exception");
                e.printStackTrace();
            }
        }
    }
	public static int[] nextLocation(int x, int y) {
		//TODO
		;
	}
	
	public static int[] detectBullets(BulletInfo[] incoming) {
		
	}
	
	
	
	
}