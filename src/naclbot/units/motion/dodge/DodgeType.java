// Managing dodging collisions with bullets
package naclbot.units.motion.dodge;
import battlecode.common.*;
import naclbot.variables.GlobalVars;

public class DodgeType extends GlobalVars {
    // Bullet attributes
    BulletInfo theBullet;
    Direction propagationDirection;
    MapLocation locationBullet;

    // Bullet + Robot attributes
    RobotInfo theRobot;
    MapLocation locationRobot;
    Direction directionToRobot;
    float distToRobot;
    float theta;
    float perpendicularDist;

    
    boolean willCollide;
    Direction directionToMove;

    public DodgeType(BulletInfo bullet) {
	this.theBullet = bullet;
	//this.theRobot = robot;
	calculateVariables();
    }

    private void calculateVariables() {
	// Bullet
	propagationDirection = theBullet.dir;
	locationBullet = theBullet.location;

	// Bullet + Robot
	locationRobot = rc.getLocation();
	directionToRobot = locationBullet.directionTo(locationRobot);
	distToRobot = locationBullet.distanceTo(locationRobot);
	theta = propagationDirection.radiansBetween(directionToRobot);
    }

    // Returns distance to robot
    public float getDistToRobot() {
	return distToRobot;
    }

    // Calculates willCollide variable and returns it, calculates perpendicularDist as well
    public boolean willCollide() {
        if (Math.abs(theta) > Math.PI/2) {
	    perpendicularDist = -1;
	    willCollide = false;
	} else {
	    perpendicularDist = (float)Math.abs(distToRobot * Math.sin(theta));
	    willCollide = (perpendicularDist <= rc.getType().bodyRadius);
	}
	return willCollide;
    }

    // Calculates the direction in which the robot should move to dodge bullet
    public Direction getDirectionToMove() {
	float newDist = (float)Math.abs(distToRobot * Math.cos(theta));
	// Calculate the location where the bullet would hit the robot
	MapLocation newLoc = locationBullet.add(propagationDirection,newDist);
	Direction dirHit = new Direction(locationRobot,newLoc);
	directionToMove = dirHit.opposite();
	    
	return directionToMove;
    }
}
