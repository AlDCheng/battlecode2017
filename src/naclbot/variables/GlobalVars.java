package naclbot.variables;

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
	public static int SCOUT_TRACKING;
	
	public static int TANK_CHANNEL;
	
	
	public static int TREE_DATA_CHANNEL;
	public static int TREE_OFFSET;
	
	public static int GROUP_NUMBER_CHANNEL;
	public static int GROUP_CHANNEL;
	public static int GROUP_CHANNEL_OFFSET;
	public static int GROUP_COMMUNICATE_OFFSET;
	public static int GROUP_LEADER_START;
	public static int GROUP_LEADER_OFFSET;
	public static int GROUP_START;
	public static int GROUP_OFFSET;
	public static int GROUP_LIMIT;
	public static int GROUP_SIZE_LIMIT;
	
	// Internal map variables
	public static ArrayList<ArrayList<Integer>> internalMap = new ArrayList<ArrayList<Integer>>();
	public static RobotType unitType;
	public static float robotRadius;
	public static MapLocation centerCoords;
	public static int offsetX, offsetY;
	public static int SOLDIER_CHANNEL;
	
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
		
		ARCHON_OFFSET = 8;
		// Offset 1: Current X Position
		// OFfset 2: Current Y Position
		
		ARCHON_LIMIT = 5;

		// Gardeners 		
		GARDENER_CHANNEL = 100; // Carries number of living Gardeners
		GARDENER_BUILDER_CHANNEL = 101; // Carries number of living Gardeners designed as unit builders
		GARDENER_WATERER_CHANNEL = 102; // Carries number of living Gardeners designed as waterers
		
		// Scouts
		SCOUT_CHANNEL = 43; // Carries number of scouts
				
		SCOUT_MESSAGE_OFFSET = 11;

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
		
		SCOUT_LIMIT= 5; // Limit to number of Scouts

		SCOUT_UPDATE_FREQUENCY = 4; // How often Scouts regularly display that they are alive
		
		SCOUT_TRACKING = SCOUT_CHANNEL + SCOUT_LIMIT * SCOUT_UPDATE_FREQUENCY + 1;
		
		LUMBERJACK_CHANNEL = 140;
		
		/* Scout Channel is the placeholding value foir all scout channels. 
		 * The broadcasts at this number contain only the number of scouts currently available to the team		
		*/
		TANK_CHANNEL = 180;
		
		SOLDIER_CHANNEL = 160;
		GROUP_NUMBER_CHANNEL = 200;
		GROUP_CHANNEL = 201;
		GROUP_COMMUNICATE_OFFSET =2;
		GROUP_CHANNEL_OFFSET = 20;
		GROUP_LIMIT = 5;
		GROUP_SIZE_LIMIT = 12;
		
		GROUP_LEADER_OFFSET = 2;
		GROUP_LEADER_START = GROUP_CHANNEL + GROUP_COMMUNICATE_OFFSET * GROUP_LIMIT;
		GROUP_START = GROUP_LEADER_START + GROUP_LEADER_OFFSET * GROUP_LIMIT;
		GROUP_OFFSET = 20;

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
	
	// need radius
	// treeSpec format [0] x; [1] y; [2]; r
	public static void updateMapTrees(float[][] treeSpecs) {
		
		// Get offset of object position to origin (centerCoords)
		for (int k = 0; k < treeSpecs.length; k++) {
			// Get tree properties from ID
			try {
				
				// Calculate displacement from origin
				float newObjOffsetX = treeSpecs[k][0] - centerCoords.x;
				float newObjOffsetY = treeSpecs[k][1] - centerCoords.y; 
				
				// Convert raw offset to tiles
				// Each tile is the same width as the unit creating this map
				int tileOffsetCenterX = (int)(newObjOffsetX/robotRadius);
				int tileOffsetCenterY = (int)(newObjOffsetY/robotRadius);
				
				// Calculate radius of object in grid
				// We will fill with square hitbox for now
				int tileRadius = (int)(treeSpecs[k][2]/robotRadius);
				
				// Loop to fill all tiles covered by radius
				for (int tileOffsetX = tileOffsetCenterX-tileRadius; 
						tileOffsetX <= tileOffsetCenterX+tileRadius; tileOffsetX++) {
					for (int tileOffsetY = tileOffsetCenterY-tileRadius; 
							tileOffsetY <= tileOffsetCenterY+tileRadius; tileOffsetY++) {

						// Insert tree in map by dynamically resizing it
						// i = row; j = col
						// Case 1: X position (extend ArrayList)
						// - Condition 1: left of origin (-offset_x)
						if ((tileOffsetX-offsetX) < 0) {
							//System.out.println("Condition 1");
							// Pad 0s to map for each row
							for (int i = 0; i < internalMap.size(); i++) {
								for (int j = 0; j < (-1*(tileOffsetX-offsetX)); j++) {
									internalMap.get(i).add(0, 0);
								}
							}
							// Set offset from original origin
							// i.e. offsetX = -2 means (0 - (-2))=2 gets location of origin
							offsetX += tileOffsetX-offsetX;
						}
						// - Condition 2: right of internal map boundaries
						// Pad 0s to map for each row
						else if ((tileOffsetX-offsetX) > internalMap.get(0).size()-1) {
							for (int i = 0; i < internalMap.size(); i++) {
								for (int j = 0; j < ((tileOffsetX-offsetX)); j++) {
									internalMap.get(i).add(0);
								}
							}
						}
						
						// Case 2: Y position (create new ArrayList)
						// - Condition 1: above the origin (-offset_y)
						ArrayList<Integer> newRow = new ArrayList<Integer>();
						if ((tileOffsetY-offsetY) < 0) {
							// Pad 0s until point
							for (int j = 0; j < internalMap.get(0).size(); j++) {
								newRow.add(0);
							}
							for (int i = 0; i < (-1*(tileOffsetY-offsetY))-1; i++) {
								ArrayList<Integer> newRowUnlinked = new ArrayList<Integer>(newRow);
								internalMap.add(0, newRowUnlinked);
							}
							
							//Add row
							ArrayList<Integer> insertRow = new ArrayList<Integer>(newRow);
							insertRow.set((tileOffsetX-offsetX), 1);
							internalMap.add(0, insertRow);
							
							// Set offset from original origin
							// i.e. offsetY = -2 means (0 - (-2))=2 gets location of origin
							offsetY += (tileOffsetY-offsetY);
						}
						// - Condition 2: below the internal map boundaries
						else if ((tileOffsetY-offsetY) > internalMap.size()-1) {
							// Pad 0s until point
							for (int j = 0; j < internalMap.get(0).size(); j++) {
								newRow.add(0);
							}
							for (int i = 0; i < ((tileOffsetY-offsetY))-1; i++) {
								ArrayList<Integer> newRowUnlinked = new ArrayList<Integer>(newRow);
								internalMap.add(newRowUnlinked);
							}
							
							//Add row
							ArrayList<Integer> insertRow = new ArrayList<Integer>(newRow);
							insertRow.set((tileOffsetX-offsetX), 1);
							internalMap.add(insertRow);
						}
						// - Condition 3: within internal map boundaries
						else
						{
							internalMap.get(tileOffsetY-offsetY).set((tileOffsetX-offsetX), 1);
						}
						
					}
				}
				
			} catch(Exception e) {
				System.out.println("InternalMapTreeAdd: TreeInfo returns error");
				e.printStackTrace();
			}
		}
	}
	
	
	// Checks to see if an array of integers contains the integer value and outputs true if so; outputs false otherwise
	public static boolean arrayContainsInt(int[] array, int value){
		for (int i = 0; i < array.length; i ++){
			 if (array[i] == value){
				 return true;
			 }
		}
		return false;
	}			
	
	// Checks to see if an array of integers contains the integer value and outputs the index of the value if it exists or -1 if it doesn't
	public static int arrayContainsIndex(int[] array, int value){
		int j = -1;
		for (int i = 0; i < array.length; i ++){
			 if (array[i] == value){
				 j = i;
			 }
		}
		return j;
	}			
	// Heap useful for finding the maximum for minimum negate all input values
	
	
	public static class Tuple{ 
		public final int X; 
		public final int Y; 
		public Tuple(int x, int y) { 
			this.X = x; 
		    this.Y = y; 
		} 
		public void printData(){
			System.out.print( "X: "+ X + "Y: "+ Y);
			System.out.println();
			
		}
	} 

}





