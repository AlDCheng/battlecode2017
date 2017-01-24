package naclbot.variables;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

/* ------------------   Overview ----------------------
 * 
 * Various constants controlling broadcasting hierarchy.....
 *
 * ~~ Coded by Illiyia (akimn@#mit.edu)
 * 
 ---------------------------------------------------- */

// Enumeration of all broadcast channels that we will be using
public class BroadcastChannels extends GlobalVars {
	
	// ---------------------- TEAM UNIT INFORMATION ----------------------//
	
	// Channel to show how many units have been produced thus far........
	public final static int UNIT_NUMBER_CHANNEL = 9999;	
	
	// ----------------------- ARCHON INFORMATION -------------------------//
	
	// Stores how many archons are currently in the game
	public final static int ARCHON_NUMBER_CHANNEL = 0;
	
	// ----------------------- SCOUT INFORMATION -------------------------//
	
	// Stores how many gardeners  exist
	public final static int GARDENER_NUMBER_CHANNEL = 9991;
	
	// ----------------------- SCOUT INFORMATION -------------------------//
	
	// Stores how many lumber jacks exist
	public final static int LUMBERJACK_NUMBER_CHANNEL = 9992;
	 

	// ----------------------- SCOUT INFORMATION -------------------------//
	
	// Stores how many scouts and harvest scouts there are
	public final static int SCOUT_NUMBER_CHANNEL = 9995;
	
	public final static int SCOUT_HARVESTER_NUMBER_CHANNEL = 9996;
	
	// ---------------------- SOLDIER INFORMATION -------------------------//
	
	// Stores how many channels have been updated with information this turn
	public final static int SOLDIER_NUMBER_CHANNEL = 9993;
	
	// ---------------------- SOLDIER INFORMATION -------------------------//
	
	// Stores how many channels have been updated with information this turn
	public final static int TANK_NUMBER_CHANNEL = 9994;
		
	
	// ----------------------- TREE INFORMATION -------------------------//
	
	// STores how many trees have been updated this turn.... (should be sent by the first scout)
	public final static int TREES_SENT_THIS_TURN_CHANNEL = 2000;
	public final static int TOTAL_TREE_NUMBER_CHANNEL = 2001;
	public final static int TREE_DATA_START_CHANNEL = 2002;
	
	// ------------------------  ENEMY INFORMATION ----------------------//
	
	// Enemy Information from Scouts
		// Bit 0 - Gives Enemy ID
		// Bit 1 - Gives Enemy X
		// Bit 2 - Gives Enemy Y
		// BIT 3 - Gives Enemy Type
			// Type Conversion: 
			// Type 0 - Archon
			// Type 1 - Gardener
			// Type 2 - SCout
			// Type 3 - Soldier
			// Type 4 - Lumberjack
			// Type 5 - Tank	
	
	// Stores whoever last updated the channel (their scout number)...	
	public final static int LAST_UPDATER_ENEMY_LOCATIONS = 1000;
	
	// Stores how many channels have been updated with information this turn
	public final static int ENEMY_LOCATIONS_SENT_THIS_TURN_CHANNEL = 1001;
	
	// Placeholder for however many enemy locations we would like to store - indexes which ones to read
	public final static int ENEMY_LOCATIONS_TOTAL_CHANNEL = 1002;
	
	// First channel to actually store data for enemy locations
	public final static int ENEMY_LOCATION_START_CHANNEL = 1003;
		
	// Sincere there are five ints needed to convey one unit of information
	public final static int ENEMY_INFORMATION_OFFSET = 4;
	
	// Upper bound on the number of enemies that can be sent in one turn (to prevent excess information levels
	public final static int ENEMY_LOCATION_LIMIT = 5;
	
	// ------------------------  ENEMY ARCHON INFORMATION ----------------------//
	
	// Archon Information from Scouts
			// Bit 0 - Gives Archon ID
			// Bit 1 - Gives Archon X
			// Bit 2 - Gives Archon Y
			// BIT 3 - Gives Archon Health
			// BIT 4 - Gives Number of Nearby Enemy Units

	// Channel to store the number of archons seen thus far......
	public final static int DISCOVERED_ARCHON_COUNT = 1100;

	// Channel to store which archon was last attacked by a group......
	public final static int FINISHED_ARCHON_COUNT = 1101;	
	
	// Channels to store the IDs of the archons discovered so far....
	public final static int ARCHON_INFORMATION_CHANNEL = 1103;
	
	// Sincere there are five ints needed to convey one unit of information
	public final static int ARCHON_INFORMATION_OFFSET = 5;
	
