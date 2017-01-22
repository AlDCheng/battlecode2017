//
package naclbot.units.motion;
import battlecode.common.*;
import naclbot.variables.GlobalVars;

//~~ by Illiyia

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
	    			desiredMove = trackedRobot.location.add(fromDir, (float) (2.5 * multiplier));
	    			
	    		} else{
	    			// Calculate the direction from the target that you want to end up at
	    			Direction fromDir = new Direction(dir.radians + (float) (2 * Math.PI/3));
	    			
	    			// Obtain the desired target location
	    			desiredMove = trackedRobot.location.add(fromDir, (float) (2.5 * multiplier));	    			
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
				
	
}
