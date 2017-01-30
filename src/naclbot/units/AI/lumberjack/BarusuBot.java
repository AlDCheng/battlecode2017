// AI for lumber jack

package naclbot.units.AI.lumberjack;
import battlecode.common.*;
import naclbot.units.motion.Move;
import naclbot.units.motion.Todoruno;
import naclbot.units.motion.Yuurei;
import naclbot.units.motion.routing.Routing;
import naclbot.variables.BroadcastChannels;
import naclbot.variables.GlobalVars;
import naclbot.units.interact.iFeed;
import java.util.Arrays;

/* --------------------------   Overview  --------------------------
 * 
 * 			AI Controlling the functions of the Lumberjack
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
 * 1. Integrate commands - i.e. an important tree to chop has been found
 * 
 * 2. Make their combat less retarded.....
 * 
 * 3. Fix up lumberjacks getting stuck on trees they can't actually get to.....
 * 
 * 
 ------------------------------------------------------------------- */

public class BarusuBot extends GlobalVars {	
	
	// ----------------------------------------------------------------------------------//
	// ------------------------- VARIABLES FOR USE BY THE ROBOT -------------------------//
	// ----------------------------------------------------------------------------------//	
	
	// ------------- GAME VARIABLES -------------//
	
	// Variable to store the round number
	private static int roundNumber;
	
	// Variables to store the teams currently in the game
	public static Team enemies;
	public static Team allies;
	
	// Gamne-defined robot class related parameters
	private static float strideRadius = battlecode.common.RobotType.LUMBERJACK.strideRadius;
	private static float bodyRadius = battlecode.common.RobotType.LUMBERJACK.bodyRadius;
	private static float sensorRadius = battlecode.common.RobotType.LUMBERJACK.sensorRadius;
	private static float strikeRadius = battlecode.common.RobotType.LUMBERJACK.strideRadius;
	private static float interactRadius = 1;
	
	// ------------- PERSONAL VARIABLES -------------//
	
	// Self-identifiers...
	public static int myID; // Game-designated ID of the robot
	public static int unitNumber; // Team-generated unit number - represents order in which units were built
	public static int lumberjackNumber; // Team generated number - represents order in which lumberjacks were built
	
	private static int initRound; // The initial round in which the robot was constructed
	
	
	// Personal movement variables
	private static MapLocation myLocation; // The current location of the lumberjack...
	private static MapLocation lastLocation; // The previous location that the lumberjack was at...
	private static Direction lastDirection; // The direction in which the lumberjack last traveled
	
	// ------------- OPERATION VARIABLES -------------//
	
	// Variables related to tracking....
	private static int trackID; // The robot that the lumberjack is currently tracking....
	private static RobotInfo trackedRobot; // The Robot that the lumberjack is currently tracking....
	private static boolean isTracking; // Boolean to show whether or not the lumberjack is currently tracking something or not...
    
	
	// Variables related to tree harvesting...
    private static int treeID; // Stores the ID of the tree the lumberjack is currently attempting to harvest
    private static TreeInfo treeToHarvest; // Stores the information of the tree that the lumberjack is currently attempting to harvest
    private static final float maxTreeSearchRange = (float) 4.5; // The maximal distance the robot will search in order to find a new tree
    private static final int treeSearchAngleNumber = 24; // The number of angles that the lumberjack will search in order to find a new tree
    private static final float interactDistance = (float) 0.2; // How close the robot will attempt to get to a tree before attempting to interact with it.....
    
    
	// ------------- PATH PLANNING VARIABLES -------------// UNUSED
    
	// Variables related to routing.... 
    private static MapLocation goalLocation;  // MapLocation to store a target location when being told to go to a location    	
	private static boolean isCommanded; // Boolean to store whether or not the soldier current has orders to go somewhere....
	public static int roundsRouting = 0; // FVariable to store the length of time the robot has been in path planning mode....
    
	// Routing constants
    public static final int attackFrequency = 0; // Asserts how often robots will attempt to go on the attack after completing a prior attack....
    public static final float attackProbability = (float) 1; // Gives probability of joining an attack at a particular time....
    private static int lastCommanded = attackFrequency; // Int to store the number of rounds since the unit was last in a commanded mode - threshold value
    public static final int giveUpOnRouting = 500; // Variable to determine after how long soldiers decide that Alan's code is a piece of shit......
    
    
	// ------------- ADDITIONAL VARIABLES/CONSTANTS -------------//
    
    // Constants for movement....
    private static final int initialDispersionRounds = 2; // Number of rounds for which the lumberjack will be forced to move away from its initial location
    private static final int distanceDefend = 5; // The maximum distance at which the lumberjack will sincerely attempt to defend its allies...
 
	// Variables related to operational behavior...
	private static MapLocation nearestCivilianLocation; // Stores for multiple rounds the location of the nearest civilian robot....	
	private static boolean nearbyAllyCheckOverride; // Stores whether or not the lumberjack will attempt to avoid hitting nearby allies.....
	
	// Miscellaneous variables.....
	private static boolean believeHasDied; // Stores whether or not the robot believes it will die this turn or not.........
    private static boolean hasCalledMove; // Stores whether or not the robot should call the move function again - prevent infinite loop....
	
	
	// ----------------------------------------------------------------------------------//
	// -------------------------------- RUNTIME FUNCTIONS -------------------------------//
	// ----------------------------------------------------------------------------------//	
	
    // Initialization function
    	// Sets important variable declarations before moving on to the main function....
	
	public static void init() throws GameActionException{
		
		// Initialize game variables
        roundNumber = rc.getRoundNum();
		enemies = rc.getTeam().opponent();
        allies = rc.getTeam(); 

		
        // Initialize personal variables
        myID = rc.getID();        
        unitNumber = rc.readBroadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL);
        lumberjackNumber = rc.readBroadcast(BroadcastChannels.LUMBERJACK_NUMBER_CHANNEL);
        
        initRound = roundNumber;
        
        myLocation = rc.getLocation();
        lastLocation = myLocation;
        
