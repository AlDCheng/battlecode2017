// AI for gardener under normal control
package naclbot.units.AI.gardener;
import battlecode.common.*;
import naclbot.units.motion.Move;
import naclbot.units.motion.search.TreeSearch;
import naclbot.variables.GlobalVars;

import java.util.ArrayList;

public class GardenerBot extends GlobalVars {
	
	public static void entry() throws GameActionException {
		System.out.println("I'm a gardener!");
		// determines whether gardener is planter and waterer or unit builder 
		// Hello
		int role;
		int treeCount = 0;

		// AC: Quick hotfix to have deterministic selection. Should update code to read from broadcast intelligently
		int numGard = rc.readBroadcast(GARDENER_CHANNEL);
		int prevNumBuilder = rc.readBroadcast(GARDENER_BUILDER_CHANNEL);
		int prevNumWaterer = rc.readBroadcast(GARDENER_WATERER_CHANNEL);
		
		System.out.println("Builders: " + prevNumBuilder + ", Waterers: " + prevNumWaterer);
		// This code is stupid for now, but creates unit builders every other gardener after at least 4 planters are built.
		if ((prevNumWaterer > 3) && ((2*prevNumBuilder) < prevNumWaterer)) {
			rc.broadcast(GARDENER_BUILDER_CHANNEL, prevNumBuilder + 1);
			role = 0; //unit builder
		} else {
			int remISTHEBESTGIRLNum = 0;
			System.out.println("Planter/Waterer; rand: " + remISTHEBESTGIRLNum);
			rc.broadcast(GARDENER_WATERER_CHANNEL, prevNumWaterer + 1);
			role = 1; //planter and waterer
		}
		
		
        // The code you want your robot to perform every round should be in this loop
        while (true) {
        	//System.out.println(role);
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	// Listen for home archon's location
                int xPos = rc.readBroadcast(0);
                int yPos = rc.readBroadcast(1);                
   
                // Check number of scouts currently in service
                int scoutCount = rc.readBroadcast(SCOUT_CHANNEL);                
                int soldierCount = rc.readBroadcast(SOLDIER_CHANNEL);
                int lumberjackCount = rc.readBroadcast(LUMBERJACK_CHANNEL);
                int tankCount = rc.readBroadcast(TANK_CHANNEL);
                
                MapLocation archonLoc = new MapLocation(xPos,yPos);
                
                Direction dir = Move.randomDirection();
                //unit builder
                if (role == 0) {
	                //Move in a random direction
	                Move.tryMove(Move.randomDirection());
	                
	                // Randomly attempt to build a soldier or lumberjack or plant a tree in this direction
	                if (rc.canBuildRobot(RobotType.SOLDIER, dir) && soldierCount <= 3*lumberjackCount) {
	                    rc.buildRobot(RobotType.SOLDIER, dir);
	                    rc.broadcast(SOLDIER_CHANNEL, soldierCount+1);
	                } else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) && rc.isBuildReady() && lumberjackCount < soldierCount) {
	                    rc.buildRobot(RobotType.LUMBERJACK, dir);
	                    rc.broadcast(LUMBERJACK_CHANNEL, lumberjackCount+1);
	                } else if (rc.canBuildRobot(RobotType.TANK, dir) && rc.isBuildReady() && tankCount*10 < soldierCount) {
	                    rc.buildRobot(RobotType.TANK, dir);
	                    rc.broadcast(TANK_CHANNEL, tankCount+1);
	                } else if (rc.canBuildRobot(RobotType.SCOUT, dir) && rc.isBuildReady() && canBuildScout(scoutCount)) {
	                	/* Check to build scout
	                     * Must assert that there are not too many scouts in service at this moment in time
	                     */
	                    rc.buildRobot(RobotType.SCOUT, dir);
	                }
            	} 
	            //waterer    
	            else if (role == 1) {
	                // First see if there is a tree nearby and if you can do anything to it
	            	ArrayList<MapLocation> lowHealthTrees = TreeSearch.getNearbyLowTrees();
	            	boolean nearbyGard = Plant.nearbyGardeners(5);
	            	boolean nearbyArc = Plant.nearbyArchons(5);
	                if (lowHealthTrees.size() > 0){
	                    if (rc.canWater(lowHealthTrees.get(0))) {
	                    	rc.water(lowHealthTrees.get(0));
	                    }
 
	                } 
	                if (rc.canPlantTree(Direction.getEast()) && rc.hasTreeBuildRequirements() && treeCount < 5 && !nearbyGard) {
	                	rc.plantTree(Direction.getEast());
			            treeCount++;
	                } else if (rc.canPlantTree(Direction.getNorth()) && rc.hasTreeBuildRequirements() && treeCount < 5 && !nearbyGard) {
                		rc.plantTree(Direction.getNorth());
		                treeCount++;
	                } else if (rc.canPlantTree(Direction.getSouth()) && rc.hasTreeBuildRequirements() && treeCount < 5 && !nearbyGard) {
                		rc.plantTree(Direction.getSouth());
		                treeCount++;
	                } else if (rc.canPlantTree(Direction.getWest()) && rc.hasTreeBuildRequirements() && treeCount < 5 && !nearbyGard) {
                		rc.plantTree(Direction.getWest());
		                treeCount++;   
	                }

	            }    	
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();
            } catch (Exception e) {
                System.out.println("Gardener Exception");
                e.printStackTrace();
            }
        }
	}
	
	public static boolean canBuildScout(int count) {
	    if (count < SCOUT_LIMIT){
	    	return true;	        
        }
	    return false;
	}
}