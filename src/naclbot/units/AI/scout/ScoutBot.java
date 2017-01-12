// AI for soldier under normal control
package naclbot;
import battlecode.common.*;

/* Values to define
 * 
 * SCOUT_CHANNEL offset 0 -> channel for number of scouts
 * SCOUT_MESSAGE_OFFSET -> offset of a report from a scout
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
        int scout_number = rc.readBroadcast(SCOUT_CHANNEL) + 1;
        
        rc.broadcast(SCOUT_CHANNEL, scout_number);

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	
            	
                MapLocation myLocation = rc.getLocation();
                

                // See if there are any nearby enemy robots
                RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);
                

                // If there are some...
                if (robots.length > 0) {
                	
                	// IF there are many - scout must choose to go around
                	if(robots.length> 3){
                		//TODO
                		;
                	}
                	// Send current coordinates
                	rc.broadcast(1 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)myLocation.x);
                	rc.broadcast(1 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)myLocation.y);                 
                    
   
                		
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
	public static RobotInfo[] countNearbyEnemies(Team nemesis){
		return rc.senseNearbyRobots(-1, nemesis);
	}
	
	public static int getNearestEnemy(){
		return 1;
		
	}
	
	public static int simpleDodge(){
		BulletInfo[] bullets = rc.senseNearbyBullets(-1);
		
		for (int i = 0; i < bullets.length; i++){
			
			if (rc.getLocation().distanceTo(bullets[i].location) < 5)
				;
			
						
		}
		return 1;
	}
	
	
	
	
}