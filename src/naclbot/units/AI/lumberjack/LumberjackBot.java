// AI for lumber jack

package naclbot.units.AI.lumberjack;
import battlecode.common.*;
import naclbot.units.motion.Move;
import naclbot.units.motion.search.TreeSearch;
import naclbot.variables.GlobalVars;
import naclbot.units.motion.dodge.BulletDodge;

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
	
	
	public static void init() throws GameActionException{
		System.out.println("I'm an lumberjack!");
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
								
					int targetX = rc.readBroadcast(GROUP_START + currentGroup * GROUP_OFFSET + 3);
					int targetY = rc.readBroadcast(GROUP_START + currentGroup * GROUP_OFFSET + 4);
					int targetID = rc.readBroadcast(GROUP_START + currentGroup * GROUP_OFFSET + 2);
					
					MapLocation targetLocation = new MapLocation(targetX, targetY);				
					
					System.out.println("Currently in attack for group: " + currentGroup);
					
					Clock.yield();
				}
				catch (Exception e) {
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
        		
                // See if there are any enemy robots within striking range (distance 1 from lumberjack's radius)
                RobotInfo[] robots = rc.senseNearbyRobots(RobotType.LUMBERJACK.bodyRadius+GameConstants.LUMBERJACK_STRIKE_RADIUS, enemy);
                if(robots.length > 0 && !rc.hasAttacked()) {
                    // Use strike() to hit all nearby robots!
                    rc.strike();
                } else {
                    // No close robots, so search for robots within sight radius
                    robots = rc.senseNearbyRobots(-1,enemy);
                    ArrayList<MapLocation> nearbyBulletTrees = TreeSearch.getNearbyBulletTrees();
                    ArrayList<MapLocation> nearbyNeutralTrees = TreeSearch.getNearbyNeutralTrees();
		    
				    // Search bullets and dodge
				    BulletInfo[] nearbyBullets = rc.senseNearbyBullets();
				    if (nearbyBullets.length > 0) {
					    	
						Direction dodge = BulletDodge.whereToDodge(nearbyBullets);
						Direction noDodge = new Direction(-1);
						
						if (dodge!= noDodge) {
		
							Move.tryMove(dodge);
						}
				    } else {
					
						if (robots.length > 0) { // If there is a robot, move towards it			
						    MapLocation enemyLocation = robots[0].getLocation();
						    
						    Direction toEnemy = myLocation.directionTo(enemyLocation);
						    
						    Move.tryMove(toEnemy);
						    
						} else if (nearbyBulletTrees.size() > 0) { // If there is instead a nearby tree that can be shaken
						    
							MapLocation nearestBulletTree = TreeSearch.locNearestTree(nearbyBulletTrees); 
						    
							if (rc.canShake(nearestBulletTree)) { // If close enough, then shake
			                    		
								rc.shake(nearestBulletTree);
						    
							} else { // If not close enough, walk towards it
				                        
								Direction toBulletTree = rc.getLocation().directionTo(nearestBulletTree);
				                        Move.tryMove(toBulletTree);
						    }
						
						} else if (nearbyNeutralTrees.size() > 0) { // If there is instead a neutral tree that can be chopped
						    
							MapLocation nearestNeutralTree = TreeSearch.locNearestTree(nearbyNeutralTrees);
						    
							if (rc.canChop(nearestNeutralTree)) {
			                    		
								rc.chop(nearestNeutralTree);
						   
							} else {
			                    		Direction toNeutralTree = rc.getLocation().directionTo(nearestNeutralTree);
			                    		Move.tryMove(toNeutralTree);
						    }
						} else {
						    // Move Randomly
						    Move.tryMove(Move.randomDirection());
							}
				    	}
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
    
}
