// This class deals with interactions between unit and neighboring trees
package naclbot;
import battlecode.common.*;

public class TreeSearch extends GlobalVars {
	
	// Returns the direction of the optimum tree
	public static Direction dirNearestTree(MapLocation[] treeLoc) {
		float eucDist = rc.getLocation().distanceTo(treeLoc[0]);
		MapLocation optimumLocation = treeLoc[0];
		for (MapLocation loc: treeLoc) {
			float curDist = rc.getLocation().distanceTo(loc);
			if (curDist < eucDist) {
				optimumLocation = loc;
				eucDist = curDist;
			}
		}
		return rc.getLocation().directionTo(optimumLocation);		
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