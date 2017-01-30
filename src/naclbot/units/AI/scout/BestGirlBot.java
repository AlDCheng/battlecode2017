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


/* --------------------------   Overview  --------------------------
 * 
 * 			AI Controlling the functions of the ScoutBot
 *
 *				 ~~ Coded by Illiyia (akimn@#mit.edu)
 *
 *			 Call the init() function to use the file...
 * 
 * 		  Note: Debug statements all begin with SYSTEM CHECK 
 * 
 ------------------------------------------------------------------- */

/* -------------------- LIST OF THINGS TO DO??? --------------------
 * 
 * 1. Create specialized files for scouts that go to the enemy, regular scouts..... etc...
 * 
 * 2. Fix their shooting of gardeners
 * 
 * 3. Clean up the file ewwwww...................... * 
 * 
 ------------------------------------------------------------------- */

public class BestGirlBot extends GlobalVars {	
	
	// ----------------------------------------------------------------------------------//
	// ------------------------- VARIABLES FOR USE BY THE ROBOT -------------------------//
	// ----------------------------------------------------------------------------------//	
	
	// ------------- GAME VARIABLES -------------//
	
	// Variable to store the round number
	protected static int onlyRemIsBestGirl;
	
	// Variables to store the teams currently in the game
	public static Team enemies;
	public static Team allies;
	
	// Gamne-defined robot class related parameters
	protected static final float strideRadius = battlecode.common.RobotType.SCOUT.strideRadius;
	protected static final float bodyRadius = battlecode.common.RobotType.SCOUT.bodyRadius;
	protected static final float sensorRadius = battlecode.common.RobotType.SCOUT.sensorRadius;
	
	// ------------- PERSONAL VARIABLES -------------//
	
	// Self-identifiers...
	public static int myID; // Game-designated ID of the robot
	public static int unitNumber; // Team-generated unit number - represents order in which units were built
	public static int scoutNumber; // Team generated number - represents order in which scouts were built
	
	private static int initRound; // The initial round in which the robot was constructed
	
	// Personal movement variables
	protected static MapLocation myLocation; // The current location of the scout...
	protected static MapLocation lastPosition; // The previous location that the scout was at...
	protected static Direction lastDirection; // The direction in which the scout last traveled
    public static boolean rotationDirection = true; // Boolean for rotation direction - true for counterclockwise, false for clockwise
	
	// ------------- OPERATION VARIABLES -------------//
	
	// Variables related to tracking....
	private static int trackID; // The robot that the scout is currently tracking....
	private static RobotInfo trackedRobot; // The Robot that the scout is currently tracking....
	private static boolean isTracking; // Boolean to show whether or not the scout is currently tracking something or not...
	    
	// Enemy data variables....
	private static RobotInfo[] previousRobotData; // Array to store the data of enemy robots from the previous turn.....
    private static int[] noTrack = new int[3]; // Array to store the data regarding the last three enemies the scout has tracked
    private static int noTrackUpdateIndex;  // Index to check where to store the next datum regarding  enemies not to track..
	private static int roundsCurrentlyTracked; 	// Stores the number of rounds that the scout has been tracking the current enemy
    public static int hasNotTracked; // Variable to see how long the robot has not tracked another unit for
    
    // Variables related to gardener defense.....
    private static MapLocation defendLocation; // Location that the scout must defend...
    private static int defendAgainstID; // Enemy to search for once the scout has reached that location
    
    // Variable to store the number of bullets the team has as of yet....
	protected static float teamBullets;

	// ------------- TREE SEARCH VARIABLES -------------// UNUSED
	
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
	// private final static int treeSenseDistance = 7; // UNUSED
	
	// ------------- ADDITIONAL VARIABLES/CONSTANTS -------------//

	// Variables related to operational behavior...
	protected static MapLocation nearestCivilianLocation; // Stores for multiple rounds the location of the nearest civilian robot....	

	// Various behavioral constants...
	private static final float obstacleCheck = (float)0.4;    
	private static int harvestThreshold = 400;
	private static float defendDistance = 30;
	
    // Store the last known location of the gardener being tracked
    private static MapLocation gardenerLocation;  
    
