package naclbot.units.motion.shoot;
import battlecode.common.*;
import naclbot.variables.GlobalVars;
import naclbot.units.motion.other.*;

import java.util.ArrayList;
import java.util.Random;

/* ------------------   Overview ----------------------
 * 
 * Functions for controlling shooting and aiming
 *
 * ~~ Coded by Illiyia (akimn@#mit.edu)
 * 
 *  * Debug statements all begin with SYSTEM CHECK 
 * 
 ---------------------------------------------------- */


// Class to contain some useful functions related to aiming and shooting....

public class Korosenai extends GlobalVars {
	
	// Maximum distance away from current location that the functions will sweep in order to make sure that the robot will not hit the ally.....
	public static final float maximumAllyCheckDistance = 3;
	
	// Firing line offsets...
	public static final float triadOffset = 20;
	public static final float pentadOffset = 15;
	
	// Probability that if a robot has previous enemy data, it will shoot at the next location the enemy might travel at.....
	public static final float probabilityPredict = (float) 0.8;

    // Function to determine if there is any tree in the line between one location and the other
    
    public static boolean isLineBLockedByTree(
    		
		// Input Variables
		MapLocation start, // The start location of the line check
		MapLocation end, // The end location of the line....
		
		float spacing // How of ten to check for an obstacle...
		
		) throws GameActionException {
    	
    	// Find the direction from the starting point to the end
    	Direction search = start.directionTo(end);
    	
    	// Iterate up through the length of the gap between the two selected points
    	for(int i = 0; i * spacing < start.distanceTo(end); i++){
    		
    		// If there is a tree in the way, say so..,
    		if (rc.isLocationOccupiedByTree(start.add(search, (float)  (i * spacing)))){
    			
    			TreeInfo blockingTree = rc.senseTreeAtLocation(start.add(search, (float) (i * spacing)));
    			
    		   	// SYSTEM CHECK print light pink dot at where the blocking tree is located...
    	    	rc.setIndicatorDot(blockingTree.location, 255, 180, 190);
    			
    			return true;
    		}
    	// If by the end of the for loop nothing is there, then we return false, meaning that the line isn't blocked
    	}
    	return false;
    }
    
    
    public static boolean isDirectionOccupiedByAlly(
    		
    		// Input variables
    		MapLocation start, // Location the robot is currently at
    		
    		Direction dir, // Intended firing location of this bullet....
    		
    		RobotInfo[] nearbyAllies, // A list of info on allies of the robot
    		
    		float distance // The max distance away to make sure that the robot isn't going to accidentally shoot an ally
    		
    		) throws GameActionException{
    	
    	// Iterate through each nearby ally...
    	for (RobotInfo allyData: nearbyAllies){
    		
    		// Obtain the distance to each of these allies
			float distanceTo = allyData.location.distanceTo(start);
			
			// SYSTEM CHECK - Print out the distance to this ally...
			// System.out.println("distanceTo: " + distanceTo);
			
			// Obtain the direction to the ally
			Direction directionTo = new Direction(start, allyData.location);
			
			// If the ally is quite close to the current location of the robot...
    		if (distanceTo < distance){
    			// Calculate the largest number of radians intercepted by the ally
    			float interceptRadians = allyData.getRadius() / distanceTo;
    			
    			// SYSTEM CHECK - Print out the distance to this ally...
    			// System.out.println("interceptRadians" + interceptRadians);
    			
    			// If the current direction to shoot will intercept the body of the currently considered ally
    			if(Math.abs(directionTo.radians - dir.radians) <= interceptRadians){
    				
    				// SYSTEM CHECK - Show which allies cannot be shot at... along with a dark blue dot....
    				// System.out.println("Cannot fire - there is an ally with ID: " + allyData.ID + " in the way" );
    				rc.setIndicatorDot(allyData.location, 0, 0, 80); 
    				
    				//  SYSTEM CHECK - Also show indicator lines showing the minimal and maximal intercept Radians of this robot from the current robot....
    				rc.setIndicatorLine(start, start.add(new Direction(dir.radians + interceptRadians), distance), 80, 0, 0);
    				rc.setIndicatorLine(start, start.add(new Direction(dir.radians - interceptRadians), distance), 80, 0, 0);
    				
    				return true;
    			}
    		}    		
    	}
    	return false;
    }
    
    
    
