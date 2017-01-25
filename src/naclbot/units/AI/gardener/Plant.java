package naclbot.units.AI.gardener;
import battlecode.common.*;
import naclbot.units.motion.*;
import naclbot.units.motion.search.TreeSearch;
import naclbot.variables.GlobalVars;
import java.util.ArrayList;

public class Plant extends GlobalVars {
	
	public static float congestion = 0;
	
	public static void plantToLocation(MapLocation potentialPlantLoc, Direction dirToPlant) {
		try {
			System.out.println(rc.getID() + " " + 2);
			
			//makes gardeners walk to left of potentialPlantLoc and plant a tree there
			float distanceFromTree = 1 + GameConstants.BULLET_TREE_RADIUS; //distance between center of gardener and center of tree being planted
			MapLocation gardenerLocation = potentialPlantLoc.subtract(dirToPlant, distanceFromTree);
			Direction dirToPlantLocation = rc.getLocation().directionTo(gardenerLocation); 
			System.out.println("isbuildready: " + rc.isBuildReady());
			
			while (!rc.getLocation().equals(gardenerLocation) || !rc.isBuildReady() || !rc.canPlantTree(dirToPlant)) {
				//not at location so move toward the gardenerLocation
				System.out.println(rc.getLocation().y + " " + gardenerLocation.y);
				float distanceToLoc = rc.getLocation().distanceTo(gardenerLocation);
				if (distanceToLoc <= 1 && rc.canMove(dirToPlantLocation,distanceToLoc)) {
					rc.move(dirToPlantLocation,distanceToLoc);
				} else {
					try {
						if (rc.canMove(dirToPlantLocation)) {
							Move.tryMove(dirToPlantLocation);
						}
					} catch(GameActionException e) {
						e.printStackTrace();
					}
				}
				Clock.yield();
				
			}
			System.out.println("teambullets:" + rc.getTeamBullets());
			rc.plantTree(dirToPlant);
		} catch(GameActionException e) {
			e.printStackTrace();
		}

	}
	
	public static MapLocation[] possibleNeighborLocations(MapLocation treeLocation) {
		// generates MapLocations where trees can be planted to make an organized grid
		MapLocation[] possibleLocations = new MapLocation[3];
		ArrayList<MapLocation> existingTrees = TreeSearch.getNearbyTrees();
		float spacing = (float) 1/10;
		try {
			/*
			Direction[] directionList = {Direction.getNorth(),Direction.getEast(),Direction.getSouth(),Direction.getWest()};
			for (int i=0; i<4; i++) {
				MapLocation possibleLoc = treeLocation.add(directionList[i], radius); //generates possible locations we can plant a tree
				if (!existingTrees.contains(possibleLoc)) {
				//if possible location is not already occupied by a tree
				possibleLocations.add(possibleLoc); 
				}
			*/
			possibleLocations[0] = treeLocation.add(Direction.getEast(),2*GameConstants.BULLET_TREE_RADIUS+spacing);
			possibleLocations[1] = possibleLocations[0].add(Direction.getSouth(),2*GameConstants.BULLET_TREE_RADIUS+spacing);
			possibleLocations[2] = treeLocation.add(Direction.getSouth(),2*GameConstants.BULLET_TREE_RADIUS+spacing);

			
		} catch(Exception e) {
			e.printStackTrace();
		}
		return possibleLocations;
		
		
	}
	
	public static void plantCluster() throws GameActionException {
		MapLocation[] possibleTreeLocations;
		if (rc.canPlantTree(Direction.getSouth())) {
			System.out.println(rc.getID() + " " + 1);
			rc.plantTree(Direction.getSouth());
			possibleTreeLocations = Plant.possibleNeighborLocations(rc.getLocation().add(Direction.getSouth(),1+GameConstants.BULLET_TREE_RADIUS));
			Plant.plantToLocation(possibleTreeLocations[0],Direction.getSouth());
			Plant.plantToLocation(possibleTreeLocations[1],Direction.getNorth());
			Plant.plantToLocation(possibleTreeLocations[2],Direction.getNorth());
		}
	}
	
	public static Direction[] generateHexagonalDirections() {
		Direction[] outputArray = new Direction[6];
		outputArray[0] = Direction.getEast();
		outputArray[1] = outputArray[0].rotateLeftDegrees(60);
		outputArray[2] = outputArray[1].rotateLeftDegrees(60);
		outputArray[3] = outputArray[2].rotateLeftDegrees(60);
		outputArray[4] = outputArray[3].rotateLeftDegrees(60);
		outputArray[5] = outputArray[4].rotateLeftDegrees(60);
		
		return outputArray;
	}
	
