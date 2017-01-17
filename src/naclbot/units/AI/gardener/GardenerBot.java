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
		if ((prevNumWaterer > 2) && ((2*prevNumBuilder) < prevNumWaterer)) {
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
	                if (rc.canBuildRobot(RobotType.SOLDIER, dir) && soldierCount <= lumberjackCount) {
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
	            //planter,waterer    
	            else if (role == 1) {
	                // First see if there is a tree nearby and if you can do anything to it
	            	ArrayList<MapLocation> lowHealthTrees = TreeSearch.getNearbyLowTrees();
	                ArrayList<MapLocation> nearbyTrees = TreeSearch.getNearbyTrees();
	                Direction dirToNearestLow;
	                float distanceNearestTree;
	                if (nearbyTrees.size() > 0) {
	                	distanceNearestTree = rc.getLocation().distanceTo(TreeSearch.locNearestTree(nearbyTrees));
	                } else {
	                	distanceNearestTree = 100;
	                }
	                
	                if (lowHealthTrees.size() > 0){
	                    dirToNearestLow = rc.getLocation().directionTo(lowHealthTrees.get(0));
		                
		            //try to water a tree
		                if (!rc.canWater(lowHealthTrees.get(0))) {
		                	Move.tryMove(dirToNearestLow);
		                } else {
		                	rc.water(lowHealthTrees.get(0));
		                }
	                } else if (rc.canPlantTree(dir) && rc.hasTreeBuildRequirements() && treeCount < 3 && distanceNearestTree > 3.0) {
		                rc.plantTree(dir);
		                treeCount++;
	                } else {
	                	Move.tryMove(Move.randomDirection());
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