// This class makes a new data type for robots that is not RobotInfo
package naclbot.units.motion.shoot;
import battlecode.common.*;
import naclbot.variables.GlobalVars;

public class RobotInfoShoot extends GlobalVars {
    int robotID;
    RobotType robotType;
    MapLocation robotCurrLoc;
    MapLocation robotPrevLoc;
    MapLocation robotNewLoc;
    
    public RobotInfoShoot(int ID, RobotType robType, MapLocation location) {
        this.robotID = ID;
        this.robotType = robType;
        this.robotCurrLoc = location;
        this.robotPrevLoc = location;
    }

    public RobotInfoShoot(int ID, RobotType robType, MapLocation currentLoc, MapLocation previousLoc) {
		this.robotID = ID;
		this.robotType = robType;
		this.robotCurrLoc = currentLoc;
		this.robotPrevLoc = previousLoc;
    }

    public RobotType getType() {
    	return robotType;
    }

    public int getID() {
    	return robotID;
    }

    public MapLocation getCurrentLocation() {
    	return robotCurrLoc;
    }

    public MapLocation getPreviousLocation() {
    	return robotPrevLoc;
    }

    public Direction getRobotDirection() {
		Direction dir = new Direction(robotPrevLoc,robotCurrLoc);
		return dir;
    }

    public float getDistTravelled() {
    	return robotPrevLoc.distanceTo(robotCurrLoc);
    }

    // This function returns the predicted location if robot keeps moving in same direction
    public MapLocation getPredictedLocation() {
		Direction direction = getRobotDirection();
		float distance = getDistTravelled();
		return robotCurrLoc.add(direction,distance);
    }

    // This function returns the location after probabilistically predicting dodging
    public MapLocation getNewLocation() {
		//MapLocation predLoc = getPreviousLocation();
    	MapLocation predLoc = getCurrentLocation(); //CHANGE
		return predLoc;
		
		/*
		if (Math.random() > 0.4) {
		    MapLocation predLoc = getPredictedLocation();
		    return predLoc;
		} else {
		    // MAKE THIS BETTER PLZ
		    MapLocation predLoc = getPreviousLocation();
		    return predLoc;
		}
		*/
	    }
	
	    public Direction getDirectionToShoot(MapLocation myLoc) {
		MapLocation newLoc = this.getNewLocation();
		Direction dir = new Direction(myLoc, newLoc);
		return dir;
    }
}
