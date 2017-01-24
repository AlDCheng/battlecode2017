// AI for lumber jack

package naclbot.units.AI.lumberjack;
import battlecode.common.*;
import naclbot.units.motion.Move;
import naclbot.units.motion.search.TreeSearch;
import naclbot.units.interact.iFeed;
import naclbot.units.motion.Chirasou;
import naclbot.units.motion.Yuurei;
import naclbot.variables.BroadcastChannels;
import naclbot.variables.DataVars;
import naclbot.variables.GlobalVars;
import naclbot.variables.DataVars.basicTreeInfo;
import naclbot.variables.DataVars.binarySearchTree;

import java.util.ArrayList;

public class LumberjackBot extends GlobalVars {	
	
	// ------------- GENERAL (IMPORTANT TO SELF) VARS -------------//
	
	// Variable for round number
	private static int roundNumber;
	
	// Variables for self and team recognition
	public static int myID;
	public static int lumberjackNumber;
	private static Team enemy;
	private static Team allies;		
	private static final float strideRadius = battlecode.common.RobotType.LUMBERJACK.strideRadius;
	private static final float strikeRadius = GameConstants.LUMBERJACK_STRIKE_RADIUS;
	private static final float bodyRadius = battlecode.common.RobotType.LUMBERJACK.bodyRadius;
	
	// The intial round in which the soldier was constructed
	public static int initRound;
	
	// Current round
	public static int currentRound;
	
	// The total number of soldiers in active service
	//private static int currentNumberofLumberjacks;
	
	// Parameters to store locations of self
	public static MapLocation myLocation;	
	
	// Boolean to store whether or not the soldier current has orders to go somewhere....
	private static boolean isCommanded;
	
	// ------------- MOVEMENT VARIABLES -------------//
	
	// Direction at which the soldier traveled last
	private static Direction lastDirection;
	private static MapLocation lastLocation;
	
	// Direction for use each round
	private static Direction myDirection;
	
	// Placeholder for desired location to go to
    public static MapLocation desiredMove;
    
    // Keep track if it has not moved
    public static int hasNotMoved = 0;
    
    // Location of wanted enemy
    public static Direction enemyDir = null;
    
	// ------------- TREE VARIABLES ------------------//
	
	// If tree has found a tree with a prize 
    public static boolean foundTree = false;
    
    // Location of tree with a prize 
    public static MapLocation prizeTreeLoc = null;
 
    // Location of regular tree
    public static Direction treeDir = null;
    
    // ArrayList of all nearby neutral trees, regardless of prize/no prize/bullets/no bullets
    public static ArrayList<TreeInfo> nearbyNeutralTrees;
    
	
    public static void init() throws GameActionException{
    	
    	// SYSTEM CHECK - See if the lumberjack has initialized...    	
    	System.out.println("I'm an lumberjack!");
        
		// Initialize variables important to self and team recognition
		enemy = rc.getTeam().opponent();
        allies = rc.getTeam();
        
    	lastDirection = Move.randomDirection();
        
        currentRound = rc.getRoundNum();
        initRound = currentRound;
        
        myID = rc.getID();
        myLocation = rc.getLocation();
        lastLocation = rc.getLocation();
        
        // Initialize lumberjack so that it does not have any commands initially;
        isCommanded = false;
        
        /*
        // Get own soldierNumber - important for broadcasting 
        soldierNumber = rc.readBroadcast(BroadcastChannels.SOLDIER_NUMBER_CHANNEL);
        currentNumberofSoldiers = soldierNumber + 1;
        
        // Update soldier number for other soldiers to see.....
        rc.broadcast(BroadcastChannels.SOLDIER_NUMBER_CHANNEL, currentNumberofSoldiers);
        */

        main();
    }
    
    public static void main() throws GameActionException {
 	
        // Actions to be completed every turn by the lumberjack...
        while (true) {

            try {
            	// ------------------------- RESET/UPDATE VARIABLES ----------------//          
    			
            	// Get nearby enemies and allies and bullets for use in other functions            	
            	RobotInfo[] enemyRobots = NearbyUnits(enemy);
            	RobotInfo[] alliedRobots = NearbyUnits(allies);
            	BulletInfo[] nearbyBullets = rc.senseNearbyBullets();

            	// Update location of self
            	myLocation = rc.getLocation();      
            	
            	// Check if robot has moved
                if (myLocation == lastLocation) {
                	hasNotMoved += 1;
                } 
            	
            	// Initialize the direction the robot would like to go to at any given round as the direction the robot moved previously....     	
            	myDirection = lastDirection;
            	
            	// Initialize the location the robot would like to go to as the location it is currently at..
            	desiredMove = myLocation;
            	
            	nearbyNeutralTrees = TreeSearch.getNearbyNeutTrees();
	        
            	
            	// --------------- ENEMY ROBOTS ---------------- //
				if (enemyRobots.length > 0) {
					enemyDir = manageEnemyUnits(enemyRobots);
				}
				
				
            	// ------------------------- TREES --------------------------- //
            	// Shakes trees if there are bullets in it
				// Chops trees if robot in it or there are no bullets in it
				if (nearbyNeutralTrees.size() > 0) {
				    treeDir = manageNeutralTrees();
				}

				
				// ----------------- MOVEMENT ------------------ //
            	
				// Call the move function to move the robots
            	move(treeDir,enemyDir);
            	
				
				// ------------------  Round End Updates --------------------//
            	
            	// At the end of the turn update the round number
                roundNumber += 1;

                // Make it so that the last direction traveled is the difference between the robot's current and final positions for the round...
                lastLocation =  rc.getLocation();
                lastDirection = new Direction(myLocation, lastLocation);
                
                // SYSTEM CHECK  Make sure the robot finishes its turn
                System.out.println("Turn completed.....");
				
				// Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
				Clock.yield();
		
            } catch (Exception e) {
		
            	System.out.println("Lumberjack Exception");
            	e.printStackTrace();
            }
	
        }
    }
    
