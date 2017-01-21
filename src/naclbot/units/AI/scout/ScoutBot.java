// AI for soldier under normal control
package naclbot.units.AI.scout;
import java.util.Arrays;


import battlecode.common.*;
import naclbot.units.motion.Yuurei;
import naclbot.variables.GlobalVars;


/* Short List of Things to Do...............
 * 
 * Get scouts to run away from things
 * 
 * Get Scouts to Kill Gardeners 
 *
 * Get Scouts to Broadcast Locations of any enemies that are near allied non combatants...
 * 
 * Improve Dodge - Post process desired location.....
 * 
 * Get Scouts to Shoot things that don't shoot them
 * 
 */


/* Brief Overview of Indicator Lines.....
 * 
 *  Yellow Line - Robot is currently RUNNING AWAY - Yellow line indicates the position it wishes to flee from
 *  Black Dot - Any enemy the scout deems as too hostile to track will be marked by a black dot
 *  
 *  Pink Dot - Any tree blocking a line of fire of the scout
 *  
 *  Grey Line - Under normal operation, the scout chooses to display a grey line for the location it wishes to travel to
 *  Orange Line - If the scout originally intended to move in a manner that doesn't work display this line
 *  
 *  Purple Line - While the scout is tracking, this shows the enemy the scout is currently tracking...
 *  Light Blue Line - While the scout is tracking, this shows where the scout originally intended to go
 *  Green-Yellow Line - If the scout wishes to shoot at something but cant, green yellow line indicates change of position the scout wants to do
 *  
 *  TO WORK ON
 *  Bright Red Line - Display any correction for dodging
 */

public class ScoutBot extends GlobalVars {
	
	// ------------- GENERAL (IMPORTANT TO SELF) VARS -------------//
	
	// Variable for round number
	private static int Rem_is_better;
	
	// Variables for self and team recognition
	public static int id;
	public static int scout_number;
	private static Team enemy;
	private static Team allies;		
	private static final float strideRadius = battlecode.common.RobotType.SCOUT.strideRadius;
	private static final float bodyRadius = battlecode.common.RobotType.SCOUT.bodyRadius;
	
	// The archon number of the archon from which this scout will receive its information
	public static int homeArchon;
	
	// The intial round in which the scout was constructed
	public static int initRound;
	
	// Parameters to store locations of self and the nearest archon
	private static MapLocation base;
	public static MapLocation myLocation;	
	
	// The total number of scouts in active service
	private static int currentNumberofScouts;	

	// ------------- TREE SEARCH VARIABLES -------------//
	
	// Parameter that asserts array storage of scout
	private static final int unitMemorySize = 5;
	private static final int staticMemorySize = 200;
	private static final int dynamicMemorySize = TOTAL_TREE_BROADCAST_LIMIT;
	
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
	
	// Max range at which the scout will scan for trees...
	private static final int treeSenseDistance = 7;
	
	// ------------- MOVEMENT VARIABLES -------------//
	
	// Direction at which the scout traveled last
	private static Direction lastDirection;
	private static MapLocation lastPosition;
	
	// Direction for use each round
	private static Direction myDirection;
	
	// The ID of the robot the scout is currently tracking and its information
	public static int trackID;	
	public static RobotInfo trackedRobot;
	public static boolean isTracking;
	
	// Placeholder for desired location to go to
    public static MapLocation desiredMove;
    
    // Place holder to show where the robot had originally intended to go before the trajectory was altered by post-track/move functions
    public static MapLocation lastDesiredMove;
	
    // Boolean for rng rotation direction - true for counterclockwise, false for clockwise
    public static boolean rotationDirection = true;
    
    // Boolean for determining if the robot wants to move or no
    private static boolean wantsToMove;    
    
    // ------------- TRACKING VARIABLES -------------//
    
	// Stores the number of rounds that the scout has been tracking the current enemy
	private static int roundsCurrentlyTracked;
	
	// Parameter to express if the scout has already broadcasted status information this turn
	public static boolean hasBroadcastedStatus;		
    
    // Array of the last three enemies tracked by the scouts 
    public static int[] noTrack = new int[3];   
    public static int noTrackUpdateIndex; 
    
