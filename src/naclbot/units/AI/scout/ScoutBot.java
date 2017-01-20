// AI for soldier under normal control
package naclbot.units.AI.scout;
import java.util.Arrays;

import battlecode.common.*;

import naclbot.units.motion.Move;
import naclbot.variables.GlobalVars;

public class ScoutBot extends GlobalVars {
	
	// Variable for round number
	private static int Rem_is_better;
	
	// Variables for self and team recognition
	public static int id;
	public static int scout_number;
	private static Team enemy;
	private static Team allies;
	
	// Parameter that asserts array storage of scout
	private static final int unitMemorySize = 5;
	private static final int staticMemorySize = 200;
	private static final int dynamicMemorySize = TOTAL_TREE_BROADCAST_LIMIT;
	
	// Parameter that determines whether a scout determines threats to be too great and run away
	private static boolean runAway = false;
	
	// Arrays to store information on enemy archons
	private static RobotInfo[] enemyArchons= new RobotInfo[unitMemorySize];
	private static int[] enemyArchonIDs = new int[unitMemorySize];
	
	// Parameter to determine at which index the archon data was updated
	private static int archonIndex;
	
	// Parameters to store locations of self and the nearest archon
	private static MapLocation base;
	public static MapLocation myLocation;
	
	public static final int senseDistance = 7;
	
	// Arrays that store the IDs of trees seen or sent by the scout
	private static int[] sentTreesIDs = new int[dynamicMemorySize];
	private static int[] knownTreesIDs = new int[staticMemorySize];
	private static int[] receivedTreesIDs = new int[dynamicMemorySize];
	private static int[] dynamicKnownTreesIDs = new int[dynamicMemorySize];
	
	// Array to store the data of trees seen by the scout 
	private static TreeInfo[] knownTrees = new TreeInfo[staticMemorySize];
	
	// Parameters keeping track of number of trees seen and sent
	private static int seenTotal;
	private static int sentTotal;
	
	// Useless param used for debugging reading of other scouts' messages
	private static int receivedTotal;
	
	// Direction at which the scout traveled last
	private static Direction last_direction;
	
	// The ID of the robot the scout is currently tracking and its information
	public static int trackID;	
	public static RobotInfo trackedRobot;
	
	// Stores the number of rounds that the scout has been tracking the current enemy
	private static int roundsCurrentlyTracked;
	
	// Stores the total number of unique enemies the scout has tracked
	private static int trackedTotal;
	
	// Parameter to express if the scout has already broadcasted status information this turn
	public static boolean hasBroadcastedStatus;	
	
	// The archon number of the archon from which this scout will receive its information
	public static int homeArchon;
	
	// The intial round in which the scout was constructed
	public static int initRound;
	
	// The total number of scouts in active service
	private static int currentNumberofScouts;	
    
    // Array of the last three enemies tracked by the scouts 
    public static int[] noTrack = new int[3];   
    public static int noTrackUpdateIndex;   
    
    // Placeholder for desired location to go to
    public static MapLocation desiredMove;
    
    // Place holder to show where the robot had originally intended to go before the trajectory was altered by post-track/move functions
    public static MapLocation lastDesiredMove;
	
    // Boolean for rng rotation direction - true for counterclockwise, false for clockwise
    public static boolean rotationDirection = true;
    
	/************************************************************************
	 ***************** Runtime Functions and Initialization *****************
	 ***********************************************************************/
 
	
	// Function for idle scout - does nothing until something triggers it to move
	// TODO
	
	private static void idle() throws GameActionException{
		
		// Code to be performed every turn
		while (true){			
			try{				
				// Yield time for the next turn
				Clock.yield();
			}			
			catch (Exception e) {
				// SYSTEM CHECK for exceptions in the scout code
                System.out.println("Scout Exception");
                e.printStackTrace();
			}
		}		
	}
	
	
	// Initialization function - makes the default values for most important parameters
	
