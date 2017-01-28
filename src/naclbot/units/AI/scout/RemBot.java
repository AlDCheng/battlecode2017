package naclbot.units.AI.scout;

import java.util.Arrays;
import battlecode.common.*;
import naclbot.units.motion.Yuurei;
import naclbot.units.motion.shoot.Korosenai;
import naclbot.units.interact.iFeed;
import naclbot.units.motion.Chirasou;
import naclbot.units.motion.Todoruno;
import naclbot.variables.GlobalVars;
import naclbot.variables.BroadcastChannels;	

/* ------------------   Overview ----------------------
 * 
 * AI Controlling the functions of the explorer scout....
 *
 * ~~ Coded by Illiyia (akimn@#mit.edu)
 * 
 * Debug statements all begin with SYSTEM CHECK 
 * 
 ---------------------------------------------------- */

// Remember.... the first scout gets its own special java file precisely because it is called RemBot!
// Anyone who sincerely doubts that Rem is best girl can look somewhere else - this code is only for those who are true believers.........

public class RemBot extends ScoutBot {	
	
	// ----------------------------------------------------------------------------------//
	// ------------------------- VARIABLES SPECIFIC TO REMBOT ---------------------------//
	// ----------------------------------------------------------------------------------//	
	
	// Behavior restricting constants..
	private static float explorerDefendDistance = 60;	// Constant to update the distance for which the scout will attempt to defend its archon......
	private static final float keepAwayDistance = (float) 9; // Distance at which the scouts will attempt to stay away from the unit they are investigating
	
	// Store the archon location to go too.....
	private static MapLocation archonLocation;
	
	// Variable to show whether or not the scout has seen an enemy yet....
	private static boolean toInvestigate = false;
	
	// Variables regarding the currently investigated unit....
	private static int investigateID = -1;
	private static RobotInfo investigateData = null;
	
	
	
	// ----------------------------------------------------------------------------------//
	// -------------------------------- RUNTIME FUNCTIONS -------------------------------//
	// ----------------------------------------------------------------------------------//	
	
	// Initialization follows from regular scoutbot behavior
		// When in RemBot mode, ScoutBots do not shoot, and only observe................
		// RemBots attempt to go to the nearest enemy archon location and calculate how many enemies are nearby.....
	
	// Main function body.......
	
	public static void main(){
		
		archonLocation = rc.getInitialArchonLocations(enemies)[0]; // Utilize the first archonlocation by default.......
		
		// Code to be performed every turn   
		while (true){
			
			// Main actions of the scout.....
			try{
				
				// SYSTEM CHECK  Make sure the robot starts its turn
                System.out.println("Remember guys Rem is best girl!!!");                 
        		
				// ------------------------ INTERNAL VARIABLES UPDATE ------------------------ //     

            	// Get nearby enemies and allies and bullets for use in other functions            	
            	RobotInfo[] enemyRobots = NearbyUnits(enemies, sensorRadius);
            	RobotInfo[] alliedRobots = NearbyUnits(allies, sensorRadius);
            	BulletInfo[] nearbyBullets = rc.senseNearbyBullets();
            	
            	// If there are enemy nearby, get the nearest enemy
            	if(enemyRobots.length > 0){
            		
            		RobotInfo nearestEnemy = Chirasou.getNearestAlly(enemyRobots, myLocation);
            		
            		investigateID = nearestEnemy.ID;
            		investigateData = nearestEnemy;
            		toInvestigate = true;            		
            	}
            	
            	// Update game variables
            	teamBullets = rc.getTeamBullets();
            	
            	// Make sure the robot shows appreciation for the one and only best girl in the world.
            	// If you are reading this and you think Emilia is best girl I have no words for you
		        onlyRemIsBestGirl = rc.getRoundNum();
		    	
		    	// Update positional and directional variables
		        myLocation = rc.getLocation();

		    	// See if there is a gardener and update the location of the nearest civilian if there is one....
		    	RobotInfo nearestGardener = Todoruno.getNearestGardener(alliedRobots, myLocation);
		    	// Set the nearest civilian location accordingly...
		       	if (nearestGardener != null){   
		       		
		       		nearestCivilianLocation = nearestGardener.location;	       		 		
		       	}
		       	
		       	// If the robot hadn't moved the previous turn... this value may be null
		       	if (lastDirection == null){
		       		
		       		// Set the direction to go to as away from the last known nearest civilian
		       		lastDirection = nearestCivilianLocation.directionTo(myLocation);
		       	}	
            	
		       	// SYSTEM CHECK - Show where the scout believes its nearest civilian is using a WHITE LINE
		       	rc.setIndicatorLine(myLocation, nearestCivilianLocation, 255, 255, 255);
		       	
		      	// SYSTEM CHECK - Show where the scout is attempting to search....... LIGHT GREEN LINE
		     	rc.setIndicatorLine(myLocation, archonLocation, 152, 251, 152);
		       	
		       	// ------------------------ BROADCAST UPDATES ------------------------ //
		    	
		       	// Update the nearest enemy and archon locations - If it sees any.......
            	BroadcastChannels.broadcastNearestEnemyLocation(enemyRobots, myLocation, unitNumber, nearestCivilianLocation, onlyRemIsBestGirl);            	
            	BroadcastChannels.broadcastEnemyArchonLocations(enemyRobots);
            	
            	// Update the distress info and retreat to the gardener if necessary..... if necessary            	
            	BroadcastChannels.BroadcastInfo distressInfo = BroadcastChannels.readDistress(myLocation, explorerDefendDistance);
            	
            	// TODO (UPDATE INITIAL UNIT COUNTS)........
            	
            	// If the robot thought it died previously but didn't.... update information...
            	if(believeHasDied){
            		fixAccidentalDeathNotification();
            	}

            	// ------------------------ MOVEMENT FUNCTIONS------------------------ //      
				
				
				
				
				
				System.out.println("I am the first scout O_o");
				
				System.out.println(unitNumber);
				System.out.println(myID);
				
			     Clock.yield();
			}
			catch (Exception exception){
				System.out.println("Explorer Scout Exception");
                exception.printStackTrace();
			}			
		}	
	}
	
