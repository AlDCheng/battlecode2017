package naclbot.units.AI.archon;
import battlecode.common.*;

import naclbot.variables.GlobalVars;
import naclbot.variables.DataVars;
import naclbot.variables.DataVars.*;

import naclbot.variables.BroadcastChannels;
import naclbot.units.interact.iFeed;
import naclbot.units.motion.*;
import naclbot.units.motion.routing.Routing;
import naclbot.units.motion.search.TreeSearch;

import java.util.ArrayList;
import java.util.Arrays;

public class Aqua extends GlobalVars {
	
	// Distress signal
	public static boolean believeHasDied = false;
	
	// Discreteness of  the initial search to find nearest walls...
    private static final float initialWallCheckGrain = 1;
	
	public static boolean manageBeingAttacked(MapLocation loc) throws GameActionException{
		boolean beingAttacked = iFeed.willBeAttacked(loc);
		float currentHealth = rc.getHealth();
		float maxHealth = rc.getType().getStartingHealth();
		if (beingAttacked && currentHealth < (0.5*maxHealth)) {
			// BIT 0 - GIVES MY X
			// BIT 1 - GIVES MY Y
			int numArchonsBeingAttacked = rc.readBroadcast(BroadcastChannels.ARCHONS_UNDER_ATTACK);
			int channelStartNum = BroadcastChannels.ARCHONS_UNDER_ATTACK + numArchonsBeingAttacked * 2;
			
			rc.broadcastFloat(channelStartNum + 1, loc.x);
			rc.broadcastFloat(channelStartNum + 2, loc.y);
			
			boolean willDie = iFeed.willFeed(loc);
			if (willDie) {
				believeHasDied = true;
				
				// Get the current number of soldiers in service
		        int numberOfAliveArchons = rc.readBroadcast(BroadcastChannels.ARCHONS_ALIVE_CHANNEL);
		        
		        // Update soldier number for other units to see.....
		        rc.broadcast(BroadcastChannels.ARCHONS_ALIVE_CHANNEL, numberOfAliveArchons - 1);
		        return true;
				
			}
			else {
				return false;
			}
		}
		return false;
	}
        
	public static void disperseFromGardeners(MapLocation myLocation, float strideRadius, float bodyRadius, Direction initDirection) throws GameActionException { 
		//initialize variables
    	MapLocation disperseLocation, correctedLocation;
    	MapLocation desiredMove = myLocation;
		
		// Find optimal space to move to 
		disperseLocation = findOptimalSpace(30, strideRadius+bodyRadius, strideRadius+bodyRadius, initDirection.getAngleDegrees());
		if (disperseLocation != null){            		
    		desiredMove = disperseLocation;
    	}
		
		// Get corrected movement
		correctedLocation = Yuurei.correctAllMove(strideRadius, bodyRadius, false, rc.getTeam(), rc.getLocation(), desiredMove);
		if (rc.canMove(correctedLocation)) {
			System.out.println("Valid movement in dispersion");
				rc.move(correctedLocation);
		}
	}
	
	// Find a direction to hire a direction or return null if there isn't one found...
	
	public static Direction tryHireGardener(Direction tryDirection){
		
		// Iterate over 36 different angles, starting near the inputed direction and diverging away to see if a gardener can be built in any one of those locations..
		for (int i = 0; i <= 18; i++){
			
			Direction newHireDirection = new Direction(tryDirection.radians + i * (float) (Math.PI/18));
			if (rc.canHireGardener(newHireDirection)){
				return newHireDirection;
			}
			newHireDirection = new Direction(tryDirection.radians - i * (float) (Math.PI/18));
			if (rc.canHireGardener(newHireDirection)){
				return newHireDirection;			
			}			
		}
		return null;
	}
	
	public static boolean checkNearbyGardeners(float radius) {
		RobotInfo[] nearbyRobots = rc.senseNearbyRobots(radius);
		
		for (RobotInfo robot: nearbyRobots) {
			if (robot.getType() == RobotType.GARDENER) {
				return true;
			}
		}
		return false;
	}
 	
	// Function to obtain the initial direction that the archon will attempt to build something - checks all nearby walls 
	// so that it gets the most open space to move forward and construct additional gardeners
	