	/******************************************************************
	******************* Functions for Movement  ***********************
	*******************************************************************/  
	
    private static void move(Direction treeDir, Direction enemyDir) throws GameActionException {
    	// Possible new location
    	MapLocation newLoc = null;
    	
    	// If enemyDir is not null then more than 2 enemies found nearby. 
    	// None of these units are within strike range so move towards nearest one. 
    	if (enemyDir != null) {
    		newLoc = Yuurei.tryMoveInDirection(enemyDir, strideRadius, myLocation);
    	// If treeDir is not null then move to nearest tree.
    	} else if (treeDir != null) {
    		newLoc = Yuurei.tryMoveInDirection(treeDir, strideRadius, myLocation);
    	}
    	
    	// MOVE
    	if (newLoc != null) {
    		rc.move(newLoc);
    		hasNotMoved = 0;
    		
    		// Output that they moved
    		System.out.println("I MOVED TO AN OBJECTIVE!");
    		
    	} else if (!foundTree) {
    		if (hasNotMoved > 10) {
    			Direction desiredDir = Move.randomDirection();
    			if (rc.canMove(desiredDir)) {
    				System.out.println("Has not moved in more than 10 turns so random dir");
    				rc.move(desiredDir);
    				hasNotMoved = 0;
    			}
    		} else {
	    		// Posit the desired move location as a forward movement along the last direction
				desiredMove = myLocation.add(myDirection, (float) (Math.random() * (strideRadius / 2)  + (strideRadius / 2)));
				if (rc.canMove(desiredMove)) {
					System.out.println("Moving like soldier.");
					rc.move(desiredMove);
					hasNotMoved = 0;
				}
    		}
    	}
    	
    }
    
    private static Direction manageEnemyUnits(RobotInfo[] enemyRobots) throws GameActionException {
    	
    	// Assumes that enemyRobots is not empty
    	
        RobotInfo nearestEnemy = enemyRobots[0];
        
        float strikeDistance = bodyRadius + strikeRadius;
        
        if (myLocation.distanceTo(nearestEnemy.getLocation()) <= strikeDistance) {
            // Use strike() to hit all nearby robots!
		    System.out.println("STRIKING ENEMIES GRRR");
            rc.strike();
            return null;
        }
       
		// If there are more than two robots in sight, move to nearest one
		if (enemyRobots.length > 2) {	
			
		    MapLocation enemyLocation = enemyRobots[0].getLocation();
		    Direction toEnemy = myLocation.directionTo(enemyLocation);
		    
		    return toEnemy;
		}		
		
		return null;
    }
    
    private static Direction manageNeutralTrees() throws GameActionException {
	
        // Assumes that nearbyNeutralTrees is not empty
    	TreeInfo nearestTree = nearbyNeutralTrees.get(0);
    	if (nearestTree.getContainedRobot() != null) {
		    
		    // There is a prize! Chop the tree to get it
		    if (rc.canChop(nearestTree.getLocation())) {
				System.out.println("DIGGING FOR PRIZE");
				rc.chop(nearestTree.getLocation());
		        foundTree = true;
				if (nearestTree.getHealth() <= 5) {
				    foundTree = false;
				    System.out.println("NOT DIGGING FOR PRIZE ANYMORE");
				}
				return null;
		
		    } 
		
    	} else if (nearestTree.getContainedBullets() > 0) {
		
    		// If there are bullets and it can shake then SHAKE IT!
    		if (rc.canShake(nearestTree.getLocation())) {
    			foundTree = true;
				System.out.println("SHAKE IT SHAKE IT");
				rc.shake(nearestTree.getLocation());
				return null;
    		}
    		
    	} else {
		
    		// If it can chop the nearest tree then do it
		    if (rc.canChop(nearestTree.getLocation())) {
		    	foundTree = true;
				rc.chop(nearestTree.getLocation());
				System.out.println("CHOP USELESS TREE");
				return null;
		    } 
    	}
    	
    	foundTree = false;
    	Direction theDir = myLocation.directionTo(nearestTree.getLocation());
    	return theDir;
    }

    /******************************************************************
	******************* Miscellaneous Functions************************
	*******************************************************************/   	
	
	// Function to obtain the robot info units in the specified team
	
	private static RobotInfo[] NearbyUnits(Team team){
	
		return rc.senseNearbyRobots(myLocation, -1, team);
	}

    
}
