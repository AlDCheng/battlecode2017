// General move function from example
package naclbot.units.motion;
import battlecode.common.*;
import naclbot.variables.GlobalVars;

import java.util.Arrays;
import java.util.ArrayList;

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
	
	//--------------------------------------------------------------------------------------
	//--------------------------------------[Movement]--------------------------------------
	//--------------------------------------------------------------------------------------
	public static boolean tryMoveReturn(Direction dir, float degreeOffset, int checksPerSide, 
										 float distance, float distanceInterval, ArrayList<MapLocation> newPath) throws GameActionException {
		MapLocation moveLoc = tryMoveRouting(dir, degreeOffset, checksPerSide, distance, distanceInterval);
		if(moveLoc != null) {
			newPath.add(moveLoc);
			return true;
		}
		else {
			return false;
		}
	}
	/**
	 * Attempts to move in a given direction, while avoiding small obstacles direction in the path.
	 *
	 * @param dir The intended direction of movement
	 * @return true if a move was performed
	 * @throws GameActionException
	 */
	public static MapLocation tryMoveRouting(Direction dir) throws GameActionException {
		float maxMove = rc.getType().strideRadius; // Set default movement distance to max 
	    return tryMoveRouting(dir, 1, 10, maxMove, maxMove);
	}
	
	/**
	 * Attempts to move in a given direction, while avoiding small obstacles direction in the path.
	 *
	 * @param dir The intended direction of movement
	 * @param distance Max length of movement
	 * @param distanceInterval Interval of movement length to check
	 * @return true if a move was performed
	 * @throws GameActionException
	 */
	public static MapLocation tryMoveRouting(Direction dir, float distance, float distanceInterval) throws GameActionException {
	    return tryMoveRouting(dir, 1, 10, distance, distanceInterval);
	}
	
	/**
	 * Attempts to move in a given direction, while avoiding small obstacles direction in the path.
	 *
	 * @param dir The intended direction of movement
	 * @param degreeOffset Spacing between checked directions (degrees)
	 * @param checksPerSide Number of extra directions checked on each side, if intended direction was unavailable
	 * @param distance Max length of movement
	 * @param distanceInterval Interval of movement length to check
	 * @return true if a move was performed
	 * @throws GameActionException
	 */
	public static MapLocation tryMoveRouting(Direction dir, float degreeOffset, int checksPerSide, 
										 float distance, float distanceInterval) throws GameActionException {
		MapLocation curLoc = rc.getLocation();
		
	    // First, try intended direction
	    if (rc.canMove(dir, distance)) {
//		        rc.move(dir, distance);
	        return curLoc.add(dir,distance);
	    }
	    
	    // Intended direction not successful:
	    
	    // Set first distance length to check to be max
	    float distanceCheck = distance;
	    
	    System.out.println("Distance Max: " + distance);
	    
	    // While distance length is above a certain threshold, continue searching
	    while(distanceCheck > 0.0001) {
//		    	System.out.println(distanceCheck);
	    	
	    	// Now try a bunch of similar angles
		    int currentCheck = 1;
		
		    while(currentCheck<=checksPerSide) {
		        // Try the offset of the left side
		        if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck),distanceCheck)) {
		        	System.out.println("Moved on Left Side");
//			            rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck),distanceCheck);
//			            return true;
		        	return curLoc.add(dir.rotateLeftDegrees(degreeOffset*currentCheck),distanceCheck);
		        }
		        // Try the offset on the right side
		        if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck),distanceCheck)) {
		        	System.out.println("Moved on Right Side");
//			            rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck),distanceCheck);
//			            return true;
		        	return curLoc.add(dir.rotateRightDegrees(degreeOffset*currentCheck),distanceCheck);
		        }
		        // No move performed, try slightly further
		        currentCheck++;
		    }
		    
		    // Set next distance to be check
		    // Decrease by interval value set
		    distanceCheck -= distanceInterval; 
	    }
	    
	    // A move never happened, so return false.
	    return null;
	}
}