	// ----------------------------------------------------------------------------------//
	// ------------------------------- MOVEMENT FUNCTIONS -------------------------------//
	// ----------------------------------------------------------------------------------//	
	
// General move function.....
    
    private static MapLocation move(RobotInfo[] enemyRobots, MapLocation targetLocation) throws GameActionException{
    	
    	// If the robot has yet to see a robot and it is far away from the initial archon location, move towards it....
    	if (myLocation.distanceTo(targetLocation) >= 5 || !toInvestigate){   	
    		
    		// Attempt to move towards the location
    		return moveTowardsLocation(targetLocation);
    		
    	
    	
    		
    		
    		
    		
    	}
    	
    	return null;
    }
    
    // Function to control how scouts move to a location.... (assumes the location is on the map)
    	// First check to see if they can move completely towards the target location
    	// Then checks to see if there is a robot in the way, and calls the directional move function....., which prioritizes the direction close to the desired
    
    private static MapLocation moveTowardsLocation(MapLocation targetLocation) throws GameActionException{    	
    	
    	// Get the direction to the desired location and the furthest move that the scout can make in that direction
    	Direction directionTo = (myLocation.directionTo(targetLocation));
		MapLocation furthestMove = myLocation.add(directionTo, strideRadius);
		
		if(rc.canMove(furthestMove)){    			
			
			// SYSTEM CHECK - Show where the move is with a TURQUOISE...
			rc.setIndicatorLine(myLocation, furthestMove, 64, 224, 208);
			
			// Just return the move that takes the robot closest to the desired location......
			return furthestMove;
		}
		else{
			// If there is a robot in the way, attempt to path around it......
			if(rc.senseRobotAtLocation(furthestMove) != null){
				
				// SYSTEM CHECK - Print out that there is an enemy in the way
				System.out.println("There appears to be a robot in my way....");
				
				// Attempt to move randomly in the desired direction......
				MapLocation moveAttempt = Yuurei.tryMoveInDirection(directionTo, 30, 5, strideRadius, myLocation);
				
				// SYSTEM CHECK - Show where the move is with a CYAN LINE...
				rc.setIndicatorLine(myLocation, moveAttempt, 0, 139, 139);
				
				return moveAttempt;
			}
			
			// This shouldn't happen, but if the scout cannot move toward the target, and it is not blocked by a robot, it is probably out of bounds
			else{
				
				// SYSTEM CHECK - Print out that the scout thinks that it is out of bounds....
				System.out.println("Cannot reach desired location and no robot is in the way, will attempt to correct movement....");
				
				// Return a small increment forward in the direction (if the robot cannot go, it will not after corrections....)
				return myLocation.add(directionTo, strideRadius / 5);			
			}			
		}
    }
    
    private static MapLocation circleTowardsTargetLocation(MapLocation targetLocation, RobotInfo enemyToAvoid, float gapDistance) throws GameActionException{
    	
    	// Get the direction to the desired location and the furthest move that the scout can make in that direction
    	
    	// SYSTEM CHECK - Print out that the robot has found something......
    	
    	Direction directionToTarget = myLocation.directionTo(targetLocation);
    	Direction directionToEnemy = myLocation.directionTo(enemyToAvoid.location);
    			
		MapLocation furthestMove = myLocation.add(directionToEnemy, strideRadius);
    	
		// If the furthest move towards the enemy would keep it out of sight range, just continue calling move towards target
    	if(furthestMove.distanceTo(enemyToAvoid.location) > gapDistance){
    		
    		return moveTowardsLocation(targetLocation);
    	}
		
		
    	return null;
    	
    }
}
