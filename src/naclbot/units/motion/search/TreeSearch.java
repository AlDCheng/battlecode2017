// This class deals with interactions between unit and neighboring trees
package naclbot;
import battlecode.common.*;

public class TreeSearch extends GlobalVars {
	
	// Returns the direction of the optimum tree
	public static Direction dirNearestTree() {
		ArrayList<MapLocation> treeList = rc.senseNearbyTrees();
		
		
		
	}
	
	// Counts the number of nearby trees
	public static int countNearbyTrees() {
		return rc.senseNearbyTrees().length;
		
	}
	
	// Returns tree locations containing bullets
	public static ArrayList<MapLocation> getNearbyBulletTrees() {
		ArrayList<MapLocation> viableList = new ArrayList<MapLocation>();
		TreeInfo[] treeList = rc.senseNearbyTrees();
		for (TreeInfo tree: treeList) {
			if (tree.containedBullets > 0) {
				viableList.add(tree.location);
			}
		}
		return viableList;
	}
	
}