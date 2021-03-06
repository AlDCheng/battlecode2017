//
package naclbot.units.motion;
import java.util.ArrayList;

import battlecode.common.*;
import naclbot.units.motion.shoot.Korosenai;
import naclbot.variables.GlobalVars;

/* ------------------   Overview ----------------------
 * 
 * Functions for controlling movement and engagement
 *
 * ~~ Coded by Illiyia (akimn@#mit.edu)
 * 
 *  * Debug statements all begin with SYSTEM CHECK 
 *  
 *  PLEASE SAVE ME FROM THE CANCER THAT THIS CODE HAS
 *  BECOME - DONT LOOK DONT LOOK AT THE SOLDIER SHOOTING
 *  FUNCTIONS THEY WILL ACTUALLY JUST GIVE YOU CANCER
 * 
 ---------------------------------------------------- */

// This is a class that contains functions related to tracking an enemy

public class Todoruno extends GlobalVars {
	
	// -------------------- ROTATION VARIABLES AND CLASS ------------------------- //
	
	// Variables for rotation functions
	public static final int rotationAnglesCheck = 5; // Number of angles the rotate functions will check to see if they can move...
	public static final float rotationMarginOfError = (float) 1;
	
	// Class for scouts - rotation orientation class helper.....
	public static class Rotation{
		
		// Value to store rotation preference indicated by this object
		public boolean orientation;
		
		// Constructor
		public Rotation(boolean direction){
			this.orientation = direction;
		}
		
		// Function to flip the orientation of this object
		public void switchOrientation(){
			this.orientation = !this.orientation;
		}	
		
		// Function to print in words the orientation of the rotation object
		public String printOrientation(){
			
			// If the orientation is true, this is clockwise....
			if(orientation){
				return "counterclockwise";
			}
			else{
				return "clockwise";
			}			
		}
	}		
	
	// --------------------------------- COMBAT CONSTANTS -------------------------------//
	
	// Variables for charging at enemy soldiers
	public final static float minSoldierHealthCharge = 10; // Variable that describes up until when the soldiers will charge..... health
	public final static float minSoldierDistanceCharge = 3; // Variable that describes up until when the soldiers will charge..... distance
 	
	public final static int coverAngleCheckNumber = 4;
	public final static float coverAngleCheckOffset = (float) (Math.PI /(coverAngleCheckNumber * 2));
	public final static int coverDistanceCheckNumber = 2;
	
	public final static float maximumPairDirectionSeparation = (float) (Math.PI/4);
	public final static float minimumPairPentadSeparation = (float) (Math.PI/6);
	public final static float maximumPairDistanceSeparation = (float) 2.5;
	public final static float maximalNearPairDistance = (float) 4.5;
	
	// ----------------------------------------------------------------------------------//
	// -------------------------------- OLD FUNCTIONS --- -------------------------------//
	// ----------------------------------------------------------------------------------//		
	
	
	// Function to move towards a given robot

	// When the robot is 6* multiplier away or greater from the current robot being tracked, it will move directly towards the currently tracked
	// At a slightly smaller distance, the robot will attempt to move so that it can start rotating around the  tracked robot...
	// When the unit is within 4.5 * multiplier of the target robot, the robot will simply attempt to rotate around it.....
	
