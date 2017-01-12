// AI for Archon
package naclbot;
import battlecode.common.*;

public class ArchonBot extends GlobalVars {
	
	// Starting game phase
	public static void entry() throws GameActionException {
		System.out.println("Archon initialized!");

        // Starting phase loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	// Check for condition to exit Starting Phase
            	if(rc.getRoundNum() > 100) {
            		break;
            	}

                // Generate a random direction
                Direction dir = Move.randomDirection();

                // Spam gardeners at random positions if possible
                if (rc.canHireGardener(dir)) {
                    rc.hireGardener(dir);
                }

                // Move randomly
                Move.tryMove(Move.randomDirection());

                // Broadcast archon's location for other robots on the team to know
                MapLocation myLocation = rc.getLocation();
                rc.broadcast(0,(int)myLocation.x);
                rc.broadcast(1,(int)myLocation.y);

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Archon Exception in Starting Phase");
                e.printStackTrace();
            }
        }
    }
	
	static void mainPhase() throws GameActionException {
		System.out.println("Archon transitioning to Main Phase");
		
		// loop for Main Phase
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

                // Generate a random direction
                Direction dir = Move.randomDirection();

                // Randomly attempt to build a gardener in this direction
                if (rc.canHireGardener(dir) && Math.random() < .01) {
                    rc.hireGardener(dir);
                }

                // Move randomly
                Move.tryMove(Move.randomDirection());

                // Broadcast archon's location for other robots on the team to know
                MapLocation myLocation = rc.getLocation();
                rc.broadcast(0,(int)myLocation.x);
                rc.broadcast(1,(int)myLocation.y);

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Archon Exception in Main Phase");
                e.printStackTrace();
            }
        }
		
	}
}