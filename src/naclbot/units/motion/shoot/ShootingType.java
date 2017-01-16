// This class makes a new data type for archons
package naclbot.units.motion.shoot;
import battlecode.common.*;
import naclbot.variables.GlobalVars;

public class ShootingType extends GlobalVars {
    String bulletType;
    boolean isArchon;
    Direction enemyDir;
    // Add whether or not they are in a "cone" so shoot more
	
    public ShootingType(String bullet, boolean archon, Direction direction) {
	this.bulletType = bullet;
	this.isArchon = archon;
	this.enemyDir = direction;
    }

    public String getBulletType() {
	return bulletType;
    }

    public boolean getIsArchon() {
	return isArchon;
    }

    public Direction getDirection() {
	return enemyDir;
    }
}
			 
