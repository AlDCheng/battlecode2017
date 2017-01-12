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
	
	public static void globalInit(RobotController _RC) {
		rc = _RC;
		ARCHON_CHANNEL = 0;
		GARDENER_CHANNEL = 1;
		LUMBERJACK_CHANNEL = 2;
		SCOUT_CHANNEL = 3;
		TANK_CHANNEL = 4;
		
	}
}
