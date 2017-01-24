// AI for Archon
package naclbot.units.AI.archon;
import battlecode.common.*;

import naclbot.variables.GlobalVars;
import naclbot.variables.DataVars;
import naclbot.variables.DataVars.*;

import naclbot.variables.BroadcastChannels;
import naclbot.units.interact.iFeed;
import naclbot.units.motion.*;
import naclbot.units.motion.routing.Routing;
import naclbot.units.motion.search.TreeSearch;

import java.util.ArrayList;
import java.util.Arrays;


/* ------------------   Overview ----------------------
 * 
 * AI Controlling the functions of the ScoutBot
 *
 * ~~ Coded by Illiyia (akimn@#mit.edu)
 * 
 * Debug statements all begin with SYSTEM CHECK 
 * 
 ---------------------------------------------------- */

public class ArchonBot extends GlobalVars {
	
	// Variable for storing the current round of the game
	public static int remIsBestGirl = 0;
	
	// The intial round in which the archon was initialized
	public static int initRound;
	
	// Variables important to self and team recognition
	public static int myID;
	private static Team enemy;
	private static Team allies;	
	private static final float strideRadius = battlecode.common.RobotType.ARCHON.strideRadius;
	private static final float bodyRadius = battlecode.common.RobotType.ARCHON.bodyRadius;

	// Variable storing number of additional victory points needed to win
	public static float victoryPointsToWin;
	
	// Parameters to store location of self (at beginning of round)
	public static MapLocation myLocation;

	// Direction at which the archon traveled last
	private static Direction lastDirection;
	private static MapLocation lastPosition;
	
	// Direction for use each round
	private static Direction myDirection;	
	
	   // Variable to determine after how long archons decide that Alan's code is a piece of shit......
    public static final int giveUpOnRouting = 100;
    
    // Variable to store the amount of time currently in routing....
    public static int roundsRouting = 0;
    
    // Arraylist to store path for routing....    
    public static ArrayList<MapLocation> routingPath;
    
    // Total number of gardeners hired by this archon...
    public static int totalGardenersHired;
     
    // Radius at which archon attempts to disperse
    public static final int disperseRadius = 8;
    
    // Stores the total number of gardeners contained
    public static int numberofGardenersConstructed;
    
    // Rotation direction of the archon naturally
    public static boolean rotationDirection;
    
    // Discreteness of  the initial search to find nearest walls...
    public static final float initialWallCheckGrain = 1;
    
    // Value to store the minimum number of time per new gardener construction...
    public static final int gardenerConstructionBreak = 50;
    
    // Value to store the length of tiem for which a gardener has not been constructed...
    public static int roundsNotConstructed = 0;
    
    // TO BE CHANGED
    // GENNERAL LIMITS FOR GARDENER PRODUCTION OVER A GAME CIRCLE
    public static int getGardenerLimit(int roundNumber){
    	
    	if (roundNumber <= 200){
    		return 4;
    	}
    	else if(roundNumber <= 500){
    		return (2 + roundNumber / 100);
    	}
    	else{
    		return (3 + roundNumber / 125);    	
    	}
    }    
    	
	// Starting game phase
	
	public static void init() throws GameActionException {
		
		// SYSTEM CHECK Initialization start check
		System.out.println("Archon initialized!");

		// Initialize variables important to self, team, and opponent 
		myID = rc.getID();
		enemy = rc.getTeam().opponent();
		allies = rc.getTeam();
			
		numberofGardenersConstructed = rc.readBroadcast(BroadcastChannels.GARDENERS_CONSTRUCTED_CHANNELS);
		
		rotationDirection = false;
		
		myLocation = rc.getLocation();
		totalGardenersHired = 0;
		
        remIsBestGirl = rc.getRoundNum();
        initRound = remIsBestGirl;
        
        roundsNotConstructed = 0;
		
	    // Initialize path list and goal location
       	routingPath = new ArrayList<MapLocation>();    	
       	Routing.setRouting(routingPath);
		constructGardeners(2);	
	}
	