    // Miscellaneous variables.....
 	protected static boolean believeHasDied; // Stores whether or not the robot believes it will die this turn or not.........
     
	// ----------------------------------------------------------------------------------//
	// -------------------------------- RUNTIME FUNCTIONS -------------------------------//
	// ----------------------------------------------------------------------------------//	
		
	// Initialization function - makes the default values for most important parameters
	
	public static void init() throws GameActionException {
				
        // Important parameters for self
        enemies = rc.getTeam().opponent();
        allies = rc.getTeam();
        myID = rc.getID();
        teamBullets = rc.getTeamBullets();
        
        // Get own scoutNumber  and unitNumber- important for broadcasting 
        scoutNumber = rc.readBroadcast(BroadcastChannels.SCOUT_NUMBER_CHANNEL);        
        unitNumber = rc.readBroadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL);      
        
        // Get the current round number......
        onlyRemIsBestGirl = rc.getRoundNum();
        initRound = onlyRemIsBestGirl;
        
        // Initialize values relating to tree broadcasting
        seenTotal = 0;
        sentTotal = 0;
        receivedTotal = 0;  
        
        // Initialize variables important to self
        myLocation = rc.getLocation();
        trackID = -1;
        noTrackUpdateIndex = 0;
        isTracking = false;
        trackedRobot = null;
        gardenerLocation = null;    
        previousRobotData = null;                
        
       	// In order to get the closest current ally..... obtain data for the nearest allied units and then the gardener if it exists....
     	RobotInfo[] alliedRobots = NearbyUnits(allies, sensorRadius);
       	RobotInfo nearestGardener = Todoruno.getNearestGardener(alliedRobots, myLocation);
       	
       	// If there is a gardener nearby, set the nearest civilian location accordingly...
       	if (nearestGardener != null){       		
       		nearestCivilianLocation = nearestGardener.location;
       	}
       	// Otherwise use the data stored in the broadcast of the initial archon locations...
       	else{           	
       		// Get the locations from the archon broadcasts
            int archonInitialX = rc.readBroadcast(BroadcastChannels.ARCHON_INITIAL_LOCATION_X) / 100;
            int archonInitialY = rc.readBroadcast(BroadcastChannels.ARCHON_INITIAL_LOCATION_Y) / 100;
    		
            // Set the nearestCivilianLocation using the data gained...
            nearestCivilianLocation = new MapLocation(archonInitialX, archonInitialY);       		
       	}
       	
       	// Set the scout to first attempt to move away from the nearest civilian initially....
       	lastDirection = nearestCivilianLocation.directionTo(myLocation);
        
        // Initialize variables relating to defending....
        defendLocation = null;
        defendAgainstID = -1;     
        
       	// Retrieve the number of active lumberjacks and increment......
       	int numberOfActiveScouts = rc.readBroadcast(BroadcastChannels.SCOUTS_ALIVE_CHANNEL);
       	rc.broadcast(BroadcastChannels.SCOUTS_ALIVE_CHANNEL, numberOfActiveScouts + 1);    
        