	public static Direction getInitialWalls(MapLocation myLocation) throws GameActionException{
		
		// Initialize an array to store the  distances to any walls (obstacles to building)
		float[] wallDistances = new float[16]; 
		Arrays.fill(wallDistances, 0);
		
		// Check up to a distance of 9 away... and 16 different angles
		for(int i = 1; i <= 9 / initialWallCheckGrain; i++){
			for(int j = 0; j < 16; j++){
				
				// Get the locations to check for the obstacles
				Direction directionToCheck = new Direction((float)(j * Math.PI/8));
				float distanceToCheck = i * initialWallCheckGrain;
				MapLocation locationToCheck = myLocation.add(directionToCheck, distanceToCheck);
				
				// Check to see if the considered point is off the map or has a tree
				if(!rc.isLocationOccupiedByTree(locationToCheck) && rc.onTheMap(locationToCheck)){
					
					// SYSTEM CHECK If the location is free print a BLUE DOT
					//rc.setIndicatorDot(locationToCheck, 0, 0, 128);
					
					if (wallDistances[j] >= 0){
						wallDistances[j] = i * initialWallCheckGrain;
					}
				}
				else{
					
					// SYSTEM CHECK - If the location isn't free print a RED DOT
					//rc.setIndicatorDot(locationToCheck, 128, 0, 0);
					
					if (wallDistances[j] >= 0){
						wallDistances[j] *= -1;
					}				
				}
			}
		}
		// Placeholder to find the minimum (moving average of three directions)
		int index = -1;
		float maximum = Integer.MIN_VALUE;
		
		// Iterate over the fall wall Distances....
		for (int i = 0; i < wallDistances.length; i ++){
			
			// Values to get the indices to iterate over....
			int indexAbove = (i+1) % 16;
			int indexBelow = (i+15) % 16;
			
			// Obtain the average of the three wall distances  surrounding the current index
			float average = Math.abs(wallDistances[i]) + (Math.abs(wallDistances[indexAbove]) + Math.abs(wallDistances[indexBelow]))/2;
			if (average > maximum){
				maximum = average;
				index = i;
			}			
		}
		
		// Calculate the direction to go to...
		Direction newDirection = new Direction(index * (float) (Math.PI/8));
		
		// SYSTEM CHECK - Put a green line to show which direction the arcon will attempt to move...
		rc.setIndicatorLine(myLocation, myLocation.add(newDirection,10), 0, 125, 0);
		
		return newDirection;
	}
	
	//----------------------------------------------------------------------------------------------------
	// Alan's Work
	// Imported from gardeners
	// Check build radius
	public static float checkBuildRadius(float angleInterval, float dist, float distRad) throws GameActionException {
		float crowdedFactor = 0;
		
		// Define direction variable
		Direction dir = new Direction(0);
		
		// Get current location
		MapLocation curLoc = rc.getLocation();
		
		// Make sure only one revolution is check
		float totalAngle = 0;
		
		// Scan for 1 revolution
		while (totalAngle < 360) {
			
			// Point to be scan
			MapLocation newLoc = curLoc.add(dir,dist);
			
			// Check if space of radius 0.5 is clear 2.5 units away
			// First check if on map
			if(rc.onTheMap(newLoc, distRad)) {
				// Then check if occupied
				if(rc.isCircleOccupiedExceptByThisRobot(newLoc,distRad)) {
					rc.setIndicatorLine(curLoc.add(dir,(float)1), newLoc.add(dir,distRad), 255, 128, 0);
					
					// Increase factor
					crowdedFactor += angleInterval/360;
				}
				else {
					rc.setIndicatorLine(curLoc.add(dir,(float)1), newLoc.add(dir,distRad), 0, 255, 255); 
				}
			}
			else {
				// Increase factor
				crowdedFactor += angleInterval/(float)360.0;
			}
			dir = dir.rotateLeftDegrees(angleInterval);
			totalAngle += angleInterval; 
		}
		
		System.out.println("Crowded Factor: " + crowdedFactor);
		
		return crowdedFactor;
	}
	
