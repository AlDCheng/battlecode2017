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
import naclbot.units.motion.Chirasou;
import naclbot.units.motion.search.TreeSearch;
import naclbot.units.motion.search.EnemyArchonSearch;

import java.util.ArrayList;
import java.util.Arrays;


/* ------------------   Overview ----------------------
 * 
 * THIS IS AN ARCHON BOT, NOT A SCOUT
 *
 * ~~ Coded by Alan Cheng (adcheng@mit.edu)
 * 
 * Don't read me plz 
 * 
 ---------------------------------------------------- */

public class KazumaBot extends GlobalVars {
	
	public static Team us = rc.getTeam();
	private static final float strideRadius = battlecode.common.RobotType.ARCHON.strideRadius;
	private static final float bodyRadius = battlecode.common.RobotType.ARCHON.bodyRadius;
	private static MapLocation lastBuilt = null;
	
	private static int remIsBestGirl = 0;
	private static int myID;
	private static int unitNumber;
	private static final int initMove = 15;
	private static MapLocation initialGoal, lastPosition;
	private static Direction lastDirection = new Direction(0);
	
	private static final float crowdThresh = (float)0.5;
	private static boolean startPoll = false;
	
	public static int archonNumber;
	
	public static int nearbyBulletTreeCount;
	public static int chosenBulletTreeChannel = -1;
	
