// AI for tank under normal control
package naclbot.units.AI.tank;
import battlecode.common.*;

import naclbot.units.motion.Move;
import naclbot.units.motion.dodge.BulletDodge;
import naclbot.units.motion.search.AllySearch;
import naclbot.units.motion.shoot.Aim;
import naclbot.units.motion.shoot.RobotInfoShoot;
import naclbot.units.motion.shoot.ShootingType;
import naclbot.variables.GlobalVars;

import java.util.ArrayList;

public class TankBot extends GlobalVars {
    public static void entry() throws GameActionException {
	System.out.println("I'm an tank!");
        Team enemy = rc.getTeam().opponent();

	// Important variables
	ArrayList<RobotInfoShoot> enemyToShoot = new ArrayList<RobotInfoShoot>();
	int notMoved = 0;
	MapLocation prevLocation = rc.getLocation();
	
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

		/* ---------------------------------- SHOOTING --------------------------------------*/
		// Shoots enemies that have been tracked and takes care not to single shoot to ally
		// TODO: implement no shoot for triad and pentad to ally (?)
		shootingEnemies(currentEnemies,currentAllies,enemyToShoot);
		
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

	        /* ---------------------------------- MOVEMENT --------------------------------------*/
		BulletInfo[] nearbyBullets = rc.senseNearbyBullets();
		if (nearbyBullets.length > 0) {
		    Direction dodge = BulletDodge.whereToDodge(nearbyBullets);
		    if (dodge != null) {
			System.out.println("OMGWILLCOLLIDE");
			Move.tryMove(dodge);
		    }
		}
				
		// If hasn't found archon or is not of protectinc archon role
		// Check if it hasn't moved
		if (myLocation == prevLocation) {
		    notMoved += 1;
		}
		
		// Move to other allies 
		if (currentAllies.length > 0 && notMoved < 5 && !rc.hasMoved()) {
		    MapLocation locAlly = AllySearch.locFurthestAlly(currentAllies);
		    if (locAlly == rc.getLocation()) {
			Move.tryMove(Move.randomDirection());
			notMoved += 1;
		    } else {
			Direction dir = rc.getLocation().directionTo(locAlly);
			Move.tryMove(dir);
		    }
		} else if (!rc.hasMoved()) {
		    Move.tryMove(Move.randomDirection());
		    notMoved = 0; // Reset counter
		}
		
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Tank Exception");
                e.printStackTrace();
            }
        }
    }


    private static void shootingEnemies(RobotInfo[] enemies, RobotInfo[] allies, ArrayList<RobotInfoShoot> pastEnemies) throws GameActionException{
	// Checks if there are enemies to trace
	// Checks if the unit has attacked already
	if (!pastEnemies.isEmpty() && !rc.hasAttacked()) {
	    ShootingType shoot = Aim.shootNearestEnemy(pastEnemies, enemies, true); // Returns details about shooting
	    boolean hitAlly = true;
	    // ShootingType: bulletType, isArchon, direction
	    // Checks if we should actually shoot (bulletType is none if we cannot shoot)
	    if (shoot != null) {
		// No shooting if there is an ally in the way
		for (RobotInfo ally: allies) {
		    // Takes into account if the bullet will hit ally
		    // This only works for single shots because they go in that direction. Triad and pentad are not considered (because they fan out)
		    hitAlly = willHitAlly(ally,shoot);

		    // This means the bullet will hit the ally
		    if (hitAlly) {
			break;
		    }
		}

		if (!hitAlly) {
		    // Shoot
		    if (shoot.getBulletType() == "pentad" && !rc.hasAttacked()) {
			rc.firePentadShot(shoot.getDirection());
		    } else if (shoot.getBulletType() == "triad" && !rc.hasAttacked()) {
			rc.fireTriadShot(shoot.getDirection());
		    } else if (shoot.getBulletType() == "single" && !rc.hasAttacked()) {
			rc.fireSingleShot(shoot.getDirection());
		    }
		}
	    }
	}
    }

    // Checks if ally will be hit
    private static boolean willHitAlly(RobotInfo frond, ShootingType shootInfo) {
	MapLocation myLoc = rc.getLocation();
	MapLocation allyLoc = frond.getLocation();
	Direction allyDir = new Direction(myLoc, allyLoc);
	Direction shootDir = shootInfo.getDirection();
	float theta = allyDir.radiansBetween(shootDir);
	float allyDist = myLoc.distanceTo(allyLoc);
	if ((allyDist * Math.sin(theta)) <= frond.getRadius()) {
	    return true;
	} else {
	    return false;
	}
    }


    
}