	public static void constructGardeners(int maxGardeners) throws GameActionException {		
		
		// Loop to terminate the starting phase of the robot
		boolean checkStatus = true;		
		
		// Variable to store the number of gardeners hired in this phase....
		int hiredGardeners = 0;
		
        // Starting phase loop
        while (hiredGardeners < maxGardeners) {

            // Try/catch blocks stop unhandled exceptions, - print stacktrace upon exception error....
            try {
            	// SYSTEM CHECK - Print out the gardener limit and the current number of gardeners constructed
            	System.out.println("Gardener Limit: " + getGardenerLimit(remIsBestGirl) + ", current constructed number: " + numberofGardenersConstructed);
            	
            	if (remIsBestGirl == 1){
            		lastDirection = getInitialWalls();
            	}
            	// Get the total number of gardeners constructed thus far.....
            	numberofGardenersConstructed = rc.readBroadcast(BroadcastChannels.GARDENERS_CONSTRUCTED_CHANNELS);
            	
            	rc.setIndicatorDot(myLocation.add(lastDirection,10), 155, 135, 244);
            	
            	// SYSTEM CHECK - Inform that the archon is attempting to construct a gardener....
            	System.out.println("Attempting to hire a gardener. Can hire a maximum of: " + maxGardeners + ", currently hired: " + hiredGardeners);
            	
            	// Boolean to determine whether or not the archon attempted to hire a gardener this turn or not......
            	boolean hiredGardener = false;  
            	
            	// Update own location
            	myLocation = rc.getLocation();
            	
            	// STore the location that archon wants to go to....
            	MapLocation desiredMove = myLocation;
            	
            	// Initialize information about world......
               	RobotInfo[] enemyRobots = NearbyUnits(enemy, -1);
            	RobotInfo[] alliedRobots = NearbyUnits(allies, -1);
            	BulletInfo[] nearbyBullets = rc.senseNearbyBullets();
            	
            	// Update the current round number.....
            	remIsBestGirl = rc.getRoundNum();
  
            	Direction testDirection = new Direction(lastDirection.radians + (float) Math.PI);
            	
            	Direction gardenerDirection = tryHireGardener(testDirection);
            	
            	// If the archon can hire a gardener in a certain direction...
            	if (gardenerDirection != null && (roundsNotConstructed >= gardenerConstructionBreak || remIsBestGirl <= 20)){
            		// Assert that the archon can actually hire it (i.e. not limited by previous hiring
            		if (rc.canHireGardener(gardenerDirection)){
	            		rc.hireGardener(gardenerDirection);
	            		
	            		// Increment counters.....
	            		hiredGardener = true;
	            		hiredGardeners += 1;
	            		totalGardenersHired += 1;
	            		rc.broadcast(BroadcastChannels.GARDENERS_CONSTRUCTED_CHANNELS, numberofGardenersConstructed+1);
	            	}
            	}
            	
            	
            	MapLocation disperseLocation = moveAwayfromGardeners(alliedRobots);
            	
            	if (disperseLocation != null){            		
            		desiredMove = disperseLocation;
            	}
            	
            	if (remIsBestGirl <= 15){
            		desiredMove = myLocation.add(lastDirection, strideRadius);
            	}
            	// Call the function to correct a move and actually move......
            	moveCorrect(desiredMove, rotationDirection, nearbyBullets);       	
      
            	// Update the last position of the robot to get the heading of the archon in the previous turn....
	        	lastPosition =  rc.getLocation();
	            lastDirection = new Direction(myLocation, lastPosition);
                  
	            remIsBestGirl += 1;
	            roundsNotConstructed += 1;
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Archon Exception in Starting Phase");
                e.printStackTrace();
            }
        }
        
        // Move to the mainPhase of operations
        idle();
    }
	
	
	// Default state of archon when it is not building anything......
	
