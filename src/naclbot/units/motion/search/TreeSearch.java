// This class deals with interactions between unit and neighboring trees
package naclbot.units.motion.search;
import battlecode.common.*;
import naclbot.variables.GlobalVars;

import java.util.ArrayList;

public class TreeSearch extends GlobalVars {
	
	// Returns the location of the optimum tree
	public static MapLocation locNearestTree(ArrayList<MapLocation> treeLoc) {
		float eucDist = rc.getLocation().distanceTo(treeLoc.get(0));
		MapLocation optimumLocation = treeLoc.get(0);
		//System.out.println("Len NT: " + treeLoc.size());
		for (int i = 0; i < treeLoc.size(); i++) {
			//System.out.println("Tree: " + treeLoc.get(0));
			float curDist = rc.getLocation().distanceTo(treeLoc.get(0));
			if (curDist < eucDist) {
				optimumLocation = treeLoc.get(0);
				eucDist = curDist;
			}
		}
		return optimumLocation;		
	}
	
	// Counts the number of nearby trees
	public static int countOwnNearbyTrees() {
		int count = 0;
		for (TreeInfo tree: rc.senseNearbyTrees()) {
			if (tree.getTeam() == rc.getTeam()) {
				count++;
			}
		}
		return count;
	}
	
	// Returns tree locations containing bullets
	public static ArrayList<MapLocation> getNearbyBulletTrees() {
		TreeInfo[] treeList = rc.senseNearbyTrees();
		ArrayList<MapLocation> viableList = new ArrayList<MapLocation>();
		for (int i = 0; i < treeList.length; i++) {
			if ((treeList[i].containedBullets > 0) && (treeList[i].getTeam() != rc.getTeam().opponent())) {
				viableList.add(treeList[i].getLocation());
				//System.out.println("Tree index " + i + ": " + treeList[i].location);
			}
		}
		//System.out.println("Len ML: " + viableList.size());
		return viableList;
	}
	
	//Returns tree locations that have less health so gardeners can water
	public static ArrayList<MapLocation> getNearbyLowTrees() {
		TreeInfo[] treeList = rc.senseNearbyTrees();
		ArrayList<MapLocation> waterList = new ArrayList<MapLocation>();
		for (int i = 0; i < treeList.length; i++) {
			if (treeList[i].health <= treeList[i].maxHealth - GameConstants.WATER_HEALTH_REGEN_RATE && treeList[i].team == rc.getTeam()) {
				waterList.add(treeList[i].getLocation());
				//System.out.println("Tree index " + i + ": " + treeList[i].location);
			}
		}
		//System.out.println("Len ML: " + waterList.size());
		return waterList;
	}
	
	public static ArrayList<MapLocation> getNearbyTrees() {
		//variation of getNearbyBulletTrees(), gets nearby tree locations 
		TreeInfo[] treeList = rc.senseNearbyTrees();
		ArrayList<MapLocation> nearList = new ArrayList<MapLocation>();
		for (int i = 0; i < treeList.length; i++) {
			nearList.add(treeList[i].getLocation());
			}
		return nearList;
	}
	
	public static ArrayList<MapLocation> getNearbyNeutralTrees() {
		//variation of getNearbyBulletTrees(), gets nearby neutral trees (used for lumberjacks)
		TreeInfo[] treeList = rc.senseNearbyTrees();
		ArrayList<MapLocation> neutralList = new ArrayList<MapLocation>();
		for (int i = 0; i < treeList.length; i++) {
			if (treeList[i].team == Team.NEUTRAL) {
				neutralList.add(treeList[i].getLocation());
				//System.out.println("Tree index " + i + ": " + treeList[i].location);
			}
		}
		return neutralList;
	}	
	
	public static ArrayList<MapLocation> getNearbyTeamTrees() {
		//variation of getNearbyBulletTrees(), gets nearby tree locations 
		TreeInfo[] treeList = rc.senseNearbyTrees();
		ArrayList<MapLocation> nearList = new ArrayList<MapLocation>();
		for (int i = 0; i < treeList.length; i++) {
			if (treeList[i].team == rc.getTeam()) {
			nearList.add(treeList[i].getLocation());
			}
		}
		return nearList;
	}
}