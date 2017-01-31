// AI for soldier under normal control
package naclbot.units.AI.soldier;
import battlecode.common.*;

import naclbot.variables.BroadcastChannels;
import naclbot.variables.GlobalVars;
import naclbot.units.interact.iFeed;
import naclbot.units.motion.Chirasou;
import naclbot.units.motion.Todoruno;
import naclbot.units.motion.Yuurei;
import naclbot.units.motion.shoot.Korosenai;
import naclbot.units.motion.routing.Routing;
import naclbot.units.motion.search.EnemyArchonSearch;



/* --------------------------   Overview  --------------------------
 * 
 * 			AI Controlling the functions of the Soldier
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
 * 1. Change tracking - reset every turn.....
 * 
 * 
 ------------------------------------------------------------------- */


public class SaberBot extends GlobalVars {
	
	// ----------------------------------------------------------------------------------//
	// ------------------------- VARIABLES FOR USE BY THE ROBOT -------------------------//
	// ----------------------------------------------------------------------------------//	
	
	// ------------- GAME VARIABLES -------------//
	
	// Variable to store the round number
	private static int myWaifuIsOnodera;
	
	// Variables to store the teams currently in the game
	public static Team enemies;
	public static Team allies;
	
	// Gamne-defined robot class related parameters
	private static float strideRadius = battlecode.common.RobotType.SOLDIER.strideRadius;
	private static float bodyRadius = battlecode.common.RobotType.SOLDIER.bodyRadius;
	private static float sensorRadius = battlecode.common.RobotType.SOLDIER.sensorRadius;
	
	// ------------- PERSONAL VARIABLES -------------//
	
	// Self-identifiers...
	public static int myID; // Game-designated ID of the robot
	public static int unitNumber; // Team-generated unit number - represents order in which units were built
	public static int soldierNumber; // Team generated number - represents order in which soldiers were built
	
	private static int initRound; // The initial round in which the robot was constructed

	
	// Personal movement variables
	private static MapLocation myLocation; // The current location of the soldier...
	private static MapLocation lastPosition; // The previous location that the soldier was at...
	private static Direction lastDirection; // The direction in which the soldier last traveled
	private static boolean rotationDirection = true; // Boolean for rotation direction - true for counterclockwise, false for clockwise
	
	// ------------- OPERATION VARIABLES -------------//
	
	// Variables related to tracking....
	private static int normieID; // The robot that the soldier is currently tracking....
	private static RobotInfo normieEmiliaLover; // The Robot that the soldier is currently tracking....
	private static boolean foundNormie; // Boolean to show whether or not the soldier is currently tracking something or not...

	// Path-planning variables
	private static boolean isCommanded; // Boolean to store whether or not the soldier current has orders to go somewhere....
    public static MapLocation goalLocation; // End location of the path planning
    public static int roundsRouting = 0; // FVariable to store the length of time the robot has been in path planning mode....
    
    
    // Routing constants
    public static final int attackFrequency = 0; // Asserts how often robots will attempt to go on the attack after completing a prior attack....
    public static final float attackProbability = (float) 1; // Gives probability of joining an attack at a particular time....
    private static int lastCommanded = attackFrequency; // Int to store the number of rounds since the unit was last in a commanded mode - threshold value
    public static final int giveUpOnRouting = 100; // Variable to determine after how long soldiers decide that Alan's code is a piece of shit......
    public static final int giveUpDefending = 100; // Variable to determine when the soldiers will stop attempting to defend a certain location.......]
    private static int roundsDefending;
    
    
    
    // Enemy data variables....
	private static RobotInfo[] previousRobotData; // Array to store the data of enemy robots from the previous turn.....

    // Variables related to gardener defense.....
    private static MapLocation defendLocation; // Location that the scout must defend...
    private static int defendAgainstID; // Enemy to search for once the scout has reached that location	
	
	// ------------- ADDITIONAL VARIABLES/CONSTANTS -------------//

	// Variables related to operational behavior...
	private static MapLocation nearestCivilianLocation; // Stores for multiple rounds the location of the nearest civilian robot....	
	private static final float separationDistance = sensorRadius - 1; // stores how large of a distance soldiers will attempt to keep from nearby units when they engage them...
	private static MapLocation archonLocation; // Stores the location of the archon that the soldier is by default sent to attack....
	
    // Miscellaneous variables.....
 	private static boolean believeHasDied; // Stores whether or not the robot believes it will die this turn or not.........
 	private static boolean checkArchons = false; // Stores whether or not the robot will attempt to look for archons or not....
 	
  
 	
	// ----------------------------------------------------------------------------------//
	// -------------------------------- RUNTIME FUNCTIONS -------------------------------//
	// ----------------------------------------------------------------------------------//	
 	
    
	// Initialization function - makes the default values for most important parameters
    
