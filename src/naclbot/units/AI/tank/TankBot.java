// AI for tank under normal control
package naclbot.units.AI.tank;
import battlecode.common.*;

import naclbot.units.motion.Move;
import naclbot.units.motion.shoot.Aim;
import naclbot.units.motion.routing.Routing;
import naclbot.variables.GlobalVars;

import java.util.ArrayList;

public class TankBot extends GlobalVars {
    
	public static MapLocation destination;
	public static ArrayList<MapLocation> pathToMove;
	
	public static void entry() throws GameActionException {
	System.out.println("I'm an tank!");
    Team enemy = rc.getTeam().opponent();

	
	// The code you want your robot to perform every round should be in this loop
        while (true) {
	    // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	// Listen for home archon's location
            	int xPos = rc.readBroadcast(ARCHON_CHANNEL);
            	int yPos = rc.readBroadcast(ARCHON_CHANNEL+1);

            	MapLocation myLocation = rc.getLocation(); // Current location

            	// Sense nearby enemy robots
            	RobotInfo[] currentEnemies = rc.senseNearbyRobots(-1, enemy);
            	RobotInfo[] currentAllies = rc.senseNearbyRobots(-1, rc.getTeam());
            	
            	// Move to desired location with ALAN D CHENG'S path planning
            	destination = new MapLocation(450,450);
            	pathToMove = new ArrayList<MapLocation>();
            	pathToMove.add(destination);
            	
            	Routing.setRouting(pathToMove);
            	
            	Routing.routingWrapper();

            	// Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
            	Clock.yield();

            } catch (Exception e) {
            	System.out.println("Tank Exception");
            	e.printStackTrace();
            }
        }
    }


    
}
