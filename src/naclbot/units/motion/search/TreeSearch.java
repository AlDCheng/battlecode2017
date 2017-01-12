// General move function from example
package naclbot;
import battlecode.common.*;

public class TreeSearch extends GlobalVars {
	
	static int countNearbyTrees() {
		return rc.senseNearbyTrees().length;
		
	}
	/*
	static TreeInfo[] getNearbyViableTrees() {
		ArrayList<MapLocation> viableList = new ArrayList<MapLocation>();
		TreeInfo[] treeList = rc.senseNearbyTrees();
		for (TreeInfo tree: treeList) {
			if (tree.containedBullets > 0) {
				viableList.add(tree.location);
			}
		}
		return viableList;
	}
	*/
}