	public static void init() throws GameActionException {
		
		// SYSTEM CHECK Initialization start check
		System.out.println("I'm a scout!");	
				
        // Important parameters for self
        enemy = rc.getTeam().opponent();
        allies = rc.getTeam();
        id = rc.getID();       
        
        scout_number = rc.readBroadcast(SCOUT_NUMBER_CHANNEL);
        currentNumberofScouts = scout_number + 1;
        
        Rem_is_better = rc.getRoundNum();
        initRound = Rem_is_better;
        
        // Get archon count and set home archon as one of those archons currently in service
        int archonCount = rc.readBroadcast(ARCHON_NUMBER_CHANNEL);
        homeArchon = (int) (Math.random() *archonCount);
        
        // SYSTEM CHECK to see if init() is completed   
        // System.out.println("My home archon has archonNumber of: " + homeArchon);	
        
        // Initialize values relating to tree broadcasting
        seenTotal = 0;
        sentTotal = 0;
        receivedTotal = 0;
   
        
        // Initialize variables important to self
        myLocation = rc.getLocation();
        trackID = -1;
        noTrackUpdateIndex = 0;
        
        // Update SCOUT_CHANNEL
        rc.broadcast(SCOUT_NUMBER_CHANNEL, currentNumberofScouts);
                
        // SYSTEM CHECK to see if init() is completed   
        // System.out.println("Scout successfully initialized!");		
        
        // By default pass on to the main function
        main();        
	}
	
	
	// Main function of the scout
	// Scouts initially move away from the home archon
	// Update team tree list every single turn
	// Shoot enemies with high_priority	
		
