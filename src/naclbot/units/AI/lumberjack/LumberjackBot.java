// AI for lumber jack

package naclbot.units.AI.lumberjack;
import battlecode.common.*;
import naclbot.units.motion.Move;
import naclbot.units.motion.search.TreeSearch;
import naclbot.units.interact.iFeed;
import naclbot.units.motion.Chirasou;
import naclbot.units.motion.Yuurei;
import naclbot.variables.DataVars;
import naclbot.variables.GlobalVars;
import naclbot.variables.DataVars.basicTreeInfo;
import naclbot.variables.DataVars.binarySearchTree;

import java.util.ArrayList;

public class LumberjackBot extends GlobalVars {	

    public static int ID;
    
    public static MapLocation myLocation;
    
    public static int initRound;
    public static int homeArchon;
    
    public static Team enemy;
    public static Team allies;
    
    public static int currentRound;
    public static int currentGroup = -1;
    
    public static int command;
    
    public static boolean isLeader;	
    
    public static RobotInfo[] currentEnemies; 
    public static RobotInfo[] currentAllies; 

	 
    public static final basicTreeInfo dummyTree = new basicTreeInfo(-1, -1, -1, -1);
    public static final basicTreeInfo[] dummyTreeInfo = {dummyTree};	
    
    public static binarySearchTree treeList = new binarySearchTree(dummyTreeInfo);
    public static boolean foundPrize = false;

    public static ArrayList<TreeInfo> nearbyNeutralTrees;
	
    public static Direction lastDirection;
    
	
    public static void init() throws GameActionException{
	System.out.println("I'm an lumberjack!");
        enemy = rc.getTeam().opponent();
        allies = rc.getTeam();
        
    	lastDirection = Move.randomDirection();
        
        currentRound = rc.getRoundNum();
        initRound = currentRound;
        
        ID = rc.getID();
        
        int archonCount = rc.readBroadcast(ARCHON_CHANNEL);
        homeArchon = (int) (Math.random() *archonCount);
		
        main();
    }
	
	
    public static void attack() throws GameActionException{
	
		// Checks if robot is leader....	
		
		if (isLeader){
		    System.out.println("I'm leader of this group WAT");
		    rc.broadcast(GROUP_LEADER_START * currentGroup * GROUP_LEADER_OFFSET + 1, ID);
		    rc.broadcast(GROUP_LEADER_START * currentGroup * GROUP_LEADER_OFFSET + 2, (int)myLocation.x);
		    rc.broadcast(GROUP_LEADER_START * currentGroup * GROUP_LEADER_OFFSET + 3, (int)myLocation.y);
		}
		
		while (true){
		    try{
			
		    	currentEnemies = rc.senseNearbyRobots(-1, enemy);
		    	currentAllies = rc.senseNearbyRobots(-1, allies);
			
		    	binarySearchTree.combatUpdateTrees(treeList, 0);
		    	updateMapTrees(DataVars.treeMapFormat);
			
				int targetX = rc.readBroadcast(GROUP_START + currentGroup * GROUP_OFFSET + 3);
				int targetY = rc.readBroadcast(GROUP_START + currentGroup * GROUP_OFFSET + 4);
				int targetID = rc.readBroadcast(GROUP_START + currentGroup * GROUP_OFFSET + 2);
				
				MapLocation targetLocation = new MapLocation(targetX, targetY);				
				
				System.out.println("Currently in attack for group: " + currentGroup);
			
				if(!rc.hasMoved()){
				    if (currentAllies.length > 0){
				    	//RobotInfo closestAlly = getNearestAlly();
				    	MapLocation newLoc = Chirasou.Disperse(rc.getTeam(), myLocation, battlecode.common.RobotType.LUMBERJACK.strideRadius);
				    	if (newLoc != null) {
				    		rc.move(newLoc);
				    		iFeed.willFeed(newLoc);
				    		System.out.println("MOVING AWAY FROM CLOSEST ALLY");
					    }
				    }
					// If it didn't move before
				    if (!rc.hasMoved()) {
						Direction testDir = Move.randomDirection();
						MapLocation newLoc = tryMoveLumberjack(testDir);
						if (newLoc != null) {
							iFeed.willFeed(newLoc);
							System.out.println("MOVING IN A RANDOM DIR");
						} else {
							System.out.println("TRIED MOVING IN A RANDOM DIR");
						}
					}
				}
			
				Clock.yield();
		    } catch (Exception e) {
		    	System.out.println("Lumberjack Exception");
		    	e.printStackTrace();
		    }
		}
    }
    
