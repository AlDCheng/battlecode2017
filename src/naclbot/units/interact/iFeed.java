// Handling the FEEDER units 

package naclbot.units.interact;

import battlecode.common.*;
import naclbot.variables.GlobalVars;

import java.util.ArrayList;

public class iFeed extends GlobalVars {
    
    // Determines if the robot will die or not 
    public static boolean willFeed(MapLocation optLocation) {
    	
    	float currentHealth = rc.getHealth();
    	float remainingHealth = currentHealth;
    	ArrayList<BulletInfo> bulletsThatWillHit = bulletsThatHit(optLocation);
		
    	// If there are bullets that will hit then determine whether robot lives or dies from this
    	if (bulletsThatWillHit.size() > 0) {
    		remainingHealth = healthLeftBullets(bulletsThatWillHit,currentHealth);
    		
    		if (remainingHealth == 0) {
    			return true;
    		}
    	}
    	
    	// If robot did not die, then determine if they will die from lumberjack strikes
    	//float strikeRadius = rc.getType().bodyRadius + GameConstants.LUMBERJACK_STRIKE_RADIUS;
    	float strikeRadius = GameConstants.LUMBERJACK_STRIKE_RADIUS;
    	RobotInfo[] nearbyRobots = rc.senseNearbyRobots(strikeRadius,rc.getTeam().opponent());
    	
    	if (nearbyRobots.length > 0) {
    		remainingHealth = healthLeftLumberjacks(nearbyRobots,remainingHealth);
    		
    		if (remainingHealth == 0) {
    			return true;
    		} else {
    			return false;
    		}
    		
    	} else {
    		System.out.println("WILL NOT DIE THIS TIME TEEHEE");
    		return false;
    	}
    	
    	
    }
    
    public static float healthLeftLumberjacks(RobotInfo[] nearbyRob, float currHealth) {
    	float totalLumberjackDamage = 0;
    	
    	int i;
    	for (RobotInfo robot: nearbyRob) {
    		if (robot.getType() == RobotType.LUMBERJACK) {
    			totalLumberjackDamage += 2;
    		}
    	}
    	
    	// If the damage is larger then no health left
    	// Otherwise there will be some health left
    	if (totalLumberjackDamage >= currHealth) {
    		// There is enough damage to kill the unit
    		System.out.println("GG I WILL DIE FROM LUMBERJACK STRIKES");
    		float health = 0;
    		return health;
    	} else {
    		if (totalLumberjackDamage == 0) {
    			System.out.println("NO LUMBERJACKS");
    		}
    		System.out.println("WILL NOT DIE FROM LUMBERJACK DAMAGE");
    		currHealth -= totalLumberjackDamage;
    		return currHealth;
    	}
    }
    
    public static float healthLeftBullets(ArrayList<BulletInfo> bullets, float currHealth) {
    	// This function determines how much health left after bullets hit
    	float totalBulletDamage = 0;
    	
		int i;
		for (i=0; i < bullets.size(); i++) {
		    totalBulletDamage += bullets.get(i).getDamage();
		}
		
		// If the damage is larger then no health left 
		// Otherwise, there will be some health left
		if (totalBulletDamage >= currHealth) {
			// There is enough damage to kill the unit
			System.out.println("GG I WILL DIE FROM BULLETS");
			float health = 0;
			return health;
			
		} else {
			currHealth -= totalBulletDamage;
			System.out.println("WILL NOT DIE FROM BULLETS");
			return currHealth;
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