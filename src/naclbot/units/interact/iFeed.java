// Handling the FEEDER units 

package naclbot.units.interact;

import battlecode.common.*;
import naclbot.variables.GlobalVars;

import java.util.ArrayList;

public class iFeed extends GlobalVars {
    
    // Determines if the robot will die or not 
    public static boolean willFeed(MapLocation optLocation) {
    	
    	float currentHealth = rc.getHealth();
    	ArrayList<BulletInfo> bulletsThatWillHit = bulletsThatHit(optLocation);
		
    	if (bulletsThatWillHit.size() > 0) {
    		float totalDamage = 0;
    		int i;
			for (i=0; i < bulletsThatWillHit.size(); i++) {
			    totalDamage += bulletsThatWillHit.get(i).getDamage();
			}
			
			if (totalDamage >= currentHealth) {
				// There is enough damage to kill the unit
				System.out.println("GG I WILL DIE");
				return true;
			    
			} else {
				// Unit takes damage but will not die
				System.out.println("WILL NOT DIE THIS TIME, NOT ENOUGH DMG ON ME");
				return false;
			    
			}
    	} else {
    		// If no bullets hit it then will not die
    		System.out.println("WILL NOT DIE THIS TIME, NO BULLETS WILL HIT");
    		return false;
    		
    	}
    }

    // Determines the bullets that will hit current unit
    public static ArrayList<BulletInfo> bulletsThatHit(MapLocation optimalLocation) {
    	
    	ArrayList<BulletInfo> bulletsThatWillHit = new ArrayList<BulletInfo>();
    	
    	// These two should have same indices
		BulletInfo[] bullets = rc.senseNearbyBullets();
		ArrayList<MapLocation> newLocBullets = whereWillBulletsBe(bullets);
		
		float myRadius = rc.getType().bodyRadius;
		
		int i;
		for (i = 0; i < newLocBullets.size(); i++) {
			// Check if the bullet will hit the unit (will be inside the body radius of its optimal location)
			boolean willHit = willBulletHit(newLocBullets.get(i),optimalLocation,myRadius);
			if (willHit) {
				bulletsThatWillHit.add(bullets[i]); // Add the bullet info to the outgoing ArrayList
			}
		}
		
		return bulletsThatWillHit;
    }
    
    // Determines if a single bullet will hit the current unit
    public static boolean willBulletHit(MapLocation bulletLoc, MapLocation optimalLoc, float radius) {
    	float distBulletUnit = bulletLoc.distanceTo(optimalLoc);
    	if (distBulletUnit <= radius) {
    		return true;
    	} else {
    		return false;
    	}
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
    
}
