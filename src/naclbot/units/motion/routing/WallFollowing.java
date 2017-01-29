//----------------------------------------------------------------------------------
// WallFollowing.java
// Alan Cheng (adcheng@mit.edu)
// 
// Info: This class houses all related movement to moving through a path 
// 		 WITH wall following
// 
//----------------------------------------------------------------------------------
package naclbot.units.motion.routing;

// Imports from package:
import naclbot.units.motion.Move;
import naclbot.units.motion.routing.Routing;
import naclbot.variables.GlobalVars;

// Imports from Battlecode packages
import battlecode.common.*;

// Imports from Java standard library
import java.util.Arrays;
import java.util.ArrayList;

public class WallFollowing extends GlobalVars{
	
	// Variable declarations:
	// - Pathing:
	private static MapLocation FD, stuckLoc;
	private static float initialDist;
	
	private static int progX = 1; 
	private static int progY = 0;
	
	// Determines starting angle for following
	private static Direction startAngle = new Direction(0);
	private static int rotL = 1;
	public static boolean wallFollow = false;
	
	// Parameters of units
	// - Scanning:
	// * This is primarily for tryMoveRouting(Direction dir, float degreeOffset, int checksPerSide, float distance, float distanceInterval)
	public static float degreeOffset = 2;  // Spacing between checked directions (degrees)
	public static int checks = 3; // Number of extra directions checked on each side, if intended direction was unavailable
	public static float maxDist = rc.getType().strideRadius; // Maximum length of movement (default is strideRadius of unit)
	public static float distIntervals = maxDist / 3; // Interval of each length check
	public static float wfOffset = 15;
	
	// Functions below:
	
	// Set path for routing
	public static void setWallFollowing(MapLocation destination) {
		System.out.println("Set Wall Following");
		
		try {
			// Pathing:
			Routing.path = new ArrayList<MapLocation>(); // Resets path to traverse with inputted path
			Routing.path.add(destination);
			
			Routing.prevPath = new ArrayList<Object[]>(); 
			
			// Set new final destination
			FD = destination;
			
			// Get current location
			MapLocation curLoc = rc.getLocation();
			
			// Get direction
			Direction toGoal = new Direction(curLoc, destination);
			
			// Set starting scan angle
			startAngle = toGoal;
			
			// Change wall follow flag
			wallFollow = true;
			Routing.togglePP = true;
			
			// Set initial distance to beat
			initialDist = curLoc.distanceTo(destination);
			
			stuckLoc = curLoc;
			progX = 1; progY = 0;
			if (toGoal.getDeltaY(1) > toGoal.getDeltaX(1)) {
				progX = 0; progY = 1;
			}
			if (toGoal.getDeltaX(1) < 0) {
				progX = -1*progX;
			}
			if (toGoal.getDeltaY(1) < 0) {
				progY = -1*progY;
			}
			
			// Define rotation direction
			rotL = 1;
			if (toGoal.getDeltaX(1)*toGoal.getDeltaY(1) > 0) {
				rotL = -1;
			}
			
		} catch (Exception e) {
			System.out.println("Error in Wall Following Entry");
			e.printStackTrace(); // Print exceptions
		}
	}
	
	// Switch to routing
	public static void switchWallFollowing(MapLocation destination) {
		System.out.println("Switch to Wall Following");
		
		try {
			// Pathing:
			Routing.path = new ArrayList<MapLocation>(); // Resets path to traverse with inputted path
			Routing.path.add(destination);
			
			Routing.prevPath = new ArrayList<Object[]>();
			
			// Get current location
			MapLocation curLoc = rc.getLocation();
			// Set initial distance to beat
			initialDist = curLoc.distanceTo(destination);
			
			// Get direction
			Direction toGoal = new Direction(curLoc, destination);
			
			stuckLoc = curLoc;
			progX = 1; progY = 0;
			if (toGoal.getDeltaY(1) > toGoal.getDeltaX(1)) {
				progX = 0; progY = 1;
			}
			if (toGoal.getDeltaX(1) < 0) {
				progX = -1*progX;
			}
			if (toGoal.getDeltaY(1) < 0) {
				progY = -1*progY;
			}
			
			wallFollow = true;
			
		} catch (Exception e) {
			System.out.println("Error in Wall Following Switch");
			e.printStackTrace(); // Print exceptions
		}
	}
	
