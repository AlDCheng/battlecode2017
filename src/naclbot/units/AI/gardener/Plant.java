package naclbot.units.AI.gardener;
import battlecode.common.*;
import naclbot.units.motion.*;
import naclbot.units.motion.search.TreeSearch;
import naclbot.variables.GlobalVars;
import java.util.ArrayList;

public class Plant extends GlobalVars {
	
	public static void plantToLocation(MapLocation potentialPlantLoc) {
		//makes gardeners walk to left of potentialPlantLoc and plant a tree there
		float distanceFromTree = 1 + GameConstants.BULLET_TREE_RADIUS;
		MapLocation gardenerLocation = potentialPlantLoc.add(Direction.getWest(),distanceFromTree);
		Direction dirToPlantLocation = rc.getLocation().directionTo(gardenerLocation);
		if (rc.getLocation() == gardenerLocation) {
			rc.plantTree(Direction.getEast());
		}
		else {
			Move.tryMove(dirToPlantLocation);
		}
	}
	
	public static ArrayList<MapLocation> possibleNeighborLocations(MapLocation treeLocation, float radius) {
		ArrayList<MapLocation> possibleLocations = new ArrayList<MapLocation>();
		ArrayList<MapLocation> existingTrees = TreeSearch.getNearbyTrees();
		Direction[] directionList = {Direction.getNorth(),Direction.getEast(),Direction.getSouth(),Direction.getWest()};
		for (int i=0; i<4; i++) {
			MapLocation possibleLoc = treeLocation.add(directionList[i], radius);
			if (!existingTrees.contains(possibleLoc)) {
			possibleLocations.add(possibleLoc); 
			}
		}
		return possibleLocations;
		
		
	}
		
}