package naclbot;
import battlecode.common.*;

public class GlobalVars {
	public static RobotController rc;
	public static int ARCHON_CHANNEL;
	public static int GARDENER_CHANNEL;
	public static int LUMBERJACK_CHANNEL;
	public static int SCOUT_CHANNEL;
	public static int SOLDIER_CHANNEL;
	public static int TANK_CHANNEL;
	public static int SCOUT_MESSAGE_OFFSET;
	
	public static void globalInit(RobotController _RC) {
		rc = _RC;
		ARCHON_CHANNEL = 0;
		GARDENER_CHANNEL = 10;
		LUMBERJACK_CHANNEL = 200;
		
		/* Scout Channel is the placeholding value foir all scout channels. 
		 * The broadcasts at this number contain only the number of scouts currently available to the team		
		*/
		SCOUT_CHANNEL = 100;
		SCOUT_MESSAGE_OFFSET = 10;
		
		TANK_CHANNEL = 400;
		
	}

}
