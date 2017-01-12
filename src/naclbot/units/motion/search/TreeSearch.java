// This class deals with interactions between unit and neighboring trees
package naclbot;
import battlecode.common.*;

public class TreeSearch extends GlobalVars {
	
	// Returns the direction of the optimum tree
	public static Direction dirNearestTree(MapLocation[] treeLoc) {
		float eucDist = rc.getLocation().distanceTo(treeLoc[0]);
		MapLocation optimumLocation = treeLoc[0];
		for (int i = 0; i < treeLoc.length-1; i++) {
			float curDist = rc.getLocation().distanceTo(treeLoc[i]);
			if (curDist < eucDist) {
				optimumLocation = treeLoc[i];
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
		for (int i = 0; i < treeList.length-1; i++) {
			if (treeList[i].containedBullets > 0) {
				viableList[i] = treeList[i].location;				
			}
		}
		return viableList;
	}
	
}