        // Update the number of scouts so that other scouts can recognize....
        rc.broadcast(BroadcastChannels.SCOUT_NUMBER_CHANNEL, scoutNumber + 1);
        rc.broadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL, unitNumber + 1);

        
        // If the scout is the first to be made and was made early enough, call Rembot..........
        if(rc.readBroadcast(BroadcastChannels.HIRE_REMBOT_CHANNEL) == 1){
        	rc.broadcast(BroadcastChannels.HIRE_REMBOT_CHANNEL, 0);    
        	RemBot.init();
        }
        RemBot.init();

        
        // By default pass on to the main function
        main();    

	}
	
	// Main function of the scout - carries out all of the necessary tasks in a turn.....
		
	protected static void main() throws GameActionException{	            
        
		// Initialize other parameters for tracking
	    roundsCurrentlyTracked = 0;
	    
	    // Clear memory of last tracked members
	    Arrays.fill(noTrack, -1);    
	    
        // Code to be performed every turn        
        while (true) {
        	
            // Main actions of the scout.....
            try {
    		    // SYSTEM CHECK  Make sure the robot starts its turn
                System.out.println("Beginning Turn!");                
        		
				// ------------------------ INTERNAL VARIABLES UPDATE ------------------------ //     

            	// Get nearby enemies and allies and bullets for use in other functions            	
            	RobotInfo[] enemyRobots = NearbyUnits(enemies, sensorRadius);
            	RobotInfo[] alliedRobots = NearbyUnits(allies, sensorRadius);
            	BulletInfo[] nearbyBullets = rc.senseNearbyBullets();
            	
		    	// Update game variables
            	teamBullets = rc.getTeamBullets();
            	
            	// Make sure the robot shows appreciation for the one and only best girl in the world.
            	// If you are reading this and you think Emilia is best girl I have no words for you
		        onlyRemIsBestGirl = rc.getRoundNum();

		    	
		    	// Update positional and directional variables
		        myLocation = rc.getLocation();
	
		        
		    	// See if there is a gardener and update the location of the nearest civilian if there is one....
		    	RobotInfo nearestGardener = Todoruno.getNearestGardener(alliedRobots, myLocation);
		    	
		    	// Set the nearest civilian location accordingly...
		       	if (nearestGardener != null){   
		       		
		       		nearestCivilianLocation = nearestGardener.location;	       		 		
		       	}
		       	
		       	// If the robot hadn't moved the previous turn... this value may be null
		       	if (lastDirection == null){
		       		
		       		// Set the direction to go to as away from the last known nearest civilian
		       		lastDirection = nearestCivilianLocation.directionTo(myLocation);
		       	}	
            	
		       	// SYSTEM CHECK - Show where the scout believes its nearest civilian is using a WHITE LINE
		       	// rc.setIndicatorLine(myLocation, nearestCivilianLocation, 255, 255, 255);		       	
            	
            	// If the robot has not tracked anything for a long time fill the no track with -1 so it can track something again // UPDATE
            	if (hasNotTracked > 25){
            		Arrays.fill(noTrack, -1);
            	}
            	
		       	// ------------------------ BROADCAST UPDATES ------------------------ //
			    	
		       	// Update the nearest enemy and archon locations - If it sees any.......
            	BroadcastChannels.broadcastNearestEnemyLocation(enemyRobots, myLocation, unitNumber, nearestCivilianLocation, onlyRemIsBestGirl);            	
            	BroadcastChannels.broadcastEnemyArchonLocations(enemyRobots);
            	
            	// Update the distress info and retreat to the gardener if necessary..... if necessary            	
            	BroadcastChannels.BroadcastInfo distressInfo = BroadcastChannels.readDistress(myLocation, defendDistance);
            	
            	// If the robot thought it died previously but didn't.... update information...
            	if(believeHasDied){
            		fixAccidentalDeathNotification();
            	}
            	
            	// ------------------------ MOVEMENT FUNCTIONS------------------------ //      
            
            	// Placeholder for the location where the robot desires to move - can be modified by dodge
            	MapLocation desiredMove = decideAction(distressInfo, enemyRobots);
            	
		       	if(desiredMove != null){
			       	// SYSTEM CHECK - Print out where the desired move is.....
			       	System.out.println("Currently attempting to move to location: " + desiredMove.toString());
		       	}
            	
            	// -------------------- MOVE CORRECTION ---------------------//
            	
            	// Get the correction from the wrapping correct all move function....
            	MapLocation correctedMove = Yuurei.correctAllMove(strideRadius, bodyRadius, false, allies, myLocation, desiredMove);            	
		       	
		       	if(correctedMove != null){
		       		
	    	       	// SYSTEM CHECK - Print out where the desired move is.....
			       	System.out.println("Corrected move is: " + correctedMove.toString());	
			       	
			       	// Set the desired location to be the corrected location
			       	desiredMove = correctedMove;
		       	}
		       	// If the robot could not find a location to go to even with the corrected location.....
		       	else{	       		
		       		// SYSTEM CHECK - Print out that the scout never had a place to go to...
		       		System.out.println("No move possible..... will simply remain in place");
		       		
		       		desiredMove = myLocation;		       		
		       	}
		       	
		       	if(desiredMove.equals(myLocation)){
		       		
		       		// Check to see if the robot is in a corner...
		       		int corner = Yuurei.checkIfNearCorner(bodyRadius, strideRadius, desiredMove);
		       		
		       		// If the above function returns a positive integer, the robot is near a corner....
		       		if (corner != 0){
		       			
		       			// SYSTEM CHECK - Print out that the robot is near a corner....
		       			System.out.println("Currently near a corner, will attempt to rectify....");
		       			
		       			desiredMove = Yuurei.moveOutOfCorner(strideRadius, corner, desiredMove);		       	
		       		}		       		
		       	}
		       	
		       	// --------------------------- DODGING ------------------------ //
		       	
            	// Call the dodge function
            	MapLocation dodgeLocation = Yuurei.attemptDodge(desiredMove, myLocation, nearbyBullets, strideRadius, bodyRadius, -1, rotationDirection, false);
            	    			
            	// If there is a location that the unit can dodge to..
            	if (dodgeLocation != null){
            		
            		// SYSTEM CHECK - Print out the dodge location.....
            		System.out.println("Dodge location is: " + dodgeLocation.toString());
            		
            		// Set the location to dodge to as the new dodge location.....
            		desiredMove = dodgeLocation;
            	}            	
		       	
		       	// If the robot can move to the location it wishes to go to.....
		       	if(rc.canMove(desiredMove)){
		       		
		       		// Check to see if the robot will die there
		       		checkDeath(desiredMove);
		       		
		       		// Move to the target location
		       		rc.move(desiredMove);
		       	}
		       	
		       	// If the robot didn't move along, check if it would die from staying in its current location....
		       	else{
		       		checkDeath(myLocation);
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
            		
            		Korosenai.tryShootAtEnemy(shootingLocation, myLocation, 0, alliedRobots, alliedTrees, sensorRadius,trackedRobot);
            	}
    
            	// ------------------------ ROUND END VARIABLE UPDATES ---------------------- //	

                // Make it so that the last direction traveled is the difference between the robot's current and final positions for the round...
                lastPosition =  rc.getLocation();
                lastDirection = new Direction(myLocation, lastPosition);
                
                // If the robot was not tracking, increment the value by one round....
                if (!isTracking){
                	hasNotTracked += 1;
                }
                
                // Store the data for the locations of the enemies previously.....
                previousRobotData = enemyRobots;
                
	            // SYSTEM CHECK  Make sure the robot finishes its turn
                System.out.println("Turn completed!");
		       	
                // Yield until the next turn.......
            	Clock.yield();

            } catch (Exception e) {
                System.out.println("Scout Exception");
                e.printStackTrace();
            }
        }
    }	
	
	// ----------------------------------------------------------------------------------//
	// ------------------------------- MOVEMENT FUNCTIONS -------------------------------//
	// ----------------------------------------------------------------------------------//	

	
	// Function to determine how the robot will act this turn....
	
	private static MapLocation decideAction(BroadcastChannels.BroadcastInfo distressInfo, RobotInfo[] enemyRobots) throws GameActionException{
		
		// Check to find the nearest bullet tree.......
		TreeInfo nearestBulletTree = findNearestBulletTree();		
		
		// If the scout found some distress signal this turn...
    	if(distressInfo != null){
    		
    		// SYSTEM CHECK - Print out that a distress signal has been received...
    		System.out.println("Distress Signal Received....");
    		
    		// If the distressed gardener is being attacked by a scout.....
    		if (distressInfo.enemyType == 2){
    			
    			// Set the location to defend...
    			defendLocation = new MapLocation (distressInfo.xPosition, distressInfo.yPosition);
				
    			// Set the ID of the offending enemy
				defendAgainstID = distressInfo.ID;
    		}            	
    	}
    	
    	// If the robot is meant to defend........
    	if (defendLocation != null){            		
			
    		return defend(enemyRobots);
    	}
		
    	// If the team currently doesn't have too many bullets and the robot is currently not tracking anything...... call the harvest function
    	else if ((teamBullets < harvestThreshold && nearestBulletTree != null) && !isTracking){
    		
    		// SYSTEM CHECK - Make sure that the scout knows that there are too few bullets on present team....
    		System.out.println("Team requires additional bullets, so will attempt to find more");            		

			// Do the harvest function to attempt to get some bullets from some trees...
    		return harvest(nearestBulletTree);            		
    	}
    	
    	// Otherwise just call the move function normally......
    	else {
    		return move(enemyRobots);
    	}
	}
	
	
	// General move function.....
    
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
    			MapLocation desiredMove = myLocation.add(lastDirection, (float) (Math.random() * 0.5  + 1));
    			
    			// SYSTEM Check - Set LIGHT GREY LINE indicating where the scout would wish to go
    			rc.setIndicatorLine(myLocation, desiredMove, 110, 110, 110);    			
       			
        		// SYSTEM CHECK - Notify that nothing to be scouted has been found
        		// System.out.println("The scout cannot find anything to track");
    			
    			return desiredMove;
    				
    		}
    	} else{ // If the robot is actually currently tracking something
    		
    		// If the currently tracked robot is a gardener, execute special tracking method    		
    		if (trackedRobot.type == battlecode.common.RobotType.GARDENER){
    			
    			// SYSTEM CHECK - Print out that the robot is currently tracking a gardener....
    			System.out.println("Currently tracking a gardener...");
    			
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
	
	
	// Function for the robot to go back and defend if it has received a distress signal....
	
	private static MapLocation defend(RobotInfo[] enemyRobots) throws GameActionException{		

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
			return track(enemyRobots, 1);            			
		}
		// If the robot has arrived at the defend location and has not found the enemy.....
		else if (myLocation.distanceTo(defendLocation) <= 5){
			
			defendLocation = null;
			defendAgainstID = -1;    			
			
			// SYSTEM CHECK - display a green line to the distress location....
			rc.setIndicatorLine(myLocation, defendLocation, 0, 128, 0);
			
			// SYSTEM CHECK IF the robot has need to defend, it will do so...
			System.out.println("Returned to distress call but found no one");
			
			// Exit the call to defendLocations and go on back to normal operations
			return move(enemyRobots);
		}
		else{
			
			Direction defendDirection = myLocation.directionTo(defendLocation);
		
			
			// SYSTEM CHECK - display a blue line to the distress location....
			rc.setIndicatorLine(myLocation, defendLocation, 0, 0, 128);
			
			// SYSTEM CHECK IF the robot has need to defend, it will do so...
			System.out.println("Travelling back to defend....");
			
			return myLocation.add(defendDirection, strideRadius);
			
		}            		         		
	}
	
	
	// Function for the scout to go around and harvest bullets.....
	
	
	private static MapLocation harvest(TreeInfo nearestBulletTree) throws GameActionException{

		MapLocation desiredMove = null;
		
		float distanceTo = myLocation.distanceTo(nearestBulletTree.location);
		
		Direction directionTo = new Direction(myLocation, nearestBulletTree.location);
		
		// If the robot is too far away from the tree.... move towards it....
		if (distanceTo >= strideRadius){
			
			desiredMove = myLocation.add(directionTo, strideRadius);
			
			// SYSTEM CHECK - Draw a WHITE LINE to the tree to be moved towards
			rc.setIndicatorLine(myLocation, nearestBulletTree.location, 255, 255, 255);			
		}
		// If the robot is within strideRadius of the tree, move to its center
		else if(distanceTo > 1){
			
			desiredMove = myLocation.add(directionTo, distanceTo);
			
			
			// SYSTEM CHECK - Draw a WHITE LINE to the tree to be moved towards
			rc.setIndicatorLine(myLocation, nearestBulletTree.location, 255, 255, 255);
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
				System.out.println("ERROR: Within one unit of tree but for some reason cannot shake it...........");
			}
		}
		return desiredMove;
	}	
	
	
	// Function to maintain distance from various enemies.....
    
    private static MapLocation runAway(RobotInfo[] enemyRobots) throws GameActionException{
    	
    	// Make sure that the robot can sense the enemy inputted.....
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
	    	
	    	// Reset the enemy track ID, since the robot should always search for the nearest hostile enemies...
	    	trackID = -1;  	trackedRobot = null;	roundsCurrentlyTracked = 0;	isTracking = false;
	    	
        	return desiredMove;   
    	}
    	
    	// If the scout has lost sight of the scary enemy right after seeing it (shouldn't happen at all but if it does here is code to handle it
    	else{
    		// SYSTEM CHECK - Print out that it has evaded enemy
    		System.out.println("The enemy I just saw disappeared from sight.....");
			
    		// Don't update no track in case the spooky comes back
        	trackID = -1;	trackedRobot = null;	roundsCurrentlyTracked = 0;	isTracking = false;  
        	
        	// Call the move function again.....
        	return move(enemyRobots);
    	}  	
	}	   	
	
	// ----------------------------------------------------------------------------------//
	// ---------------------- TREE SEARCH AND BROADCAST FUNCTIONS -----------------------//
	// ----------------------------------------------------------------------------------//	
	
	
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
    	TreeInfo[] nearbyTrees = addTrees(sensorRadius);
    	
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
    
    
	// Function to Broadcast the locations of all trees found this turn // UNUSED
	
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
	    
       
	// ----------------------------------------------------------------------------------//
	// ------------------------------- TRACKING FUNCTIONS -------------------------------//
	// ----------------------------------------------------------------------------------//	    

    // Function to follow a unit and follow it....
    
	private static MapLocation track(RobotInfo[] enemyRobots, float multiplier) throws GameActionException{
		
		
		// If the robot can currently sense the robot it is tracking and if it has not been tracking this robot for too long
    	if ((rc.canSenseRobot(trackID) && roundsCurrentlyTracked < 80)){
    		
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

    
    // Function to execute when the robot is attempting to track down a gardener
    
    private static MapLocation trackGardener(RobotInfo[] enemyRobots) throws GameActionException{
    	
    	// SYSTEM CHECK - Notify that the scout is following a gardener......
    	System.out.println("Attempting to track a gardener with ID:" + trackID);       	
    	
    	if(rc.canSenseRobot(trackID)){
    		
    		MapLocation desiredMove = null;
    		
    		// Update location of tracked robot - the Gardener...
    		trackedRobot = rc.senseRobot(trackID);
    		
    	   	// SYSTEM CHECK Print an INDIGOfrom current location to the gardener's location
        	rc.setIndicatorLine(myLocation, trackedRobot.location, 75, 0, 130);   	
    		
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
    
	// ----------------------------------------------------------------------------------//
	// --------------------------- MISCELANNELOUS FUNCTIONS -----------------------------//
	// ----------------------------------------------------------------------------------//	     
	
    
	// Simple function to obtain data about units of a certain team within a certain distance
	
	protected static RobotInfo[] NearbyUnits(Team team, float distance){	
		
		return rc.senseNearbyRobots(myLocation, distance, team);
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

	// Function to check if the scout will die if it moves to a certain location
	
    private static void checkDeath(MapLocation location) throws GameActionException{
    	
    	// Boollean to store if the robot believes it will be hit if it moves to a certain location......
		boolean beingAttacked = iFeed.willBeAttacked(location);
		
		// If it will get hit from that location....
		if (beingAttacked) {
			
			// SYSTEM CHECK - Print out that the robot thinks it will die this turn....
			System.out.println("Moving to desired location will result in death........");
			
			// If the lumberjack will lose all of its health from moving to that location....
			boolean willDie = iFeed.willFeed(location);
			
			// If the lumberjack believes that it will die this turn....
			if (willDie) {
				
				// Set the belief variable to true.....
				believeHasDied = true;
				
				// Get the current number of lumberjacks in service
		        int currentScoutNumber = rc.readBroadcast(BroadcastChannels.SCOUTS_ALIVE_CHANNEL);
		        
		        // Update lumberjack number for other units to see.....
		        rc.broadcast(BroadcastChannels.SCOUTS_ALIVE_CHANNEL, currentScoutNumber - 1);

			}
		}
	}
    
    // Function to correct an accidental death update
    
    private static void fixAccidentalDeathNotification() throws GameActionException{
    	
    	// Reset belief in the robot dying this round....
    	believeHasDied = false;    	

		// Get the current number of lumberjacks in service
        int currentScoutNumber = rc.readBroadcast(BroadcastChannels.SCOUTS_ALIVE_CHANNEL);
        
        // Update lumberjack number for other units to see.....
        rc.broadcast(BroadcastChannels.SCOUTS_ALIVE_CHANNEL, currentScoutNumber + 1);
    	
    }  
}	
