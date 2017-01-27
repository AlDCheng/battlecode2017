// AI for soldier under normal control
package naclbot.units.AI.soldier;
import battlecode.common.*;
import naclbot.units.motion.Move;
//import naclbot.units.motion.routing.PathPlanning;
import naclbot.units.motion.routing.Routing;
import naclbot.units.motion.dodge.BulletDodge;
import naclbot.units.motion.search.AllySearch;
import naclbot.units.motion.shoot.Aim;
import naclbot.units.motion.shoot.RobotInfoShoot;
import naclbot.units.motion.shoot.ShootingType;
import naclbot.units.motion.Yuurei;
import naclbot.variables.DataVars;
import naclbot.variables.DataVars.*;
import naclbot.variables.GlobalVars;
import naclbot.variables.DataVars.basicTreeInfo;
import naclbot.variables.DataVars.binarySearchTree;
import naclbot.units.motion.routing.WallFollowing;

import java.util.ArrayList;
import java.util.Arrays;

public class SoldierBotMoveTest extends GlobalVars {
	
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
	
	public static Direction lastDirection;
    
    public static final basicTreeInfo dummyTree = new basicTreeInfo(-1, -1, -1, -1);
    public static final basicTreeInfo[] dummyTreeInfo = {dummyTree};	
    
    public static binarySearchTree treeList = new binarySearchTree(dummyTreeInfo);
    
    public static ArrayList<RobotInfoShoot> enemyToShoot = new ArrayList<RobotInfoShoot>();

    public static int currentTrackID;
    public static RobotInfo currentTrack;
    
    public static void init() throws GameActionException{
    	
    	/*lastDirection = Move.randomDirection();
    	
    	currentTrackID = -1;
    	
    	System.out.println("I'm an soldier!");
        enemy = rc.getTeam().opponent();
        allies = rc.getTeam();
        
        
        currentRound = rc.getRoundNum();
        initRound = currentRound;
        
        ID = rc.getID();
        
        int archonCount = rc.readBroadcast(ARCHON_CHANNEL);
        homeArchon = (int) (Math.random() *archonCount);*/
		
        main();
    }
    
