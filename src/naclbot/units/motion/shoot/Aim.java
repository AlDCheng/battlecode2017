// This class deals with interactions between unit and neighboring trees
package naclbot;
import battlecode.common.*;
import java.util.ArrayList;
import java.util.Random;

public class Aim extends GlobalVars {
    
    // Returns the direction of the optimum single shot
    public static ShootingType toShoot (ArrayList<RobotInfoShoot> pastEnemies, RobotInfo[] currentEnemies) {
	Direction noDir = new Direction(0);
	MapLocation loc = rc.getLocation();
	boolean isArchon = false;
	//RobotInfoShoot archon;
	ArrayList<RobotInfoShoot> repeatedEnemies = new ArrayList<RobotInfoShoot>();

	// Looks over all robots and checks which are repeated
	for (RobotInfo robot: currentEnemies) {
	    int robotID = robot.getID();
	    for (int x=0; x<pastEnemies.size(); x++) {
		// If robot detected was also detected last turn...
		if (pastEnemies.get(x).getID() == robotID) {
		    MapLocation newLoc = robot.getLocation();
		    MapLocation oldLoc = pastEnemies.get(x).getCurrentLocation();
		    RobotType robType = robot.getType();
		    RobotInfoShoot robInfo = new RobotInfoShoot(robotID,robType,newLoc,oldLoc);
		    repeatedEnemies.add(robInfo);
		    
		    // Check if one of the ones to attack is an archon
		    if (robot.getType() == RobotType.ARCHON && isArchon == false) {
			isArchon = true;
		        RobotInfoShoot archon = new RobotInfoShoot(robotID,robType,newLoc,oldLoc);
			Direction dir = archon.getDirectionToShoot(loc);
			if (rc.canFirePentadShot()) {
			    ShootingType result = new ShootingType("pentad", isArchon, dir);
			    return result;
			} else if (rc.canFireTriadShot()) {
			    ShootingType result = new ShootingType("triad", isArchon, dir);
			    return result;
			} else if (rc.canFireSingleShot()) {
			    ShootingType result = new ShootingType("single", isArchon, dir);
			    return result;
			} else {
			    ShootingType result = new ShootingType("none", isArchon, dir);
			    return result;
			}
		    }
		}			
	    }
	}

	// Change this to be smarter
	if (repeatedEnemies.size() == 0) {
	    // If don't know where to shoot, shoot at random location
	    if (pastEnemies.size() > 0 && rc.canFireSingleShot()) {
		MapLocation someLoc = pastEnemies.get(0).getCurrentLocation();
		Direction shootDir = new Direction(loc,someLoc);
		ShootingType result = new ShootingType("single", isArchon, shootDir);
		return result;
	    } else {
		ShootingType result = new ShootingType("none", isArchon, noDir);
		return result;
	    }
	} else if (repeatedEnemies.size() == 1) {
	    if (rc.canFireSingleShot()) {
		Direction shootDir = repeatedEnemies.get(0).getDirectionToShoot(loc);
		ShootingType result = new ShootingType("single", isArchon, shootDir);
		return result;
	    }
	} else if (repeatedEnemies.size() < 4) {
	    int index = new Random().nextInt(repeatedEnemies.size()); 
	    if (rc.canFireTriadShot()) {
		Direction shootDir = repeatedEnemies.get(index).getDirectionToShoot(loc);
		ShootingType result = new ShootingType("single", isArchon, shootDir);
		return result;
	    } else if (rc.canFireSingleShot()) {
		Direction shootDir = repeatedEnemies.get(index).getDirectionToShoot(loc);
		ShootingType result = new ShootingType("single", isArchon, shootDir);
		return result;
	    }
	} else if (repeatedEnemies.size() > 4) {
	    int index = new Random().nextInt(repeatedEnemies.size());
	    if (rc.canFirePentadShot()) {
		Direction shootDir = repeatedEnemies.get(index).getDirectionToShoot(loc);
		ShootingType result = new ShootingType("pentad", isArchon, shootDir);
		return result;
	    } else if (rc.canFireTriadShot()) {
		Direction shootDir = repeatedEnemies.get(index).getDirectionToShoot(loc);
		ShootingType result = new ShootingType("triad", isArchon, shootDir);
		return result;
	    } else if (rc.canFireSingleShot()) {
		Direction shootDir = repeatedEnemies.get(index).getDirectionToShoot(loc);
		ShootingType result = new ShootingType("single", isArchon, shootDir);
		return result;
	    }
	}
	ShootingType result = new ShootingType("none", isArchon, noDir);
	return result;
    }
}
