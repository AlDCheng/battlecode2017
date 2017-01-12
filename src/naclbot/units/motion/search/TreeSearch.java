// This class deals with interactions between unit and neighboring trees
package naclbot;
import battlecode.common.*;

public class TreeSearch extends GlobalVars {
	
	// Returns the direction of the optimum tree
	public static Direction dirNearestTree() {
		MapLocation[] treeList = rc.senseNearbyTrees();
		
	}
	
	// Counts the number of nearby trees
	public static int countNearbyTrees() {
		return rc.senseNearbyTrees().length;
		
	}
	
	// Returns tree locations containing bullets
	public static MapLocation[] getNearbyBulletTrees() {
		TreeInfo[] treeList = rc.senseNearbyTrees();
		MapLocation[] viableList = new MapLocation[treeList.length];
		int i = 0;
		for (TreeInfo tree: treeList) {
			if (tree.containedBullets > 0 && tree.getTeam() == rc.getTeam() && tree.getTeam() == Team.valueOf("NEUTRAL")) {
				viableList[i] = tree.location;
			}
		}
		return viableList;
	}
	
}