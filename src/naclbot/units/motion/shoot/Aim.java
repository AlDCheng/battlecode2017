// This class deals with interactions between unit and neighboring trees
package naclbot;
import battlecode.common.*;

public class Aim extends GlobalVars {
    
    // Returns the direction of the optimum single shot
    public static Direction dirSingleShoot(RobotInfo[] nearbyRobots) {
        Direction[] robDirections = new Direction[nearbyRobots.length];
	int i = 0;
	for (RobotInfo robot: nearbyRobots) {
	    MapLocation locRobot = robot.getLocation();
	    Direction dirRobot = new Direction(rc.getLocation(),locRobot);
	    robDirections[i] = dirRobot;
	}
	return robDirections[0];
    }
}
