// Dodging collisions with bullets
package naclbot.units.motion;
import battlecode.common.*;
import naclbot.variables.GlobalVars;

//~~ by Illiyia

// This is a class that contains functions related to group movement and dispersion

public class Chirasou extends GlobalVars {
	
    // Function for the robot to move away from the nearest ally - returns a location that the robot would like to go to....
	
    public static MapLocation tryMoveAway(MapLocation myLocation, MapLocation allyLocation) throws GameActionException{

    	// Location to store where the robot will attempt to run away to
    	MapLocation newLocation = null;
 		
    	// Generate the direction towards the ally
     	Direction dir = myLocation.directionTo(allyLocation);
     	
     	// Add some randomness - not always directly away...
     	float keikaku = (float)(Math.random() *  Math.PI/2 - Math.PI/4);
     	
     	// Generate a new direction to try to go to
     	Direction testDir = new Direction(dir.radians+(float) Math.PI + keikaku);
     	
     	// Test a variety of distances in that direction
     	for (int i = 0; i < 4; i ++){
	     	if (rc.canMove(testDir, (float)(2- 0.4*i))){
	     		newLocation = myLocation.add(testDir, (float)(2- 0.4*i));
	     		return newLocation;
	     	}		
    	}
     	
     	// Try the same offset but in the opposite direction
     	Direction testDir2 = new Direction(dir.radians+(float) Math.PI - keikaku);
     	
     	// Test a variety of distances in that direction
     	for (int i = 0; i < 4; i ++){
	     	if (rc.canMove(testDir, (float)(2- 0.4*i))){
	     		newLocation = myLocation.add(testDir2, (float)(2- 0.4*i));
	     		return newLocation;
	     	}		
    	}    	
     	// If none of these work, return the default null value
     	return newLocation;
    }	
    
    // Wrapper function to disperse units...
    
    public static MapLocation Disperse(Team allies, MapLocation myLocation) throws GameActionException{
    	
    	// Get all nearest allies
    	RobotInfo[]currentAllies = rc.senseNearbyRobots(-1, allies);
    	
    	// Get the closest one...
    	RobotInfo nearestAlly = getNearestAlly(currentAllies, myLocation);
    	
    	// Attempt to move away from it, if such an ally exists
    	if (nearestAlly != null){
    		return tryMoveAway(myLocation, nearestAlly.location);
    	}
    	// If no allies are nearby, simply return none!
    	else{
    		return null;    	
    	}
    }
    
    // Secondary routine to obtain the nearest ally to a certain robot...
    
    public static RobotInfo getNearestAlly(RobotInfo[] currentAllies, MapLocation myLocation){
    	
    	// Initialize a value to store the minimum and the index of the nearest ally
    	float minimum = Integer.MAX_VALUE;
		int index = -1;
		
		// Iterate through all nearby allies
		for (int i = 0; i < currentAllies.length; i++){
			// Find minimal distance
			float dist = myLocation.distanceTo(currentAllies[i].location);

			if (dist < minimum ){
				minimum = dist;
				index = i;	
			}			
		}	
		// If there actually was an ally in the array this should always trigger
		if (index >= 0){
			return currentAllies[index];
		}
		else{
			return null;
		}
    }
	
}
