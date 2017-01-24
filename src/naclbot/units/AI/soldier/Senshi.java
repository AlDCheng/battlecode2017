// AI for soldier under normal control
package naclbot.units.AI.soldier;
import battlecode.common.*;

import naclbot.variables.BroadcastChannels;
import naclbot.variables.GlobalVars;
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
	
	// ------------- GENERAL (IMPORTANT TO SELF) VARS -------------//
	
	// Variable for round number
	private static int roundNumber;
	
	// Variables for self and team recognition
	public static int myID;
	public static int soldierNumber;
	public static int unitNumber;
	private static Team enemy;	
	private static Team allies;		
	private static final float strideRadius = battlecode.common.RobotType.SOLDIER.strideRadius;
	private static final float bodyRadius = battlecode.common.RobotType.SOLDIER.bodyRadius;
	private static final float senseRadius = battlecode.common.RobotType.SOLDIER.sensorRadius;
	private static float teamBullets;
	
	// The intial round in which the soldier was constructed
	public static int initRound;
	
	// Parameters to store locations of self and the nearest archon
	public static MapLocation myLocation;	
	
	// The total number of soldiers in active service
	private static int currentNumberofSoldiers;
	
	// Boolean to store whether or not the soldier current has orders to go somewhere....
	private static boolean isCommanded;
	
	// Array to store the data of enemy robots from the previous turn.....
	private static RobotInfo[] previousRobotData;
	
	// ------------- MOVEMENT VARIABLES -------------//
	
	// Direction at which the soldier traveled last
	private static Direction lastDirection;
	private static MapLocation lastPosition;
	
	// Direction for use each round
	private static Direction myDirection;
	
	// The ID of the robot the soldier is currently tracking and its information
	public static int trackID;	
	public static RobotInfo trackedRobot;
	public static boolean isTracking;
    
    // Variable to see how long the robot has not tracked another unit for
    public static int hasNotTracked;   
    
    // Variable to determine in which direction the soldier will rotate about a particular robot when tracking it....
    public static boolean rotationDirection;
    
    // Arraylist to store path for routing....    
    public static ArrayList<MapLocation> routingPath;
    
    // Maplocation to store a target location when being told to go to a location
    public static MapLocation goalLocation;
    
    // Asserts how often robots will attempt to go on the attack after completing a prior attack....
    public static final int attackFrequency = 50;
    
    // Gives probability of joining an attack at a particular time....
    public static final float attackProbability = (float) 0.4;
    
    // Int to store the number of rounds since the unit was last in a commanded mode - threshold value
    public static int lastCommanded = attackFrequency;
    
    // Variable to determine after how long soldiers decide that Alan's code is a piece of shit......
    public static final int giveUpOnRouting = 75;
    
    // Variable to store the amount of time currently in routing....
    public static int roundsRouting = 0;

    
	/************************************************************************
	 ***************** Runtime Functions and Initialization *****************
	 ***********************************************************************/
    
	// Initialization function - makes the default values for most important parameters
    
    public static void init() throws GameActionException{
    	
    	// SYSTEM CHECK - See if the soldier has initialized...    	
    	System.out.println("I'm an soldier!");
    	
    	// Initialize variables important to self and team recognition
        enemy = rc.getTeam().opponent();
        allies = rc.getTeam();        
        
        roundNumber = rc.getRoundNum();
        initRound = roundNumber;
        
        myID = rc.getID();
        myLocation = rc.getLocation();
        
        // Initialize Tracking Variables...
        trackID = -1;
        isTracking = false;
        trackedRobot = null;
        rotationDirection = false;
        previousRobotData = null;
        
        // Initialize path list and goal location
    	routingPath = new ArrayList<MapLocation>();    	
    	Routing.setRouting(routingPath);
    	
    	// Goal location.....
        goalLocation = null;
        		
        // Initialize soldier so that it does not have any commands initially;
        isCommanded = false;

        // Get own soldierNumber - important for broadcasting 
        soldierNumber = rc.readBroadcast(BroadcastChannels.SOLDIER_NUMBER_CHANNEL);
        currentNumberofSoldiers = soldierNumber + 1;
        
        unitNumber = rc.readBroadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL);
        rc.broadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL, unitNumber + 1);
        
        // Update soldier number for other soldiers to see.....
        rc.broadcast(BroadcastChannels.SOLDIER_NUMBER_CHANNEL, currentNumberofSoldiers);
        
        // SYSTEM CHECK to see if init() is completed   
        // System.out.println("Soldier successfully initialized!");		
        
        main();
    }
    
    
    public static void main() throws GameActionException{
    	
    	// Actions to be completed every turn by the soldier.....,
    	while(true){
    		
    		try{
    			// ------------------------- RESET/UPDATE VARIABLES ----------------//          
    			
            	// Get nearby enemies and allies and bullets for use in other functions            	
            	RobotInfo[] enemyRobots = NearbyUnits(enemy);
            	RobotInfo[] alliedRobots = NearbyUnits(allies);
            	BulletInfo[] nearbyBullets = rc.senseNearbyBullets();
            	
            	// Update location of self
            	myLocation = rc.getLocation();         	
            	
            	// Initialize the direction the robot would like to go to at any given round as the direction the robot moved previously....     	
            	myDirection = lastDirection;

            	// Placeholder for desired location to go to
            	MapLocation tryMove = myLocation;
            	
            	// Update the team's bullets
            	teamBullets = rc.getTeamBullets(); 
            	
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
            	
            	// If a nearby enemy is sighted, broadcast its information......
            	
               	BroadcastChannels.broadcastEnemyArchonLocations(enemyRobots);     
               	
               	// Periodically check to go to a nearby archon......
               	if (roundNumber % 25 == 0 && !isCommanded && lastCommanded >= attackFrequency && trackID == -1){
                   	
               		// Reset the lastCommanded 
               		lastCommanded = 0;
               		
               		// Read archon data
                   	BroadcastChannels.BroadcastInfo newInfo = BroadcastChannels.readEnemyArchonLocations();
                   	
                   	// Pseudo random number for joining the attack....
                   	float willJoin = (float) Math.random();
                   	
                   	// If an archon has been seen before and the robot's pseudo random number falls within bounds to join an  attack, create the goal location
                   	if (willJoin <= attackProbability && newInfo != null){
                   		
                   		// The robot now has a command to follow, so will no longer track...
                   		isCommanded = true;
                   		
                   		goalLocation = new MapLocation(newInfo.xPosition, newInfo.yPosition);                    	
                   		
                   		// If there was a previous path.... clear it
                   		Routing.resetRouting();
                    	routingPath.add(goalLocation);
                    	Routing.setRouting(routingPath);
                   	}
                   	else{
                   		isCommanded = false;
                   		goalLocation = null;
                   	}
               	}

            	
            	// Call the move function - to either track an enemy or simply 
            	MapLocation desiredMove = move(enemyRobots, tryMove);
            	
            	// -------------------- MOVE CORRECTION ---------------------//
            	
            	// Check if the initially selected position was out of bounds...
            	
               	// SYSTEM CHECK - Show desired move after path planning
    	    	System.out.println("desiredMove before post-processing: " + desiredMove.toString());
    	    	
            	// Correct desiredMove to within one soldier  stride location of where the robot is right now....
            	if(myLocation.distanceTo(desiredMove) > strideRadius){
            		
	            	Direction desiredDirection = new Direction(myLocation, desiredMove);	
	            	
	            	desiredMove = myLocation.add(desiredDirection, strideRadius);
            	}
            	
               	// SYSTEM CHECK - Show desired move after path planning
    	    	System.out.println("desiredMove after rescaling: " + desiredMove.toString());
            	
            	// Hold uncorrected desired Move for routing purposes.........
            	boolean moveWasEdited = false;
            	
            	// SYSTEM CHECK Make sure the new desired move is in the correct location LIGHT BLUE DOT
            	// rc.setIndicatorDot(desiredMove, 102, 255, 255);
            	
            	// Check to see if the desired move is out of bounds and make it bounce off of the wall if it is...            	
            	if (!rc.canMove(desiredMove)){
            		MapLocation newLocation = Yuurei.correctOutofBoundsError(desiredMove, myLocation, bodyRadius, strideRadius, rotationDirection);
            		
            		myDirection = new Direction(myLocation, newLocation);
            		
            		desiredMove = newLocation;
            		moveWasEdited = true;
            		
            	   	// SYSTEM CHECK - Show desired move after path planning
        	    	System.out.println("desiredMove after out of bounds correction: " + desiredMove.toString());  
            	}
            	
             	
            	// Check if the initial desired move can be completed and wasn't out of bounds/corrected by the above function
            	if(!rc.canMove(desiredMove)){          		
            	
					MapLocation newLocation = Yuurei.attemptRandomMove(myLocation, desiredMove, strideRadius);
					
					// SYSTEM CHECK See if the robot called the attemptRandom Move function or no....
					// System.out.println("Attempted to find a new location to move to randomly...");
					
					desiredMove = newLocation;
					moveWasEdited = true;

	    	       	// SYSTEM CHECK - Show desired move after path planning
	    	    	System.out.println("desiredMove after collision correcetion " + desiredMove.toString());
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
            	 
            	// If the canDodge variable was modified, the robot did attempt to dodge....
            	if(!canDodge){
            		moveWasEdited = true;
            	}
            	
            	// If there is a location that the unit can dodge to..
            	if (dodgeLocation != null){
            		desiredMove = dodgeLocation;
            	   	// SYSTEM CHECK - Show desired move after path planning
        	    	System.out.println("desiredMove after dodge correction: " + desiredMove.toString());
            	}  

               	// SYSTEM CHECK - Show desired move after path planning
    	    	System.out.println("desiredMove before final move....: " + desiredMove.toString());
    	    	
            	// See whether or not the robot can move to the completely processed desired move, and move if it does
            	if(rc.canMove(desiredMove)){
            		rc.move(desiredMove);
            	}
            	// If the robot wasn't able to move....
            	else{
            		// SYSTEM CHECK - Make sure that the robot didn't move because it didn't want to....
            		// System.out.println("This robot did not move because it forgot to show Rem appreciation........");
            	}
            	
            	// If the robot post-processed its final given location, reset the routing function to clear waypoints
            	if (isCommanded && moveWasEdited){
            		Routing.resetRouting();
            	}            	
            	
            	// ------------------------ Shooting ------------------------//
            	
            	// SYSTEM CHECK - Notify that the robot is now attempting to shoot at something........
            	// System.out.println("Moving on to shooting phase...................");
            	
            	boolean hasShot = false;
            	
            	if (trackID >= 0){
            		
            		// SYSTEM CHECK - Show who the robot is aiming at...
            		System.out.println("Currently shooting at a robot with ID: " + trackID);
            		
            		hasShot = decideShoot(enemyRobots, alliedRobots);
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
                
                // If the robot was not tracking, increment the value by one round....
                if (!isTracking){
                	hasNotTracked += 1;
                }
                
                // Store the data for the locations of the enemies previously.....
                previousRobotData = enemyRobots;
                
                // SYSTEM CHECK  Make sure the robot finishes its turn
                // System.out.println("Turn completed.....");
                
                // If the robot is not currently in a commanded state, increment
                if (!isCommanded){
                	lastCommanded += 1;
                }
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
    
    
    
	/******************************************************************
	******************* Functions for Movement  ***********************
	*******************************************************************/   
    
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
    			return track(enemyRobots, (float)2, desiredMove);
    		}
    		else{
    			// TODO Insert path planning here........
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
    	if (myLocation.distanceTo(goalLocation) < 12 || roundsRouting >= giveUpOnRouting){
    		
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
    	if (rc.canSenseRobot(trackID)){
    		
    		// SYSTEM CHECK - See if the robot identifies that it is actually tracking something
    		// System.out.println("I am continuing to follow a normie Emilia lover with ID: " + trackID);
    		
    		// Update location of tracked robot 
    		trackedRobot = rc.senseRobot(trackID);
    		
    		// SYSTEM CHECK - Draw a VIOLET LINE between current position and position of robot
    		// rc.setIndicatorLine(myLocation, trackedRobot.location, 150, 0, 200);
    		
    		// Attempt to move towards the new location.....
    		desiredMove = Todoruno.moveTowardsTarget(trackedRobot, myLocation, strideRadius, rotationDirection, desiredMove, multiplier);
    		
        	// SYSTEM CHECK Print line from current location to intended move location - light blue green
        	//  rc.setIndicatorLine(myLocation, desiredMove, 0, 200, 200);   	
        	
        	isTracking = true;
        	hasNotTracked = 0;
        	
        	return desiredMove;
        	
        // If the robot has lost sight of its target....
    	} else {

    		// Reset the track ID and call the move function again to either get a new target or just move on.....
        	trackID = -1;       
        	isTracking = false;
        	trackedRobot = null;
			
        	// SYSTEM CHECK - Notify of target loss
        	// System.out.println("Lost sight of target/Finding a new target");        	
        	
        	// Posit the desired move location as a forward movement along the last direction
			desiredMove = myLocation.add(myDirection, (float) (Math.random() * (strideRadius / 2)  + (strideRadius / 2)));
			
			// SYSTEM Check - Set LIGHT GREY LINE indicating where the soldier would wish to go
			rc.setIndicatorLine(myLocation, desiredMove, 110, 110, 110);    			
   			
    		// SYSTEM CHECK - Notify that nothing to be scouted has been found
    		// System.out.println("The soldier cannot find anything to track"); 
			
			return desiredMove;
    	}	                		
    }	       
    
    
	/******************************************************************
	******************* Miscellaneous Functions************************
	*******************************************************************/   	
	
	// Function to obtain the robot info units in the specified team
	
	private static RobotInfo[] NearbyUnits(Team team){
	
	return rc.senseNearbyRobots(myLocation, (float)10, team);
	}
	
	
	private static boolean decideShoot(RobotInfo[] enemyRobots, RobotInfo[] alliedRobots) throws GameActionException{
		
		// Obtain a location to shoot at
		MapLocation shootingLocation = Korosenai.getFiringLocation(trackedRobot, previousRobotData, myLocation);
		
		// Return value to store whether or not the has fired this turn or no....
		boolean hasShot;
		
		// If there is more than one enemy nearby, attempt to fire a pentad at the location
		if(enemyRobots.length > 1){
			
			// If a pentad can be shot...
			hasShot = Korosenai.tryShootAtEnemy(shootingLocation, myLocation, 2, alliedRobots);
			
			// If that was not possible, try a triad and then a single shot 
			if(!hasShot){
				hasShot = Korosenai.tryShootAtEnemy(shootingLocation, myLocation, 1, alliedRobots);
			}			
			if(!hasShot){
				hasShot = Korosenai.tryShootAtEnemy(shootingLocation, myLocation, 0, alliedRobots);
			}			
		}
		else{
			// If a triad can be shot
			hasShot = Korosenai.tryShootAtEnemy(shootingLocation, myLocation, 1, alliedRobots);
			
			// If that was not possible, try a single shot 
			if(!hasShot){
				hasShot = Korosenai.tryShootAtEnemy(shootingLocation, myLocation, 0, alliedRobots);
			}		
		}
		return hasShot;
	}
	
	private static void readNearestEnemyLocations() throws GameActionException{
		
		int sentNumber = rc.readBroadcast(BroadcastChannels.ENEMY_LOCATIONS_SENT_THIS_TURN_CHANNEL);
		
		if (sentNumber > 0){
			
			// SYSTEM CHECK - See if the soldier has actually read a broadcast from a scout or not....
			System.out.println("Enemy locations updated by scouts.......");
			
			
			}		
		}	
}	
	
	


