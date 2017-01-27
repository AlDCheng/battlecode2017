// AI for soldier under normal control
package naclbot.units.AI.soldier;
import battlecode.common.*;

import naclbot.variables.BroadcastChannels;
import naclbot.variables.GlobalVars;
import naclbot.units.interact.iFeed;
import naclbot.units.motion.Chirasou;
import naclbot.units.motion.Move;
import naclbot.units.motion.Todoruno;
import naclbot.units.motion.Yuurei;
import naclbot.units.motion.shoot.Korosenai;
import naclbot.units.motion.routing.Routing;

import java.util.ArrayList;


/* ------------------   Overview ----------------------
 * 
 * Overhaul of original SoldierBot
 *
 * ~~ Coded by Illiyia (akimn@#mit.edu)
 * 
 ---------------------------------------------------- */

public class Senshi extends GlobalVars {
	
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
	private static int trackID; // The robot that the soldier is currently tracking....
	private static RobotInfo trackedRobot; // The Robot that the soldier is currently tracking....
	private static boolean isTracking; // Boolean to show whether or not the soldier is currently tracking something or not...
    private static int roundsTracked = 0; // Variable to store for how long the robot has been tracking something
    
	// Path-planning variables
	private static boolean isCommanded; // Boolean to store whether or not the soldier current has orders to go somewhere....
    public static MapLocation goalLocation; // End location of the path planning
    public static int roundsRouting = 0; // FVariable to store the length of time the robot has been in path planning mode....
    
    
    // Routing constants
    public static final int attackFrequency = 25; // Asserts how often robots will attempt to go on the attack after completing a prior attack....
    public static final float attackProbability = (float) 1; // Gives probability of joining an attack at a particular time....
    private static int lastCommanded = attackFrequency; // Int to store the number of rounds since the unit was last in a commanded mode - threshold value
    public static final int giveUpOnRouting = 250; // Variable to determine after how long soldiers decide that Alan's code is a piece of shit......
    
    // Enemy data variables....
	private static RobotInfo[] previousRobotData; // Array to store the data of enemy robots from the previous turn.....

    // Variables related to gardener defense.....
    private static boolean mustDefend; // Variable to determine whether or not a scout should defend a unit or not...
    private static MapLocation defendLocation; // Location that the scout must defend...
    private static int defendAgainstID; // Enemy to search for once the scout has reached that location	
	
	// ------------- ADDITIONAL VARIABLES/CONSTANTS -------------//

	// Variables related to operational behavior...
	private static MapLocation nearestCivilianLocation; // Stores for multiple rounds the location of the nearest civilian robot....	

    // Store whether or not an archon has been seen...
    public static boolean archonSeen = false;
    
    // Miscellaneous variables.....
 	private static boolean believeHasDied; // Stores whether or not the robot believes it will die this turn or not.........
  
    
    // GET RID OF THIS PIECE OF SHIT VARIABLE
    
	private static Direction myDirection;
    

    
	/************************************************************************
	 ***************** Runtime Functions and Initialization *****************
	 ***********************************************************************/
    
	// Initialization function - makes the default values for most important parameters
    
    public static void init() throws GameActionException{
    	
        // Important parameters for self
        enemies = rc.getTeam().opponent();
        allies = rc.getTeam();
        myID = rc.getID();      
        
        roundNumber = rc.getRoundNum();
        initRound = roundNumber;

        // Get own scoutNumber  and unitNumber- important for broadcasting 
        soldierNumber = rc.readBroadcast(BroadcastChannels.SOLDIER_NUMBER_CHANNEL);        
        unitNumber = rc.readBroadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL);      
          
        // Get the current round number......
        roundNumber = rc.getRoundNum();
        initRound = roundNumber;
 
        // Initialize variables important to self
        myLocation = rc.getLocation();
        trackID = -1;
        isTracking = false;
        trackedRobot = null;
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
       	
       	// Set the soldier to first attempt to move away from the nearest civilian initially....
       	lastDirection = nearestCivilianLocation.directionTo(myLocation);
        
        // Initialize variables relating to defending....
        defendLocation = null;
        defendAgainstID = -1;     
        
