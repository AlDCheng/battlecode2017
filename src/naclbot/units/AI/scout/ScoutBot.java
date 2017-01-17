// AI for soldier under normal control
package naclbot.units.AI.scout;
import java.util.Arrays;

import battlecode.common.*;
import battlecode.instrumenter.inject.System;
import naclbot.units.motion.Move;
import naclbot.variables.GlobalVars;


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
	
	private static int Rem_is_better;
	public static int id;
	public static int scout_number;
	private static Team enemy;
	
	private static final int memorySize = 15;
	
	private static boolean runAway = false;
	
	public static RobotInfo[] enemyArchons= new RobotInfo[3];
	private static int[] enemyArchonIDs = new int[3];
	
	private static MapLocation base;
	private static MapLocation myLocation;
	
	private static int[] sentTreesIDs = new int[memorySize];
	private static int[] seenTreesIDs = new int[memorySize];
	
	private static TreeInfo[] seenTrees = new TreeInfo[memorySize];
	
	public static int seenTotal;
	public static int sentTotal;
	public static int sentIndex;
	
	public static Direction last_direction;
	
	public static int trackID;
	
	public static int currentlyTracked;
	public static int trackedTotal;
	
	public static boolean hasBroadcasted;
	public static int index;
	  
 
	public static void idle()throws GameActionException{
		while (true){
			try{
				
			}
			catch (Exception e) {
                System.out.println("Scout Exception");
                e.printStackTrace();
			}
		}
		
	}
	public static void init() throws GameActionException {
		System.out.println("I'm a scout!");	
				
        // Important parameters for self
        enemy = rc.getTeam().opponent();
        id = rc.getID();
        scout_number = rc.readBroadcast(SCOUT_CHANNEL);
        
        // Get round number
 
        Rem_is_better = rc.getRoundNum();
        
        // Iniitalize default array values
        Arrays.fill(enemyArchonIDs, -1);        
        Arrays.fill(seenTreesIDs, -1);
        Arrays.fill(sentTreesIDs, -2);
        
        // Values relating to tree broadcasting
        seenTotal = 0;
        sentTotal = 0;
        sentIndex = 0;    
        
        base = updateBase();  
        myLocation = rc.getLocation();       
        trackID = -1;
                
        // System check to see if init() is completed   
        
        rc.broadcast(SCOUT_CHANNEL, scout_number + 1);
        
		System.out.println("My scout_id is: " + scout_number);    
                       
        main();        
	}
	
	public static void main() throws GameActionException{	
	            
        // initial starting movement away from Archon
		last_direction = new Direction(myLocation.directionTo(base).radians + (float) Math.PI);
	 
          
	    currentlyTracked = 0;
	    trackedTotal = -1;
	    int[] noTrack = new int[3];
        Arrays.fill(noTrack, -1);
        
  
        
        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	hasBroadcasted = false;
            	
            	index = -1;
            	
            	// Update Location
            	myLocation = rc.getLocation();         
                
            	// Get nearby enemies            	
            	RobotInfo[] enemyRobots = NearbyEnemies(enemy);
            	
            	
             	/***********************************************************************************
            	 * *************************** Code for Broadcasting *********************
            	 **********************************************************************************/
            	
            	
            	
            	// Once in a while broadcast to base new information
            	// (editor's note: Rem = round num)
        		if (Rem_is_better % SCOUT_UPDATE_FREQUENCY == 1){
        			base = updateBase();
        			
        			if (enemyRobots.length>0){
        				MapLocation nearest = getNearestEnemytoBase(enemyRobots,  true);
                 
                    	rc.broadcast(3 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)nearest.x);
                    	rc.broadcast(4 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)nearest.y);

                    	
               			//Broadcast own coordinates and coordinates of nearest robots
            			rc.broadcast(1 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)myLocation.x);
            			rc.broadcast(2 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)myLocation.y);   
                     	rc.broadcast(9 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, id);
                     	System.out.println("index" + index);
                     	if (index != -1){
                     		rc.broadcast(10 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, 5);
                     		
                     		rc.broadcast(5 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)enemyArchonIDs[index]);
                        	rc.broadcast(6 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)enemyArchons[index].location.x);
                        	rc.broadcast(7 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)enemyArchons[index].location.y);
                        	System.out.println("NORMIE PIECE OF SHIT ID: " + enemyArchonIDs[index] + "xPos: " + enemyArchons[index].location.x + "yPos: "+ enemyArchons[index].location.y);
                     	}
                     	else{
                     		
                     		rc.broadcast(10 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, 1);
                     	}
                    	hasBroadcasted = true;
        			}
        		}
        		
        		// On every offset 2, broadcast the locations of two trees that you have not yet broadcasted before....

        		else if (Rem_is_better % SCOUT_UPDATE_FREQUENCY == 2){
        			System.out.println(" Broadcast Tree");
        			
        			broadcastTree (2, SCOUT_CHANNEL, scout_number, SCOUT_MESSAGE_OFFSET);      
        			System.out.println(" Broadcast Tree");
        
        			if (hasBroadcasted){
        				
        				rc.broadcast(10 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, 3);
        			}
                	
                
        	    	
        		}
        		
        		// Regular broadcast
        		if (!hasBroadcasted){
        			rc.broadcast(1 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)myLocation.x);
                			
        			rc.broadcast(2 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)myLocation.y);
                	rc.broadcast(SCOUT_TRACKING + scout_number,  trackID);                	           	
        			rc.broadcast(9 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, id);
                	rc.broadcast(10 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, 0);
        		}
        		
        		
        		// Too many enemies nearby will commit sudoku
        		if (enemyRobots.length > 3){
                	System.out.println(" OMG WHY DO THEY LIKE EMILIA SO MUCH FUCKING KILL ME");
        			
    				base = updateBase();        			
        			MapLocation nearest = getNearestEnemytoBase(enemyRobots, false);
        			rc.broadcast(1 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)myLocation.x);
                	rc.broadcast(2 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)myLocation.y);              
                	rc.broadcast(3 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)nearest.x);
                	rc.broadcast(4 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, (int)nearest.y);

                	          	
                	rc.broadcast(9 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, id);
                	rc.broadcast(10 + SCOUT_CHANNEL + scout_number * SCOUT_MESSAGE_OFFSET, 2);
                	
                	/***********************************************************************************
                	 * *************************** Code for Movement for next turn *********************
                	 **********************************************************************************/
                	
                	
                	
    	
                	if (!runAway){	
                		Direction testDir = Move.randomDirection();
                		tryMoveScout(testDir);
                		last_direction = testDir;
                		runAway = true;
                		
                	}
                	else{
	                	if (rc.canMove(last_direction)){
	            			rc.move(last_direction, (float)2.5);
	            		
	            			
	            		}
	            		else{
	            			Direction testDir = Move.randomDirection();
	            			tryMoveScout(testDir);
	            			last_direction = testDir;
            		
	            		}
        			
                	}
                	trackID = -1;
                	currentlyTracked = 0;
        		}
        		
        		// If there are not many enemies around...
        		else{               			
        			track(enemyRobots, noTrack);       			
        		}
        		
        		System.out.println("Kitty says hi");
            	      		
        		                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Rem_is_better += 1;

                Clock.yield();

            } catch (Exception e) {
                System.out.println("Scout Exception");
                e.printStackTrace();
            }
        }
    }
	
	private static void track(RobotInfo[] robots, int[] noTrack) throws GameActionException{


		
        // If there is no current enemy being tracked
		if (trackID == -1){
			
			runAway = false;        	
        	int[] already_tracked = getOtherTracked();
        	
        	//If there is a possible robot to be tracked
        	if (robots.length > 0 && getNearestEnemy(myLocation, robots, noTrack, already_tracked)!= null){

        	 
                RobotInfo quandary = getNearestEnemy(myLocation, robots, noTrack, already_tracked);
                trackID = quandary.ID;
            	last_direction = moveTowards(quandary);
            	rc.broadcast(SCOUT_TRACKING + scout_number,  trackID);
        		System.out.println("I am now tracking an enemy robot with ID: " + trackID);
            	
            	
            	trackedTotal+=1;
            	currentlyTracked = 0;
            	
        	} else{
        		
        		rc.broadcast(SCOUT_TRACKING + scout_number,  -1);

        		if (rc.canMove(last_direction)){
        			rc.move(last_direction, (float)2.5);               		
        		}
        		
        		else{
        			Direction testDir = Move.randomDirection();
        			tryMoveScout(testDir);
        			last_direction = testDir;                		
        		}
        	}
       
        	
        // Otherwise if already tracking 
   
        } else{                	
        	
        	// If the robot to be tracked is visible - move towards visible location to within 5 units
        	if (rc.canSenseRobot(trackID) && currentlyTracked < 10){
        		
            	RobotInfo quandary = rc.senseRobot(trackID);
            	// if the robot's current location is far from the 
            	last_direction = moveTowards(quandary);
            	currentlyTracked +=1;                	
                  		
        	} else if (currentlyTracked >= 10){
        		
        		System.out.println("Switching Targets");
        		
            	noTrack[trackedTotal % 3] = trackID;		
				
				
				Direction testDir = Move.randomDirection();
    			tryMoveScout(testDir);
    			last_direction = testDir;        			           		
            	trackID = -1;
            	currentlyTracked = 0;               		
        		                		
        	} else{
        		
        		trackID = -1;
        		currentlyTracked = 0;
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
	}
	
	// Get All Nearest Enemies
	
	private static RobotInfo[] NearbyEnemies(Team nemesis){
		return rc.senseNearbyRobots(-1, nemesis);
	}
	
	// Get Nearest trackable enemy
	private static RobotInfo getNearestEnemy(MapLocation myLocation, RobotInfo[] enemies, int[] ikenai, int[] shinai){
		System.out.println("Checking");		
		// Smallest distance to another robot
		float minimum = Integer.MAX_VALUE;
				
		// Index of the closest robot defaults to the first					
		int index = -1;
		for (int i = 0; i < enemies.length; i++){

			float dist = myLocation.distanceTo(enemies[i].location);

			if (dist < minimum ){
				
				if (!arrayContainsInt(ikenai, enemies[i].ID)){
					
					 if (!arrayContainsInt(shinai, enemies[i].ID)){
						minimum = dist;
						index = i;
	
					}
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

	// move twoards a target robot
	private static Direction moveTowards(RobotInfo quandary) throws GameActionException{
		
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
	
	// Get the nearest enemy to the last known location of the archon
	private static MapLocation getNearestEnemytoBase(RobotInfo[] enemies,boolean update){
		
		// Smallest distance to another robot
		float minimum = Integer.MAX_VALUE;
		boolean updated = false;			
		// arrayIndex of the closest robot defaults to the first					
		int arrayIndex = 0;
		for (int i = 0; i < enemies.length; i++){
			
			if (enemies[i].type == battlecode.common.RobotType.ARCHON && !updated && update){
				if (!arrayContainsInt(enemyArchonIDs, enemies[i].ID)){
					for (int j = 0; j < enemyArchons.length; j++){
						if (enemyArchons[j] == null && updated){
							enemyArchons[j] = enemies[i];
							enemyArchonIDs[j] = enemies[i].ID;
							index = (int) j;
							updated = true;
							System.out.println("I SEE A NEW ARCHON OMG IT LIKES EMILIA PLZ KILL NORMIE PIECE OF SHIT PLS NAO: " + enemies[i].ID + "sighted:" + index);
							
						}
					}
				}
			}
			
			float dist = base.distanceTo(enemies[i].location);
			if (dist > minimum){
				minimum = dist;
				arrayIndex = i;
			}			
		}
		return enemies[arrayIndex].location;		
	}
	
	// Get location of starting archon
	private static MapLocation updateBase() throws GameActionException{
		
		MapLocation base = new MapLocation(rc.readBroadcast(ARCHON_CHANNEL), rc.readBroadcast(ARCHON_CHANNEL + 1));
		return base;		
		
	}
	
	// Get the IDs of the other tracked units
	private static int[] getOtherTracked() throws GameActionException{
		
		int[] tracked = new int[SCOUT_LIMIT];
		Arrays.fill(tracked, -1);
		
		for (int i = 0; i < SCOUT_LIMIT; i++){
			tracked[i] = rc.readBroadcast(SCOUT_TRACKING + i);		
			
		}
		return tracked;
	}
	
	// attempt to move the Scout in the given direction
    private static boolean tryMoveScout(Direction dir) throws GameActionException {
        return tryMoveScout(dir,50,3);
    }

    
    private static boolean tryMoveScout(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {
    	
    	float testDistance = (float) Math.random() * (float) 2.5;
        // First, try intended direction
        if (rc.canMove(dir, testDistance)) {
            rc.move(dir, testDistance);
            return true;
        }

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
    
    // sense nearby trees
    private static TreeInfo[] addTrees(){
    	TreeInfo[] nearby_trees = rc.senseNearbyTrees();
    	return nearby_trees;   	
    	 	    	    	
    }
    
    // broadcast all found trees
    private static void broadcastTree (int broadcastLimit, int typeChannel, int typeNumber, int typeOffset) throws GameActionException {
    	
    	// Update Tree storage and broadcast first two previously unsent trees
    	
    	// seenTrees is the TreeInfo array containing the TreeInfo for all of the trees stored in memory by this particular robot
    	// sentTreesIDs is the array/memory that the robot has of all the trees whos info IT has sent
    	// seenTreesIDs is the array/memory that the robot has of all the trees that it has seen - acts like a dictionary key system for the seenTrees array
    	// memorySize is the number of entries that the robot stores for each of the datatypes above. For a scout this value is 50
    	// broadcastLimit is the number of trees that this robot can broadcast within its offest limit. This is either one or two
    	// int typeChannel, typeNumber and typeOffset are the initial channel for this type of robot,  the number of the type that this robot is and the offset that this type of robot has.
    	
    	// Update trees that are able to be sensed
    	int[] update_data = new int [4];
    	Arrays.fill(update_data, -1);


    	
		TreeInfo[] newTrees = addTrees();
		for(int i = 0; i < newTrees.length; i++){
			
			// Check if the current tree has been seen before or no
			if (!arrayContainsInt(seenTreesIDs, newTrees[i].ID)) {
				
				// Add new tree ID to list of stored IDs of seen trees
				seenTreesIDs[seenTotal % memorySize] = newTrees[i].ID;
				seenTrees[seenTotal % memorySize] = newTrees[i];
				seenTotal += 1;     					     					       					
			}			
		}

		// Decide the trees to be sent and send
		int sentThisTurn = 0;
		TreeInfo[] toSend = new TreeInfo[broadcastLimit];
		while (sentThisTurn < broadcastLimit){
			for (int i = sentIndex % memorySize; i < memorySize; i++){
				if (!arrayContainsInt(sentTreesIDs, seenTreesIDs[i]) && (sentThisTurn < broadcastLimit)){
					if(seenTrees[i] != null) {
						toSend[sentThisTurn] = seenTrees[i]; //Error here?
						sentTreesIDs[sentTotal % memorySize] = seenTreesIDs[i];
						sentTotal += 1;
				
						sentThisTurn += 1;
					}
				}
				sentIndex += 1;
			}
		}
		

		// Information of first tree to be sent
		if (sentThisTurn > 0){
			hasBroadcasted = true;
			
			rc.broadcast(1 + typeChannel + typeNumber * typeOffset, toSend[0].ID);
        	rc.broadcast(2 + typeChannel + typeNumber * typeOffset, (int)toSend[0].location.x);
        	rc.broadcast(3 + typeChannel + typeNumber * typeOffset, (int)toSend[0].location.y);
        	rc.broadcast(4 + typeChannel + typeNumber * typeOffset, (int)toSend[0].radius);        		
		}
		
		// Information of second tree to be sent
		
		if (sentThisTurn > 1){
			
			rc.broadcast(5 + typeChannel + typeNumber * typeOffset, toSend[1].ID);
			rc.broadcast(6 + typeChannel + typeNumber * typeOffset, (int)toSend[1].location.x);
        	rc.broadcast(7 + typeChannel + typeNumber * typeOffset, (int)toSend[1].location.y);
        	rc.broadcast(8 + typeChannel + typeNumber * typeOffset, (int)toSend[1].radius);        				
		}
	
		rc.broadcast(9 + typeChannel + typeNumber * typeOffset, sentThisTurn);		
    
      
    

    }
}

