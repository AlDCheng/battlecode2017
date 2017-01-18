// This class makes a new data type for archons
package naclbot.units.motion.shoot;
import battlecode.common.*;
import naclbot.variables.GlobalVars;

public class ShootingType extends GlobalVars {
    String bulletType;
    RobotType robotType;
    Direction enemyDir;
    float enemyDist;
    // Add whether or not they are in a "cone" so shoot more
	
    public ShootingType(String bullet, RobotType type, Direction direction, float distance) {
		this.bulletType = bullet;
		this.robotType = type;
		this.enemyDir = direction;
		this.enemyDist = distance;
    }

    public String getBulletType() {
		return bulletType;
    }

    public RobotType getType() {
		return robotType;
    }

    public Direction getDirection() {
		return enemyDir;
    }

    public float getDistance() {
		return enemyDist;
    }
}
			 
