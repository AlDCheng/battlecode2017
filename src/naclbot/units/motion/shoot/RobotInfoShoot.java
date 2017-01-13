// This class deals with interactions between unit and neighboring trees
package naclbot;
import battlecode.common.*;

public class RobotInfoShoot extends GlobalVars {
    int robotID;
    MapLocation robotLoc;
    
    public RobotInfoShoot(int ID, MapLocation location) {
        this.robotID = ID;
        this.robotLoc  = location;
    }

    public int getID() {
	return robotID;
    }

    public MapLocation getLocation() {
	return robotLoc;
    }
}
