// Dodging collisions with bullets
package naclbot.units.motion;
import battlecode.common.*;

import naclbot.variables.GlobalVars;
import java.util.ArrayList;

/* ------------------   Overview ----------------------
 * 
 * Functions for controlling Robot movement and dodging
 *
 * ~~ Coded by Illiyia (akimn@#mit.edu)
 * 
 *  * Debug statements all begin with SYSTEM CHECK 
 * 
 ---------------------------------------------------- */
// This is a class that contains functions related to dodging and positional correction....

public class Yuurei extends GlobalVars {
	
	public static final float scanGranularity = (float) 0.5;
	public static final int scanAngleNumber = 8;
	public static final float scanningAngleDiff = (float)(Math.PI * 2 / scanAngleNumber);
	
	public static final int dodgeAngleNumber = 12;
	public static final float dodgeAngleOffset = (float)(Math.PI * 2 / dodgeAngleNumber);
	
	// Value after which units decide to give up trying to dodge......
	public static final float maxIncomingBullets = 5;
	
	// Do all wrapper for the functions.....
	// ONLY Use if there are bullets nearby.. Otherwise there is no point in calling this function
	// Note this ASSUMES that there is nothing at the desired location the robot is currently attempting to move to....
	// TODO Fix a few errors related to dodging bullets at sharp angles - reduce time complexity if there are many bullets nearby...
	
	
	// Simple class to show a line on the map....
	
	public static class Line{
		
		public MapLocation start;
		public MapLocation end;
		public MapLocation middle;
		
		public Line(MapLocation point1, MapLocation point2){
			this.start = point1;
			this.end = point2;
			this.middle = new MapLocation((point1.x + point2.x)/2, (point1.y + point2.y)/2);
		}
		
		public void display(){
			
			// Displays an aquamarine line on the map from the start and end locations of the Line
			rc.setIndicatorLine(start, end, 127, 255, 212);
		}
		
		public void print(){
			
			// Print a message detailing the start and end points of the line
			System.out.println("Line start: [" + start.x + ", " + start.y + "]. Line end: [" + end.x + ", " + end.y + "]");
		}
	
	}

	
	// -------------------------------------------------------------------------------------------//
	// -------------------------------- ATTACK ORIENTED FUNCTIONS  -------------------------------//
	// -------------------------------------------------------------------------------------------//	

	public static MapLocation tryDodge(
			
		// Input Variables
		MapLocation desiredLocation, // Location that the robot wishes to go to...
		MapLocation startingLocation, // Location that the robot is currently at....
		MapLocation enemyLocation, // Location that the nearest enemy is currently at....
		
		BulletInfo[] nearbyBullets, // A non empty array of nearby bullets
				
		float strideRadius, // Float representing the distance the robot can travel in one turn... 																																					
		float bodyRadius // Float representing how wide the robot
		){			
		
		// SYSTEM CHECK - Draw a bule dot on the location that the robot wishes to go to....
		rc.setIndicatorDot(desiredLocation, 0, 0, 255);
	
		// Find the maximal distance away from the startingLocation that we must scan to determine the optimal location....
		float scanRadius = strideRadius + bodyRadius + (float) (0.5);	
		
		Direction directionAway;
		
		if(enemyLocation == null){
			directionAway = Move.randomDirection();
		}
		else{
			// Get the direction pointing away from the robot in question....
			directionAway = enemyLocation.directionTo(startingLocation);
		}
		
		// Get the bullet lines for the current scenario....
		ArrayList<Line> bulletLines = getBulletLines(nearbyBullets, scanRadius, startingLocation);
		
       	// SYSTEM CHECK- Print out the amount of bytecode used prior to  calculating the dodge location......
       	System.out.println("Bytecode used prior to calculating dodge location: " + Clock.getBytecodeNum());
		
		if (bulletLines.size() > maxIncomingBullets){
			// SYSTEM CHECK - If there are too many bullets nearby, the robot will just give up......
			System.out.println("Too many bullets nearby, will not attempt to dodge");
			
			return null;
		}
		// Otherwise if there is at least one bullet nearby...
		else if(bulletLines.size() > 0){
			
			// SYSTEM CHECK - Notify that this function has been called
			System.out.println("Nearby bullets detected attempting to dodge");
			
			// Placeholder to determine whether or not the bullets currently considered would hit the robot.....
			boolean willCollide = false;
			
			// Iterate through each bullet line - if the desired location would intersect with any of them.... attempt to find a new point to go to..
			if(ifBulletLinesWillIntersect(bulletLines, desiredLocation, bodyRadius + (float) 0.1)){
				
				willCollide = true;
			}

			// If no bullets will collide in the desired location, simply return it...
			if(!willCollide){
				
				//SYSTEM CHECK - Print out that the desired location is valid and that the robot may continue to use it
				System.out.println("The current bullets pose no threat to the robot's move to the desired location");
				
				return desiredLocation;
			}
			
			// If bullets will collide with the desired location
			else{
				// Return the result of the dodge function...					
				return findDodgeLocation(bulletLines, startingLocation, strideRadius, bodyRadius  + (float) 0.05, directionAway);				
			}
		}
		// If there are no bullets nearby, just return the original desired location....
		else{
			
			//SYSTEM CHECK - Print out that there are no bullets nearby....
			System.out.println("There are no bullets nearby....");
			
			return desiredLocation;
			
		}
	}
	
