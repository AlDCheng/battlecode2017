package naclbot;
import battlecode.common.*;
import java.util.ArrayList;

public class GlobalVars {
	public static RobotController rc;
	public static int ARCHON_CHANNEL;
	public static int ARCHON_OFFSET;
	public static int ARCHON_LIMIT;
	
	public static int GARDENER_CHANNEL;
	public static int GARDENER_BUILDER_CHANNEL;
	public static int GARDENER_WATERER_CHANNEL;
	
	public static int LUMBERJACK_CHANNEL;	
	
	public static int SCOUT_CHANNEL;
	public static int SCOUT_MESSAGE_OFFSET;
	public static int SCOUT_LIMIT;
	public static int SCOUT_UPDATE_FREQUENCY;
	
	public static int TANK_CHANNEL;
	
	
	public static int TREE_DATA_CHANNEL;
	public static int TREE_OFFSET;
	public static int GROUP_CHANNEL;
	public static int GROUP_CHANNEL_OFFSET;
	
	// Internal map variables
	public static ArrayList<ArrayList<Integer>> internalMap = new ArrayList<ArrayList<Integer>>();
	public static RobotType unitType;
	public static float robotRadius;
	public static MapLocation centerCoords;
	public static int offsetX, offsetY;
	
	public static void globalInit(RobotController _RC) {
		rc = _RC;
		
		/* --------------------------------------------------------------------
		 * -------------------------- Internal Map ----------------------------
		-------------------------------------------------------------------- */
		unitType = rc.getType();
		robotRadius = unitType.bodyRadius;
		
		centerCoords = rc.getLocation();
		offsetX = 0;
		offsetY = 0;
		
		/* --------------------------------------------------------------------
		 * --------- Broadcast Channel Setup and Unit Organisation -----------
		-------------------------------------------------------------------- */
		
		// Archons
		
		ARCHON_CHANNEL = 0; // Carries number of living Archons
		
		ARCHON_OFFSET = 5;
		// Offset 1: Current X Position
		// OFfset 2: Current Y Position
		
		ARCHON_LIMIT = 3;

		// Gardeners 		
		GARDENER_CHANNEL = 20; // Carries number of living Gardeners
		GARDENER_BUILDER_CHANNEL = 21; // Carries number of living Gardeners designed as unit builders
		GARDENER_WATERER_CHANNEL = 22; // Carries number of living Gardeners designed as waterers
		
		// Scouts
		SCOUT_CHANNEL = 45; // Carries number of scouts
		
		SCOUT_MESSAGE_OFFSET = 10;
		// Offset 1:  Current X Position
		// Offset 2:  Current Y Position
		// Offset 3-8:  Message bits
		// Offset 9: ID Broadcast
		// Offset 10: Message type identifier
			// Type 0: Clear Message only ID and 0 type transmit - means ignore everything  to some functions
			// Type 1: Regular transmission of location/id/nearest 
			// Type 2: Transmission of sudoku - many enemies here
			// Type 3: Transmission of tree data
			// Type 4: Update of tracked object
			// ...
		
		SCOUT_LIMIT=5; // Limit to number of Scouts

		SCOUT_UPDATE_FREQUENCY = 4; // How often Scouts regularly display that they are alive
		
		LUMBERJACK_CHANNEL = 100;
		
		/* Scout Channel is the placeholding value foir all scout channels. 
		 * The broadcasts at this number contain only the number of scouts currently available to the team		
		*/
		TANK_CHANNEL = 115;
		
		GROUP_CHANNEL = 150;
		GROUP_CHANNEL_OFFSET = 20;
		
		TREE_DATA_CHANNEL = 400; 
		TREE_OFFSET = 4;
		//Offset 0: Tree ID
		//Offset 2: Tree X Position
		//Offset 3: Tree Y Position
		//Offset 4:  Something Else
	}
	
	// Updates map for trees
	public static void updateMapTrees(MapLocation[] coords) {
		for (int i = 0; i < coords.length-1; i++) {
			float newObjOffsetX = coords[i].x - centerCoords.x;
			float newObjOffsetY = coords[i].y - centerCoords.y;
			
		}
	}

}
