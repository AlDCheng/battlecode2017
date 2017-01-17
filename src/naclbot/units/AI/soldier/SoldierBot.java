// AI for soldier under normal control
package naclbot.units.AI.soldier;
import battlecode.common.*;
import naclbot.units.motion.Move;
import naclbot.units.motion.dodge.BulletDodge;
import naclbot.units.motion.search.AllySearch;
import naclbot.units.motion.shoot.Aim;
import naclbot.units.motion.shoot.RobotInfoShoot;
import naclbot.units.motion.shoot.ShootingType;
import naclbot.variables.DataVars;
import naclbot.variables.GlobalVars;
import naclbot.variables.DataVars.basicTreeInfo;
import naclbot.variables.DataVars.binarySearchTree;

import java.util.ArrayList;

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
	
	public static final basicTreeInfo dummyTree = new basicTreeInfo(-1, -1, -1, -1);
	public static final basicTreeInfo[] dummyTreeInfo = {dummyTree};	

	public static binarySearchTree treeList = new binarySearchTree(dummyTreeInfo);
		
	
	
	public static void init() throws GameActionException{
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
			System.out.println("I'm leader of this group WAT");
			rc.broadcast(GROUP_LEADER_START * currentGroup * GROUP_LEADER_OFFSET + 1, ID);
			rc.broadcast(GROUP_LEADER_START * currentGroup * GROUP_LEADER_OFFSET + 2, (int)myLocation.x);
			rc.broadcast(GROUP_LEADER_START * currentGroup * GROUP_LEADER_OFFSET + 3, (int)myLocation.y);
		}
		
		while (true){
			try{
				DataVars.updateTrees(treeList);
				updateMapTrees(DataVars.treeMapFormat);
				
				int targetX = rc.readBroadcast(GROUP_START + currentGroup * GROUP_OFFSET + 3);
				int targetY = rc.readBroadcast(GROUP_START + currentGroup * GROUP_OFFSET + 4);
				int targetID = rc.readBroadcast(GROUP_START + currentGroup * GROUP_OFFSET + 2);
				
				MapLocation targetLocation = new MapLocation(targetX, targetY);				
				
				System.out.println("Currently in attack for group: " + currentGroup);
				
				Clock.yield();
			}
			catch (Exception e) {
                System.out.println("Soldier Exception");
                e.printStackTrace();
			}		
		}
	}
	
	
	//TODO make soldiers patrol around a certain location
	public static void patrol(MapLocation targetLocation) throws GameActionException{
		
	}
	
    public static void main() throws GameActionException {
    	
		// Important variables
		ArrayList<RobotInfoShoot> enemyToShoot = new ArrayList<RobotInfoShoot>();
		int notMoved = 0;
		MapLocation prevLocation = rc.getLocation();
		boolean hasMoved = false;
	
		myLocation = rc.getLocation(); // Current location
		
		boolean leaveArchon = false; // If we want to make it surround archon until later grouping
		
        // The code you want your robot to perform every round should be in this loop
        while (true) {
	    // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	DataVars.updateTrees(treeList);
            	checkGroupAssignments();
            	
            	// check if thee robot has entered a group or not
            	if (currentGroup >= 0){
            		            		
            		// Check the value of the command bit of the group
            		
            		// command 1 is an attack command
            		if (command == 1){
            			attack();
            		}
            	}
		 
			// Sense nearby enemy robots
			RobotInfo[] currentEnemies = rc.senseNearbyRobots(-1, enemy);
			RobotInfo[] currentAllies = rc.senseNearbyRobots(-1, allies);
	
			// Shooting 
			if (!enemyToShoot.isEmpty()) {
			    //boolean canShoot = false;
			    ShootingType shoot = Aim.toShoot(enemyToShoot, currentEnemies);
	
			    if (shoot.getBulletType() != "none") {
				// No shooting if there is an ally in the way
				for (RobotInfo ally: currentAllies) {
				    MapLocation allyLoc = ally.getLocation();
				    Direction allyDir = new Direction(myLocation, allyLoc);
						    
				    // TODO: implement radius location of ally
				    if (allyDir != shoot.getDirection()) {
					// Shoot
					if (shoot.getBulletType() == "pentad") {
					    rc.firePentadShot(shoot.getDirection());
					} else if (shoot.getBulletType() == "triad") {
					    rc.fireTriadShot(shoot.getDirection());
					} else if (shoot.getBulletType() == "single") {
					    rc.fireSingleShot(shoot.getDirection());
					}
				    }	    
				}
			    }
			}
			// Resets the list to add new ones
			enemyToShoot.clear();
	
			// Adds the ones seen this turn
			for (RobotInfo enemyRobot: currentEnemies) {
			    MapLocation currentLoc = enemyRobot.getLocation();
			    int ID = enemyRobot.getID();
			    RobotType robType = enemyRobot.getType();
			    RobotInfoShoot r = new RobotInfoShoot(ID, robType, currentLoc);
			    enemyToShoot.add(r);
			}
	
			// MOVEMENT 
			BulletInfo[] nearbyBullets = rc.senseNearbyBullets();
			if (nearbyBullets.length > 0) {
			    Direction dodge = BulletDodge.whereToDodge(nearbyBullets);
			    Direction noDodge = new Direction(-1);
			    if (dodge != noDodge) {
	
				Move.tryMove(dodge);
				hasMoved = true;
			    }
			}
			
			/*		
			// TODO: Make it stay near archon
			if (leaveArchon == false && hasMoved == false) {
			    MapLocation archonLoc = new MapLocation(xPos,yPos);
			    Direction dir = new Direction(myLocation,archonLoc);
			    Move.tryMove(dir);
			}
			*/
			
			// If hasn't found archon or is not of protectinc archon role
			// Check if it hasn't moved
			if (myLocation == prevLocation) {
			    notMoved += 1;
			}
			
			// Move to other allies 
			if (currentAllies.length > 0 && notMoved < 5 && hasMoved == false) {
			    MapLocation locAlly = AllySearch.locFurthestAlly(currentAllies);
			    if (locAlly == rc.getLocation()) {
				Move.tryMove(Move.randomDirection());
				notMoved += 1;
			    } else {
				Direction dir = rc.getLocation().directionTo(locAlly);
				Move.tryMove(dir);
			    }
			    
			    
			} else if (hasMoved == false) {
			    Move.tryMove(Move.randomDirection());
			    notMoved = 0; // Reset counter
			}
			
			
	                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
			hasMoved = false;
			Clock.yield();
			

            } catch (Exception e) {
                System.out.println("Soldier Exception");
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
    
    
    private static RobotInfo[] senseNearbyEnemies(Team enemy){
		return rc.senseNearbyRobots(-1, enemy);
	}
}


