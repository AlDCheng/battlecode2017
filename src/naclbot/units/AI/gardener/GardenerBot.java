// AI for gardener under normal control
package naclbot.units.AI.gardener;
import battlecode.common.*;
import naclbot.units.motion.Move;
import naclbot.units.motion.search.TreeSearch;
import naclbot.variables.GlobalVars;

import java.util.ArrayList;

public class GardenerBot extends GlobalVars {
	
	public static int role;
	
	public static int treeCount = 0;
	public static boolean canMove = true;
	
	public static int numGard;
	public static int prevNumBuilder;
	public static int prevNumWaterer;
	
	public static int scoutCount;
	public static int soldierCount;
	public static int lumberjackCount;
	public static int tankCount;
	
	public static int xPos;
	public static int yPos;
	public static MapLocation archonLoc;
	
	public static Direction dir;
	
	public static ArrayList<MapLocation> lowHealthTrees;
	
	public static boolean nearbyGardAndArc;
	
	
	public static void init() throws GameActionException {
		System.out.println("I'm a gardener!");
		
		role = 0;
		
		// AC: Quick hotfix to have deterministic selection. Should update code to read from broadcast intelligently
		numGard = rc.readBroadcast(GARDENER_CHANNEL);
		prevNumBuilder = rc.readBroadcast(GARDENER_BUILDER_CHANNEL);
		prevNumWaterer = rc.readBroadcast(GARDENER_WATERER_CHANNEL);
		
		main();
	}
		
		
	public static void main() throws GameActionException {
        // The code you want your robot to perform every round should be in this loop
        while (true) {
        	//System.out.println(role);
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	// Listen for home archon's location
                xPos = rc.readBroadcast(0);
                yPos = rc.readBroadcast(1);                
                archonLoc = new MapLocation(xPos,yPos);
                
                // Check number of all units currently in service
                scoutCount = rc.readBroadcast(SCOUT_CHANNEL);                
                soldierCount = rc.readBroadcast(SOLDIER_CHANNEL);
                lumberjackCount = rc.readBroadcast(LUMBERJACK_CHANNEL);
                tankCount = rc.readBroadcast(TANK_CHANNEL);

                //generate random direction
                dir = Direction.getEast().rotateLeftDegrees(288);
                
                //generate list of trees that are not full health
                lowHealthTrees = TreeSearch.getNearbyLowTrees();
                
                if (lowHealthTrees.size() > 0 || TreeSearch.getNearbyTeamTrees().size() == 0) {
                	role = 0;
                } else {
                	role = 1;
                }
                
                //unit builder
                if (role == 0) {
                	incompleteSurroundTrees(6);
                } else if (role == 1) {
                	buildUnits();
                } else if (role == 2) {
                	completeSurroundTrees(6);
                }
                
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();
            } catch (Exception e) {
                System.out.println("Gardener Exception");
                e.printStackTrace();
            }
        }
	}
	
	
	public static void completeSurroundTrees(float spacing) throws GameActionException {
		lowHealthTrees = TreeSearch.getNearbyLowTrees();
		nearbyGardAndArc = Plant.nearbyGardenersAndArchons(spacing);
		
		if (lowHealthTrees.size() > 0){
            if (rc.canWater(lowHealthTrees.get(0))) {
            	rc.water(lowHealthTrees.get(0));
            }
        } 
		
		//makes sure to stay (spacing) units away from other gardeners and archons before surrounding itself
		while (nearbyGardAndArc && canMove) {
    		Move.tryMove(Move.randomDirection());
    		Clock.yield();
    		nearbyGardAndArc = Plant.nearbyGardenersAndArchons(spacing);
    	}
    	canMove = false; //prevents moving again
        
        // plants trees around itself in 5 directions
        if (rc.canPlantTree(Direction.getEast()) && rc.hasTreeBuildRequirements() && treeCount < 5) {
        	rc.plantTree(Direction.getEast());
            treeCount++;
        } else if (rc.canPlantTree(Direction.getEast().rotateLeftDegrees(72)) && rc.hasTreeBuildRequirements() && treeCount < 5) {
    		rc.plantTree(Direction.getEast().rotateLeftDegrees(72));
            treeCount++;
        } else if (rc.canPlantTree(Direction.getEast().rotateLeftDegrees(144)) && rc.hasTreeBuildRequirements() && treeCount < 5) {
    		rc.plantTree(Direction.getEast().rotateLeftDegrees(144));
            treeCount++;
        } else if (rc.canPlantTree(Direction.getEast().rotateLeftDegrees(216)) && rc.hasTreeBuildRequirements() && treeCount < 5) {
    		rc.plantTree(Direction.getEast().rotateLeftDegrees(216));
            treeCount++;   
        }
		
	}
	
	public static void incompleteSurroundTrees(float spacing) throws GameActionException {
		lowHealthTrees = TreeSearch.getNearbyLowTrees();
		nearbyGardAndArc = Plant.nearbyGardenersAndArchons(spacing);
		
		if (lowHealthTrees.size() > 0){
            if (rc.canWater(lowHealthTrees.get(0))) {
            	rc.water(lowHealthTrees.get(0));
            }
        } 
		
		//makes sure to stay (spacing) units away from other gardeners and archons before surrounding itself
		while (nearbyGardAndArc && canMove) {
    		Move.tryMove(Move.randomDirection());
    		Clock.yield();
    		nearbyGardAndArc = Plant.nearbyGardenersAndArchons(spacing);
    	}
    	canMove = false; //prevents moving again
        
        // plants trees around itself in 4 directions
        if (rc.canPlantTree(Direction.getEast()) && rc.hasTreeBuildRequirements() && treeCount < 4) {
        	rc.plantTree(Direction.getEast());
            treeCount++;
        } else if (rc.canPlantTree(Direction.getEast().rotateLeftDegrees(72)) && rc.hasTreeBuildRequirements() && treeCount < 4) {
    		rc.plantTree(Direction.getEast().rotateLeftDegrees(72));
            treeCount++;
        } else if (rc.canPlantTree(Direction.getEast().rotateLeftDegrees(144)) && rc.hasTreeBuildRequirements() && treeCount < 4) {
    		rc.plantTree(Direction.getEast().rotateLeftDegrees(144));
            treeCount++;
        } else if (rc.canPlantTree(Direction.getEast().rotateLeftDegrees(216)) && rc.hasTreeBuildRequirements() && treeCount < 4) {
    		rc.plantTree(Direction.getEast().rotateLeftDegrees(216));
            treeCount++;   
        }
        
        
	}
	
	public static void buildUnits() throws GameActionException {
		//Move in a random direction
	    //Move.tryMove(Move.randomDirection());
	    System.out.println(soldierCount + " " + lumberjackCount + " " + tankCount);
	    
	    //try to build SCOUT, make sure not over SCOUT_LIMIT
	    if (rc.canBuildRobot(RobotType.SCOUT, dir) && rc.isBuildReady() && scoutCount < SCOUT_LIMIT) {
	        rc.buildRobot(RobotType.SCOUT, dir);
	        rc.broadcast(SCOUT_CHANNEL, scoutCount+1);
	    }
	    //try to build SOLDIER, make sure soldierCount:lumberjackCount < lumberjackRatio
	    if (rc.canBuildRobot(RobotType.SOLDIER, dir) && soldierCount <= LUMBERJACK_RATIO*lumberjackCount  && lumberjackCount >= START_LUMBERJACK_COUNT) {
	        rc.buildRobot(RobotType.SOLDIER, dir);
	        rc.broadcast(SOLDIER_CHANNEL, soldierCount+1);
	    } 
	    //try to build LUMBERJACK, "
	    if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) && rc.isBuildReady() && (LUMBERJACK_RATIO*lumberjackCount < soldierCount || lumberjackCount < START_LUMBERJACK_COUNT)) {
	        rc.buildRobot(RobotType.LUMBERJACK, dir);
	        rc.broadcast(LUMBERJACK_CHANNEL, lumberjackCount+1);
	    }
	    /*
	    if (rc.canBuildRobot(RobotType.TANK, dir) && rc.isBuildReady() && TANK_RATIO*tankCount < soldierCount) {
	        rc.buildRobot(RobotType.TANK, dir);
	        rc.broadcast(TANK_CHANNEL, tankCount+1);
	    } 
	    */
	        
	}
}