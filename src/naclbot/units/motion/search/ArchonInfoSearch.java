// This class makes a new data type for archons
package naclbot.units.motion.search;
import battlecode.common.*;
import naclbot.variables.GlobalVars;

public class ArchonInfoSearch extends GlobalVars {
    String bulletType;
    boolean isArchon;
    Direction enemyDir;
    // Add whether or not they are in a "cone" so shoot more
	
    public ArchonInfoSearch(String bullet, boolean archon, Direction direction) {
	this.bulletType = bullet;
	this.isArchon = archon;
	this.enemyDir = direction;
    }
}
