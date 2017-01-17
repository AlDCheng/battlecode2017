package naclbot;
import battlecode.common.*;
import naclbot.units.AI.archon.ArchonBot;
import naclbot.units.AI.gardener.GardenerBot;
import naclbot.units.AI.lumberjack.LumberjackBot;
import naclbot.units.AI.scout.ScoutBot;
import naclbot.units.AI.soldier.SoldierBot;
import naclbot.units.AI.tank.TankBot;
import naclbot.variables.GlobalVars;

public strictfp class RobotPlayer extends GlobalVars{
    static RobotController rc;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
    **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
    	// System.out.println("Sys call. ");

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        
    	//RobotPlayer.rc = rc;
    	globalInit(rc);

        // Here, we've separated the controls into a different method for each RobotType.
        // You can add the missing ones or rewrite this into your own control structure.
        switch (rc.getType()) {
			case ARCHON:
			    ArchonBot.entry();
			    ArchonBot.mainPhase(); // Move to main phase
			    break; 
			case GARDENER:
			    GardenerBot.entry();
			    break;
			case SOLDIER:
			    SoldierBot.entry();
			    break;
			case TANK:
			    TankBot.entry();
			case LUMBERJACK:
			    LumberjackBot.entry();
			    break;
			case SCOUT:
			    ScoutBot.init();
			    break;
	    
        }
    }
}
