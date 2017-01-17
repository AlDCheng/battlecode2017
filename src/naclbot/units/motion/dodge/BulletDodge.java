// Dodging collisions with bullets
package naclbot.units.motion.dodge;
import battlecode.common.*;
import naclbot.variables.GlobalVars;

public class BulletDodge extends GlobalVars {

    public static Direction whereToDodge(BulletInfo[] bullets) {
		    
	for (BulletInfo bullet: bullets) {
	    
	    DodgeType dodgeInfo = new DodgeType(bullet);
	    boolean willCollide = dodgeInfo.willCollide();
	    float distance = dodgeInfo.getDistToRobot();
	    	    
	    if (willCollide == true) {
		Direction dir = dodgeInfo.getDirectionToMove();
		return dir;
	    }	    
	}
	
	Direction dir = new Direction(-1);
	return dir;
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
