package naclbot;
import battlecode.common.*;
import naclbot.units.AI.archon.ArchonBot;
import naclbot.units.AI.archon.KazumaBot;
import naclbot.units.AI.gardener.EmiliaBot;
import naclbot.units.AI.gardener.GARNiDELiABot;
import naclbot.units.AI.gardener.GardenerBot;
import naclbot.units.AI.lumberjack.BarusuBot;
import naclbot.units.AI.scout.BestGirlBot;
import naclbot.units.AI.soldier.SaberBot;
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
				// EmiliaBot.init();
			    KazumaBot.init();	
			    break; 
			case GARDENER:
				// EmiliaBot.init();
			    GARNiDELiABot.init();
			    break;
			case SOLDIER:
				// EmiliaBot.init();
				SaberBot.init();
			    break;
			case TANK:
				// EmiliaBot.init();
				TankBot.init();
			    break;
			case LUMBERJACK:
				// EmiliaBot.init();
				BarusuBot.init();
			    break;
			case SCOUT:
				// EmiliaBot.init();
			    BestGirlBot.init();
			    break;
	    
        }
    }
}