	// Starting game phase
	public static void init() throws GameActionException {
		
		// SYSTEM CHECK Initialization start check
		System.out.println("Hai, hai Kazuma Desu");
		
		myID = rc.getID();
		
		// Let everyone know where the archon started off......
		rc.broadcast(BroadcastChannels.ARCHON_INITIAL_LOCATION_X, (int) (rc.getLocation().x * 100));
		rc.broadcast(BroadcastChannels.ARCHON_INITIAL_LOCATION_Y, (int) (rc.getLocation().y * 100));
		
		unitNumber = rc.readBroadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL);
        rc.broadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL, unitNumber + 1);
		
		archonNumber = rc.readBroadcast(BroadcastChannels.ARCHON_NUMBER_CHANNEL);
		rc.broadcast(BroadcastChannels.ARCHON_NUMBER_CHANNEL, archonNumber + 1);
		
		// Only have one gardener in play
		int numberofGardenersConstructed = rc.readBroadcast(BroadcastChannels.GARDENERS_CONSTRUCTED_CHANNELS);
		
		MapLocation oppositeEnemyArchon = EnemyArchonSearch.getCorrespondingArchon();
		lastDirection = new Direction(oppositeEnemyArchon, rc.getLocation());
		
		// Def not Aqua
        remIsBestGirl = rc.getRoundNum();
        
        int treeNum = 0;
        if (numberofGardenersConstructed <= 0) {
        	treeNum = 1;
        }
        
        broadcastBulletTreeCount();
        
        System.out.println(rc.readBroadcast(20) + " " +  rc.readBroadcast(21));
        System.out.println(rc.readBroadcast(22) + " " +  rc.readBroadcast(23));
        System.out.println(rc.readBroadcast(24) + " " + rc.readBroadcast(25));
        
        // Build gardeners
		constructGardeners(treeNum);
	}
	
	//--------------------------------------------------------------------------------------------------------------
	// Idle state / Main function
	public static void idle() throws GameActionException {
		int pollTimeOut = 0;
		boolean hold = false;
        while (true) {        	
            // catch 
            try {
            	// If can win, win
            	Win();
            	
            	//update round number 
            	remIsBestGirl = rc.getRoundNum();

            	// Update own location
            	MapLocation myLocation = rc.getLocation();
            	
            	Chirasou.attemptInteractWithTree(myLocation, bodyRadius);
            	
            	lastPosition = myLocation;
            	
            	RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
            	BroadcastChannels.broadcastNearestEnemyLocation(enemyRobots, myLocation, unitNumber, myLocation.add(Move.randomDirection(), (float)0.5), remIsBestGirl);
            	
            	// Store the location that archon wants to go to.... it doesnt want to move by default
            	MapLocation desiredMove = myLocation;	
            	
            	// Get the total number of units constructed thus far.....
//            	numberofGardenersConstructed = rc.readBroadcast(BroadcastChannels.GARDENERS_CONSTRUCTED_CHANNELS);
            	int gardenerCount = rc.readBroadcast(BroadcastChannels.GARDENERS_ALIVE_CHANNEL);
            	int soldierCount = rc.readBroadcast(BroadcastChannels.SOLDIERS_ALIVE_CHANNEL);
                int lumberjackCount = rc.readBroadcast(BroadcastChannels.LUMBERJACKS_ALIVE_CHANNEL);
                
        		// Check surroundings
        		boolean crowded = (Aqua.checkBuildRadius((float)30, (float)3, (float)0.5) >= crowdThresh);
        		System.out.println("Gardener Count: " + gardenerCount);
        		
        		// If not crowded...
        		// See if we can start polling for a new gardener.
    			int pollState = rc.readBroadcast(BroadcastChannels.GARDENER_POLL);
    			
    			if(!(pollState == 1)) {
    				pollTimeOut = 0;
    			}
    			
    			if ((gardenerCount <= 0) && (remIsBestGirl > 20)) {
                	constructGardeners(1);
                }
    			else if (((!crowded) || (pollState == 1)) && (remIsBestGirl >= 75)) {
        			System.out.println("Polling State: " + pollState);
        			// If available:
        			if (pollState == 2) {
        				constructGardeners(1);
        			}
        			else if ((pollState == 0) && (rc.getTeamBullets() >= 100)) {
        				rc.broadcast(BroadcastChannels.GARDENER_BUILD_FILL, 0);
        				rc.broadcast(BroadcastChannels.GARDENER_POLL, 1);
        				startPoll = true;
        			}
        			else if ((pollState == 1) && (startPoll)) {
        				startPoll = false;
        				int fillState = rc.readBroadcast(BroadcastChannels.GARDENER_BUILD_FILL);
        				
        				System.out.println("Build Fill: " + fillState);
        				
        				if (fillState == 0) {
        					rc.broadcast(BroadcastChannels.GARDENERS_ALIVE_CHANNEL, 0);
        				}
        				else if (fillState >= 1) {
        					constructGardeners(1);
        					rc.broadcast(BroadcastChannels.GARDENER_POLL, 2);
        				}
        				else {
        					rc.broadcast(BroadcastChannels.GARDENER_POLL, 0);
        				}
        			}
        			else {
        				System.out.println("Poll Timer: " + pollTimeOut);
        				if (pollTimeOut > 3) {
        					rc.broadcast(BroadcastChannels.GARDENER_POLL, 0);
        				}
        				pollTimeOut++;
        			}
        		}
    			else if (pollState == 1){
    				System.out.println("Poll Timer: " + pollTimeOut);
    				if (pollTimeOut > 3) {
    					rc.broadcast(BroadcastChannels.GARDENER_POLL, 0);
    				}
    				pollTimeOut++;
    			}
        		// Building gardeners if none exists (i.e. killed)
        		
    			int treeCount = rc.getTreeCount();
    			if (rc.getTeamBullets() >= 100) {
    				if (rc.isBuildReady() && (treeCount > 10) && (3*gardenerCount < treeCount)) {
            			constructGardeners(1);
            		}
    			}
            	
            	// SYSTEM CHECK - Inform that the archon is attempting to construct a gardener....
            	System.out.println("Currently not doing anything..............." );
            	     	
            	Direction testDirection = new Direction(lastDirection.radians + (float) Math.PI);
            	
            	
            	// Movement----------------------------------------------------------------------------------
            	if (remIsBestGirl > initMove) {
            		Aqua.disperseFromGardeners(myLocation, strideRadius, bodyRadius, testDirection.opposite(), unitNumber);
            	}
            	// Move to optimal spot at initialization
            	else {
            		System.out.println("Moving in general direction");
            		if (initialGoal != null) {
            			Direction dir = new Direction (rc.getLocation(), initialGoal);
            			
            			desiredMove = myLocation.add(dir, strideRadius);
            			rc.setIndicatorLine(rc.getLocation(), desiredMove, 255, 0, 0);
            			
            			MapLocation correctedMove = Yuurei.correctAllMove(strideRadius, bodyRadius, false, us, myLocation, desiredMove);
    					Aqua.believeHasDied = Aqua.manageBeingAttacked(correctedMove, unitNumber);
            			rc.move(correctedMove);
            		}
            	}
            	//-------------------------------------------------------------------------------------------
      
            	// Update the last position of the robot to get the heading of the archon in the previous turn....
            	if (lastPosition != null) {
            		if (lastPosition.distanceTo(rc.getLocation()) > 0.1) {
                		lastPosition =  rc.getLocation();
        	            lastDirection = new Direction(myLocation, lastPosition);
        	            
        	            rc.setIndicatorLine(myLocation,myLocation.add(testDirection,3),0,255,0);
                	}
            	}
            	
	            System.out.println("current round number: " + remIsBestGirl);
	            
	            if (Clock.getBytecodesLeft() > 4000) {
	            	float density = Aqua.checkEmptySpace(myLocation);
	            	System.out.println("New Empty Density: " + density);
	            	
	            	int broadcastStart = BroadcastChannels.ARCHONS_TREE_DENSITY_CHANNEL + 2*archonNumber;
                    rc.broadcastFloat(broadcastStart+1, density);
	            	
	            }
	            
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                
                e.printStackTrace();
            }
        }
    }
	
	//--------------------------------------------------------------------------------------------------------------
	// Function to build gardeners
	public static void constructGardeners(int maxGardeners) throws GameActionException {	
		
		// If can win, win
    	Win();
		
		// Loop to terminate the starting phase of the robot
		boolean checkStatus = true;		
		
		// Variable to store the number of gardeners hired in this phase....
		int hiredGardeners = 0;
		
		
		
        // Starting phase loop
        while ((hiredGardeners < maxGardeners)) {

            // Try/catch blocks stop unhandled exceptions, - print stacktrace upon exception error....
            try {
            	// Why not just win?
            	Win();
            	
            	//--INITIALIZATION--
            	// Update own location
            	MapLocation myLocation = rc.getLocation();
            	
            	lastPosition = myLocation;
            	
            	// Store the location that archon wants to go to....
            	MapLocation desiredMove = myLocation;
            	
            	// Update the current round number.....
            	remIsBestGirl = rc.getRoundNum();
            	
            	// Scan surrounding first:
            	boolean crowded = (Aqua.checkBuildRadius((float)30, (float)2.5, (float)0.5) >= crowdThresh);
            	
            	// SYSTEM CHECK - Print out the gardener limit and the current number of gardeners constructed
//            	System.out.println("Gardener Limit: " + getGardenerLimit(remIsBestGirl) + ", current constructed number: " + numberofGardenersConstructed);
            	
            	// On turn 1
            	if (remIsBestGirl == 1){
            		lastDirection = Aqua.getInitialWalls(myLocation, lastDirection);
            		initialGoal = rc.getLocation().add(lastDirection, 10);
            		
            		// Broadcast density for gardeners
//                    float treeDensity = Aqua.calculateTreeDensity();
//                    System.out.println("Tree Density: " + treeDensity);
            		System.out.println("Empty Density: " + Aqua.emptyDensity);
                    
                    int broadcastStart = BroadcastChannels.ARCHONS_TREE_DENSITY_CHANNEL + 2*archonNumber;
                    int myID = rc.getID();
                    System.out.println("My ID: " + myID);
                    rc.broadcast(broadcastStart, myID);
                    rc.broadcastFloat(broadcastStart+1, Aqua.emptyDensity);
            	}
            	// Get the total number of gardeners constructed thus far.....
            	int numberofGardenersConstructed = rc.readBroadcast(BroadcastChannels.GARDENERS_CONSTRUCTED_CHANNELS);
            	
            	rc.setIndicatorDot(myLocation.add(lastDirection,10), 155, 135, 244);
            	
            	// SYSTEM CHECK - Inform that the archon is attempting to construct a gardener....
            	System.out.println("Attempting to hire a gardener. Can hire a maximum of: " + maxGardeners + ", currently hired: " + hiredGardeners);
            	
            	// Boolean to determine whether or not the archon attempted to hire a gardener this turn or not......
            	boolean hiredGardener = false;  
            	
            	Direction testDirection = new Direction(0);
            	Direction gardenerDirection = new Direction(0);
            	
            	// Build if not crowded
            	if (!crowded || (remIsBestGirl <= initMove)) {
//            		testDirection = new Direction(lastDirection.radians + (float) Math.PI);
//            		if (lastBuilt != null) {
//            			testDirection = new Direction(rc.getLocation(), lastBuilt); 
//            		}
//            		else {
//            			testDirection = new Direction(lastDirection.radians + (float) Math.PI);
//            		}
                	
            		if (lastBuilt == null) {
            			testDirection = new Direction(lastDirection.radians + (float) Math.PI);
            		}
            		else {
            			if(lastBuilt.distanceTo(myLocation) > 0.5) {
            				testDirection = new Direction(myLocation, lastBuilt);
            			}
            		}
            		
            		rc.setIndicatorLine(myLocation,myLocation.add(testDirection,3),255,0,172);
            		
            		gardenerDirection = Aqua.scanBuildRadius(2, testDirection.getAngleDegrees(), (float)3, (float)1, (float)360);
            		if (!rc.canHireGardener(gardenerDirection)) {
            			gardenerDirection = Aqua.tryHireGardener(testDirection);
            		}
            	}
  
            	// If the archon can hire a gardener in a certain direction...
            	if (gardenerDirection != null){
            		// Assert that the archon can actually hire it (i.e. not limited by previous hiring
            		if (rc.canHireGardener(gardenerDirection)){
	            		rc.hireGardener(gardenerDirection);
	            		
	            		// Increment counters.....
	            		hiredGardener = true;
	            		hiredGardeners += 1;
	            		
	            		// Update broadcasted counter
	            		rc.broadcast(BroadcastChannels.GARDENERS_CONSTRUCTED_CHANNELS, numberofGardenersConstructed+1);
	            		rc.broadcast(BroadcastChannels.GARDENER_POLL, 0);
	            		
	            		// Reset polling 
	            		
	            		//update last built location
	            		lastBuilt = rc.getLocation();              	
	            	}
            	}
            	else if(rc.readBroadcast(BroadcastChannels.GARDENER_POLL) == 2) {
            		return;
            	}
            	
            	// Movement----------------------------------------------------------------------------------
            	if (remIsBestGirl > initMove) {
            		Aqua.disperseFromGardeners(myLocation, strideRadius, bodyRadius, testDirection.opposite(), unitNumber);
            	}
            	// Move to optimal spot at initialization
            	else {
            		System.out.println("Moving in general direction");
            		if (initialGoal != null) {
            			Direction dir = new Direction (rc.getLocation(), initialGoal);
            			
            			desiredMove = myLocation.add(dir, strideRadius);
            			MapLocation correctedMove = Yuurei.correctAllMove(strideRadius, bodyRadius, false, us, myLocation, desiredMove);
    					Aqua.believeHasDied = Aqua.manageBeingAttacked(correctedMove, unitNumber);
            			rc.move(correctedMove);
            		}
            	}
            	//-------------------------------------------------------------------------------------------
            	
            	// Update the last position of the robot to get the heading of the archon in the previous turn....
            	if (lastPosition != null) {
            		if (lastPosition.distanceTo(rc.getLocation()) > 0.1) {
                		lastPosition =  rc.getLocation();
        	            lastDirection = new Direction(myLocation, lastPosition);
        	            
        	            rc.setIndicatorLine(myLocation,myLocation.add(testDirection,3),0,255,0);
                	}
            	}
	            
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Archon Exception in Gardener Building Phase");
                e.printStackTrace();
            }
        }
        
        // Move to the mainPhase of operations
        idle();
    }
	
	public static void broadcastBulletTreeCount() throws GameActionException {
		nearbyBulletTreeCount = countNearbyShakeableTrees();
		
		// Count nearby bullet trees..
		if (chosenBulletTreeChannel < 0) {
			//if hasn't initialized it's own unique channel
			for (int i=0; i<3; i++) {
	    		//asserts that channel is currently empty
	    		if (rc.readBroadcast(BroadcastChannels.ARCHONS_BULLET_TREE_CHANNEL + 2*i) == 0) {
	    			// sets a unique channel for this archon
	    			rc.broadcast(BroadcastChannels.ARCHONS_BULLET_TREE_CHANNEL + 2*i, myID);
	    			chosenBulletTreeChannel = BroadcastChannels.ARCHONS_BULLET_TREE_CHANNEL + 2*i + 1;
	    			break;
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