// This class deals with interactions between unit and neighboring trees
package naclbot;
import battlecode.common.*;
import java.util.ArrayList;

public class Aim extends GlobalVars {
    
    // Returns the direction of the optimum single shot
    public static Direction dirSingleShot(ArrayList<RobotInfoShoot> pastEnemies, RobotInfo[] currentEnemies) {
	MapLocation loc = rc.getLocation();
	boolean foundEnemy = false; 
	for (RobotInfo robot: currentEnemies) {
	    int robotID = robot.getID();
	    for (int x=0; x<pastEnemies.size(); x++) {
		// If robot detected was also detected last turn...
		if (pastEnemies.get(x).getID() == robotID) {
		    MapLocation newLoc = robot.getLocation();
		    MapLocation oldLoc = pastEnemies.get(x).getLocation();
		    Direction enemyDir = new Direction(oldLoc, newLoc);
		    float distTravelled = oldLoc.distanceTo(newLoc);
		    // Predict the new location with info 
		    MapLocation predLoc = newLoc.add(enemyDir,distTravelled);

		    if (Math.random() > 0.4) {
			// More likely they will keep moving in the direction they were moving in
			Direction shootDir = new Direction(loc,predLoc);
			return shootDir;
	  
		    } else {
			// Otherwise they are trying to dodge, right now predict they move back to their original position
			Direction shootDir = new Direction(loc,oldLoc);
			return shootDir;
		    }
		}			
	    }
	}
	MapLocation someLoc = pastEnemies.get(0).getLocation();
	Direction shootDir = new Direction(loc,someLoc);
	return shootDir;
    }
}