    public static void main() throws GameActionException {
 	
        // The code you want your robot to perform every round should be in this loop
        while (true) {
	    
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
		
            	currentEnemies = rc.senseNearbyRobots(-1, enemy);
            	currentAllies = rc.senseNearbyRobots(-1, allies);

            	nearbyNeutralTrees = TreeSearch.getNearbyNeutTrees(); // Different from getNearbyNeutralTrees()
	        
            	checkGroupAssignments();
            	
            	// check if thee robot has entered a group or not
            	if (currentGroup >= 0){
		    
            		// Check the value of the command bit of the group
		    
            		// command 1 is an attack command
            		if (command == 1){
            			attack();
            		}
            	}
            	
            	myLocation = rc.getLocation(); // Current location

            	// Shakes trees if there are bullets in it
				// Chops trees if robot in it or there are no bullets in it
				// If cannot do those things to nearest tree then moves towards nearest tree
				if (nearbyNeutralTrees.size() > 0) {
				    manageNeutralTrees();
				}
		
                // See if there are any enemy robots within striking range (distance 1 from lumberjack's radius)
                RobotInfo[] robots = rc.senseNearbyRobots(GameConstants.LUMBERJACK_STRIKE_RADIUS, enemy);
                if(robots.length > 0 && !rc.hasAttacked()) {
                    // Use strike() to hit all nearby robots!
				    System.out.println("STRIKING ENEMIES GRRR");
		            rc.strike();
                }
               
				// No close robots, so search for robots within sight radius
				robots = rc.senseNearbyRobots(-1,enemy);
				if (robots.length > 0 && !rc.hasMoved() && !foundPrize) { // If there is a robot, move towards it			
				    MapLocation enemyLocation = robots[0].getLocation();
				    
				    Direction toEnemy = myLocation.directionTo(enemyLocation);
				    
				    if (rc.canMove(toEnemy)) {
				    	// If it can move then determine whether it will die if it moves there and move it
				    	MapLocation endLoc = myLocation.add(toEnemy);
				    	iFeed.willFeed(endLoc);
				    	rc.move(toEnemy);
				    	System.out.println("TRYING TO MOVE TO ENEMY");
				    }

				}			    
		    
				// If hasn't moved yet then get away from ally or move randomly
				if(!rc.hasMoved() && !foundPrize){
				    if (currentAllies.length > 0){
				    	//RobotInfo closestAlly = getNearestAlly();
				    	MapLocation newLoc = Chirasou.Disperse(rc.getTeam(), myLocation, battlecode.common.RobotType.LUMBERJACK.strideRadius);
				    	if (newLoc != null) {
				    		rc.move(newLoc);
				    		iFeed.willFeed(newLoc);
				    		System.out.println("MOVING AWAY FROM CLOSEST ALLY");
				    	}
				    	System.out.println("Disperse is null");
				    	
				    } else {
						Direction testDir = Move.randomDirection();
						MapLocation newLoc = tryMoveLumberjack(testDir);
						if (newLoc != null) {
							iFeed.willFeed(newLoc);
							System.out.println("MOVING IN A RANDOM DIR");
						} else {
							System.out.println("TRIED MOVING IN A RANDOM DIR");
						}
				    }
		    
				}
				
				// This means the lumberjack found the prize and isn't moving until he gets it
				if (foundPrize) {
					iFeed.willFeed(myLocation);
					System.out.println("DIGGING PRIZE");
				}
				
				// Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
				Clock.yield();
		
            } catch (Exception e) {
		
            	System.out.println("Lumberjack Exception");
            	e.printStackTrace();
            }
	
        }
    }
	
    private static void checkGroupAssignments() throws GameActionException{
	
    	int newGroups = rc.readBroadcast(8 + homeArchon * ARCHON_OFFSET);
    	if (newGroups == 1){
	    
		    int check = rc.readBroadcast(GROUP_NUMBER_CHANNEL);
		    
		    boolean isGroup = false;
		    
		    for (int i = 0; i < GROUP_SIZE_LIMIT; i++){
				if (rc.readBroadcast(GROUP_START + check * GROUP_OFFSET + 5 + i) == ID){
				    isGroup = true;
				    if (i == 0){
				    	isLeader = true;    					
				    }
				}
		    }    		
		    if (isGroup){
			
		    	command = rc.readBroadcast(GROUP_START + check * GROUP_OFFSET + 1);
		    	currentGroup = check;
		    	System.out.println("I have joined a group: " + currentGroup + "with command type: " + command);
		    }   		
    	}
    }

    private static void manageNeutralTrees() throws GameActionException {
	
        // Assumes that nearbyNeutralTrees is not empty
    	TreeInfo nearestTree = nearbyNeutralTrees.get(0);
    	if (nearestTree.getContainedRobot() != null) {
		    
		    // There is a prize! Chop the tree to get it
		    if (rc.canChop(nearestTree.getLocation())) {
			System.out.println("DIGGING FOR PRIZE");
			rc.chop(nearestTree.getLocation());
		        foundPrize = true;
				if (nearestTree.getHealth() <= 5) {
				    foundPrize = false;
				    System.out.println("NOT DIGGING FOR PRIZE ANYMORE");
				}
		
		    } else {
				// If can't chop then move towards the tree (most likely it means it is too far away)
		    	if (foundPrize) {
		    		foundPrize = false;
		    	}
				if (!rc.hasMoved()) {
				    Direction move = new Direction(myLocation,nearestTree.getLocation());
				    if (rc.canMove(move)) {
				    	// If it can move then determine whether it will die if it moves there and move it
				    	MapLocation endLoc = myLocation.add(move);
				    	iFeed.willFeed(endLoc);
				    	rc.move(move);
				    	System.out.println("TRYING TO MOVE TO TREE");
				    }
				}
		    }
		
    	} else if (nearestTree.getContainedBullets() > 0) {
		
    		// There are bullets! Shake it SHAKE IT
    		if (rc.canShake(nearestTree.getLocation())) {
				System.out.println("SHAKE IT SHAKE IT");
				rc.shake(nearestTree.getLocation());
		
    		} else {
				// If can't shake then move towards the tree (most likely it means it is too far away)
				if (!rc.hasMoved()) {
				    Direction move = new Direction(myLocation,nearestTree.getLocation());
				    if (rc.canMove(move)) {
				    	// If it can move then determine whether it will die if it moves there and move it
				    	MapLocation endLoc = myLocation.add(move);
				    	iFeed.willFeed(endLoc);
				    	rc.move(move);
				    	System.out.println("TRYING TO MOVE TO TREE");
				    }
				}
    		}
    	} else {
		
		    if (rc.canChop(nearestTree.getLocation())) {
				rc.chop(nearestTree.getLocation());
				System.out.println("CHOP USELESS TREE");
		
		    } else {
			// If can't chop then move towards the tree (most likely it means it is too far away)
				if (!rc.hasMoved()) {
				    Direction move = new Direction(myLocation,nearestTree.getLocation());
				    if (rc.canMove(move)) {
				    	// If it can move then determine whether it will die if it moves there and move it
				    	MapLocation endLoc = myLocation.add(move);
				    	iFeed.willFeed(endLoc);
				    	rc.move(move);
				    	System.out.println("TRYING TO MOVE TO TREE");
				    }

				}
		    }
    	}
    }
    
    private static MapLocation tryMoveLumberjack(Direction dir) throws GameActionException {
    	return tryMoveLumberjack(dir,15,4);
    }
    
    
    private static MapLocation tryMoveLumberjack(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {
	
    	MapLocation newLoc = null;
		float testDistance = (float) Math.random() * (float) 2;
		// First, try intended direction
		if (rc.canMove(dir, testDistance)){
		    rc.move(dir, testDistance);
		    newLoc = rc.getLocation().add(dir, testDistance);
		    lastDirection = dir;
		    return newLoc;
		}
	
		int currentCheck = 1;
	
		while(currentCheck<=checksPerSide) {
		    // Try the offset of the left side
		    if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
				rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck));
				lastDirection = dir.rotateLeftDegrees(degreeOffset*currentCheck);
				newLoc = rc.getLocation().add(dir.rotateLeftDegrees(degreeOffset*currentCheck));
				return newLoc;
		    }
		    // Try the offset on the right side
		    if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
				rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck));
				lastDirection = dir.rotateRightDegrees(degreeOffset*currentCheck);
				newLoc = rc.getLocation().add(dir.rotateRightDegrees(degreeOffset*currentCheck));
				return newLoc;
		    }
		    // No move performed, try slightly further
		    currentCheck+=1;
		}
	
		// A move never happened, so return false.
		return newLoc;
    }
    /*
    private static RobotInfo getNearestAlly(){
		
		float minimum = Integer.MAX_VALUE;
		
		int index = 0;
		
		for (int i = 0; i < currentAllies.length; i++){
		    
		    float dist = myLocation.distanceTo(currentAllies[i].location);
		    
		    if (dist < minimum ){
			minimum = dist;
			index = i;
			
		    }			
		}	
	
		return currentAllies[index];
    }

    
    private static void tryMoveAway(RobotInfo ally) throws GameActionException{
	
		float gap = myLocation.distanceTo(ally.location);
		
		
		Direction dir = myLocation.directionTo(ally.location);
		
		float keikaku = (float)(Math.random() *  Math.PI/3 - Math.PI/6);
		
		Direction anti_dir = new Direction(dir.radians+(float) Math.PI + keikaku);
		
		for (int i = 0; i < 4; i ++) {
		    if (rc.canMove(anti_dir, (float)(2- 0.4*i)) && !rc.hasMoved()){
		    	rc.move(anti_dir, (float)(2- 0.4*i));
		    	lastDirection = anti_dir;
		    }			
		}
    }
    */
    
}