	// Upper bound on the number of enemies that can be sent in one turn (to prevent excess information levels
	public final static int ARCHON_LOCATION_LIMIT = 5;
	
	
	
	
	// ------------------------- BROADCASTING RELATED AUXILLARY FUNCTIONS --------------------------- //
	
	
	// Convert a robot's type converted to an int for transmission...
	
	public static int getRobotTypeToInt(RobotInfo robotInfo){
		
		if(robotInfo.type == battlecode.common.RobotType.ARCHON){
			return 0;
		}
		else if(robotInfo.type == battlecode.common.RobotType.GARDENER){
			return 1;
		}
		else if(robotInfo.type == battlecode.common.RobotType.SCOUT){
			return 2;
		}
		else if(robotInfo.type == battlecode.common.RobotType.SOLDIER){
			return 3;
		}
		else if(robotInfo.type == battlecode.common.RobotType.LUMBERJACK){
			return 4;
		}
		else if(robotInfo.type == battlecode.common.RobotType.TANK){
			return 5;
		}
		else{ // Shouldn't happen but if the robotInfo isn't valid, return -1
			return -1;
		}
	}
	
	// Convert a robot's int converted to its type for interpretation
	
	public static RobotType getRobotIntToType(int type){
		
		if(type == 0){
			return battlecode.common.RobotType.ARCHON;
		}
		else if(type == 1){
			return  battlecode.common.RobotType.GARDENER;
		}
		else if(type == 2){
			return battlecode.common.RobotType.SCOUT;
		}
		else if(type == 3){
			return battlecode.common.RobotType.SOLDIER;
		}
		else if(type == 4){
			return battlecode.common.RobotType.LUMBERJACK;
		}
		else if(type == 5){
			return battlecode.common.RobotType.TANK;
		}
		else{ // Shouldn't happen but if the robotInfo isn't valid, return -1
			return null;
		}
	}
	
	// ----------------------------- SPECIFIC BROADCASTING FUNCTIONS ----------------------- //
	
	
	// Class to better obtain data from broadcasts - since cannot initialize RobotInfo but want ID.....	
	public static class BroadcastInfo{
		// Stores an ID value and locations....
		public int ID;
		public float xPosition;
		public float yPosition;
		
		BroadcastInfo(int newID, float xLocation, float yLocation){	
			
			this.ID = newID;
			this.xPosition = xLocation;
			this.yPosition = yLocation;
		}		
	}
	
	
	// Easily called function to broadcast all enemy archon locations nearby.......
	
	public static void broadcastEnemyArchonLocations(RobotInfo[] nearbyEnemies) throws GameActionException{
		
		// Iterate through each of the enemies on the list....
		for (RobotInfo enemyInfo: nearbyEnemies){
			// If the type of the enemy is an archon....
			if (enemyInfo.type == battlecode.common.RobotType.ARCHON){
				// Call the function to update the team shared array
				updateEnemyArchonLocations(nearbyEnemies, enemyInfo);
			}
		}
	}	
	
	
	// Function to update the enemyArchon information stored in the team shared array 
	// Only call if an enemy archon has been seen
	
