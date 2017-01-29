package naclbot.units.AI.gardener;
import battlecode.common.*;
import naclbot.units.motion.*;
import naclbot.variables.GlobalVars;
import java.util.ArrayList;

public class ReLife extends GlobalVars {
	
	public static float congestion = 0;
	
	// Find largest nearby empty space for tree planting
	// Also finds an empty space for unit/tree production
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
					if(!(rc.onTheMap(potLoc, radius))) {
						openness = 18;
					} else {
						openness = 0;
						// Tree weighting; radius = 1 (body) + 2 (tree)
						float treeLen = rc.senseNearbyTrees(potLoc, radius, null).length;
						openness += treeLen;
						
						// Unit weighting (for own Team)
						openness += rc.senseNearbyRobots(potLoc, radius, us).length;
						// Unit weighting (for enemy Team)
//						openness += 3*rc.senseNearbyRobots(potLoc, radius, them).length;
						RobotInfo[] themBots = rc.senseNearbyRobots(potLoc, radius, them);
						for (int i = 0; i < themBots.length; i++) {
							if (themBots[i].type == battlecode.common.RobotType.ARCHON) {
								openness += 3;
							}
							else {
								openness += 0.1;
							}
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
		
		return finalLoc;
	}
	
	// Scans immediate radius of gardener (in degrees)
	// Output Format: [0]last availible tree plant space; [1]reserved unit building space
	public static Direction[] scanBuildRadius(float angleInterval, float start, float dist, float extend) throws GameActionException {
		congestion = 0;
		
		// Storage output
		Direction finalDir[] = new Direction[2];
		
		// Define direction variable
		Direction dir = new Direction((float)Math.toRadians(start));
		Direction dirCheck = new Direction((float)Math.toRadians(start)); 
		Direction treePosDir = null; 
		
		MapLocation curLoc = rc.getLocation();
		
		int selectiveSample = 1;
		float sampleInterval = 4;
		
		// Make sure only one revolution is checked
		float totalAngle = 0;
		
		// Scan for 1 revolution
		while (totalAngle <= 180) {
			
			// Scan both sides around
			for (int rotSwitch = -1; rotSwitch < 2; rotSwitch+=2) {
				
				// Increment values
				if (start <= 0) {
					dirCheck = dir.rotateLeftDegrees(rotSwitch * totalAngle);
				}
				else {
					dirCheck = dir.rotateRightDegrees(rotSwitch * totalAngle);
				}
//				dirCheck = new Direction((float)Math.round(dirCheck.radians * 100.0)/(float)100.0);
//				System.out.println("Dir Check: " + dirCheck);
				
				// Get location to search
				MapLocation newLoc = curLoc.add(dirCheck,dist);
				
				// Check if tree can be planted
				if(rc.canPlantTree(dirCheck)) {
					rc.setIndicatorLine(curLoc, newLoc, 0, 255, 255);
					
					// Keep one space for unit building, fill other if possible
					if ((finalDir[1] != null) && (finalDir[0] == null)) {
						if (Math.abs(finalDir[1].degreesBetween(dirCheck)) > 60-angleInterval/2) {
							finalDir[0] = treePosDir;
						}
					}
					else {
						if (treePosDir == null) {
							treePosDir = dirCheck;
						}
//						finalDir[1] = dirCheck;
						
						if(!(rc.isCircleOccupiedExceptByThisRobot(curLoc.add(dirCheck, dist),(float)0.5))) {
							if (rc.onTheMap(newLoc.add(dirCheck, extend), (float)1)) {
								finalDir[1] = dirCheck;
							}
						}
					}
				}
				else {
//					if (rc.senseTreeAtLocation(newLoc) != null) {
					if (selectiveSample >= 3) {
						if (rc.senseNearbyTrees(newLoc,(float)0.5,Team.NEUTRAL).length > 0) {
							congestion += (sampleInterval+1)*(float)angleInterval/(float)360.0;
							rc.setIndicatorLine(curLoc, newLoc, 173, 255, 47);
						}
					}
					else {
//						rc.setIndicatorLine(curLoc, newLoc, 255, 128, 0);
					}
				}
			}
			selectiveSample++;
			if (selectiveSample > 3) {
				selectiveSample = 0;
			}
			totalAngle += angleInterval;
			
//			System.out.println("Angle: " + dir + ", Total Angle: " + totalAngle);
		}
		
		return finalDir;
	}
	
	// Scans immediate radius for building Tank
	public static Direction scanBuildRadiusTank(float angleInterval, float start) throws GameActionException {
		
		// Storage output
		Direction finalDir = null;
		
		// Define direction variable
		Direction dir = new Direction((float)Math.toRadians(start));
		
		MapLocation curLoc = rc.getLocation();
		
		// Make sure only one revolution is check
		float totalAngle = 0;
		
		// Scan for 1 revolution
		while (totalAngle < 180) {
			
			// Scan both sides around
			for (int rotSwitch = -1; rotSwitch < 1; rotSwitch+=2) {
				
				// Increment values
				if (start <= 0) {
					dir = dir.rotateLeftDegrees(angleInterval);
				}
				else {
					dir = dir.rotateRightDegrees(angleInterval);
				}
				
				// Get location to search
				MapLocation newLoc = curLoc.add(dir,2);
				
				// Check if tree can be planted
				if(rc.canBuildRobot(RobotType.TANK, dir)) {
//					rc.setIndicatorLine(curLoc, newLoc, 0, 255, 255);
					if(!(rc.isCircleOccupied(newLoc.add(dir, 2),(float)2)) && (rc.onTheMap(newLoc.add(dir, 2), (float)2))) {
						finalDir = dir;
					}
				}				
				
			}
			totalAngle += angleInterval;	
		}
		
		return finalDir;
	}
}