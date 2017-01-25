//
package naclbot.units.motion;
import battlecode.common.*;
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
	
	public static RobotInfo getNewEnemyToTrack(RobotInfo[] enemyRobots, MapLocation myLocation, boolean targetCombat,  boolean targetScout, boolean targetCivilian){

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
					// Update all placeholder parameters
					minimumPriorityUnitDistance = distanceTo;
					minimumTotalDistance = distanceTo;
					anyEnemyRobot = enemyRobot;
					prioirityRobot = enemyRobot;
				}
				// If the enemy is a scout and it is given that the robot may track scouts
				else if (enemyRobot.type == battlecode.common.RobotType.SCOUT && targetScout){
					// Update all placeholder parameters
					minimumTotalDistance = distanceTo;
					anyEnemyRobot = enemyRobot;
					
				}
				else if(enemyRobot.type == battlecode.common.RobotType.GARDENER){
					minimumPriorityUnitDistance = distanceTo;
					minimumTotalDistance = distanceTo;
					anyEnemyRobot = enemyRobot;
					prioirityRobot = enemyRobot;
				}
				// Finally if the robot is either a gardener or archon and the robot can target civilians....
				else if (enemyRobot.type == battlecode.common.RobotType.ARCHON){
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
}
