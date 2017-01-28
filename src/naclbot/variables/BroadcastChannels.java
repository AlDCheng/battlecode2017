package naclbot.variables;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import naclbot.units.motion.Chirasou;

/* ------------------   Overview ----------------------
 * 
 * Various constants controlling broadcasting hierarchy.....
 *
 * ~~ Coded by Illiyia (akimn@#mit.edu)
 * 
 ---------------------------------------------------- */

// Enumeration of all broadcast channels that we will be using
public class BroadcastChannels extends GlobalVars {
	
	public static final int BROADCAST_CLEARING_PERIOD = 50;
	
	// ---------------------- TEAM UNIT INFORMATION ----------------------//
	
	// Channel to show how many units have been produced thus far........
	public final static int UNIT_NUMBER_CHANNEL = 9999;	
	
	// ----------------------- ARCHON INFORMATION -------------------------//
	
	// Stores how many archons are currently in the game
	public final static int ARCHON_NUMBER_CHANNEL = 0;
	
	// Channels to store the initial location of the archon....
	public final static int ARCHON_INITIAL_LOCATION_X = 1;	
	public final static int ARCHON_INITIAL_LOCATION_Y = 2;
	
	// ----------------------- SCOUT INFORMATION -------------------------//
	
	// Stores how many gardeners  exist
	public final static int GARDENER_NUMBER_CHANNEL = 9991;
	
	// Stores how many gardeners have been initialized....
	public final static int GARDENERS_CONSTRUCTED_CHANNELS = 9998;
	
	// The last gardener to express distress..
	public final static int LAST_UPDATER_GARDENER_DISTRESS = 9500;	
	
	// Stores how many distress signals were sent out this turn...
	public final static int DISTRESS_SIGNAL_SENT_THIS_TURN = 9501;
	
	// Stores information related to which gardeners are being attacked
		// Distress Information from Scouts
		// Bit 0 - Gives My ID
		// Bit 1 - Gives My X
		// Bit 2 - Gives My Y
		// BIT 3 - Gives NearestEnemy ID
		// BIT 4 - Gives Enemy Type...
	
	public final static int GARDENER_DISTRESS_CHANNEL = 9502;
	
	// ----------------------- SCOUT INFORMATION -------------------------//
	
	// Stores how many lumber jacks have been constructed so far......
	public final static int LUMBERJACK_NUMBER_CHANNEL = 9950;
	
	// Stores how many lumber jacks are currently alive
	public final static int LUMBERJACKS_ALIVE_CHANNEL = 9951;
	 

	// ----------------------- SCOUT INFORMATION -------------------------//
	
	// Stores how many scouts have been constructed thus far
	public final static int SCOUT_NUMBER_CHANNEL = 9960;
	
	// Stores how many scouts are currently harvesting (unused).....	
	public final static int SCOUT_HARVESTER_NUMBER_CHANNEL = 9961;
	
	// Stores how many scouts are currently active on the map....
	public final static int SCOUTS_ALIVE_CHANNEL = 9962;
	
	
	
	// ---------------------- SOLDIER INFORMATION -------------------------//
	
	// Stores how many channels have been updated with information this turn
	public final static int SOLDIER_NUMBER_CHANNEL = 9970;
	
	// Stores how many soldiers are currently alive on a given turn....
	public final static int SOLDIERS_ALIVE_CHANNEL = 9971;
	
	// ---------------------- SOLDIER INFORMATION -------------------------//
	
	// Stores how many channels have been updated with information this turn
	public final static int TANK_NUMBER_CHANNEL = 9980;
	
	
		
	
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
	
	// STores whether or not an enemy has been seen before...
	public final static int ENEMY_HAS_BEEN_SEEN_CHANNEL = 1002;
	
	// First channel to actually store data for enemy locations
	public final static int ENEMY_LOCATION_START_CHANNEL = 1003;
	
	
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
	
	// Enemy archon initial locations information from gardeners 
			// Bit 0 - Gives Archon X
			// Bit 1 - Gives Archon Y
	
	
	// Channel to store if there is an available enemy archon 1 
	public final static int OPPOSING_ARCHON_1 = 1200; // 1201 & 1202 for X and Y values
	
