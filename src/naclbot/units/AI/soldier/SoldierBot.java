// AI for soldier under normal control
package naclbot.units.AI.soldier;
import battlecode.common.*;
import naclbot.units.motion.Move;
import naclbot.units.motion.routing.PathPlanning;
import naclbot.units.motion.dodge.BulletDodge;
import naclbot.units.motion.search.AllySearch;
import naclbot.units.motion.shoot.Aim;
import naclbot.units.motion.shoot.RobotInfoShoot;
import naclbot.units.motion.shoot.ShootingType;
import naclbot.variables.DataVars;
import naclbot.variables.DataVars.*;
import naclbot.variables.GlobalVars;
import naclbot.variables.DataVars.basicTreeInfo;
import naclbot.variables.DataVars.binarySearchTree;

import java.util.ArrayList;
import java.util.Arrays;

public class SoldierBot extends GlobalVars {
	
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
    
    public static ArrayList<RobotInfoShoot> enemyToShoot = new ArrayList<RobotInfoShoot>();

    public static int currentTrackID;
    
    
    public static void init() throws GameActionException{
    	
    	currentTrackID = -1;
    	
    	System.out.println("I'm an soldier!");
        enemy = rc.getTeam().opponent();
        allies = rc.getTeam();
        
        
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
				
				tryShoot();

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
		
		// The code you want your robot to perform every round should be in this loop
        while (true) {
	    // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

            	currentEnemies = rc.senseNearbyRobots(-1, enemy);
            	currentAllies = rc.senseNearbyRobots(-1, allies);
            	
            	binarySearchTree.combatUpdateTrees(treeList, 0);
            	//treeList.printInOrder(treeList.tree_root);
          
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
					
					
			BulletInfo[] nearbyBullets = rc.senseNearbyBullets();
			if (nearbyBullets.length > 0) {
			    Direction dodge = BulletDodge.whereToDodge(nearbyBullets);
			    if (dodge != null) {
					System.out.println("TRYING TO DODGE");
					Move.tryMove(dodge);
			    }
			}
			
			// Check if it hasn't moved
			if (myLocation == prevLocation) {
			    notMoved += 1;
			}
			
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
			
			System.out.println("End turn");
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
	

    }
    
    private static RobotInfo[] senseNearbyEnemies(Team enemy){
    	return rc.senseNearbyRobots(-1, enemy);
    }

    private static void shootingEnemies(RobotInfo[] enemies, RobotInfo[] allies, ArrayList<RobotInfoShoot> pastEnemies) throws GameActionException {
		// Checks if there are enemies to trace
		// Checks if the unit has attacked already
		if (!pastEnemies.isEmpty() && !rc.hasAttacked()) {
		    ShootingType shoot = Aim.shootNearestEnemy(pastEnemies, enemies, false); // Returns details about shooting
		    boolean hitAlly = false;
		    
		    // ShootingType: bulletType, isArchon, direction
		    // Checks if we should actually shoot (bulletType is none if we cannot shoot)
		    if (shoot != null) {
		    	// No shooting if there is an ally in the way
		    	for (RobotInfo ally: allies) {
				    // Takes into account if the bullet will hit ally
				    // This only works for single shots because they go in that direction. Triad and pentad are not considered (because they fan out)
				    hitAlly = willHitAlly(ally,shoot);
		
				    // This means the bullet will hit the ally
				    if (hitAlly) {
					System.out.println("WILL HIT ALLY");
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
		    if (allyDist * Math.sin(theta) <= frond.getRadius()) {
		    	return true;
		    } else {
		    	return false;
		    }
		    
		} else {
		    // Enemy is not in front of ally
		    return false;
		}
    }

}