	public static MapLocation findOptimalSpace(float angleInterval, float lengthInterval, float maxLength, float start) throws GameActionException {
		
//		System.out.println("Angle Interval: " + angleInterval + ", Length Interval: " + lengthInterval);
		
		// Get robot type
		RobotType thisBot = rc.getType();
		Team us = rc.getTeam();
		Team them = us.opponent();
		
		// Get current location
		MapLocation curLoc = rc.getLocation();
		
		// Declare potential and final location
		MapLocation potLoc = curLoc;
		MapLocation finalLoc = curLoc;
		
		// Find openness of current space:
		float minOpenness = Float.MAX_VALUE;
		float radius = 2;
		
		// Tree weighting; radius = 1 (body) + 2 (tree)
		minOpenness += rc.senseNearbyTrees(radius).length;
		// Unit weighting (for own Team)
		minOpenness += rc.senseNearbyRobots(radius, us).length;
		// Unit weighting (for enemy Team)
		minOpenness += 3*rc.senseNearbyRobots(radius, them).length;
		
//		System.out.println("Max Length: " + maxLength);
		float length = lengthInterval;
		// Declare angle/length modifiers
		// Check
		while(length <= maxLength) {
			float angle = start;
			float totalAngle = 0;
			
			while(totalAngle < 360) {
				// Get potential location
				potLoc = curLoc.add((float)Math.toRadians(angle), length);
				rc.setIndicatorDot(potLoc, 178, 102, 255);
				
				// Check surroundings
				float openness = Float.MAX_VALUE;
				
				// Check if on Map
				if(rc.onTheMap(potLoc)) {
					
					// Add penalty for cases on edge of map (equivalent to 3 trees)
					if(!(rc.onTheMap(potLoc, 2))) {
//						openness = (float)0.5;
					} else {
						openness = 0;
						// Tree weighting; radius = 1 (body) + 2 (tree)
						openness += rc.senseNearbyTrees(potLoc, radius, null).length;
						// Unit weighting (for own Team)
//						openness += rc.senseNearbyRobots(potLoc, radius, us).length;
						RobotInfo[] ourBots = rc.senseNearbyRobots(potLoc, radius, us);
						for (int i = 0; i < ourBots.length; i++) {
							if (ourBots[i].type == battlecode.common.RobotType.GARDENER) {
								openness += 1;
							}
							else {
								//openness += 0.1;
							}
						}
						// Unit weighting (for enemy Team)
						openness += 3*rc.senseNearbyRobots(potLoc, radius, them).length;
						openness += 5*rc.senseNearbyBullets(potLoc, radius).length;
					}
					
//					System.out.println("Angle: " + angle + ", length: " + length + ", Open: " + openness);
				}
				
				// Compare with existing, and replace if lower
				if (openness < minOpenness) {
					minOpenness = openness;
					finalLoc = potLoc;
				}
				angle += angleInterval;
				totalAngle += angleInterval;
			}
			length += lengthInterval;
		}
		
//		System.out.println("Final: " + finalLoc[0] + ", " + finalLoc[1]);
		
		return finalLoc;
	}
	
	public static float updateTreeDensity() throws GameActionException {
		
		//initialize some variables 
		ArrayList<TreeInfo> neutralTrees = new ArrayList<TreeInfo>();
		
		//area of archon sight range
		float sightArea = (float) (Math.PI * Math.pow(10, 2));
		
		//we use this variable to keep a total of area taken up by all tree in sight range
		float treeArea = 0;
		
		//get information about nearby trees
		TreeInfo[] surroundingTrees = rc.senseNearbyTrees();
		
		//filter for neutral trees 
		for (TreeInfo tree : surroundingTrees) {
			if (tree.getTeam() == Team.NEUTRAL && rc.canSenseAllOfCircle(tree.getLocation(),tree.getRadius())) {
				treeArea = treeArea + (float) (Math.PI * Math.pow(tree.getRadius(), 2));
			}
		}
		
		// percentage of space in archon sight range that is taken up by trees
		float areaRatio = treeArea/sightArea;
		
		// produces a scaled ratio taking into account area taken up by trees and number of trees
		float scaledValue = (float) ((treeArea*100/sightArea)*0.4 + surroundingTrees.length*0.6);
				
		//returns this ratio as an integer by multiplying by 100
		return scaledValue;
		
	}
}