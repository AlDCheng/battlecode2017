// Handling the FEEDER units 

package naclbot.units.interact;
import battlecode.common.*;
import naclbot.variables.GlobalVars;

public class iFeed extends GlobalVars {
    
    // Determines if the robot will die or not 
    public static boolean willFeed(float health, BulletInfo[] bullets) {
	// bullets is a list of bullets that will hit current unit
	// health is the amount of health left in current unit
	float totalDamage = 0;
	for (BulletInfo bullet: bullets) {
	    totalDamage += bullet.getDamage();
	}
	if (totalDamage >= health) {
	    return true;
	} else {
	    return false;
	}
    }

    // Determines the bullets that will hit current unit
    //public static BulletInfo[] bulletsThatHit() {

    //}

    
}
