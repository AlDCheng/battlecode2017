// AI for gardener under normal control
package naclbot;
import battlecode.common.*;
import java.util.ArrayList;

public class GardenerBot extends GlobalVars {
	
	public static void entry() throws GameActionException {
		System.out.println("I'm a gardener!");
		//determines whether gardener is planter and waterer or unit builder 
		int role;
		if (Math.random() < 0.5) {
			role = 0; //unit builder
		} else {
			role = 1; //planter and waterer
		}
		
		
        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

                // Listen for home archon's location
                int xPos = rc.readBroadcast(0);
                int yPos = rc.readBroadcast(1);                
   
                // Check number of scouts currently in service
                int scoutCount = rc.readBroadcast(SCOUT_CHANNEL);
                
            
                MapLocation archonLoc = new MapLocation(xPos,yPos);
                
                //unit builder
                if (role == 0) {
	                //Move in a random direction
	                Move.tryMove(Move.randomDirection());
	                
	                // Randomly attempt to build a soldier or lumberjack or plant a tree in this direction
	                if (rc.canBuildRobot(RobotType.SOLDIER, dir) && Math.random() < .01) {
	                    rc.buildRobot(RobotType.SOLDIER, dir);
	                } else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) && Math.random() < .01 && rc.isBuildReady()) {
	                    rc.buildRobot(RobotType.LUMBERJACK, dir);
	                    
	                    /* Check to build scout
	                     * Must assert that there are not too many scouts in service at this moment in time
	                     */
	                } else if (rc.canBuildRobot(RobotType.SCOUT, dir) && Math.random() < .01 && rc.isBuildReady() && canBuildScout(scoutCount)) {
	                    rc.buildRobot(RobotType.SCOUT, dir);
	                } 
	            //planter,waterer    
	            else if (role == 1) {
	            	//try to plant a tree
	                if (rc.canPlantTree(dir) && rc.hasTreeBuildRequirements() &&  Math.random() < .01) {
	                    rc.plantTree(dir);
	                
	                // First see if there is a tree nearby and if you can do anything to it
	                ArrayList<MapLocation> lowHealthTrees = TreeSearch.getNearbyLowTrees();
	                MapLocation nearestLowTree;
	                if (lowHealthTrees.size() > 0){
	                    nearestLowTree = TreeSearch.locNearestTree(lowHealthTrees);
	                    dir = rc.getLocation().directionTo(nearestLowTree);
	                } 
	                
	                // Move toward trees that need to be watered
	                Move.tryMove(dir);
	                
	                //try to water a tree
	                if (rc.canWater(nearestLowTree)) {
	                	rc.water(nearestLowTree);
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