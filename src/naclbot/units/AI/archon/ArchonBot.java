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
	
	// Miscellaneous variables.....
	private static boolean believeHasDied; // Stores whether or not the robot believes it will die this turn or not.........

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
    public static final int disperseRadius = 7;
    
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
    
    // Determines what is considered empty
    public static float crowdThresh = (float)0.5;
    
    public static int gardenerNumber = 0;
    
    public static int initMove = 30;
    
    public static MapLocation initialGoal;
    
    public static MapLocation lastBuilt;
    
    public static RobotInfo[] alliedRobots;
    public static RobotInfo[] enemyRobots;
	public static BulletInfo[] nearbyBullets;
	
	public static MapLocation desiredMove;
	
	public static int nearbyBulletTreeCount;
	public static int chosenBulletTreeChannel = 0;
    
    // GENNERAL LIMITS FOR GARDENER PRODUCTION OVER A GAME CYCLE
    public static int getGardenerLimit(int roundNumber) throws GameActionException{
    	//initialize tree and gardener counts 
    	float treeCount = rc.getTreeCount(); //directly proportional to bullet income
    	int gardenerCount = rc.readBroadcast(BroadcastChannels.GARDENER_NUMBER_CHANNEL);
    	
    	// Offset for mid game 
    	if (roundNumber <= 200){
    		return 0;
    	}
    	
    	// Make exponential
    	else if(roundNumber <= 1000){
    		return (int) (2 + roundNumber / 100 + treeCount / 10);
    	} else {
    		return (int) (3 + roundNumber / 125 + treeCount / 10);    	
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
		
		// Let everyone know where the archon started off......
		rc.broadcast(BroadcastChannels.ARCHON_INITIAL_LOCATION_X, (int) (rc.getLocation().x * 100));
		rc.broadcast(BroadcastChannels.ARCHON_INITIAL_LOCATION_Y, (int) (rc.getLocation().y * 100));
		
		rotationDirection = false;
		
		myLocation = rc.getLocation();
		totalGardenersHired = 0;
		
        remIsBestGirl = rc.getRoundNum();
        initRound = remIsBestGirl;
        
        roundsNotConstructed = 0;
        
        // Check initial tree conditions
        float crowd = checkBuildRadius((float)15, (float)5, (float)0.5);
        float ratioTree = 1;
        
        int treeNum = (int)((1-crowd)*ratioTree);
        if (treeNum <= 0) {
        	treeNum = 1;
        }
        
	    // Initialize path list and goal location
//       	routingPath = new ArrayList<MapLocation>();    	
//       	Routing.setRouting(routingPath);

        broadcastBulletTreeCount();
        
        System.out.println("broadcasts: " + rc.readBroadcast(20) + rc.readBroadcast(21));
        System.out.println(rc.readBroadcast(22) + rc.readBroadcast(23));
        System.out.println(rc.readBroadcast(24) + rc.readBroadcast(25));
        
        
        rc.broadcastFloat(BroadcastChannels.ARCHONS_TREE_DENSITY_CHANNEL, calculateTreeDensity());
        
		constructGardeners(treeNum);
	}
	
	public static void constructGardeners(int maxGardeners) throws GameActionException {		
		
		// Loop to terminate the starting phase of the robot
		boolean checkStatus = true;		
		
		// Variable to store the number of gardeners hired in this phase....
		int hiredGardeners = 0;
		
        // Starting phase loop
        while ((hiredGardeners < maxGardeners)) {

            // Try/catch blocks stop unhandled exceptions, - print stacktrace upon exception error....
            try {
            	//--INITIALIZATION--
            	// Update own location
            	myLocation = rc.getLocation();
            	
            	// Store the location that archon wants to go to....
            	desiredMove = myLocation;
            	
            	// Initialize information about world......
               	enemyRobots = NearbyUnits(enemy, -1);
            	alliedRobots = NearbyUnits(allies, -1);
            	nearbyBullets = rc.senseNearbyBullets();
            	
            	// Update the current round number.....
            	remIsBestGirl = rc.getRoundNum();
            	
            	
            	// Scan surrounding first:
            	boolean crowded = (checkBuildRadius((float)30, (float)2.5, (float)0.5) >= crowdThresh);
            	
            	// SYSTEM CHECK - Print out the gardener limit and the current number of gardeners constructed
            	System.out.println("Gardener Limit: " + getGardenerLimit(remIsBestGirl) + ", current constructed number: " + numberofGardenersConstructed);
            	
            	if (remIsBestGirl == 1){
            		lastDirection = getInitialWalls();
            		initialGoal = rc.getLocation().add(lastDirection, 10);
            	}
            	// Get the total number of gardeners constructed thus far.....
            	numberofGardenersConstructed = rc.readBroadcast(BroadcastChannels.GARDENERS_CONSTRUCTED_CHANNELS);
            	
            	rc.setIndicatorDot(myLocation.add(lastDirection,10), 155, 135, 244);
            	
            	// SYSTEM CHECK - Inform that the archon is attempting to construct a gardener....
            	System.out.println("Attempting to hire a gardener. Can hire a maximum of: " + maxGardeners + ", currently hired: " + hiredGardeners);
            	
            	// Boolean to determine whether or not the archon attempted to hire a gardener this turn or not......
            	boolean hiredGardener = false;  
            	 	
            	
            	Direction testDirection = null;
            	Direction gardenerDirection = null;
            	
            	// Build if not crowded
            	if (!crowded || (remIsBestGirl <= initMove)) {
//            		testDirection = new Direction(lastDirection.radians + (float) Math.PI);
            		if (lastBuilt != null) {
            			testDirection = new Direction(rc.getLocation(), lastBuilt); 
            		}
            		else {
            			testDirection = new Direction(lastDirection.radians + (float) Math.PI);
            		}
                	gardenerDirection = tryHireGardener(testDirection);
            	}
  
            	// If the archon can hire a gardener in a certain direction...
            	if (gardenerDirection != null/* && (roundsNotConstructed >= gardenerConstructionBreak || remIsBestGirl <= 20)*/){
            		// Assert that the archon can actually hire it (i.e. not limited by previous hiring
            		if (rc.canHireGardener(gardenerDirection)){
	            		rc.hireGardener(gardenerDirection);
	            		
	            		// Increment counters.....
	            		hiredGardener = true;
	            		hiredGardeners += 1;
	            		totalGardenersHired += 1;
	            		
	            		// Update broadcasted counter
	            		rc.broadcast(BroadcastChannels.GARDENERS_CONSTRUCTED_CHANNELS, numberofGardenersConstructed+1);
	            		
	            		//update last built location
	            		lastBuilt = rc.getLocation();
	            		
	            		//gets archons to move away from gardeners
	            		System.out.println("dispersing archons");
	                	disperseFromGardeners();
	            	}
            	}
            	
      
            	// Update the last position of the robot to get the heading of the archon in the previous turn....
	        	lastPosition =  rc.getLocation();
	            lastDirection = new Direction(myLocation, lastPosition);
//	        	lastDirection = new Direction(lastPosition, myLocation);
	            System.out.println("Hired num: " + hiredGardeners);
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
            	//update round number 
            	remIsBestGirl = rc.getRoundNum();

            	// Update own location
            	myLocation = rc.getLocation();
            	
            	// STore the location that archon wants to go to.... it doesnt want to move by default
            	MapLocation desiredMove = myLocation;	
            	
            	// Initialize information about world......
               	enemyRobots = NearbyUnits(enemy, -1);
            	alliedRobots = NearbyUnits(allies, -1);
            	nearbyBullets = rc.senseNearbyBullets();
  
            	
            	//check if we can cash in bullets for victory points 
            	Win();
            	
              	// SYSTEM CHECK - Print out the gardener limit and the current number of gardeners constructed
//            	System.out.println("Gardener Limit: " + getGardenerLimit(remIsBestGirl) + ", current constructed number: " + numberofGardenersConstructed);
            	
            	// Get the total number of units constructed thus far.....
//            	numberofGardenersConstructed = rc.readBroadcast(BroadcastChannels.GARDENERS_CONSTRUCTED_CHANNELS);
            	int gardenerCount = rc.readBroadcast(BroadcastChannels.GARDENER_NUMBER_CHANNEL);
            	int soldierCount = rc.readBroadcast(BroadcastChannels.SOLDIER_NUMBER_CHANNEL);
                int lumberjackCount = rc.readBroadcast(BroadcastChannels.LUMBERJACK_NUMBER_CHANNEL);
                
        		// Check surroundings
        		boolean crowded = (checkBuildRadius((float)30, (float)3, (float)0.5) >= crowdThresh);
        		
        		
                System.out.println("Gardener Limit: " + getGardenerLimit(remIsBestGirl) + ", current constructed number: " + gardenerCount);
                
                if ((gardenerCount <= 0)) {
                	constructGardeners(1);
                } else if (!crowded && (gardenerCount < getGardenerLimit(remIsBestGirl))) {
                	constructGardeners(1);                		
                } 
            	
            	// SYSTEM CHECK - Inform that the archon is attempting to construct a gardener....
            	System.out.println("Currently not doing anything..............." );
            	     	
            	Direction testDirection = new Direction(lastDirection.radians + (float) Math.PI);
            	
            	
            	MapLocation disperseLocation;
            	if (remIsBestGirl > initMove) {
//            		disperseLocation = moveAwayfromGardeners(alliedRobots);
            		disperseLocation = findOptimalSpace(30, strideRadius+bodyRadius, strideRadius+bodyRadius, testDirection.getAngleDegrees());
            		if (disperseLocation != null){            		
                		desiredMove = disperseLocation;
                	}
            		
//            		moveCorrect(desiredMove, rotationDirection, nearbyBullets);
            		rc.move(Yuurei.correctAllMove(strideRadius, bodyRadius, false, rc.getTeam(), rc.getLocation(), desiredMove));
            	}
            	else {
            		System.out.println("Moving in general direction");
            		if (initialGoal != null) {
            			Direction dir = new Direction (rc.getLocation(), initialGoal);
            			
            			desiredMove = myLocation.add(dir, strideRadius);
//            			moveCorrect(desiredMove, rotationDirection, nearbyBullets);
            			rc.setIndicatorLine(rc.getLocation(), desiredMove, 255, 0, 0);
            			rc.move(Yuurei.correctAllMove(strideRadius, bodyRadius, false, rc.getTeam(), rc.getLocation(), desiredMove));
            			/*
            			for(int i = 0; i < strideRadius; i+=strideRadius/4) {
            				if(Move.tryMoveWithDist(dir, 1, 10, i)) {
            					break;
            				}
            			}*/
            		}
            		
//            		desiredMove = myLocation.add(lastDirection, strideRadius);
//            		rc.setIndicatorLine(rc.getLocation(), desiredMove, 255, 0, 0);
            	}

            	/*MapLocation disperseLocation;
            	if (remIsBestGirl < 15) {
            		disperseLocation = moveAwayfromGardeners(alliedRobots);
            	}
            	else {
            		disperseLocation = findOptimalSpace(30, strideRadius+bodyRadius, strideRadius+bodyRadius, testDirection.getAngleDegrees());
            	}
            	
            	if (disperseLocation != null){            		
            		desiredMove = disperseLocation;
            	}
            	
            	
            	// Call the function to correct a move and actually move......
            	moveCorrect(desiredMove, rotationDirection, nearbyBullets);*/       	
      
            	// Update the last position of the robot to get the heading of the archon in the previous turn....
	        	lastPosition =  rc.getLocation();
	            lastDirection = new Direction(myLocation, lastPosition);
	            System.out.println("current round number: " + remIsBestGirl);
	            roundsNotConstructed += 1;
	            
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                
                e.printStackTrace();
            }
        }
    }
	
	public static void manageBeingAttacked(MapLocation loc) throws GameActionException{
		boolean beingAttacked = iFeed.willBeAttacked(loc);
		float currentHealth = rc.getHealth();
		float maxHealth = rc.getType().getStartingHealth();
		if (beingAttacked && currentHealth < (0.5*maxHealth)) {
			// BIT 0 - GIVES MY X
			// BIT 1 - GIVES MY Y
			int numArchonsBeingAttacked = rc.readBroadcast(BroadcastChannels.ARCHONS_UNDER_ATTACK);
			int channelStartNum = BroadcastChannels.ARCHONS_UNDER_ATTACK + numArchonsBeingAttacked * 2;
			
			rc.broadcastFloat(channelStartNum + 1, loc.x);
			rc.broadcastFloat(channelStartNum + 2, loc.y);
			
			boolean willDie = iFeed.willFeed(loc);
			if (willDie) {
				believeHasDied = true;
				
				// Get the current number of soldiers in service
		        int numberOfAliveGardeners = rc.readBroadcast(BroadcastChannels.GARDENERS_ALIVE_CHANNEL);
		        
		        // Update soldier number for other units to see.....
		        rc.broadcast(BroadcastChannels.GARDENERS_ALIVE_CHANNEL, numberOfAliveGardeners - 1);

			}
		}
	}
        
	private static void disperseFromGardeners() throws GameActionException { 
		//initialize variables
    	MapLocation disperseLocation;
    	MapLocation correctedLocation;
    	int idleCount = 0; 	
    	
    	while (checkNearbyGardeners(7) && idleCount <= 20) { 
    		//keeps trying to move away from gardener until there is none in 7 unit radius or it's been trying to move for 20 turns
    		
    		alliedRobots = rc.senseNearbyRobots(7);
    		myLocation = rc.getLocation(); 
    		
    		disperseLocation = moveAwayfromGardeners(alliedRobots);
    		//disperseLocation = findOptimalSpace(30, strideRadius+bodyRadius, strideRadius+bodyRadius, testDirection.getAngleDegrees());
    		if (disperseLocation != null){            		
        		desiredMove = disperseLocation;
        	}
    		
//    		moveCorrect(desiredMove, rotationDirection, nearbyBullets);
    		
    		correctedLocation = Yuurei.correctAllMove(strideRadius, bodyRadius, false, rc.getTeam(), rc.getLocation(), desiredMove);
    		if (rc.canMove(correctedLocation)) {
    			System.out.println("moving away rn");
    				rc.move(correctedLocation);
    		}
    		
    		idleCount++;
    		/*
	    		System.out.println("Moving in general direction while dispersing");
	    		if (initialGoal != null) {
	    			System.out.println("hi I'm moving");
	    			Direction dir = new Direction (rc.getLocation(), initialGoal);
	    			
	    			desiredMove = myLocation.add(dir, strideRadius);
	//    			moveCorrect(desiredMove, rotationDirection, nearbyBullets);
	    			
	    			rc.setIndicatorDot(desiredMove, 0, 255, 0);
	    			
	    			correctedLocation = Yuurei.correctAllMove(strideRadius, bodyRadius, false, rc.getTeam(), rc.getLocation(), desiredMove);
	    			rc.move(correctedLocation);
	    			
	    			/*
	    			for(int i = 0; i < strideRadius; i+=strideRadius/4) {
	    				if(Move.tryMoveWithDist(dir, 1, 10, i)) {
	    					break;
	    				}
	    			}*/
	    	
	    	Clock.yield();
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
			System.out.println("distance away from nearest robot: " + distanceTo);
			// The direction that the robot needs to travel to get away from said robot
			Direction moveAway = new Direction(nearestRobot.location, myLocation);
			
			// Attempt to move directly away from it to a point that is exactly one disperseRadius from the robot
		
			MapLocation newLocation = myLocation.add(moveAway, strideRadius);
			
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
	
	public static boolean checkNearbyGardeners(float radius) {
		RobotInfo[] nearbyRobots = rc.senseNearbyRobots(radius);
		
		for (RobotInfo robot: nearbyRobots) {
			if (robot.getType() == RobotType.GARDENER) {
				return true;
			}
		}
		return false;
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
    		MapLocation newLocation = Yuurei.correctOutofBoundsError(desiredMove, myLocation, bodyRadius, strideRadius);
    		
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
					//rc.setIndicatorDot(locationToCheck, 0, 0, 128);
					
					if (wallDistances[j] >= 0){
						wallDistances[j] = i * initialWallCheckGrain;
					}
				}
				else{
					
					// SYSTEM CHECK - If the location isn't free print a RED DOT
					//rc.setIndicatorDot(locationToCheck, 128, 0, 0);
					
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
	
	//----------------------------------------------------------------------------------------------------
	// Alan's Work
	// Imported from gardeners
	// Check build radius
	public static float checkBuildRadius(float angleInterval, float dist, float distRad) throws GameActionException {
		float crowdedFactor = 0;
		
		// Define direction variable
		Direction dir = new Direction(0);
		
		// Get current location
		MapLocation curLoc = rc.getLocation();
		
		// Make sure only one revolution is check
		float totalAngle = 0;
		
		// Scan for 1 revolution
		while (totalAngle < 360) {
			
			// Point to be scan
			MapLocation newLoc = curLoc.add(dir,dist);
			
			// Check if space of radius 0.5 is clear 2.5 units away
			// First check if on map
			if(rc.onTheMap(newLoc, distRad)) {
				// Then check if occupied
				if(rc.isCircleOccupiedExceptByThisRobot(newLoc,distRad)) {
					rc.setIndicatorLine(curLoc.add(dir,(float)1), newLoc.add(dir,distRad), 255, 128, 0);
					
					// Increase factor
					crowdedFactor += angleInterval/360;
				}
				else {
					rc.setIndicatorLine(curLoc.add(dir,(float)1), newLoc.add(dir,distRad), 0, 255, 255); 
				}
			}
			else {
				// Increase factor
				crowdedFactor += angleInterval/(float)360.0;
			}
			dir = dir.rotateLeftDegrees(angleInterval);
			totalAngle += angleInterval; 
		}
		
		System.out.println("Crowded Factor: " + crowdedFactor);
		
		return crowdedFactor;
	}
	
	public static MapLocation findOptimalSpace(float angleInterval, float lengthInterval, float maxLength, float start) throws GameActionException {
		
//		System.out.println("Angle Interval: " + angleInterval + ", Length Interval: " + lengthInterval);
		
		// Get robot type
		RobotType thisBot = rc.getType();
		Team us = rc.getTeam();
		Team them = us.opponent();
		
		// Get current location
		MapLocation curLoc = rc.getLocation();
		
		// Declare potential and final location
		MapLocation potLoc = curLoc;
		MapLocation finalLoc = curLoc;
		
		// Find openness of current space:
		float minOpenness = Float.MAX_VALUE;
		float radius = 2;
		
		// Tree weighting; radius = 1 (body) + 2 (tree)
		minOpenness += rc.senseNearbyTrees(radius).length;
		// Unit weighting (for own Team)
		minOpenness += rc.senseNearbyRobots(radius, us).length;
		// Unit weighting (for enemy Team)
		minOpenness += 3*rc.senseNearbyRobots(radius, them).length;
		
//		System.out.println("Max Length: " + maxLength);
		float length = lengthInterval;
		// Declare angle/length modifiers
		// Check
		while(length <= maxLength) {
			float angle = start;
			float totalAngle = 0;
			
			while(totalAngle < 360) {
				// Get potential location
				potLoc = curLoc.add((float)Math.toRadians(angle), length);
				rc.setIndicatorDot(potLoc, 178, 102, 255);
				
				// Check surroundings
				float openness = Float.MAX_VALUE;
				
				// Check if on Map
				if(rc.onTheMap(potLoc)) {
					
					// Add penalty for cases on edge of map (equivalent to 3 trees)
					if(!(rc.onTheMap(potLoc, 2))) {
//						openness = (float)0.5;
					} else {
						openness = 0;
						// Tree weighting; radius = 1 (body) + 2 (tree)
						openness += rc.senseNearbyTrees(potLoc, radius, null).length;
						// Unit weighting (for own Team)
//						openness += rc.senseNearbyRobots(potLoc, radius, us).length;
						RobotInfo[] ourBots = rc.senseNearbyRobots(potLoc, radius, us);
						for (int i = 0; i < ourBots.length; i++) {
							if (ourBots[i].type == battlecode.common.RobotType.GARDENER) {
								openness += 1;
							}
							else {
								//openness += 0.1;
							}
						}
						// Unit weighting (for enemy Team)
						openness += 3*rc.senseNearbyRobots(potLoc, radius, them).length;
						openness += 5*rc.senseNearbyBullets(potLoc, radius).length;
					}
					
//					System.out.println("Angle: " + angle + ", length: " + length + ", Open: " + openness);
				}
				
				// Compare with existing, and replace if lower
				if (openness < minOpenness) {
					minOpenness = openness;
					finalLoc = potLoc;
				}
				angle += angleInterval;
				totalAngle += angleInterval;
			}
			length += lengthInterval;
		}
		
//		System.out.println("Final: " + finalLoc[0] + ", " + finalLoc[1]);
		
		return finalLoc;
	}
	
	public static float calculateTreeDensity() throws GameActionException {
		
		//initialize some variables 
		ArrayList<TreeInfo> neutralTrees = new ArrayList<TreeInfo>();
		
		//area of archon sight range
		float sightArea = (float) (Math.PI * Math.pow(10, 2));
		
		//we use this variable to keep a total of area taken up by all tree in sight range
		float treeArea = 0;
		
		//get information about nearby trees
		TreeInfo[] surroundingTrees = rc.senseNearbyTrees();
		
		//filter for neutral trees 
		for (TreeInfo tree : surroundingTrees) {
			if (tree.getTeam() == Team.NEUTRAL && rc.canSenseAllOfCircle(tree.getLocation(),tree.getRadius())) {
				treeArea = treeArea + (float) (Math.PI * Math.pow(tree.getRadius(), 2));
			}
		}
		
		// percentage of space in archon sight range that is taken up by trees
		float areaRatio = treeArea/sightArea;
		
		// produces a scaled ratio taking into account area taken up by trees and number of trees
		float scaledValue = (float) ((treeArea*100/sightArea)*0.4 + surroundingTrees.length*0.6);
				
		//returns this ratio as an integer by multiplying by 100
		return scaledValue;
		
	}
	
	public static void broadcastBulletTreeCount() throws GameActionException {
		nearbyBulletTreeCount = countNearbyShakeableTrees();
		
		// Count nearby bullet trees..
		if (chosenBulletTreeChannel == 0) {
			//if hasn't initialized it's own unique channel
			for (int i=0; i<3; i++) {
	    		//asserts that channel is currently empty
	    		if (rc.readBroadcast(BroadcastChannels.ARCHONS_BULLET_TREE_CHANNEL + 2*i) != myID) {
	    			// sets a unique channel for this archon
	    			rc.broadcast(BroadcastChannels.ARCHONS_BULLET_TREE_CHANNEL + 2*i, myID);
	    			chosenBulletTreeChannel = BroadcastChannels.ARCHONS_BULLET_TREE_CHANNEL + 2*i + 1;
	    			
	    		}
			}
		}
	
		rc.broadcast(chosenBulletTreeChannel, nearbyBulletTreeCount);
	
	}
	
	//counts number of nearby neutral trees than can be shaken (for scout production)
	public static int countNearbyShakeableTrees() throws GameActionException { 
		TreeInfo[] nearbyTrees = rc.senseNearbyTrees();
		int count = 0;
		
		for (TreeInfo tree: nearbyTrees) { 
			if (tree.getTeam() == Team.NEUTRAL && tree.getContainedBullets() > 0) {
				count++;
			}
		}
		
		return count; 
	}
}