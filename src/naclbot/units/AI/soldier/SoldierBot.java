// AI for soldier under normal control
package naclbot;
import battlecode.common.*;
import java.util.ArrayList;

public class SoldierBot extends GlobalVars {
	public static void entry() throws GameActionException {
		System.out.println("I'm an soldier!");
        Team enemy = rc.getTeam().opponent();

	ArrayList<RobotInfoShoot> enemyToShoot = new ArrayList<RobotInfoShoot>();
	
        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                MapLocation myLocation = rc.getLocation();

		// Sense nearby enemy robots
		RobotInfo[] currentEnemies = rc.senseNearbyRobots(-1, enemy);
		RobotInfo[] currentAllies = rc.senseNearbyRobots(-1, rc.getTeam());

		// Shooting 
		if (!enemyToShoot.isEmpty()) {
		    boolean canShoot = false;
		    Direction shoot = Aim.dirSingleShot(enemyToShoot, currentEnemies);
		    // No shooting if there is an ally in the way
		    for (RobotInfo ally: currentAllies) {
			MapLocation allyLoc = ally.getLocation();
			Direction allyDir = new Direction(myLocation, allyLoc);
			float allyDist = myLocation.distanceTo(allyLoc);
			float allyRadius = ally.getRadius();

			// Implement radius around TO DO
			if (allyDir != shoot && rc.canFireSingleShot()) {
			    rc.fireSingleShot(shoot);
			}	    
		    }
		}
		// Resets the list to add new ones
		enemyToShoot.clear();

		// Adds the ones seen this turn
		for (RobotInfo enemyRobot: currentEnemies) {
		    MapLocation currentLoc = enemyRobot.getLocation();
		    int ID = enemyRobot.getID();
		    enemyToShoot.add(new RobotInfoShoot(ID,currentLoc));
		}

                // Move randomly
                Move.tryMove(Move.randomDirection());

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Soldier Exception");
                e.printStackTrace();
            }
        }
    }
}
