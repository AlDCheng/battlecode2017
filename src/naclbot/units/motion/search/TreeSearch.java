// This class deals with interactions between unit and neighboring trees
package naclbot;
import battlecode.common.*;
import java.util.ArrayList;

public class TreeSearch extends GlobalVars {
	
	// Returns the location of the optimum tree
	public static MapLocation locNearestTree(ArrayList<MapLocation> treeLoc) {
		float eucDist = rc.getLocation().distanceTo(treeLoc.get(0));
		MapLocation optimumLocation = treeLoc.get(0);
		System.out.println("Len NT: " + treeLoc.size());
		for (int i = 0; i < treeLoc.size()-1; i++) {
			System.out.println("Tree: " + treeLoc.get(0));
			float curDist = rc.getLocation().distanceTo(treeLoc.get(0));
			if (curDist < eucDist) {
				optimumLocation = treeLoc.get(0);
				eucDist = curDist;
			}
		}
		return optimumLocation;		
	}
	
	// Counts the number of nearby trees
	public static int countNearbyTrees() {
		return rc.senseNearbyTrees().length;
	}
	
	// Returns tree locations containing bullets
	public static ArrayList<MapLocation> getNearbyBulletTrees() {
		TreeInfo[] treeList = rc.senseNearbyTrees();
		ArrayList<MapLocation> viableList = new ArrayList<MapLocation>();
		for (int i = 0; i < treeList.length-1; i++) {
			if ((treeList[i].containedBullets > 0) && (treeList[i].getTeam() != rc.getTeam().opponent())) {
				viableList.add(treeList[i].location);
				System.out.println("Tree index " + i + ": " + treeList[i].location);
			}
		}
		System.out.println("Len ML: " + viableList.size());
		return viableList;
	}
	
	//Returns tree locations below 60% health
	public static ArrayList<MapLocation> getNearbyLowTrees() {
		TreeInfo[] treeList = rc.senseNearbyTrees();
		ArrayList<MapLocation> waterList = new ArrayList<MapLocation>();
		for (int i = 0; i < treeList.length-1; i++) {
			float percentageHealth = treeList[i].health/treeList[i].maxHealth;
			if (percentageHealth < 0.6 && treeList[i].team == rc.getTeam()) {
				waterList.add(treeList[i].location);
				System.out.println("Tree index " + i + ": " + treeList[i].location);
			}
		}
		System.out.println("Len ML: " + waterList.size());
		return waterList;
	}
	
}