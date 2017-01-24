// General move function from example
package naclbot.units.motion;
import battlecode.common.*;
import naclbot.variables.GlobalVars;

public class Move extends GlobalVars {
	/**
	 * Returns a random Direction
	 * @return a random Direction
	 */
	public static Direction randomDirection() {
	    return new Direction((float)Math.random() * 2 * (float)Math.PI);
	}
	
	/**
	 * Attempts to move in a given direction, while avoiding small obstacles directly in the path.
	 *
	 * @param dir The intended direction of movement
	 * @return true if a move was performed
	 * @throws GameActionException
	 */
	public static boolean tryMove(Direction dir) throws GameActionException {
	    return tryMove(dir,20,3);
	}
	
	/**
	 * Attempts to move in a given direction, while avoiding small obstacles direction in the path.
	 *
	 * @param dir The intended direction of movement
	 * @param degreeOffset Spacing between checked directions (degrees)
	 * @param checksPerSide Number of extra directions checked on each side, if intended direction was unavailable
	 * @return true if a move was performed
	 * @throws GameActionException
	 */
	public static boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {
	
	    // First, try intended direction
	    if (rc.canMove(dir)) {
	        rc.move(dir);
	        return true;
	    }
	
	    // Now try a bunch of similar angles
	    int currentCheck = 1;
	
	    while(currentCheck<=checksPerSide) {
	        // Try the offset of the left side
	        if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
	            rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck));
	            return true;
	        }
	        // Try the offset on the right side
	        if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
	            rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck));
	            return true;
	        }
	        // No move performed, try slightly further
	        currentCheck++;
	    }
	
	    // A move never happened, so return false.
	    return false;
	}
	
	// Insert by Alan
	
	/**
	 * Attempts to move in a given direction, while avoiding small obstacles direction in the path. With distance considered
	 *
	 * @param dir The intended direction of movement
	 * @param degreeOffset Spacing between checked directions (degrees)
	 * @param checksPerSide Number of extra directions checked on each side, if intended direction was unavailable
	 * @return true if a move was performed
	 * @throws GameActionException
	 */
	public static boolean tryMoveWithDist(Direction dir, float degreeOffset, int checksPerSide, float distance) throws GameActionException {
		
	    // First, try intended direction
	    if (rc.canMove(dir, distance)) {
	        rc.move(dir, distance);
	        return true;
	    }
	
	    // Now try a bunch of similar angles
	    int currentCheck = 1;
	
	    while(currentCheck<=checksPerSide) {
	        // Try the offset of the left side
	        if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck),distance)) {
	            rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck),distance);
	            return true;
	        }
	        // Try the offset on the right side
	        if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck),distance)) {
	            rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck),distance);
	            return true;
	        }
	        // No move performed, try slightly further
	        currentCheck++;
	    }
	
	    // A move never happened, so return false.
	    return false;
	}
}