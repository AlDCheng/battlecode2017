// Dodging collisions with bullets
package naclbot.units.motion;
import battlecode.common.*;

import naclbot.variables.GlobalVars;
import java.util.ArrayList;


// This is a class that contains functions related to dodging and positional correction....

public class Yuurei extends GlobalVars {
	
	public static final float scanGranularity = (float) 0.5;
	public static final int scanAngleNumber = 9;
	public static final float scanningAngleDiff = (float)(Math.PI * 2 / scanAngleNumber);
	
	// Do all wrapper for the functions.....
	// ONLY Use if there are bullets nearby.. Otherwise there is no point in calling this function
	// Note this ASSUMES that there is nothing at the desired location the robot is currently attempting to move to....
	
	public static MapLocation attemptDodge(
			
		// Input Variables`			
		MapLocation desiredLocation, // Location that the robot wishes to go to...
		MapLocation startingLocation, // Location that the robot is currently at....
		
		BulletInfo[] nearbyBullets, // A non empty array of nearby bullets
				
		float strideRadius, // Float representing the distance the robot can travel in one turn... 																																					
		float bodyRadius, // Float representing how wide the robot
		
		float changeRadius, // Float representing how far the robot will be willing to move away from the desired location
							  // Input value of -1 means that the robot will be willing to accept any location....
		
		boolean rotationDirection, // Boolean representing the rotation direction of the robot currently, dodge will attempt to prioritize this...
								   // True entails counterclockwise and clockwise rotation follows from false
		
		boolean canDodge // Final verdict of the function - outputs true if can dodge or doesn't need to
						 // Outputs false if it cannot dodge the bullets at all
		){			
		
		// Find the maximal distance away from the startingLocation that we must scan to determine the optimal location....
		float scanRadius = strideRadius + bodyRadius;		
		
		// SYSTEM CHECK - Make sure variables are being called correctly
		System.out.println("Inputs are..... nearbyBullets: " + nearbyBullets);
		System.out.println("bodyRadius: " + bodyRadius + "strideRadius" + strideRadius);
		
		// Obtain the locations of all bullets within the scanRadius centered at the robot's current location
		ArrayList <MapLocation> newBulletLocations = getNextBulletLocations(nearbyBullets, scanRadius, startingLocation);
		
		// If any of the bullets are near enough to warrant issue, proceed forwards
		if (newBulletLocations.size() > 0){	
			
			// SYSTEM CHECK - Notify that this function has been called
			System.out.println("Nearby bullets detected attempting to dodge");
			
			// If there is a bullet that will collide with the robot in the current desired location
			if (ifBulletWillCollide(newBulletLocations, desiredLocation, bodyRadius)){
				
				// SYSTEM CHECK - Notify that this function has been called
				System.out.println("Nearby bullets may collide with my desired location next turn - attempting to find solution");
				
				// Call the find optimal function...
				MapLocation dodgeLocation = findOptimalLocation(newBulletLocations, true, desiredLocation, startingLocation, 
						strideRadius, bodyRadius, changeRadius, rotationDirection);
				
				// If the robot will be able to dodge something the dodgeLocation should be non-null
				if(dodgeLocation != null){
					
					
					
					canDodge = true;
				}
				return dodgeLocation;
	
			}
			// Otherwise if none of the bullets impede on the desired location....
			else{
				canDodge = true;
				return desiredLocation;
			}
		}
		// If there are no bullets within the scanDistance.. simply return the desiredLocation as there is no conflict
		else{
			canDodge = true;
			return desiredLocation;
		}
	}
	
	
	// Function to find the optimal dodging point given a location of bullets....
	
