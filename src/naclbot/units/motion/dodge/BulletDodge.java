// Dodging collisions with bullets
package naclbot.units.motion.dodge;
import battlecode.common.*;

import naclbot.variables.GlobalVars;

import java.util.ArrayList;

public class BulletDodge extends GlobalVars {
	/*
	public static MapLocation goPlacesAndDodge(MapLocation desiredLocation) {
		
		ArrayList<MapLocation> bulletLocations;
		BulletInfo[] bullets = rc.senseNearbyBullets();
		MapLocation optimalLocation;
		
		// If there are bullets then try to dodge while still going near the desired location
		// Otherwise then just go to the desired location
		if (bullets.length > 0) {
			bulletLocations = whereWillBulletsBe(bullets);
			optimalLocation = findOptimalLocation(bulletLocations,desiredLocation);
			return optimalLocation;
			
		} else {
			// Just go places
			return desiredLocation;
		}
	}
	
	public static MapLocation findOptimalLocation(ArrayList<MapLocation> nearbyBulletLoc, MapLocation goalLocation) {
		// CHANGE THIS
		return goalLocation;
		
	}
	
	public static ArrayList<MapLocation> whereWillBulletsBe(BulletInfo[] nearbyBullets) {
		// This function assumes that the list nearbyBullets is not empty
		ArrayList<MapLocation> newBulletLocations = new ArrayList<MapLocation>();
		// Says the location of where the bullets will be one turn from now
		for (BulletInfo bullet: nearbyBullets) {
			MapLocation currLoc = bullet.getLocation();
			Direction currDir = bullet.getDir();
			float currSpeed = bullet.getSpeed();
			MapLocation newLoc = currLoc.add(currDir,currSpeed);
			newBulletLocations.add(newLoc);
		}
		return newBulletLocations;
	}
	*/
	
    public static Direction whereToDodge(BulletInfo[] bullets) {
		float nearestBulletDistance = -1;
		DodgeType nearestBullet = null;
	
		// Search for nearest bullet 
		for (BulletInfo bullet: bullets) {
		    DodgeType dodgeInfo = new DodgeType(bullet);
		    boolean willCollide = dodgeInfo.willCollide();
		    float distance = dodgeInfo.getDistToRobot();
	
		    if (willCollide == true && (nearestBulletDistance == -1)) {
		    	nearestBulletDistance = distance;
		    	nearestBullet = dodgeInfo;
		    } else if (willCollide == true && (distance < nearestBulletDistance)) {
		    	nearestBulletDistance = distance;
		    	nearestBullet = dodgeInfo;
		    }	    
		}
		if (nearestBullet != null) {
		    Direction dodgeDir = nearestBullet.getDirectionToMove();
		    return dodgeDir;
		} else {
		    return null;
		}
    }
    
   
}
