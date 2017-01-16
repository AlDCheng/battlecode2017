package naclbot.variables;
import battlecode.common.*;
import java.util.ArrayList;

public class ArchonVars extends GlobalVars {
	// Get number of units originating from given Archon
	public static int numGardeners;
	public static int numLumberjacks;
	public static int numScouts;
	public static int numSoldiers;
	public static int numTanks;
	
	// Array of all team broadcasts (not possible?)
	//public static MapLocation[] teamBroadcasts;
	
	// Initialize variables
	public static void archonVarInit() {
		numGardeners = 0;
		numLumberjacks = 0;
		numScouts = 0;
		numSoldiers = 0;
		numTanks = 0;
		//teamBroadcasts = new MapLocation[0];
	}
	
	// Examine each position in broadcast
	public static ArrayList<MapLocation> enemyBroadcasts(MapLocation[] allBroadcasts) {
		ArrayList<MapLocation> enemyUnitLoc = new ArrayList<MapLocation>();
		for(int i = 0; i < allBroadcasts.length; i++) {
			try {
				rc.senseRobotAtLocation(allBroadcasts[i]);
			} catch (GameActionException e) {
				enemyUnitLoc.add(allBroadcasts[i]);
			}
		}
		
		return enemyUnitLoc;
	}
}