	// Gets next point to move to
	public static ArrayList<MapLocation> MoveTo(MapLocation dest) {
		
		// Array to store the new path
		ArrayList<MapLocation> nextPoints = new ArrayList<MapLocation>();
		MapLocation curLoc = rc.getLocation();
		
		System.out.println(rotL);
		
		try {			
			// Indicator line pink for debug
			rc.setIndicatorLine(curLoc, dest, 255, 192, 203);
			
			// Get point distance
			float distance = curLoc.distanceTo(dest);
			
			// If distance is too far, truncate
			float distDest = distance;
			if (distDest > maxDist) {
				distDest = maxDist;
			}
			distIntervals = distDest;
			
			Direction destDir = new Direction(curLoc, dest);
			
			// If can move in intended direction, exit
			if (!rc.isCircleOccupiedExceptByThisRobot(curLoc.add(destDir, distDest), rc.getType().bodyRadius)) {
				System.out.print("Seeing if path has cleared");
				if (((progX * (curLoc.x - stuckLoc.x) > maxDist) && (progX != 0)) ||
						((progY * (curLoc.y - stuckLoc.y) > maxDist) && (progY != 0))) {
//				if(distance < initialDist) {
					if(!Routing.checkPrevPath(curLoc.add(destDir, distDest))) {
						if (Move.tryMoveReturn(destDir, degreeOffset, checks, distDest, distIntervals, nextPoints)) {
						
							System.out.println("Switching to routing mode");
							nextPoints.add(dest); // Append final destination to path
							wallFollow = false;
							return nextPoints;
						}
					}					
				}
			}
			
			rc.setIndicatorLine(curLoc, curLoc.add(startAngle,2), 255, 0, 0);
			if (tryWallFollowReturn(startAngle, wfOffset, maxDist, maxDist/2, nextPoints, curLoc)) {
				nextPoints.add(dest); // Append final destination to path
				return nextPoints;
			}
			else {
				// Can't move
				nextPoints.add(curLoc);
				nextPoints.add(dest); 
				return new ArrayList<MapLocation>();
			}
		
		} catch (Exception e) {
			System.out.println("Error in Wall Following Move Select");
			e.printStackTrace(); // Print exceptions
			
			
		}
		nextPoints = new ArrayList<MapLocation>();
		nextPoints.add(curLoc);
		nextPoints.add(dest); 
		return nextPoints;
	}
	
	public static boolean tryWallFollowReturn(Direction dir, float degreeOffset, float distance, float distanceInterval, 
												ArrayList<MapLocation> newPath, MapLocation curLoc) throws GameActionException {

		MapLocation moveLoc = tryWallFollow(dir, degreeOffset, distance, distanceInterval, rc.getType().bodyRadius, curLoc);
		
		if(moveLoc != null) {
			newPath.add(moveLoc);
			return true;
		}
			else {
			return false;
		}
	}
	
