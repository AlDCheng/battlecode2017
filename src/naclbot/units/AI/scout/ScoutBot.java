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
        int turn_count = 0;
        int track_id = -1;
        MapLocation base = updateBase();  
        MapLocation myLocation = rc.getLocation(); 
        Direction last_direction = new Direction(myLocation.directionTo(base).radians + PI);
 
        
        rc.broadcast(SCOUT_CHANNEL, scout_number);

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	boolean hasBroadcasted = false;
            	
            	// Update Location
            	myLocation = rc.getLocation();         
                
            	// Get nearby enemies            	
            	RobotInfo[] robots = NearbyEnemies(enemy);
            	
            	// Once in a while broadcast to base new information
            	
        		if (turn_count % SCOUT_UPDATE_FREQUENCY == 1){
        			base = updateBase();        			
        			MapLocation nearest = getNearestEnemytoBase(base, robots);
        			
        			
        			//Broadcast own coordinates and coordinates of nearest robots
        			rc.broadcast(1 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)myLocation.x);
                	rc.broadcast(2 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)myLocation.y);              
                	rc.broadcast(3 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)nearest.x);
                	rc.broadcast(4 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)nearest.y);
                	hasBroadcasted = true;
        		}
        		
        		// Too many enemies nearby will commit sudoku
        		if (robots.length > 5){
        			if (hasBroadcasted != true){
        				base = updateBase();        			
            			MapLocation nearest = getNearestEnemytoBase(base, robots);
	        			rc.broadcast(1 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)myLocation.x);
	                	rc.broadcast(2 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)myLocation.y);              
	                	rc.broadcast(3 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)nearest.x);
	                	rc.broadcast(4 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)nearest.y);
        			}        			
        			rc.disintegrate();
        		}
            	
                // If there is no current enemy being tracked
                if (track_id == -1){
                	
                	//If there is a possible robot to be tracked
                	if (robots.length > 0){
                		
                        RobotInfo quandary = getNearestEnemy(myLocation, robots);
                        track_id = quandary.ID;
                    	moveTowards(quandary, myLocation);
                		
                	} else{
                		if (rc.canMove(last_direction));
                		rc.move(last_direction);
                	}    
                	
                // Otherwise if already tracking 
                } else                	
                	
                	// If the robot to be tracked is visible - move towards visible location to within 5 units
                	if (rc.canSenseRobot(track_id)){
                    	RobotInfo quandary = rc.senseRobot(track_id);
                    	// if the robot's current location is far from the 
                    	moveTowards(quandary, myLocation);
                    	
                    	
                			
                	
                		
                		
                	} else{
                		track_id = -1;
                		// Go towards last known position for one turn
                   		if (rc.canMove(last_direction));
                		rc.move(last_direction);
                	}             	

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                turn_count += 1;
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Scout Exception");
                e.printStackTrace();
            }
        }
    }
	public static RobotInfo[] NearbyEnemies(Team nemesis){
		return rc.senseNearbyRobots(-1, nemesis);
	}
	
	public static RobotInfo getNearestEnemy(MapLocation myLocation, RobotInfo[] enemies){
		
		// Smallest distance to another robot
		float minimum = 1000;
				
		// Index of the closest robot defaults to the first					
		int index = 0;
		for (int i = 0; i < enemies.length; i++){
			float dist = myLocation.distanceTo(enemies[i].location);
			if (dist > minimum){
				minimum = dist;
				index = i;
			}			
		}
		return enemies[index];	
	}
	
	public static void moveTowards(RobotInfo quandary, MapLocation myLocation) throws GameActionException{
		float gap = myLocation.distanceTo(quandary.location);
    	Direction dir = myLocation.directionTo(quandary.location);
    	Direction anti_dir = new Direction(dir.radians+PI);
		if  (gap > 7.5){
			// Move towards target]
			if (rc.canMove(dir)){							
				rc.move(dir);
			}
			
		} else if (gap < 2.5) {
			// Move away from target
			if (rc.canMove(anti_dir)){							
				rc.move(anti_dir);
			}
			
		} else {
			if (rc.canMove(dir, gap-5)){							
				rc.move(dir, gap-5);
			}
			
			// Move to a 5 unit distance of the target (either away or towards)
		}
	}
	
	public static MapLocation getNearestEnemytoBase(MapLocation baseLocation, RobotInfo[] enemies){
		
		// Smallest distance to another robot
		float minimum = 1000;
						
		// Index of the closest robot defaults to the first					
		int index = 0;
		for (int i = 0; i < enemies.length; i++){
			float dist = baseLocation.distanceTo(enemies[i].location);
			if (dist > minimum){
				minimum = dist;
				index = i;
			}			
		}
		return enemies[index].location;		
	}
	
	public static int simpleDodge(MapLocation myLocation){
		BulletInfo[] bullets = rc.senseNearbyBullets(-1);
		
		for (int i = 0; i < bullets.length; i++){
			
			if (myLocation.distanceTo(bullets[i].location) < 5)
				;
			
						
		}
		return 1;
	}	
	
	// Get location of starting archon
	public static MapLocation updateBase() throws GameActionException{
		
		MapLocation base = new MapLocation(rc.readBroadcast(ARCHON_CHANNEL), rc.readBroadcast(ARCHON_CHANNEL + 1));
		return base;		
		
	}
}