	public static MapLocation findDodgeLocation(
			
			// Input Variables
			ArrayList<Line> bulletLines,  // List of bullets nearby lines														  
	
			MapLocation startingLocation, // Location that the robot is currently at
			
			float strideRadius, // Float representing the distance the robot can travel in one turn... 																																					
			float bodyRadius, // Float representing how wide the robot
			
			Direction directionAway // Direction away from offending robot - will be prioritized in the dodge......
			){

			// Location to start search
			MapLocation scanStartLocation = startingLocation;
			float searchRadius = bodyRadius;
			
			// Iterate through all the scanning angles - does this clockwise or counterclockwise depending on the current rotation setting of the robot
			for (int i = 0; i <= 8; i++){
				
				// Obtain the direction created by the offset
				Direction testDir1 = new Direction(directionAway.radians - (i * dodgeAngleOffset));
				Direction testDir2 = new Direction(directionAway.radians + (i * dodgeAngleOffset));
				
				// Iterate through a number of points determined by the granularity of the search
				for(int j = 0; strideRadius - (scanGranularity * j) > 0; j++){
					
					// Obtain the possible new locations
					MapLocation testLocation1 = scanStartLocation.add(testDir1, searchRadius - (scanGranularity * j));
					MapLocation testLocation2 = scanStartLocation.add(testDir2, searchRadius - (scanGranularity * j));
			
					if(isLocationValid(testLocation1)){
						// Assert that no bullet will collide with the robot at this location if the robot is actually looking to dodge				
							
						if(!ifBulletLinesWillIntersect(bulletLines, testLocation1, bodyRadius)){	
							
							// SYSTEM CHECK - Show which location the robot decides as valid - navy blue dot
							rc.setIndicatorDot(testLocation1, 0, 0, 128);
							
							return testLocation1;
						}
						// If the robot would collide with a bullet in the location 
						else{
							
							// SYSTEM CHECK - Show which location the robot decides as invalid - red
							rc.setIndicatorDot(testLocation1, 255, 0, 0);								
						}						
					}
					// If the test location isn't possible for the robot to move to
					else{
					// SYSTEM CHECK - Show which location the robot decides as invalid - lavender
					rc.setIndicatorDot(testLocation1, 230, 230, 250);
					}
					if(isLocationValid(testLocation2)){
						// Assert that no bullet will collide with the robot at this location if the robot is actually looking to dodge				
							
						if(!ifBulletLinesWillIntersect(bulletLines, testLocation2, bodyRadius)){	
							
							// SYSTEM CHECK - Show which location the robot decides as valid - navy blue dot
							rc.setIndicatorDot(testLocation2, 0, 0, 128);
							
							return testLocation2;
						}
						// If the robot would collide with a bullet in the location 
						else{
							
							// SYSTEM CHECK - Show which location the robot decides as invalid - red
							rc.setIndicatorDot(testLocation2, 255, 0, 0);								
						}						
					}
					// If the test location isn't possible for the robot to move to
					else{
					// SYSTEM CHECK - Show which location the robot decides as invalid - lavender
					rc.setIndicatorDot(testLocation2, 230, 230, 250);
					}			
				}		
			} 
			// If no locations have been deemed as valid
			
			// SYSTEM CHECK - see if the robot has actually attempted to find a point and eventually didn't
			// System.out.println("Search for a new point with given parameters unsuccesful");
			
			return null;
		}

	
	
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
		