	public static void updateEnemyArchonLocations(RobotInfo[] nearbyEnemies, RobotInfo enemyArchon) throws GameActionException{
		
		// Get the ID for the archon to be updated
		int updatedArchonID = enemyArchon.ID;
		
		// Get the current number of archons stored in the team shared array
		int currentArchonCount = rc.readBroadcast(DISCOVERED_ARCHON_COUNT);
		
		// Placeholder to store the index of the archon in the team shared array
		int storedIndex = -1;		
		System.out.println(currentArchonCount);
		System.out.println(updatedArchonID);

		// Iterate through each of the  locations storing an archon ID to see if the archon can be found.....
		for (int i = 0; i < currentArchonCount; i++){
			
			int readID = rc.readBroadcast(ARCHON_INFORMATION_CHANNEL + i * ARCHON_INFORMATION_OFFSET);
			System.out.println(i + " + " + readID);
			
			if (readID == updatedArchonID){
				storedIndex = i;
			}			
		}
		
		// Placeholder variable for the location at which to start a broadcast........
		int updateBroadcastChannel;
		
		// If the enemy archon has not been seen previously........
		if (storedIndex == -1){
			// Only broadcast if the enemy archon will still be alive next turn.....
			if (enemyArchon.health > 20){
				// Increment the number of archons discovered and posit self as the last updater of the archon information......
				rc.broadcast(DISCOVERED_ARCHON_COUNT, (currentArchonCount + 1) % ARCHON_LOCATION_LIMIT);
	
				// Obtain the channel at which to broadcast.....
				updateBroadcastChannel = ARCHON_INFORMATION_CHANNEL + (currentArchonCount * ARCHON_INFORMATION_OFFSET);
				
				// SYSTEM CHECK - Let everyone know that a new archon has been found......
				// System.out.println("New enemy archon sighted!!! The archon has ID: " + enemyArchon.ID);
				
				// Broadcast all the relevant data, multiplying integers by 100 to preserve accuracy
				rc.broadcast(updateBroadcastChannel, enemyArchon.ID);
				rc.broadcast(updateBroadcastChannel + 1, (int) (100 * enemyArchon.location.x));
				rc.broadcast(updateBroadcastChannel + 2, (int) (100 * enemyArchon.location.y));
				rc.broadcast(updateBroadcastChannel + 3, (int) enemyArchon.health);
				rc.broadcast(updateBroadcastChannel + 4, nearbyEnemies.length);	
			}
		}
		// If there has already been an archon with that ID discovered....................
		else{			
		
			updateBroadcastChannel = ARCHON_INFORMATION_CHANNEL + (storedIndex * ARCHON_INFORMATION_OFFSET);
			
			// If there is a previously checked archon with very little health, update its information so that it has negative ID and will be ignored by readers
			if (enemyArchon.health < 20){
				
				// SYSTEM CHECK Make sure that it is known that the archon is quite dead - DARK RED DOT
				rc.setIndicatorDot(enemyArchon.location, 51, 0, 0);
				
				// Read the current value in that broadcast channel
				int currentValue = rc.readBroadcast(updateBroadcastChannel + 3);
				
				// If the archon hasn't been made to appear dead in the array
				if (currentValue != -1){
					
					// SYSTEM CHECK - Let everyone know that the archon is probably almost dead...
					// System.out.println("Enemy archon is almost dead.... DO NOT CONSIDER ANYMORE.... The archon has ID: " + enemyArchon.ID);
					
					// Broadcast an ID of 0 for the archon, so that it is no longer considered as a target
					rc.broadcast(updateBroadcastChannel + 3, -1);
					
					int archonsDisabled = rc.readBroadcast(FINISHED_ARCHON_COUNT);
					rc.broadcast(FINISHED_ARCHON_COUNT, archonsDisabled + 1);					
				}

			}
			
			// SYSTEM CHECK - Let everyone know that a new archon has been found......
			// System.out.println("Enemy archon information updated!!!!!!! The archon has ID: " + enemyArchon.ID);
			
			// Broadcast all the relevant data, multiplying integers by 100 to preserve accuracy
			rc.broadcast(updateBroadcastChannel, enemyArchon.ID);
			rc.broadcast(updateBroadcastChannel + 1, (int) (100 * enemyArchon.location.x));
			rc.broadcast(updateBroadcastChannel + 2, (int) (100 * enemyArchon.location.y));
			rc.broadcast(updateBroadcastChannel + 3, (int) enemyArchon.health);
			rc.broadcast(updateBroadcastChannel + 4, nearbyEnemies.length);		
		}				
	}

	
	public static BroadcastInfo readEnemyArchonLocations() throws GameActionException{
				
		// Get the current number of archons stored in the team shared array
		int currentArchonCount = rc.readBroadcast(DISCOVERED_ARCHON_COUNT);
		
		int archonsDisabled = rc.readBroadcast(FINISHED_ARCHON_COUNT);
		
		if (currentArchonCount > 0){
			
			for (int i = 0; i < currentArchonCount; i++){
				
				int archonID = rc.readBroadcast(ARCHON_INFORMATION_CHANNEL + i * ARCHON_INFORMATION_OFFSET + 0);
				int archonHealth = rc.readBroadcast(ARCHON_INFORMATION_CHANNEL + i * ARCHON_INFORMATION_OFFSET + 3);
				if (archonHealth >= 20){
					
					float readX = rc.readBroadcast(ARCHON_INFORMATION_CHANNEL + i * ARCHON_INFORMATION_OFFSET + 1) / 100;
					float readY = rc.readBroadcast(ARCHON_INFORMATION_CHANNEL + i * ARCHON_INFORMATION_OFFSET + 2) / 100;
					
					// Generate the data neeeded to return...
					BroadcastInfo newInfo = new BroadcastInfo(archonID, readX, readY);
					
					// SYSTEM CHECK Make sure that the archon dot is correctly placed... BRIGHT PURPLE			
					MapLocation newLocation = new MapLocation(readX, readY);
					rc.setIndicatorDot(newLocation, 147, 112, 219);
					
					return newInfo;							
				}
			}
			return null;
		}
		// If no archons have thus far been broadcasted or if they are all dead......
		else{
			return null;
		}
	}
	
}



