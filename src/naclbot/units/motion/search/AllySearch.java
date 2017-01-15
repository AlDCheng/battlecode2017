// This class deals with interactions between units and allied units
package naclbot;
import battlecode.common.*;
import java.util.ArrayList;

public class AllySearch extends GlobalVars {
    // Returns the location of allies that are not archons
    public static MapLocation locFurthestAlly(RobotInfo[] currAllies) {
	float eucDist = 0;
	MapLocation optimumLocation = rc.getLocation();
	for (RobotInfo ally: currAllies) {
	    if (ally.getType() == RobotType.GARDENER || ally.getType() == RobotType.LUMBERJACK || ally.getType() == RobotType.SCOUT) {
		float curDist = rc.getLocation().distanceTo(ally.getLocation());
		if (curDist > eucDist) {
		    optimumLocation = ally.getLocation();
		    eucDist = curDist;
		}
	    }
	}
	return optimumLocation;
    }

    // Returns location and ID of archon
    //public static 
}