	public static MapLocation findOptimalLocation(
			
		// Input Variables
		ArrayList<MapLocation> nearbyBulletLocations, // List of bullets nearby robots 
													  // The data is for the next turn - construct using ifBulletWillCollide
		
		boolean dodge, // Decides whether or not the robot is actually going to dodge bullets or is simply looking for a correction
					   // nearbyBulletLocation should not be empty if this is true
		
		MapLocation desiredLocation, // Location that the robot initially wants to go to			
		MapLocation startingLocation, // Location that the robot is currently at
		
		float strideRadius, // Float representing the distance the robot can travel in one turn... 																																					
		float bodyRadius, // Float representing how wide the robot
		
		float changeRadius, // Float representing how far the robot will be willing to move away from the desired location
							  // Input value of -1 means that the robot will be willing to accept any location....
		
		boolean rotationDirection // Boolean representing the rotation direction of the robot currently, dodge will attempt to prioritize this...
								   // True entails counterclockwise and clockwise rotation follows from false
		){
		
		
		// Since we will start by searching away from the current desiredLocation...
		Direction desiredDir = new Direction(startingLocation, desiredLocation);
		
		// Local value of the offset
		float scanAngleOffset = scanningAngleDiff;
		
		// Location to start search
		MapLocation scanStartLocation;
		float searchRadius;
		
		// If the user inputed that there is no preference for how far away the  the robot was willing to go away from the desired location
		if (changeRadius < 0){
			// Scan all possibilities, beginning at the starting location....
			scanStartLocation = startingLocation;
			searchRadius = strideRadius;
		}
		else{
			scanStartLocation = desiredLocation;
			searchRadius = changeRadius;
		}
		// If the robot is supposed to be going counterclockwise, change priority direction of search
		if(rotationDirection){
			scanAngleOffset *= 1;
		}
		
		
		// Iterate through all the scanning angles - does this clockwise or counterclockwise depending on the current rotation setting of the robot
		for (int i = 0; i < scanAngleNumber; i++){
			
			// Obtain the direction created by the offset
			Direction testDir = new Direction(desiredDir.radians - (i * scanAngleOffset));
			
			System.out.println(i);
			
			// Iterate through a number of points determined by the granularity of the search
			for(int j = 0; searchRadius - (scanGranularity * j) > 0; j++){
				
				// Obtain the possible new location
				MapLocation testLocation = scanStartLocation.add(testDir, searchRadius - (scanGranularity * j));
				
				// Assert that it is within stride distance of the current location of the robot
				if(testLocation.distanceTo(startingLocation) <= strideRadius){
					// Assert that it is actually possible to move to this new location
					if(isLocationValid(testLocation)){
						// Assert that no bullet will collide with the robot at this location if the robot is actually looking to dodge
						if(dodge){
							
							if(!ifBulletWillCollide(nearbyBulletLocations, desiredLocation, bodyRadius)){	
								
								// SYSTEM CHECK - Show which location the robot decides as valid - navy blue dot
								rc.setIndicatorDot(testLocation, 0, 0, 128);
								
								return testLocation;
							}
						} else{ // If the robot is not currently looking to dodge, the test location has become valid
							
							// SYSTEM CHECK - Show which location the robot decides as valid - navy blue dot
							rc.setIndicatorDot(testLocation, 0, 0, 128);
							
							return  testLocation;
						}						
					}
				}				
			}			
		} 
		// If the function has not found a valid location, return nothing - there are no valid points for the robot.... 
		
		// SYSTEM CHECK - see if the robot has actually attempted to find a point and eventually didn't
		System.out.println("Search for a new point with given parameters unsuccesful");
		
		return null;
	}
	
	
	// Rewrap of the canMove function... for use in this class only - takes a location to see if the robot can move to it 
	
	private static boolean isLocationValid(MapLocation testLocation){
		
		return rc.canMove(testLocation);
	}
	
	
	// Checks to see if there will be a bullet within a body radius of the currently selected location next turn. Returns true if so....
	
