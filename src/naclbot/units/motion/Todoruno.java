//
package naclbot.units.motion;
import java.util.ArrayList;

import battlecode.common.*;
import naclbot.units.motion.shoot.Korosenai;
import naclbot.variables.GlobalVars;

/* ------------------   Overview ----------------------
 * 
 * Functions for controlling shooting and aiming
 *
 * ~~ Coded by Illiyia (akimn@#mit.edu)
 * 
 *  * Debug statements all begin with SYSTEM CHECK 
 * 
 ---------------------------------------------------- */

// This is a class that contains functions related to tracking an enemy

public class Todoruno extends GlobalVars {
	
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
	
	// Function to move towards a given robot

	// When the robot is 6* multiplier away or greater from the current robot being tracked, it will move directly towards the currently tracked
	// At a slightly smaller distance, the robot will attempt to move so that it can start rotating around the  tracked robot...
	// When the unit is within 4.5 * muliplier of the target robot, the robot will simply attempt to rotate around it.....
	
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
	
	public static MapLocation engageSoldier(
			
		// Input Variables
		MapLocation startingLocation, // The current location of the robot
		
		RobotInfo enemySoldier, // The RobotInfo of the enemy soldier....
		
		float strideRadius, // The stride radius of the robot.........
		
		RobotInfo[] nearbyEnemies, // The RobotInfo of all nearby soldiers.............	
		
		RobotInfo[] nearbyAllies // The RobotInfo of all nearby allies...		
		){
		
		// SYSTEM CHECK - Make sure that the soldier knows that it is engaging a soldier........... Print out a crimson dot on the soldier.....
		System.out.println("Engaging an enemy soldier with ID: " );
		rc.setIndicatorDot(startingLocation, 220, 20, 60);
		
		ArrayList<RobotInfo> nearestEnemySoldiers = Chirasou.getNearestSoldiers(nearbyEnemies, startingLocation);
		ArrayList<RobotInfo> nearestAlliedSoldiers = Chirasou.getNearestSoldiers(nearbyAllies, startingLocation);
		
		
		
		
		
		
		
		
	
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		return null;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
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