	// Channel to store if there is an available enemy archon 2
	public final static int OPPOSING_ARCHON_2 = 1203; // 1204 & 1205 for X and Y values
	
	// Channel to store if there is an available enemy archon 3
	public final static int OPPOSING_ARCHON_3 = 1206; // 1207 & 1208 for X and Y values
	

	
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
		public int myID;
		public int enemyType;
		
		BroadcastInfo(int newID, float xLocation, float yLocation, int senderID, int enemyType){	
			
			this.ID = newID;
			this.xPosition = xLocation;
			this.yPosition = yLocation;
			this.myID = senderID;
			this.enemyType = enemyType;
		}		
	}
	
	
	// Easily called function to broadcast all enemy archon locations nearby.......
	
	public static void broadcastEnemyArchonLocations(RobotInfo[] nearbyEnemies) throws GameActionException{
		
		// Iterate through each of the enemies on the list....
		for (RobotInfo enemyInfo: nearbyEnemies){
			// If the type of the enemy is an archon....
			if (enemyInfo.type == battlecode.common.RobotType.ARCHON){
				
				// SYSTEM CHECK - Draw a line to the enemy archon to show that it has been spotted....
				rc.setIndicatorLine(rc.getLocation(), enemyInfo.location, 255, 0, 0);
				
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
		// System.out.println(currentArchonCount);
		// System.out.println(updatedArchonID);

		// Iterate through each of the  locations storing an archon ID to see if the archon can be found.....
		for (int i = 0; i < currentArchonCount; i++){
			
			int readID = rc.readBroadcast(ARCHON_INFORMATION_CHANNEL + i * ARCHON_INFORMATION_OFFSET);
			// System.out.println(i + " + " + readID);
			
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

	// Function to obtain the locations of any archons on the map, if there are any....
	
	public static BroadcastInfo readEnemyArchonLocations() throws GameActionException{
				
		// Get the current number of archons stored in the team shared array
		int currentArchonCount = rc.readBroadcast(DISCOVERED_ARCHON_COUNT);
		
		int archonsDisabled = rc.readBroadcast(FINISHED_ARCHON_COUNT);
		
		if (currentArchonCount > 0 && archonsDisabled < currentArchonCount){
			
			for (int i = 0; i < currentArchonCount; i++){
				int archonID = rc.readBroadcast(ARCHON_INFORMATION_CHANNEL + i * ARCHON_INFORMATION_OFFSET + 0);
				int archonHealth = rc.readBroadcast(ARCHON_INFORMATION_CHANNEL + i * ARCHON_INFORMATION_OFFSET + 3);
				if (archonHealth >= 20){
					
					float readX = rc.readBroadcast(ARCHON_INFORMATION_CHANNEL + i * ARCHON_INFORMATION_OFFSET + 1) / 100;
					float readY = rc.readBroadcast(ARCHON_INFORMATION_CHANNEL + i * ARCHON_INFORMATION_OFFSET + 2) / 100;
					
					// Generate the data neeeded to return...
					BroadcastInfo newInfo = new BroadcastInfo(archonID, readX, readY, -1, -1);
					
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
	
	// Function for gardeners and archons to show that they are being attacked.......
	public static void broadcastDistress(float previousHealth, RobotInfo[] nearestEnemies, MapLocation myLocation, int unitNumber) throws GameActionException{
		
		// Get the current health of the robot
		float currentHealth = rc.getHealth();
		
		// The last updater to the data...
		int lastUpdatedGardenerNumber = rc.readBroadcast(LAST_UPDATER_GARDENER_DISTRESS);
		
		// Have the first active unit running the distress calls clear....
		if (lastUpdatedGardenerNumber >= unitNumber){
			
			// Make the total signals sent this turn zero, and reset the last updater to be -1...
			rc.broadcast(LAST_UPDATER_GARDENER_DISTRESS, -1);
			rc.broadcast(DISTRESS_SIGNAL_SENT_THIS_TURN, 0);		
			
			// SYSTEM CHECK - Tell that the channel has been cleared
			System.out.println("Updater channel for gardener distress signals reset");	
		}		

		
		// If the robot has lost health in the previous round...
		if (currentHealth < previousHealth){
			
			// Get the nearest enemy...
			RobotInfo nearestEnemy = Chirasou.getNearestAlly(nearestEnemies, myLocation);
			
			// SYSTEM CHECK - Check to see if the gardener is actuall going to send a distress signal
			System.out.println("Am currently being attacked, will attempt to broadcast");
			
			// Broadcast ID, position and enemy ID  to the distress channel
			rc.broadcast(GARDENER_DISTRESS_CHANNEL, rc.getID());
			rc.broadcast(GARDENER_DISTRESS_CHANNEL + 1, (int)(myLocation.x * 100));
			rc.broadcast(GARDENER_DISTRESS_CHANNEL + 2, (int)(myLocation.y * 100));
			
			// If the gardener can sense a robot near it...
			if(nearestEnemy != null){
				rc.broadcast(GARDENER_DISTRESS_CHANNEL + 3, nearestEnemy.ID);
				rc.broadcast(GARDENER_DISTRESS_CHANNEL + 4, getRobotTypeToInt(nearestEnemy));
				
				// SYSTEM CHECK Print out that the gardener is actually sensing a nearby unit
				System.out.println("The attacker has ID: " + nearestEnemy.ID);
			}
			else{
				rc.broadcast(GARDENER_DISTRESS_CHANNEL + 3, -1);
				rc.broadcast(GARDENER_DISTRESS_CHANNEL + 4, -1);
				// SYSTEM CHECK Print out that the gardener cannot sense what is attacking it
				System.out.println("Unfortunately cannot sense who attacker might be.....");
			}
			rc.broadcast(LAST_UPDATER_GARDENER_DISTRESS, unitNumber);
			rc.broadcast(DISTRESS_SIGNAL_SENT_THIS_TURN, 1);			
		}
	}	
	
	// Function to obtain information on distress signals if they happened....
	// Returns a location to go to if the location is within the respondDistance of the current location of the roobot
	
	public static BroadcastInfo readDistress(MapLocation myLocation, float respondDistance) throws GameActionException{
		
		// Boolean to test whether or not a distress message was actually sent this turn
		boolean distressSignalSent = false;
		
		if (rc.readBroadcast(DISTRESS_SIGNAL_SENT_THIS_TURN) != 0 && rc.readBroadcast(LAST_UPDATER_GARDENER_DISTRESS) != -1){
			
			distressSignalSent = true;
		}
		
		if(distressSignalSent){
			int readSentID = rc.readBroadcast(GARDENER_DISTRESS_CHANNEL);
			float readX = rc.readBroadcast(GARDENER_DISTRESS_CHANNEL + 1) / 100;
			float readY = rc.readBroadcast(GARDENER_DISTRESS_CHANNEL + 2) / 100;
			int readEnemyID = rc.readBroadcast(GARDENER_DISTRESS_CHANNEL + 3);
			int readEnemyType = rc.readBroadcast(GARDENER_DISTRESS_CHANNEL + 4);
			
			BroadcastInfo newInfo = new BroadcastInfo(readEnemyID, readX, readY, readSentID, readEnemyType);
			
			MapLocation distressLocation = new MapLocation(readX, readY);
			
			if(distressLocation.distanceTo(myLocation) < respondDistance){
				
				// SYSTEM CHECK - display a blue line to the distress location and print out some data...
				// rc.setIndicatorLine(myLocation, distressLocation, 0, 0, 128);
				
				
				
				return newInfo;
			}
			// If the location was too far away
			else{
				// SYSTEM CHECK - Print out that the robot noticed the call but it was too far away
				System.out.println("Distress signal from ID: " + readSentID + " noted, but too far away to respond to....");
				return null;
			}
		}
		else{
			return null;
		}		
	}
	
    // Get the location of the nearest enemy and broadcast.... Clearly if you can see the enemy it is likely that they can see you as well
    
	public static void broadcastNearestEnemyLocation(RobotInfo[] enemyRobots, MapLocation myLocation, int unitNumber, MapLocation nearestAllyLocation, int currentRound) throws GameActionException{
		
		// Get information and the closest enemy
		RobotInfo nearestEnemy = Chirasou.getNearestAlly(enemyRobots, myLocation);
		
		// See who last updated the enemy locations
		int lastUpdatedUnitNumber = rc.readBroadcast(BroadcastChannels.LAST_UPDATER_ENEMY_LOCATIONS);
			
		// SYSTEM CHECK - Check who last updated the channel....
		 System.out.println("The last unit  to have updated this channel has unitNumber: " + lastUpdatedUnitNumber);
		 
		if (lastUpdatedUnitNumber >= unitNumber && currentRound % BROADCAST_CLEARING_PERIOD == 1){ 
			
			// Clear the number of signals sent this turn
			rc.broadcast(BroadcastChannels.ENEMY_LOCATIONS_SENT_THIS_TURN_CHANNEL, 0);
			rc.broadcast(BroadcastChannels.LAST_UPDATER_ENEMY_LOCATIONS, -1);
			
			// Reset so that minimal position can be found...
			rc.broadcast(ENEMY_LOCATION_START_CHANNEL + 1, -100);
			rc.broadcast(ENEMY_LOCATION_START_CHANNEL + 2, -100);
			
			// SYSTEM CHECK - Tell that the channel has been cleared
			System.out.println("Updater channel for enemy locations reset");				
		}
		
		// Make sure there is an enemy nearby before attempting to broadcast information.....
		if (nearestEnemy != null && nearestAllyLocation != null && currentRound % BROADCAST_CLEARING_PERIOD != 0){
			
			float distanceTo = nearestAllyLocation.distanceTo(nearestEnemy.location);
			
			int x = rc.readBroadcast(BroadcastChannels.ENEMY_LOCATIONS_SENT_THIS_TURN_CHANNEL);
			
			float currentX = rc.readBroadcast(ENEMY_LOCATION_START_CHANNEL + 1) / 100;
			float currentY = rc.readBroadcast(ENEMY_LOCATION_START_CHANNEL + 2) / 100;
			
			MapLocation currentStored = new MapLocation(currentX, currentY);			
			
			// SYSTEM CHECK Draw a RED LINE to the nearest enemy so far and a BLUE LINE one to the current enemy discovered??
			//	rc.setIndicatorLine(myLocation, nearestEnemy.location, 255, 0, 0);
			//	rc.setIndicatorLine(myLocation, currentStored, 0, 0, 255);
				
			if(nearestAllyLocation.distanceTo(currentStored) >= distanceTo || x == 0 && distanceTo > 20){
				
				// SYSTEM CHECK.. - Since there was an enemy nearby... Notify that something may be sent
				System.out.println("Enemy nearby - will attempt to broadcast location");
				
				// Broadcast all relevant information
				rc.broadcast(ENEMY_LOCATION_START_CHANNEL, nearestEnemy.ID);
				rc.broadcast(ENEMY_LOCATION_START_CHANNEL + 1, (int) (nearestEnemy.location.x * 100));
				rc.broadcast(ENEMY_LOCATION_START_CHANNEL + 2, (int) (nearestEnemy.location.y * 100));
				rc.broadcast(ENEMY_HAS_BEEN_SEEN_CHANNEL, 1);
				
				// Inform all that a new enemy location has been implemented....
				rc.broadcast(BroadcastChannels.ENEMY_LOCATIONS_SENT_THIS_TURN_CHANNEL, 1);
				rc.broadcast(BroadcastChannels.LAST_UPDATER_ENEMY_LOCATIONS, unitNumber);				
			}
		}				
	}	
	
	public static BroadcastInfo readEnemyLocations() throws GameActionException{
		
		if(rc.readBroadcast(ENEMY_HAS_BEEN_SEEN_CHANNEL) != 0){
		
			int readSentID = rc.readBroadcast(ENEMY_LOCATION_START_CHANNEL);
			int readSentX = rc.readBroadcast(ENEMY_LOCATION_START_CHANNEL + 1) / 100;
			int readSentY = rc.readBroadcast(ENEMY_LOCATION_START_CHANNEL + 2) / 100;
			
			// Get rid of the test messages that are negative.....
			if(readSentX < 0){
				return null;
			}
			
			// Create and return a packet of broadcastinfo...
			BroadcastInfo newInfo = new BroadcastInfo(readSentID, readSentX, readSentY, -1, -1);
			return newInfo;
		}
		else{
			return null;
		}
	}
}