	public static void idle() throws GameActionException {

		
        while (true) {
        	
            // catch 
            try {            	
              	// SYSTEM CHECK - Print out the gardener limit and the current number of gardeners constructed
            	System.out.println("Gardener Limit: " + getGardenerLimit(remIsBestGirl) + ", current constructed number: " + numberofGardenersConstructed);
            	
            	// Get the total number of gardeners constructed thus far.....
            	numberofGardenersConstructed = rc.readBroadcast(BroadcastChannels.GARDENERS_CONSTRUCTED_CHANNELS);
            	
            	if (numberofGardenersConstructed < getGardenerLimit(remIsBestGirl)){
            		constructGardeners(1);
            	}
            	
            	// SYSTEM CHECK - Inform that the archon is attempting to construct a gardener....
            	System.out.println("Currently not doing anything..............." );
            	
            	// Update own location
            	myLocation = rc.getLocation();
            	
            	// STore the location that archon wants to go to.... it doesnt want to move by default
            	MapLocation desiredMove = myLocation;
            	
            	
            	// Initialize information about world......
               	RobotInfo[] enemyRobots = NearbyUnits(enemy, -1);
            	RobotInfo[] alliedRobots = NearbyUnits(allies, -1);
            	BulletInfo[] nearbyBullets = rc.senseNearbyBullets();
            	
            	// Update the current round number.....
            	remIsBestGirl = rc.getRoundNum();
  
            	Direction testDirection = new Direction(lastDirection.radians + (float) Math.PI);

            	MapLocation disperseLocation = moveAwayfromGardeners(alliedRobots);
            	
            	if (disperseLocation != null){            		
            		desiredMove = disperseLocation;
            	}
            	
            	// Call the function to correct a move and actually move......
            	moveCorrect(desiredMove, rotationDirection, nearbyBullets);       	
      
            	// Update the last position of the robot to get the heading of the archon in the previous turn....
	        	lastPosition =  rc.getLocation();
	            lastDirection = new Direction(myLocation, lastPosition);
                  
	            remIsBestGirl += 1;           	
	            roundsNotConstructed += 1;
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                
                e.printStackTrace();
            }
        }
    }
        
	// Function to get the archon to move away from gardeners to give them more space....
	
	private static MapLocation moveAwayfromGardeners(RobotInfo alliedRobots[]){
		
		// Placeholder variables to store the minimum distance to a friendly gardener and the gardener's data
		float minimum = Integer.MAX_VALUE;
		RobotInfo nearestRobot = null;
		
		// ITerate through all the allied robots....
		for (RobotInfo alliedRobot: alliedRobots){
			
			// Assert that the robot is a gardener, has the minimal found distance and is within bounds to make the archon disperse...
			if (alliedRobot.type == battlecode.common.RobotType.GARDENER && myLocation.distanceTo(alliedRobot.location) < minimum 
					&& myLocation.distanceTo(alliedRobot.location) < disperseRadius){
				nearestRobot = alliedRobot;
				minimum = myLocation.distanceTo(alliedRobot.location);
				
			}
		}
		// If there is a gardener nearby, attempt to move away from it
		if (nearestRobot != null){
			// Get the distance away that the robot is
			float distanceTo = myLocation.distanceTo(nearestRobot.location);
			
			// The direction that the robot needs to travel to get away from said robot
			Direction moveAway = new Direction(nearestRobot.location, myLocation);
			
			// Attempt to move directly away from it to a point that is exactly one disperseRadius from the robot
		
			MapLocation newLocation = myLocation.add(moveAway, strideRadius);
			rc.setIndicatorLine(myLocation, myLocation.add(moveAway, 25), 125, 125, 0);
			return newLocation;
			
		}
		// If no gardeners are close enough to warrant walking away, return nothing....
		else{
			return null;
		}
	}
	
	// Find a direction to hire a direction or return null if there isn't one found...
	
	private static Direction tryHireGardener(Direction tryDirection){
		
		// Iterate over 36 different angles, starting near the inputed direction and diverging away to see if a gardener can be built in any one of those locations..
		for (int i = 0; i <= 18; i++){
			
			Direction newHireDirection = new Direction(tryDirection.radians + i * (float) (Math.PI/18));
			if (rc.canHireGardener(newHireDirection)){
				return newHireDirection;
			}
			newHireDirection = new Direction(tryDirection.radians - i * (float) (Math.PI/18));
			if (rc.canHireGardener(newHireDirection)){
				return newHireDirection;			
			}			
		}
		return null;
	}
					
	private static RobotInfo[] NearbyUnits(Team team, float distance){
		
		return rc.senseNearbyRobots(myLocation, distance, team);
	}
	
	// Copy of the code from the scoutbot - correct movement after finding a location......
	
