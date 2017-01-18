// This class deals with interactions between unit and neighboring trees
package naclbot.units.motion.shoot;
import battlecode.common.*;
import naclbot.variables.GlobalVars;

import java.util.ArrayList;
import java.util.Random;

public class Aim extends GlobalVars {

    // Returns the direction of the nearest enemy single shot
    public static ShootingType shootNearestEnemy (ArrayList<RobotInfoShoot> pastEnemies, RobotInfo[] currentEnemies, boolean isTank) {
		MapLocation myLoc = rc.getLocation();
		RobotInfoShoot nearestEnemy = null;
		float nearestEnemyDistance = -1;
		int nearestEnemies = 0;
		String typeBullet;
	
		// Look over all robots and find nearest enemy that has already been seen
		for (RobotInfo robot: currentEnemies) {
		    int robotID = robot.getID();
		    for (int x=0; x<pastEnemies.size(); x++) {
		    	
				// If robot detected was also detected last turn ...
				if (pastEnemies.get(x).getID() == robotID) {
				    nearestEnemies += 1;
				    MapLocation newLoc = robot.getLocation();
				    MapLocation oldLoc = pastEnemies.get(x).getCurrentLocation();
				    RobotType robType = robot.getType();
				    
				    // Hasn't found one yet so set it
				    if (nearestEnemyDistance == -1) {
						// Tanks only shoot at soldiers, archons or tanks
						if (isTank && (robType != RobotType.SOLDIER || robType != RobotType.ARCHON || robType != RobotType.TANK)) {
						    continue;
						} else {
						    RobotInfoShoot newNearestEnemy = new RobotInfoShoot(robotID,robType,newLoc,oldLoc);
						    nearestEnemy = newNearestEnemy;
						    nearestEnemyDistance = myLoc.distanceTo(newLoc);
						} 
				    } else {
						if (isTank && (robType != RobotType.SOLDIER || robType != RobotType.ARCHON || robType != RobotType.TANK)) {
						    continue;
						} else {
						    // If the distance is less than prev distance then update the nearest enemy
						    if (myLoc.distanceTo(newLoc) < nearestEnemyDistance) {
								RobotInfoShoot newNearestEnemy = new RobotInfoShoot(robotID,robType,newLoc,oldLoc);
								nearestEnemy = newNearestEnemy;
								nearestEnemyDistance = myLoc.distanceTo(newLoc);
						    }
						}
				    }
				}
		    }
		}

		// If no enemies are found then return null
		if (nearestEnemies == 0) {
		    return null;
		} else {
		    Direction dirShoot = nearestEnemy.getDirectionToShoot(myLoc);
		   // If big units like archon or tank then try to fire as many bullets as possible
		   // if (nearestEnemy.getType() == RobotType.TANK || nearestEnemy.getType() == RobotType.ARCHON || nearestEnemy.getType() == RobotType.LUMBERJACK || nearestEnemy.getType() == RobotType.SOLDIER) {
		    	if (true) {
				if (rc.canFirePentadShot()) {
				    ShootingType enemy = new ShootingType("pentad",nearestEnemy.getType(),dirShoot,nearestEnemyDistance);
				    return enemy;
				} else if (rc.canFireTriadShot()) {
				    ShootingType enemy = new ShootingType("triad",nearestEnemy.getType(),dirShoot,nearestEnemyDistance);
				    return enemy;
				} else if (rc.canFireSingleShot()) {
				    ShootingType enemy = new ShootingType ("single",nearestEnemy.getType(),dirShoot,nearestEnemyDistance);
				    return enemy;
				} else {
				    return null;
				}
					
		    } else {
			// If other type of unit then just fire single shot
		    	if (rc.canFireSingleShot()) {
				    ShootingType enemy = new ShootingType("single",nearestEnemy.getType(),dirShoot,nearestEnemyDistance);
				    return enemy;
				} else {
				    return null;
				}
		    }
		}
    }


    /*
    // Returns the direction of the optimum single shot
    public static ShootingType shootEnemies (ArrayList<RobotInfoShoot> pastEnemies, RobotInfo[] currentEnemies) {
	Direction noDir = new Direction(0);
	MapLocation myLoc = rc.getLocation();
	boolean isArchon = false;
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
		    
		    // Prioritizes shooting archon
		    if (robot.getType() == RobotType.ARCHON && isArchon == false && !rc.hasAttacked()) {
			isArchon = true;
		        RobotInfoShoot archon = new RobotInfoShoot(robotID,robType,newLoc,oldLoc);
			Direction dir = archon.getDirectionToShoot(myLoc);
			if (rc.canFirePentadShot()) {
			    ShootingType result = new ShootingType("pentad", robType, dir);
			    return result;
			} else if (rc.canFireTriadShot()) {
			    ShootingType result = new ShootingType("triad", robType, dir);
			    return result;
			} else if (rc.canFireSingleShot()) {
			    ShootingType result = new ShootingType("single", robType, dir);
			    return result;
			} 
			ShootingType result = new ShootingType("none", robType, dir);
			return result;
			
		    }
		}			
	    }
	}

	if (repeatedEnemies.size() == 0) {
	    // If don't know where to shoot, shoot at random location
	    if (pastEnemies.size() > 0 && rc.canFireSingleShot()) {
		MapLocation someLoc = pastEnemies.get(0).getCurrentLocation();
		Direction shootDir = new Direction(myLoc,someLoc);
		ShootingType result = new ShootingType("single", pastEnemies.get(0).getType(), shootDir);
		return result;
	    } else {
		return null;
	    }
	} else if (repeatedEnemies.size() == 1) {
	    if (rc.canFireSingleShot()) {
		Direction shootDir = repeatedEnemies.get(0).getDirectionToShoot(myLoc);
		ShootingType result = new ShootingType("single", repeatedEnemies.get(0).getType(), shootDir);
		return result;
	    }
	} else if (repeatedEnemies.size() < 6) {
	    int index = new Random().nextInt(repeatedEnemies.size()); 
	    if (rc.canFireTriadShot()) {
		Direction shootDir = repeatedEnemies.get(index).getDirectionToShoot(myLoc);
		ShootingType result = new ShootingType("single", repeatedEnemies.get(index).getType(), shootDir);
		return result;
	    } else if (rc.canFireSingleShot()) {
		Direction shootDir = repeatedEnemies.get(index).getDirectionToShoot(myLoc);
		ShootingType result = new ShootingType("single", repeatedEnemies.get(index).getType(), shootDir);
		return result;
	    }
	} else if (repeatedEnemies.size() >= 6) {
	    int index = new Random().nextInt(repeatedEnemies.size());
	    if (rc.canFirePentadShot()) {
		Direction shootDir = repeatedEnemies.get(index).getDirectionToShoot(myLoc);
		ShootingType result = new ShootingType("pentad", repeatedEnemies.get(index).getType(), shootDir);
		return result;
	    } else if (rc.canFireTriadShot()) {
		Direction shootDir = repeatedEnemies.get(index).getDirectionToShoot(myLoc);
		ShootingType result = new ShootingType("triad", repeatedEnemies.get(index).getType(), shootDir);
		return result;
	    } else if (rc.canFireSingleShot()) {
		Direction shootDir = repeatedEnemies.get(index).getDirectionToShoot(myLoc);
		ShootingType result = new ShootingType("single", repeatedEnemies.get(index).getType(), shootDir);
		return result;
	    }
	}
	return null;
    }
    */
}