    public static void init() throws GameActionException{
    	
        // Important parameters for self
        enemies = rc.getTeam().opponent();
        allies = rc.getTeam();
        myID = rc.getID();      
        
        myWaifuIsOnodera = rc.getRoundNum();
        initRound = myWaifuIsOnodera;

        // Get own scoutNumber  and unitNumber- important for broadcasting 
        soldierNumber = rc.readBroadcast(BroadcastChannels.SOLDIER_NUMBER_CHANNEL);        
        unitNumber = rc.readBroadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL);      
          
        // Get the current round number......
        myWaifuIsOnodera = rc.getRoundNum();
        initRound = myWaifuIsOnodera;
 
        // Initialize variables important to self
        myLocation = rc.getLocation();
        normieID = -1;
        foundNormie = false;
        normieEmiliaLover = null;
        previousRobotData = null;
        roundsDefending = 0;
    	
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
       	
       	// Set the soldier to first attempt to move away from the nearest civilian initially....
       	lastDirection = nearestCivilianLocation.directionTo(myLocation);
        
        // Initialize variables relating to defending....
        defendLocation = null;
        defendAgainstID = -1;     
        
       	// Retrieve the number of active soldiers and increment......
       	int numberOfActiveSoldiers = rc.readBroadcast(BroadcastChannels.SOLDIERS_ALIVE_CHANNEL);
       	rc.broadcast(BroadcastChannels.SOLDIERS_ALIVE_CHANNEL, numberOfActiveSoldiers + 1);    
        