	public static boolean ifBulletWillCollide(ArrayList<MapLocation> nearbyBulletLocations, MapLocation desiredLocation, float bodyRadius){
		
		// Iterate through each bullet location nearby
		for (MapLocation bulletLocation: nearbyBulletLocations){
			// If the location is within the radius distance of the object at the desired location, return false
			
			// SYSTEM CHECK place indicator dots at the predicted locations of each of the bullets - bright red
			rc.setIndicatorDot(bulletLocation, 255, 0, 0);
			
			if(desiredLocation.distanceTo(bulletLocation) < bodyRadius){
				return true;
			}
		}
		return false;		
	}
	

	// Function to obtain the locations of all the bullets visible within the next turn...
	
	public static ArrayList<MapLocation> getNextBulletLocations(BulletInfo[] nearbyBullets, float distance, MapLocation centre) {
		
		// Initialize a list of locations where all the bullets will be...
		ArrayList <MapLocation> newBulletLocations = new ArrayList<MapLocation>(nearbyBullets.length);
		
		// For each of the nearby bullets in the bullet list
		for (BulletInfo bullet: nearbyBullets) {
			
			// Retrieve the location of the bullets currently
			MapLocation currentLocation = bullet.getLocation();
			
			// Get the velocity of the bullets
			Direction currentDirection = bullet.getDir();
			float currentSpeed = bullet.getSpeed();
			
			// Calculate the location that the bullet will be at in one turn and add to the list of Locations....
			MapLocation newLocation = currentLocation.add(currentDirection, currentSpeed);			
			
			// If the bullet's location is within the considered distance....
			if (centre.distanceTo(newLocation) <= distance){
				newBulletLocations.add(newLocation);
				
				// SYSTEM CHECK place indicator dots at the predicted locations of each of the bullets - bright pink
				rc.setIndicatorDot(newLocation, 255, 20, 147);
			}
		}
		return newBulletLocations;
	}
	
	
	// Function to firstly correct any out of bounds errors created by a desired location.......
	
	public static MapLocation correctOutofBoundsError(
			
		// Input Variables
		MapLocation desiredLocation, // Location that the robot wishes to go to...
		MapLocation startingLocation, // Location that the robot is currently at....
		
		float bodyRadius, // Float representing how wide the robot
		float strideRadius, // Float representing the distance the robot can travel in one turn... 
		
		boolean rotationDirection // Boolean representing the rotation direction of the robot currently, dodge will attempt to prioritize this...
		   						  // True entails counterclockwise and clockwise rotation follows from false
		
		) throws GameActionException{
		
		// Variable to store the output of this function
		MapLocation newLocation;
	
		// SYSTEM CHECK - See if the robot recognizes that it cannot currently move to the desired location
		// System.out.println("Cannot move to desired location");
		
			
		// SYSTEM CHECK - Make sure that the scouts determination of the incorrect map location is accurate...
		System.out.println("Your current selection of Chitoge Kirisaki as best girl is incorrect.... please select Onodera to continue!!!");
		
		// If the robot is attempting to move above the map bounds...
		if (!rc.onTheMap(new MapLocation(desiredLocation.x, desiredLocation.y + bodyRadius))){
			// Correct the discrepancy in the y coordinates
			float yCorrect = desiredLocation.y - startingLocation.y;				
			MapLocation newMove = new MapLocation(desiredLocation.x, desiredLocation.y - 2 * yCorrect);
			newLocation = newMove;		
		}
		// If the robot is attempting to move to the left of the map bounds...
		else if (!rc.onTheMap(new MapLocation(desiredLocation.x - bodyRadius, desiredLocation.y))){				
			// Correct the discrepancy in the y coordinates
			float xCorrect = desiredLocation.x - startingLocation.x;
			MapLocation newMove = new MapLocation(desiredLocation.x - 2 * xCorrect, desiredLocation.y);
			newLocation = newMove;				
		}
		// If the robot is attempting to move below the map bounds...
		else if ((!rc.onTheMap(new MapLocation(desiredLocation.x, desiredLocation.y - bodyRadius)))){				
			// Correct the discrepancy in the y coordinates
			float yCorrect = desiredLocation.y - startingLocation.y;
			MapLocation newMove = new MapLocation(desiredLocation.x, desiredLocation.y - 2 * yCorrect);
			newLocation = newMove;				
		}
		// If the robot is attempting to move to the right of the map bounds...
		else{	
			// Correct the discrepancy in the y coordinates
			float xCorrect = desiredLocation.x - startingLocation.x;
			MapLocation newMove = new MapLocation(desiredLocation.x - 2 * xCorrect, desiredLocation.y);
			newLocation = newMove;			
		}			
		
		// If it the corrected point is on the map
		if (rc.onTheMap(newLocation, bodyRadius)){
			
			// SYSTEM CHECK - Display the corrected move on screen as an orange line
			rc.setIndicatorLine(startingLocation, newLocation, 255, 165, 0);

			return newLocation;			
		}
		
		// Otherwise if the correction is not on the map (the robot is then in a corner.... reverse directions feelsbadman
		else{
			// SYSTEM CHECK - See if object is returning the oppostite line or not
			System.out.println("Turnin all the way around....");
			
			Direction oppositeDirection = new Direction(desiredLocation, startingLocation);
			
			newLocation = startingLocation.add(oppositeDirection, strideRadius);
			rotationDirection = !rotationDirection;
			
			// SYSTEM CHECK - Display the corrected move on screen as an orange line
			rc.setIndicatorLine(startingLocation, newLocation, 255, 165, 0);
			
			return newLocation;			
		}		
	}
	
