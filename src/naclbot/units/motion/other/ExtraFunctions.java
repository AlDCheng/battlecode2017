// This class has extra functions
package naclbot.units.motion.other;
import battlecode.common.*;
import naclbot.variables.GlobalVars;
import naclbot.units.motion.shoot.RobotInfoShoot;

import java.util.ArrayList;

public class ExtraFunctions extends GlobalVars {
    public static RobotInfoShoot findNearestEnemyShooting (ArrayList<RobotInfoShoot> pEnemies, RobotInfo[] cEnemies, boolean tank) {
	MapLocation myLoc = rc.getLocation();
	RobotInfoShoot nearestEnemy = null;
	float nearestEnemyDistance = -1;
	int nearestEnemies = 0;

	// Look over all robots and find nearest enemy that has already been seen
	for (RobotInfo robot: cEnemies) {
	    int robotID = robot.getID();
	    for (int x=0; x<pEnemies.size(); x++) {
		
		// If robot detected was also detected last turn ...
		if (pEnemies.get(x).getID() == robotID) {
		    System.out.println("FOUND SEEN UNIT");
		    nearestEnemies += 1;
		    MapLocation newLoc = robot.getLocation();
		    MapLocation oldLoc = pEnemies.get(x).getCurrentLocation();
		    RobotType robType = robot.getType();
		    
		    // Hasn't found one yet so set it
		    if (nearestEnemyDistance == -1) {
			// Tanks only shoot at soldiers, archons or tanks
			if (tank && (robType != RobotType.SOLDIER || robType != RobotType.ARCHON || robType != RobotType.TANK)) {
			    continue;
			} else {
			    nearestEnemyDistance = myLoc.distanceTo(newLoc);
			    RobotInfoShoot newNearestEnemy = new RobotInfoShoot(robotID,robType,newLoc,oldLoc,nearestEnemyDistance,nearestEnemies);
			    nearestEnemy = newNearestEnemy;
			    
			} 
		    } else {
			if (tank && (robType != RobotType.SOLDIER || robType != RobotType.ARCHON || robType != RobotType.TANK)) {
			    continue;
			} else {
			    // If the distance is less than prev distance then update the nearest enemy
			    if (myLoc.distanceTo(newLoc) < nearestEnemyDistance) {
				nearestEnemyDistance = myLoc.distanceTo(newLoc);
				RobotInfoShoot newNearestEnemy = new RobotInfoShoot(robotID,robType,newLoc,oldLoc,nearestEnemyDistance,nearestEnemies);
				nearestEnemy = newNearestEnemy;
			    }
			}
		    }
		}
	    }
	}
	return nearestEnemy;
    }

    public static findNearestEnemyLumberjack(RobotInfo[] enemies) {
	MapLocation myLoc = rc.getLocation();
	RobotInfoShoot nearestEnemy = null;
	float nearestEnemyDistance = -1;
	int nearestEnemies = 0;

	for (RobotInfo enemy: enemies) {
	    nearestEnemies += 1;
	    MapLocation enemyLoc = enemy.getLocation();

	    // Hasn't found one yet so assign
	    if (nearestEnemyDistance == -1) {
		nearestEnemyDistance = myLoc.distanceTo(enemyLoc);
		RobotInfoShoot newNearestEnemy = new RobotInfoShoot(enemy.getID(),enemy.getType(),enemyLoc,enemyLoc,nearestEnemyDistance,nearestEnemies);
		nearestEnemy = newNearestEnemy;
	    } else {
		if (myLoc.distanceTo(enemyLoc) < nearestEnemyDistance) {
		    nearestEnemyDistance = myLoc.distanceTo(enemyLoc);
		    RobotInfoShoot newNearestEnemy = new RobotInfoShoot(enemy.getID(),enemy.getType(),enemyLoc,enemyLoc,nearestEnemyDistance,nearestEnemies);
		    nearestEnemy = newNearestEnemy;
		}
	    }
	}
	return nearestEnemy;
    }

    //public static findNearestAlly() {



    //}

}
