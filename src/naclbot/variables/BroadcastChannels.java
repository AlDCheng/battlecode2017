package naclbot.variables;

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
public class BroadcastChannels {
	
	// ----------------------- ARCHON INFORMATION -------------------------//
	
	// Stores how many channels have been updated with information this turn
	public final static int ARCHON_NUMBER_CHANNEL = 0;

	// ----------------------- SCOUT INFORMATION -------------------------//
	
	// Stores how many channels have been updated with information this turn
	public final static int SCOUT_NUMBER_CHANNEL = 9995;
	
	public final static int SCOUT_HARVESTER_NUMBER_CHANNEL = 9996;
	
	// ---------------------- SOLDIER INFORMATION -------------------------//
	
	// Stores how many channels have been updated with information this turn
	public final static int SOLDIER_NUMBER_CHANNEL = 9995;
		
	
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
}