	// Function to attempt to move in a target direction
	
    public static MapLocation tryMoveInDirection(Direction dir, float distance, MapLocation myLocation) throws GameActionException {
    	
    	// If only a direction is given  use arbitrary values
    		// Arbitrary values are 30 degree offsets with 4 sweeps per side... 
    		// A sweep in one direction and its opposite will cover nearly all possibilities
    	
        return tryMoveInDirection(dir, 20, 4, distance, myLocation);
    }    

    
    // Function to attempt to move in a target direction (with inputed values), returns true if it actually can move in that direction
    
    private static MapLocation tryMoveInDirection(
    		
		// Input Variables    		
		Direction dir, // Target Direction of the robot
		
		float degreeOffset, // Number of degrees from the target direction that each check is done...
		int checksPerSide, // Number of checks per side of the target direction that are done...
		
		float distance, // Maximal distance away from the current robot that is supposed to be checked...
		
		MapLocation myLocation // Current Location of the robot....
		
		) throws GameActionException {
    	
    	// Generate distances to test - prioritize initial input direction
    	for (int i = 0; i < 5; i++){
    		
    		float testDistance = (float)(distance - (i * distance / 5));    		
	        // Try going the test distance in the targeted direction
	        if (rc.canMove(dir, testDistance)) {	            
	            return myLocation.add(dir, testDistance);
	        }
        }
    	// Generate distances to test on all of the offsets
		for (int i = 0; i < 5; i++){
		    		
		    float testDistance = (float)(distance - (i * distance / 5));    
		    // Current number of degree offsets off of original being checked
	        int currentCheck = 1;
	        
	        while(currentCheck <= checksPerSide) {
	        	
	            // Try the offset of the left side
	            if(rc.canMove(dir.rotateLeftDegrees(degreeOffset * currentCheck), testDistance)) {
	                return myLocation.add(dir.rotateLeftDegrees(degreeOffset * currentCheck), testDistance);
	            }
	            // Try the offset on the right side
	            if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck), testDistance)) {
	                return myLocation.add(dir.rotateRightDegrees(degreeOffset * currentCheck), testDistance);
	            }
	            // Since no move has been performed, check to a higher offset on either side....
	            currentCheck+=1;
	        }
		}
	    // A move through the checks cannot happen, so return a null to express this
        return null;
    }    
}
