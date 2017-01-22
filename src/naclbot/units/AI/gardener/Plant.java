package naclbot.units.AI.gardener;
import battlecode.common.*;
import naclbot.units.motion.*;
import naclbot.units.motion.search.TreeSearch;
import naclbot.variables.GlobalVars;
import java.util.ArrayList;

public class Plant extends GlobalVars {
	
	public static void plantToLocation(MapLocation potentialPlantLoc, Direction dirToPlant) {
		try {
			System.out.println(rc.getID() + " " + 2);
			//makes gardeners walk to left of potentialPlantLoc and plant a tree there
			float distanceFromTree = 1 + GameConstants.BULLET_TREE_RADIUS; //distance between center of gardener and center of tree being planted
			MapLocation gardenerLocation = potentialPlantLoc.subtract(dirToPlant,distanceFromTree);
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
		TreeInfo[] nearbyTrees = rc.senseNearbyTrees(radius);
		
		if (nearbyTrees.length > 0) {
			return true;
		}
		for (RobotInfo robot: nearbyRobots) {
			if (robot.getType() == RobotType.ARCHON) {
				return true;
			}
		}
		return false;
	}

		
}