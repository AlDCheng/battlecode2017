// AI for tank under normal control
package naclbot.units.AI.tank;
import battlecode.common.*;
import naclbot.units.interact.iFeed;
import naclbot.units.motion.Chirasou;
import naclbot.units.motion.Move;
import naclbot.units.motion.shoot.Aim;
import naclbot.units.motion.routing.Routing;
import naclbot.units.motion.search.TreeSearch;
import naclbot.variables.DataVars;
import naclbot.variables.GlobalVars;
import naclbot.variables.DataVars.basicTreeInfo;
import naclbot.variables.DataVars.binarySearchTree;

import java.util.ArrayList;

public class TankBot extends GlobalVars {
    
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
    
	public static MapLocation destination;
	public static ArrayList<MapLocation> pathToMove;

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
            	
            	// check if the robot has entered a group or not
            	if (currentGroup >= 0){
		    
            		// Check the value of the command bit of the group
		    
            		// command 1 is an attack command
            		if (command == 1){
            			attack();
            		}
            	}
            	
            	myLocation = rc.getLocation(); // Current location
		   
            	// Move to desired location with ALAN D CHENG'S path planning
            	destination = new MapLocation(450,450);
            	pathToMove = new ArrayList<MapLocation>();
            	pathToMove.add(destination);
            	
            	Routing.setRouting(pathToMove);
            	
            	Routing.routingWrapper();
				
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