    // ------------- SHOOTING VARIABLES -------------//
  
	// Separation distance of shoot check...
	private static final float obstacleCheck = (float)0.4;
    
    // TreeInfo for any tree blocking the way to a shot...
    private static TreeInfo blockingTree;
    
    // Variable to store last working firing location - in case dodging is necessary (may not be used in future)
    private static MapLocation lastFiringLocation;
    
    // Variable to store last working firing direction (measured from target)...
    private static Direction lastFiringDirection;    
    
    // Variable to store whether or not the scout will elect to shoot this turn....
    private static boolean willShoot;    
    
    // Variable to store where the scout wants to shoot this turn    
    private static MapLocation locationToShoot;
    
    // ------------- ENEMY DATA VARIABLES -------------//
    
    // Store the last known location of the gardener...
    private static MapLocation gardenerLocation;    
		
	// Arrays to store information on enemy archons
	private static RobotInfo[] enemyArchons= new RobotInfo[unitMemorySize];
	private static int[] enemyArchonIDs = new int[unitMemorySize];
	
	// Parameter to determine at which index the archon data was updated #TODO
	private static int archonIndex;	
    
    
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
        isTracking = false;
        gardenerLocation = null;
        
        
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
	    // Clear memory of last tracked members
	    Arrays.fill(noTrack, -1);    
	    
