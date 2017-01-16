// Dodging collisions with bullets
package naclbot.units.motion.dodge;
import battlecode.common.*;
import naclbot.variables.GlobalVars;

public class BulletDodge extends GlobalVars {

    public static Direction whereToDodge(BulletInfo bullet) {
    	
		DodgeType dodgeInfo = new DodgeType(bullet);
		 
		boolean willCollide = dodgeInfo.willCollide();
		
		if (willCollide == true) {
		    Direction toDodge = dodgeInfo.getDirectionToMove();
		    return toDodge;
		} else {
		    Direction toDodge = new Direction(-1);
		    return toDodge;
		}
		//return toDodge;
	    }
    
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
}
