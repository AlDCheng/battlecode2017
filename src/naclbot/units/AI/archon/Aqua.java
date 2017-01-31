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
	
	public static float emptyDensity = 0;
	private static float gardenerWeight = 1;
	private static float bulletWeight = 0;
	private static float enemyWeight = 20;
	
	private static final float sensorRadius = battlecode.common.RobotType.ARCHON.sensorRadius;	
	
	// Discreteness of  the initial search to find nearest walls...
    private static final float initialWallCheckGrain = 1;
	
  //----------------------------------[Distress]----------------------------------
  	public static void fixAccidentalDeathNotification() throws GameActionException {
  		// Reset belief in the robot dying this round....
//      	believeHasDied = false;    	

  		// Get the current number of soldiers in service
          int numberOfAliveGardeners = rc.readBroadcast(BroadcastChannels.GARDENERS_ALIVE_CHANNEL);
          
          // Update soldier number for other units to see.....
          rc.broadcast(BroadcastChannels.GARDENERS_ALIVE_CHANNEL, numberOfAliveGardeners + 1);
  	}
  	
  	public static boolean manageBeingAttacked(MapLocation loc, int unitNumber) throws GameActionException{
  		boolean beingAttacked = iFeed.willFeed(loc);
  		if (beingAttacked) {
  			RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
  			BroadcastChannels.broadcastDistress(rc.getHealth(), enemyRobots, rc.getLocation(), unitNumber);
  			boolean willDie = iFeed.willFeed(loc);
  			if (willDie) {
  				
  				// Get the current number of soldiers in service
  		        int numberOfAliveGardeners = rc.readBroadcast(BroadcastChannels.GARDENERS_ALIVE_CHANNEL);
  		        
  		        // Update soldier number for other units to see.....
  		        rc.broadcast(BroadcastChannels.GARDENERS_ALIVE_CHANNEL, numberOfAliveGardeners - 1);
  		        
  		        return true;

  			}
  			else {
  				return false;
  			}
  		}
  		return false;
  	}
  	
        
	public static void disperseFromGardeners(MapLocation myLocation, float strideRadius, float bodyRadius, Direction initDirection, int unitNumber) throws GameActionException { 
		//initialize variables
    	MapLocation disperseLocation, correctedLocation;
    	MapLocation desiredMove = myLocation;
		
		// Find optimal space to move to
//		disperseLocation = findOptimalSpace(30, strideRadius+bodyRadius, strideRadius+bodyRadius, initDirection.getAngleDegrees(), (float)4);
//    	disperseLocation = findOptimalSpaceNew(30, strideRadius+bodyRadius, strideRadius+bodyRadius, 
//    											initDirection.getAngleDegrees(), (float)4, (float)2);
    	disperseLocation = findOptimalSpaceSweep(15, strideRadius+bodyRadius, strideRadius+bodyRadius, initDirection.getAngleDegrees(), (float)4);
//    	disperseLocation = findOptimalSpace(30, sensorRadius-3, sensorRadius-3, initDirection.getAngleDegrees());
		if (disperseLocation != null){            		
    		desiredMove = disperseLocation;
    	}
		
		// Get corrected movement
		correctedLocation = Yuurei.correctAllMove(strideRadius, bodyRadius, false, rc.getTeam(), rc.getLocation(), desiredMove);
		
		System.out.println("Original: " + disperseLocation + ", Corrected: " + correctedLocation + ", Distance: " + disperseLocation.distanceTo(correctedLocation));
		
		if (correctedLocation != null) {
			if (disperseLocation.distanceTo(correctedLocation) < 2.8) {
				if (rc.canMove(correctedLocation)) {
					believeHasDied = manageBeingAttacked(correctedLocation, unitNumber);
					System.out.println("Valid movement in dispersion");
						rc.move(correctedLocation);
				}
			}
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
	
	public static Direction getInitialWalls(MapLocation myLocation, Direction start) throws GameActionException{
		
		// Initialize an array to store the  distances to any walls (obstacles to building)
		float[] wallDistances = new float[16];
		boolean[] hitWall = new boolean[16];
		Arrays.fill(wallDistances, 0);
		
		float emptyArea = 0;
		float emptyAreaWall = 0;
		
		// Check up to a distance of 9 away... and 16 different angles
		for(int i = 1; i <= 9 / initialWallCheckGrain; i++){			
			for(int j = 0; j < 16; j++){
				
				// Get the locations to check for the obstacles
				Direction directionToCheck = new Direction((float)(start.radians + j * Math.PI/8));
				float distanceToCheck = i * initialWallCheckGrain;
				MapLocation locationToCheck = myLocation.add(directionToCheck, distanceToCheck);
				
				// Check to see if the considered point is off the map or has a tree
				if(!rc.isLocationOccupiedByTree(locationToCheck) && rc.onTheMap(locationToCheck)){
					
					// SYSTEM CHECK If the location is free print a BLUE DOT
					//rc.setIndicatorDot(locationToCheck, 0, 0, 128);
					
					if ((i == 9) && !(hitWall[j])) {
						emptyArea += (float)(1.0/16.0)*(Math.PI*(float)9*(float)9);
					}
					
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
					
					if(!hitWall[j] && rc.isLocationOccupiedByTree(locationToCheck)) {
						hitWall[j] = true;
//						System.out.println((float)(1.0/16.0)*(Math.PI*(float)i*(float)i));
						emptyArea += (float)(1.0/16.0)*(Math.PI*(float)i*(float)i);
					}
					else if(!hitWall[j]) {
						hitWall[j] = true;
						emptyAreaWall += (float)(1.0/16.0)*(Math.PI*(float)9*(float)9);
					}
				}
			}
		}
		
		System.out.println("Empty Area: " + emptyArea);
		emptyDensity = (float)(emptyArea/(Math.PI*81.0 - emptyAreaWall));
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
		Direction newDirection = new Direction(start.radians + index * (float) (Math.PI/8));
		
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
				if(rc.isCircleOccupiedExceptByThisRobot(newLoc, distRad)) {
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
	
	public static MapLocation findOptimalSpace(float angleInterval, float lengthInterval, float maxLength, float start, float radius) throws GameActionException {
		
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
		
		// Tree weighting; radius = 1 (body) + 2 (tree)
		minOpenness += rc.senseNearbyTrees(radius).length;
		// Unit weighting (for own Team)
		minOpenness += rc.senseNearbyRobots(radius, us).length;
		// Unit weighting (for enemy Team)
		minOpenness += enemyWeight*rc.senseNearbyRobots(radius, them).length;
		
		int empty = 0;
		int totalchecks = 0;
		
//		System.out.println("Max Length: " + maxLength);
		float length = lengthInterval;
		// Declare angle/length modifiers
		// Check
		while(length <= maxLength) {
			float angle = start;
			float totalAngle = 0;
			
			while(totalAngle < 360) {
				
				totalchecks++;
				
				// Get potential location
				potLoc = curLoc.add((float)Math.toRadians(angle), length);
				rc.setIndicatorDot(potLoc, 178, 102, 255);
				
				// Check surroundings
				float openness = Float.MAX_VALUE;
				
				// Check if on Map
				if(rc.onTheMap(potLoc)) {
					
					// Add penalty for cases on edge of map (equivalent to 3 trees)
					if(!(rc.onTheMap(potLoc, 2))) {
						empty++;
						openness = (float)10;
//						openness = (float)0.5;
					} else {
						openness = 0;
						// Tree weighting; radius = 1 (body) + 2 (tree)
						TreeInfo[] trees = rc.senseNearbyTrees(potLoc, radius, null);
						for (int i = 0; i < trees.length; i++) {
							if (trees[i].team == us) {
								openness += 4;
							}
							else {
								openness += 1;
							}
						}
						
//						openness += 2*rc.senseNearbyTrees(potLoc, radius, null).length;
						// Unit weighting (for own Team)
//						openness += rc.senseNearbyRobots(potLoc, radius, us).length;
						RobotInfo[] ourBots = rc.senseNearbyRobots(potLoc, radius, us);
						for (int i = 0; i < ourBots.length; i++) {
							if (ourBots[i].type == battlecode.common.RobotType.GARDENER) {
								openness += gardenerWeight;
							}
							else if (ourBots[i].type == battlecode.common.RobotType.ARCHON) {
								openness += 1;
							}
							else {
								//openness += 0.1;
							}
						}
						// Unit weighting (for enemy Team)
						openness += enemyWeight*rc.senseNearbyRobots(potLoc, radius, them).length;
						openness += bulletWeight*rc.senseNearbyBullets(potLoc, radius).length;
						
						System.out.println("Angle: " + angle + ", openness: " + openness);
						
						// Check if every space is open
						if(openness <= 1){
							empty++;
						}
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
		
		if(empty == totalchecks) {
			return curLoc;
		}
		return finalLoc;
	}
	
	public static float calculateTreeDensity() throws GameActionException {
		
		//initialize some variables 
		ArrayList<TreeInfo> neutralTrees = new ArrayList<TreeInfo>();
		
		//area of archon sight range
		float sightArea = (float) (Math.PI * Math.pow(10, 2));
		
		//we use this variable to keep a total of area taken up by all tree in sight range
		float treeArea = 0;
		
		//get information about nearby trees
		TreeInfo[] surroundingTrees = rc.senseNearbyTrees(rc.getLocation(), rc.getType().sensorRadius, Team.NEUTRAL);
		
		//filter for neutral trees 
		for (TreeInfo tree : surroundingTrees) {
//			if (tree.getTeam() == Team.NEUTRAL && rc.canSenseAllOfCircle(tree.getLocation(),tree.getRadius())) {
			if (rc.canSenseAllOfCircle(tree.getLocation(),tree.getRadius())) {
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
	
	public static float checkEmptySpace(MapLocation myLocation) throws GameActionException{
		
		// Initialize an array to store the  distances to any walls (obstacles to building)
		float[] wallDistances = new float[16];
		boolean[] hitWall = new boolean[16];
		Arrays.fill(wallDistances, 0);
		
		float emptyArea = 0;
		float emptyAreaWall = 0;
		
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
					
					if ((i == 9) && !(hitWall[j])) {
						emptyArea += (float)(1.0/16.0)*(Math.PI*(float)9*(float)9);
					}
					
				}
				else{
					
					// SYSTEM CHECK - If the location isn't free print a RED DOT
					//rc.setIndicatorDot(locationToCheck, 128, 0, 0);
					
					if (wallDistances[j] >= 0){
						wallDistances[j] *= -1;
					}
					
					if(!hitWall[j] && rc.isLocationOccupiedByTree(locationToCheck)) {
						hitWall[j] = true;
//						System.out.println((float)(1.0/16.0)*(Math.PI*(float)i*(float)i));
						emptyArea += (float)(1.0/16.0)*(Math.PI*(float)i*(float)i);
					}
					else if(!hitWall[j]) {
						hitWall[j] = true;
						emptyAreaWall += (float)(1.0/16.0)*(Math.PI*(float)9*(float)9);
					}
				}
			}
		}
		
		System.out.println("Empty Area: " + emptyArea);
		float density = (float)(emptyArea/(Math.PI*81.0 - emptyAreaWall));
		return density;
	}
	
	//---------------------------------------------------------------------------------------------
	// Scans immediate radius of gardener (in degrees)
	// Output Format: [0]last availible tree plant space; [1]reserved unit building space
	public static Direction scanBuildRadius(float angleInterval, float start, float dist, float extend, float totalSweep) throws GameActionException {
		// Storage output
		Direction finalDir = new Direction(start + (float)Math.PI);
		
		// Define direction variable
		Direction dir = new Direction((float)Math.toRadians(start));
		Direction dirCheck = new Direction((float)Math.toRadians(start)); 
		
		MapLocation curLoc = rc.getLocation();
		
		// Make sure only one revolution is checked
		float totalAngle = 0;
		
		System.out.println("Sweep rad: " + (totalSweep/2)+1);
		
		// Scan for 1 revolution
		while (totalAngle <= (totalSweep/2)+1) {
			
			// Scan both sides around
			for (int rotSwitch = -1; rotSwitch < 2; rotSwitch+=2) {
				// Increment values
				if (start <= 0) {
					dirCheck = dir.rotateLeftDegrees(rotSwitch * totalAngle);
				}
				else {
					dirCheck = dir.rotateRightDegrees(rotSwitch * totalAngle);
				}
				
				// Get location to search
				MapLocation newLoc = curLoc.add(dirCheck,dist);
				
				rc.setIndicatorLine(curLoc, newLoc, 255, 128, 0);
				
//				System.out.println("New Loc: " + newLoc);
			
				rc.setIndicatorLine(curLoc, newLoc, 0, 255, 255);
				
				if(!(rc.isCircleOccupiedExceptByThisRobot(curLoc.add(dirCheck, dist),(float)0.6)) &&
						!(rc.isCircleOccupiedExceptByThisRobot(curLoc.add(dirCheck, dist+extend),(float)0.6))) {
					if (rc.onTheMap(newLoc.add(dirCheck, extend), (float)0.5)) {
						rc.setIndicatorLine(curLoc, newLoc, 255, 128, 0);
						finalDir = dirCheck;
						return finalDir;
					}
				}
			}
			totalAngle += angleInterval;
		}
		
		return finalDir;
	}
	
	public static MapLocation findOptimalSpaceNew(float angleInterval, float lengthInterval, float maxLength, float start, 
													float radius, float radiusTrees) throws GameActionException {
		
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
		
		/*
		// Tree weighting; radius = 1 (body) + 2 (tree)
		minOpenness += rc.senseNearbyTrees(radius).length;
		// Unit weighting (for own Team)
		minOpenness += rc.senseNearbyRobots(radius, us).length;
		// Unit weighting (for enemy Team)
		minOpenness += enemyWeight*rc.senseNearbyRobots(radius, them).length;
		*/
		
		int empty = 0;
		int totalchecks = 0;
		
//		System.out.println("Max Length: " + maxLength);
		float length = lengthInterval;
		// Declare angle/length modifiers
		// Check
		while(length <= maxLength) {
			float angle = start;
			float totalAngle = 0;
			
			while(totalAngle <= 180) {
				
				// Scan both sides around
				for (int rotSwitch = -1; rotSwitch < 1; rotSwitch+=2) {
				
					float[] openness = {Float.MAX_VALUE,Float.MAX_VALUE};
					
					for(int k = 0; k <= 1; k++) {
						totalchecks++;
//						System.out.println("k: " + k);
						// Get potential location
						potLoc = curLoc.add((float)Math.toRadians(rotSwitch*angle + 180*k), length);
						
						rc.setIndicatorDot(potLoc, 178, 102, 255);
						
						
						// Check if on Map
						if(rc.onTheMap(potLoc)) {
							
							// Add penalty for cases on edge of map (equivalent to 3 trees)
							if(!(rc.onTheMap(potLoc, 2))) {
								empty++;
								openness[k] = (float)10;
	//							openness = (float)0.5;
							} else {
								openness[k] = 0;
								// Tree weighting; radius = 1 (body) + 2 (tree)
								TreeInfo[] trees = rc.senseNearbyTrees(potLoc, radius, null);
								for (int i = 0; i < trees.length; i++) {
									if (trees[i].team == us) {
										openness[k] += 5;
									}
									else if (trees[i].location.distanceTo(curLoc) <= radiusTrees){
										openness[k] += 1;
									}
								}
								
	//							openness += 2*rc.senseNearbyTrees(potLoc, radius, null).length;
								// Unit weighting (for own Team)
	//							openness += rc.senseNearbyRobots(potLoc, radius, us).length;
								RobotInfo[] ourBots = rc.senseNearbyRobots(potLoc, radius, us);
								for (int i = 0; i < ourBots.length; i++) {
									if (ourBots[i].type == battlecode.common.RobotType.GARDENER) {
										openness[k] += 2*gardenerWeight;
									}
									else if (ourBots[i].type == battlecode.common.RobotType.ARCHON) {
										openness[k] += 1;
									}
									else {
										//openness += 0.1;
									}
								}
								// Unit weighting (for enemy Team)
								openness[k] += enemyWeight*rc.senseNearbyRobots(potLoc, radius, them).length;
								openness[k] += bulletWeight*rc.senseNearbyBullets(potLoc, radius).length;
								
								System.out.println("loc: " + potLoc + ", angle: " + (angle + 180*k) + ", openness: " + openness[k]);
	//							System.out.println("Angle: " + angle + ", openness: " + openness);
								
								// Check if every space is open
								if(openness[k] <= 1){
									empty++;
								}
							}
							
	//						System.out.println("Angle: " + angle + ", length: " + length + ", Open: " + openness);
						}
						
					}
					// Compare with existing, and replace if lower
					int index = 0;
					float lowOpen = openness[0];
					if ((openness[1] - openness[0]) > 4) {
						index = 0;
						lowOpen = openness[0] - (float)0.75*openness[1];
					}
					else if ((openness[0] - openness[1]) > 4) {
						index = 1;
						lowOpen = openness[1] - (float)0.75*openness[0];
					}
					else {
						if (openness[1] < openness[0]) {
							lowOpen = 1;
							index = 1;
						}
					}
					
					System.out.println("0: " + openness[0] + ", 1: " + openness[1]);
					
					if (lowOpen < minOpenness) {
						minOpenness = lowOpen;
						finalLoc = curLoc.add((float)Math.toRadians(angle + 180*index), length);
					}
					angle += angleInterval;
					totalAngle += angleInterval;
				}
			}
			length += lengthInterval;
		}
		
//		System.out.println("Final: " + finalLoc[0] + ", " + finalLoc[1]);
		
		if(empty == totalchecks) {
			return curLoc;
		}
		return finalLoc;
	}
	
	public static MapLocation findOptimalSpaceSweep(float angleInterval, float lengthInterval, float maxLength, float start, float radius) throws GameActionException {
		
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
		
		/*
		// Tree weighting; radius = 1 (body) + 2 (tree)
		minOpenness += rc.senseNearbyTrees(radius).length;
		// Unit weighting (for own Team)
		minOpenness += rc.senseNearbyRobots(radius, us).length;
		// Unit weighting (for enemy Team)
		minOpenness += enemyWeight*rc.senseNearbyRobots(radius, them).length;
		*/
		
		int empty = 0;
		int totalchecks = 0;
		
//		System.out.println("Max Length: " + maxLength);
		float length = lengthInterval;
		// Declare angle/length modifiers
		// Check
		while(length <= maxLength) {
			float angle = start;
			float totalAngle = 0;
			
			while(totalAngle < 181) {
				
				float[] openness = {Float.MAX_VALUE,Float.MAX_VALUE};
				
				for(int k = 1; k >= 0; k--) {
					totalchecks++;
//					System.out.println("k: " + k);
					// Get potential location
					potLoc = curLoc.add((float)Math.toRadians(angle + 180*k), length);
					
					rc.setIndicatorDot(potLoc, 178, 102, 255);
					
					
					// Check if on Map
					if(rc.onTheMap(potLoc)) {
						
						// Add penalty for cases on edge of map (equivalent to 3 trees)
						if(!(rc.onTheMap(potLoc, radius))) {
							empty++;
							openness[k] = (float)10;
//							openness = (float)0.5;
						} else {
							openness[k] = 0;
							// Tree weighting; radius = 1 (body) + 2 (tree)
							TreeInfo[] trees = rc.senseNearbyTrees(potLoc, radius, null);
							for (int i = 0; i < trees.length; i++) {
								if (trees[i].team == us) {
									openness[k] += 5;
								}
								else {
									openness[k] += 0.1;
								}
							}
							
//							openness += 2*rc.senseNearbyTrees(potLoc, radius, null).length;
							// Unit weighting (for own Team)
//							openness += rc.senseNearbyRobots(potLoc, radius, us).length;
							RobotInfo[] ourBots = rc.senseNearbyRobots(potLoc, radius, us);
							for (int i = 0; i < ourBots.length; i++) {
								if (ourBots[i].type == battlecode.common.RobotType.GARDENER) {
									openness[k] += 2*gardenerWeight;
								}
								else if (ourBots[i].type == battlecode.common.RobotType.ARCHON) {
									openness[k] += 1;
								}
								else {
									//openness += 0.1;
								}
							}
							// Unit weighting (for enemy Team)
							openness[k] += 3*enemyWeight*rc.senseNearbyRobots(potLoc, radius, them).length;
							openness[k] += 3*bulletWeight*rc.senseNearbyBullets(potLoc, radius).length;
							
//							System.out.println("loc: " + potLoc + ", angle: " + (angle + 180*k) + ", openness: " + (float)openness[k]);
//							System.out.println("Angle: " + angle + ", openness: " + openness);
							
							// Check if every space is open
							if(openness[k] <= 1){
								empty++;
							}
						}
						
//						System.out.println("Angle: " + angle + ", length: " + length + ", Open: " + openness);
					}	
					
				}
				// Compare with existing, and replace if lower
				int index = 0;
				float lowOpen = openness[0];
				if ((openness[1] - openness[0]) > 2) {
					index = 0;
					lowOpen = openness[0] - (float)0.75*openness[1];
				}
				else if ((openness[0] - openness[1]) > 2) {
					index = 1;
					lowOpen = openness[1] - (float)0.75*openness[0];
				}
				else {
					if (openness[1] < openness[0]) {
						lowOpen = openness[1];
						index = 1;
					}
				}
				
//				System.out.println("Low Open: " + lowOpen + ", Angle: " + angle + 180*index);
				
				if (lowOpen < minOpenness) {
					minOpenness = lowOpen;
					finalLoc = curLoc.add((float)Math.toRadians(angle + 180*index), length);
//					System.out.println("Adding degree: " + angle + 180*index + ", with value: " + minOpenness);
				}
				angle += angleInterval;
				totalAngle += angleInterval;
			}
			length += lengthInterval;
		}
		
//		System.out.println("Final: " + finalLoc[0] + ", " + finalLoc[1]);
		
		if(totalchecks - empty == 0) {
			return curLoc;
		}
		return finalLoc;
	}
}