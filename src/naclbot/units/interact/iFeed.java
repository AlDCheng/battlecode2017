// iFeed.java
// Isabel Naomi Quispe (naoqi@mit.edu) @pinguina

package naclbot.units.interact;

import battlecode.common.*;
import naclbot.units.motion.Yuurei.Line;
import naclbot.variables.GlobalVars;

import java.util.ArrayList;

public class iFeed extends GlobalVars {
	
	public static class Line{
		
		public MapLocation start;
		public MapLocation end;
		public MapLocation middle;
		public float damage;
		
		public Line(MapLocation point1, MapLocation point2, float dmg){
			this.start = point1;
			this.end = point2;
			this.middle = new MapLocation((point1.x + point2.x)/2, (point1.y + point2.y)/2);
			this.damage = dmg;
		}
		
		public void display(){
			
			// Displays an aquamarine line on the map from the start and end locations of the Line
			rc.setIndicatorLine(start, end, 127, 255, 212);
		}
		
		public void print(){
			
			// Print a message detailing the start and end points of the line
			System.out.println("Line start: [" + start.x + ", " + start.y + "]. Line end: [" + end.x + ", " + end.y + "]");
		}
	
	}
    
    // Determines if the robot will die or not 
    public static boolean willFeed(MapLocation optLocation) {
    	
    	float currentHealth = rc.getHealth();
    	float remainingHealth = currentHealth;
    	
    	//System.out.print("Bytecodes before finding bullets:");
    	//System.out.println(Clock.getBytecodeNum());
    	
    	float bulletDmg = bulletsThatHit(optLocation);

    	//System.out.print("Bytecodes after finding bullets:");
    	//System.out.println(Clock.getBytecodeNum());
    	
    	// If there are bullets that will hit then determine whether robot lives or dies from this
    	if (bulletDmg > 0) {
    		remainingHealth = currentHealth - bulletDmg;
    		
    		//System.out.println("Remaining Health: " + remainingHealth);
    		
    		if (remainingHealth <= 0) {
    			//System.out.print("Bytecodes after detecting bullet death:");
    			//System.out.println(Clock.getBytecodeNum());
    			return true;
    		}
    	}
    	
    	// If robot did not die, then determine if they will die from lumberjack strikes
    	
    	RobotInfo[] nearbyRobots = rc.senseNearbyRobots(rc.getType().bodyRadius + GameConstants.LUMBERJACK_STRIKE_RADIUS,rc.getTeam().opponent());
    	
    	if (nearbyRobots.length > 0) {
    		remainingHealth = healthLeftLumberjacks(nearbyRobots,remainingHealth);
    		
    		if (remainingHealth == 0) {
    			//System.out.print("Bytecodes after detecting lumberjack death:");
    			//System.out.println(Clock.getBytecodeNum());
    			return true;
    		} else {
    			//System.out.print("Bytecodes after not dying by lumberjack");
    			//System.out.println(Clock.getBytecodeNum());
    			return false;
    		}
    		
    	} else {
    		//System.out.print("Bytecodes after no lumberjacks");
			//System.out.println(Clock.getBytecodeNum());
    		return false;
    	}
    }
    