	private static void moveCorrect(MapLocation desiredMove, boolean rotationDirection, BulletInfo[] nearbyBullets) throws GameActionException{
		
		// Correct desiredMove to within one soldier  stride location of where the robot is right now....
    	if(myLocation.distanceTo(desiredMove) > strideRadius){
    		
        	Direction desiredDirection = new Direction(myLocation, desiredMove);	
        	
        	desiredMove = myLocation.add(desiredDirection, strideRadius);
    	}
    	// Make the robot bounce of walls if they are too far......
    	if (!rc.canMove(desiredMove)){
    		MapLocation newLocation = Yuurei.correctOutofBoundsError(desiredMove, myLocation, bodyRadius, strideRadius, rotationDirection);
    		
    		myDirection = new Direction(myLocation, newLocation);
    		
    		desiredMove = newLocation;
    	}
    	
    	// Check if the initial desired move can be completed and wasn't out of bounds/corrected by the above function
    	if(!rc.canMove(desiredMove)){          		
    	
			MapLocation newLocation = Yuurei.attemptRandomMove(myLocation, desiredMove, strideRadius);
			
			desiredMove = newLocation;
    	}     	
    	
    	// --------------------------- DODGING ------------------------ //
    	
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
    	}
    	
    	// See whether or not the robot can move to the current desired move, and move if it does
    	if(rc.canMove(desiredMove)){
    		rc.move(desiredMove);
    	}           	
    	else{
    		// SYSTEM CHECK - Make sure that the robot didn't move because it didn't want to....
    		// System.out.println("This robot did not move because it did not want to....");
    	}    	
	}
	
	// Function to obtain the initial direction that the archon will attempt to build something - checks all nearby walls 
	// so that it gets the most open space to move forward and construct additional gardeners
	
	private static Direction getInitialWalls() throws GameActionException{
		
		// Initialize an array to store the  distances to any walls (obstacles to building)
		float[] wallDistances = new float[16]; 
		Arrays.fill(wallDistances, 0);
		
		// Check up to a distance of 9 away... and 16 different angles
		for(int i = 1; i <= 9 / initialWallCheckGrain; i++){
			for(int j = 0; j < 16; j++){
				
				// Get the locations to check for the obstacles
				Direction directionToCheck = new Direction((float)(j * Math.PI/8));
				float distanceToCheck = i * initialWallCheckGrain;
				MapLocation locationToCheck = myLocation.add(directionToCheck, distanceToCheck);
				
				// Check to see if the considered point is off the map or has a tree
				if(!rc.isLocationOccupiedByTree(locationToCheck) && rc.onTheMap(locationToCheck)){
					
					// SYSTEM CHECK If the location is free print a BLUE DOT
					rc.setIndicatorDot(locationToCheck, 0, 0, 128);
					
					if (wallDistances[j] >= 0){
						wallDistances[j] = i * initialWallCheckGrain;
					}
				}
				else{
					
					// SYSTEM CHECK - If the location isn't free print a RED DOT
					rc.setIndicatorDot(locationToCheck, 128, 0, 0);
					
					if (wallDistances[j] >= 0){
						wallDistances[j] *= -1;
					}				
				}
			}
		}
		// Placeholder to find the minimum (moving average of three directions)
		int index = -1;
		float maximum = Integer.MIN_VALUE;
		
		// Iterate over the fall wall Distances....
		for (int i = 0; i < wallDistances.length; i ++){
			
			// Values to get the indices to iterate over....
			int indexAbove = (i+1) % 16;
			int indexBelow = (i+15) % 16;
			
			// Obtain the average of the three wall distances  surrounding the current index
			float average = Math.abs(wallDistances[i]) + (Math.abs(wallDistances[indexAbove]) + Math.abs(wallDistances[indexBelow]))/2;
			if (average > maximum){
				maximum = average;
				index = i;
			}			
		}
		
		// Calculate the direction to go to...
		Direction newDirection = new Direction(index * (float) (Math.PI/8));
		
		// SYSTEM CHECK - Put a green line to show which direction the arcon will attempt to move...
		rc.setIndicatorLine(myLocation, myLocation.add(newDirection,10), 0, 125, 0);
		
		return newDirection;
	}
}