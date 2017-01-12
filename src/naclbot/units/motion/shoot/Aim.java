// This class deals with interactions between unit and neighboring trees
package naclbot;
import battlecode.common.*;

public class Aim extends GlobalVars {
    
    // Returns the direction of the optimum single shot
    public static Direction dirSingleShoot(RobotInfo[] nearbyRobots, MapLocation myRobot) {
	// Direction[] robDirections 
	for (RobotInfo robot: nearbyRobots) {
	    MapLocation locRobot = robot.getLocation();
	    Direction dirRobot = new Direction(myRobot,locRobot);
	}
	return dirRobot;
    }
}