    // Function to make robot shoot if the robot can from the current location (this should be used after post-processing
    // Returns true if the robot has shot at the enemy and false otherwise.....
    
    public static boolean tryShootAtEnemy(
    		
		// Input variables
		MapLocation targetLocation, // Location of the robot to be shot at
		MapLocation currentLocation, // Location that this robot is currently at
		
		int shotType, // type of shot to be made
						// This value is 0 if the shot type is going to be a single
						// This value should be 1 if the shot type is to be a triple..
						// This value should be a 2 if the shot type is to be a pentad...
		
		RobotInfo[] nearbyAllies // An array storing the robot information of nearby allies		

    	) throws GameActionException{
    	
    	// Obtain the direction wanted to shoot at....
    	Direction dirToShoot = new Direction (currentLocation, targetLocation);
    	
    	// If the robot wishes to fire a single shot....
    	if (shotType == 0){
    		// If no allies are going to be instantly hit by the bullets... 
    		if (!isDirectionOccupiedByAlly(currentLocation, dirToShoot, nearbyAllies, maximumAllyCheckDistance)){
    			
    			// Make sure your team is rich enough for you to fire something at them......
    			if(rc.canFireSingleShot()){
	    			// Fire!
	    			rc.fireSingleShot(dirToShoot);
	    			return true;    	
    			}
    		}    	
    	}
    	else if (shotType == 1){
    		
    		// Iterate over the three bullets to be fired
    		for (int j = -1; j <= 1; j++){
    			// Get the direction that the bullet will be traveling at
    			Direction fireDirection = new Direction (dirToShoot.radians + j * triadOffset);
    			// If no allies are going to be instantly hit by the bullets... 
	    		if (isDirectionOccupiedByAlly(currentLocation, fireDirection, nearbyAllies, maximumAllyCheckDistance)){	    			
	    			return false;
	    			
	    		}     
    		}   		
    		// Make sure your team is rich enough for you to fire something at them......
			if(rc.canFireTriadShot()){
    			// Fire!
    			rc.fireTriadShot(dirToShoot);
    			return true; 
			}else{
    			return false;
    		}
    	}
		else if (shotType == 2){
		    		
    		// Iterate over the three bullets to be fired
    		for (int j = -2; j <= 2; j++){
    			// Get the direction that the bullet will be traveling at
    			Direction fireDirection = new Direction (dirToShoot.radians + j * pentadOffset);
    			// Check to see if no allies are going to be hit by bullets.....
	    		if (isDirectionOccupiedByAlly(currentLocation, fireDirection, nearbyAllies, maximumAllyCheckDistance)){
	    			
	    			return false;
	    		}
    			// Make sure your team is rich enough for you to fire something at them......
    			if(rc.canFirePentadShot()){
	    			// Fire!
	    			rc.firePentadShot(dirToShoot);
	    			return true;    	
    			} else{
    				return false;
	    		}    	
    		}
    	}
    	// Invalid shotType
		else{ 
			return false;
		}
    	// Desired shot is not possible, return false...
    	return false;
    }
    
    
    // Function to get the next location of an enemy based off of data from the previous turn....
    
    public static MapLocation getEnemyNextLocation(RobotInfo enemy, RobotInfo[] previousEnemyData){
    	
    	// Variable to store the data of the robot in question from the previous turn (if it exists)
    	RobotInfo prevEnemyData = null;
    	
    	// Iterate through all of the previous enemy Data....
    	for (RobotInfo enemyData: previousEnemyData){
    		// If the enemy was seen previously....
    		if (enemy.ID == enemyData.ID){
    			
    			// SYSTEM CHECK - Inform that the enemy the unit is attempting to shoot was seen last turn 
    			// 				  and place BROWN LINE LINE between the previous location and current...
    			// System.out.println("Currently tracked enemy previously seen before");
    			rc.setIndicatorLine(enemy.location, enemyData.location, 102, 51, 0);
    			prevEnemyData = enemyData;
    		}    		
    	}
    	// If the enemy data from the past was correctly loaded...........
    	if (prevEnemyData != null){
    		
    		// Get the direction the robot is currently moving at
    		Direction movingDirection = new Direction(prevEnemyData.location, enemy.location);
    		
    		// Get how far it was previously moving per turn
    		float distancePreviouslyMoved = prevEnemyData.location.distanceTo(enemy.location);
    		
    		// Extrapolate the trajectory
    		MapLocation newLocation = enemy.location.add(movingDirection, distancePreviouslyMoved);
    		
    		// SYSTEM CHECK - Place a DARK RED DOT on where the enemy will now be....
    		// rc.setIndicatorDot(newLocation, 102, 0, 0); 
    		
    		return newLocation;   
    	}
    	
    	// If there is no matching data return null as no future position can be extrapolated......    	
    	return null;    	
    }
    
