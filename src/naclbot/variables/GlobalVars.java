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
		ArrayList<Integer> zeroList = new ArrayList<Integer>();
		zeroList.add(0);
		internalMap.add(zeroList);
		
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
	// 0 = empty
	// 1 = tree
	// 2 = friendly unit
	// 3 = enemy unit
	public static void updateMapTrees(int[] treeID) {
		
		// Get offset of object position to origin (centerCoords)
		for (int k = 0; k < treeID.length-1; k++) {
			// Get tree properties from ID
			try {
				TreeInfo treeSpecs = rc.senseTree(treeID[k]);
				
				// Calculate displacement from origin
				float newObjOffsetX = treeSpecs.location.x - centerCoords.x;
				float newObjOffsetY = treeSpecs.location.y - centerCoords.y;
				
				// Convert raw offset to tiles
				// Each tile is the same width as the unit creating this map
				int tileOffsetX = (int)(newObjOffsetX/robotRadius);
				int tileOffsetY = (int)(newObjOffsetY/robotRadius);
				
				// Insert tree in map by dynamically resizing it
				// i = row; j = col
				// Case 1: X position (extend ArrayList)
				// - Condition 1: left of origin (-offset_x)
				if ((tileOffsetX-offsetX) < 0) {
					// Pad 0s to map for each row
					for (int i = 0; i < internalMap.size()-1; i++) {
						for (int j = 0; j < (-1*tileOffsetY); j++) {
							internalMap.get(i).add(0, 0);
						}
					}
					// Set offset from original origin
					// i.e. offsetX = -2 means (0 - (-2))=2 gets location of origin
					offsetX -= (tileOffsetX-offsetX);
				}
				// - Condition 2: right of internal map boundaries
				// Pad 0s to map for each row
				else if ((tileOffsetX-offsetX) > internalMap.size()-1) {
					for (int i = 0; i < internalMap.size()-1; i++) {
						for (int j = 0; j < (-1*tileOffsetY); j++) {
							internalMap.get(i).add(0);
						}
					}
				}
				
				// Case 2: Y position (create new ArrayList)
				// - Condition 1: above the origin (-offset_y)
				ArrayList<Integer> newRow = new ArrayList<Integer>();
				if ((tileOffsetY-offsetY) < 0) {
					// Pad 0s until point
					for (int j = 0; j < internalMap.size()-1; j++) {
						newRow.add(0);
					}
					for (int i = 0; i < (-1*(tileOffsetY-offsetY))-1; i++) {
						internalMap.add(0, newRow);
					}
					
					//Add row
					newRow.set((tileOffsetX-offsetX), 1);
					internalMap.add(0, newRow);
					
					// Set offset from original origin
					// i.e. offsetY = -2 means (0 - (-2))=2 gets location of origin
					offsetY -= (tileOffsetY-offsetY);
				}
				// - Condition 2: below the internal map boundaries
				else if ((tileOffsetY-offsetY) > internalMap.size()-1) {
					// Pad 0s until point
					for (int j = 0; j < internalMap.size()-1; j++) {
						newRow.add(0);
					}
					for (int i = 0; i < (-1*(tileOffsetY-offsetY))-1; i++) {
						internalMap.add(newRow);
					}
					
					//Add row
					newRow.set((tileOffsetX-offsetX), 1);
					internalMap.add(newRow);
				}
				// - Condition 3: within internal map boundaries
				else
				{
					internalMap.get(tileOffsetY-offsetY).set((tileOffsetX-offsetX), 1);
				}
			} catch(GameActionException e) {
				System.out.println("InternalMapTreeAdd: TreeInfo returns error");
			}
		}
	}

}