        // Update the number of soldiers so other soldiers can know....
        rc.broadcast(BroadcastChannels.SOLDIER_NUMBER_CHANNEL, soldierNumber + 1);
        rc.broadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL, unitNumber + 1);
        
		// Retrieve the correct corresponding archon...
		archonLocation = EnemyArchonSearch.getCorrespondingArchon();
		
		// SYSTEM CHECK - Draw a line to the target enemy archon location...
		rc.setIndicatorLine(myLocation, archonLocation, 255, 0, 0);
        
        main();
    }
    
    // Main function of the soldier, contains all turn by turn actions....
    
    public static void main() throws GameActionException{
    	
    	// Actions to be completed every turn by the soldier.....,
    	while(true){
    		
    		try{    	
    		    // SYSTEM CHECK  Make sure the robot starts its turn
                System.out.println("Beginning Turn!");    
    			
    			// ------------------------- RESET/UPDATE VARIABLES ----------------//        
    			
            	// Get nearby enemies and allies and bullets for use in other functions            	
            	RobotInfo[] enemyRobots = NearbyUnits(enemies, sensorRadius);
            	RobotInfo[] alliedRobots = NearbyUnits(allies, sensorRadius);
            	BulletInfo[] nearbyBullets = rc.senseNearbyBullets();
    			
            	// Update global variables.....
            	myWaifuIsOnodera = rc.getRoundNum();
		    	
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
		       		
		       		// SYSTEM CHECK - Print out that the last turn, lastDirection was null....
		       		System.out.println("Last direction was previously null....");
		       		
		       		// Set the direction to go to as away from the last known nearest civilian
		       		lastDirection = nearestCivilianLocation.directionTo(myLocation);
		       	}	
            	
		       	
		       	// SYSTEM CHECK - Show where the soldier believes its nearest civilian is using a WHITE LINE
		       	// rc.setIndicatorLine(myLocation, nearestCivilianLocation, 255, 255, 255);		    
            	
            	
            	// ------------ ACTIONS TO BE COMPLETED -------------//
            	
               	
            	// Update the nearest enemy and archon locations
               	BroadcastChannels.broadcastEnemyArchonLocations(enemyRobots);     
            	BroadcastChannels.broadcastNearestEnemyLocation(enemyRobots, myLocation, unitNumber, nearestCivilianLocation, myWaifuIsOnodera);
               	
              	// Update the distress info and retreat to a scout if necessary            	
            	BroadcastChannels.BroadcastInfo distressInfo = BroadcastChannels.readDistress(myLocation, 30);
            	
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
            	
            	// --------------------------- DODGING ------------------------ //

            	// Call the dodge function
		       	MapLocation dodgeLocation = null;
		       	
		       	if (normieEmiliaLover != null){
		       		
		       		// SYSTEM CHECK - Print out that the dodge function has been called with an enemy nearby....
		       		System.out.println("Calling dodge with a nearby enemy....");
		       		
		       		dodgeLocation = Yuurei.tryDodge(desiredMove, myLocation, normieEmiliaLover.location, nearbyBullets, strideRadius, bodyRadius); 
		       	}
		       	else{		
		       		
		       		// SYSTEM CHECK - Print out that the dodge function has been called with an enemy nearby....
		       		System.out.println("Calling dodge without a nearby enemy....");
		       		
		       		dodgeLocation = Yuurei.tryDodge(desiredMove, myLocation, null, nearbyBullets, strideRadius, bodyRadius); 
				    
		       	}
            	// If there is a location that the unit can dodge to..
            	if (dodgeLocation != null){
            		
            		if(!dodgeLocation.equals(desiredMove)){
            			
	            		desiredMove = dodgeLocation;
	            	   	// SYSTEM CHECK - Show desired move after path planning
	        	    	System.out.println("desiredMove altered by dodge to: " + desiredMove.toString());
            		}            		
            		else{
            			// And there is an enemy being shot at.....
                		if(normieID != -1 && normieEmiliaLover != null){
                			
                			// Check to see if the current line of fire is blocked by a tree....
                			if(Korosenai.isLineBLockedByTree(desiredMove, normieEmiliaLover.location, 1)){
                				
	                			// SYSTEM CHECK - Print out that the robot will attempt to find a different firing location..
	                			System.out.println("Attempting to find another firing location");
	                			
	                			MapLocation newFiringLocation = Korosenai.findLocationToFireFrom(myLocation, normieEmiliaLover.location, desiredMove, strideRadius);
	                			
	                			if(newFiringLocation != null){
	    	            			// SYSTEM CHECK - Print out that another firing location had been found...
	    	            			System.out.println("New firing Location found.....");
	    	            			desiredMove = newFiringLocation;
	                			}
                			}            		
                		} 
            		}            		
            	}  
            	
               	// SYSTEM CHECK- Print out the amount of bytecode used prior to movecorrect
		       	System.out.println("Bytecode used before move correct: " + Clock.getBytecodeNum());
            	
                
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
		       	          	
		       	// SYSTEM CHECK- Print out the amount of bytecode used after move correct
		       	System.out.println("Bytecode used after move correct: " + Clock.getBytecodeNum());
		       	
		     // ------------------------ Movement Execution  ------------------------//

    	       	// If the robot can move to the location it wishes to go to.....
		       	if(rc.canMove(desiredMove) && desiredMove != myLocation){
		       		
		       		// SYSTEM CHECK - Print out that the robot successfully moved....
		       		System.out.println("Soldier succesfully moved to desired location");
		       		
		       		// Check to see if the robot will die there
		       		// checkDeath(desiredMove);
		       		// Move to the target location
		       		rc.move(desiredMove);
		       	}
		       	
		       	// If the robot didn't move along, check if it would die from staying in its current location....
		       	else{
		       		
		    		// SYSTEM CHECK - Print out that the robot did not move
		       		System.out.println("Soldier did not move this turn....");
		       				       		
		       		checkDeath(myLocation);
		       	} 
		       	
		       	// SYSTEM CHECK- Print out the amount of bytecode used prior to shooting.......
		       	System.out.println("Bytecode used prior to shooting: " + Clock.getBytecodeNum());		       	
		       	
		       	// Update the position for the end of the round...
                lastPosition =  rc.getLocation();

            	// ------------------------ Shooting ------------------------//
            
            	// SYSTEM CHECK - Notify that the robot is now attempting to shoot at something........
            	// System.out.println("Moving on to shooting phase...................");
            	
            	boolean hasShot = false;
            	
            	if (normieID != -1){
            		
            		// SYSTEM CHECK - Show who the robot is aiming at...
            		System.out.println("Currently shooting at a robot with ID: " + normieID);
            		
            		// Get a list of allied trees to avoid shooting..
            		TreeInfo[] alliedTrees = rc.senseNearbyTrees(-1, allies);
            		
            		if(rc.canSenseRobot(normieID)){
            			hasShot = decideShoot(enemyRobots, alliedRobots, alliedTrees);
            		}
            		else{
            			normieID= -1;
            			normieEmiliaLover = null;
            		}
            	}
            	
            	if(hasShot){            		
            		// SYSTEM CHECK - Inform that the robot has shot something this round....
            		System.out.println("The robot has fired a shot this round....");
            	}
            	else{
              		// SYSTEM CHECK - Inform that the robot has not shot something this round.......
            		System.out.println("The robot has not fired a shot this round....");            		
            	}            	
				
				// Check to see if the unit can shake a tree....
				Chirasou.attemptInteractWithTree(myLocation, bodyRadius);
            	            	
            	// ------------------  Round End Updates --------------------//

                // Make it so that the last direction traveled is the difference between the robot's current and final positions for the round...
                lastDirection = new Direction(myLocation, lastPosition);
                
                
                // Make sure to reset track data.....
            	normieID = -1;
            	normieEmiliaLover = null;  

                // Store the data for the locations of the enemies previously.....
                previousRobotData = enemyRobots;
                
                // SYSTEM CHECK  Make sure the robot finishes its turn
                System.out.println("Turn completed.....");
                
                // If the robot is not currently in a commanded state, increment
                if (!isCommanded){
                	lastCommanded += 1;
                }
                // The robot was in routing phase, so increment that counter
                else{
                	roundsRouting += 1;
                }    

                Clock.yield();    	
            	
	        } catch (Exception e) {
	            System.out.println("Soldier Exception");
	            e.printStackTrace();
	        }    	
    	}
    }
    
    
	// ----------------------------------------------------------------------------------//
	// ------------------------------- MOVEMENT FUNCTIONS -------------------------------//
	// ----------------------------------------------------------------------------------//	 
    
	// Function to determine how the robot will act this turn....
	
	private static MapLocation decideAction(BroadcastChannels.BroadcastInfo distressInfo, RobotInfo[] enemyRobots) throws GameActionException{
		
		// If the scout found some distress signal this turn...
    	if(distressInfo != null){
    		
    		// SYSTEM CHECK - Print out that a distress signal has been received...
    		System.out.println("Distress Signal Received....");
    		
    		// If the distressed gardener is being attacked by a scout.....
    		if (distressInfo.enemyType == 2 || distressInfo.enemyType == 3){
    			
    			// Set the location to defend...
    			defendLocation = new MapLocation (distressInfo.xPosition, distressInfo.yPosition);
				
    			// Set the ID of the offending enemy
				defendAgainstID = distressInfo.ID;
				
		        roundsDefending = 0;
    		}            	
    	}
    	
    	// If the robot is meant to defend........
    	if (defendLocation != null && roundsDefending <= giveUpDefending){    		

			// Call the defend function to determine what to do.....
    		return defend(enemyRobots);
    	}
    	
    	// Otherwise just call the move function normally......
    	else {
    		
    		defendLocation = null;
    		
    		// If the robot currently has orders call the setCommandLocation to see if a new order could be made
    		if(!isCommanded){    			
    			setCommandLocation(null);
    		}
    		
    		// Call the move function to determine where the robot will actually end up going.....
    		return move(enemyRobots);
    	}
	}
    
    
	// Function for the robot to go back and defend if it has received a distress signal....
    
	private static MapLocation defend(RobotInfo[] enemyRobots) throws GameActionException{		

		// If it already nearby or can simply sense the offending unit, track it
		if(rc.canSenseRobot(defendAgainstID)){
			
			// Start tracking the robot to defend against....
			normieID = defendAgainstID;            			
			normieEmiliaLover = rc.senseRobot(normieID);
			
			// Exit the return to defending location - actually defend!!!
			defendLocation = null;            			
			defendAgainstID = -1; 
			foundNormie = true;
			
			// SYSTEM CHECK Display a yellow dot on the enemy to kill now...
			// rc.setIndicatorDot(normieEmiliaLover.location, 255, 255, 0);			
			
			// SYSTEM CHECK IF the robot has need to defend, it will do so...
			System.out.println("Found the offending enemy....");
			
			// Track the enemy....            			
			return engage(enemyRobots);           			
		}
		// If the robot has gotten close enough to the defend location and has not yet exited the defned loop, do so....
		else if (myLocation.distanceTo(defendLocation) <= strideRadius){
			
			// SYSTEM CHECK - Print out that the soldier is near the location to defend and has not seen anything....
			System.out.println("Soldier has arrived at defend location but found nothing.........");			
			
			// Reset the variables related to defending........
			defendLocation = null;            			
			defendAgainstID = -1; 		
			
			// Decide a new action to go to the move loop......			
			return decideAction(null, enemyRobots);
		
		}
		
		else{				
			// SYSTEM CHECK IF the robot has need to defend, it will do so...
			System.out.println("Travelling back to defend....");			
			
			// SYSTEM CHECK - display a BLUE LINE to the distress location....
			rc.setIndicatorLine(myLocation, defendLocation, 0, 0, 128);
     		
			// If the goal location hasn't already been set to the defending location.... set it as the location to go to
			if(goalLocation == null){			
				goalLocation = defendLocation;
				
	     		// Append the location to the routing...
	       		Routing.setRouting(goalLocation);    
	       		
				isCommanded = true;
			}
			
			// Get an enemy to attack on the way, if it can find one...
			
			// See if a robot to be tracked can be found, allow soldier to track any and all units
			normieEmiliaLover = Todoruno.getNewEnemyToTrack(enemyRobots, myLocation, true, false, true, false);	
			
			// If no combat units were found.....
			if (normieEmiliaLover == null){
				
				// Enable selection of scouts
				normieEmiliaLover = Todoruno.getNewEnemyToTrack(enemyRobots, myLocation, true, true, true, false);	
				
				if(normieEmiliaLover != null){
				
					if (normieEmiliaLover.type == RobotType.SCOUT && rc.senseTreeAtLocation(normieEmiliaLover.location) != null){
						
						// SYSTEM CHECK - Print out that the soldier found a scout but that it was in a tree, and that it ignored it...
						System.out.println("Found a scout but ignored it... because it was in a tree....");
						
						// SYSTEM CHECK - Draw a green line to any scout it ignored....
						rc.setIndicatorLine(myLocation, normieEmiliaLover.location, 0, 122, 23);
						
						normieEmiliaLover = null;		
					}	
				}
			}
			
			// If there was no nearby enemy on the way back to defend...			
			if (normieEmiliaLover == null){
				
				// Call the move function
				return moveTowardsGoalLocation(enemyRobots);	
			}
			else{				
				// Update the normieID
				normieID = normieEmiliaLover.ID;	foundNormie = true;
				
				// SYSTEM CHECK - Notify what the robot will now track and set an indicator RED DOT on it
	    		System.out.println("The soldier has noticed the enemy Robot with ID: " + normieID);

				// Call the engage function.........
				return engage(enemyRobots);  
			
			}
		}            		         		
	} 
	
    private static MapLocation move(RobotInfo[] enemyRobots) throws GameActionException{

		// SYSTEM CHECK - Print out that the robot is searching for nearest enemy to engage
		System.out.println("Searching for the next enemy to engage...."); 
		
		// See if a robot to be tracked can be found, allow soldier to track any and all units
		normieEmiliaLover = Todoruno.getNewEnemyToTrack(enemyRobots, myLocation, true, false, true, false);	
		
		// If no combat units were found.....
		if (normieEmiliaLover == null){
			
			// Enable selection of scouts
			normieEmiliaLover = Todoruno.getNewEnemyToTrack(enemyRobots, myLocation, true, true, true, false);	
			
			if(normieEmiliaLover != null){
			
				if (normieEmiliaLover.type == RobotType.SCOUT && rc.senseTreeAtLocation(normieEmiliaLover.location) != null){
					
					// SYSTEM CHECK - Print out that the soldier found a scout but that it was in a tree, and that it ignored it...
					System.out.println("Found a scout but ignored it... because it was in a tree....");
					
					// SYSTEM CHECK - Draw a green line to any scout it ignored....
					rc.setIndicatorLine(myLocation, normieEmiliaLover.location, 0, 122, 23);
					
					normieEmiliaLover = null;		
				}	
			}
		}
		
		
		if((myWaifuIsOnodera >= 500 && normieEmiliaLover == null && rc.getTreeCount() > 10) || (normieEmiliaLover == null && myWaifuIsOnodera > 1500)){
			
			// SYSTEM CHECK - Print out that the robot will now attempt to fire at archons...
			System.out.println("Will now attempt to shoot archons....");
			
			// Search again for enemies......
			normieEmiliaLover = Todoruno.getNewEnemyToTrack(enemyRobots, myLocation, true, true, true, true);			
		}
		
		
		// If the nearest unit is through a tree, ignore it........
		
		if(normieEmiliaLover != null){
			
			if(Korosenai.isLineBLockedByTree(myLocation, normieEmiliaLover.location, 1)  && normieEmiliaLover.type != RobotType.SCOUT){
				
				// SYSTEM CHECK - Draw a red line to all rejected enemies
				rc.setIndicatorLine(myLocation, normieEmiliaLover.location, 255, 0, 0);
				
				// SYSTEM CHECK - Print out that the robot has found an enemy but rejected it...
				System.out.println("Enemy rejected due to tree in the way..");
				
				normieEmiliaLover = null;
			}
		}
		
		// Switch over to the move command after getting a new unit to track.... if the unit is currently being told to go somewhere
		if(isCommanded && normieEmiliaLover == null || (!isCommanded && goalLocation != null)){
		
			// SYSTEM CHECK - Print out that the soldier will attempt to move to a goal location..
			System.out.println("Attempting to move to the goal location at: " + goalLocation.toString());
			
       		// Call the routing function to obtain a location to go to........
			return moveTowardsGoalLocation(enemyRobots);
    	} 
		
		// If there is a robot to track....
		else if (normieEmiliaLover != null){
			
			// Reset the values necessary for switching into a command phase
    		goalLocation = null;	isCommanded = false;    		
			
			// Update the normieID
			normieID = normieEmiliaLover.ID;	foundNormie = true;
			
			// SYSTEM CHECK - Notify what the robot will now track and set an indicator RED DOT on it
    		System.out.println("The soldier has noticed the enemy Robot with ID: " + normieID);

			// Call move again with the updated information - so that the robot will pass into the second bloc of logic
			return engage(enemyRobots);   	
		
		// If there is no robot to be tracked and the robot is not receiving any orders
		} else{
			
			// Make sure that the tracking variables are reset....
			normieEmiliaLover = null;	normieID = -1;	 foundNormie = false;	
			
    		// SYSTEM CHECK - Notify that nothing to be scouted has been found
    		System.out.println("The soldier cannot find anything to engage");    			

    		// Simply add a stride radius away from the initial location if possible.....
    		for (int i = 5; i >= 1; i--){
    			
    			// Get the distance to move away for..... and the resulting map location
    			float testDistance = strideRadius / 5 * i;	            			
    			MapLocation testLocation = myLocation.add(lastDirection, testDistance);
    			
    			// Check if the robot can move to the location and if it can, do so.....
    			if (rc.canMove(testLocation)){	      
    				
    				isCommanded = false;
    				goalLocation = null;
    				
    				// SYSTEM Check - Set LIGHT GREY LINE indicating where the soldier would wish to go
        			rc.setIndicatorLine(myLocation, testLocation, 110, 110, 110);   
        			
    				return testLocation;	            			
    			}	            			
    		}    		
    		// If a move in the last direction was not possible, order the robot to go to original archon location
    		
    		if (archonLocation.distanceTo(myLocation) > 2){
    			setCommandLocation(archonLocation);
    		}
    		// If there was a valid point to go to...
    		if(isCommanded){    	
    			
    			// SYSTEM CHECK - Print out that the robot will now attempt to go to a goal location...
    			System.out.println("Attempting to move to last known location of the archon.....");

    			// Tell the robot to go towards the commanded location....		            			
    			return moveTowardsGoalLocation(enemyRobots);
    		}
    		else{
    			return myLocation;	            		
    		} 	
		}
    }
    
    // Function to follow a unit and approach it..... Similar to scout code but a soldier will never stop following the robot..... 
    // A soldier bot's job in life is to hunt down and kill what it is tracking... especially if the thing it is tracking likes Emilia
    
	private static MapLocation engage(RobotInfo[] enemyRobots) throws GameActionException{
		
		// If the robot can currently sense the robot it was tracking in the previous turn
    	if (rc.canSenseRobot(normieID) && normieEmiliaLover != null){

    		
    		// SYSTEM CHECK - See if the robot identifies that it is actually tracking something
    		System.out.println("Engaging a normie lover with ID: " + normieID);
    		
    		// Update location of tracked robot 
    		normieEmiliaLover = rc.senseRobot(normieID);
    		
    		if (normieEmiliaLover.type == RobotType.SOLDIER || normieEmiliaLover.type == RobotType.LUMBERJACK || normieEmiliaLover.type == RobotType.TANK){
    		
	    		// SYSTEM CHECK - Draw a VIOLET LINE between current position and position of robot
	    		rc.setIndicatorLine(myLocation, normieEmiliaLover.location, 150, 0, 200);
	    		
	    		// Attempt to move towards the new location.....
	    		MapLocation desiredMove = Todoruno.engageEnemy(myLocation, normieEmiliaLover, strideRadius, separationDistance);
	        	
	        	return desiredMove;
    		}
    		else if (normieEmiliaLover.type == RobotType.SCOUT){    			
    			
    	 		// SYSTEM CHECK - Draw a FUSCHIA LINE between current position and position of robot - 
	    		rc.setIndicatorLine(myLocation, normieEmiliaLover.location, 255, 0, 255);
	    		
	    		// Attempt to move towards the new location.....
	    		return Todoruno.engageCivilian(myLocation, normieEmiliaLover, strideRadius);
    			
    		}
    		// If the target is a gardener or archon.....
    		else{
    			
    			// SYSTEM CHECK - Draw a INDIGO LINE between current position and position of robot
	    		rc.setIndicatorLine(myLocation, normieEmiliaLover.location, 75, 0, 130);
    			
    			return Todoruno.engageCivilian(myLocation, normieEmiliaLover, strideRadius);  			
    		}
    		
        // If the robot has lost sight of its target....
    	} else {

    		// Reset the track ID and call the move function again to either get a new target or just move on.....
        	normieID = -1;	foundNormie = false;	normieEmiliaLover = null;

        	// SYSTEM CHECK - Notify of target loss
        	System.out.println("Lost sight of target");        	
        	
        	// Simply add a stride radius away from the initial location if possible.....
    		for (int i = 5; i >= 1; i--){
    			
    			// Get the distance to move away for..... and the resulting map location
    			float testDistance = strideRadius / 5 * i;	            			
    			MapLocation testLocation = myLocation.add(lastDirection, testDistance);
    			
    			// Check if the robot can move to the location and if it can, do so.....
    			if (rc.canMove(testLocation)){	       
    				
    				// SYSTEM Check - Set LIGHT GREY LINE indicating where the soldier would wish to go
        			rc.setIndicatorLine(myLocation, testLocation, 110, 110, 110);   
        			
    				return testLocation;	            			
    			}	            			
    		}    		
    		// If a move in the last direction was not possible, simply order the robot to remain still...		            		
    		
			// SYSTEM CHECK - Print out that the robot cannot move in its previous direction and will remain still...
			System.out.println("Cannot seem to move in the last direction traveled and no other commands issued.. Unit will not move");
			
			// Return the current location of the robot.......
			return myLocation;	    	
    	}	                		
    }	
	
    // Function to use when moving towards a certain location with a certain target.....
    
    private static MapLocation moveTowardsGoalLocation(RobotInfo[] enemyRobots) throws GameActionException{
    	
    	// If the robot has gotten close enough to the goal location, exit the command phase and do something else
    	if (myLocation.distanceTo(goalLocation) < 2 || roundsRouting >= giveUpOnRouting){
    		
    		// SYSTEM CHECK - Print out that the robot has gotten close to the desired location but did not find anything of note...
    		System.out.println("Soldier has reached destination/ Failed to do so and given up.....");
    		
    		// Reset the rounds routing counter.....
    		roundsRouting = 0;
    		
    		// Reset the values necessary for switching into a command phase
    		goalLocation = null;
    		isCommanded = false;
    		
    		// Call the move function again...
    		return move(enemyRobots);
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
	// --------------------------- MISCELLANEOUS FUNCTIONS ------------------------------//
	// ----------------------------------------------------------------------------------//	
	
	// Function to obtain the robot info units in the specified team
	
	private static RobotInfo[] NearbyUnits(Team team, float distance){
	
	return rc.senseNearbyRobots(myLocation, distance , team);
	}
	
	
	// Function to set a command location to the location of the nearest archon......
	
	private static void setCommandLocation(MapLocation location) throws GameActionException{	
	
		// Parameters for a successful command initiation............
		// 1. Make sure that the turn number isn't equivalent to the clearing time of the broadcast....
		// 2. Make sure that the robot is not currently being commanded......
		// 3. Make sure that the robot has not been commanded for the last attackFrequency number of turns
		// 4. Make sure that the robot is not yet tracking anything.......
		
    	
		if(location == null){
			if (myWaifuIsOnodera % BroadcastChannels.BROADCAST_CLEARING_PERIOD != 1  && lastCommanded >= attackFrequency){
	
	       		// Attempt to read enemy archon data
	           	BroadcastChannels.BroadcastInfo newInfo = null;
	           	
	           	// If the robot is allowed to check for archon locations.....	           
	        	BroadcastChannels.BroadcastInfo archonInfo = BroadcastChannels.readEnemyArchonLocations();
	           	
	        	// Update archon information....
	           	if (archonInfo!= null){
	           		archonLocation = new MapLocation(archonInfo.xPosition, archonInfo.yPosition);
	           	}
	        	
	           	//If the soldier is allowed to check for archon information...
	        	if (checkArchons){
	        		newInfo = archonInfo;
	           	}
	           	
	           	// If no archons are left or none have been found, read an enemy location instead...                   	
	           	if(newInfo == null){
	           		newInfo = BroadcastChannels.readEnemyLocations();
	           	}
	      
	           	
	           	// Pseudo random number for joining the attack....
	           	float willJoin = (float) Math.random();
	           	
	           	// If an archon has been seen before and the robot's pseudo random number falls within bounds to join an  attack, create the goal location
	           	// Make sure that the goal location is sufficiently far away - i.e. don't go if it is within sensor Radius....
	           	if (willJoin <= attackProbability && newInfo != null){ 
	           			
	           		// Obtain the location from the broadcast data
	           		MapLocation targetLocation = new MapLocation(newInfo.xPosition, newInfo.yPosition);    
	           		
	           		// Make sure that the robot is somewhat far away....
	           		if(myLocation.distanceTo(targetLocation) >= 3 * strideRadius){
	           		
		           		// The robot now has a command to follow, so will no longer track enemies continuously.....
		           		isCommanded = true;
		           		
		           		// Set the location of the target to go to as the data from the broadcast
		           		goalLocation = targetLocation;           	
		           		
		           		// Append the location to the routing...
		           		Routing.setRouting(goalLocation);           		
		               	
		           		// Reset the lastCommanded since the unit has now received a command
		           		lastCommanded = 0;
		           	}         		
		           	else{
		           		// Calculate the number of archons remaining on the enemy team (that the team has seen)                 
		           		int finishedArchons = rc.readBroadcast(BroadcastChannels.FINISHED_ARCHON_COUNT);
		           		int discoveredArchons = rc.readBroadcast(BroadcastChannels.DISCOVERED_ARCHON_COUNT);                   		
		           		
		           		// IF there are no more enemies to be found....... (as far as the team knows           		
		           		if(finishedArchons == discoveredArchons && rc.getInitialArchonLocations(enemies).length == finishedArchons){
		           			
		           			// SYSTEM CHECK - Print out that the robot will no longer seek archon locations...
		           			System.out.println("Number of archons killed is equivalent to the number seen, the robot will now simply check for nearby enemies....");
		           			
		           			checkArchons = false;
		           			isCommanded = false;
		               		goalLocation = null;          		
		           		}
	           		}
	           	}
	           	else{
		    		// Calculate the number of archons remaining on the enemy team (that the team has seen)                 
		       		int finishedArchons = rc.readBroadcast(BroadcastChannels.FINISHED_ARCHON_COUNT);
		       		int discoveredArchons = rc.readBroadcast(BroadcastChannels.DISCOVERED_ARCHON_COUNT);                   		
		       		
		       		// If it is near the beginning of the game... tell the robot to go to the location of the enemy archon.....
		       		if (discoveredArchons == 0 && myWaifuIsOnodera >= initRound + 2){
		       			
		       			// SYSTEM CHECK - Print out that the soldier will be attempting to go to the initial archon location
		       			System.out.println("Attempting to go to the enemy archon location......");
		       			
		       			// The robot now has a command to follow, so will no longer track enemies continuously.....
		           		isCommanded = true;
		           		
		           		// Set the location of the target to go to as the data from the broadcast
		           		goalLocation = archonLocation;           	
		           		
		           		// Append the location to the routing...
		           		Routing.setRouting(goalLocation);           		
		               	
		           		// Reset the lastCommanded since the unit has now received a command
		           		lastCommanded = 0;	       			
		       		}
		       		
		       		
		       		// IF there are no more enemies to be found....... (as far as the team knows           		
		       		if(finishedArchons == discoveredArchons && rc.getInitialArchonLocations(enemies).length == finishedArchons){
		       			
		       			// SYSTEM CHECK - Print out that the robot will no longer seek archon locations...
		       			System.out.println("Number of archons killed is equivalent to the number seen, the robot will now simply check for nearby enemies....");
		       			
		       			checkArchons = false;
		       			isCommanded = false;
		           		goalLocation = null;          		
		       		}
	           	}
	    	}
		}
		// If no target location was inputted, default to the location of the archon initially...
		else{
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
	
	
	// Target selection and actual shooting decision function	
	
	private static boolean decideShoot(RobotInfo[] enemyRobots, RobotInfo[] alliedRobots, TreeInfo[] alliedTrees) throws GameActionException{
		
		// Obtain a location to shoot at
		MapLocation shootingLocation = Korosenai.getFiringLocation(normieEmiliaLover, previousRobotData, lastPosition);
		
		// Return value to store whether or not the has fired this turn or no....
		boolean hasShot;
		
		// If there is more than one enemy nearby, attempt to fire a pentad at the location
		if(enemyRobots.length >= 3 || normieEmiliaLover.type == RobotType.TANK || (myWaifuIsOnodera >= 500 && rc.getTreeCount() >= 15) || (myWaifuIsOnodera < 500 && normieEmiliaLover.type == RobotType.SOLDIER)){
			
			// If a pentad can be shot...
			hasShot = Korosenai.tryShootAtEnemy(shootingLocation, lastPosition, 2, alliedRobots, alliedTrees, sensorRadius, normieEmiliaLover);
			
			// If that was not possible, try a triad and then a single shot 
			if(!hasShot){
				hasShot = Korosenai.tryShootAtEnemy(shootingLocation, lastPosition, 1, alliedRobots, alliedTrees, sensorRadius, normieEmiliaLover);
			}			
			if(!hasShot){
				hasShot = Korosenai.tryShootAtEnemy(shootingLocation, lastPosition, 0, alliedRobots, alliedTrees, sensorRadius, normieEmiliaLover);
			}			
		}
		else if (enemyRobots.length >= 2 || normieEmiliaLover.type == RobotType.SOLDIER){
			// If a triad can be shot
			hasShot = Korosenai.tryShootAtEnemy(shootingLocation, lastPosition, 1, alliedRobots, alliedTrees, sensorRadius, normieEmiliaLover);
			
			// If that was not possible, try a single shot 
			if(!hasShot){
				hasShot = Korosenai.tryShootAtEnemy(shootingLocation, lastPosition, 0, alliedRobots, alliedTrees, sensorRadius, normieEmiliaLover);
			}		
		}
		else{
			hasShot = Korosenai.tryShootAtEnemy(shootingLocation, lastPosition,0, alliedRobots, alliedTrees, sensorRadius, normieEmiliaLover);
		}
		return hasShot;
	}	
	

	// Function to check if the robot thinks it will die this turn and broadcast if it will.............
	
    public static void checkDeath(MapLocation location) throws GameActionException{
    	
    	// Boollean to store if the robot believes it will be hit if it moves to a certain location......
		boolean beingAttacked = iFeed.willBeAttacked(location);
		
		// If it will get hit from that location....
		if (beingAttacked) {
			
			// SYSTEM CHECK - Print out that the robot thinks it will die this turn....
			System.out.println("Moving to desired location will result in death........");
			
			// If the soldier will lose all of its health from moving to that location....
			boolean willDie = iFeed.willFeed(location);
			
			// If the soldier believes that it will die this turn....
			if (willDie) {
				
				// Set the belief variable to true.....
				believeHasDied = true;
				
				// Get the current number of soldiers in service
		        int currentSoldierNumber = rc.readBroadcast(BroadcastChannels.SOLDIERS_ALIVE_CHANNEL);
		        
		        // Update soldier number for other units to see.....
		        rc.broadcast(BroadcastChannels.SOLDIERS_ALIVE_CHANNEL, currentSoldierNumber - 1);

			}
		}
	}
    
    
    // Function to correct an accidental death update*
    
    public static void fixAccidentalDeathNotification() throws GameActionException{
    	
    	// Reset belief in the robot dying this round....
    	believeHasDied = false;    	

		// Get the current number of soldiers in service
        int currentSoldierNumber = rc.readBroadcast(BroadcastChannels.SOLDIERS_ALIVE_CHANNEL);
        
        // Update soldier number for other units to see.....
        rc.broadcast(BroadcastChannels.SOLDIERS_ALIVE_CHANNEL, currentSoldierNumber + 1);
    	
    }   	
	
}	
	
	


