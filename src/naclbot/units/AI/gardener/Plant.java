package naclbot.units.AI.gardener;
import battlecode.common.*;
import naclbot.units.motion.*;
import naclbot.units.motion.search.TreeSearch;
import naclbot.variables.GlobalVars;
import java.util.ArrayList;

public class Plant extends GlobalVars {
	
	public static void plantToLocation(MapLocation potentialPlantLoc) {
		try {
			//makes gardeners walk to left of potentialPlantLoc and plant a tree there
			float distanceFromTree = 1 + GameConstants.BULLET_TREE_RADIUS; //distance between center of gardener and center of tree being planted
			MapLocation gardenerLocation = potentialPlantLoc.add(Direction.getWest(),distanceFromTree);
			Direction dirToPlantLocation = rc.getLocation().directionTo(gardenerLocation); 
			if (rc.getLocation() == gardenerLocation) {
				//at location so can plant 
				rc.plantTree(Direction.getEast());
			}
			else {
				//not at location so move toward the gardenerLocation
				Move.tryMove(dirToPlantLocation);
			}
		} catch(GameActionException e) {
			e.printStackTrace();
		}
	}
	
	public static ArrayList<MapLocation> possibleNeighborLocations(MapLocation treeLocation, float radius) {
		// generates MapLocations where trees can be planted to make an organized grid
		ArrayList<MapLocation> possibleLocations = new ArrayList<MapLocation>();
		ArrayList<MapLocation> existingTrees = TreeSearch.getNearbyTrees();
		
		try {
			Direction[] directionList = {Direction.getNorth(),Direction.getEast(),Direction.getSouth(),Direction.getWest()};
			for (int i=0; i<4; i++) {
				MapLocation possibleLoc = treeLocation.add(directionList[i], radius); //generates possible locations we can plant a tree
				if (!existingTrees.contains(possibleLoc)) {
				//if possible location is not already occupied by a tree
				possibleLocations.add(possibleLoc); 
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return possibleLocations;
		
		
	}
		
}