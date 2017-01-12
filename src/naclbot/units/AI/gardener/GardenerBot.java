// AI for gardener under normal control
package naclbot;
import battlecode.common.*;

public class GardenerBot extends GlobalVars {
	
	public static void entry() throws GameActionException {
		System.out.println("I'm a gardener!");

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

                // First see if there is a tree nearby and if you can do anything to it
                System.out.println(TreeSearch.countNearbyTrees());

                // Generate a random direction
                Direction dir = Move.randomDirection();

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
                } else if (rc.canPlantTree(dir) && rc.hasTreeBuildRequirements() &&  Math.random() < .01) {
                    rc.plantTree(dir);
                }

                // Move randomly
                Move.tryMove(Move.randomDirection());

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