		boolean canDodge // Final verdict of the function - outputs false if it won't correct with dodge...
						 // Outputs true if the robot must move to dodge
		){			
		 
		// Find the maximal distance away from the startingLocation that we must scan to determine the optimal location....
		float scanRadius = strideRadius + bodyRadius;		
		
		// SYSTEM CHECK - Make sure variables are being called correctly
		// System.out.println("Inputs are..... nearbyBullets: " + nearbyBullets);
		// System.out.println("bodyRadius: " + bodyRadius + "strideRadius" + strideRadius);
		
		// Obtain the locations of all bullets within the scanRadius centered at the robot's current location
		ArrayList <MapLocation> newBulletLocations = getNextBulletLocations(nearbyBullets, scanRadius, startingLocation);
		
		// Add the current bullet locations to the array list...
		for(int i = 0; i < nearbyBullets.length; i++){
			// Only add again if the they are too close
			if (startingLocation.distanceTo(nearbyBullets[i].location) <= scanRadius){
				
			newBulletLocations.add(nearbyBullets[i].location);	
			}
		}
		
		if (newBulletLocations.size() > maxIncomingBullets){
			// SYSTEM CHECK - If there are too many bullets nearby, the robot will just give up......
			System.out.println("Too many bullets nearby, will not attempt to dodge");			
			return null;
		}
		
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
					
					// SYSTEM CHECK - Tell everyone that you can dodge the bullet
					// System.out.println("Robot can dodge bullet yay!");	
					
					canDodge = true;
					return dodgeLocation;
				}
				else{
					canDodge = false;
					return dodgeLocation;
				}	
			}
			// Otherwise if none of the bullets impede on the desired location....
			else{
				canDodge = false;
				return desiredLocation;
			}
		}
		// If there are no bullets within the scanDistance.. simply return the desiredLocation as there is no conflict
		else{
			canDodge = false;
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
		Direction dir = new Direction(startingLocation, desiredLocation);
		
		// Introduce a little bit of rng
		float offset = (float)(Math.random() * (Math.PI/3) + Math.PI/3);
		
		// New offset direction to start searching from
		Direction desiredDir;
		
		// Decide based on rotation direction which one to use....
		if(rotationDirection){
			desiredDir = new Direction(dir.radians - offset);
		}
		else{
			desiredDir = new Direction(dir.radians + offset);
		}
		
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
							
							if(!ifBulletWillCollide(nearbyBulletLocations, testLocation, bodyRadius)){	
								
								// SYSTEM CHECK - Show which location the robot decides as valid - navy blue dot
								// rc.setIndicatorDot(testLocation, 0, 0, 128);
								
								return testLocation;
							}
							// If the robot would collide with a bullet in the location 
							else{
								
								// SYSTEM CHECK - Show which location the robot decides as invalid - sky blue
								// rc.setIndicatorDot(testLocation, 0, 191, 255);								
							}
						} else{ // If the robot is not currently looking to dodge, the test location has become valid
							
							// SYSTEM CHECK - Show which location the robot decides as valid - navy blue dot
							// rc.setIndicatorDot(testLocation, 0, 0, 128);
							
							return  testLocation;
						}						
					}
					// If the test location isn't possible for the robot to move to
					else{
					// SYSTEM CHECK - Show which location the robot decides as invalid - lavender
					// rc.setIndicatorDot(testLocation, 230, 230, 250);
					}
				}
				// If the test location is too far away
				else{
					// SYSTEM CHECK - Show which location the robot decides as invalid - very light blue
					// rc.setIndicatorDot(testLocation, 240, 248, 255);					
				}
			}			
		} 
		// If the function has not found a valid location, return nothing - there are no valid points for the robot.... 
		
		// SYSTEM CHECK - see if the robot has actually attempted to find a point and eventually didn't
		// System.out.println("Search for a new point with given parameters unsuccesful");
		
		return null;
	}
	
	
	// Rewrap of the canMove function... for use in this class only - takes a location to see if the robot can move to it 
	
	private static boolean isLocationValid(MapLocation testLocation){
		
		return rc.canMove(testLocation);
	}
	
	
	// Checks to see if there will be a bullet within a body radius of the currently selected location next turn. Returns true if so....
	
	public static boolean ifBulletWillCollide(ArrayList<MapLocation> allBulletLocations, MapLocation desiredLocation, float bodyRadius){
		
		// Iterate through each bullet location nearby
		for (MapLocation bulletLocation: allBulletLocations){
			// If the location is within the radius distance of the object at the desired location, return false
			
			// SYSTEM CHECK place indicator dots at the predicted locations of each of the bullets - bright red
			rc.setIndicatorDot(bulletLocation, 255, 0, 0);
			
			if(desiredLocation.distanceTo(bulletLocation) < bodyRadius){
				return true;
			}
		}
		return false;		
	}
	
	
	// Technically not correct, just tests the start and end points of the line...
	public static boolean ifBulletLinesWillIntersect(ArrayList<Line> bulletLines, MapLocation testLocation, float bodyRadius){
		
		//TODO IMplement a different intersect function -based off of distance to a line....
		
		for(Line bulletLine: bulletLines){
			// If either endpoint is within one body radius of the test location, return true...
			if(bulletLine.start.distanceTo(testLocation) <= bodyRadius || bulletLine.end.distanceTo(testLocation) <= bodyRadius || bulletLine.middle.distanceTo(testLocation) <= bodyRadius){			
				return true;
			}
		}
		// If no intersecting lines have been found...  return false - no collisions will occur...
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
	
	// Function to obtain the lines of all the bullets in this turn....
	
	public static ArrayList<Line> getBulletLines(BulletInfo[] nearbyBullets, float distance, MapLocation centre) {
		
		// Initialize a list of locations where all the bullets will be...
		ArrayList <Line> bulletLines = new ArrayList<Line>();
		
		// For each of the nearby bullets in the bullet list
		for (BulletInfo bullet: nearbyBullets) {
			
			// Retrieve the location of the bullets currently
			MapLocation currentLocation = bullet.getLocation();
			
			// Get the velocity of the bullets
			Direction currentDirection = bullet.getDir();
			float currentSpeed = bullet.getSpeed();
			
			// Calculate the location that the bullet will be at in one turn and add to the list of Locations....
			MapLocation newLocation = currentLocation.add(currentDirection, currentSpeed);	
			
			// If either endpoing of the bullet's trajectory is within the search bounds....
			if(currentLocation.distanceTo(centre) <= distance || newLocation.distanceTo(centre) <= distance){
				
				// Create the line corresponding to that bullet's path during this turn.....
				Line newLine = new Line(currentLocation, newLocation);
				
				// Add the line to the list of bullet lines....
				bulletLines.add(newLine);
				
				// SYSTEM CHECK - Display all the bullet lines....
				newLine.display();
			}
		}
		return bulletLines;
	}
	
	
	
	// Function to firstly correct any out of bounds errors created by a desired location.......
	
	public static MapLocation correctOutofBoundsError(
			
		// Input Variables
		MapLocation desiredLocation, // Location that the robot wishes to go to...
		MapLocation startingLocation, // Location that the robot is currently at....
		
		float bodyRadius, // Float representing how wide the robot
		float strideRadius // Float representing the distance the robot can travel in one turn... 

		) throws GameActionException{
		
		// Variable to store the output of this function
		MapLocation newLocation;
	
		// SYSTEM CHECK - See if the robot recognizes that it cannot currently move to the desired location
		// System.out.println("Cannot move to desired location");
		
			
		// SYSTEM CHECK - Make sure that the scouts determination of the incorrect map location is accurate...
		// System.out.println("Your current selection of Chitoge Kirisaki as best girl is incorrect.... please select Onodera to continue!!!");
		
		// Generate some random element to make reflections imperfect
		float addRandom = (float) ((Math.random() * strideRadius / 3) - (strideRadius / 6));
		
		if (!rc.onTheMap(new MapLocation(desiredLocation.x, desiredLocation.y + bodyRadius + (float) 0.01))){
			// Correct the discrepancy in the y coordinates
			float yCorrect = desiredLocation.y - startingLocation.y;				
			MapLocation newMove = new MapLocation(desiredLocation.x + addRandom, desiredLocation.y - 2 * yCorrect);
			newLocation = newMove;		
		}
		// If the robot is attempting to move to the left of the map bounds...
		else if (!rc.onTheMap(new MapLocation(desiredLocation.x - bodyRadius - (float) 0.01, desiredLocation.y))){				
			// Correct the discrepancy in the y coordinates
			float xCorrect = desiredLocation.x - startingLocation.x;
			MapLocation newMove = new MapLocation(desiredLocation.x - 2 * xCorrect, desiredLocation.y + addRandom);
			newLocation = newMove;				
		}
		// If the robot is attempting to move below the map bounds...
		else if ((!rc.onTheMap(new MapLocation(desiredLocation.x, desiredLocation.y - bodyRadius - (float) 0.01)))){				
			// Correct the discrepancy in the y coordinates
			float yCorrect = desiredLocation.y - startingLocation.y;
			MapLocation newMove = new MapLocation(desiredLocation.x + addRandom, desiredLocation.y - 2 * yCorrect);
			newLocation = newMove;				
		}
		// If the robot is attempting to move to the right of the map bounds...
		else{	
			// Correct the discrepancy in the y coordinates
			float xCorrect = desiredLocation.x - startingLocation.x;
			MapLocation newMove = new MapLocation(desiredLocation.x - 2 * xCorrect, desiredLocation.y + addRandom);
			newLocation = newMove;			
		}			
		
		// If it the corrected point is on the map
		if (rc.onTheMap(newLocation, bodyRadius)){
			
			// SYSTEM CHECK - Display the corrected move on screen as an orange line
			rc.setIndicatorLine(startingLocation, newLocation, 255, 165, 0);
			
			// TODO Check to see if switching rotation direction here is better
			return newLocation;			
		}
		
		// Otherwise if the correction is not on the map (the robot is then in a corner.... reverse directions feelsbadman
		else{
			// SYSTEM CHECK - See if object is returning the opposite line or not
			// System.out.println("Turning all the way around....");
			
			Direction oppositeDirection = new Direction(desiredLocation, startingLocation);
			
			newLocation = startingLocation.add(oppositeDirection, strideRadius);
			
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
    	
        return tryMoveInDirection(dir, 25, 4, distance, myLocation);
    }    

    
    // Function to attempt to move in a target direction (with inputed values), returns true if it actually can move in that direction
    
    public static MapLocation tryMoveInDirection(
    		
		// Input Variables    		
		Direction dir, // Target Direction of the robot
		
		float degreeOffset, // Number of degrees from the target direction that each check is done...
		int checksPerSide, // Number of checks per side of the target direction that are done...
		
		float distance, // Maximal distance away from the current robot that is supposed to be checked...
		
		MapLocation myLocation // Current Location of the robot....
		
		) throws GameActionException {
    	
    	// Generate distances to test - prioritize initial input direction
    	for (int i = 5; i >= 1; i--){
    		
    		float testDistance = (float)(i * distance / 5);    		
	        // Try going the test distance in the targeted direction
	        if (rc.canMove(dir, testDistance)) {	            
	            return myLocation.add(dir, testDistance);
	        }
        }
    	// Generate distances to test on all of the offsets
		for (int i = 5; i >= 1; i--){
		    		
		    float testDistance = (float)(i * distance / 5);    
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
    
    // Function to return a location to move to if the robot has not yet found a place to attempt to go to........
    
    public static MapLocation attemptRandomMove(MapLocation myLocation, MapLocation desiredLocation, float strideDistance) throws GameActionException{
    	
    	// SYSTEM CHECK - Show that this function has been called.....................
    	System.out.println("The robot will attempt a random move in the given direction......");
    	
    	// Obtain the desired direction wanted to travel
    	Direction directionTo = new Direction(myLocation, desiredLocation);    	
    	
    	// Test a various number of places to move towards in the general direction of the robot's desired movement....
    	return tryMoveInDirection(directionTo, 20, 9, strideDistance, myLocation);

    }
    
    public static MapLocation correctAllMove(
		
		//Input variables
		
		float strideRadius, // The stride radius of the robot		
		float bodyRadius, // The body radius of the robot
		
		boolean rotationDirection, // The current rotation orientation of the robot....
		
		Team allies, // The team that the robot is on...		
		
		MapLocation myLocation, // The current location of the robot		
		MapLocation desiredMove // The current location the robot wants to go to..   	  
    		
    	) throws GameActionException{
    	    	
    	// If no desired move was initially inputted or if the desired move already works.....
	   if(desiredMove == null){
		   return null;
	   }
	      	
	    // Check if the initially selected position was out of bounds...
		
	   	// SYSTEM CHECK - Show desired move after path planning
		// System.out.println("desiredMove before post-processing: " + desiredMove.toString());
		
		// Correct desiredMove to within one soldier  stride location of where the robot is right now....
		if(myLocation.distanceTo(desiredMove) > strideRadius){
			
	    	Direction desiredDirection = new Direction(myLocation, desiredMove);	
	    	
	    	desiredMove = myLocation.add(desiredDirection, strideRadius);
		}
		
	   	// SYSTEM CHECK - Show desired move after path planning
		// System.out.println("desiredMove after rescaling: " + desiredMove.toString());	
		
		// SYSTEM CHECK Make sure the new desired move is in the correct location LIGHT BLUE DOT
		// rc.setIndicatorDot(desiredMove, 102, 255, 255);
		
		// Check to see if the desired move is out of bounds and make it bounce off of the wall if it is...            	
		if (!rc.canMove(desiredMove)){
			MapLocation newLocation = correctOutofBoundsError(desiredMove, myLocation, bodyRadius, strideRadius);
			
			desiredMove = newLocation;
			
		   	// SYSTEM CHECK - Show desired move after path planning
	    	// System.out.println("desiredMove after out of bounds correction: " + desiredMove.toString());  
		}
	 	
		// Check if the initial desired move can be completed and wasn't out of bounds/corrected by the above functions
		if(!rc.canMove(desiredMove)){          	
			
			// SYSTEM CHECK - Print out that the robot still cannot move...
			System.out.println("The robot still cannot seem to move");
			
			
			// Check if there is a robot located at the desired move spot....
			if (rc.isLocationOccupiedByRobot(desiredMove)){
				
				// CHeck to see if the nearby unit is allied or not...
				RobotInfo closebyRobot = rc.senseRobotAtLocation(desiredMove);
				
				if(closebyRobot.ID != rc.getID()){
					
					if (closebyRobot.team == allies){
						
						// SYSTEM CHECK Show nearby colliding allies with PURPLE DOT
						rc.setIndicatorDot(closebyRobot.location, 255, 0, 255);
						
						// SYSTEM CHECK See status of movement
						System.out.println("Colliding with ally ----- will stop before collision");
						
						// Close the distance to the robot but do not bounce off...
						float distanceToClose = myLocation.distanceTo(closebyRobot.location) - closebyRobot.getRadius() - bodyRadius;
						
						// Obtain the distance towards the robot and add the gap to close the distance...
						Direction directionTo = myLocation.directionTo(closebyRobot.location);
						desiredMove = myLocation.add(directionTo, distanceToClose);            					
					}
					
					else{
						
						if (rc.getType() == RobotType.LUMBERJACK){
						// SYSTEM CHECK Show nearby colliding enemy with DARK YELLOW DOT
						rc.setIndicatorDot(closebyRobot.location, 102, 102, 0);
						
						// SYSTEM CHECK See status of movement
						System.out.println("Colliding with enemy will move towards to attack");
						
						// Close the distance to the target robot but do not bounce off...					
						float distanceToClose = myLocation.distanceTo(closebyRobot.location) - closebyRobot.getRadius() - bodyRadius;
						
						// Obtain the distance towards the robot and add the gap to close the distance...
						Direction directionTo = myLocation.directionTo(closebyRobot.location);
						
						desiredMove = myLocation.add(directionTo, distanceToClose);            					
						}
						
						// If the robot is colliding with the enemy and is not a lumberjack....
						else{
							// SYSTEM CHECK See if the robot called the attemptRandom Move function or no....
							System.out.println("Colliding with enemy and attempting to move away....");
							
							Direction directionAway = closebyRobot.location.directionTo(myLocation);
							
							// SYSTEM CHECK Show nearby colliding enemy with LIGHT YELLOW DOT
							rc.setIndicatorDot(closebyRobot.location, 255, 255, 153);
		
							
							MapLocation newLocation = tryMoveInDirection(directionAway, 15, 5, strideRadius, myLocation);
							
							desiredMove = newLocation;
						}
					}
				}
				// The robot just detected its own self so just treat like tree.....
				else{
					// Get the distance to the previous desired point - use for calculating new place to move to....
					float reCalc = myLocation.distanceTo(desiredMove);
					
					MapLocation newLocation = attemptRandomMove(myLocation, desiredMove, strideRadius / 2 );
					
					// SYSTEM CHECK See if the robot called the attemptRandom Move function or no....
					System.out.println("Attempted to find a new location to move to randomly...");
					
					desiredMove = newLocation;					
				}
			}
			// Otherwise the current desired move is likely a tree....			
			else{            							
				// Get the distance to the previous desired point - use for calculating new place to move to....
				float reCalc = myLocation.distanceTo(desiredMove);
				
				MapLocation newLocation = attemptRandomMove(myLocation, desiredMove, strideRadius / 2 );
				
				// SYSTEM CHECK See if the robot called the attemptRandom Move function or no....
				System.out.println("Attempted to find a new location to move to randomly...");
				
				desiredMove = newLocation;						
			}
	       	// SYSTEM CHECK - Show desired move after path planning
	    	// System.out.println("desiredMove after collision correcetion " + desiredMove.toString());
		} 
		
	    return desiredMove;
    }
    
    // Function that returns true if the robot is near a corner or false if not....
    public static int checkIfNearCorner(float bodyRadius, float strideRadius, MapLocation myLocation) throws GameActionException{
    	
    	// Get locations surrounding robot....    	
    	MapLocation northLocation = new MapLocation (myLocation.x, myLocation.y + 2 * bodyRadius + strideRadius);
    	MapLocation southLocation = new MapLocation (myLocation.x, myLocation.y - 2 * bodyRadius - strideRadius);
    	MapLocation eastLocation = new MapLocation (myLocation.x + 2 * bodyRadius + strideRadius, myLocation.y);
    	MapLocation westLocation = new MapLocation (myLocation.x - 2 * bodyRadius - strideRadius, myLocation.y);
    	
    	// If the robot is in the top right corner...
    	if(!rc.onTheMap(eastLocation) && !rc.onTheMap(northLocation)){
    		return 1;
    	}
    	
    	// If the robot is in the top left corner....
    	else if(!rc.onTheMap(westLocation) && !rc.onTheMap(northLocation)){
    		return 2;
    	}
    	
    	// If the robot is in the bottom left corner....
    	else if(!rc.onTheMap(westLocation) && !rc.onTheMap(southLocation)){
    		return 3;
    	}
    	
    	// If the robot is in the bottom right corner....
    	else if(!rc.onTheMap(eastLocation) && !rc.onTheMap(southLocation)){
    		return 4;
    	}

    	else{
    		return 0;
    	}    	
    }   
    
    // Function to remove a robot from a corner...
    
    public static MapLocation moveOutOfCorner(float strideRadius, int corner, MapLocation myLocation){
    	
    	// Generate a random direction to choose which direction to move to....
    	float randomNumber = (float) Math.random();
    	
    	for (int i = 5; i >= 0; i--){
			
			// Get the distance to move away for..... and the resulting map location
			float testDistance = strideRadius / 5 * i;	
			
			// Placeholder for the location to test to go to......
			MapLocation testLocation = null;
	    	
			// Generate the locations to test.....
	    	if (corner == 1){    		
	    		if(randomNumber >= 0.5){	    			
	    			testLocation =  myLocation.add(Direction.getWest(), testDistance);
	    		} else{
	    			testLocation =  myLocation.add(Direction.getSouth(), testDistance);	
	    		}
	    	}
	    	else if (corner == 2){
	    		if(randomNumber >= 0.5){	    			
	    			testLocation =  myLocation.add(Direction.getSouth(), testDistance);
	    		} else{
	    			testLocation =  myLocation.add(Direction.getEast(), testDistance);	
	    		}
	    	}
	    	else if (corner == 3){
	    		if(randomNumber >= 0.5){	    			
	    			testLocation =  myLocation.add(Direction.getEast(), testDistance);
	    		} else{
	    			testLocation =  myLocation.add(Direction.getNorth(), testDistance);	
	    		}
	    	}
	    	else if (corner == 4){
	    		if(randomNumber >= 0.5){	    			
	    			testLocation =  myLocation.add(Direction.getNorth(), testDistance);
	    		} else{
	    			testLocation =  myLocation.add(Direction.getWest(), testDistance);	
	    		}
	    	}
			// If the robot can move in that direction....
			if (rc.canMove(testLocation)){
				// SYSTEM CHECK - Print a BLUE DOT in the location that the robot can move...
				rc.setIndicatorDot(testLocation, 0, 0, 255);
				return testLocation;
			}
			else{
				// SYSTEM CHECK - Print a DARK BLUE DOT that the location canno move to...
				rc.setIndicatorDot(testLocation, 0, 0, 102);	    			
    		}    	
    	}
    	// If no corner locations can be found...
    	return myLocation;
    }
}

