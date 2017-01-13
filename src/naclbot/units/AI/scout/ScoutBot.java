// AI for soldier under normal control
package naclbot;
import java.util.Arrays;

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
        int Rem_is_better = 0;
        int track_id = -1;
        MapLocation base = updateBase();  
        MapLocation myLocation = rc.getLocation(); 
        
        // initial starting movement away from Archon
        Direction last_direction = new Direction(myLocation.directionTo(base).radians + (float) Math.PI);
 
        
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
            	
        		if (Rem_is_better % SCOUT_UPDATE_FREQUENCY == 1){
        			base = updateBase();
        			if (robots.length>0){
        				MapLocation nearest = getNearestEnemytoBase(base, robots);
                 
                    	rc.broadcast(3 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)nearest.x);
                    	rc.broadcast(4 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)nearest.y);
        			}
        			
        			
        			//Broadcast own coordinates and coordinates of nearest robots
        			rc.broadcast(1 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)myLocation.x);
        			rc.broadcast(2 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)myLocation.y);     

                	rc.broadcast(6 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, id);
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
	                	rc.broadcast(6 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, id);
	                	rc.broadcast(7 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, 1);
        			}
        			int x = rc.readBroadcast(SCOUT_CHANNEL);
        			rc.broadcast(SCOUT_CHANNEL, x-1);
        			rc.disintegrate();
        		}
            	
                // If there is no current enemy being tracked
                if (track_id == -1){
               
                	rc.broadcast(5 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, track_id);
                	int[] already_tracked = getOtherTracked();
                	
                	//If there is a possible robot to be tracked
                	if (robots.length > 0){
                	
                        RobotInfo quandary = getNearestEnemy(myLocation, robots, already_tracked);
                        track_id = quandary.ID;
                    	last_direction = moveTowards(quandary, myLocation);
                  
                    	
                	} else{
                		if (rc.canMove(last_direction)){
                			rc.move(last_direction, (float)2.5);
                		
                			
                		}
                		else{
                			Direction asdf = Move.randomDirection();
                			tryMoveScout(asdf);
                			last_direction = asdf;
                		
                		}
                	}
               
                	
                // Otherwise if already tracking 
           
                } else{
                	
                	rc.broadcast(5 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, track_id);
                	
                	// If the robot to be tracked is visible - move towards visible location to within 5 units
                	if (rc.canSenseRobot(track_id)){
                    	RobotInfo quandary = rc.senseRobot(track_id);
                    	// if the robot's current location is far from the 
                    	last_direction = moveTowards(quandary, myLocation);
                    	
                    	
                			
                	
                		
                		
                	} else{
                		track_id = -1;
                		// Go towards last known position for one turn
                   		if (rc.canMove(last_direction)|| rc.isLocationOccupiedByTree(myLocation.add(last_direction, (float)2.5))){
                   			rc.move(last_direction);
                   		}
                   		
                   		if (!rc.hasMoved()){
                     		int i = 0; 
                     	
                     		while(!rc.hasMoved() && i < 10){
                     			Direction adir = Move.randomDirection();
                     			tryMoveScout(adir);
                     			i++;
                     			last_direction = adir;
                     		}
                     		
              
                     	}
                		
                	}             	
                }
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Rem_is_better += 1;
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
	
	public static RobotInfo getNearestEnemy(MapLocation myLocation, RobotInfo[] enemies, int[] ikenai){
		
		// Smallest distance to another robot
		float minimum = 1000;
				
		// Index of the closest robot defaults to the first					
		int index = 0;
		for (int i = 0; i < enemies.length; i++){
			float dist = myLocation.distanceTo(enemies[i].location);
			if (dist > minimum && !Arrays.asList(ikenai).contains(enemies[i].ID)){
				minimum = dist;
				index = i;
			}			
		}
		return enemies[index];	
	}
	
	public static Direction moveTowards(RobotInfo quandary, MapLocation myLocation) throws GameActionException{
		float gap = myLocation.distanceTo(quandary.location);
    	Direction dir = myLocation.directionTo(quandary.location);
    	Direction perp = new Direction(dir.radians+((float) Math.PI/2));
    	Direction anti_perp = new Direction(dir.radians+((float) Math.PI/2));

    	Direction anti_dir = new Direction(dir.radians+(float) Math.PI);

		if  (gap > 7.5){
			// Move towards target]
			if (rc.canMove(dir) || rc.isLocationOccupiedByTree(myLocation.add(dir, (float)2.5))){							
				rc.move(dir);
				return dir;
			}
			else{Direction dir2 = Move.randomDirection();
     			tryMoveScout(dir);
     			return dir2;
			}
			
		} else if (gap < 2.5) {
			// Move away from target
			if (rc.canMove(anti_dir) || rc.isLocationOccupiedByTree(myLocation.add(anti_dir, (float)2.5))){							
				rc.move(anti_dir);
				return dir;
			}
			else{Direction dir2 = Move.randomDirection();
     			tryMoveScout(dir);
     			return dir2;
			}
			
		} else {
			float nani = (float) Math.random();
			float keikaku =  (float) Math.random() + (float) 1.5;
			if (nani>0.5){
				if (rc.canMove(perp)|| rc.isLocationOccupiedByTree(myLocation.add(perp, (float)2.5))){							
					rc.move(perp, keikaku);
					return perp;
				} else if (rc.canMove(anti_perp)|| rc.isLocationOccupiedByTree(myLocation.add(anti_perp, (float)2.5))){							
					rc.move(anti_perp,keikaku);
					return anti_perp;
				} else{Direction dir2 = Move.randomDirection();
					tryMoveScout(dir);
					return dir2;
				}
			}   else{
				
				if (rc.canMove(anti_perp)|| rc.isLocationOccupiedByTree(myLocation.add(anti_perp, (float)2.5))){							
					rc.move(anti_perp, keikaku);
					return anti_perp;
				} else if (rc.canMove(perp)|| rc.isLocationOccupiedByTree(myLocation.add(perp, (float)2.5))){							
					rc.move(perp, keikaku);
					return perp;
				} else{Direction dir2 = Move.randomDirection();
					tryMoveScout(dir);
					return dir2;
				}
		
				
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
	public static int[] getOtherTracked() throws GameActionException{
		int[] tracked = new int[SCOUT_LIMIT];
		Arrays.fill(tracked, -1);
		for (int i = 0; i < SCOUT_LIMIT; i++){
			tracked[i] = rc.readBroadcast(5 + SCOUT_CHANNEL + i * SCOUT_MESSAGE_OFFSET);
			
			
		}
		return tracked;
	}
	
    static boolean tryMoveScout(Direction dir) throws GameActionException {
        return tryMoveScout(dir,50,3);
    }

    /**
     * Attempts to move in a given direction, while avoiding small obstacles direction in the path.
     *
     * @param dir The intended direction of movement
     * @param degreeOffset Spacing between checked directions (degrees)
     * @param checksPerSide Number of extra directions checked on each side, if intended direction was unavailable
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMoveScout(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {
    	
    	float testDistance = (float) Math.random() * (float) 2.5;
        // First, try intended direction
        if (rc.canMove(dir, testDistance)) {
            rc.move(dir, testDistance);
            return true;
        }

        // Now try a bunch of similar angles
        boolean moved = false;
        int currentCheck = 1;

        while(currentCheck<=checksPerSide) {
            // Try the offset of the left side
            if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck));
                return true;
            }
            // Try the offset on the right side
            if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck));
                return true;
            }
            // No move performed, try slightly further
            currentCheck++;
        }

        // A move never happened, so return false.
        return false;
    }
}