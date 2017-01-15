// AI for tank under normal control
package naclbot;
import battlecode.common.*;
import java.util.ArrayList;

public class TankBot extends GlobalVars {
    public static void entry() throws GameActionException {
	System.out.println("I'm an tank!");
        Team enemy = rc.getTeam().opponent();

	// Important variables
	ArrayList<RobotInfoShoot> enemyToShoot = new ArrayList<RobotInfoShoot>();
	int notMoved = 0;
	MapLocation prevLocation = rc.getLocation();

	// Set role
	int role;
	boolean leaveArchon = false; // If we want to make it surround archon until later grouping
	
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

		// Shooting 
		if (!enemyToShoot.isEmpty()) {
		    //boolean canShoot = false;
		    ShootingType shoot = Aim.toShoot(enemyToShoot, currentEnemies);

		    if (shoot.getBulletType() != "none") {
			// No shooting if there is an ally in the way
			for (RobotInfo ally: currentAllies) {
			    MapLocation allyLoc = ally.getLocation();
			    Direction allyDir = new Direction(myLocation, allyLoc);
			    float allyDist = myLocation.distanceTo(allyLoc);
			    float allyRadius = ally.getRadius();
			    
			    // TODO: implement radius location of ally
			    if (allyDir != shoot.getDirection()) {
				// Shoot
				if (shoot.getBulletType() == "pentad") {
				    rc.firePentadShot(shoot.getDirection());
				} else if (shoot.getBulletType() == "triad") {
				    rc.fireTriadShot(shoot.getDirection());
				} else if (shoot.getBulletType() == "single") {
				    rc.fireSingleShot(shoot.getDirection());
				}
			    }	    
			}
		    }
		}
		// Resets the list to add new ones
		enemyToShoot.clear();

		// Adds the ones seen this turn
		for (RobotInfo enemyRobot: currentEnemies) {
		    MapLocation currentLoc = enemyRobot.getLocation();
		    int ID = enemyRobot.getID();
		    RobotType robType = enemyRobot.getType();
		    RobotInfoShoot r = new RobotInfoShoot(ID, robType, currentLoc);
		    enemyToShoot.add(r);
		}

		// MOVEMENT 
		BulletInfo[] nearbyBullets = rc.senseNearbyBullets();
		for (BulletInfo bullet: nearbyBullets) {
		    boolean willCollide = BulletDodge.willCollideWithMe(bullet);
		    if (willCollide = true) {
			System.out.println("OMGWILLCOLLIDE");
		    }
		}
		
		
		// TODO: Make it stay near archon
		if (leaveArchon == false) {
		    MapLocation archonLoc = new MapLocation(xPos,yPos);
		    Direction dir = new Direction(myLocation,archonLoc);
		    Move.tryMove(dir);
		}
		
		/*
		// If hasn't found archon or is not of protectinc archon role
		// Check if it hasn't moved
		if (myLocation == prevLocation) {
		    notMoved += 1;
		}
		
		// Move to other allies 
		if (currentAllies.length > 0 && notMoved < 5) {
		    MapLocation locAlly = AllySearch.locFurthestAlly(currentAllies);
		    if (locAlly == rc.getLocation()) {
			Move.tryMove(Move.randomDirection());
			notMoved += 1;
		    } else {
			Direction dir = rc.getLocation().directionTo(locAlly);
			Move.tryMove(dir);
		    }
		} else {
		    Move.tryMove(Move.randomDirection());
		    notMoved = 0; // Reset counter
		}
		*/
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Soldier Exception");
                e.printStackTrace();
            }
        }
    }
}
