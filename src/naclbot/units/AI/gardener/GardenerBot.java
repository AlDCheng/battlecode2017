// AI for gardener under normal control
package naclbot;
import battlecode.common.*;
import java.util.ArrayList;

public class GardenerBot extends GlobalVars {
	
	public static void entry() throws GameActionException {
		System.out.println("I'm a gardener!");
		// determines whether gardener is planter and waterer or unit builder 
		// Hello
		int role;
		int treeCount = 0;
		double randNum = Math.random();
		
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
			System.out.println("Planter/Waterer; rand: " + randNum);
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
            
                MapLocation archonLoc = new MapLocation(xPos,yPos);
                
                Direction dir = Move.randomDirection();
                
                //unit builder
                if (role == 0) {
	                //Move in a random direction
	                Move.tryMove(Move.randomDirection());
	                
	                // Randomly attempt to build a soldier or lumberjack or plant a tree in this direction
	                if (rc.canBuildRobot(RobotType.SOLDIER, dir) && Math.random() < .01) {
	                    rc.buildRobot(RobotType.SOLDIER, dir);
	                } else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) && Math.random() < .01 && rc.isBuildReady()) {
	                    rc.buildRobot(RobotType.LUMBERJACK, dir);
	                } else if (rc.canBuildRobot(RobotType.TANK, dir) && Math.random() < .01 && rc.isBuildReady()) {
	                    rc.buildRobot(RobotType.TANK, dir);    
	                } else if (rc.canBuildRobot(RobotType.SCOUT, dir) && Math.random() < .01 && rc.isBuildReady() && canBuildScout(scoutCount)) {
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
	                MapLocation nearestLowTree;
	                float distanceNearestTree;
	                if (nearbyTrees.size() > 0) {
	                	distanceNearestTree = rc.getLocation().distanceTo(TreeSearch.locNearestTree(nearbyTrees));
	                } else {
	                	distanceNearestTree = 100;
	                }
	                
	                if (lowHealthTrees.size() > 0){
	                    nearestLowTree = TreeSearch.locNearestTree(lowHealthTrees);
	                    dir = rc.getLocation().directionTo(nearestLowTree);
		                
		                //try to water a tree
		                if (!rc.canWater(nearestLowTree)) {
		                	Move.tryMove(dir);
		                } else {
		                	rc.water(nearestLowTree);
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