        // Code to be performed every turn        
        while (true) {
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	// Allow the scout to move by default - would be prety dumb if being stupid was original staste Q________Q...
            	wantsToMove = true;
            	
            	// Force base to be null at start of round - rest closest ally
            	base = null;
            	
            	// Set it so that there is by default no tree blocking the path to the target...
            	blockingTree = null;
            	
            	// Update total number of scouts
            	currentNumberofScouts = rc.readBroadcast(SCOUT_CHANNEL);
            	
            	// Since robot has not yet broadcasted this turn, set param to false by default
            	hasBroadcastedStatus = false;
            	
            	// Initialize archon index to invalid value - becomes valid if something is updated
            	archonIndex = -1;
            	
            	// Update Location and location of base as well as refresh the desired travel location
            	myLocation = rc.getLocation();         	
                
            	// Get nearby enemies and allies and bulletsfor use in other functions            	
            	RobotInfo[] enemyRobots = NearbyUnits(enemy);
            	RobotInfo[] alliedRobots = NearbyUnits(allies);
            	BulletInfo[] nearbyBullets = rc.senseNearbyBullets();
            	
        		// Get information on all trees that are able to be sensed    	
            	TreeInfo[] sensedTrees = addTrees(treeSenseDistance);
            	
            	// Param to store the direction that the robot wants to move to
            	myDirection = lastDirection;
            	
            	// Param to store the location of the nearest ally for the current turn            	
            	RobotInfo NearestAlly;
            	
            	// Update the location of the nearest noncombatant allied location and sdore into the variable Nearest Ally - which is null if no nearby ally exists
            	if (alliedRobots.length > 0){            		
                 	NearestAlly = getNearestCivilian(alliedRobots);
            	}
            	else{
            		NearestAlly = null;
            	}
             	
             	// If there is a friendly noncombatant nearby
             	if(NearestAlly != null){
             		
             		base = NearestAlly.location;
             		
             		// For Initialization - have last direction originally point away from the closest ally, rounded to 30 degree intervals
             		int randOffset = (int)(Math.random() * 4 - 2);
            		Direction awayAlly = new Direction(myLocation.directionTo(base).radians + (float) (Math.PI + randOffset * Math.PI/8));
            		float newRadians = (float) (((int) (awayAlly.radians / (float) (Math.PI / 6))) * Math.PI / 6);
            		
            		myDirection = new Direction(newRadians);
            		
            		// SYSTEM CHECK - make sure direction is multiple of 30 degrees
            		// System.out.println("Direction updated: nearest ally is in direction opposite to roughly" + myDirection.getAngleDegrees());            		
             	}             	
                
            	// Placeholder for the location where the robot desires to move - can be modified by dodge
            	desiredMove = null;
            	            	
             	/***********************************************************************************
            	 *************************** Actions to be Completed ******************************
            	 **********************************************************************************/
            	
            	// Broadcast Tree Data
            	
            	// Currently on hold until we find use for it... D:
            	// broadcastTree(TOTAL_TREE_BROADCAST_LIMIT, sensedTrees);
            	
            	// Other broadcasts -> Only do if the unit is 10 units away from any ally #TODO
            	
            	// SYSTEM CHECK - See if Broadcasting is completed
            	// System.out.println("Broadcasting Completed");
            	
            	// Update the desired place to move to
            	move(enemyRobots);
            	
              	// SYSTEM CHECK - See if move function has been completed
            	// System.out.println("Move Completed");            	
            	
            	// Check if the initially selected position was out of bounds...
            	if (!rc.canMove(desiredMove)){
            		MapLocation newLocation = Yuurei.correctOutofBoundsError(desiredMove, myLocation, bodyRadius, strideRadius, rotationDirection);
            		
            		lastDirection = new Direction(myLocation, newLocation);
            		
            		desiredMove = newLocation;
            	}
            	
            	// Check if the initial desired move can be completed
            	if(!rc.canMove(desiredMove)){            		
            	
        			// Obtain the reverse direction
        			Direction newTestDirection = new Direction(myDirection.radians + (float) Math.PI);
        			
        			if (rc.canMove(myLocation.add(newTestDirection))){            				
        				// Attempt to run the open path finding check
        				MapLocation testMove = Yuurei.tryMoveInDirection(newTestDirection, strideRadius, myLocation);
        				if (testMove != null){
        					desiredMove = testMove;
            			}
            		}
            	}
            	
            	
            	// May use this later idk...
            	MapLocation dodgeLocation = desiredMove;
            	
            	boolean canDodge = false;
            	System.out.println("Calling Dodge Function....");
            	
            	dodgeLocation = Yuurei.attemptDodge(desiredMove, myLocation, nearbyBullets, strideRadius, bodyRadius, -1, rotationDirection, canDodge);
            	    			
            	
            	if (dodgeLocation != null){
            		desiredMove = dodgeLocation;
            	}
            	
            	if(rc.canMove(desiredMove) && wantsToMove){
            		rc.move(desiredMove);
            	}
            	else{
            		System.out.println("This scout cannot find anywhere to go and is sad Q____Q)");
            	}
    
            	// Make sure to show appreciation for the one and only best girl in the world.
            	// If you are reading this and you think Emilia is best girl I have no words for you
                Rem_is_better += 1;

                // Make it so that the last direction traveled is the difference between the robot's current and final positions for the round...
                lastPosition =  rc.getLocation();
                lastDirection = new Direction(myLocation, lastPosition);
                
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Scout Exception");
                e.printStackTrace();
            }
        }
    }
	
	
	/*****************************************************************************
	 ******************** Tree Search and Broadcast Functions ********************
	 ****************************************************************************/
			
    // Function to sense and return the information of all nearby trees
	
    private static TreeInfo[] addTrees(float senseDistance){
    	
    	// Sense all nearby trees
    	TreeInfo[] nearby_trees = rc.senseNearbyTrees(senseDistance);
    	
    	// SYSTEM CHECK to check to see if sensing function is run
    	// System.out.println("Currently sensing location of nearby trees");
    	
    	return nearby_trees; 	    	 	    	    	
    }
    
	
	// Function to Broadcast the locations of all trees found this turn
	
    private static void broadcastTree (int totalBroadcastLimit, TreeInfo[] newTrees) throws GameActionException {
    	
    	// Takes one parameter: totalBroadcastLimit
    	// totalBroadcastLimit is an integer representing the maximum number of trees that may be broadcasted in a single turn
   		
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
    		// System.out.println("Currently not tracking anything");
    		
    		// If there is a robot
    		if (trackedRobot != null){
    			// Update the trackID
    			trackID = trackedRobot.ID;
    			isTracking = true;
    			
    			// SYSTEM CHECK - Notify what the robot will now track
        		// System.out.println("The scout has noticed the enemy Robot with ID: " + trackID);
    			
    			// Call move again with the updated information
    			move(enemyRobots);    	
    		
    		} else{ // If there is no robot to be tracked 
    			// Posit the desired move location as a forward movement along the last direction
    			desiredMove = myLocation.add(myDirection, (float) (Math.random() * 0.5  + 1));
    			
    			// SYSTEM Check - Set light grey line indicating where the scout would wish to go
    			rc.setIndicatorLine(myLocation, desiredMove, 110, 110, 110);    			
       			
        		// SYSTEM CHECK - Notify that nothing to be scouted has been found
        		// System.out.println("The scout cannot find anything to track");     			
    				
    		}
    	} else{ // If the robot is actually currently tracking something
    		// If the currently tracked robot is a gardener, execute special tracking method
    		if (trackedRobot.type == battlecode.common.RobotType.GARDENER){
    			
    			trackGardener(enemyRobots);     			
    			
    		} else if (trackedRobot.type == battlecode.common.RobotType.ARCHON){
    			
    			track(enemyRobots, (float) 1.5);
    			
    		} else if (trackedRobot.type == battlecode.common.RobotType.SCOUT){
    			
    			track(enemyRobots, 1);
    		}
    		
    		else{ // Otherwise the enemy is a soldier/tank/lumberjack and it would be wise to run away!!
    			
    			// SYSTEM CHECK - Display black indicator dot on nearest hostile to run away from...
    			rc.setIndicatorDot(trackedRobot.location, 0, 0, 0);
    			
    			runAway(enemyRobots);
    		}
    	}        	
    }
    
    
    private static void runAway(RobotInfo[] enemyRobots) throws GameActionException{
    	
    	if (rc.canSenseRobot(trackID)){
    		
    		// SYSTEM CHECK - Make scout nofity of the presence of hostile
    		//  System.out.println("Am currently running away from a hostile enemy with ID: " + trackID);
	    	
	    	// Variable to store the distance from the robot to run away from and the current position of the robot
			float gap = trackedRobot.location.distanceTo(myLocation);
			
			// Get the direction from the target enemy
	    	Direction dir = trackedRobot.location.directionTo(myLocation);
	    	
	    	// If the gap is small, move directly away from the target enemy
	    	if (gap < 3){
	    		desiredMove = myLocation.add(dir, (float) strideRadius);
	    	}
	    	
	    	// If the gap is slightly smaller, moves so that the approach is not so direct
	    	else if (gap < 4.5){	    		
	    		// If the object was set to be rotating go clockwsie in an increasing manner away from robot
	    		if (rotationDirection){	    			
	    			// Rotate 15 degrees clockwise
	    			Direction newDir = new Direction(dir.radians - (float) (Math.PI/3));
	    			
	    			// Set new move point
	    			desiredMove = trackedRobot.location.add(newDir, (float) (6));
	    			
	    		}
	    		else{
	    			// Rotate 15 degrees counterclockwise
	    			Direction newDir = new Direction(dir.radians + (float) (Math.PI/3));
	    			
	    			// Set new move point
	    			desiredMove = trackedRobot.location.add(newDir, (float) (6));
  				    			
	    		}	    		
	    	}
	    	else{
	    		// If the robot is far enough away, get to the outer limit of the range away from the robot
	    		if (rotationDirection){
	    			// Calculate the direction from the target that you want to end up at
	    			Direction fromDir = new Direction(dir.radians - (float) (Math.PI/6));
	    			
	    			// Obtain the desired target location
	    			desiredMove = trackedRobot.location.add(fromDir, (float) (12));
	    			
	    		} else{
	    			// Calculate the direction from the target that you want to end up at
	    			Direction fromDir = new Direction(dir.radians + (float) (Math.PI/6));
	    			
	    			// Obtain the desired target location
	    			desiredMove = trackedRobot.location.add(fromDir, (float) (12));	   

	    		}		    
	    	} 	    
	    	
	    	// Correct desiredMove slightly to be within 2.5 units of scout
	    	Direction targetDir = new Direction(myLocation, desiredMove);
	    	
	    	desiredMove = myLocation.add(targetDir, (float) strideRadius);
	    	
	    	// SYSTEM CHECK Print line from current location to intended move location - yellow line...
	    	rc.setIndicatorLine(myLocation, desiredMove, 255, 255, 0);   
	    	
	    	// Reset the enemy track ID, since it is not something that the robot would like to follow at the moment
	    	trackID = -1;
        	roundsCurrentlyTracked = 0;	 
        	isTracking = true;
    	}
    	
    	// If the scout has lost sight of the scary enemy right after seeing it (shouldn't happen at all but if it does here is code to handle it
    	else{
    		// SYSTEM CHECK - Print out that it has evaded enemy
    		System.out.println("The enemy I just saw disappeared WTF");
			
    		// Don't update no track in case the spooky comes back
        	trackID = -1;
        	roundsCurrentlyTracked = 0;
        	isTracking = false;  
        	
        	move(enemyRobots);
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
   
        
    // Function to attempt to find a new firing location to the target object at a certain distance away
    
    private static MapLocation findFiringLocation(float Distance, Direction dirFromTarget) throws GameActionException{
    
    	// SYSTEM CHECK - Print that firing currently is impossible and that the scout is searching for a new trajectory
    	System.out.println("Cannot fire at target with ID: " + trackID + " - there appears to be a tree blocking the way... Searching for alternative line of fire");;
    	
    	// Check 30 degree intervals of direction out FROM the target object
    	for(int i = 1; i <= 17; i ++){
    		Direction testDir = new Direction(dirFromTarget.radians + (float) (Math.PI / 18 * i));
    		MapLocation newCheck = trackedRobot.location.add(testDir, Distance);
    		
    		// Make sure that it is possible to reach the new considered location
    		if (myLocation.distanceTo(newCheck) < strideRadius){    			
    			// Make sure the location has a clear line of sight
    			if (!isLineBLocked(trackedRobot.location, newCheck, obstacleCheck)){
    				return newCheck;
    			}
    		}
    	} 
    	// If no possible firing location is found....
    	return null;    	
    }
    
    
    // Function to execute when the robot is attempting to track down a gardener
    
    private static void trackGardener(RobotInfo[] enemyRobots) throws GameActionException{
    	
    	if(rc.canSenseRobot(trackID)){
    		
    		// Update location of tracked robot - the Gardener...
    		trackedRobot = rc.senseRobot(trackID);
    		
    	   	// SYSTEM CHECK Print line from current location to intended move location violet line
        	rc.setIndicatorLine(myLocation, trackedRobot.location, 150, 0, 200);   	
    		
	    	// Get distance and direction between scout and the Gardener
	    	float gap = myLocation.distanceTo(trackedRobot.location);
	    	
	    	// Prevent null pointer exception, assert that the gardener's location has been seen before attempting to access 
	    	if(gardenerLocation == null){
	    	   	// If it has not yet been initialized set the robot's last location as its current
	    		gardenerLocation = trackedRobot.location;
	    	}
	    		
	    	// If the gap between the scout and the gardener is too large, move towards the gardener....
	    	if (gap > 6){
		    	// Update the gardener's location
	    		gardenerLocation = trackedRobot.location;
	    		// Elect to just move towards the gardener
	    		moveTowardsTarget();    		
	    	}
	    	// Otherwise if the gardener is fairly close...
	    	else{
	    		// See if it is possible for the gardener to shoot at the robot from where it is
	    		boolean noShoot = isLineBLocked(myLocation,trackedRobot.location, obstacleCheck);
	    		
	    		// If the robot was unable to shoot, attempt to find a shooting location
	    		if (noShoot){
	    			// Get the direction to the robot FROM the target - should be opposed to dir
	    			Direction dirFromTarget = new Direction(trackedRobot.location, myLocation);
	    			
	    			// Obtain a random number between 3 and 4 to find a firing location...
	    			float testDist = 3 + (float) Math.random();
	    			
	    			// See if such a firing location exists
	    			MapLocation tryFiringLocation = findFiringLocation(testDist, dirFromTarget);
	    			
	    			// If it does set it as the desired moving point and shoot in the direction of the gardener
	    			if (tryFiringLocation != null){
	    				
	    				desiredMove = tryFiringLocation;
	    				if(rc.canFireSingleShot()){
	    					
	    					willShoot = true;
	    		    		
	    		    		lastFiringDirection = new Direction(tryFiringLocation, trackedRobot.location);
	    			    	lastFiringLocation = tryFiringLocation;
	    			    	desiredMove = tryFiringLocation;
	    			    	
	    			    	// SYSTEM CHECK print green-yellow to new location to fire from
	    			    	rc.setIndicatorLine(myLocation, tryFiringLocation, 175, 255, 50);
	    		    	}	    			
	    			}
	    			else{
	    		    	// Update the gardener's location
	    	    		gardenerLocation = trackedRobot.location;
	    				// If no firing location can be found simply utilize the tracking portion of the algorithm
	    				moveTowardsTarget();
	    			}
	    		}
	    		else{
	    			// If the current position allows the robot to keep shooting, it will utilize the gardener's movement to decide a new place to move to
	    			
	    			// Difference in the gardener's movement
		    		float delX = trackedRobot.location.x - gardenerLocation.x ;
		    		float delY = trackedRobot.location.y - gardenerLocation.y ;
		    		
		    		// Generate delta multipliers randomly..		    		
		    		float ranX = (float)(Math.random() * 0.4 + 0.8);
		    		float ranY = (float)(Math.random() * 0.4 + 0.8);
		    		
		    		desiredMove = new MapLocation(myLocation.x + delX * ranX, myLocation.y + delY * ranY);
		    		
		    	   	// SYSTEM CHECK Print line from current location to intended move location - light blue green
		        	rc.setIndicatorLine(myLocation, desiredMove, 0, 200, 200);   			    		
		    		
    		    	// Update the gardener's location
    	    		gardenerLocation = trackedRobot.location;
	    		}   
	    	}
	    	
	    // Of the gardener is no longer visible, run following code..
    	} else{    		
    		// If the scout can no longer sense the target gardener...
    		noTrack[noTrackUpdateIndex % 3] = trackID;
			noTrackUpdateIndex += 1;
			
        	trackID = -1;
        	roundsCurrentlyTracked = 0;
        	isTracking = false;        	
			
        	// Update the location of be null, so that the new gardener refreshes the data...
        	gardenerLocation = null;
        	// SYSTEM CHECK - Notify of lost sight of gardener
        	System.out.println("Lost sight of gardener (ittai nani??????????) /Finding a new target");    
        	
        	// Call the move function to obtain a new target
        	move(enemyRobots);    
    	}    	
    }
    
    
    // Function to execute when the robot is attempting to track down an archon 
    // For now just rotates around for a greater amount....
    
    private static void trackArchon () throws GameActionException{
    	
    	moveTowardsTargetMulti((float) 1.5);
    }
    
    // Function to follow a unit and approach it
    
	private static void track(RobotInfo[] enemyRobots, float distance) throws GameActionException{
		
		// If the robot can currently sense the robot it is tracking and if it has not been tracking this robot for too long
    	if (rc.canSenseRobot(trackID) && roundsCurrentlyTracked < 15){
    		
    		// SYSTEM CHECK - See if the robot identifies that it is actually tracking something
    		System.out.println("I am currently tracking a robot with ID: " + trackID);
    		
    		// Update location of tracked robot 
    		trackedRobot = rc.senseRobot(trackID);
    		// SYSTEM CHECK - Draw a violet line between current position and position of robot
    		rc.setIndicatorLine(myLocation, trackedRobot.location, 150, 0, 200);
    		
    		// Increment number of rounds tracked
        	roundsCurrentlyTracked +=1;
        	
        	moveTowardsTargetMulti(distance);    
        	
        // If the robot has been tracking its current prey for too long or has lost sight of its target
    	} else {
    		
    		// If the robot has been tracking the current enemy for a long time make sure it doesnt check the same enemy again
    		noTrack[noTrackUpdateIndex % 3] = trackID;
			noTrackUpdateIndex += 1;
			
        	trackID = -1;
        	roundsCurrentlyTracked = 0;
        	isTracking = false;        	
			
        	// SYSTEM CHECK - Notify of target loss
        	System.out.println("Lost sight of target/Finding a new target");        	
        	
        	// Call move to obtain a new location to try to move to
        	move(enemyRobots);       	   	
    	}	                		
    }
	
	// If there is no multiplier argument provided
	private static void moveTowardsTarget() throws GameActionException{
		moveTowardsTargetMulti(1);
	}
	
	// Function to move towards a given robot
	
	private static void moveTowardsTargetMulti(float multiplier) throws GameActionException{
		
		// Variable to store the distance to the robot currently being tracked
		float gap = myLocation.distanceTo(trackedRobot.location);
		
		// Get the direction to the target enemy
    	Direction dir = myLocation.directionTo(trackedRobot.location);
    	
    	// If the gap is large enough move directly towards the target
    	if (gap > 6 * multiplier){
    		desiredMove = myLocation.add(dir, (float) strideRadius);
    	}
    	
    	// If the gap is slightly smaller, moves so that the approach is not so direct
    	else if (gap > 4.5 * multiplier){	    		
    		// If the object was set to be rotating counterclockwise, go clockwise
    		if (rotationDirection){	    			
    			// Rotate 20 degrees clockwise
    			Direction newDir = new Direction(dir.radians - (float) (Math.PI/9));
    			
    			// Set new move point
    			desiredMove = myLocation.add(newDir, (float) (strideRadius));
    			
    			// Set rotation direction to be clockwise
    			rotationDirection = false;	    			
    		}
    		else{
    			// Rotate 30 degrees counterclockwise
    			Direction newDir = new Direction(dir.radians + (float) (Math.PI/9));
    			
    			// Set new move point
    			desiredMove = myLocation.add(newDir, (float) (strideRadius));
    			
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
    			desiredMove = trackedRobot.location.add(fromDir, (float) (2.5 * multiplier));
    			
    		} else{
    			// Calculate the direction from the target that you want to end up at
    			Direction fromDir = new Direction(dir.radians + (float) (2 * Math.PI/3));
    			
    			// Obtain the desired target location
    			desiredMove = trackedRobot.location.add(fromDir, (float) (2.5 * multiplier));	    			
    		}
    	// SYSTEM CHECK Print line from current location to intended move location - light blue green
    	// rc.setIndicatorLine(myLocation, desiredMove, 0, 200, 200);   			
    	}   		
	}   
		
	/*****************************************************************************
	 ******************* Miscellaneous Functions************** ********************
	 ****************************************************************************/   
	
    // Function to determine if there is any tree in the line between one location and the other
    
    private static boolean isLineBLocked(MapLocation start, MapLocation end, float spacing) throws GameActionException {
    	
    	// Find the direction from the starting point to the end
    	Direction search = start.directionTo(end);
    	
    	// Iterate up through the length of the gap between the two selected points
    	for(int i = 0; i * spacing < start.distanceTo(end); i++){
    		
    		// If there is a tree in the way, say so..,
    		if (rc.isLocationOccupiedByTree(start.add(search, (float)  (i * spacing)))){
    			
    			blockingTree = rc.senseTreeAtLocation(myLocation.add(search, (float) (i * spacing)));
    			
    		   	// SYSTEM CHECK print light pink dot at where the blocking tree is located...
    	    	rc.setIndicatorDot(blockingTree.location, 255, 180, 190);
    			
    			return true;
    		}
    	// If by the end of the for loop nothing is there, then we return false, meaning that the line isn't blocked
    	}
    	return false;
    }

    
    // Get location of home Archon if it has broadcasted previously
	// TODO
    
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

		return rc.senseNearbyRobots(myLocation, (float)10, team);
	}
	
	
	// Function to obtain the data for the nearest ally to the robot currently (only gardeners and archons)
	
	private static RobotInfo getNearestCivilian(RobotInfo[] currentAllies){
    	
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
				
				if (dist < minimum && dist < 7){
					// If the robot has not been tracked recently
					if (!arrayContainsInt(noTrack, enemyRobots[i].ID)){
						
						// Update the index
						
						minimum = dist;
						index = i;		
					}
				}			
			}
		}
		// This should always happen, but if the found index is positive return the closest robot that is a Gardener
		if (index >= 0){	
			
			return enemyRobots[index];	
			
		} else{			
			return null;
		}		
	}
	
	
	//Get the nearest enemy to the last updated location of base (i.e. closest ally)
	// TODOrr 
	
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
  