	public static MapLocation tryWallFollow(Direction dir, float degreeOffset, float distance, float distanceInterval,
	float bodyRadius, MapLocation curLoc) throws GameActionException {
	
	
		// Set first distance length to check to be max	    
		float totalAngle = 0;
		MapLocation nextLoc = curLoc;
		
		Direction dirBU = dir;
		int modifiedAngle = 0;
		
		System.out.println("Trying to wall-follow");
		// Toggle based off starting angle state:
		// Blocked:
		if((rc.isCircleOccupiedExceptByThisRobot(curLoc.add(dir,(float)maxDist), bodyRadius))
				|| (!rc.onTheMap(curLoc.add(dir,(float)maxDist), bodyRadius))) {
			System.out.println("Starting: wall");
			
			while (totalAngle < 359) {
			
				float distanceCheck = distance;
				
				// While distance length is above a certain threshold, continue searching
				while(distanceCheck > 0.0001) {
					System.out.println(dir + ", " + distanceCheck);
					
					//rc.setIndicatorLine(curLoc, curLoc.add(dir, 1), 0, 0, 255);
					
					//if(!rc.isCircleOccupiedExceptByThisRobot(curLoc.add(dir,(float)maxDist+bodyRadius), bodyRadius)) {
					if((!rc.isCircleOccupiedExceptByThisRobot(curLoc.add(dir,(float)distanceCheck), bodyRadius))
							&& (rc.onTheMap(curLoc.add(dir,(float)distanceCheck), bodyRadius))) {
						//if(rc.canMove(dir,distanceCheck)) {
						
	//					Direction dirRot = dir.rotateLeftDegrees(-rotL * 90);
//						Direction dirMove = dir.rotateLeftDegrees(rotL * 5);
						Direction dirMove = dir;
						
						System.out.println("Checking if can move...");
						
						if(rc.canMove(dirMove,distanceCheck)) {
							// Check if previously visited
							if(!Routing.checkPrevPath(curLoc.add(dirMove, distanceCheck))) {
								rc.setIndicatorLine(curLoc, curLoc.add(dirMove, 1), 0, 255, 0);
								
								Direction dirRot = dir.rotateLeftDegrees(-rotL * 90);
								
								startAngle = dirRot;
//								System.out.println(dirMove + ", " + distanceCheck);
								return curLoc.add(dirMove,distanceCheck);
							}
							else {
								nextLoc = curLoc.add(dirMove, distanceCheck);
								
								Direction dirRot = dir.rotateLeftDegrees(-rotL * 90);
								startAngle = dirRot;
							}
							//System.out.println(-rotL * 90);
						}
					}
					else {
						rc.setIndicatorLine(curLoc, curLoc.add(dir, 1), 0, 0, 255);
					}
					// Set next distance to be check
					// Decrease by interval value set
					distanceCheck -= distanceInterval; 
				}
				
				// Check if we are at HV components, and add extra check
				dir = dir.rotateLeftDegrees(rotL * degreeOffset);
				dirBU = dir;
				if((Math.abs(dir.degreesBetween(Direction.EAST)) <= degreeOffset) && (modifiedAngle <= 0)) {
					modifiedAngle = 2;
					dir = Direction.EAST;
				}
				else if((Math.abs(dir.degreesBetween(Direction.SOUTH)) <= degreeOffset) && (modifiedAngle <= 0)) {
					modifiedAngle = 2;
					dir = Direction.SOUTH;
				}
				else if((Math.abs(dir.degreesBetween(Direction.WEST)) <= degreeOffset) && (modifiedAngle <= 0)) {
					modifiedAngle = 2;
					dir = Direction.WEST;
				}
				else if((Math.abs(dir.degreesBetween(Direction.NORTH)) <= degreeOffset) && (modifiedAngle <= 0)) {
					modifiedAngle = 2;
					dir = Direction.NORTH;
				}
				else {
					totalAngle += degreeOffset;
					modifiedAngle--;
					
					if(modifiedAngle > 0) {
						dir = dirBU;
					}
				}
			}
		}
		// Free:
		else {
			System.out.println("Starting: empty");
			while (totalAngle <= 360) {
				
				float distanceCheck = distance;
				
				// While distance length is above a certain threshold, continue searching
				while(distanceCheck > 0.0001) {
					//System.out.println(dir + ", " + distanceCheck);
					
					//rc.setIndicatorLine(curLoc, curLoc.add(dir, 1), 0, 0, 255);
					
					//if(rc.isCircleOccupiedExceptByThisRobot(curLoc.add(dir,(float)maxDist+bodyRadius), bodyRadius)) {
					if((rc.isCircleOccupiedExceptByThisRobot(curLoc.add(dir,(float)distanceCheck), bodyRadius))
							|| (!rc.onTheMap(curLoc.add(dir,(float)maxDist), bodyRadius))) {
						//if(rc.canMove(dir,distanceCheck)) {
						
	//					Direction dirRot = dir.rotateLeftDegrees(-rotL * 90);
						Direction dirMove = dir.rotateLeftDegrees(-rotL * degreeOffset);
//						Direction dirMove = dir;
						
						if(rc.canMove(dirMove,distanceCheck)) {
							// Check if previously visited
							if(!Routing.checkPrevPath(curLoc.add(dirMove, distanceCheck))) {
								rc.setIndicatorLine(curLoc, curLoc.add(dirMove, 1), 0, 255, 0);
								
								Direction dirRot = dir.rotateLeftDegrees(rotL * 90);
								
								startAngle = dirRot;
//								System.out.println(dirMove + ", " + distanceCheck);
								return curLoc.add(dirMove,distanceCheck);
							}
							else {
								nextLoc = curLoc.add(dirMove, distanceCheck);
								
								Direction dirRot = dir.rotateLeftDegrees(rotL * 90);
								startAngle = dirRot;
							}
							//System.out.println(-rotL * 90);
							
						}
					}
					else {
						rc.setIndicatorLine(curLoc, curLoc.add(dir, 1), 0, 0, 255);
					}
					// Set next distance to be check
					// Decrease by interval value set
					distanceCheck -= distanceInterval; 
				}
				
				dir = dir.rotateLeftDegrees(rotL * degreeOffset);
				totalAngle += degreeOffset;
			}
		}
		
		// A move never happened, so return false.
		return nextLoc;
	}	
}