        // Initialize operational variables       	
        trackID = -1;
        trackedRobot = null;
        isTracking = false;
        
        treeID = -1;
        treeToHarvest = null;
       
        // Initialize path list and goal location
//       	routingPath = new ArrayList<MapLocation>();    	
//       	Routing.setRouting(routingPath);
       	
       	// In order to get the closest current ally..... obtain data for the nearest allied units and then the gardener if it exists....
     	RobotInfo[] alliedRobots = NearbyUnits(allies, sensorRadius);
       	RobotInfo nearestGardener = getNearestGardener(alliedRobots);
       	
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
       	
       	// Set the lumberjack to first attempt to move away from the nearest civilian initially....
       	lastDirection = nearestCivilianLocation.directionTo(myLocation);
        
       	// Retrieve the number of active lumberjacks and increment......
       	int numberOfActiveLumberjacks = rc.readBroadcast(BroadcastChannels.LUMBERJACKS_ALIVE_CHANNEL);
       	rc.broadcast(BroadcastChannels.LUMBERJACKS_ALIVE_CHANNEL, numberOfActiveLumberjacks + 1);       	
       	
        // Update broadcasts for unitcount and lumberjack count for other robots....
        rc.broadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL, unitNumber + 1);
        rc.broadcast(BroadcastChannels.LUMBERJACK_NUMBER_CHANNEL, lumberjackNumber);
        
        // Pass into main function
        main();        
	}
	
	private static void main() throws GameActionException{
		
		// While loop to keep the lumberrjack going......
		while (true){
			
			// Main actions of the lumberjack........
			try{
			    // SYSTEM CHECK  Make sure the robot starts its turn
                System.out.println("Beginning Turn!");
				
				// ------------------------ INTERNAL VARIABLES UPDATE ------------------------ //
				
		    	// Get nearby enemies and allies and bullets for use in other functions            	
		    	RobotInfo[] enemyRobots = NearbyUnits(enemies, sensorRadius);
		    	RobotInfo[] alliedRobots = NearbyUnits(allies, sensorRadius);
		      	TreeInfo[] nearbyTrees = rc.senseNearbyTrees();
				
		    	// Reset the value of this so that the robot doesn't try hitting its allies again.....
		    	nearbyAllyCheckOverride = false;
		    	
		    	// Reset the calling of move..
		    	hasCalledMove = false;
		    		
		    	// Update game variables
		        roundNumber = rc.getRoundNum();
		    	
		    	// Update positional and directional variables
		        myLocation = rc.getLocation();
		    	
		    	// See if there is a gardener and update the location of the nearest civilian if there is one....
		    	RobotInfo nearestGardener = getNearestGardener(alliedRobots);
		    	// Set the nearest civilian location accordingly...
		       	if (nearestGardener != null){       		
		       		nearestCivilianLocation = nearestGardener.location;
		       	}
		       	
		       	// If the robot hadn't moved the previous turn... this value may be null
		       	if (lastDirection == null){
		       		
		       		// Set the direction to go to as away from the last known nearest civilian
		       		lastDirection = nearestCivilianLocation.directionTo(myLocation);
		       	}		       	
		      
		       	// SYSTEM CHECK - Show where the lumberjack believes its nearest civilian is using a WHITE LINE
		       	rc.setIndicatorLine(myLocation, nearestCivilianLocation, 255, 255, 255);
		       	
		       	// ------------------------ BROADCAST UPDATES ------------------------ //
		    	
		       	// Update the nearest enemy and archon locations - If it sees any.......
            	BroadcastChannels.broadcastNearestEnemyLocation(enemyRobots, myLocation, unitNumber, nearestCivilianLocation, roundNumber);            	
            	BroadcastChannels.broadcastEnemyArchonLocations(enemyRobots);  
            	
            	// If the robot thought it died previously but didn't.... update information...
            	if(believeHasDied){
            		fixAccidentalDeathNotification();
            	}
            	
            	// ------------------------ MOVEMENT FUNCTIONS------------------------ //
            	
		       	MapLocation desiredMove = move(enemyRobots, nearbyTrees);
		       	
		       	if(desiredMove != null){
			       	// SYSTEM CHECK - Print out where the desired move is.....
			       	System.out.println("Currently attempting to move to location: " + desiredMove.toString());
		       	}
		       	
		       	// ------------------------ MOVEMENT CORRECTION ---------------------- //
		       	
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
		       		// SYSTEM CHECK - Print out that the lumberjack never had a place to go to...
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
		       	
		    	// ------------------------ UNIT/TREE INTERACTION ---------------------- //		       	
		       	
		       	// If the robot was tracking something
		       	if(trackID != -1 && trackedRobot != null){
		       		// Check to see if the robot can strike the target....
		       		strikeTarget(trackedRobot, trackID, alliedRobots);		       		
		       	}
		       	// Otherwise make sure both variables are correctly reset......
		       	else{
		       		trackID = -1; trackedRobot = null;		       		
		       	}
		       	
		       	// If the robot was attempting to go to a tree....
		       	if(treeID != -1 && treeToHarvest!= null && rc.canSenseTree(treeID)){
		       		
		       		// Check to see if can interact with the tree
		       		 boolean hasInteracted = interactWithTree(treeToHarvest, treeID);
		       		 
		       		 // If the tree was not interacted with this turn....
		       		 if(!hasInteracted){
		       			 treeID = -1;
		       			 treeToHarvest = null;
		       		 }
		       	}
		       	// Otherwise make sure both variables are correctly reset.......
		       	else{
		       		treeID = -1; treeToHarvest = null;
		       	}
		       	
		       	// ------------------------ ROUND END VARIABLE UPDATES ---------------------- //	
		       	
		       	// Update the last location of the lumberjack and point the last direction it moved....
		       	lastLocation = rc.getLocation();
		       	lastDirection = myLocation.directionTo(lastLocation);
		       	
				// Assert that the variables are correctly set to their non tracking state
				trackID = -1;	trackedRobot = null;	isTracking = false;			
		       	
	            // SYSTEM CHECK  Make sure the robot finishes its turn
                System.out.println("Turn completed!");
                
                // If the robot is not currently in a commanded state, increment
                if (!isCommanded){
                	lastCommanded += 1;
                }
                // The robot was in routing phase, so increment that counter
                else{
                	roundsRouting += 1;
                }   
		       	
                // Yield until the next turn.......
            	Clock.yield();
            	
			}
			// If it runs into an error....
			catch (Exception exception){
				
            	System.out.println("Lumberjack Exception");
            	exception.printStackTrace();
			}
		}
	}
	
	// ----------------------------------------------------------------------------------//
	// ------------------------------- MOVEMENT FUNCTIONS -------------------------------//
	// ----------------------------------------------------------------------------------//	
	
	// Encompassing function for controlling where the robot wishes to go to....
		// Initially checks for tracking - if the robot is currently tracking something....
			// The function attempts to find a new robot to track, or if nothing is there, continues harvesting.....
			// In general, tracking takes precedence over harvesting...... 
	
	 private static MapLocation move(RobotInfo[] enemyRobots, TreeInfo[] nearbyTrees) throws GameActionException{
		 
		// Check if the robot is currently not tracking anything	
		if(trackID == -1 || trackedRobot == null){ 
			
			// Assert that the variables are correctly set to their non tracking state
			trackID = -1;	trackedRobot = null;	isTracking = false;			
			
			// SYSTEM CHECK - see if the robot recognizes that it is currently not tracking anything
			System.out.println("The lumberjack is currently not tracking anything....");
			
			// Check to see there is a new robot for which the lumberjack to attack.....
			if (roundNumber <= 400){
				// Allow the robot to target scouts and civilians for the first four hundred turns but not afterwards....
				trackedRobot = Todoruno.getNewEnemyToTrack(enemyRobots, myLocation, true, true, true, false);
			}
			else{
				trackedRobot = Todoruno.getNewEnemyToTrack(enemyRobots, myLocation, true, false, true, false);
			}

			// If the above check found a robot for the lumberjack to track
			if (trackedRobot != null){
				
				// Annul any previous command for the robot...
        		isCommanded = false;	goalLocation = null;
				
				// Update the relevant information, force the trees to be gone to to be nullified so that the robot no longer remembers the tree if it returns to harvesting
				trackID = trackedRobot.ID;	isTracking = true;	treeID = -1;	treeToHarvest = null;			
				
				// SYSTEM CHECK - Notify that the lumberjack has found a new unit to track and print out its ID
				System.out.println("The lumberjack will not attempt to track the enemy Robot with ID: " + trackID);
			
				// Since the lumberjack has called a new robot to track, call the move function with the updated track ID so that the robot moves past first clause
				hasCalledMove = true;
				
				return move(enemyRobots, nearbyTrees);    	
			
			// Otherwise, either attempt to find a tree to track or continue moving in the previous direction that the robot was on....
			} else{
				
				// SYSTEM CHECK - Print out that no robot to track was found....
				System.out.println("No target enemy found, attempting to harvest");
				
									
            	// Attempt to move away from the initial ally directly after spawn.... # TODO Update the initial direction.....
            	if (roundNumber <= initRound + initialDispersionRounds){
            		
            		// System Check - Print out that the lumberjack is attempting to move away from its starting location
            		System.out.println("Attempting to move away from the start location");
            		
              		// If the point to move to is out of bounds utilize correctional tools...
            		if(!rc.onTheMap(myLocation.add(lastDirection, bodyRadius + strideRadius))){

	            		// SYSTEM CHECK - Print out that the lumberjack is trying to move to is out of bounds....
	            		System.out.println("Previous direction is out of bounds");	
	            		
            			return myLocation.add(lastDirection, strideRadius / 5);		            			
            		}
            		
            		// Simply add a stride radius away from the initial location if possible.....
            		for (int i = 5; i >= 1; i--){
            			
            			// Get the distance to move away for..... and the resulting map location
            			float testDistance = strideRadius / 5 * i;	            			
            			MapLocation testLocation = myLocation.add(lastDirection, testDistance);
            			
            			// Check if the robot can move to the location and if it can, do so.....
            			if (rc.canMove(testLocation)){	            				
            				return testLocation;	            			
            			}	            			
            		}
            		
            		// If a move in the last direction was not possible, simply order the robot to remain still...		            		
            		
        			// SYSTEM CHECK - Print out that the robot cannot move in its previous direction and will remain still...
        			System.out.println("Cannot seem to move in the last direction traveled and no other commands issued.. Unit will not move");
        			
        			// Return the current location of the robot.......
        			return myLocation;		    
            		
            	}
            	// Since the lumberjack should have successfully separated from the robot that made it... attempt to find a new tree to find...
            	else{	            		
            
	        		// SYSTEM CHECK - Print out that the tree will attempt to find a tree to go harvest
					System.out.println("Currently have no target tree... Will attempt to search for a tree to target");						
				
					
					if(rc.canSenseTree(treeID)){
						
						treeToHarvest = rc.senseTree(treeID);
						
						// If the lumberjack can still harvest that tree... do so...
						return harvest(treeToHarvest, nearbyTrees);
						
					}
					else{
						// Search for a tree to harvest
		            	TreeInfo nearestNeutralTree = getNextTreeToHarvest(nearbyTrees, myLocation); 
		 
		            	// If a viable tree was found
		            	if (nearestNeutralTree != null){
		            		
		            		// Annul any previous command for the robot...
		            		isCommanded = false;	goalLocation = null;
		            		
		            		// SYSTEM CHECK - Print out that the lumberjack has found a tree to cut.....
		            		System.out.println("Found a new tree to harvest with ID: " + nearestNeutralTree.ID);
		            		
		            		// Set the harvest tree variables accordingly....
		            		treeToHarvest = nearestNeutralTree;
		            		treeID = nearestNeutralTree.ID;
		            		
		            		// Call the harvest function.....
		            		return harvest(nearestNeutralTree, nearbyTrees);
		            	}
		            	
		            	// If the robot was previously attempting to move to a location.....
		            	else if(isCommanded){		            		

	            			// Tell the robot to go towards the commanded location....		            			
	            			return moveTowardsGoalLocation(enemyRobots, nearbyTrees);
		            	}
		            	else{
		            		
		            		// SYSTEM CHECK - Print out that no tree was found....
		            		System.out.println("No tree was found in the search");		
		            		
		            		// If the point to move to is out of bounds utilize correctional tools...
		            		if(!rc.onTheMap(myLocation.add(lastDirection, bodyRadius + strideRadius))){
		            		
			            		// SYSTEM CHECK - Print out that the lumberjack is trying to move to is out of bounds....
			            		System.out.println("Previous direction is out of bounds");	
			            		
		            			return myLocation.add(lastDirection, strideRadius / 5);		            			
		            		}
		            		
		            		// Attempt to move in the last direction traveled....
		            		for (int i = 5; i >= 1; i--){
		            			
		            			// Get the distance to move away for..... and the resulting map location
		            			float testDistance = strideRadius / 5 * i;	            			
		            			MapLocation testLocation = myLocation.add(lastDirection, testDistance);
		            			
		            			// Check if the robot can move to the location and if it can, do so.....
		            			if (rc.canMove(testLocation)){	 
		            				
		            				// SYSTEM CHECK - Show a LIGHT GRAY LINE to the location that the lumberjack now wishes to go to....
		            				// rc.setIndicatorLine(myLocation, testLocation, 110, 110, 110);    
		            				return testLocation;	            	
		            			}		            			
		            			// SYSTEM CHECK Place a dot on all rejected points...
		            			// rc.setIndicatorDot(testLocation, 255, 211, 25);
		            		}
		            		// If a move in the last direction was not possible, simply order the robot to remain still...	
		            		setCommandLocation(null);
		            		
		            		// If there was a valid point to go to...
		            		if(isCommanded){    			

		            			// Tell the robot to go towards the commanded location....		            			
		            			return moveTowardsGoalLocation(enemyRobots, nearbyTrees);
		            		}
		            		else{
		            			return myLocation;	            		
		            		}
		            	}           				
		            }
	            }	        		
			}         

		// If the robot is actually currently tracking something
		} else{
			
			// If the robot can no longer sense the robot being tracked... i.e. it has been killed or moved out of range
			if (!rc.canSenseRobot(trackID)){
				
				// SYSTEM CHECK - Print out that the robot has lost sight of the currently tracked enemy
				System.out.println("The previously tracked enemy can no longer be seen, will attempt to do something else....");
				
				// Reset tracking variables
				trackID = -1;
				trackedRobot = null;
				
				// Call the move function again with the nullified track variables...
				if(!hasCalledMove){
					return move(enemyRobots, nearbyTrees);
				}
				
				// Something went wrong.....
				else{					
					// If the point to move to is out of bounds utilize correctional tools...
            		if(!rc.onTheMap(myLocation.add(lastDirection, bodyRadius + strideRadius))){

	            		// SYSTEM CHECK - Print out that the lumberjack is trying to move to is out of bounds....
	            		System.out.println("Previous direction is out of bounds");	
	            		
            			return myLocation.add(lastDirection, strideRadius / 5);		            			
            		}
            		
            		// Attempt to move in the last direction traveled....
            		for (int i = 5; i >= 1; i--){
            			
            			// Get the distance to move away for..... and the resulting map location
            			float testDistance = strideRadius / 5 * i;	            			
            			MapLocation testLocation = myLocation.add(lastDirection, testDistance);
            			
            			// Check if the robot can move to the location and if it can, do so.....
            			if (rc.canMove(testLocation)){	 
            				
            				// SYSTEM CHECK - Show a LIGHT GRAY LINE to the location that the lumberjack now wishes to go to....
            				// rc.setIndicatorLine(myLocation, testLocation, 110, 110, 110);    
            				return testLocation;	            	
            			}		            			
            			// SYSTEM CHECK Place a dot on all rejected points...
            			rc.setIndicatorDot(testLocation, 255, 211, 25);
            		}
            		// If a move in the last direction was not possible, simply order the robot to remain still...		            		
        		
        			// SYSTEM CHECK - Print out that the robot cannot move in its previous direction and will remain still...
        			// System.out.println("Cannot seem to move in the last direction traveled and no other commands issued.. Unit will not move");
        			
        			// Return the current location of the robot.......
        			return myLocation;		      
				}
			}
			
			// If the robot was tracking something and can actually still sense it....
			else{
				// SYSTEM CHECK - Print out that the robot is currently tracking an enemy...
				System.out.println("Currently tracking a robot with ID: " + trackID);
				
				// Update the stored information regarding the robot to be tracked....
				trackedRobot = rc.senseRobot(trackID);
				
				// Call the function to move towards the target....# TODO
				return followTrackedTarget();
				}
			}	
		}
	 
		
	    // Function to use when moving towards a certain location with a certain target.....
	    
	    private static MapLocation moveTowardsGoalLocation(RobotInfo[] enemyRobots, TreeInfo[] nearbyTrees) throws GameActionException{
	    	
	    	// If the robot has gotten close enough to the goal location, exit the command phase and do something else
	    	if (myLocation.distanceTo(goalLocation) <= RobotType.LUMBERJACK.strideRadius + bodyRadius || roundsRouting >= giveUpOnRouting){
	    		
	    		// SYSTEM CHECK - Print out that the robot has gotten close to the desired location but did not find anything of note...
	    		System.out.println("Lumberjack has reached destination/ Failed to do so and given up.....");
	    		
	    		// Reset the rounds routing counter.....
	    		roundsRouting = 0;
	    		
	    		// Reset the values necessary for switching into a command phase
	    		goalLocation = null;
	    		isCommanded = false;
	    		
	    		// Call the move function again...
	    		return move(enemyRobots, nearbyTrees);
	    	}
	    	
	    	else{
	    		// SYSTEM CHECK - Inform that the robot is currently attempting to following a route to a goal destination.....    	
		    	System.out.println("Currently attempting to move to a goal location with x: " + goalLocation.x + " and y: " + goalLocation.y);
		    	
		    	// Otherwise, call the routing wrapper to get a new location to go to...
		    	Routing.routingWrapper();
		    	
		    	// Set the desired Move
		    	MapLocation desiredMove = Routing.path.get(0);
		    	
		    	// SYSTEM CHECK - Show desired move after path planning
		    	System.out.println("desiredMove from path planning: " + desiredMove.toString());
		    	
		    	return desiredMove;
	    	}
	    }    
	 
	 
	 // ----------------------------------------------------------------------------------//
	 // ------------------------------ TRACKING FUNCTIONS  -------------------------------//
	 // ----------------------------------------------------------------------------------//	
	 
	 
	 // Function on what to do if there is a robot to be tracked.... # EDIT
	 
	 private static MapLocation followTrackedTarget() throws GameActionException{
		 
		 // SYSTEM CHECK - Draw a PURPLE LINE from the robot to the location of the robot to track....
		 rc.setIndicatorLine(myLocation, trackedRobot.location, 128, 0, 128);
		 
		 // Variable to store the distance to the robot currently being tracked
		 float distanceToTarget = myLocation.distanceTo(trackedRobot.location);
		 
		 // Get the direction to the target enemy
		 Direction targetDirection = myLocation.directionTo(trackedRobot.location);
		 
		 // If the robot is near a gardener
		 if (myLocation.distanceTo(nearestCivilianLocation) <= distanceDefend && myLocation.distanceTo(nearestCivilianLocation) >= bodyRadius + 1){
			 
			 // SYSTEM CHECK - Print out that the lumberjack will allow itself to hit allies.....
			 System.out.println("The lumberjack senses that the enemy is near a friendly gardener... it will strike at all costs");
			 // Allow the robot to hit allies as well...
			 nearbyAllyCheckOverride = true;
		 }
		 
		 // Specifics on dealing with scouts		 
		 if(trackedRobot.type == battlecode.common.RobotType.SCOUT){			 
			 
			 TreeInfo checkTree = null;
			 
			 if(rc.canSenseLocation(trackedRobot.location)){
				 // If there is a scout in the given location.....
				 checkTree = rc.senseTreeAtLocation(trackedRobot.location);
			 }
			 // If the robot is in a tree......
			 if(checkTree != null){
				 
				 // SYSTEM CHECK - Print out that the lumberjack is following a scout in a tree..
				 System.out.println("Moving towards the scout... it is in a tree!");
				 
				 // Call the function to move towards the tree
				 return moveTowardsTree(checkTree);				 
			 }
			 else{
				 // SYSTEM CHECK - Print out that the robot is moving towards its target
				 System.out.println("Attempting to move to within strike range of the enemy scout ....");
				 				 
				 				 // If the scout is further away than stride radius, attempt to move towards it
				 if(distanceToTarget > strideRadius + bodyRadius + trackedRobot.getRadius()){
					 
					 // Return a location in the target direction exactly one stride radius away....
					 MapLocation targetLocation = myLocation.add(targetDirection, strideRadius);				
					 
					 // If the robot can simply move towards the target...
					 if (rc.canMove(targetLocation)){
						 return targetLocation;
					 }
					 else{
						 // SYSTEM CHECK - Print out that the robot will attempt to move in the general direction of the scout....
						 System.out.println("Could not move directly towards scout");
						 
						 return Yuurei.attemptRandomMove(myLocation, targetLocation, strideRadius);
					 }					 
				 }
				 else{
					 // Attempt to close the gap and get right next to the robot.....
					 return myLocation.add(targetDirection, (distanceToTarget - bodyRadius- trackedRobot.getRadius()));				 
				 }
			 }		 
		 }		
		 // As for other units...... treat them as scouts not within trees.....
		 else{
			 // SYSTEM CHECK - Print out that the robot is moving towards its target
			 System.out.println("Attempting to move to within strike range of the enemy....");
			 
			 // If the target is further away than stride radius, attempt to move towards it
			 if(distanceToTarget > strideRadius + bodyRadius + trackedRobot.getRadius()){
				 
				 // Return a location in the target direction exactly one stride radius away....
				 MapLocation targetLocation = myLocation.add(targetDirection, strideRadius);
				 
				 float distanceToCheck = (float) (bodyRadius + (0.5));
				 
				 // If there is a tree directly in front of the robot.....
				 TreeInfo checkTree = rc.senseTreeAtLocation(myLocation.add(targetDirection, distanceToCheck));
				 
				 // If there is a tree in the way...
				 if(checkTree != null){
					 
					 // If the tree isn't allied
					 if(checkTree.team != allies){
						 
						 // Set the lumberjack to harvest the offending tree
						 treeToHarvest = checkTree;
						 treeID = checkTree.ID;
						 
						 // SYSTEM CHECK - Print out that there is a tree in the way...
						 System.out.println("Tree in the way.... Will attempt to remove....");
						 
						 return moveTowardsTree(checkTree);					 
					 }
					 else{
						return targetLocation;						 
					 }
				 }
				 return targetLocation;
			 }
			 else{
				 // Attempt to close the gap and get right next to the robot.....
				 return myLocation.add(targetDirection, (distanceToTarget - bodyRadius- trackedRobot.getRadius()));				 
			 }			 
		 }
	 } 
	
	 // ----------------------------------------------------------------------------------//
	 // --------------------------- TREE HARVESTING FUNCTIONS ----------------------------//
	 // ----------------------------------------------------------------------------------//	
	
	 // Function to harvest the tree that the robot is currently tracking
	 	// Calls a function to harvest the currently targeted tree
	 	// Requires a valid tree in order to execute....
	 
	private static MapLocation harvest(TreeInfo nearestTree, TreeInfo[] nearbyTrees) throws GameActionException{
			
		// SYSTEM CHECK - Print out that the lumberjack has succesfully called the function to harvest tree
		System.out.println("The lumberjack is currently attempting to harvest a tree with ID: " + nearestTree.ID);
		
		// Call the function moveTowardsTree to determine the optimal way of getting to the tree in question....	
		MapLocation testLocation = moveTowardsTree(nearestTree);
		
		// Assert that the target location isn't null...... in that the robot has a clear path to go to the desired tree....
		if (testLocation != null){
			
			// SYSTEM CHECK - Draw a GREEN LINE to where the lumberjack wishes to go to in order to harvest the tree in
			rc.setIndicatorLine(myLocation, testLocation, 0, 255, 0);		
			
			return testLocation;
		}
		// TODO tell the lumberjack what to do when its not attempting to move......
		else{
			testLocation = Yuurei.tryMoveInDirection(Move.randomDirection(), strideRadius, myLocation);
			
			return testLocation;
		}
	}

	// Function to move to a desired tree to be harvested....... TODO
		// Takes as an input the tree to be harvested and the holding variable for the move to be made........
	
    private static MapLocation moveTowardsTree(TreeInfo tree){
    	
    	// Obtain the distance to the tree in question....
    	float distanceTo = myLocation.distanceTo(tree.location);
    	
    	// If the lumberjack is further away from the tree than one stride radius...
    	if (distanceTo > tree.getRadius() + bodyRadius + strideRadius){
    	
    		// Get the direction towards the tree....
    		Direction newDirection = new Direction(myLocation.directionTo(tree.location).radians);
    		
    		// Get the distance that the lumberjack needs to move in order to get within the interact distance of the tree..........
    		float remainingDistance = distanceTo - (tree.getRadius() + bodyRadius + interactDistance);
    		
    		// Return a movement towards the tree getting it towards the target tree equivalent to either one stride radius or the distance remaining....   		
    		if (remainingDistance >= strideRadius){    			
    			return myLocation.add(newDirection, strideRadius);
    		}
    		else{
    			return myLocation.add(newDirection, remainingDistance);
    		}
    		
    	// If the lumberjack is currently within one stride of the tree
    	} else if (distanceTo > tree.getRadius() + bodyRadius){
    		
    		// Search for points on the tree that the unit can interact with....
			Direction directionFrom = tree.location.directionTo(myLocation);
			
			// Iterate through small angles around the tree from the desired location, and attempt to move to the locations....
			for(int i = 0; i <= 12; i++){				
				// Generate the target direction
				Direction testDirection1 = new Direction(directionFrom.radians - (float)(i * Math.PI/12));
				// Generate the target location
				MapLocation testLocation1 = tree.location.add(testDirection1, tree.getRadius() + bodyRadius);
				// Check if the robot can move to it and return if it can....
				if (rc.canMove(testLocation1)){
					return testLocation1;
				}
				// Do the same for the other side
				Direction testDirection2 = new Direction(directionFrom.radians - (float)(i * Math.PI/12));
				
				MapLocation testLocation2 = tree.location.add(testDirection2, tree.getRadius() + bodyRadius);
				
				if (rc.canMove(testLocation2)){
					return testLocation2;
				}	
			}
			
			// SYSTEM CHECK - Print out that the robot is already close to the tree...
	    	System.out.println("Close to tree and cannot find a new location to move to.... will not move..."); 
			
			return myLocation;
    	} 
    	else{    	
    	// SYSTEM CHECK - Print out that the robot is already close to the tree...
    	System.out.println("Already next to tree.. will not move...");    		
    		
    	// If no valid location has been found return nothing....
    	return myLocation;  
    	}
    }	
	 
	 // Function to obtain the next tree for the lumberjack to harvest... 
		// Sweeps through the nearby area and searches for the nearest nearby non allied tree for which there is a clear path to get to..
	
	private static TreeInfo getNextTreeToHarvest(TreeInfo[] nearbyTrees, MapLocation myLocation) throws GameActionException{
    	
    	// Initialize an array to store the distances to the nearby trees....
    	float[] distancesToTrees = new float[treeSearchAngleNumber];
    	Arrays.fill(distancesToTrees, -1);
    	
    	// Random number to choose in which direction from lastDirection the lubmerjack begins its search.....
    	float randomNumber = (float) Math.random();
    	
    	// Angle difference of each sweep...
    	float searchAngle = (float) Math.PI / (treeSearchAngleNumber/2);
    	
    	// Iterate through all the different angles
    	for(int i = 0; i < treeSearchAngleNumber; i++){
    		
    		// Placeholding direction
    		Direction directionToSearch;
    		
    		// Enforce the random number constraint
    		if (randomNumber >= 0.5){
    			directionToSearch = new Direction ((float)(lastDirection.radians + i * searchAngle));
    		}
    		else{
    			directionToSearch = new Direction ((float)(lastDirection.radians - i * searchAngle));
    		}    
    		
    		// Iterate through each of the distances, incrementing by one unit each time
    		for (float j = (float) (bodyRadius + 0.5); j <= maxTreeSearchRange; j += 1){   
    			
    			// Placeholder for the location to check
    			MapLocation locationToCheck = myLocation.add(directionToSearch, j);
    			
    			// If the currently considered location has a tree and the curernt angle has not yet seen a tree
    			if(distancesToTrees[i] < 0 && rc.isLocationOccupiedByTree(locationToCheck)){
    				
    				// Get information regarding the tree that occupies the target location
    				TreeInfo treeX = rc.senseTreeAtLocation(locationToCheck);
    				
    				// Make sure that the tree is not a friendly tree
    				if (treeX.team != allies){    					
	    				// Add the tree to the list of tree distances
	    				distancesToTrees[i] = treeX.location.distanceTo(myLocation);    
	    				// SYSTEM CHECK - Place a YELLOW DOT on any potential trees sensed
	    				rc.setIndicatorDot(locationToCheck, 255, 255, 0);
    				}
    				// Otherwise the trees were allied
    				else{
    					// Makes sure that the sweep doesn't update this index, as it is no longer valid
    					distancesToTrees[i] = 0;
    					
    					// SYSTEM CHECK - Place a MAGENTA on any nearby allied trees sensed
    					rc.setIndicatorDot(locationToCheck, 255, 0, 255);
    				}
    			}
    			// Otherwise if there is an ally in the way....
    			else if(rc.isLocationOccupiedByRobot(locationToCheck) && distancesToTrees[i] < 0){
    				
    				if(rc.senseRobotAtLocation(locationToCheck).getID() != rc.getID()){
	    				// Makes sure that the sweep doesn't update this index, as it is no longer valid
						distancesToTrees[i] = 0;
						
						// SYSTEM CHECK - Place a GREEN on any nearby allied trees sensed
						rc.setIndicatorDot(locationToCheck, 0, 255, 0);
    				}
    			}
    			// Otherwise no trees were discovered at that location/the angle had already been invalidated
    			else{
    				// SYSTEM CHECK - Place a RED DOT on any invalid location
    				rc.setIndicatorDot(locationToCheck, 255, 0, 0);
    			}    			
    		}    		
    	}
    	// Placeholders to later retrieve the data for the tree....
    	float minimum = Integer.MAX_VALUE;
    	int directionIndex = -1;
    	
    	// Iterate through the array and pick the minimal positive value.....
    	for (int i = 0; i < 20; i++){
    		
    		if(distancesToTrees[i] < minimum && distancesToTrees[i] > 0){
    			// Update the placeholderse
    			directionIndex = i;
    			minimum = distancesToTrees[i];
    		}    		
    	}
    	
    	// If the tree sensed was outside of range because it was too large...
    	if (minimum >= sensorRadius){
    		
    		// Set the distance to scan at be equivalent to the sensor range of the lumberjack.....
    		minimum = sensorRadius;
    	}
    	
    	// Assert that a valid tree was found....
    	if (directionIndex >= 0){
    		// Obtain again the direction that corresponds to the directionIndex value...
    	   	Direction directionToRead;
    	   	
    		if (randomNumber >= 0.5){
				directionToRead = new Direction ((float)(lastDirection.radians + directionIndex * searchAngle));
			}
			else{
				directionToRead = new Direction ((float)(lastDirection.radians - directionIndex * searchAngle));
			}
    		// Location of the ideal tree...
    		MapLocation idealTreeLocation = myLocation.add(directionToRead, minimum);
    		
    		// SYSTEM CHECK - Place a WHITE DOT on the tree that the lumberjack eventually decides to chop down....
    		rc.setIndicatorDot(idealTreeLocation, 255, 255, 255);
    		
    		// Obtain again the tree data corresponding to the tree at the ideal location and return the value
    		TreeInfo tree = rc.senseTreeAtLocation(idealTreeLocation);
    		return tree;
    	}
    	// Otherwise if no valid tree has been found, return null....
    	return null;    	
    }
	
	// Function to determine how the robot interacts with the tree....
	
	private static boolean interactWithTree(TreeInfo targetTree, int targetTreeID) throws GameActionException{
		
		// SYSTEM CHECK - Print out that the lumberjack was attempting to interact with a tree....
		System.out.println("Attempting to interact with a tree with ID: " + targetTreeID);
		
		// Double check to make sure that the robot can interact with the tree....
		if(rc.canInteractWithTree(targetTreeID)){
			
			// SYSTEM CHECK - Print out that the lumberjack can actually interact with the tree...
			System.out.println("Can interact with target tree!!");
			
			// SYSTEM CHECK - Indicate which tree was just interacted with a MAROON DOT.
			rc.setIndicatorDot(targetTree.location, 128, 0, 0);
			
			// Check to see if the tree contains bullets....
			if (targetTree.getContainedBullets() > 0){
				
				// Shake the tree to get the bullets
				rc.shake(targetTreeID);
				
			}
			// Otherwise chop it!
			else{
				rc.chop(targetTreeID);						
			}
			return true;
		}
		// If the unit did not interact with a tree.. return false..
		else{
			return false;
		}

	}
	
	// ----------------------------------------------------------------------------------//
	// --------------------------- MISCELLANEOUS FUNCTIONS ------------------------------//
	// ----------------------------------------------------------------------------------//	
		
	// Function to control striking behavior.......
	private static void strikeTarget(RobotInfo enemyRobot, int enemyRobotID, RobotInfo[] nearbyAllies) throws GameActionException{
		
		// Assert that the enemy isn't null....
		if(enemyRobotID > 0 && enemyRobot != null){
			
			// Check if the enemy Robot is within strike distance
			if(myLocation.distanceTo(enemyRobot.location) <= bodyRadius + 1 + enemyRobot.getRadius()){
				
				// If the robot doesn't have to check for ally proximity
				if(nearbyAllyCheckOverride){
					
					// SYSTEM CHECK - Print out that the lumberjack will strike the enemy 
					System.out.println("Striking the enemy with ID: " + enemyRobotID);
					
					// SYSTEM CHECK - Place a dot on the enemy robot being attacked.....
					rc.setIndicatorDot(enemyRobot.location, 0, 0, 128);
					
					rc.strike();					
				}
				else{
					RobotInfo nearestAlly = getNearestRobot(nearbyAllies);
					
					// If there was an ally nearby...
					if(nearestAlly != null){
						if(myLocation.distanceTo(nearestAlly.location) > bodyRadius + 1 + nearestAlly.getRadius()){
							
							// SYSTEM CHECK - Print out that the lumberjack will strike the enemy 
							System.out.println("Striking the enemy with ID: " + enemyRobotID);
							
							// SYSTEM CHECK - Place a dot on the enemy robot being attacked.....
							rc.setIndicatorDot(enemyRobot.location, 0, 0, 128);
							
							rc.strike();						
						}
						// Otherwise the ally was too close and the lumberjack will not attempt to strike....
						else{
							// SYSTEM CHECK - Print out that the lumberjack did not strike because its ally was too close
							System.out.println("Did not strike... Ally with ID: " + nearestAlly.ID + " was in the way.......");
						}
					}
					// Just strike if there are no allies nearby!
					else{
						
						// SYSTEM CHECK - Print out that the lumberjack will strike the enemy 
						System.out.println("Striking the enemy with ID: " + enemyRobotID);
						
						// SYSTEM CHECK - Place a dot on the enemy robot being attacked.....
						rc.setIndicatorDot(enemyRobot.location, 0, 0, 128);
						
						rc.strike();						
					}
				}				
			}			
		}	
	}
	
	// Simple function to obtain data about units of a certain team within a certain distance
	
	private static RobotInfo[] NearbyUnits(Team team, float distance){	
		
		return rc.senseNearbyRobots(myLocation, distance, team);
	}	
	
	// Simple function to return the information regarding the nearest gardener to the lumberjack at present....
	
	private static RobotInfo getNearestGardener(RobotInfo[] nearbyRobots){
		
		// Variable to store the data to be returned...
		RobotInfo returnRobot = null;
		
		// Variable to store the minimum distance thus far....		
		float distance = Integer.MAX_VALUE;
		
		// Iterate through the list
		for(RobotInfo nearbyRobot: nearbyRobots){
			
			// If the robot is a gardener...
			if (nearbyRobot.type == RobotType.GARDENER){
				
				// If the robot is the closest gardener thus far update the placeholders
				if (myLocation.distanceTo(nearbyRobot.location) <= distance){
					
					returnRobot = nearbyRobot;
					distance = myLocation.distanceTo(nearbyRobot.location);
				}
			}			
		}	
		
		// After the correct information has been ascertained, return it.....
		return returnRobot;
	}
	
	// Simple function to return the information of a nearest robot of a given list
	
	private static RobotInfo getNearestRobot(RobotInfo[] nearbyRobots){
			
		// Variable to store the data to be returned...
		RobotInfo returnRobot = null;
		
		// Variable to store the minimum distance thus far....		
		float distance = Integer.MAX_VALUE;
		
		// Iterate through the list
		for(RobotInfo nearbyRobot: nearbyRobots){
			
			// If the robot is the closest gardener thus far update the placeholders
			if (myLocation.distanceTo(nearbyRobot.location) <= distance){
				
				returnRobot = nearbyRobot;
				distance = myLocation.distanceTo(nearbyRobot.location);
			
			}			
		}	
		
		// After the correct information has been ascertained, return it.....
		return returnRobot;
	}
	
	// Function to check if the robot thinks it will die this turn and broadcast if it will.............
	
    public static void checkDeath(MapLocation location) throws GameActionException{
    	
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
		        int currentLumberjackNumber = rc.readBroadcast(BroadcastChannels.LUMBERJACKS_ALIVE_CHANNEL);
		        
		        // Update lumberjack number for other units to see.....
		        rc.broadcast(BroadcastChannels.LUMBERJACKS_ALIVE_CHANNEL, currentLumberjackNumber - 1);

			}
		}
	}
    
    // Function to correct an accidental death update
    
    public static void fixAccidentalDeathNotification() throws GameActionException{
    	
    	// Reset belief in the robot dying this round....
    	believeHasDied = false;    	

		// Get the current number of lumberjacks in service
        int currentLumberjackNumber = rc.readBroadcast(BroadcastChannels.LUMBERJACKS_ALIVE_CHANNEL);
        
        // Update lumberjack number for other units to see.....
        rc.broadcast(BroadcastChannels.LUMBERJACKS_ALIVE_CHANNEL, currentLumberjackNumber + 1);
    	
    }   
    
	// Function to set a command location to the location of the nearest enemy......
	
	private static void setCommandLocation(MapLocation location) throws GameActionException{	
	
		// Parameters for a successful command initiation............
		// 1. Make sure that the turn number isn't equivalent to the clearing time of the broadcast....
		// 2. Make sure that the robot is not currently being commanded......
		// 3. Make sure that the robot has not been commanded for the last attackFrequency number of turns
		// 4. Make sure that the robot is not yet tracking anything.......
		
    	if(location == null){
		
			if (roundNumber % BroadcastChannels.BROADCAST_CLEARING_PERIOD != 1  && lastCommanded >= attackFrequency){   		
	
	       		// Attempt to read enemy archon data
	           	BroadcastChannels.BroadcastInfo newInfo = null;
	
	           	newInfo = BroadcastChannels.readEnemyLocations();       
	           	
	           	// Pseudo random number for joining the attack....
	           	float willJoin = (float) Math.random();
	           	
	           	// If an archon has been seen before and the robot's pseudo random number falls within bounds to join an  attack, create the goal location
	           	// Make sure that the goal location is sufficiently far away - i.e. don't go if it is within sensor Radius....
	           	if (willJoin <= attackProbability && newInfo != null){ 
	           			
	           		// Obtain the location from the broadcast data
	           		MapLocation targetLocation = new MapLocation(newInfo.xPosition, newInfo.yPosition);    
	           		
	           		// Make sure that the robot is somewhat far away....
	           		if(myLocation.distanceTo(targetLocation) >= strikeRadius + interactRadius){
	           			
	            		// SYSTEM CHECKC - Print out that a command has been succesfully given...
	            		System.out.println("Command succesfully ordered to attack the location: " + targetLocation.toString());
	           		
		           		// The robot now has a command to follow, so will no longer track enemies continuously.....
		           		isCommanded = true;
		           		
		           		// Set the location of the target to go to as the data from the broadcast
		           		goalLocation = targetLocation;           	
		           		
		           		// Append the location to the routing...
		           		Routing.setRouting(goalLocation);           		
		               	
		           		// Reset the lastCommanded since the unit has now received a command
		           		lastCommanded = 0;
		           	}
	           	}
	    	}
    	}
    	else{
    		
    		// SYSTEM CHECKC - Print out that a command has been succesfully given...
    		System.out.println("Command succesfully ordered to attack the location: " + location.toString());
    		
      		// The robot now has a command to follow, so will no longer track enemies continuously.....
       		isCommanded = true;
       		
       		// Set the location of the target to go to as the data from the broadcast
       		goalLocation = location;           	
       		
       		// Append the location to the routing...
       		Routing.setRouting(goalLocation);           		
           	
       		// Reset the lastCommanded since the unit has now received a command
       		lastCommanded = 0;
   		
    	}
	}
}