	public static boolean checkNearbyTreesAndArchons(float radius) {
		RobotInfo[] nearbyRobots = rc.senseNearbyRobots(radius);
		ArrayList<MapLocation> nearbyTeamTrees = TreeSearch.getNearbyTeamTrees(5);
		
		if (nearbyTeamTrees.size() > 0) {
			return true;
		}
		for (RobotInfo robot: nearbyRobots) {
			if (robot.getType() == RobotType.ARCHON) {
				return true;
			}
		}
		return false;
	}
	
	// These functions are added by Alan
	
	// Find largest nearby empty space for tree planting
	// Also finds an empty space for unit/tree production
	public static MapLocation findOptimalSpace(float angleInterval, float lengthInterval, float maxLength, float start) throws GameActionException {
		
//		System.out.println("Angle Interval: " + angleInterval + ", Length Interval: " + lengthInterval);
		
		congestion = 0;
		
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
		float radius = 3;
		
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
					if(!(rc.onTheMap(potLoc, 3))) {
						openness = 18;
					} else {
						openness = 0;
						// Tree weighting; radius = 1 (body) + 2 (tree)
						float treeLen = rc.senseNearbyTrees(potLoc, radius, null).length;
						openness += treeLen;
						if(treeLen > 0) {
							if (rc.senseNearbyTrees(potLoc, radius, Team.NEUTRAL).length > 0) {
								congestion += angleInterval/(float)360.0;
							}
						}
						
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
	public static Direction[] scanBuildRadius(float angleInterval, float start) throws GameActionException {
		
		congestion = 0;
		
		// Storage output
		Direction finalDir[] = new Direction[2];
		
		// Define direction variable
		Direction dir = new Direction((float)Math.toRadians(start));
		
		MapLocation curLoc = rc.getLocation();
		
		// Make sure only one revolution is check
		float totalAngle = 0;
		
		// Scan for 1 revolution
		while (totalAngle < 360) {
			
			MapLocation newLoc = curLoc.add(dir,2);
			// Check if tree can be planted
			if(rc.canPlantTree(dir)) {
				rc.setIndicatorLine(curLoc, newLoc, 0, 255, 255);
				// Keep one space for unit building, fill other if possible
				if (finalDir[1] != null) {
					if (Math.abs(finalDir[1].degreesBetween(dir)) > 60) {
						finalDir[0] = finalDir[1];
					}
				}
				else {
					if (rc.senseTreeAtLocation(newLoc) != null) {
						congestion += angleInterval/(float)360.0;
					}
					if((!(rc.isCircleOccupied(newLoc.add(dir, 1),(float)0.5))) && (rc.onTheMap(newLoc.add(dir, 1), (float)1))) {
						finalDir[1] = dir;
					}
				}
			}
			else {
				rc.setIndicatorLine(curLoc, newLoc, 255, 128, 0);
				/*if ((finalDir[0] != null) && (finalDir[1] != null)) {
					break;
				}*/
			}
			
			// Increment values
			if (start <= 0) {
				dir = dir.rotateLeftDegrees(angleInterval);
			}
			else {
				dir = dir.rotateRightDegrees(angleInterval);
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
		while (totalAngle < 360) {
			
			MapLocation newLoc = curLoc.add(dir,2);
			// Check if tree can be planted
			if(rc.canBuildRobot(RobotType.TANK, dir)) {
//				rc.setIndicatorLine(curLoc, newLoc, 0, 255, 255);
				if(!(rc.isCircleOccupied(newLoc.add(dir, 2),(float)2)) && (rc.onTheMap(newLoc.add(dir, 2), (float)2))) {
					finalDir = dir;
				}
			}
			else {
//				rc.setIndicatorLine(curLoc, newLoc, 255, 128, 0);
				/*if ((finalDir[0] != null) && (finalDir[1] != null)) {
					break;
				}*/
			}
			
			// Increment values
			if (start <= 0) {
				dir = dir.rotateLeftDegrees(angleInterval);
			}
			else {
				dir = dir.rotateRightDegrees(angleInterval);
			}
			totalAngle += angleInterval;
			
//				System.out.println("Angle: " + dir + ", Total Angle: " + totalAngle);
		}
		
		return finalDir;
	}
}