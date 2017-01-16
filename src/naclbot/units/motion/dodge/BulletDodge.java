// Dodging collisions with bullets
package naclbot;
import battlecode.common.*;

public class BulletDodge extends GlobalVars {

    static Direction whereToDodge(BulletInfo bullet) {
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
}