    public static boolean willBeAttacked (MapLocation optLocation) {
    	
    	float currentHealth = rc.getHealth();
    	float remainingHealth = currentHealth;
    	float bulletDmg = bulletsThatHit(optLocation);
		
    	// Determine if bullets will hit it
    	if (bulletDmg > 0) {
    		return true;
    	}
    	
    	// Determine if lumberjacks will hit it
    	
    	RobotInfo[] nearbyRobots = rc.senseNearbyRobots(rc.getType().bodyRadius + GameConstants.LUMBERJACK_STRIKE_RADIUS,rc.getTeam().opponent());
    	
    	if (nearbyRobots.length > 0) {
    		remainingHealth = healthLeftLumberjacks(nearbyRobots,remainingHealth);
    		// Only if attacked by lumberjacks does it return true
    		if (remainingHealth < currentHealth) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public static float healthLeftLumberjacks(RobotInfo[] nearbyRob, float currHealth) {
    	float totalLumberjackDamage = 0;
    	
    	for (RobotInfo robot: nearbyRob) {
    		if (robot.getType() == RobotType.LUMBERJACK) {
    			totalLumberjackDamage += 2;
    		}
    	}
    	
    	// If the damage is larger then no health left
    	// Otherwise there will be some health left
    	if (totalLumberjackDamage >= currHealth) {
    		// There is enough damage to kill the unit
    		float health = 0;
    		return health;
    	} else {
    		currHealth -= totalLumberjackDamage;
    		return currHealth;
    	}
    }
    
    /*
    public static float healthLeftBullets(ArrayList<Float> bullets, float currHealth) {
    	// This function determines how much health left after bullets hit
    	float totalBulletDamage = 0;
    	
		int i;
		for (i=0; i < bullets.size(); i++) {
		    totalBulletDamage += bullets.get(i);
		}
		
		// If the damage is larger then no health left 
		// Otherwise, there will be some health left
		if (totalBulletDamage >= currHealth) {
			// There is enough damage to kill the unit
			float health = 0;
			return health;
			
		} else {
			currHealth -= totalBulletDamage;
			return currHealth;
		}
    }
    */

    // Determines the bullets that will hit current unit
    public static float bulletsThatHit(MapLocation optimalLocation) {
    	
    	// Find the maximal distance away from the startingLocation that we must scan to determine the optimal location....
		float scanRadius = rc.getType().strideRadius + rc.getType().bodyRadius + (float) (0.5);	
		
		// Senses all nearby bullets 
		BulletInfo[] nearbyBullets = rc.senseNearbyBullets();
		
		// Gets the starting location of the unit 
		MapLocation startingLocation = rc.getLocation();
		
		//System.out.print("Before bullet lines:");
		//System.out.println(Clock.getBytecodeNum());
		
		// Get the bullet lines for the current scenario....
		ArrayList<Line> bulletLines = getBulletLines(nearbyBullets, scanRadius, startingLocation);
		
		//System.out.print("After bullet lines:");
		//System.out.println(Clock.getBytecodeNum());
    	
		// Find lines that intersect and then store the damage that the bullet does 
		float intersectBulletDamage = bulletLinesWillIntersect(bulletLines,optimalLocation,rc.getType().bodyRadius);
		
		//System.out.print("After finding which intersect: ");
		//System.out.println(Clock.getBytecodeNum());
		
		return intersectBulletDamage;
    }
    
    
    // Technically not correct, just tests the start and end points of the line...
 	public static float bulletLinesWillIntersect(ArrayList<Line> bulletLines, MapLocation testLocation, float bodyRadius){
 		
 		float totaldmg = 0;
 		
 		for(Line bulletLine: bulletLines){
 			// If either endpoint is within one body radius of the test location, return true...
// 			System.out.println(bulletLine.start + "; " + bulletLine.end);
 			rc.setIndicatorLine(bulletLine.start, bulletLine.end, 255, 0, 0);
 			
 			if(bulletLine.start.distanceTo(testLocation) <= bodyRadius || bulletLine.end.distanceTo(testLocation) <= bodyRadius || bulletLine.middle.distanceTo(testLocation) <= bodyRadius){			
 				totaldmg += bulletLine.damage;
 			}
 		}
 		// If no intersecting lines have been found...  return empty arraylist
 		return totaldmg;	
 	}
    
    // Function to obtain the lines of all the bullets in this turn....
 	public static ArrayList<Line> getBulletLines(BulletInfo[] nearbyBullets, float distance, MapLocation centre) {
 		
 		// Initialize a list of locations where all the bullets will be...
 		ArrayList <Line> bulletLines = new ArrayList<Line>();
 		
 		// For each of the nearby bullets in the bullet list
 		for (BulletInfo bullet: nearbyBullets) {
 			
 			// Retrieve the location of the bullets currently
 			MapLocation currentLocation = bullet.getLocation();
 			
 			// Get the velocity of the bullets
 			Direction currentDirection = bullet.getDir();
 			float currentSpeed = bullet.getSpeed();
 			
 			// Calculate the location that the bullet will be at in one turn and add to the list of Locations....
 			MapLocation newLocation = currentLocation.add(currentDirection, currentSpeed);	
 			
 			// If either endpoint of the bullet's trajectory is within the search bounds....
 			if(currentLocation.distanceTo(centre) <= distance || newLocation.distanceTo(centre) <= distance){
 			// Initialize the bullet line...
				Line newLine;

				newLine = new Line(currentLocation,newLocation,bullet.getDamage());				
				// Add the line to the list of bullet lines....
				bulletLines.add(newLine);

 			}
 		}
 		return bulletLines;
 	}
    
    
}