       	// Retrieve the number of active lumberjacks and increment......
       	int numberOfActiveScouts = rc.readBroadcast(BroadcastChannels.SOLDIERS_ALIVE_CHANNEL);
       	rc.broadcast(BroadcastChannels.SOLDIERS_ALIVE_CHANNEL, numberOfActiveScouts + 1);    
        
        // Update the number of soldiers so other soldiers can know....
        rc.broadcast(BroadcastChannels.SOLDIER_NUMBER_CHANNEL, soldierNumber + 1);
        rc.broadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL, unitNumber + 1);
        
        main();
    }
    
    
    public static void main() throws GameActionException{
    	
    	// Actions to be completed every turn by the soldier.....,
    	while(true){
    		
    		try{
    		
    			// ------------------------- RESET/UPDATE VARIABLES ----------------//        
    			
            	// Get nearby enemies and allies and bullets for use in other functions            	
            	RobotInfo[] enemyRobots = NearbyUnits(enemies, sensorRadius);
            	RobotInfo[] alliedRobots = NearbyUnits(allies, sensorRadius);
            	BulletInfo[] nearbyBullets = rc.senseNearbyBullets();
    			
            	// If the robot was just initialized or did not move last turn, set the last direction to point away from anything....
            	
    			if(lastDirection == null){
    		        RobotInfo nearestAlly = Chirasou.getNearestAlly(alliedRobots, myLocation);
    		        
    		        if(nearestAlly != null){
    		        	lastDirection = new Direction(myLocation.directionTo(nearestAlly.location).radians + (float)Math.PI);
    		        }
    		        else{
    		        	lastDirection = Move.randomDirection();
    		        }
    			}    			  
    			
    	    	// If the robot thought it died previously but didn't.... update information...
            	if(believeHasDied){
            		fixAccidentalDeathNotification();
            	}
            	
            	// Update location of self
            	myLocation = rc.getLocation();    
            	MapLocation desiredMove = myLocation;
            	
            	// Initialize the direction the robot would like to go to at any given round as the direction the robot moved previously....     	
            	myDirection = lastDirection;
            	
            	// If the robot is probably going to attempt to move straight to another unit.,.
            	if(rc.isLocationOccupied(myLocation.add(myDirection, bodyRadius + (float) 0.1))){
            		
            		float newValue = (float)(Math.random() * Math.PI - Math.PI / 2);
            		myDirection = new Direction(lastDirection.radians + (float)Math.PI + newValue);
            	}

                      	
            	if (goalLocation != null){
            		// SYSTEM CHECK - Show where the robot is attempting to go to.... SILVER LINE
            		rc.setIndicatorLine(myLocation, goalLocation, 192, 192, 192);
            	}
            	
            	// Have the soldiers change rotation angle every 10 turns?
            	float changeRotation = (float) Math.random();
            	if (changeRotation > 0.9){
            		rotationDirection = !rotationDirection;
            	}
            	
            	// ------------ ACTIONS TO BE COMPLETED -------------//
            	
               	
            	// Update the nearest enemy and archon locations
               	BroadcastChannels.broadcastEnemyArchonLocations(enemyRobots);     
            	BroadcastChannels.broadcastNearestEnemyLocation(enemyRobots, myLocation, unitNumber, nearestCivilianLocation, roundNumber);
               	
              	// Update the distress info and retreat to a scout if necessary            	
            	BroadcastChannels.BroadcastInfo distressInfo = BroadcastChannels.readDistress(myLocation, 25);
         
             	// Parameter to store the location of the nearest ally for the current turn - use this to determine whether or not to break out of a tracking operation.....       	
            	RobotInfo NearestAlly;
               	
            	// Update the location of the nearest noncombatant allied location and store into the variable Nearest Ally - which is null if no nearby ally exists
            	
            	if (alliedRobots.length > 0){            		
                 	NearestAlly =  Todoruno.getNearestGardener(alliedRobots, myLocation);
            	}
            	else{
            		NearestAlly = null;
            	}
             	
             	// If there is a friendly noncombatant nearby
             	if(NearestAlly != null){
             		
             		nearestCivilianLocation = NearestAlly.location;
             		
             		// For Initialization and for the future,- have last direction originally point away from the closest ally, rounded to 30 degree intervals             		
             		if (myLocation.distanceTo(nearestCivilianLocation) <= 2.5){
	             		int randOffset = (int)(Math.random() * 4 - 2);
	            		Direction awayAlly = new Direction(myLocation.directionTo(nearestCivilianLocation).radians + (float) (Math.PI + randOffset * Math.PI/8));
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
	            		if(nearestEnemy.location.distanceTo(nearestCivilianLocation) < 20){
	            			mustDefend = true;
	            		}
            		}
             	}
             	
          		if (nearestCivilianLocation != null){
	         		// SYSTEM CHECK - Draw a white line to the nearest civilian's location
	             	rc.setIndicatorLine(myLocation, nearestCivilianLocation, 255, 255, 255);
         		}
             	
             	if(mustDefend){
             		isCommanded = false;
             	}
             	
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
            	if (defendLocation != null && enemyRobots.length == 0){            		
    				
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
            			desiredMove = track(enemyRobots, 1, desiredMove);            			
            		}
            		// If the robot has arrived at the defend location and has not found the enemy.....
            		else if (myLocation.distanceTo(defendLocation) <= 5){
            			
            			defendLocation = null;
            			defendAgainstID = -1;
            			trackID = -1;
            			trackedRobot = null;
            			
            			
            			// SYSTEM CHECK - display a green line to the distress location....
        				// rc.setIndicatorLine(myLocation, defendLocation, 0, 128, 0);
            			
            			// SYSTEM CHECK IF the robot has need to defend, it will do so...
            			System.out.println("Returned to distress call but found no one");
            			
            			// Exit the call to defendLocations and go on back to normal operations
            			desiredMove = move(enemyRobots, desiredMove);
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

            	
            	else{
	               	// Periodically check to go to a nearby archon......
	            	if (roundNumber % BroadcastChannels.BROADCAST_CLEARING_PERIOD == 0 && !isCommanded && lastCommanded >= attackFrequency && trackID == -1){
	                   	
	               		// Reset the lastCommanded 
	               		lastCommanded = 0;
	               		
	               		// Read archon data
	                   	BroadcastChannels.BroadcastInfo newInfo = BroadcastChannels.readEnemyArchonLocations();
	                   	
	                   	// If no archons are left or none have been found, read an enemy location instead...                   	
	                   	if(newInfo == null && archonSeen){
	                   		newInfo = BroadcastChannels.readEnemyLocations();
	                   	}
	                   	else{
	                   		archonSeen = true;
	                   	}
	                   	
	                   	// Pseudo random number for joining the attack....
	                   	float willJoin = (float) Math.random();
	                   	
	                   	// If an archon has been seen before and the robot's pseudo random number falls within bounds to join an  attack, create the goal location
	                   	if (willJoin <= attackProbability && newInfo != null){
	                   		
	                   		// The robot now has a command to follow, so will no longer track...
	                   		isCommanded = true;
	                   		
	                   		goalLocation = new MapLocation(newInfo.xPosition, newInfo.yPosition);                    	
	                   		
	                   		// If there was a previous path.... clear it
//	                   		Routing.resetRouting();
	                   		Routing.setRouting(goalLocation);
//	                    	routingPath.add(goalLocation);
//	                    	Routing.setRouting(routingPath);
	                   	}
	                   	else{
	                   		// Calculate the number of archons remaining on the enemy team (that the team has seen)                 
	                   		int finishedArchons = rc.readBroadcast(BroadcastChannels.FINISHED_ARCHON_COUNT);
	                   		int discoveredArchons = rc.readBroadcast(BroadcastChannels.DISCOVERED_ARCHON_COUNT);                   		
	                   		// IF there are no more enemies to be found.......
	                   		
	                   		if(finishedArchons == discoveredArchons){
	                   			isCommanded = false;
	                       		goalLocation = null;         
	                   			
	                   		}
	                   		else{
	                   			isCommanded = false;
	                       		goalLocation = null;                   			
	                   		}
	          
	                   	}
	               	}               	
	            	
	            	// Call the move function - to either track an enemy or simply 
	            	desiredMove = move(enemyRobots, desiredMove);
            	}
            	
            	// -------------------- MOVE CORRECTION ---------------------//
            	
            	// Get the correction from the wrapping correct all move function....            	
            	MapLocation correctedMove = Yuurei.correctAllMove(strideRadius, bodyRadius, rotationDirection, allies, myLocation, desiredMove);
            	
            	if (correctedMove != null){
            		desiredMove = correctedMove;
            	}            	
            	
            	// --------------------------- DODGING ------------------------ //
            	
              	// SYSTEM CHECK - Notify that the robot is now attempting to call the dodge function
            	// System.out.println("Calling the dodge function");
            	
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
            	   	// SYSTEM CHECK - Show desired move after path planning
        	    	System.out.println("desiredMove after dodge correction: " + desiredMove.toString());
            	}  

               	// SYSTEM CHECK - Show desired move after path planning
    	    	System.out.println("desiredMove before final move....: " + desiredMove.toString());
    	    	
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
            	// ------------------------ Shooting ------------------------//
            	
            	// SYSTEM CHECK - Notify that the robot is now attempting to shoot at something........
            	// System.out.println("Moving on to shooting phase...................");
            	
            	boolean hasShot = false;
            	
            	if (trackID >= 0){
            		
            		// SYSTEM CHECK - Show who the robot is aiming at...
            		System.out.println("Currently shooting at a robot with ID: " + trackID);
            		
            		// Get a list of allied trees to avoid shooting..
            		TreeInfo[] alliedTrees = rc.senseNearbyTrees(-1, allies);
            		
            		if(rc.canSenseRobot(trackID)){
            			hasShot = decideShoot(enemyRobots, alliedRobots, alliedTrees);
            		}
            		else{
            			trackID= -1;
            			trackedRobot = null;
            		}
            	}
            	
            	if(hasShot){            		
            		// SYSTEM CHECK - Inform that the robot has shot something this round....
            		// System.out.println("The robot has fired a shot this round....");
            	}
            	else{
              		// SYSTEM CHECK - Inform that the robot has not shot something this round.......
            		// System.out.println("The robot has not fired a shot this round....");            		
            	}
            	
            	
            	// ------------------  Round End Updates --------------------//
                            	
            	// At the end of the turn update the round number
                roundNumber += 1;

                // Make it so that the last direction traveled is the difference between the robot's current and final positions for the round...
                lastPosition =  rc.getLocation();
                lastDirection = new Direction(myLocation, lastPosition);
                
                
                // Make sure (if the above code is missing something) that trackedRobot and trackID are both null if either is..
                if(trackedRobot == null || trackID == -1){
                	trackID = -1;
                	trackedRobot = null;
                }
         
                
                // Store the data for the locations of the enemies previously.....
                previousRobotData = enemyRobots;
                
                // SYSTEM CHECK  Make sure the robot finishes its turn
                // System.out.println("Turn completed.....");
                
                // If the robot is not currently in a commanded state, increment
                if (!isCommanded){
                	lastCommanded += 1;
                }
                // The robot was in routing phase, so increment that counter
                else{
                	roundsRouting += 1;
                }    
                // If the robot was tracking something, increment the counter for it...
                if (isTracking){
                	roundsTracked +=1;
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
    
    private static MapLocation move(RobotInfo[] enemyRobots, MapLocation desiredMove) throws GameActionException{
    	
    	// If the robot is currently not tracking anything
    	if(trackID == -1){  
    		
    		// See if a robot to be tracked can be found, allow soldier to track any and all units
    		trackedRobot = Todoruno.getNewEnemyToTrack(enemyRobots, myLocation, true, true, true);
    		
    		// SYSTEM CHECK - see if the robot recognizes that it is currently not tracking anything
    		// System.out.println("Currently not tracking anything");
    		
    		// Switch over to the move command after getting a new unit to track.... if the unit is currently being told to go somewhere
    		if(isCommanded){    		
    	   		// If there is a robot
        		if (trackedRobot != null){
        			// Update the trackID
        			trackID = trackedRobot.ID;
        			isTracking = true;
        		}        		
    			return moveTowardsGoalLocation(enemyRobots, desiredMove);
    		}
    		
    		// If there is a robot
    		if (trackedRobot != null){
    			// Update the trackID
    			trackID = trackedRobot.ID;
    			isTracking = true;
    			
    			// SYSTEM CHECK - Notify what the robot will now track and set an indicator RED DOT on it
        		// System.out.println("The soldier has noticed the enemy Robot with ID: " + trackID);
        		
        		rc.setIndicatorDot(trackedRobot.location, 255, 0, 0);        		
        		
    			// Call move again with the updated information
    			return move(enemyRobots, desiredMove);    	
    		
    		} else{ // If there is no robot to be tracked and the robot is not receiving any orders
    			
    			trackedRobot = null;
    			trackID = -1;
    			
    			// Posit the desired move location as a forward movement along the last direction
    			desiredMove = myLocation.add(myDirection, (float) (Math.random() * (strideRadius / 2)  + (strideRadius / 2)));
    			
    			// SYSTEM Check - Set LIGHT GREY LINE indicating where the soldier would wish to go
    			rc.setIndicatorLine(myLocation, desiredMove, 110, 110, 110);    			
       			
        		// SYSTEM CHECK - Notify that nothing to be scouted has been found
        		// System.out.println("The soldier cannot find anything to track");     
    			
    			return desiredMove;
    				
    		}
    	// If the robot is actually currently tracking something
    	} else{
    		
    		// If the soldier is currently not commanded to go anywhere... follow the robot in question
    		if(!isCommanded){
    			// Call the track function.....
    			return track(enemyRobots, (float) 1.8, desiredMove);
    		}
    		else{
    			return moveTowardsGoalLocation(enemyRobots, desiredMove);
    		}
    	}    			
    }
    
    
    // Function to use when moving towards a certain location with a certain target.....
    
    private static MapLocation moveTowardsGoalLocation(RobotInfo[] enemyRobots, MapLocation desiredMove) throws GameActionException{
    	
    	// If it can no longer sense the tracked enemy.....
    	if (trackID != -1 && !rc.canSenseRobot(trackID)){
    		trackID = -1;       
        	isTracking = false;
        	trackedRobot = null;
    	}
    	
    	// If the robot has gotten close enough to the goal location, exit the command phase and do something else
    	if (myLocation.distanceTo(goalLocation) < 5 || roundsRouting >= giveUpOnRouting){
    		
    		// Reset the values necessary for switching into a command phase
    		goalLocation = null;
    		isCommanded = false;
    		
    		// Call the move function again...
    		return move(enemyRobots, desiredMove);
    	}
    	else{
    		// SYSTEM CHECK - Inform that the robot is currently attempting to following a route to a goal destination.....
    	
	    	System.out.println("Currently attempting to move to a goal location with x: " + goalLocation.x + " and y: " + goalLocation.y);
	    	
	    	// Otherwise, call the routing wrapper to get a new location to go to...
	    	Routing.routingWrapper();
	    	
	    	// Set the desired Move
	    	desiredMove = Routing.path.get(0);
	    	
	    	// SYSTEM CHECK - Show desired move after path planning
	    	System.out.println("desiredMove before post-processing: " + desiredMove.toString());
	    	
	    	// SYSTEM CHECK Print dot from current location to intended move location - GREY DOT
	    	rc.setIndicatorDot(desiredMove, 105, 105, 105);    
	    	
	    	return desiredMove;
    	}
    } 
    
    
    // Function to follow a unit and approach it..... Similar to scout code but a soldier will never stop following the robot..... 
    // A soldier bot's job in life is to hunt down and kill what it is tracking... especially if the thing it is tracking likes Emilia
    
	private static MapLocation track(RobotInfo[] enemyRobots, float multiplier, MapLocation desiredMove) throws GameActionException{
		
		// If the robot can currently sense the robot it was tracking in the previous turn
    	if (rc.canSenseRobot(trackID) && !(roundsTracked >= 10 && trackedRobot.type == RobotType.ARCHON)){
    		
    		
    		// SYSTEM CHECK - See if the robot identifies that it is actually tracking something
    		System.out.println("I am continuing to follow a normie Emilia lover with ID: " + trackID);
    		
    		// Update location of tracked robot 
    		trackedRobot = rc.senseRobot(trackID);
    		
    		// SYSTEM CHECK - Draw a VIOLET LINE between current position and position of robot
    		rc.setIndicatorLine(myLocation, trackedRobot.location, 150, 0, 200);
    		
    		// Attempt to move towards the new location.....
    		desiredMove = Todoruno.moveTowardsTarget(trackedRobot, myLocation, strideRadius, rotationDirection, desiredMove, multiplier);
    		
        	// SYSTEM CHECK Print line from current location to intended move location - light blue green
        	//  rc.setIndicatorLine(myLocation, desiredMove, 0, 200, 200);   	
        	
        	isTracking = true;
        	
        	return desiredMove;
        	
        // If the robot has lost sight of its target....
    	} else {

    		// Reset the track ID and call the move function again to either get a new target or just move on.....
        	trackID = -1;       
        	isTracking = false;
        	trackedRobot = null;
        	roundsTracked = 0;
			
        	// SYSTEM CHECK - Notify of target loss
        	// System.out.println("Lost sight of target/Finding a new target");        	
        	
        	return move(enemyRobots,desiredMove);
    	}	                		
    }	       
    
    
	// ----------------------------------------------------------------------------------//
	// --------------------------- MISCELLANEOUS FUNCTIONS ------------------------------//
	// ----------------------------------------------------------------------------------//	
	
	// Function to obtain the robot info units in the specified team
	
	private static RobotInfo[] NearbyUnits(Team team, float distance){
	
	return rc.senseNearbyRobots(myLocation, distance , team);
	}
	
	
	private static boolean decideShoot(RobotInfo[] enemyRobots, RobotInfo[] alliedRobots, TreeInfo[] alliedTrees) throws GameActionException{
		
		// Obtain a location to shoot at
		MapLocation shootingLocation = Korosenai.getFiringLocation(trackedRobot, previousRobotData, myLocation);
		
		// Return value to store whether or not the has fired this turn or no....
		boolean hasShot;
		
		// If there is more than one enemy nearby, attempt to fire a pentad at the location
		if(enemyRobots.length >= 4){
			
			// If a pentad can be shot...
			hasShot = Korosenai.tryShootAtEnemy(shootingLocation, myLocation, 2, alliedRobots, alliedTrees, sensorRadius);
			
			// If that was not possible, try a triad and then a single shot 
			if(!hasShot){
				hasShot = Korosenai.tryShootAtEnemy(shootingLocation, myLocation, 1, alliedRobots, alliedTrees, sensorRadius);
			}			
			if(!hasShot){
				hasShot = Korosenai.tryShootAtEnemy(shootingLocation, myLocation, 0, alliedRobots, alliedTrees, sensorRadius);
			}			
		}
		else if (enemyRobots.length >= 2 || trackedRobot.type == battlecode.common.RobotType.ARCHON){
			// If a triad can be shot
			hasShot = Korosenai.tryShootAtEnemy(shootingLocation, myLocation, 1, alliedRobots, alliedTrees, sensorRadius);
			
			// If that was not possible, try a single shot 
			if(!hasShot){
				hasShot = Korosenai.tryShootAtEnemy(shootingLocation, myLocation, 0, alliedRobots, alliedTrees, sensorRadius);
			}		
		}
		else{
			hasShot = Korosenai.tryShootAtEnemy(shootingLocation, myLocation,0, alliedRobots, alliedTrees, sensorRadius);
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
    
    // Function to correct an accidental death update
    
    public static void fixAccidentalDeathNotification() throws GameActionException{
    	
    	// Reset belief in the robot dying this round....
    	believeHasDied = false;    	

		// Get the current number of soldiers in service
        int currentSoldierNumber = rc.readBroadcast(BroadcastChannels.SOLDIERS_ALIVE_CHANNEL);
        
        // Update soldier number for other units to see.....
        rc.broadcast(BroadcastChannels.SOLDIERS_ALIVE_CHANNEL, currentSoldierNumber + 1);
    	
    }   	
	
}	
	
	


