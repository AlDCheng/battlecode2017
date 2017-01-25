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
 * AI Controlling the functions of the ScoutBot
 *
 * ~~ Coded by Illiyia (akimn@#mit.edu)
 * 
 * Debug statements all begin with SYSTEM CHECK 
 * 
 ---------------------------------------------------- */

/* Brief Overview of Indicator Debug Lines and Dots.....
 * 
 *  ORANGE DOT - Tree not to be broadcasted this turn
 *  BLUE DOT - Tree to be broadcasted this turn
 *   
 *  LIGHT GREY LINE - Line that the scout initially wishes to go if it is not tracking anything....
 *  
 *  BLACK DOT  - Any enemy the scout deems as too hostile to track will be marked by a black dot
 *  YELLOW LINE - Robot is currently running away from unit with black dot... indicates the position it wishes to flee from
 * 
 *  VIOLET LINE - Line to the location of the robot that the scout is tracking.....
 *  
 *  GREEN-YELLOW LINE - Correction to a non-movement from the scout whilst following a gardener - moves to a new location where it can actually shoot
 *  LIGHT BLUE GREEN LINE - Location to move to if the gardener has moved that turn....
 *  
 *  WHITE LINE - Line to a bullet tree that the robot is going towards............
 *  MAROON DOT - Bullet tree just shaken by the scout....
 *  
 */

public class ScoutBot extends GlobalVars {
	
	// ------------- GENERAL (IMPORTANT TO SELF) VARS -------------//
	
	// Variable for round number
	private static int onlyRemIsBestGIrl;
	
	// Variables for self and team recognition
	public static int id;
	public static boolean iDied;
	public static int scoutNumber;
	public static int unitNumber;
	private static Team enemy;
	private static Team allies;		
	private static final float strideRadius = battlecode.common.RobotType.SCOUT.strideRadius;
	private static final float bodyRadius = battlecode.common.RobotType.SCOUT.bodyRadius;
	private static final float senseRadius = battlecode.common.RobotType.SCOUT.sensorRadius;
	private static float teamBullets;
	
	// Threshold value to determine the point at which the scout would decide to harvest trees rather than carry on normal operations.........
	private static final int harvestThreshold = 100;
	
	// The intial round in which the scout was constructed
	public static int initRound;
	
	// Parameters to store locations of self and the nearest archon
	private static MapLocation nearestCivilian;
	public static MapLocation myLocation;	
	
	// The total number of scouts in active service
	private static int currentNumberofScouts;	

	// ------------- TREE SEARCH VARIABLES -------------//
	
	// Parameter that asserts array storage of scout
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
	private final static int treeSenseDistance = 7;
	
	// Array to store the data of enemy robots from the previous turn.....
	private static RobotInfo[] previousRobotData;
	
	// ------------- MOVEMENT VARIABLES -------------//
	
	// Direction at which the scout traveled last
	private static Direction lastDirection;
	private static MapLocation lastPosition;
	
	// Direction for use each round
	private static Direction myDirection;
	
	// The ID of the robot the scout is currently tracking and its information
	public static  int trackID;	
	public static RobotInfo trackedRobot;
	public static boolean isTracking;
    
    // Boolean for rng rotation direction - true for counterclockwise, false for clockwise
    public static boolean rotationDirection = true;
    
    // ------------- TRACKING VARIABLES -------------//
    
	// Stores the number of rounds that the scout has been tracking the current enemy
	private static int roundsCurrentlyTracked;
	
	// Parameter to express if the scout has already broadcasted status information this turn
	public static boolean hasBroadcastedStatus;		
    
    // Array of the last three enemies tracked by the scouts 
    private static int[] noTrack = new int[3];   
    private static int noTrackUpdateIndex; 
    
    // Variable to see how long the robot has not tracked another unit for
    public static int hasNotTracked;
    
    // Variable to determine whether or not a scout should defend a unit or not....
    private static boolean mustDefend;
    
    // Location that the scout must defend...
    private static MapLocation defendLocation;
    
    // Enemy to attack...
    private static int defendAgainstID;
    
    // ------------- SHOOTING VARIABLES -------------//
  
	// Separation distance of shoot check...
	private static final float obstacleCheck = (float)0.4;    
    
    // ------------- ENEMY DATA VARIABLES -------------//
    
    // Store the last known location of the gardener...
    private static MapLocation gardenerLocation;  
    
    
    
	/************************************************************************
	 ***************** Runtime Functions and Initialization *****************
	 ***********************************************************************/
		
	// Initialization function - makes the default values for most important parameters
	
	public static void init() throws GameActionException {
		
		// SYSTEM CHECK Initialization start check
		System.out.println("I'm a scout!");	
				
        // Important parameters for self
        enemy = rc.getTeam().opponent();
        allies = rc.getTeam();
        id = rc.getID();
        teamBullets = rc.getTeamBullets();
        
        // Get own scoutNumber  and unitNumber- important for broadcasting 
        scoutNumber = rc.readBroadcast(BroadcastChannels.SCOUT_NUMBER_CHANNEL);
        currentNumberofScouts = scoutNumber + 1;
        
        unitNumber = rc.readBroadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL);
        rc.broadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL, unitNumber + 1);
        
        // Get the current round number......
        onlyRemIsBestGIrl = rc.getRoundNum();
        initRound = onlyRemIsBestGIrl;
        
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
        previousRobotData = null;        
        
        // Initialize nearest CIvilian to be the stored location of the archon...
        int archonInitialX = rc.readBroadcast(BroadcastChannels.ARCHON_INITIAL_LOCATION_X) / 100;
        int archonInitialY = rc.readBroadcast(BroadcastChannels.ARCHON_INITIAL_LOCATION_Y) / 100;
        
        nearestCivilian = new MapLocation(archonInitialX, archonInitialY);
        
        // Initialize variables relating to defending....
        defendLocation = null;
        defendAgainstID = -1;        
        
        // Update the number of scouts so that other scouts can recognize....
        rc.broadcast(BroadcastChannels.SCOUT_NUMBER_CHANNEL, currentNumberofScouts);
                
        // SYSTEM CHECK to see if init() is completed   
        // System.out.println("Scout successfully initialized!");		
        
        // By default pass on to the main function
        main();        
	}
	
	
	// Main function of the scout
	// Scouts initially move away from the home archon
	// Update team tree list every single turn
	// Shoot enemies with high_priority	
		
	private static void main() throws GameActionException{	            
        
		// Initialize other parameters for tracking
	    roundsCurrentlyTracked = 0;
	    // Clear memory of last tracked members
	    Arrays.fill(noTrack, -1);    
	    
        // Code to be performed every turn        
        while (true) {
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	
            	// ------------------------- RESET/UPDATE VARIABLES ----------------//            	
            	
            	
            	// If the iFeed function incorrectly thought that the unit had died.....
            	if (iDied) {
            		iDied = false;
            		
            		// Get own scoutNumber  and unitNumber- important for broadcasting 
                    scoutNumber = rc.readBroadcast(BroadcastChannels.SCOUT_NUMBER_CHANNEL);
                    currentNumberofScouts = scoutNumber + 1;
                    
                    unitNumber = rc.readBroadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL);
                    rc.broadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL, unitNumber + 1);
                    
                    // Update the number of scouts so that other scouts can recognize....
                    rc.broadcast(BroadcastChannels.SCOUT_NUMBER_CHANNEL, currentNumberofScouts);
            		
            	}
            	
            	// If the robot has not tracked anything for a long time fill the no track with -1 so it can track something again
            	if (hasNotTracked > 25){
            		Arrays.fill(noTrack, -1);
            	}
            	
            	// Force nearestCivilian to be null at start of round - closest gardener / Archon
            	nearestCivilian = null;
            	mustDefend = false;
            	
            	// Update total number of scouts
            	currentNumberofScouts = rc.readBroadcast(SCOUT_CHANNEL);
            	
            	// Since robot has not yet broadcasted this turn, set param to false by default
            	hasBroadcastedStatus = false;

            	// Update location of self
            	myLocation = rc.getLocation();         	
                
            	// Get nearby enemies and allies and bullets for use in other functions            	
            	RobotInfo[] enemyRobots = NearbyUnits(enemy);
            	RobotInfo[] alliedRobots = NearbyUnits(allies);
            	BulletInfo[] nearbyBullets = rc.senseNearbyBullets();
            	
        		// Get information on all trees that are able to be sensed    	
            	TreeInfo[] sensedTrees = addTrees(treeSenseDistance);
            	
            	// Param to store the direction that the robot wants to move to 
            		// Initialize every round to the last direction traveled - if possible have the scout continue in that direction for as long as possible...
            	myDirection = lastDirection;
            	
            	// Param to store the location of the nearest ally for the current turn            	
            	RobotInfo NearestAlly;
            	
            	// Update the location of the nearest noncombatant allied location and store into the variable Nearest Ally - which is null if no nearby ally exists
            	if (alliedRobots.length > 0){            		
                 	NearestAlly = getNearestCivilian(alliedRobots);
            	}
            	else{
            		NearestAlly = null;
            	}
             	
             	// If there is a friendly noncombatant nearby
             	if(NearestAlly != null){
             		
             		nearestCivilian = NearestAlly.location;
             		
             		// For Initialization and for the future,- have last direction originally point away from the closest ally, rounded to 30 degree intervals             		
             		if (myLocation.distanceTo(nearestCivilian) <= 2.5){
	             		int randOffset = (int)(Math.random() * 4 - 2);
	            		Direction awayAlly = new Direction(myLocation.directionTo(nearestCivilian).radians + (float) (Math.PI + randOffset * Math.PI/8));
	            		float newRadians = (float) (((int) (awayAlly.radians / (float) (Math.PI / 6))) * Math.PI / 6);
	            		
	            		myDirection = new Direction(newRadians);
	            		
	            		// SYSTEM CHECK - make sure direction is multiple of 30 degrees
	            		// System.out.println("Direction updated: nearest ally is in direction opposite to roughly" + myDirection.getAngleDegrees());  
             		}
             		// Get the nearest enemy to the scout..
            		RobotInfo nearestEnemy = Chirasou.getNearestAlly(enemyRobots, myLocation);
            		// If there is one...
            		if (nearestEnemy != null){
	            		// If the nearest enemy is close enough to the nearest ally....
	            		if(nearestEnemy.location.distanceTo(nearestCivilian) < 20){
	            			mustDefend = true;
	            		}
            		}
             	}      
             	
         		if (nearestCivilian != null){
	         		// SYSTEM CHECK - Draw a white line to the nearest civilian's location
	             	rc.setIndicatorLine(myLocation, nearestCivilian, 255, 255, 255);
         		}
                
            	// Placeholder for the location where the robot desires to move - can be modified by dodge
            	MapLocation desiredMove = myLocation;
            	            	
            	// ------------ ACTIONS TO BE COMPLETED -------------//
            	
            	// Useless code for now....             	
            	if (alliedRobots.length > 1000){
	            	// Broadcast Tree Data
	            	
	            	// Currently on hold until we find use for it... D:
            		broadcastTree(TOTAL_TREE_BROADCAST_LIMIT, sensedTrees);
	            	
	            	// Other broadcasts -> Only do if the unit is 10 units away from any ally #TODO
	            	
	            	// SYSTEM CHECK - See if Broadcasting is completed
	            	// System.out.println("Broadcasting Completed");
            	}
            	
            	// Update the nearest enemy and archon locations
            	BroadcastChannels.broadcastNearestEnemyLocation(enemyRobots, myLocation, unitNumber, nearestCivilian, onlyRemIsBestGIrl);
            	
            	BroadcastChannels.broadcastEnemyArchonLocations(enemyRobots);        
            	
            	// Update the distress info and retreat to a scout if necessary            	
            	BroadcastChannels.BroadcastInfo distressInfo = BroadcastChannels.readDistress(myLocation, 30);
         
            	
            	// Update the team's bullet count - will determine if it attempts to search for bullet trees or no.....
            	teamBullets = rc.getTeamBullets();
            	
        		// Check to find the nearest bullet tree.......
        		TreeInfo nearestBulletTree = findNearestBulletTree();
            	
        		
        		// If the scout found some distress signal this turn...
            	if(distressInfo != null){
            		
            		// If the distressed gardener is being attacked by a scout.....
            		if (distressInfo.enemyType == 2 || distressInfo.enemyType == -1){
            			
            			// Set the location to defend...
            			defendLocation = new MapLocation (distressInfo.xPosition, distressInfo.yPosition);
        				
            			// Set the ID of the offending enemy
        				defendAgainstID = distressInfo.ID;
            		}            	
            	}
            	
            	// If the robot is meant to defend........
            	if (defendLocation != null){            		
    				
            		// If it already nearby or can simply sense the offending unit, track it
            		if(rc.canSenseRobot(defendAgainstID)){
            			
            			// Start tracking the robot to defend against....
            			trackID = defendAgainstID;            			
            			trackedRobot = rc.senseRobot(trackID);
            			
            			// Exit the return to defending location - actually defend!!!
            			defendLocation = null;            			
            			defendAgainstID = -1; 
            			isTracking = true;
        
            			
            			// SYSTEM CHECK Display a yellow dot on the enemy to kill now...
            			// rc.setIndicatorDot(trackedRobot.location, 255, 255, 0);
            			
            			
            			// SYSTEM CHECK IF the robot has need to defend, it will do so...
            			System.out.println("Found the offending enemy....");
            			
            			// Track the enemy....            			
            			desiredMove = track(enemyRobots, 1);            			
            		}
            		// If the robot has arrived at the defend location and has not found the enemy.....
            		else if (myLocation.distanceTo(defendLocation) <= 5){
            			
            			defendLocation = null;
            			defendAgainstID = -1;
            			
            			
            			// SYSTEM CHECK - display a green line to the distress location....
        				// rc.setIndicatorLine(myLocation, defendLocation, 0, 128, 0);
            			
            			// SYSTEM CHECK IF the robot has need to defend, it will do so...
            			System.out.println("Returned to distress call but found no one");
            			
            			// Exit the call to defendLocations and go on back to normal operations
            			desiredMove = move(enemyRobots);
            		}
            		else{
            			
            			Direction defendDirection = myLocation.directionTo(defendLocation);
            			desiredMove = myLocation.add(defendDirection, strideRadius);
            			
            			// SYSTEM CHECK - display a blue line to the distress location....
        				// rc.setIndicatorLine(myLocation, defendLocation, 0, 0, 128);
            			
            			// SYSTEM CHECK IF the robot has need to defend, it will do so...
            			System.out.println("Travelling back to defend....");
            			
            		}            		         		
            	}

        		// If the team currently has too few bullets and there is a bullet tree nearby and no enemies currently threaten its workers
            	else if (teamBullets < harvestThreshold && nearestBulletTree != null && !mustDefend && !isTracking){
            		
            		// SYSTEM CHECK - Make sure that the scout knows that there are too few bullets on present team....
            		// System.out.println("Team requires additional bullets, so will attempt to find more");            		
 
        			// Do the harvest function to attempt to get some bullets from some trees...
            		desiredMove = harvest(nearestBulletTree);            		
            	}
            	
            	// Otherwise just call the move function normally......
            	else {
            		desiredMove = move(enemyRobots);
            	}
            	
              	// SYSTEM CHECK - See if move function has been completed
            	// System.out.println("Move Completed");            	
            	
            	// -------------------- MOVE CORRECTION ---------------------//
            	
            	// Check if the initially selected position was out of bounds...
            	
            	// Correct desiredMove to within one soldier  stride location of where the robot is right now....
            	if(myLocation.distanceTo(desiredMove) > strideRadius){
            		
	            	Direction desiredDirection = new Direction(myLocation, desiredMove);	
	            	
	            	desiredMove = myLocation.add(desiredDirection, strideRadius);
            	}
            	
            	if (!rc.canMove(desiredMove)){
            		MapLocation newLocation = Yuurei.correctOutofBoundsError(desiredMove, myLocation, bodyRadius, strideRadius, rotationDirection);
            		
            		myDirection = new Direction(myLocation, newLocation);
            		
            		desiredMove = newLocation;
            	}
            	
            	// Check if the initial desired move can be completed and wasn't out of bounds/corrected by the above function
            	if(!rc.canMove(desiredMove)){          		
            	
					MapLocation newLocation = Yuurei.attemptRandomMove(myLocation, desiredMove, strideRadius);
					
					desiredMove = newLocation;
            	}     	
            	
            	// --------------------------- DODGING ------------------------ //
            	
            	// Placeholder Variable for any dodge that the dodge function creates....
            	MapLocation dodgeLocation;
            	
            	// Currently does nothing may use later XD
            	boolean canDodge = false;
            	
            	// SYSTEM CHECK - Make sure that the dodge function is called...
            	// System.out.println("Calling Dodge Function....");
            	
            	// Call the dodge function
            	dodgeLocation = Yuurei.attemptDodge(desiredMove, myLocation, nearbyBullets, strideRadius, bodyRadius, -1, rotationDirection, canDodge);
            	    			
            	// If there is a location that the unit can dodge to..
            	if (dodgeLocation != null){
            		desiredMove = dodgeLocation;
            	}
            	
            	// See whether or not the robot can move to the current desired move, and move if it does
            	if(rc.canMove(desiredMove)){
            		manageBeingAttacked(desiredMove);
            		rc.move(desiredMove);
            	}           	
            	else{
            		// SYSTEM CHECK - Make sure that the robot didn't move because it didn't want to....
            		// System.out.println("This robot did not move because it did not want to....");
            	}
            	
            	
            	// ------------------------ Shooting -----------------------------//
            	
            	// SYSTEM CHECK - Notify that the scout is now attempting to shoot at something........
            	System.out.println("Moving on to shooting phase...................");
            	
            	if (trackID >= 0 && trackedRobot != null){
            		
            		System.out.println("Attempted to shoot");
            		
            		// Obtain a location to shoot at
            		MapLocation shootingLocation = Korosenai.getFiringLocation(trackedRobot, previousRobotData, myLocation);
            		
            		// Get a list of allied trees to avoid shooting..
            		TreeInfo[] alliedTrees = rc.senseNearbyTrees(-1, allies);
            		
            		Korosenai.tryShootAtEnemy(shootingLocation, myLocation, 0, alliedRobots, alliedTrees, senseRadius);
            	}
    
            	// Make sure to show appreciation for the one and only best girl in the world.
            	// If you are reading this and you think Emilia is best girl I have no words for you
                onlyRemIsBestGIrl += 1;

                // Make it so that the last direction traveled is the difference between the robot's current and final positions for the round...
                lastPosition =  rc.getLocation();
                lastDirection = new Direction(myLocation, lastPosition);
                
                // If the robot was not tracking, increment the value by one round....
                if (!isTracking){
                	hasNotTracked += 1;
                }
                
                // Store the data for the locations of the enemies previously.....
                previousRobotData = enemyRobots;
                
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Scout Exception");
                e.printStackTrace();
            }
        }
    }	
    
	// Function for idle scout - does nothing until something triggers it to move
	// TODO
	
	private static MapLocation harvest(TreeInfo nearestBulletTree) throws GameActionException{
		
		// SYSTEM CHECK - Inform that the scout has entered harvest mode.............
		// System.out.println("Team bullet total insufficient and nearby bullet tree found - entering harvest mode...............");
		
		MapLocation desiredMove = null;
		
		float distanceTo = myLocation.distanceTo(nearestBulletTree.location);
		
		Direction directionTo = new Direction(myLocation, nearestBulletTree.location);
		
		// If the robot is too far away from the tree.... move towards it....
		if (distanceTo >= strideRadius){
			
			desiredMove = myLocation.add(directionTo, strideRadius);
			
			// SYSTEM CHECK - Draw a WHITE LINE to the tree to be moved towards
			// rc.setIndicatorLine(myLocation, nearestBulletTree.location, 255, 255, 255);			
		}
		// If the robot is within strideRadius of the tree, move to its center
		else if(distanceTo > 1){
			
			desiredMove = myLocation.add(directionTo, distanceTo);
			
			
			// SYSTEM CHECK - Draw a WHITE LINE to the tree to be moved towards
			// rc.setIndicatorLine(myLocation, nearestBulletTree.location, 255, 255, 255);
		}
		// If the robot is then within range to shake the bullets off of the tree............
		else{
			// Double check that the robot can interact with the tree print with MAROON DOT
			if(rc.canInteractWithTree(nearestBulletTree.ID)){
				
				// SYSTEM CHECK - Indicate which tree was just shaken...
				rc.setIndicatorDot(nearestBulletTree.location, 128, 0, 0);
				
				// Shake the tree to obtain the bullets.........
				rc.shake(nearestBulletTree.ID);
				
				// Since the robot has not yet moved, find the next tree to be shaken
				TreeInfo newTree = findNearestBulletTree();				
				Direction newDirection = new Direction(myLocation, newTree.location);
				
				// Attempt to move towards it
				desiredMove = myLocation.add(newDirection, strideRadius);
			}
			else{
				// SYSTEM CHECK - Should not happen.... but if the robot is close to the tree but cannot shake it print this fact....
				// System.out.println("ERROR: Within one unit of tree but for some reason cannot shake it...........");
			}
		}
		return desiredMove;
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
    
	
    // Function to find nearby bullet trees and go to them......
    
    private static TreeInfo findNearestBulletTree(){
    	
    	// Obtain a list of the nearest trees.......
    	TreeInfo[] nearbyTrees = addTrees(senseRadius);
    	
    	// Value to store the minimum location to a bullet tree......
    	float minimum = Integer.MAX_VALUE;
    	
    	for(TreeInfo tree: nearbyTrees){    		
    		// If the tree is closer than the previously discovered valid tree
    		if (tree.location.distanceTo(myLocation) < minimum){
    		
	    		if (tree.containedBullets > 0){
	    			return tree;	    			
	    		}    		
    		}
    	} 
    	// If no bullet trees can be sensed....
    	return null;
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
			// SYSTEM CHECK draw an ORANGE DOT to notify which trees are not even attempted to be broadcasted this turn
			rc.setIndicatorDot(newTrees[i].location, 255, 165, 200);	
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
				// System.out.println("Noting that Tree with ID: " + newTreeID + "has been broadcasted this turn");
				
				receivedTreesIDs[receivedTotal] = newTreeID;
				receivedTotal += 1;								
			}		
		}		
		
		// SYSTEM CHECK check to see if scouts are reporting this parameter correctly
		// System.out.println("Trees sent this turn by other scouts: " + otherSent);
		
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
							// System.out.println("Currently broadcasting the location of the tree with ID: " + canSend[i].ID);						
							
							// SYSTEM CHECK draw a BLUE DOT to notify which tree is currently being broadcasted
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
						// SYSTEM CHECK draw an ORANGE DOT to notify which trees are not broadcasted this turn
						rc.setIndicatorDot(canSend[i].location, 255, 165, 0);					
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
    
    private static MapLocation move(RobotInfo[] enemyRobots) throws GameActionException{
    	
    	// If the robot is currently not tracking anything
    	if(trackID == -1 || !rc.canSenseRobot(trackID)){    		
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
    			return move(enemyRobots);    	
    		
    		} else{ // If there is no robot to be tracked 
    			trackID = -1;
    			trackedRobot = null;
    			
    			// Posit the desired move location as a forward movement along the last direction
    			MapLocation desiredMove = myLocation.add(myDirection, (float) (Math.random() * 0.5  + 1));
    			
    			// SYSTEM Check - Set LIGHT GREY LINE indicating where the scout would wish to go
    			rc.setIndicatorLine(myLocation, desiredMove, 110, 110, 110);    			
       			
        		// SYSTEM CHECK - Notify that nothing to be scouted has been found
        		// System.out.println("The scout cannot find anything to track");
    			
    			return desiredMove;
    				
    		}
    	} else{ // If the robot is actually currently tracking something
    		// If the currently tracked robot is a gardener, execute special tracking method
    		if (trackedRobot.type == battlecode.common.RobotType.GARDENER){
    			
    			return trackGardener(enemyRobots);     			
    			
    		} else if (trackedRobot.type == battlecode.common.RobotType.ARCHON){
    			
    			return track(enemyRobots, (float) 1.5);
    			
    		} else if (trackedRobot.type == battlecode.common.RobotType.SCOUT){
    			
    			return track(enemyRobots, 1);
    		}
    		
    		else{ // Otherwise the enemy is a soldier/tank/lumberjack and it would be wise to run away!!
    			
    			// SYSTEM CHECK - Display BLACK DOT on nearest hostile to run away from...
    			rc.setIndicatorDot(trackedRobot.location, 0, 0, 0);
    			
    			return runAway(enemyRobots);
    		}
    	}        	
    }
    
    
    private static MapLocation runAway(RobotInfo[] enemyRobots) throws GameActionException{
    	
    	if (rc.canSenseRobot(trackID)){
    		
    		trackedRobot = rc.senseRobot(trackID);
    		
    		// SYSTEM CHECK - Make scout nofity of the presence of hostile
    		//  System.out.println("Am currently running away from a hostile enemy with ID: " + trackID);
	    	
	    	MapLocation desiredMove = null;
    		
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
	    	
	    	// SYSTEM CHECK Print a YELLOW LINE from current location to intended move location
	    	// rc.setIndicatorLine(myLocation, desiredMove, 255, 255, 0);   
	    	
	    	// Reset the enemy track ID, since it is not something that the robot would like to follow at the moment
	    	trackID = -1;
	    	trackedRobot = null;
        	roundsCurrentlyTracked = 0;	 
        	isTracking = false;
        	return desiredMove;   
    	}
    	
    	// If the scout has lost sight of the scary enemy right after seeing it (shouldn't happen at all but if it does here is code to handle it
    	else{
    		// SYSTEM CHECK - Print out that it has evaded enemy
    		System.out.println("The enemy I just saw disappeared WTF");
			
    		// Don't update no track in case the spooky comes back
        	trackID = -1;
        	trackedRobot = null;
        	roundsCurrentlyTracked = 0;
        	isTracking = false;  
        	
        	return move(enemyRobots);
    	}  	
	}	   	

	
	// Wrapper function for finding a new enemy to track
    
    private static RobotInfo findNewTrack(RobotInfo[] enemyRobots){
    	
    	// If there is actually an enemy robot nearby
    	if (enemyRobots.length > 0){
    		// Return the closest one or gardener    		
    		return getNextTarget(enemyRobots);    		
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
    			if (!Korosenai.isLineBLockedByTree(trackedRobot.location, newCheck, obstacleCheck)){
    				
    				return newCheck;
    			}
    		}
    	} 
    	// If no possible firing location is found....
    	return null;    	
    }    
    
    
    // Function to execute when the robot is attempting to track down a gardener
    
    private static MapLocation trackGardener(RobotInfo[] enemyRobots) throws GameActionException{
    	
    	// SYSTEM CHECK - Notify that the scout is following a gardenerr......
    	System.out.println("Attempting to track a  gardener rn......");       	
    	
    	if(rc.canSenseRobot(trackID)){
    		
    		MapLocation desiredMove = null;
    		
    		// Update location of tracked robot - the Gardener...
    		trackedRobot = rc.senseRobot(trackID);
    		
    	   	// SYSTEM CHECK Print a VIOLET LINE from current location to the gardener's location
        	// rc.setIndicatorLine(myLocation, trackedRobot.location, 150, 0, 200);   	
    		
	    	// Get distance and direction between scout and the Gardener
	    	float gap = myLocation.distanceTo(trackedRobot.location);
	    	
	    	// Prevent null pointer exception, assert that the gardener's location has been seen before attempting to access 
	    	if(gardenerLocation == null){
	    	   	// If it has not yet been initialized set the robot's last location as its current
	    		gardenerLocation = trackedRobot.location;
	    	}
	    		
	    	// If the gap between the scout and the gardener is too large, move towards the gardener....
	    	if (gap > 4){
		    	// Update the gardener's location
	    		gardenerLocation = trackedRobot.location;
	    		// Elect to just move towards the gardener
	    		desiredMove = Todoruno.moveTowardsTarget(trackedRobot, myLocation, strideRadius, rotationDirection, desiredMove);     		
	    	}
	    	// Otherwise if the gardener is fairly close...
	    	else{
	    		// See if it is possible for the gardener to shoot at the robot from where it is
	    		boolean noShoot = Korosenai.isLineBLockedByTree(trackedRobot.location, myLocation, obstacleCheck);
	    		
	    		// If the robot was unable to shoot, attempt to find a shooting location
	    		if (noShoot){
	    			
	    			// SYSTEM CHECK - Print out that the scout wants to shoot a robot but there is a tree in the way...
	    			System.out.println("Attempting to shoot from currentlocation is impossible - path blocked to target");
	    			
	    			// Get the direction to the robot FROM the target - should be opposed to dir
	    			Direction dirFromTarget = new Direction(trackedRobot.location, myLocation);
	    			
	    			// Obtain a random number between 3 and 4 to find a firing location...
	    			float testDist = 3 + (float) Math.random();
	    			
	    			// See if such a firing location exists
	    			MapLocation tryFiringLocation = findFiringLocation(testDist, dirFromTarget);
	    			
	    			// If it does set it as the desired moving point (though this may be altered by positional post-processing
	    			if (tryFiringLocation != null){
	    				
	    				desiredMove = tryFiringLocation;
	    				if(rc.canFireSingleShot()){
	    					
	    			    	desiredMove = tryFiringLocation;
	    			    	
	    			    	// SYSTEM CHECK print GREEN_YELLOW to new location to fire from
	    			    	rc.setIndicatorLine(myLocation, tryFiringLocation, 175, 255, 50);
	    		    	}	    			
	    			}
	    			else{
	    		    	// Update the gardener's location
	    	    		gardenerLocation = trackedRobot.location;
	    				// If no firing location can be found simply utilize the tracking portion of the algorithm
	    	    		desiredMove = Todoruno.moveTowardsTarget(trackedRobot, myLocation, strideRadius, rotationDirection, desiredMove);   
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
		    		
		    	   	// SYSTEM CHECK Print LIGHT BLUE GREEN LINE from current location to intended move location when tracking gardener
		        	rc.setIndicatorLine(myLocation, desiredMove, 0, 200, 200);   			    		
		    		
    		    	// Update the gardener's location
    	    		gardenerLocation = trackedRobot.location;
	    		}   
	    	}
	    	
	    	return desiredMove;
	    	
	    // Of the gardener is no longer visible, run following code..
    	} else{    		
    		// If the scout can no longer sense the target gardener...
    		noTrack[noTrackUpdateIndex % 3] = trackID;
			noTrackUpdateIndex += 1;
			
        	trackID = -1;
        	trackedRobot = null;
        	roundsCurrentlyTracked = 0;
        	isTracking = false;        	
			
        	// Update the location of be null, so that the new gardener refreshes the data...
        	gardenerLocation = null;
        	// SYSTEM CHECK - Notify of lost sight of gardener
        	System.out.println("Lost sight of gardener (ittai nani??????????) /Finding a new target");    
        	
        	// Call the move function to obtain a new target
        	return move(enemyRobots);    
    	}    	
    }    
    

    // Function to follow a unit and approach it
    
	private static MapLocation track(RobotInfo[] enemyRobots, float multiplier) throws GameActionException{
		
		
		// If the robot can currently sense the robot it is tracking and if it has not been tracking this robot for too long
    	if ((rc.canSenseRobot(trackID) && roundsCurrentlyTracked < 80) || rc.canSenseRobot(trackID) && mustDefend){
    		
    		MapLocation desiredMove = null;
    		
    		// SYSTEM CHECK - See if the robot identifies that it is actually tracking something
    		System.out.println("I am currently tracking a robot with ID: " + trackID);
    		
    		// Update location of tracked robot 
    		trackedRobot = rc.senseRobot(trackID);
    		// SYSTEM CHECK - Draw a VIOLET LINE between current position and position of robot
    		rc.setIndicatorLine(myLocation, trackedRobot.location, 150, 0, 200);
    		
    		// Increment number of rounds tracked
        	roundsCurrentlyTracked +=1;
        	
        	desiredMove = Todoruno.moveTowardsTarget(trackedRobot, myLocation, strideRadius, rotationDirection, desiredMove, multiplier);
        	
        	isTracking = true;
        	hasNotTracked = 0;
        	
        	return desiredMove;
        	
        // If the robot has been tracking its current prey for too long or has lost sight of its target
    	} else {
    		
    		// If the robot has been tracking the current enemy for a long time make sure it doesnt check the same enemy again
    		noTrack[noTrackUpdateIndex % 3] = trackID;
			noTrackUpdateIndex += 1;
			
        	trackID = -1;
        	trackedRobot = null;
        	roundsCurrentlyTracked = 0;
        	isTracking = false;        	
			
        	// SYSTEM CHECK - Notify of target loss
        	System.out.println("Lost sight of target/Finding a new target");        	
        	
        	// Call move to obtain a new location to try to move to
        	return move(enemyRobots);       	   	
    	}	                		
    }	
		
	/*****************************************************************************
	 ******************* Miscellaneous Functions************** ********************
	 ****************************************************************************/   
	
	
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
			if (currentAllies[i].type == battlecode.common.RobotType.GARDENER){
				
				float dist = myLocation.distanceTo(currentAllies[i].location);

				if (dist < minimum){					
							
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
	
	private static RobotInfo getNextTarget(RobotInfo[] enemyRobots){

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
	
	
	// Function to notify everyone that the unit has died.
	
    public static void manageBeingAttacked(MapLocation location) throws GameActionException{
    	
    	// Boolean to determine whether or not the scout will lose health if it moves to a certain location
		boolean beingAttacked = iFeed.willBeAttacked(location);
		
		//If it will lose health for going there...
		if (beingAttacked) {
			
			// Check if the unit will die from the damage
			boolean willDie = iFeed.willFeed(location);
			
			//If it will die, broadcast to all relevant channesl.....
			
			if (willDie) {
				iDied = true;
				// Get own scoutNumber  and unitNumber- important for broadcasting 
			    scoutNumber = rc.readBroadcast(BroadcastChannels.SCOUT_NUMBER_CHANNEL);
			    currentNumberofScouts = scoutNumber - 1;
			    
			    unitNumber = rc.readBroadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL);
			    rc.broadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL, unitNumber - 1);
			    
			    // Update the number of scouts so that other scouts can recognize....
			    rc.broadcast(BroadcastChannels.SCOUT_NUMBER_CHANNEL, currentNumberofScouts);
			}
		}
    }    
}	
