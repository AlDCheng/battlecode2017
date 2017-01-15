// AI for lumberjack
package naclbot;
import battlecode.common.*;
import java.util.ArrayList;

public class LumberjackBot extends GlobalVars {
	public static void entry() throws GameActionException {
		System.out.println("I'm a lumberjack!");
        Team enemy = rc.getTeam().opponent();

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

                // See if there are any enemy robots within striking range (distance 1 from lumberjack's radius)
                RobotInfo[] robots = rc.senseNearbyRobots(RobotType.LUMBERJACK.bodyRadius+GameConstants.LUMBERJACK_STRIKE_RADIUS, enemy);
                if(robots.length > 0 && !rc.hasAttacked()) {
                    // Use strike() to hit all nearby robots!
                    rc.strike();
                } else {
                    // No close robots, so search for robots within sight radius
                    robots = rc.senseNearbyRobots(-1,enemy);
                    ArrayList<MapLocation> nearbyBulletTrees = TreeSearch.getNearbyBulletTrees();

                    if (robots.length > 0) { // If there is a robot, move towards it
                        MapLocation myLocation = rc.getLocation();
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
                    } else {
                        // Move Randomly
                    	Move.tryMove(Move.randomDirection());
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
}