	public static MapLocation moveTowardsTarget(
			
		// Input Variables
		RobotInfo trackedRobot, // The robot that is currently being tracked
		
		MapLocation myLocation, // The current MapLocation of the robot
		
		float strideRadius, // The stride radius of the robot
		
		boolean rotationDirection, // Current rotation orientation of the robot - this value is true if counterclockwise
		
		MapLocation desiredMove, // The location that the robot will attempt to go to				
		
		float multiplier // Multiplier off of the original constants here that the robot will track (useful for tracking big robots)
		
		) throws GameActionException{
	
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
    			desiredMove = trackedRobot.location.add(fromDir, (float) (3* multiplier));
    			
    		} else{
    			// Calculate the direction from the target that you want to end up at
    			Direction fromDir = new Direction(dir.radians + (float) (2 * Math.PI/3));
    			
    			// Obtain the desired target location
    			desiredMove = trackedRobot.location.add(fromDir, (float) (3 * multiplier));	    			
    		}
    	// SYSTEM CHECK Print line from current location to intended move location - light blue green
    	// rc.setIndicatorLine(myLocation, desiredMove, 0, 200, 200);   			
    	} 
    	
    	return desiredMove;	
	} 
	
	
	// If there is no multiplier argument provided
	
	public static MapLocation moveTowardsTarget(RobotInfo trackedRobot, MapLocation myLocation, 
			float strideRadius, boolean rotationDirection, MapLocation desiredMove) throws GameActionException{
		
		// Return the same function with default muliplier 1
		return moveTowardsTarget(trackedRobot, myLocation, strideRadius, rotationDirection, desiredMove, 1);
	}
	
 	
	// ----------------------------------------------------------------------------------//
	// -------------------------------- TRACKING FUNCTIONS ------------------------------//
	// ----------------------------------------------------------------------------------//	
		
	
	// Function to return a new entry to follow (prioritizes combat units...
	// Various booleans controlling priority
	
	public static RobotInfo getNewEnemyToTrack(RobotInfo[] enemyRobots, MapLocation myLocation, 
			boolean targetCombat,  boolean targetScout, boolean targetGardener, boolean targetArchon) throws GameActionException{

		// Parameter to store the smallest distance to another robot
		float minimumPriorityUnitDistance = Integer.MAX_VALUE;
		float minimumTotalDistance = Integer.MAX_VALUE;
		
		// Parameters to store the data for the nearest combat unit (not including scout) and for any robot
		RobotInfo anyEnemyRobot = null;
		RobotInfo prioirityRobot = null;
				
		// Parameter to store the array index of the closest robot						
		for (RobotInfo enemyRobot: enemyRobots){
			
			// Calculate the distance to the robot in question
			float distanceTo = enemyRobot.location.distanceTo(myLocation);
			
			// If the distance to that robot is less than previous distances to any combat unit or if no combat unit is found, less than any distance to a unit
			if (distanceTo < minimumPriorityUnitDistance || (minimumPriorityUnitDistance == 0 && distanceTo < minimumTotalDistance)){
				
				// If the enemy is a combat type, and killing of combat units is allowable, track them
				if (targetCombat && enemyRobot.type == battlecode.common.RobotType.SOLDIER || enemyRobot.type == battlecode.common.RobotType.LUMBERJACK || enemyRobot.type == battlecode.common.RobotType.TANK){
					
					Direction directionToEnemy = myLocation.directionTo(enemyRobot.location);
					
					if(Korosenai.isLineBLockedByTree(myLocation, myLocation.add(directionToEnemy, 3), 1)){
						// SYSTEM CHECK - Print out that the current considered target cannot be targeted due to a tree directly in the way
						System.out.println("Discovered an enemy with ID: " + enemyRobot.ID + "But there is a tree in the way....");
						rc.setIndicatorLine(myLocation, enemyRobot.getLocation(), 33, 0, 0);
						
					}
					else{
						// Update all placeholder parameters
						minimumPriorityUnitDistance = distanceTo;
						minimumTotalDistance = distanceTo;
						anyEnemyRobot = enemyRobot;
						prioirityRobot = enemyRobot;
					}
				}
				// If the enemy is a scout and it is given that the robot may track scouts
				else if (enemyRobot.type == battlecode.common.RobotType.SCOUT && targetScout){
					// Update all placeholder parameters
					minimumTotalDistance = distanceTo;
					anyEnemyRobot = enemyRobot;
					
				}
				else if(enemyRobot.type == battlecode.common.RobotType.GARDENER && targetGardener){
					minimumPriorityUnitDistance = distanceTo;
					minimumTotalDistance = distanceTo;
					anyEnemyRobot = enemyRobot;
					prioirityRobot = enemyRobot;
				}
				// Finally if the robot is either a gardener or archon and the robot can target civilians....
				else if (enemyRobot.type == battlecode.common.RobotType.ARCHON && targetArchon){
					// Update all placeholder parameters
					minimumTotalDistance = distanceTo;
					anyEnemyRobot = enemyRobot;
				}
				else{
					// SYSTEM CHECK - Make sure that the robot is properly rejected a target
					System.out.println("The currently considered enemy with ID:" + enemyRobot.ID + " does not match input traking parameters...");
				}
			}							
		}		
		// If there is an enemy combat unit robot nearby...
		if (prioirityRobot != null){
			// Return it
			return prioirityRobot;
		}
		// Else attempt to return a valid robot or nothing if there has not been one fond....
		else{
			if (anyEnemyRobot != null){
				return anyEnemyRobot;
			}
			else{
				return null;
			}
		}			
	}		
	
	// Simple function to return the information regarding the nearest gardener to the lumberjack at present....
	
	public static RobotInfo getNearestGardener(RobotInfo[] nearbyRobots, MapLocation myLocation){
		
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
	
 	
	// ----------------------------------------------------------------------------------//
	// -------------------------------- COMBAT MOVEMENT ---------------------------------//
	// ----------------------------------------------------------------------------------//	
	
	
	// Function used primarily to fight lumberjacks - kites away..........
	
	public static MapLocation engageEnemy(
		
		// Input variables....
		MapLocation startingLocation, // The current location of the robot....
		
		RobotInfo enemyRobot, // The enemyRobot to be engaged
		
		float strideRadius, // Float representing the distance the robot can travel in one turn... 																																					
		
		float engageDistance // Float representing the distance that the robot will attempt to keep between itself and the robot near it....

		) throws GameActionException{
	
		
		// Pseudo random number to determine which side the soldier will check first.......
		float randomNumber = (float) Math.random();
		
		// Retrieve the location of the enemy robot and the robot's current distance away from it and the direction from the engaged enemy..
		MapLocation enemyLocation = enemyRobot.location;
		Direction directionFrom = enemyLocation.directionTo(startingLocation);	
		
		// Variable to determine the number of radians that a soldier's move would intercept along the circle of size engageDistance
		float interceptRadians = strideRadius / engageDistance;

		// Change the order of rotations to check by negating the offset angle given by interceptRadians.....
		if (randomNumber >= 0.5){		
			interceptRadians *= -1;
		}
	
		// Calculate the direction from the engaged robot that is equivalent to a rotation about the target robot of interceptRadians
		Direction targetDirection1 = new Direction(directionFrom.radians + interceptRadians);
		MapLocation desiredLocation1 = enemyLocation.add(targetDirection1, engageDistance);
		
		// Calculate the direction from the current location to the considered location
		Direction attemptedDirection1 = startingLocation.directionTo(desiredLocation1);
	
		// Attempt to move in that general direction.....
		MapLocation moveAttempt1 = Yuurei.tryMoveInDirection(attemptedDirection1, 20, 2, strideRadius, startingLocation);
		
		// If a move to that location was possible, return it.....
		if (moveAttempt1 != null){
			
			// SYSTEM CHECK - Print out that a move was found and draw a PINK LINE
			System.out.println("First attempt to find an engage location successful");
			
			rc.setIndicatorLine(startingLocation, moveAttempt1, 255, 20, 147);
			
			return moveAttempt1;
		}
		
		// If the function still has yet to return.......
			
		// Calculate the direction from the engaged robot that is equivalent to a rotation about the target robot of interceptRadians
		Direction targetDirection2 = new Direction(directionFrom.radians - interceptRadians);
		MapLocation desiredLocation2 = enemyLocation.add(targetDirection2, engageDistance);
		
		// Calculate the direction from the current location to the considered location
		Direction attemptedDirection2 = startingLocation.directionTo(desiredLocation2);
	
		// Attempt to move in that general direction.....
		MapLocation moveAttempt2 = Yuurei.tryMoveInDirection(attemptedDirection2, 20, 2, strideRadius, startingLocation);
		
		// If a move to that location was possible, return it.....
		if (moveAttempt2 != null){			
			
			// SYSTEM CHECK - Print out that a move was found and draw a PINK LINE
			System.out.println("Second attempt to find an engage location successful");
			
			rc.setIndicatorLine(startingLocation, moveAttempt2, 255, 20, 147);
			
			return moveAttempt2;
		}	
		
		// If the function still cannot return, simply return nothing... the robot will prioritize staying in the same location......
		return null;
	}
	
	// Function utilized when engaging a civilian - runs up towards the enemy always...
	
	
	public static MapLocation engageCivilian(
			
		// Input variables....
		MapLocation startingLocation, // The current location of the robot....
		
		RobotInfo enemyRobot, // The enemyRobot to be engaged
		
		float strideRadius // Float representing the distance the robot can travel in one turn... 																												
	
		) throws GameActionException{ 
		
		// SYSTEM CHECK - Print out that the robot is attempting to find a location to shoot the civilian from.....
		System.out.println("Attempting to find a location to shoot the gardener/Archon from....");
		
		Direction directionTo = startingLocation.directionTo(enemyRobot.location);
			
		for (int i = 5; i >= 1; i--){
			
			float distanceCheck = i * strideRadius / 5;
			
			for (int j = 0; j <= 6; j ++){
				
				Direction directionCheck1 = new Direction((float) (directionTo.radians + j * Math.PI / 6));
				Direction directionCheck2 = new Direction((float) (directionTo.radians - j * Math.PI / 6));
				
				MapLocation testLocation1 = startingLocation.add(directionCheck1, distanceCheck);
				MapLocation testLocation2 = startingLocation.add(directionCheck2, distanceCheck);
				
				if(!Korosenai.isLineBLockedByTree(testLocation1, enemyRobot.location, 2)){
					
					// SYSTEM CHECK - Show the location to shoot from as a green dot....
					rc.setIndicatorDot(testLocation1, 0, 255, 0);
					return testLocation1;				
				}
				if(!Korosenai.isLineBLockedByTree(testLocation2, enemyRobot.location, 2)){
					
					// SYSTEM CHECK - Show the location to shoot from as a green dot....
					rc.setIndicatorDot(testLocation2, 0, 255, 0);
					return testLocation2;				
				}				
			}		
		}	
		return startingLocation;		
	}
	
	
	// Function used specifically when engaging an enemy soldier..............
	
	public static MapLocation engageSoldier(
			
		// Input Variables
		MapLocation startingLocation, // The current location of the robot
		
		RobotInfo enemySoldier, // The RobotInfo of the enemy soldier....
		
		float strideRadius, // The stride radius of the robot.........		
		float bodyRadius, // The body radius of the robot...................		
		float sensorRadius, // The sensor ardius of the robot
		
		RobotInfo[] nearbyEnemies, // The RobotInfo of all nearby soldiers.............			
		RobotInfo[] nearbyAllies, // The RobotInfo of all nearby allies...		
		TreeInfo[] nearbyTrees, // The location of any nearby trees....
		
		ArrayList<RobotInfo> nearestEnemySoldiers, // List of all nearby soldiers of enemy team
		ArrayList<RobotInfo> nearestAlliedSoldiers // List of all nearby soldiers of allied team
		
		) throws GameActionException{
		
		// SYSTEM CHECK - Make sure that the soldier knows that it is engaging a soldier........... Print out a crimson dot on the soldier.....
		System.out.println("Engaging an enemy soldier with ID: " + enemySoldier.ID);		
		rc.setIndicatorDot(startingLocation, 220, 20, 60);
		
		// Get array lists of all nearby allied and enemy soldiers..........		
		if(nearestEnemySoldiers == null){
			nearestEnemySoldiers = Chirasou.getNearestSoldiers(nearbyEnemies, startingLocation);
		}
		if(nearestAlliedSoldiers == null){
			nearestAlliedSoldiers = Chirasou.getNearestSoldiers(nearbyAllies, startingLocation);
		}
		// Retrieve the location of the enemy soldier.... and the direction to it
		MapLocation enemyLocation = enemySoldier.location;
		Direction enemyDirection = startingLocation.directionTo(enemyLocation);
		
		// Retrieve whether or not there are trees in the way.....
		TreeInfo treesInWay = Chirasou.treesInDirection(startingLocation, enemyDirection, nearbyTrees, bodyRadius, strideRadius);
		TreeInfo treesBehind = Chirasou.treesInOppositeDirection(startingLocation, enemyDirection, nearbyTrees, bodyRadius, strideRadius);
		
		// Get a list of allied trees to avoid shooting..
		TreeInfo[] alliedTrees = rc.senseNearbyTrees(-1, rc.getTeam());
		
		float enemyHealth = enemySoldier.health;
		float currentHealth = rc.getHealth();
		
		// SYSTEM CHECK - Print out the number of nearby enemy soldiers and allied soldiers.....
		System.out.println("Number of nearby enemy soldiers: " + nearestEnemySoldiers.size() + " allied soldiers: " + nearestAlliedSoldiers.size());
		
		// If there is only one enemy soldier and the line to shoot is free (and there are no trees within the vicinity and the robot is fairly healthy...
		if ((nearestEnemySoldiers.size() == 1 && nearestAlliedSoldiers.size() <= 2) && !Korosenai.isLineBLockedByTree(startingLocation, enemyLocation, 1)
				&& treesInWay == null && (currentHealth >= minSoldierHealthCharge && startingLocation.distanceTo(enemyLocation) >= minSoldierDistanceCharge && currentHealth >= enemyHealth + 10)){
			
			// SYSTEM CHECK - Draw a Yellow line to the target enemy...........
			rc.setIndicatorLine(startingLocation, enemyLocation, 255, 255, 0);
			
			return chargeSoldier(startingLocation, enemySoldier, strideRadius, nearbyAllies, alliedTrees);
		}
		
		// If the robot is in low health or is too close to the enemy, of which there is only one, shoot forward while moving backwards....
		else if((nearestEnemySoldiers.size() == 1 && nearestAlliedSoldiers.size() <= 2) && !Korosenai.isLineBLockedByTree(startingLocation, enemyLocation, 1) 
				&& treesBehind == null && (currentHealth < minSoldierHealthCharge || startingLocation.distanceTo(enemyLocation) < minSoldierDistanceCharge || currentHealth < enemyHealth + 10)){
			
			// SYSTEM CHECK - Draw an orange line to the target enemy...........
			rc.setIndicatorLine(startingLocation, enemyLocation, 255, 165, 0);
			
			return fadeAwayFromSoldier(startingLocation, enemySoldier, strideRadius, nearbyAllies, alliedTrees);
			
		}
		// If there is a tree in the way and there are one or two soldiers nearby and there is a tree in the way, that the soldier can hide behind......
		else if ((nearestEnemySoldiers.size() <= 2 && nearestAlliedSoldiers.size() <= 2) && !Korosenai.isLineBLockedByTree(startingLocation, enemyLocation, 1) && treesInWay != null){
			
			// SYSTEM CHECK - Draw a Light green line to the target enemy...........
			rc.setIndicatorLine(startingLocation, enemyLocation, 152, 251, 152);
			
			return shootWithCover(startingLocation, enemySoldier, strideRadius, bodyRadius, nearbyAllies, alliedTrees);			
		}
		// If there is simply a pair of enemies to deal with.......
		else if((nearestEnemySoldiers.size() == 2  && nearestAlliedSoldiers.size() <= 2)){
			
			return engagePair(startingLocation, enemySoldier, nearestEnemySoldiers, nearestAlliedSoldiers, strideRadius, bodyRadius, sensorRadius, 
					nearbyEnemies, nearbyAllies, nearbyTrees, alliedTrees, treesInWay, treesBehind);			
		}
		// For all other cases.....
		else{
			return engageEnemy(startingLocation, enemySoldier, strideRadius, sensorRadius -1);
		}
	}	
	
	
	// Function to run at the soldier and fire pentads........... 
	// Assumes that there are no trees in the way between the soldier and the enemy soldier being charged
	
	public static MapLocation chargeSoldier(MapLocation startingLocation, RobotInfo enemySoldier, 
			float strideRadius, RobotInfo[] nearbyAllies, TreeInfo[] alliedTrees) throws GameActionException{
		
		// Retrieve the location of the enemy soldier....
		MapLocation enemyLocation = enemySoldier.location;		
		
		// Value to add some randomized to inject randomness into firing...
		float randomize = (float) ((Math.random() * Math.PI / 6) - Math.PI/12); 
		 
		// SYSTEM CHECK - Print out the randomized value.....
		System.out.println("Randomized value: " + randomize);
		
		// Direction to..........
		Direction directionTo = startingLocation.directionTo(enemyLocation);
		Direction directionToSoldier = new Direction(directionTo.radians + randomize);		
		
		MapLocation desiredMove = null;
		
		// SYSTEM CHECK - Print out that the soldier will attempt to charge.....
		System.out.println("Will attempt to charge");
		
		for(int i = 1; i <= 10; i ++){
			
			if (rc.canMove(startingLocation.add(directionTo, strideRadius * i / 10))){
				desiredMove = startingLocation.add(directionTo, strideRadius * i / 10);
			}			
		}
		
		if(desiredMove != null){
			
			// If the soldier can move in the target direction......
			rc.move(desiredMove);
			
			// Check to see if the robot can fire the pentad......
				// Only fires pentad if no other allies will get hit and that the robot is sufficiently close to the enemy........
			if(Korosenai.canFirePentad(directionToSoldier, startingLocation, nearbyAllies, alliedTrees) && desiredMove.distanceTo(enemyLocation) <= 4){
				
				// Fire the pentad!
				rc.firePentadShot(directionToSoldier);
			}
			// If the robot cannot fire the pentad.....
			else{
				
				// Check to see if the robot can fire a triad.....
				if(Korosenai.canFireTriad(directionToSoldier, startingLocation, nearbyAllies, alliedTrees) && desiredMove.distanceTo(enemyLocation) <= 7){
					
					// Fire the triad
					rc.fireTriadShot(directionToSoldier);
				}
				// If the robot cannot fire a triad shot for some rason....
				else{					
					// Check to see if the robot can fire a single...
					if(Korosenai.canFireSingle(directionToSoldier, startingLocation, nearbyAllies, alliedTrees)){
						
						// Fire the triad
						rc.fireSingleShot(directionToSoldier);
					}				
				}
			}
			return desiredMove;
		}			
		// If the robot was unable to move towards the enemy soldier..........
		return startingLocation;
	}
	
	// Function to move away from soldier while shooting away......
	
	public static MapLocation fadeAwayFromSoldier(MapLocation startingLocation, RobotInfo enemySoldier, 
			float strideRadius, RobotInfo[] nearbyAllies, TreeInfo[] alliedTrees) throws GameActionException{
		
		// Retrieve the location of the enemy soldier....
		MapLocation enemyLocation = enemySoldier.location;		
		
		// Value to add randomness to retreat direction and the firing pattern....
		float randomize = (float) ((Math.random() * Math.PI / 6) - Math.PI/12); 
		
		// SYSTEM CHECK - Print out the randomized value.....
		System.out.println("Randomized value: " + randomize);
		
		// Get the direction away from the enemy...
		Direction directionFrom = enemyLocation.directionTo(startingLocation);
		
		// Get a direction to move away from the enemy.... (This should be due to check on trees behind.....)
		Direction directionAway = new Direction(directionFrom.radians + 3 * randomize);
		
		Direction directionTo = startingLocation.directionTo(enemyLocation);
		Direction directionToSoldier = new Direction(directionTo.radians + randomize);				
		
		MapLocation desiredMove = null;
		
		// SYSTEM CHECK - Print out that the soldier will attempt to track back after shooting...
		System.out.println("Will attempt to track back");
		
		for(int i = 1; i <= 10; i ++){
			
			if (rc.canMove(startingLocation.add(directionAway, strideRadius * i / 10))){
				desiredMove = startingLocation.add(directionAway, strideRadius * i / 10);
			}			
		}
		
		// If no direction was found in that manner, try something else......
		if(desiredMove == null){
			
			// Get the opposite offset from direction from the enemy.....
			directionAway = new Direction(directionFrom.radians - 3 * randomize);
			
			for(int i = 1; i <= 10; i ++){
				
				if (rc.canMove(startingLocation.add(directionAway, strideRadius * i / 10))){
					desiredMove = startingLocation.add(directionAway, strideRadius * i / 10);
				}			
			}
		}
		
		if(desiredMove != null){			
			
			// Check to see if the robot can fire the pentad......
				// Only fires pentad if no other allies will get hit and that the robot is sufficiently close to the enemy........
			if(Korosenai.canFirePentad(directionToSoldier, startingLocation, nearbyAllies, alliedTrees) && desiredMove.distanceTo(enemyLocation) <= 4){
				
				// Fire the pentad!
				rc.firePentadShot(directionToSoldier);
			}
			// If the robot cannot fire the pentad.....
			else{
				
				// Check to see if the robot can fire a triad.....
				if(Korosenai.canFireTriad(directionToSoldier, startingLocation, nearbyAllies, alliedTrees) && desiredMove.distanceTo(enemyLocation) <= 7){
					
					// Fire the triad
					rc.fireTriadShot(directionToSoldier);
				}
				// If the robot cannot fire a triad shot for some rason....
				else{					
					// Check to see if the robot can fire a single...
					if(Korosenai.canFireSingle(directionToSoldier, startingLocation, nearbyAllies, alliedTrees)){
						
						// Fire the triad
						rc.fireSingleShot(directionToSoldier);
					}				
				}
			}
			
			// DO NOT Move prior to this.... just return the movement before firing......
			return desiredMove;
		}			
		// If the robot was unable to move towards the enemy soldier..........
		return startingLocation;
	}
	
	// Function to shoot at an enemy and then find cover...........	
	// Assumes that there is a tree in front of the soldier......
	
	public static MapLocation shootWithCover(MapLocation startingLocation, RobotInfo enemySoldier, 
			float strideRadius, float bodyRadius, RobotInfo[] nearbyAllies, TreeInfo[] alliedTrees) throws GameActionException{
		
		// SYSTEM CHECK - Print out that the soldier will attempt to find cover after shooting...
		System.out.println("Will attempt to find cover.....");
		
		// Retrieve the location of the enemy soldier....
		MapLocation enemyLocation = enemySoldier.location;		
		
		// Value to add some randomized to inject randomness into firing...
		float randomize = (float) ((Math.random() * Math.PI / 6) - Math.PI/12); 
		
		// SYSTEM CHECK - Print out the randomized value.....
		System.out.println("Randomized value: " + randomize);
		
		// Direction to..........
		Direction directionTo = startingLocation.directionTo(enemyLocation);
		Direction directionToSoldier = new Direction(directionTo.radians + randomize);	
		
		// Attempt to shoot first before moving.......
		
			// Check to see if the robot can fire the pentad......
			// Only fires pentad if no other allies will get hit and that the robot is sufficiently close to the enemy........
		if(Korosenai.canFirePentad(directionToSoldier, startingLocation, nearbyAllies, alliedTrees) && startingLocation.distanceTo(enemyLocation) <= 5){
			
			// Fire the pentad!
			rc.firePentadShot(directionToSoldier);
		}
		// If the robot cannot fire the pentad.....
		else{
			
			// Check to see if the robot can fire a triad.....
			if(Korosenai.canFireTriad(directionToSoldier, startingLocation, nearbyAllies, alliedTrees)){
				
				// Fire the triad
				rc.fireTriadShot(directionToSoldier);
			}
			// If the robot cannot fire a triad shot for some rason....
			else{					
				// Check to see if the robot can fire a single...
				if(Korosenai.canFireSingle(directionToSoldier, startingLocation, nearbyAllies, alliedTrees)){
					
					// Fire the triad
					rc.fireSingleShot(directionToSoldier);
				}				
			}
		}
		
		// Find a location to seek cover........
		for(int j = coverDistanceCheckNumber; j >= 1; j --){
			
			// Get a distance to check.....
			float distanceCheck = (strideRadius / coverDistanceCheckNumber) * j;
			
			// Iterate over each angle to check, making sure that the angles to go to are away from the current bullets fired....
			for (int i = 0; i <= coverAngleCheckNumber; i ++){
				
				// Obtain the directions to check
				Direction directionCheck1 = new Direction((float) (directionTo.radians + Math.PI / 2 + i * coverAngleCheckOffset));				
				Direction directionCheck2 = new Direction((float) (directionTo.radians + Math.PI / 2 - i * coverAngleCheckOffset));
				
				// Obtain the locations to check
				MapLocation locationCheck1 = startingLocation.add(directionCheck1, distanceCheck);
				MapLocation locationCheck2 = startingLocation.add(directionCheck2, distanceCheck);
				
				Direction directionFromCheck1 = locationCheck1.directionTo(enemyLocation);
				Direction directionFromCheck2 = locationCheck2.directionTo(enemyLocation);
				
				// Check if the location is valid - if the robot is able to move there....
				if(rc.canMove(locationCheck1)){
					
					// Check if the line from the desired location to the enemy's location is blocked by a tree.....
					if(Korosenai.isLineBLockedByTree(locationCheck1.add(directionFromCheck1, bodyRadius + (float) 0.05), 
							locationCheck1.add(directionFromCheck1, bodyRadius + 2 + (float) 0.05), 1)){
						
						// Move to the location
						rc.move(locationCheck1);
						
						// SYSTEM CHECK - Draw a purple dot to indicate a location that the robot used to find cover....
						rc.setIndicatorDot(locationCheck1, 139, 0, 139);
						
						// Return to exit the above loops :P
						return locationCheck1;						
					}
					// If the location wasn't valid....
					else{
						// SYSTEM CHECK - Draw a white dot to show where the robots couldn't find cover.....
						rc.setIndicatorDot(locationCheck1, 255, 255, 255);
					}					
				}
				// Check if the location is valid - if the robot is able to move there....
				if(rc.canMove(locationCheck2)){
					
					// Check if the line from the desired location to the enemy's location is blocked by a tree.....
					if(Korosenai.isLineBLockedByTree(locationCheck2.add(directionFromCheck2, bodyRadius + (float) 0.05), 
							locationCheck2.add(directionFromCheck2, bodyRadius + 2 + (float) 0.05), 1)){
						// Move to the location
						rc.move(locationCheck2);
						
						// SYSTEM CHECK - Draw a purple dot to indicate a location that the robot used to find cover....
						rc.setIndicatorDot(locationCheck2, 139, 0, 139);
						
						// Return to exit the above loops :P
						return locationCheck2;						
					}
					// If the location wasn't valid....
					else{
						// SYSTEM CHECK - Draw a white dot to show where the robots couldn't find cover.....
						rc.setIndicatorDot(locationCheck1, 255, 255, 255);
					}
				}
					
			}
		}

		return startingLocation;
	}
	
	public static MapLocation engagePair(			
			
			MapLocation startingLocation, RobotInfo enemySoldier, ArrayList<RobotInfo> enemySoldiers, ArrayList<RobotInfo> alliedSoldiers,
			float strideRadius, float bodyRadius, float sensorRadius, RobotInfo[] nearbyEnemies, RobotInfo[] nearbyAllies, 
			TreeInfo[] nearbyTrees, TreeInfo[] alliedTrees, TreeInfo treesInWay, TreeInfo treesBehind) throws GameActionException{
		
		// Retrieve the locations of the two soldiers....
		MapLocation location1 = enemySoldiers.get(0).location;
		MapLocation location2 = enemySoldiers.get(1).location;
		
		// Retrieve the distances to the two soldiers......
		float distance1 = startingLocation.distanceTo(location1);
		float distance2 = startingLocation.distanceTo(location2);
		
		// Get the difference in distances between the two units
		float diffDistance = Math.abs(distance1 - distance2);
		float minimumDistance = Math.min(distance1, distance2);
		
		// Get the directions to the two units
		Direction direction1 = startingLocation.directionTo(location1);		
		Direction direction2 = startingLocation.directionTo(location2);
		
		// Get the direction in the centre of the two units
		Direction middleDirection = new Direction((float) ((direction1.radians + Math.PI * 2) % (Math.PI * 2) + (direction2.radians + Math.PI * 2) % (Math.PI * 2)) / 2);
		Direction directionAway = middleDirection.opposite();
		
		// Initialize a move to make.....
		MapLocation desiredMove = null;
		
		// If the angular distance between the two units is small enough so that the soldier could reliably shoot both....
		if(Math.abs(direction1.radians - direction2.radians) <= maximumPairDirectionSeparation
				&& diffDistance <= maximumPairDistanceSeparation && minimumDistance >= maximalNearPairDistance && treesInWay == null){
			
			if(Math.abs(direction1.radians - direction2.radians) <= minimumPairPentadSeparation){
				
				// SYSTEM CHECK - Draw a line to the two enemies in SEA GREEN
				rc.setIndicatorLine(startingLocation, location1, 32, 178, 170);
				rc.setIndicatorLine(startingLocation, location2, 32, 178, 170);
				
				// SYSTEM CHECK - Draw a MEDIUMM BLUE line towards the middle location....
				rc.setIndicatorLine(startingLocation, startingLocation.add(middleDirection, 10), 30, 144, 255);
				
				// Check to see if the robot can fire a triad.....
				if(Korosenai.canFireTriad(middleDirection, startingLocation, nearbyAllies, alliedTrees)){
					
					// Fire the triad
					rc.fireTriadShot(middleDirection);
				}
				// If the robot cannot fire a triad shot for some rason....
				else{					
					// Check to see if the robot can fire a single...
					if(Korosenai.canFireSingle(middleDirection, startingLocation, nearbyAllies, alliedTrees)){
						
						// Fire the triad
						rc.fireSingleShot(middleDirection);
					}				
				}				
			}
			else{
				
				// SYSTEM CHECK - Draw a line to the two enemies in TEAL
				rc.setIndicatorLine(startingLocation, location1, 0, 128, 128);	
				rc.setIndicatorLine(startingLocation, location2, 0, 128, 128);	
				
				// SYSTEM CHECK - Draw a MEDIUMM BLUE line towards the middle location....
				rc.setIndicatorLine(startingLocation, startingLocation.add(middleDirection, 10), 30, 144, 255);
				
				if(Korosenai.canFirePentad(middleDirection, startingLocation, nearbyAllies, alliedTrees)){
					
					// Fire the pentad!
					rc.firePentadShot(middleDirection);
				}
				// If the robot cannot fire the pentad.....
				else{
					
					// Check to see if the robot can fire a triad.....
					if(Korosenai.canFireTriad(middleDirection, startingLocation, nearbyAllies, alliedTrees)){
						
						// Fire the triad
						rc.fireTriadShot(middleDirection);
					}
					// If the robot cannot fire a triad shot for some rason....
					else{					
						// Check to see if the robot can fire a single...
						if(Korosenai.canFireSingle(middleDirection, startingLocation, nearbyAllies, alliedTrees)){
							
							// Fire the triad
							rc.fireSingleShot(middleDirection);
						}				
					}
				}	
			}
			
			// If there is nothing blocking the way behind..... attempt to move back
			if(treesBehind == null){
				
				// SYSTEM CHECK - Print out that the soldier will attempt to track back after shooting...
				System.out.println("Will attempt to track back");
				
				for(int i = 1; i <= 10; i ++){
					
					if (rc.canMove(startingLocation.add(directionAway, strideRadius * i / 10))){
						desiredMove = startingLocation.add(directionAway, strideRadius * i / 10);
					}			
				}
				// If the soldier can indeed track backwards, do so.......
				if(desiredMove != null){
					
					// If the soldier can move in the target direction......
					rc.move(desiredMove);
					return desiredMove;
				}
				// If there was no location for the robot to go to..
				else{
					
					// SYSTEM CHECK - Print out that the unit couldn't move back even though the back check said otherwise....
					System.out.println("Backtracking unsuccesfully executed... ");
					
					return startingLocation;
				}
			}
			else{
				
				// Attempt a random move in the general backwards direction.....
				MapLocation moveAttempt = Yuurei.tryMoveInDirection(directionAway, 15, 6, strideRadius, startingLocation);
				
				// If the robot could successfully move backwards.....
				if(moveAttempt!= null){
					
					// SYSTEM CHECk - Print out that the move was succesful...
					System.out.println("Found a way to move backwards....");
					
					// Return the successful move 
					return moveAttempt;
				}
				else{
					
					// Otherwise return starting location....
					return startingLocation;
				}
				
			}
		}
		else if (diffDistance > maximumPairDistanceSeparation){
			
			if (distance1 > distance2){
				
				// SYSTEM CHECK - Draw a pink indicator light to the discarded location.......
				rc.setIndicatorLine(startingLocation, location1, 255, 192, 203);
				
				// Remove the entry from the arraylist and re run the engageSoldier command....
				enemySoldiers.remove(0);
				
				return engageSoldier(startingLocation, enemySoldier, strideRadius, bodyRadius, sensorRadius, 
						nearbyEnemies, nearbyAllies, nearbyTrees, enemySoldiers, alliedSoldiers);
			}
			else{
				// SYSTEM CHECK - Draw a pink indicator light to the discarded location.......
				rc.setIndicatorLine(startingLocation, location2, 255, 192, 203);
				
				// Remove the entry from the arraylist and re run the engageSoldier command....
				enemySoldiers.remove(1);
				
				return engageSoldier(startingLocation, enemySoldier, strideRadius, bodyRadius, sensorRadius, 
						nearbyEnemies, nearbyAllies, nearbyTrees, enemySoldiers, alliedSoldiers);
			}			
		}
		else{
			
			// SYSTEM CHECK - Print that the pair engage will be dealt with normally...
			System.out.println("Pair engagement should be done normally..... Calling engageEnemy... ");
			
			return engageEnemy(startingLocation, enemySoldier, strideRadius, sensorRadius -1);
		}
	}	

 	
	// ----------------------------------------------------------------------------------//
	// -------------------------------- SCOUTING FUNCTIONS -------------------------------//
	// ----------------------------------------------------------------------------------//	
	
	
	
	// This function should be called whenever the unit is close to pass by distance of the robot..... TODO
	
	public static MapLocation passByEnemy(	
			
		// Input variables....
		MapLocation startingLocation, // The current location of the robot....			
		MapLocation targetLocation, // The location that the robot intends to go to....
		
		RobotInfo enemyRobot, // The enemyRobot to be ignored
		
		float strideRadius, // Float representing the distance the robot can travel in one turn... 																																					
		
		float passByDistance, // Float representing the distance that the robot will attempt to keep between itself and the robot near it....
		
		Rotation rotationOrientation // Represents if the unit currently forced into 
		
		) throws GameActionException{
		
		// SYSTEM CHECK  Draw a VIOLET RED line to the target location.......
		rc.setIndicatorLine(startingLocation, targetLocation, 199, 21, 133);
		
		// SYSTEM CHECK - Print out that the robot is attempting to pass by...
		System.out.println("Attempting to pass by an enemy with ID: " + enemyRobot.ID);
		
		
		// Get the direction to the target location
		Direction directionToTarget = startingLocation.directionTo(targetLocation);
		
		// Get the directions to the enemy....
		Direction directionToEnemy = startingLocation.directionTo(enemyRobot.location);		
		
		// Get the direction to the enemy....
		float distanceToEnemy = startingLocation.distanceTo(enemyRobot.location);
		
		// Placeholder variable for the distance at which the robot is to pass by the enemy....
		float passDistance;
		
		// If the robot is further than the desired distance from the enemy, use its slightly larger distance.....
		if(distanceToEnemy >= passByDistance && distanceToEnemy<= passByDistance + rotationMarginOfError){
			passDistance = distanceToEnemy;
		}
		// Otherwise default to the pass by distance inputted into the function...
		else{
			passDistance = passByDistance;
		}
		
		// Angle to rotate about the target.....
		float angleToRotate = strideRadius/passDistance;		
		
		System.out.println((directionToTarget.radians + (float)(2 * Math.PI) % (2 * Math.PI)));
		System.out.println((directionToEnemy.radians + (float)(2 * Math.PI)) % (2 * Math.PI));
		System.out.println((directionToEnemy.radians + (float)(2 * Math.PI)) % (2 * Math.PI) - Math.PI / 2);
		System.out.println((directionToEnemy.radians + (float)(2 * Math.PI)) % (2 * Math.PI) + Math.PI / 2);

		
		// Rotate clockwise if....
		if((directionToTarget.radians + (float)(2 * Math.PI)) % (2 * Math.PI) > (directionToEnemy.radians + (float)(2 * Math.PI)) % (2 * Math.PI)){
			
			// SYSTEM CHECK - Print out that the robot will attempt to go clockwise
			System.out.println("Attempting to go clockwise about the target.....");
			
			rotationOrientation.orientation = false;
			
			if ((directionToTarget.radians + (float)(2 * Math.PI)) % (2 * Math.PI) > (directionToEnemy.radians + (float)(2 * Math.PI)) % (2 * Math.PI) + Math.PI / 2){
				
				// SYSTEM CHECK - The unit should simply attempt to continue to the target location....	 - Print out that
				System.out.println("Sufficiently rotated about enemy, will now proceed to target location");
				
				return startingLocation.add(directionToTarget, strideRadius);				
			}
			else{
				// SYSTEM CHECK - Print out that the scout will attempt to rotate about enemy...
				System.out.println("Attempting to rotate about enemy now....");
				
				// Otherwise simply rotate about the enemy in the correct direction
				return rotateAboutEnemy(startingLocation, enemyRobot, strideRadius, passDistance, rotationOrientation);				
			}			
		}
		else{
			// SYSTEM CHECK - Print out that the robot will attempt to go counterclockwise
			System.out.println("Attempting to go counterclockwise about the target.....");
			
			rotationOrientation.orientation = true;
			
			if ((directionToTarget.radians + (float)(2 * Math.PI)) % (2 * Math.PI) < (directionToEnemy.radians + (float)(2 * Math.PI)) % (2 * Math.PI) - Math.PI / 2){
				
				// SYSTEM CHECK - The unit should simply attempt to continue to the target location....	 - Print out that
				System.out.println("Sufficiently rotated about enemy, will now proceed to target location");
				
				return startingLocation.add(directionToTarget, strideRadius);				
			}
			else{
				// SYSTEM CHECK - Print out that the scout will attempt to rotate about enemy...
				System.out.println("Attempting to rotate about enemy now....");
				
				// Otherwise simply rotate about the enemy in the correct direction
				return rotateAboutEnemy(startingLocation, enemyRobot, strideRadius, passDistance, rotationOrientation);				
			}			
		}
	}		
	
	// TODO - for scouts.......
	
	
	public static MapLocation rotateAboutEnemy(
		
		// Input variables....
		MapLocation startingLocation, // The current location of the robot....
		
		RobotInfo enemyRobot, // The enemyRobot to be engaged
		
		float strideRadius, // Float representing the distance the robot can travel in one turn... 																																					
		
		float engageDistance, // Float representing the distance that the robot will attempt to keep between itself and the robot near it....
		
		Rotation rotationOrientation // Represents if the unit currently forced into 

		) throws GameActionException{	
				

		// SYSTEM CHECK - Draw a pink line to the enemy that the robot is attempting to rotate about...
		rc.setIndicatorLine(startingLocation, enemyRobot.location, 255, 20, 147);
			
		// Get the location of the enemy, the distance from it
		MapLocation enemyLocation = enemyRobot.location;		
		float distanceFromEnemy = startingLocation.distanceTo(enemyLocation);
		
		// Place holder to store the distance at which the robot will attempt to rotate about the enemy....
		float distanceSpacing;
		
		// If the current spacing is larger, use it
		if(distanceFromEnemy >= engageDistance && distanceFromEnemy <= rotationMarginOfError + engageDistance){
			distanceSpacing = distanceFromEnemy;
		}
		
		// If the current distance is even larger than the margin of error use the upper bound...
		else if(distanceFromEnemy >= engageDistance + rotationMarginOfError){
			distanceSpacing = engageDistance + rotationMarginOfError;			
		}
		
		// Otherwise utilize the inputed distance
		else{
			distanceSpacing = engageDistance;
		}
		
		// Get the direction from the enemy and the maximum number of radians to rotate about it....
		Direction directionFromEnemy = enemyLocation.directionTo(startingLocation);		
		float radiansToRotate = strideRadius / distanceFromEnemy;
		
		// If the rotation distance wasn't yet given...... randomly select one..
		if(rotationOrientation == null){
			
			// Randomizing variable
			float randomize = (float) Math.random();
			
			if (randomize >= 0.5){
				rotationOrientation = new Rotation(true);
			}			
			else{
				rotationOrientation = new Rotation(false);				
			}		
		}
		
		// Positive orientation means counterclockwise rotation.....
		if(rotationOrientation.orientation){
			
			Direction finalDirection = new Direction(directionFromEnemy.radians + radiansToRotate);
			
			// Check five different angles for possible movement...
			for(int j = rotationAnglesCheck; j >= 1; j--){
				
				float radianCheck = radiansToRotate * j / rotationAnglesCheck;				
				
				Direction targetDirection = new Direction(directionFromEnemy.radians + radianCheck);
				
				MapLocation targetLocation = enemyLocation.add(targetDirection, distanceSpacing);
				
				if(rc.canMove(targetLocation)){
					
					// SYSTEM CHECK - Print out the specifics of the location it chose.....
					System.out.println("Will attempt to move counterclockwise with: " + radianCheck + " radians about the enemy with a distance of: " + distanceSpacing);
					return targetLocation;
				}
			}
			// SYSTEM CHECK - Print out the specifics of the location it chose.....
			System.out.println("Cannot seem to move in desired direction, will simply return full value and corect....");
			
			// If none of the rotation options were viable... return the full one so that a correction can be made.....
			return enemyLocation.add(finalDirection, distanceSpacing);
		}
		// Otherwise attempt clockwise rotation.......
		else{
			
			Direction finalDirection = new Direction(directionFromEnemy.radians - radiansToRotate);
			
			// Check five different angles for possible movement...
			for(int j = rotationAnglesCheck; j >= 1; j--){
				
				float radianCheck = radiansToRotate * j / rotationAnglesCheck;				
				
				Direction targetDirection = new Direction(directionFromEnemy.radians - radianCheck);
				
				MapLocation targetLocation = enemyLocation.add(targetDirection, distanceSpacing);
				
				if(rc.canMove(targetLocation)){
					
					// SYSTEM CHECK - Print out the specifics of the location it chose.....
					System.out.println("Will attempt to move clockwise with: " + radianCheck + " radians about the enemy with a distance of: " + distanceSpacing);
					return targetLocation;
				}				
			}
			// SYSTEM CHECK - Print out the specifics of the location it chose.....
			System.out.println("Cannot seem to move in desired direction, will simply return full value and corect....");
			
			// If none of the rotation options were viable... return the full one so that a correction can be made.....
			return enemyLocation.add(finalDirection, distanceSpacing);
		}
	}
}
