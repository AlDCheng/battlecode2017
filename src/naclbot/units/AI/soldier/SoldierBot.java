// AI for soldier under normal control
package naclbot;
import battlecode.common.*;

public class SoldierBot extends GlobalVars {
	public static void entry() throws GameActionException {
		System.out.println("I'm an soldier!");
        Team enemy = rc.getTeam().opponent();

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                MapLocation myLocation = rc.getLocation();

                // See if there are any nearby enemy robots
                RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

		// If there are some...
		if (robots.length > 6) {
		     // And we have enough bullets, and haven't attacked yet this turn...
		    if (rc.canFirePentadShot()) {
			// ...Then fire a bullet in the direction of one of the enemies.
			rc.firePentadShot(rc.getLocation().directionTo(robots[0].location));
		    } else if (rc.canFireTriadShot()) {
			// ...Then fire a bullet in the direction of one of the enemies.
			rc.fireTriadShot(rc.getLocation().directionTo(robots[0].location));
		    } else if (rc.canFireSingleShot()) {
			// ...Then fire a bullet in the direction of one of the enemies.
			rc.fireSingleShot(rc.getLocation().directionTo(robots[0].location));
		    }
		} else if (robots.length > 3 && robots.length < 6) {
		    // And we have enough bullets, and haven't attacked yet this turn...
		    if (rc.canFireTriadShot()) {
			// ...Then fire a bullet in the direction of one of the enemies.
			rc.fireTriadShot(rc.getLocation().directionTo(robots[0].location));
		    } else if (rc.canFireSingleShot()) {
			// ...Then fire a bullet in the direction of one of the enemies.
			rc.fireSingleShot(rc.getLocation().directionTo(robots[0].location));
		    }
		} else if (robots.length > 0 && robots.length < 3) {
                    // And we have enough bullets, and haven't attacked yet this turn...
                    if (rc.canFireSingleShot()) {
                        // ...Then fire a bullet in the direction of one of the enemies.
                        rc.fireSingleShot(rc.getLocation().directionTo(robots[0].location));
                    }
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