    public static void attack() throws GameActionException{
    	
	
		// Checks if robot is leader....	
		
		if (isLeader){
			// If the robot is deigned to be the leader of this group
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
				//				ArrayList<MapLocation> path = PathPlanning.findPath(rc.getLocation(), targetLocation);
				
				System.out.println("Currently in attack for group: " + currentGroup);							
				
				trackingEnemies();
				
				tryShoot();
				
				if(!rc.hasMoved()){
					if (currentAllies.length > 0){
						RobotInfo closestAlly  = getNearestAlly();
						tryMoveAway(closestAlly);
					}
					if (!rc.hasMoved()){
						Direction testDir = Move.randomDirection();
	        			tryMoveSoldier(testDir);
					}
						
					
								
								
				}

				Clock.yield();
			
				
		    }  
		    
		    catch (Exception e) {
	                System.out.println("Soldier Exception");
	                e.printStackTrace();
		    }		
		    
		    // Checks if robot is leader....	
		    
		   
		}
    }
    
    
    //TODO make soldiers patrol around a certain location
    public static void patrol(MapLocation targetLocation) throws GameActionException{
	
    }
	
    public static void main() throws GameActionException {
    	
		// Important variables
		
		int notMoved = 0;
		MapLocation prevLocation = rc.getLocation();
	
		// Tracking variables
		//SoldierVictim currentlyTracking = null;
			
		myLocation = rc.getLocation(); // Current location
		MapLocation goalLoc = new MapLocation(480,380);
		Routing.setRouting(goalLoc);
		MapLocation desiredMove = rc.getLocation(); 
		
		// The code you want your robot to perform every round should be in this loop
        while (true) {
	    // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	if(rc.canMove(desiredMove)){
//            		manageBeingAttacked(desiredMove);
            		rc.move(desiredMove);
            	}
            	
            	Routing.routingWrapper();
    	    	
    	    	// Set the desired Move
//    	    	rc.move(Routing.path.get(0));
    	    	desiredMove = Routing.path.get(0);
            	
    	    	/*
    	    	MapLocation correctedMove = Yuurei.correctAllMove(strideRadius, bodyRadius, rotationDirection, allies, myLocation, desiredMove);
            	
            	if (correctedMove != null){
            		desiredMove = correctedMove;
            	}*/
            	
            	
    	    	
    	    	
    	    	// SYSTEM CHECK - Show desired move after path planning
//    	    	System.out.println("desiredMove before post-processing: " + desiredMove.toString());
            	
            	/*currentEnemies = rc.senseNearbyRobots(-1, enemy);
            	currentAllies = rc.senseNearbyRobots(-1, allies);
            	
            	binarySearchTree.combatUpdateTrees(treeList, 0);
            	     
            	checkGroupAssignments();
            	
            	// check if thee robot has entered a group or not
            	if (currentGroup >= 0){
		    
				    // Check the value of the command bit of the group
				    
				    // command 1 is an attack command
				    if (command == 1){
				    	attack();
				    }
		    	}
								
				trackingEnemies();
				
				tryShoot();
				
				if(!rc.hasMoved()){
					if (lastDirection != null){
						tryMoveSoldier(lastDirection);
					}
				}
				if(!rc.hasMoved()){
					if (currentAllies.length > 0){
						RobotInfo closestAlly  = getNearestAlly();
						tryMoveAway(closestAlly);
					}
				}
					if (!rc.hasMoved()){
						Direction testDir = Move.randomDirection();
	        			tryMoveSoldier(testDir);
	        			if (rc.hasMoved()){
	        			lastDirection = testDir;
	        			}
					}
						
					
								
								
				*/
				/*	
				BulletInfo[] nearbyBullets = rc.senseNearbyBullets();
				if (nearbyBullets.length > 0) {
				    Direction dodge = BulletDodge.whereToDodge(nearbyBullets);
				    Direction noDodge = new Direction(-1);
				    if (dodge != null) {
						System.out.println("TRYING TO DODGE");
						Move.tryMove(dodge);
				    }
				}
				*/
				
				//*Check if it hasn't moved
				/*
				if (myLocation == prevLocation) {
				    notMoved += 1;
				}
				
				*/
				/*
				// Move to other allies 
				if (currentAllies.length > 0 && notMoved < 5 && !rc.hasMoved()) {
				    MapLocation locAlly = AllySearch.locFurthestAlly(currentAllies);
				    if (locAlly == rc.getLocation()) {
						Move.tryMove(Move.randomDirection());
						notMoved += 1;
				    } else {
						Direction dir = rc.getLocation().directionTo(locAlly);
						Move.tryMove(dir);
				    }
				    
				} else if (!rc.hasMoved()) {
				    Move.tryMove(Move.randomDirection());
				    notMoved = 0; // Reset counter
				}
				
				*/
				

				// Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
				Clock.yield();
		
		
            } catch (Exception e) {
                System.out.println("Soldier Exception");
                e.printStackTrace();
            }
        }
    }
    
    private static void tryShoot() throws GameActionException{
    	
		/* ---------------------------------- SHOOTING --------------------------------------*/
		// Shoots enemies that have been tracked and takes care not to single shoot to ally
		// TODO: implement no shoot for triad and pentad to ally (?)
		
		// Sense nearby enemy robots
		
		shootingEnemies(currentEnemies,currentAllies,enemyToShoot); // SHOOTS TO NEAREST ENEMY
		
		// Resets the list of past enemies to add new ones
		enemyToShoot.clear();
		
		// Adds the ones seen this turn
		for (RobotInfo enemyRobot: currentEnemies) {
		    MapLocation currentLoc = enemyRobot.getLocation();
		    int ID = enemyRobot.getID();
		    RobotType robType = enemyRobot.getType();
		    RobotInfoShoot r = new RobotInfoShoot(ID, robType, currentLoc);
		    enemyToShoot.add(r);
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
    
    private static void trackingEnemies() throws GameActionException {
    	
    	if(currentTrackID == -1){
			float minimum = Integer.MAX_VALUE;
			
			int index = -1;
			
			for (int i = 0; i < currentEnemies.length; i++){
	
				float dist = myLocation.distanceTo(currentEnemies[i].location);
	
				if (dist < minimum ){
					minimum = dist;
					index = i;
		
				}
				
			}		
			
			if (index != -1){
					currentTrack =  currentEnemies[index];		
					currentTrackID = currentTrack.ID;
					
					lastDirection = moveTowards(currentTrack);
			
			}	   			
    	}
    	else{
    		if (rc.canSenseRobot(currentTrackID)){
    			
    			lastDirection = moveTowards(currentTrack);
	
    		}
    		else{
    			currentTrack = null;
    			currentTrackID = -1;
    			
    			System.out.println("Lost sight of previous enemy");
    		}    		
    		
    	}
    		
    }
		
    		
    	
    
    private static RobotInfo[] senseNearbyEnemies(Team enemy){
    	return rc.senseNearbyRobots(-1, enemy);
    }

    private static void shootingEnemies(RobotInfo[] enemies, RobotInfo[] allies, ArrayList<RobotInfoShoot> pastEnemies) throws GameActionException {
		// Checks if there are enemies to trace
		// Checks if the unit has attacked already
		if (pastEnemies.size() != 0 && !rc.hasAttacked()) {
		    ShootingType shoot = Aim.shootNearestEnemy(pastEnemies, enemies, false); // Returns details about shooting
		    boolean hitAlly = false;
		    
		    // ShootingType: bulletType, isArchon, direction
		    // Checks if we should actually shoot (bulletType is none if we cannot shoot)
		    if (shoot != null) {
		    	// No shooting if there is an ally in the way
		    	for (RobotInfo ally: allies) {
				    // Takes into account if the bullet will hit ally
				    // This only works for single shots because they go in that direction. Triad and pentad are not considered (because they fan out)
				    boolean h = willHitAlly(ally,shoot);
		
				    // This means the bullet will hit the ally
				    if (h) {
					System.out.println("WILL HIT ALLY ZOMG");
					hitAlly = h;
				    	break;
				    }
				}
		
				if (!hitAlly) {
				    // Shoot
				    if (shoot.getBulletType() == "pentad" && !rc.hasAttacked()) {
						rc.firePentadShot(shoot.getDirection());
						System.out.println("FIRING PENTAD");
				    } else if (shoot.getBulletType() == "triad" && !rc.hasAttacked()) {
						rc.fireTriadShot(shoot.getDirection());
						System.out.println("FIRING TRIAD");
				    } else if (shoot.getBulletType() == "single" && !rc.hasAttacked()) {
						rc.fireSingleShot(shoot.getDirection());
						System.out.println("FIRING SINGLE");
				    }
				}
		    } else {
			System.out.println("IM NOT SHOOTING BECAUSE THE ENEMY LIKES REM");
		    }
 		}
    }

    private static boolean willHitAlly(RobotInfo frond, ShootingType shootInfo) {
		MapLocation myLoc = rc.getLocation();
		MapLocation allyLoc = frond.getLocation();
		
		Direction allyDir = new Direction(myLoc, allyLoc);
		Direction shootDir = shootInfo.getDirection();
		
		float theta = allyDir.radiansBetween(shootDir);
		
		float allyDist = myLoc.distanceTo(allyLoc);
		float enemyDist = shootInfo.getDistance();
	
		// If ally in front of enemy then if the radius thing complies it will hit ally 
		if (enemyDist > allyDist) {
		    //if (allyDist * Math.sin(theta) <= frond.getRadius()) {
		    if (allyDir == shootDir) {
		    	return true;
		    } else {
		    	return false;
		    }
		    
		} else {
		    // Enemy is not in front of ally
		    return false;
		}
    }
    
    
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
     	
     	for (int i = 0; i < 4; i ++)
	     	if (rc.canMove(anti_dir, (float)(2- 0.4*i)) && !rc.hasMoved()){
	     		rc.move(anti_dir, (float)(2- 0.4*i));
	     		lastDirection = anti_dir;
	     	}			
     	
    }
    
	private static Direction moveTowards(RobotInfo quandary) throws GameActionException{
		
			if (quandary != null){
		 		
		 		float gap = myLocation.distanceTo(quandary.location);
		 		
		     	Direction dir = myLocation.directionTo(quandary.location);
		     	Direction perp = new Direction(dir.radians+((float) Math.PI/2));
		     	
		     	Direction anti_perp = new Direction(dir.radians+((float) Math.PI/2));	
		     	Direction anti_dir = new Direction(dir.radians+(float) Math.PI);
		
		 		if  (gap > 7){
		 			// Move towards target]
		 			if (rc.canMove(dir)){							
		 				rc.move(dir);
		 				return dir;
		 			}
		 			else{Direction dir2 = Move.randomDirection();
		      			tryMoveSoldier(dir2);
		      			return dir2;
		 			}
		 			
		 		} else if (gap < 3) {
		 			// Move away from target
		 			if (rc.canMove(anti_dir)){							
		 				rc.move(anti_dir);
		 				return dir;
		 			}
		 			else{Direction dir2 = Move.randomDirection();
		      			tryMoveSoldier(dir2);
		      			return dir2;
		 			}
		 			
		 		} else {
		 			float nani = (float) Math.random();
		 			float keikaku =  (float) Math.random() + (float) 1;
		 			
		 			if (nani>0.5){
		 				
		 				if (rc.canMove(perp)){							
		 					rc.move(perp, keikaku);
		 					return perp;
		 					
		 				} else if (rc.canMove(anti_perp)){							
		 					rc.move(anti_perp,keikaku);
		 					return anti_perp;
		 					
		 				} else{Direction dir2 = Move.randomDirection();
		 					tryMoveSoldier(dir2);
		 					return dir2;
		 				}
		 			}   else{
		 				
		 				if (rc.canMove(anti_perp)){							
		 					rc.move(anti_perp, keikaku);
		 					return anti_perp;
		 					
		 				} else if (rc.canMove(perp)){							
		 					rc.move(perp, keikaku);
		 					return perp;
		 					
		 				} else{Direction dir2 = Move.randomDirection();
		 					tryMoveSoldier(dir2);
		 					return dir2;
		 				}	 		
		 				
		 			}
		 		}	 	
	 		}
			return Move.randomDirection();
	 	}
 	private static boolean tryMoveSoldier(Direction dir) throws GameActionException {
 	        return tryMoveSoldier(dir,30,3);
 	    }

 	    
    private static boolean tryMoveSoldier(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {
    	
    	float testDistance = (float) Math.random() * (float) 2;
        // First, try intended direction
        if (rc.canMove(dir, testDistance)){
            rc.move(dir, testDistance);
            
            lastDirection = dir;
            return true;
        }

        int currentCheck = 1;

        while(currentCheck<=checksPerSide) {
            // Try the offset of the left side
            if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck));
                lastDirection = dir.rotateLeftDegrees(degreeOffset*currentCheck);
                return true;
            }
            // Try the offset on the right side
            if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck));
                lastDirection = dir.rotateRightDegrees(degreeOffset*currentCheck);
                return true;
            }
            // No move performed, try slightly further
            currentCheck+=1;
        }

        // A move never happened, so return false.
        return false;
    }

    
    

}