	public static void main() throws GameActionException{	            
        
		// Initialize other parameters for tracking
	    roundsCurrentlyTracked = 0;
	    trackedTotal = -1; 
        
	    // Clear memory of last tracked members
	    Arrays.fill(noTrack, -1);    
	    
        // Code to be performed every turn        
        while (true) {
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	// Force base to be null at start of round - rest closest ally
            	base = null;
            	
            	// Update total number of scouts
            	currentNumberofScouts = rc.readBroadcast(SCOUT_CHANNEL);
            	
            	// Since robot has not yet broadcasted this turn, set param to false by default
            	hasBroadcastedStatus = false;
            	
            	// Initialize archon index to invalid value - becomes valid if something is updated
            	archonIndex = -1;
            	
            	// Update Location and location of base as well as refresh the desired travel location
            	myLocation = rc.getLocation();         	
                
            	// Get nearby enemies and allies for use in other functions            	
            	RobotInfo[] enemyRobots = NearbyUnits(enemy);
            	RobotInfo[] alliedRobots = NearbyUnits(allies);
            	
            	// Update the location of the nearest noncombatant allied location
             	RobotInfo NearestAlly = getNearestAlly(alliedRobots);
             	
             	// If there is a friendly noncombatant nearby
             	if(NearestAlly != null){
             		
             		base = NearestAlly.location;
             		
             		// For Initialization - have last direction originally point away from the closest ally, rounded to 30 degree intervals
            		Direction awayAlly = new Direction(myLocation.directionTo(base).radians + (float) Math.PI);
            		float newRadians = (float) (((int) (awayAlly.radians / (float) (Math.PI / 6))) * Math.PI / 6);
            		
            		last_direction = new Direction(newRadians);
            		
            		// SYSTEM CHECK - make sure direction is multiple of 30 degrees
            		System.out.println("Direction updated: nearest ally is in direction opposite to roughly" + last_direction.getAngleDegrees());            		
             	}             	
                
            	// Placeholder for the location where the robot desires to move - can be modified by dodge
            	desiredMove = myLocation.add(last_direction, (float) 2.5);
            	            	
             	/***********************************************************************************
            	 *************************** Actions to be Completed ******************************
            	 **********************************************************************************/
            	
            	// Broadcast Tree Data
            	
            	// Currently on hold until we find use for it... D:
            	// broadcastTree(TOTAL_TREE_BROADCAST_LIMIT);
            	
            	// Other broadcasts -> Only do if the unit is 10 units away from any ally #TODO
            	
            	// SYSTEM CHECK - See if Broadcasting is completed
            	// System.out.println("Broadcasting Completed");
            	
            	// Update the desired place to move to
            	move(enemyRobots);
            	
              	// SYSTEM CHECK - See if move function has been completed
            	// System.out.println("Move Completed");
            	
            	if (rc.canMove(desiredMove)){
            		rc.move(desiredMove);
            	}
            	else{
            		System.out.println("Cannot move to desired location");
            		
            	}
    
            	// Make sure to show appreciation for the one and only best girl in the world.
            	// If you are reading this and you think Emilia is best girl I have no words for you
                Rem_is_better += 1;

                Clock.yield();

            } catch (Exception e) {
                System.out.println("Scout Exception");
                e.printStackTrace();
            }
        }
    }
	
	
	/*****************************************************************************
	 ******************** Tree SEarch and Broadcast Functions ********************
	 ****************************************************************************/
			
    // Function to sense and return the information of all nearby trees
	
    private static TreeInfo[] addTrees(){
    	
    	// Sense all nearby trees
    	TreeInfo[] nearby_trees = rc.senseNearbyTrees(senseDistance);
    	
    	// SYSTEM CHECK to check to see if sensing function is run
    	// System.out.println("Currently sensing location of nearby trees");
    	
    	return nearby_trees; 	    	 	    	    	
    }
    
	
	// Function to Broadcast the locations of all trees found this turn
	
    private static void broadcastTree (int totalBroadcastLimit) throws GameActionException {
    	
    	// Takes one parameter: totalBroadcastLimit
    	// totalBroadcastLimit is an integer representing the maximum number of trees that may be broadcasted in a single turn
   
		// Get information on all trees that are able to be sensed    	
    	TreeInfo[] newTrees = addTrees();
		
    	// Array of the trees that the scout could potentially send this turn    	
    	TreeInfo [] canSend = new TreeInfo[5];
    	int canSendCounter = 0;
    	
		// Iterate through only the trees seen
		for(int i = 0; i < newTrees.length; i++){
			// If there are still trees that the scout can add to the sent list
			if(canSendCounter < 5){
				// Check if the current tree has been seen recently in the dynamic looping array
				if (!arrayContainsInt(dynamicKnownTreesIDs, newTrees[i].ID)) {				
					// Add new tree ID to list of stored IDs of seen trees, stores the corresponding information, and updates the total number of trees seen
					dynamicKnownTreesIDs[seenTotal % dynamicMemorySize] = newTrees[i].ID;
					knownTreesIDs[seenTotal % staticMemorySize] = newTrees[i].ID;
					knownTrees[seenTotal % staticMemorySize] = newTrees[i];
					seenTotal += 1;
					
					//Increment the counter for potential trees to send
					canSend[canSendCounter] = newTrees[i];
					canSendCounter +=1;
					
					
					// SYSTEM CHECK to see which trees the scout will attempt to send
					// System.out.println("Will attempt to update info about tree with ID: " + canSend[canSendCounter-1]);
				}
			}
			// SYSTEM CHECK draw a red dot to notify which trees are not even attempted to be broadcasted this turn
			rc.setIndicatorDot(newTrees[i].location, 200, 200, 200);	
		}
		
		// SYSTEM CHECK to see which trees are currently stored within the dynamic memory of the robot
		for(int i = 0; i < dynamicMemorySize; i++){
			// System.out.println("Current dynamic memory holds: " + dynamicKnownTreesIDs[i]);
		}

		// Parameters to store the trees to be broadcasted this turn and the number of trees that are actually to be sent this turn
		int sentThisTurn = 0;		

		// Get a value as to how many trees have been sent this turn and how many are currently in the array
		int otherSent = rc.readBroadcast(TREES_SENT_THIS_TURN);
		int currentlyStored = rc.readBroadcast(TREE_DATA_CHANNEL);
		
		// Update received data to reflect those gained from other scouts
		if(otherSent > 0){
			
			for(int j = 0; j < otherSent; j++){		
				
				int newTreeID = rc.readBroadcast(TREE_DATA_CHANNEL + (currentlyStored % TOTAL_TREE_NUMBER - j - 1) * TREE_OFFSET + 1);
				
				// System Check - Display new information received
				System.out.println("Noting that Tree with ID: " + newTreeID + "has been broadcasted this turn");
				
				receivedTreesIDs[receivedTotal] = newTreeID;
				receivedTotal += 1;								
			}		
		}		
		
		// SYSTEM CHECK check to see if scouts are reporting this parameter correctly
		System.out.println("Trees sent this turn by other scouts: " + otherSent);
		
		// Parameter to see if any more trees can be sent this turn or no
		boolean filled = false;
		
		// If too many trees have already been sent
		if(sentThisTurn == totalBroadcastLimit - otherSent){			
			// SYSTEM CHECK check to see if Scout can recognize that too many other trees have been sent this turn
			// System.out.println("Too many trees sent this term, cannot send anymore");
			filled = true;			
		}
		
		// Iterate through the trees sensed this turn until it is clear no more can be sent or until the list is exhausted
		while (!filled){
			
			for (int i = 0; i < 5; i++){	
				// If there is actually a tree stored at the current array index
				if (canSend[i] != null){
					// Check to see if the current tree has been sent already or not or if the number of new transmissions this turn is too high
					if (!arrayContainsInt(sentTreesIDs, canSend[i].ID) && (sentThisTurn < totalBroadcastLimit - otherSent)){
						// Check to see if the current tree has been recently received
						if(!arrayContainsInt(receivedTreesIDs, canSend[i].ID)){
					
							// Add the current tree considered to the list of trees to be sent
							TreeInfo toSend = canSend[i];
							
							// SYSTEM CHECK to make sure the scout is actually sending trees
							System.out.println("Currently broadcasting the location of the tree with ID: " + canSend[i].ID);						
							
							// SYSTEM CHECK draw a blue dot to notify which tree is currently being broadcasted
							rc.setIndicatorDot(canSend[i].location, 0, 0, 200);
							
							// Broadcast Information
							rc.broadcast(TREE_DATA_CHANNEL + ((currentlyStored + sentThisTurn) % TOTAL_TREE_NUMBER) * TREE_OFFSET * + 1, toSend.ID);
							rc.broadcast(TREE_DATA_CHANNEL + ((currentlyStored + sentThisTurn) % TOTAL_TREE_NUMBER) * TREE_OFFSET * + 2, (int)(toSend.location.x * 100));
							rc.broadcast(TREE_DATA_CHANNEL + ((currentlyStored + sentThisTurn) % TOTAL_TREE_NUMBER) * TREE_OFFSET * + 3, (int)(toSend.location.y * 100));
							rc.broadcast(TREE_DATA_CHANNEL + ((currentlyStored + sentThisTurn) % TOTAL_TREE_NUMBER) * TREE_OFFSET * + 4, (int)toSend.radius);
							
							
							
							// Add tree to the list of trees sent thus far and increment counters
							sentTreesIDs[sentTotal % dynamicMemorySize] = newTrees[i].ID;
							sentTotal += 1;				
							sentThisTurn += 1;						
						}
					} else{						
						// SYSTEM CHECK draw a red dot to notify which trees are not broadcasted this turn
						rc.setIndicatorDot(canSend[i].location, 200, 0, 0);					
					}				
					// If the number sent thus far is equivalent to the cap of trees sent per turn				
					if(sentThisTurn == totalBroadcastLimit - otherSent){						
						// SYSTEM CHECK check to see if Scout can recognize that too many other trees have been sent this turn
						// System.out.println("I cannot send anymore trees");
						
						filled = true;
					}
				}
			}			
			filled = true;
		}
		
		// Update total number of trees in array count and total number sent this turn
		rc.broadcast(TREE_DATA_CHANNEL, currentlyStored + sentThisTurn);
		rc.broadcast(TREES_SENT_THIS_TURN, otherSent + sentThisTurn);
    }
	
	/*****************************************************************************
	 ****************** Tracking and Motion Related Functions ********************
	 ****************************************************************************/   
    
    
    // Overarching move function for the scout
    private static void move(RobotInfo[] enemyRobots) throws GameActionException{
    	
    	// If the robot is currently not tracking anything
    	if(trackID == -1){    		
    		// See if a robot to be tracked can be found
    		trackedRobot = findNewTrack(enemyRobots);    
    		
    		// SYSTEM CHECK - see if the robot recognizes that it is currently not tracking anything
    		System.out.println("Currently not tracking anything");
    		
    		// If there is a robot
    		if (trackedRobot != null){
    			// Update the trackID
    			trackID = trackedRobot.ID;   			
    			
    			// SYSTEM CHECK - Notify what the robot will now track
        		System.out.println("The scout will now track the robot with ID: " + trackID);
    			
    			// Call move again with the updated information
    			move(enemyRobots);    	
    		
    		} else{ // If there is no robot to be tracked 
    			// Check to see if a move in the last moved direction or in the forward field of view is possible 
    			MapLocation testMove = tryMoveScout(last_direction);
    			
        		// SYSTEM CHECK - Notify that nothing to be scouted has been found
        		System.out.println("The scout cannot find anything to track");
        		
    			
    			// If the robot cannot move to any of the tested locations try the opposite direction 
    			if(testMove == null){
    				Direction new_attempt = new Direction(last_direction.radians + (float)Math.PI);
    				MapLocation newTestMove = tryMoveScout(new_attempt);
    				desiredMove = newTestMove;
    										
    			} else{
    				// If it can move to that location update the desired location with the data
    				desiredMove = testMove;
    			}    			
    		}
    	} else{ // If the robot is actually currently tracking something
    		// If the currently tracked robot is a gardener, execute special tracking method
    		if (trackedRobot.type == battlecode.common.RobotType.GARDENER){
    			trackGardener();
    			
    		} else{ // Otherwise execute the standard tracking method
    			track(enemyRobots);    		
    		}
    	}        	
    }
    
	
	
	// Function to retrieve the nearest enemy to the robot
	
	private static RobotInfo getNearestEnemy(RobotInfo[] enemyRobots){

		// Parameter to store the smallest distance to another robot
		float minimum = Integer.MAX_VALUE;
				
		// Parameter to store the array index of the closest robot				
		int index = -1;			
		for (int i = 0; i < enemyRobots.length; i++){
			
			// If the type of the robot spotted is a gardener, return it
			if (enemyRobots[i].type == battlecode.common.RobotType.GARDENER){
				
				return enemyRobots[i];
			} else{		
				// Otherwise search for the closest one that has not recently been tracked
				float dist = myLocation.distanceTo(enemyRobots[i].location);
				if (dist < minimum ){
					// If the robot has not been tracked recently
					if (!arrayContainsInt(noTrack, enemyRobots[i].ID)){
						// Update no track and the index
						noTrack[noTrackUpdateIndex] = enemyRobots[i].ID;
						noTrackUpdateIndex += 1;
						
						minimum = dist;
						index = i;		
					}
				}			
			}
		}
		// This should always happen, but if the found index is positive return the closest robot
		if (index >= 0){	
			
			return enemyRobots[index];	
			
		} else{			
			return null;
		}		
	}
	
	
	// Wrapper function for finding a new enemy to track
    
    private static RobotInfo findNewTrack(RobotInfo[] enemyRobots){
    	
    	// If there is actually an enemy robot nearby
    	if (enemyRobots.length > 0){
    		// Return the closest one or gardener    		
    		return getNearestEnemy(enemyRobots);    		
    	} else{
    		// Otherwise return that there is no enemy to be found
    		return null;    		
    	}    
    }
   
    
    // Function to execute when the robot is attempting to track down a gardener
    private static void trackGardener() throws GameActionException{
    	moveTowardsTarget();
    }
    
    
    // Function to follow a unit and approach it
    
	private static void track(RobotInfo[] enemyRobots) throws GameActionException{
		
		// If the robot can currently sense the robot it is tracking and if it has not been tracking this robot for too long
    	if (rc.canSenseRobot(trackID) && roundsCurrentlyTracked < 10){
    		
    		// SYSTEM CHECK - See if the robot identifies that it is actually tracking something
    		System.out.println("I am currently tracking a robot with ID: " + trackID);
    		
    		// Update location of tracked robot 
    		trackedRobot = rc.senseRobot(trackID);
    		// SYSTEM CHECK - Draw a violet line between current position and position of robot
    		rc.setIndicatorLine(myLocation, trackedRobot.location, 150, 0, 200);
    		
    		// Increment number of rounds tracked
        	roundsCurrentlyTracked +=1;
        	
        	moveTowardsTarget();    
        	
        // If the robot has been tracking its current prey for too long or has lost sight of its target
    	} else {
    		
    		// If the robot has been tracking the current enemy for a long time        	
        	trackID = -1;
        	roundsCurrentlyTracked = 0;  
        	
        	// SYSTEM CHECK - Notify of target loss
        	System.out.println("Lost sight of target/Finding a new target");        	
        	
        	// Call move to obtain a new location to try to move to
        	move(enemyRobots);       	   	
    	}	                		
    }
	
	
	// Function to move towards a given robot
	
	private static void moveTowardsTarget() throws GameActionException{
		
		// Variable to store the distance to the robot currently being tracked
		float gap = myLocation.distanceTo(trackedRobot.location);
		
		// Get the direction to the target enemy
    	Direction dir = myLocation.directionTo(trackedRobot.location);
    	
    	// If the gap is large enough move directly towards the target
    	if (gap > 4.5){
    		desiredMove = myLocation.add(dir, (float) 2.5);
    	}
    	
    	// If the gap is slightly smaller, moves so that the approach is not so direct
    	else if (gap > 3){	    		
    		// If the object was set to be rotating counterclockwise, go clockwise
    		if (rotationDirection){	    			
    			// Rotate 30 degrees clockwise
    			Direction newDir = new Direction(dir.radians - (float) (Math.PI/6));
    			
    			// Set new move point
    			desiredMove = myLocation.add(newDir, (float) 2.5);
    			
    			// Set rotation direction to be clockwise
    			rotationDirection = false;	    			
    		}
    		else{
    			// Rotate 30 degrees counterclockwise
    			Direction newDir = new Direction(dir.radians + (float) (Math.PI/6));
    			
    			// Set new move point
    			desiredMove = myLocation.add(newDir, (float) 2.5);
    			
    			// Set rotation direction to be counterclockwise
    			rotationDirection = true;	    				    			
    		}	    		
    	}
    	else{
    		// If the robot was supposed to be going counterclockwise, continue
    		if (rotationDirection){
    			// Calculate the direction from the target that you want to end up at
    			Direction fromDir = new Direction(dir.radians - (float) (2 * Math.PI/3));
    			
    			// Obtain the desired target location
    			desiredMove = trackedRobot.location.add(fromDir, (float) 1.5);
    			
    		} else{
    			// Calculate the direction from the target that you want to end up at
    			Direction fromDir = new Direction(dir.radians + (float) (2 * Math.PI/3));
    			
    			// Obtain the desired target location
    			desiredMove = trackedRobot.location.add(fromDir, (float) 1.5);	    			
    		}
    	// SYSTEM CHECK Print line from current location to intended move location
    	rc.setIndicatorLine(myLocation, desiredMove, 0, 200, 200);   			
    	}   		
	}	
		
	/*****************************************************************************
	 ******************* Miscellaneous Functions************** ********************
	 ****************************************************************************/   
	
	// Get location of home Archon if it has broadcasted previously
	
	private static MapLocation updateArchon() throws GameActionException{
		
		MapLocation base = new MapLocation(rc.readBroadcast(1 + homeArchon * ARCHON_OFFSET), rc.readBroadcast(2 + homeArchon * ARCHON_OFFSET));
		
		// If no base has yet been broadcasted
		if (base.x ==  0 && base.y == 0){
			return null;
		} else{ // If they have been broadcasted
			return base;
		}	
	}
	
	
	// Function to obtain the robot info units in the specified team
	
	private static RobotInfo[] NearbyUnits(Team team){
		
		return rc.senseNearbyRobots(-1, team);
	}
	
	
	// Function to obtain the data for the nearest ally to the robot currently (only gardeners and archons)
	
	private static RobotInfo getNearestAlly(RobotInfo[] currentAllies){
    	
    	float minimum = Integer.MAX_VALUE;
		
		int index = -1;
		
		for (int i = 0; i < currentAllies.length; i++){
			// Only consider allies that are archons or gardeners
			if (currentAllies[i].type == battlecode.common.RobotType.ARCHON || currentAllies[i].type == battlecode.common.RobotType.GARDENER){
				
				float dist = myLocation.distanceTo(currentAllies[i].location);

				if (dist < minimum ){					
							
					minimum = dist;
					index = i;	
				}		
			}			
		}
		// If such an ally has been found return its data or otherwise return null
		if (index >= 0){
			
			// SYSTEM CHECK - Check to see if the robot returns a valid ally
			// System.out.println("I have an ally nearby and its ID is: " + currentAllies[index].ID);
			
			return currentAllies[index];
		} else{
			return null;
		}
    }
	

	// Function to attempt to move in a target direction
	
    private static MapLocation tryMoveScout(Direction dir) throws GameActionException {
    	// If only a direction is given  use arbitrary values
        return tryMoveScout(dir,30,3);
    }    

    // Function to attempt to move in a target direction (with inputed values), returns true if it actually can move in that direction
    
    private static MapLocation tryMoveScout(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {
    	
    	// Generate distances to test 
    	for (int i = 0; i < 5; i++){
    		
    		float testDistance = (float)(2.5 - i * 0.5);    		
	        // Try going the test distance in the targeted direction
	        if (rc.canMove(dir, testDistance)) {	            
	            return myLocation.add(dir, testDistance);
	        }
        }

    	// Now check with 30 degree offsets to either side of the intended direction
        int currentCheck = 1;
        while(currentCheck<=checksPerSide) {
            // Try the offset of the left side
            if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
                return myLocation.add(dir.rotateLeftDegrees(degreeOffset*currentCheck), (float) 2.5);
            }
            // Try the offset on the right side
            if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
                return myLocation.add(dir.rotateRightDegrees(degreeOffset*currentCheck), (float) 2.5);
            }
            // No move performed, try slightly further to either direction
            currentCheck+=1;
        }
        // A move through the checks cannot happen, so return a null to express this
        return null;
    }    


//Get the nearest enemy to the last updated location of base
	private static MapLocation getNearestEnemytoBase(RobotInfo[] enemies, boolean update){
		
		// Smallest distance to another robot
		float minimum = Integer.MAX_VALUE;
		boolean updated = false;			
		// arrayIndex of the closest robot defaults to the first					
		int arrayIndex = 0;
		for (int i = 0; i < enemies.length; i++){
			
			if (enemies[i].type == battlecode.common.RobotType.ARCHON && !updated && update){
				if (!arrayContainsInt(enemyArchonIDs, enemies[i].ID)){
					for (int j = 0; j < enemyArchons.length; j++){
						if (enemyArchons[j] == null && !updated){
							enemyArchons[j] = enemies[i];
							enemyArchonIDs[j] = enemies[i].ID;
							archonIndex = (int) j;
							updated = true;									
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
}	
  



