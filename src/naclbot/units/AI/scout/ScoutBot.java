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
        int Rem_is_better = rc.getRoundNum();
        int track_id = -1;
        MapLocation base = updateBase();  
        MapLocation myLocation = rc.getLocation();
        int currently_tracked = 0;
        TreeInfo[] seen_Trees = new TreeInfo[100];
    
        int[] sent_TreesID = new int[100];
        int[] seen_TreesID = new int[100];
        int seen_notSent = 0;
 
        int seen_total = 0;
        int sent_total = 0;
        
        int sent_index = 0;
        
        // Array to store number of enemies tracked to date
        int[] no_track = new int[3];
        int tracked_total = -1;
        
   
        
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
               			//Broadcast own coordinates and coordinates of nearest robots
            			rc.broadcast(1 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)myLocation.x);
            			rc.broadcast(2 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)myLocation.y);   
                     	rc.broadcast(9 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, id);
                    	rc.broadcast(10 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, 1);
                    	hasBroadcasted = true;
        			}
        			
        			
     

   
        		}
        		
        		// On every offset 2, broadcast the locations of two trees that you have not yet broadcasted before....
        		// Should also check the broadcasts of other scouts to see if they have sent out info about that tree too
        		else if (Rem_is_better % SCOUT_UPDATE_FREQUENCY == 2){
        			
        			
        			// Update Tree storage and broadcast any unsent trees
        			TreeInfo[] newTrees = addTrees();
        			for(int i = 0; i < newTrees.length; i++){
        				
        				// Check if the current tree has been seen before or no
        				if(!Arrays.asList(seen_TreesID).contains(newTrees[i].ID)) {
        					seen_TreesID[seen_total % 100] = newTrees[i].ID;
        					seen_Trees[seen_total % 100] = newTrees[i];
        					seen_total += 1;     					     					       					
        				}
        				
        			}
        			
        
        			
        			// Update trees that have been sent
        			int[] updatedSent = retrieveTrees();
        			for(int i=0; i<updatedSent.length; i++){
        				if (updatedSent[i] > 0 && !Arrays.asList(sent_TreesID).contains(updatedSent[i])){
        					sent_TreesID[sent_total %100] = updatedSent[i];
        					sent_total += 1;
        					
        				}
        			}
        			
        			int sentThisTurn = 0;
        			TreeInfo[] toSend = new TreeInfo[2];
        			for (int i = sent_index % 100; i < 100; i++){
        				if (!Arrays.asList(sent_TreesID).contains(seen_TreesID[i])){
        					toSend[sentThisTurn] = seen_Trees[i];
        					sent_TreesID[sent_total % 100] = seen_TreesID[i];
        					sent_total += 1;
        					sentThisTurn += 1;     							      					
        				}
        				
        					
        			}
        			
        			if (sentThisTurn > 0){
        				
        				rc.broadcast(3 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)toSend[0].ID);
                    	rc.broadcast(4 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)toSend[0].location.x);
                    	rc.broadcast(5 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)toSend[0].location.y);        				
        			}
        			
        			if (sentThisTurn > 1){
        				
        				rc.broadcast(6 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)toSend[1].ID);
                    	rc.broadcast(7 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)toSend[1].location.x);
                    	rc.broadcast(8 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)toSend[1].location.y);        				
        			}
        			
        			rc.broadcast(1 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)myLocation.x);
                	rc.broadcast(2 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)myLocation.y);              
                	
                   	rc.broadcast(9 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, id);
                	rc.broadcast(10 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, 3);
                	hasBroadcasted = true;
        			
        		}
        		// Regular broadcast
        		if (!hasBroadcasted){
        			rc.broadcast(1 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)myLocation.x);
        			rc.broadcast(2 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)myLocation.y);   
        			rc.broadcast(9 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, id);
                	rc.broadcast(10 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, 0);
        		}
        		
        		
        		// Too many enemies nearby will commit sudoku
        		if (robots.length > 5){
        			
    				base = updateBase();        			
        			MapLocation nearest = getNearestEnemytoBase(base, robots);
        			rc.broadcast(1 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)myLocation.x);
                	rc.broadcast(2 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)myLocation.y);              
                	rc.broadcast(3 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)nearest.x);
                	rc.broadcast(4 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)nearest.y);
                	rc.broadcast(9 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, id);
                	rc.broadcast(10 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, 2);
    			
        			int x = rc.readBroadcast(SCOUT_CHANNEL);
        			rc.broadcast(SCOUT_CHANNEL, x-1);
        			rc.disintegrate();
        		}
            	
                // If there is no current enemy being tracked
                if (track_id == -1){
               
                	int[] already_tracked = getOtherTracked();
                	
                	//If there is a possible robot to be tracked
                	if (robots.length > 0 && getNearestEnemy(myLocation, robots, no_track, already_tracked)!= null){
                	 
                        RobotInfo quandary = getNearestEnemy(myLocation, robots, no_track, already_tracked);
                        track_id = quandary.ID;
                    	last_direction = moveTowards(quandary, myLocation);
                    	rc.broadcast(5 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, track_id);
                    	rc.broadcast(10 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, 4);
                    	
                    	tracked_total+=1;
                    	currently_tracked = 0;
            
                  
                    	
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
                	if (rc.canSenseRobot(track_id) && currently_tracked < 50){
                    	RobotInfo quandary = rc.senseRobot(track_id);
                    	// if the robot's current location is far from the 
                    	last_direction = moveTowards(quandary, myLocation);
                    	currently_tracked +=1;                 	
                    	
                			
                	
                		
                		
                	} else if (currently_tracked >= 50){
                    	no_track[tracked_total % 3] = track_id;
                    	track_id = -1;
                    	currently_tracked = 0;
                		
                		
                		
                	} else{
                		track_id = -1;
                		currently_tracked = 0;
                		// Go towards last known position for one turn
                   		if (rc.canMove(last_direction)|| rc.isLocationOccupiedByTree(myLocation.add(last_direction, (float)2.5))){
                   			rc.move(last_direction);
                   		}
                   		
                   		if (!rc.hasMoved()){
                     		int i = 0; 
                     	
                     		while(!rc.hasMoved() && i < 10){
                     			Direction adir = Move.randomDirection();
                     			tryMoveScout(adir);
                     			i+=1;
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
	
	public static RobotInfo getNearestEnemy(MapLocation myLocation, RobotInfo[] enemies, int[] ikenai, int[] shinai){
		
		// Smallest distance to another robot
		float minimum = 1000;
				
		// Index of the closest robot defaults to the first					
		int index = -1;
		for (int i = 0; i < enemies.length; i++){
			float dist = myLocation.distanceTo(enemies[i].location);
			if (dist > minimum ){
				if (Arrays.asList(ikenai).contains(enemies[i].ID) || Arrays.asList(shinai).contains(enemies[i].ID)){
					;
				}
				else{
					minimum = dist;
					index = i;
				}
			}			
		}
		if (index != -1){
			return enemies[index];	
		}
		else{
			return null;
		}
		
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
            currentCheck+=1;
        }

        // A move never happened, so return false.
        return false;
    }
    
    public static TreeInfo[] addTrees(){
    	TreeInfo[] nearby_trees = rc.senseNearbyTrees();
    	return nearby_trees;   	
    	 	
    	    	
    }
    public static int[] retrieveTrees() throws GameActionException{
    	int[] receivedTreeIDs = new int [SCOUT_LIMIT*2];
		Arrays.fill(receivedTreeIDs, -1);
		for (int i = 0; i < SCOUT_LIMIT; i++){
			if(rc.readBroadcast(10 + SCOUT_CHANNEL + i * SCOUT_MESSAGE_OFFSET) == 3){
				receivedTreeIDs[2*i] = rc.readBroadcast(3 + SCOUT_CHANNEL + i * SCOUT_MESSAGE_OFFSET);
				receivedTreeIDs[2*i + 1] = rc.readBroadcast(6 + SCOUT_CHANNEL + i * SCOUT_MESSAGE_OFFSET);			
			}
		}
		return receivedTreeIDs;    	
    }

}