    // Function to obtain a firing location for an enemy.... Does not take into account tree..
    
    public static MapLocation getFiringLocation(RobotInfo enemy, RobotInfo[] previousEnemyData, MapLocation myLocation){
    	
    	// Location to store the future location of the enemy (if it can be calculated)
    	MapLocation enemyNextLocation; 
    	
    	// Make sure to not iterate through null array
    	if (previousEnemyData != null){    		
    		enemyNextLocation = getEnemyNextLocation(enemy, previousEnemyData);
    	}
    	else{
    		enemyNextLocation = null;
    	} 
    	
    	// If there was previous enemy data, attempt to use future location....
    	if (enemyNextLocation != null){
    		
    		float randomChance = (float) Math.random();

    		// With a great chance use the future location
			if (randomChance <= probabilityPredict){
    			
    			// Obtain the distance to the enemy
            	float distanceTo = myLocation.distanceTo(enemyNextLocation);		

        		Direction directionTo = new Direction(myLocation, enemyNextLocation);		

        		// Calculate the largest number of radians intercepted by the ally
        		float interceptRadians = enemy.getRadius() / distanceTo;
        		
        		// Obtain a random offset from the current direction to be shooting in that does not exceed half the number of radians intercepted by the robot
        		float randomOffset = (float) ((Math.random() * interceptRadians/2) - (interceptRadians / 4));
        		
        		// Obtain a direction to fire in...
        		Direction firingDirection = new Direction(directionTo.radians + randomOffset);
        		
        		MapLocation firingLocation = myLocation.add(firingDirection, distanceTo);
        		
        		// SYSTEM CHECK show where the intended firing location is..... LIGHT GREEN LINE
        		// rc.setIndicatorLine(myLocation, firingLocation, 153, 255, 51);   
        		// System.out.println("Attempting to fire near the future location");
        		
        		return firingLocation;    			
    		}		
    	
			// Utilize the current location
    		else{
    			
    			// Obtain the distance to the enemy
            	float distanceTo = myLocation.distanceTo(enemy.location);		

        		Direction directionTo = new Direction(myLocation, enemy.location);		

        		// Calculate the largest number of radians intercepted by the ally
        		float interceptRadians = enemy.getRadius() / distanceTo;
        		
        		// Obtain a random offset from the current direction to be shooting in that does not exceed half the number of radians intercepted by the robot
        		float randomOffset = (float) ((Math.random() * interceptRadians/2) - (interceptRadians / 4));
        		
        		// Obtain a direction to fire in...
        		Direction firingDirection = new Direction(directionTo.radians + randomOffset);
        		
        		MapLocation firingLocation = myLocation.add(firingDirection, distanceTo);
        		
        		// SYSTEM CHECK show where the intended firing location is..... DARK GREEN LINE
        		// rc.setIndicatorLine(myLocation, firingLocation, 0, 102, 0);
        		
        		return firingLocation;        		
    		}    		
    	}
    	// If there was no previous enemy data, just use the current location of the robot to shoot to...
    	else{
    		
    		// Obtain the distance to the enemy
        	float distanceTo = myLocation.distanceTo(enemy.location);		

    		Direction directionTo = new Direction(myLocation, enemy.location);		

    		// Calculate the largest number of radians intercepted by the ally
    		float interceptRadians = enemy.getRadius() / distanceTo;
    		
    		// Obtain a random offset from the current direction to be shooting in that does not exceed half the number of radians intercepted by the robot
    		float randomOffset = (float) ((Math.random() * interceptRadians/2) - (interceptRadians / 4));
    		
    		// Obtain a direction to fire in...
    		Direction firingDirection = new Direction(directionTo.radians + randomOffset);
    		
    		MapLocation firingLocation = myLocation.add(firingDirection, distanceTo);
    		
    		// SYSTEM CHECK show where the intended firing location is..... DARK GREEN LINE
    		// rc.setIndicatorLine(myLocation, firingLocation, 0, 102, 0);
    		
    		return firingLocation;    		
    	}		
    }    
}
