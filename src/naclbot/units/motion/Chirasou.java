package naclbot.units.motion;
import java.util.ArrayList;

import battlecode.common.*;
import naclbot.variables.GlobalVars;


/* ------------------   Overview ----------------------
 * 
 * Functions for controlling info of nearby units....
 *
 * ~~ Coded by Illiyia (akimn@#mit.edu)
 * 
 * Debug statements all begin with SYSTEM CHECK 
 *  
 ---------------------------------------------------- */


// This is a class that contains functions related to group movement and dispersion

public class Chirasou extends GlobalVars {
	
    // Function for the robot to move away from the nearest ally - returns a location that the robot would like to go to....
	
    public static MapLocation tryMoveAway(MapLocation myLocation, MapLocation allyLocation, float strideDistance) throws GameActionException{

    	// Location to store where the robot will attempt to run away to
    	MapLocation newLocation = null;
 		
    	// Generate the direction towards the ally
     	Direction dir = myLocation.directionTo(allyLocation);
     	
     	// Generate a new direction to try to go to
     	Direction testDir = new Direction(dir.radians+(float) Math.PI);
     	MapLocation testLocation = myLocation.add(testDir, strideDistance);
     	
     	// Test a variety of distances in that direction
     	for (int i = 0; i < 4; i ++){
	     	if (rc.canMove(testDir, (float)(2- 0.4*i))){
	     		newLocation = myLocation.add(testDir, (float)(2- 0.4*i));
	     		return newLocation;
	     	}		
    	} 	
 		newLocation = Yuurei.attemptRandomMove(myLocation, testLocation, strideDistance);

     	// Return the location created by the attemptRandomMove function, which may be null if no such location can be found....
 		
     	return newLocation;
    }
    // Wrapper function to disperse units...
    
    public static MapLocation Disperse(Team allies, MapLocation myLocation, float strideDistance) throws GameActionException{
    	
    	// Get all nearest allies
    	RobotInfo[]currentAllies = rc.senseNearbyRobots(-1, allies);
    	
    	// Get the closest one...
    	RobotInfo nearestAlly = getNearestAlly(currentAllies, myLocation);
    	
    	// Attempt to move away from it, if such an ally exists
    	if (nearestAlly != null){
    		return tryMoveAway(myLocation, nearestAlly.location, strideDistance);
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
    
    // Secondary routine to get the nearest non-scout / non archon
    
    public static RobotInfo getNearestEnemyToBroadcast(RobotInfo[] currentEnemies, MapLocation myLocation){
    	
    	// Initialize a value to store the minimum and the index of the nearest ally
    	float minimum = Integer.MAX_VALUE;
		int index = -1;
		
		// Iterate through all nearby allies
		for (int i = 0; i < currentEnemies.length; i++){
			// Find minimal distance
			float dist = myLocation.distanceTo(currentEnemies[i].location);

			if (dist < minimum && (currentEnemies[i].getType() != RobotType.ARCHON) && (currentEnemies[i].getType() != RobotType.SCOUT)){
				minimum = dist;
				index = i;	
			}			
		}	
		// If there actually was an ally in the array this should always trigger
		if (index >= 0){
			return currentEnemies[index];
		}
		else{
			return null;
		}
    }
    
    // Secondary routine to obtain the nearest ally to a certain robot...
    
    public static RobotInfo getNearestNonScoutEnemy(RobotInfo[] currentEnemies, MapLocation myLocation){
    	
    	// Initialize a value to store the minimum and the index of the nearest ally
    	float minimum = Integer.MAX_VALUE;
		int index = -1;
		
		// Iterate through all nearby allies
		for (int i = 0; i < currentEnemies.length; i++){
			// Find minimal distance
			float dist = myLocation.distanceTo(currentEnemies[i].location);

			if (dist < minimum && currentEnemies[i].type != RobotType.SCOUT){
				minimum = dist;
				index = i;	
			}			
		}	
		// If there actually was an ally in the array this should always trigger
		if (index >= 0){
			return currentEnemies[index];
		}
		else{
			return null;
		}
    }
    
   // Secondary routine to obtain the nearby soldiers to the enemy.......
    
    public static ArrayList<RobotInfo> getNearestSoldiers(RobotInfo[] teamSoldiers, MapLocation myLocation){
    	
    	// Initialize a value to store the minimum and the index of the nearest ally
    	
    	ArrayList<RobotInfo> nearbySoldiers = new ArrayList<RobotInfo>();

		// Iterate through all nearby allies
		for (int i = 0; i < teamSoldiers.length; i++){
			// Find minimal distance
			if (teamSoldiers[i].type == RobotType.SOLDIER){
				nearbySoldiers.add(teamSoldiers[i]);
			}			
		}	
		return nearbySoldiers;
    }
    
    // Function for units to collect bullets from any nearby trees......
    
    public static void attemptInteractWithTree(MapLocation startingLocation, float bodyRadius) throws GameActionException{
    	
    	// Boolean to store whether or not a unit has interacted with a tree yet or no....
    	boolean hasShaken = false;
    	
    	// Iterate over ten different angles....
    	for (int i = 0; i <= 12; i ++){
    		
    		// Derive the direction to check....
    		Direction directionToCheck = new Direction((float) (i * Math.PI / 6));
    		
    		// Derive the location to check
    		MapLocation locationToCheck = startingLocation.add(directionToCheck, (float)(bodyRadius + 0.9));
    		
    		// Get the tree at the location checked, if there is one....
    		TreeInfo tree = rc.senseTreeAtLocation(locationToCheck);
    		
    		// If there is a tree there and no tree has yet been shaken....
    		if (tree != null && !hasShaken){
    			
    			// If there are bullets in the tree.....
    			if(tree.getContainedBullets() > 0){
    				
    				// SYSTEM CHECK - Place a orange dot on any tree that has been shaken...
    				rc.setIndicatorDot(locationToCheck, 255, 165, 0);
    				
    				// Check to see if the robot is capable of interacting with a tree....
    				
    				if(rc.canInteractWithTree(locationToCheck)){
    					
	    				// Shake the tree and set the boolean for having shaken a tree to true....
	    				rc.shake(tree.ID);
	    				hasShaken = true;
    				}
    			}
    		}    		
    	}    	
    }
    
    
    // Function to see if there are any trees in the general direction of movement of the robot......
    
    public static TreeInfo treesInDirection(MapLocation startingLocation, Direction targetDirection, TreeInfo[] nearbyTrees, float bodyRadius, float strideRadius) throws GameActionException{
    	    	
    	for (int i = -4; i <= 4; i ++){
    		
    		// Get the direction to be sweeped
    		Direction testDirection = new Direction((float) (targetDirection.radians + i * Math.PI / 12));
    		
    		// Retrieve the location to be sweeped
    		MapLocation testLocation = startingLocation.add(testDirection, bodyRadius + strideRadius);
    		
    		// Get information on any potential tree in the vicinity, if there is one.......    		
    		TreeInfo tree = rc.senseTreeAtLocation(testLocation);
    		
    		if(tree != null){
    			
    			// SYSTEM CHECK - Draw a red dot on any trees found in this manner....
    			rc.setIndicatorDot(testLocation, 130, 0, 0);
    			
    			return tree;
    		}
    		
    		// If there are no trees in the location to be sweeped......
    		else{
    			
    			// SYSTEM CHECK - Draw a red dot on any trees found in this manner....
    			rc.setIndicatorDot(testLocation, 0, 130, 0);    			
    		}
    	}
    	return null;
    }
}
