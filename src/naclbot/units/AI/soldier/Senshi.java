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
	
	// ------------- GENERAL (IMPORTANT TO SELF) VARS -------------//
	
	// Variable for round number
	private static int roundNumber;
	
	// Variables for self and team recognition
	public static int myID;
	public static boolean iDied;
	public static int soldierNumber;
	public static int unitNumber;
	private static Team enemy;	
	private static Team allies;		
	private static final float strideRadius = battlecode.common.RobotType.SOLDIER.strideRadius;
	private static final float bodyRadius = battlecode.common.RobotType.SOLDIER.bodyRadius;
	
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
    public static final int attackFrequency = 25;
    
    // Gives probability of joining an attack at a particular time....
    public static final float attackProbability = (float) 1;
    
    // Int to store the number of rounds since the unit was last in a commanded mode - threshold value
    public static int lastCommanded = attackFrequency;
    
    // Variable to determine after how long soldiers decide that Alan's code is a piece of shit......
    public static final int giveUpOnRouting = 125;
    
    // Variable to store the amount of time currently in routing....
    public static int roundsRouting = 0;
    
    // Variable to store for how long the robot has been tracking something
    public static int roundsTracked = 0;
    
    // To store the last known location of a civilian
    public static MapLocation nearestCivilian;

    
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
    			boolean mustDefend = false;
    			
    			// ------------------------- RESET/UPDATE VARIABLES ----------------//        
    			
            	// Get nearby enemies and allies and bullets for use in other functions            	
            	RobotInfo[] enemyRobots = NearbyUnits(enemy);
            	RobotInfo[] alliedRobots = NearbyUnits(allies);
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
    			
    			// Check if unit actually died or not
    			if (iDied) {
    				
    				iDied = false;
    				
    				// Get own soldierNumber - important for broadcasting 
    		        soldierNumber = rc.readBroadcast(BroadcastChannels.SOLDIER_NUMBER_CHANNEL);
    		        currentNumberofSoldiers = soldierNumber + 1;
    		        
    		        unitNumber = rc.readBroadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL);
    		        rc.broadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL, unitNumber + 1);
    		        
    		        // Update soldier number for other soldiers to see.....
    		        rc.broadcast(BroadcastChannels.SOLDIER_NUMBER_CHANNEL, currentNumberofSoldiers);

    			}
            	
            	// Update location of self
            	myLocation = rc.getLocation();         	
            	
            	// Initialize the direction the robot would like to go to at any given round as the direction the robot moved previously....     	
            	myDirection = lastDirection;
            	
            	// If the robot is probably going to attempt to move straight to another unit.,.
            	if(rc.isLocationOccupied(myLocation.add(myDirection, bodyRadius + (float) 0.1))){
            		
            		float newValue = (float)(Math.random() * Math.PI - Math.PI / 2);
            		myDirection = new Direction(lastDirection.radians + (float)Math.PI + newValue);
            	}

            	// Placeholder for desired location to go to
            	MapLocation tryMove = myLocation;
            	
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
               	
             	// Param to store the location of the nearest ally for the current turn - use this to determine whether or not to break out of a tracking operation.....       	
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
             	
             	if(mustDefend){
             		isCommanded = false;
             	}

               	
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
            	MapLocation desiredMove = move(enemyRobots, tryMove);
            	
            	
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
    	    	
            	// See whether or not the robot can move to the completely processed desired move, and move if it does
            	if(rc.canMove(desiredMove)){
            		manageBeingAttacked(desiredMove);
            		rc.move(desiredMove);
            	}
            	// If the robot wasn't able to move....
            	else{
            		// SYSTEM CHECK - Make sure that the robot didn't move because it didn't want to....
            		// System.out.println("This robot did not move because it forgot to show Rem appreciation........");
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
            		
            		hasShot = decideShoot(enemyRobots, alliedRobots, alliedTrees);
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
    		// System.out.println("I am continuing to follow a normie Emilia lover with ID: " + trackID);
    		
    		// Update location of tracked robot 
    		trackedRobot = rc.senseRobot(trackID);
    		
    		// SYSTEM CHECK - Draw a VIOLET LINE between current position and position of robot
    		rc.setIndicatorLine(myLocation, trackedRobot.location, 150, 0, 200);
    		
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
        	roundsTracked = 0;
			
        	// SYSTEM CHECK - Notify of target loss
        	// System.out.println("Lost sight of target/Finding a new target");        	
        	
        	return move(enemyRobots,desiredMove);
    	}	                		
    }	       
    
    
	/******************************************************************
	******************* Miscellaneous Functions************************
	*******************************************************************/   	
	
	// Function to obtain the robot info units in the specified team
	
	private static RobotInfo[] NearbyUnits(Team team){
	
	return rc.senseNearbyRobots(myLocation, (float)10, team);
	}
	
	
	private static boolean decideShoot(RobotInfo[] enemyRobots, RobotInfo[] alliedRobots, TreeInfo[] alliedTrees) throws GameActionException{
		
		// Obtain a location to shoot at
		MapLocation shootingLocation = Korosenai.getFiringLocation(trackedRobot, previousRobotData, myLocation);
		
		// Return value to store whether or not the has fired this turn or no....
		boolean hasShot;
		
		// If there is more than one enemy nearby, attempt to fire a pentad at the location
		if(enemyRobots.length >= 4){
			
			// If a pentad can be shot...
			hasShot = Korosenai.tryShootAtEnemy(shootingLocation, myLocation, 2, alliedRobots, alliedTrees);
			
			// If that was not possible, try a triad and then a single shot 
			if(!hasShot){
				hasShot = Korosenai.tryShootAtEnemy(shootingLocation, myLocation, 1, alliedRobots, alliedTrees);
			}			
			if(!hasShot){
				hasShot = Korosenai.tryShootAtEnemy(shootingLocation, myLocation, 0, alliedRobots, alliedTrees);
			}			
		}
		else if (enemyRobots.length >= 2 || trackedRobot.type == battlecode.common.RobotType.ARCHON){
			// If a triad can be shot
			hasShot = Korosenai.tryShootAtEnemy(shootingLocation, myLocation, 1, alliedRobots, alliedTrees);
			
			// If that was not possible, try a single shot 
			if(!hasShot){
				hasShot = Korosenai.tryShootAtEnemy(shootingLocation, myLocation, 0, alliedRobots, alliedTrees);
			}		
		}
		else{
			hasShot = Korosenai.tryShootAtEnemy(shootingLocation, myLocation,0, alliedRobots, alliedTrees);
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
				// Get own soldierNumber - important for broadcasting 
		        soldierNumber = rc.readBroadcast(BroadcastChannels.SOLDIER_NUMBER_CHANNEL);
		        currentNumberofSoldiers = soldierNumber - 1;
		        
		        unitNumber = rc.readBroadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL);
		        rc.broadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL, unitNumber - 1);
		        
		        // Update soldier number for other soldiers to see.....
		        rc.broadcast(BroadcastChannels.SOLDIER_NUMBER_CHANNEL, currentNumberofSoldiers);
			}
		}
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
	
}	
	
	


