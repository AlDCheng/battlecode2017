// Dodging collisions with bullets
package naclbot.units.motion.dodge;
import battlecode.common.*;
import naclbot.variables.GlobalVars;

public class BulletDodge extends GlobalVars {

    public static Direction whereToDodge(BulletInfo[] bullets) {
	float nearestBulletDistance = -1;
	DodgeType nearestBullet = null;

	// Search for nearest bullet 
	for (BulletInfo bullet: bullets) {
	    DodgeType dodgeInfo = new DodgeType(bullet);
	    boolean willCollide = dodgeInfo.willCollide();
	    float distance = dodgeInfo.getDistToRobot();

	    if (willCollide == true && (nearestBulletDistance == -1)) {
		nearestBulletDistance = distance;
		nearestBullet = dodgeInfo;
	    } else if (willCollide == true && (distance < nearestBulletDistance)) {
		nearestBulletDistance = distance;
		nearestBullet = dodgeInfo;
	    }	    
	}
	if (nearestBullet != null) {
	    Direction dodgeDir = nearestBullet.getDirectionToMove();
	    return dodgeDir;
	} else {
	    return null;
	}
    }
    
    /*
    public static class DodgeType{
    	public int xtype;
    	
    	public DodgeType(BulletInfo bullet){
    		this.xtype = 0;
    	}

		public Direction getDirectionToMove() {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean willCollide() {
			// TODO Auto-generated method stub
			return false;
		}
    	
    }
    */
}
