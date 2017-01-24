//----------------------------------------------------------------------------------
// Routing.java
// Alan Cheng (adcheng@mit.edu)
// 
// Info: This class houses all related movement to moving through a path
// TODO: Wall following
//----------------------------------------------------------------------------------
package naclbot.units.motion.routing;

// Imports from package:
import naclbot.units.motion.Move;
import naclbot.variables.GlobalVars;

// Imports from Battlecode packages
import battlecode.common.*;

// Imports from Java standard library
import java.util.Arrays;
import java.util.ArrayList;

//----------------------------------------------------------------------------------
// This class contains the entire routing movement
public class Routing extends GlobalVars{
	
	// Variable declarations:
	// - Pathing:
	public static ArrayList<MapLocation> path = new ArrayList<MapLocation>(); // Stores the path to be followed
	
	// - Scanning:
	// * This is primarily for tryMoveRouting(Direction dir, float degreeOffset, int checksPerSide, float distance, float distanceInterval)
	public static float degreeOffset = 2;  // Spacing between checked directions (degrees)
	public static int checks = 3; // Number of extra directions checked on each side, if intended direction was unavailable
	public static float maxDist = rc.getType().strideRadius; // Maximum length of movement (default is strideRadius of unit)
	public static float distIntervals = maxDist / 3; // Interval of each length check
	
	// - Wall following:
	// TODO: Comment later
	public static Direction prevDir = new Direction(0);
	public static int reRoute = 0; 
	public static boolean newDest = false;
	
	public static MapLocation prevLoc = rc.getLocation();
	public static MapLocation curLoc = new MapLocation(prevLoc.x,prevLoc.y);
	public static MapLocation lastDest = new MapLocation(prevLoc.x,prevLoc.y);
	
	/**
	 * Sets path to traverse and resets wall following variables
	 *
	 * @param pathNew New path to follow
	 */
	public static void setRouting(ArrayList<MapLocation> pathNew) {
		// Pathing:
		path = new ArrayList<MapLocation>(pathNew); // Resets path to traverse with inputted path
		
		// Reset wall following variables:
		// TODO: Comment later
		prevDir = new Direction(0);
		reRoute = 0; 
		newDest = false;
		
		prevLoc = rc.getLocation();
		curLoc = new MapLocation(prevLoc.x,prevLoc.y);
		lastDest = new MapLocation(prevLoc.x,prevLoc.y);
	}
	
	public static void resetRouting() {
		prevDir = new Direction(0);
		
		prevLoc = rc.getLocation();
		curLoc = new MapLocation(prevLoc.x,prevLoc.y);
		lastDest = new MapLocation(prevLoc.x,prevLoc.y);
		
		if (path.size() > 0) {
			MapLocation dest = path.get(path.size()-1);
			path = new ArrayList<MapLocation>();
			path.add(dest);
		}
	}
	
	/**
	 * Wrapper function for movement through path.
	 */	
	public static void routingWrapper() {
		try {
			// Wall following:
			// TODO: Comment later
	    	curLoc = rc.getLocation(); // Get current location

	    	Routing.prevDir = new Direction(prevLoc, curLoc);
	    	// Update previous location
	    	// This is as moving occurs below this
	    	prevLoc = new MapLocation(curLoc.x,curLoc.y);
	    	
 		   	System.out.println(path.size());
//    		System.out.println("ReRoute: " + Routing.reRoute);
	    	
	    	// Pathing:
	    	// If there is a path to move through, then attempt moving to each point one at a time
	    	if(!(path.isEmpty())) {
	    		System.out.println("Path: " + path + ", Size: " + path.size());
	    		
	    		// Transfer the first point to nextLoc
	    		// If point isn't reached, it will be re-added to Path later 
	    		MapLocation nextLoc = path.remove(0);
	    		
	    		while(path.size() > 0) {
	    			if(nextLoc.isWithinDistance(curLoc, (float)0.8)) {
	    				nextLoc = path.remove(0);
	    				System.out.println("Removed: " + nextLoc);
		    		}
	    			else {
	    				break;
	    			}
	    		}
	    		
//    			System.out.println(nextLoc);
	    		
	    		// Calling function to attempt to move to point
	    		ArrayList<MapLocation> newPath = Routing.moveToPoint(nextLoc);
//	    		path = Routing.moveToPoint(nextLoc);
	    		
	    		if(!(newPath.isEmpty())) {
	    			// Add entire newPath to path
    				for (int i = newPath.size()-1; i > -1; i--) {
    					path.add(0, newPath.get(i));
    				}
	    		}
	    		
	    		/*
	    		// If there is a path resulting from the movement...
	    		// - It is only empty when the robot is stuck
	    		if(!(newPath.isEmpty())) {
    				System.out.println("New Path: " + newPath);
	    			
	    			// Detect if we have not reached point
	    			if(!(newPath.get(0).isWithinDistance(rc.getLocation(), (float)0.5))) {
//    					System.out.println("Not at point");
	    				
	    				// Add entire newPath to path
	    				for (int i = newPath.size()-1; i > -1; i--) {
	    					path.add(0, newPath.get(i));
	    				}
	    			}
	    			
	    			// Point is reached
	    			else
	    			{
	    				// Add newPath except for point just reached (at index 0)
	    				for (int i = newPath.size()-1; i > 0; i--) {
	    					path.add(0, newPath.get(i));
	    				}
	    				
	    				// Wall following:
	    				// Set signal that point is found, so new point will be taken
	    				// and store this point that has been cleared
	    				Routing.newDest = true;
	    				lastDest = new MapLocation(newPath.get(0).x, newPath.get(0).y);
	    			}
	    		}
	    		// No continuing path, so it is halted
	    		else {
	    			path = new ArrayList<MapLocation>();
	    		}*/
	    	}
	    // Catch for all exceptions
		} catch (Exception e) {
			e.printStackTrace(); // Print exceptions
		}
	    	
	}
	
	/**
	 * Attempts to move to a given point. Has 3 stages:
	 * Stage 1: Attempts to move in direction of point
	 * Stage 2: If not possible, search the horizontal and vertical components of the direction
	 * Stage 3: If not possible, search for empty space behind the unit in the same horizontal and vertical components
	 * 
	 * @param MapLocation dest Destination point to attempt to move to
	 * @return ArrayList<MapLocation> of path to follow in the next turn
	 */
	public static ArrayList<MapLocation> moveToPoint(MapLocation dest) {
		
		// Array to store the new path
		ArrayList<MapLocation> nextPoints = new ArrayList<MapLocation>();
		
		// Distance from current location to destination
		float distance = curLoc.distanceTo(dest);
		float distDest = distance;
		if (distDest > maxDist) {
			distDest = maxDist;
		}
		distIntervals = distDest / 3;
		
		// Try-catch to catch exceptions
		try {			
//			MapLocation curLoc = rc.getLocation();
			
			// Get robot type (for properties later)
			RobotType curType = rc.getType();
			
			// Draw pink line for reference
			// rc.setIndicatorLine(curLoc, dest, 255, 192, 203);
			
			// Get direction to point
			Direction destDir = new Direction(curLoc, dest);
			
			// Try moving in that direction
			
			if(!(rc.isCircleOccupiedExceptByThisRobot(curLoc, (float)1.5*curType.bodyRadius)) || (distance < curType.sensorRadius)) {
			//if(!(rc.isCircleOccupiedExceptByThisRobot(curLoc, curType.sensorRadius/3)) || (distance < curType.sensorRadius)) {
				System.out.println("Distance < sensorRadius: " + distance);
				if(tryMoveReturn(destDir, degreeOffset, checks, distDest, distDest, nextPoints)) {
					// Movement successful with no edits
					nextPoints.add(dest); // Append final destination to path
					return nextPoints;
				}
				else if(tryMoveReturn(prevDir, degreeOffset, checks, distDest, distIntervals, nextPoints)) {
					// Movement successful with no edits
					nextPoints.add(dest); // Append final destination to path
					return nextPoints;
				}
			} /*else if(rc.isCircleOccupiedExceptByThisRobot(curLoc, (float)1.5*curType.bodyRadius)) {
				
				if(tryMoveRouting(prevDir, degreeOffset, checks, distDest, distIntervals)) {
					// Movement successful with no edits
					nextPoints.add(dest); // Append final destination to path
					return nextPoints;
				}
			}*/
			// Used in Stage 3: determines what are the horizontal and vertical components
			int rightMod = 1;
			int upMod = 1;
			
			// Get angle of movement:
			float angleDir = destDir.getAngleDegrees();
			
			System.out.println("Blocked; Angle: " + angleDir);
			
			// Case 0: If direction is in a cardinal direction:
			if ((Math.abs(angleDir) % 90) <= 1) {
//					System.out.println("Angle ~ 0");
				
				// Try moving at 90 degree offsets
				// Right now, it prioritizes moving Up;Right / Down;Left
				
				// First try 90 degrees left
				Direction newDir = destDir.rotateLeftDegrees(90);
				
				if(!(tryMoveReturn(destDir, 90, checks, distDest, distIntervals, nextPoints))) {
					// Failure to move left:
					
					// Then try 90 degrees right
					newDir = destDir.rotateRightDegrees(90);
					
					// Attempt movement right
					if(!(tryMoveReturn(newDir, 90, checks, distDest, distIntervals, nextPoints))) {
						// Failure to move right:
						// Jump to Stage 3 - search behind unit in perpendicular directions
						
						nextPoints = moveAroundTree(curLoc, destDir, curType.bodyRadius);
						// nextPoints is empty if there are no space; otherwise append re-routing path
						if(!(nextPoints.isEmpty())) {
							nextPoints.add(dest); // Add final destination to path
							reRoute = 3;
							return nextPoints; // Return path to exit function
						}
						
						// If intended movement is horizontal
						if((Math.abs(angleDir) < 45) && (Math.abs(angleDir) > 135)) {
							rightMod = 1; // Default to Right
							
							// Detect if movement is Left
							if(Math.abs(angleDir) > 135) {
								rightMod = -1; // Switch modifier to Left
							}
							
							// Check vertical lines behind unit
							// First check Up
							// - Checks up to sensorRadius boundary at intervals of bodyRadius
							for(float hOffset = curType.bodyRadius/2; hOffset < curType.sensorRadius-curType.bodyRadius; hOffset += curType.bodyRadius) {
								nextPoints = viewVertical(curLoc, curType.sensorRadius, curType.bodyRadius, hOffset, rightMod, 1);
								
								// nextPoints is empty if there are no space; otherwise append re-routing path
								if(!(nextPoints.isEmpty())) {
									nextPoints.add(dest); // Add final destination to path
									reRoute = 3;
									return nextPoints; // Return path to exit function
								}
							}
							// If no space found, check Down
							// - Checks up to sensorRadius boundary at intervals of bodyRadius
							for(float hOffset = curType.bodyRadius/2; hOffset < curType.sensorRadius-curType.bodyRadius; hOffset += curType.bodyRadius) {
								nextPoints = viewVertical(curLoc, curType.sensorRadius, curType.bodyRadius, hOffset, rightMod, -1);
								
								// nextPoints is empty if there are no space; otherwise append re-routing path
								if(!(nextPoints.isEmpty())) {
									nextPoints.add(dest); // Add final destination to path
									reRoute = 3;
									return nextPoints; // Return path to exit function
								}
							}
						}
						
						// Intended movement is vertical
						else {
							upMod = 1; // Default to Up
							
							// Detect if movement is Down
							if(angleDir < 0) {
								upMod = -1; // Switch modifier to Left 
							}
							
							// Check horizontal lines behind unit
							// First check Right
							// - Checks up to sensorRadius boundary at intervals of bodyRadius
							for(float vOffset = curType.bodyRadius/2; vOffset < curType.sensorRadius-curType.bodyRadius; vOffset += curType.bodyRadius) {
								nextPoints = viewHorizontal(curLoc, curType.sensorRadius, curType.bodyRadius, vOffset, 1, upMod);
								
								// nextPoints is empty if there are no space; otherwise append re-routing path
								if(!(nextPoints.isEmpty())) {
									nextPoints.add(dest); // Add final destination to path
									reRoute = 3;
									return nextPoints;
								}
							}
							
							// If no space found, check Left
							// - Checks up to sensorRadius boundary at intervals of bodyRadius
							for(float vOffset = curType.bodyRadius/2; vOffset < curType.sensorRadius-curType.bodyRadius; vOffset += curType.bodyRadius) {
								nextPoints = viewHorizontal(curLoc, curType.sensorRadius, curType.bodyRadius, vOffset, -1, upMod);
							
								// nextPoints is empty if there are no space; otherwise append re-routing path
								if(!(nextPoints.isEmpty())) {
									nextPoints.add(dest); // Add final destination to path
									reRoute = 3;
									return nextPoints; // Return path to exit function
								}
							}
						}
						
						// No possible path found
						return new ArrayList<MapLocation>();
					}
				}
				
			}
			// Case 1: If direction at angle <= 45 relative to horizontal
//			else if (((Math.abs(angleDir) % 90) <= 45)||((Math.abs(angleDir) % 90) >= 135)) {
			else if ((Math.abs(angleDir) <= 45)||(Math.abs(angleDir) >= 135)) {
				System.out.println("Angle <= 45");
				
				// newAngle stores either 0, PI/2, -PI/2, or PI
				// In other words, it dictates movements in a cardinal direction
				float newAngle = 0; // Default to Right
				
				// Check if movement is Left
				if(Math.abs(angleDir) > 90) {
					newAngle = (float)Math.PI; // Switch to Left
					rightMod = -1; // Update modifier
				}
				
				// Create new direction to move to based off cardinal direction
				Direction newDestDir = new Direction(newAngle);

				if(!(tryMoveReturn(newDestDir, degreeOffset, checks, distDest, distIntervals, nextPoints))) {
					// Movement failure
					
					// Draw Red line to indicate failed direction
					// rc.setIndicatorLine(curLoc, curLoc.add(newDestDir), 255, 0, 0);
					
					// Not succesful; set up trying vertical
					newAngle = (float)Math.PI/2; // Default is Up
					
					// Check if movement is Down
					if(angleDir < 0) {
						newAngle = (float)(-1*(Math.PI/2)); // Switch to Down
						upMod = -1; // Update modifier
					}
					
					// Update new direction to move to based off cardinal direction
					newDestDir = new Direction(newAngle);
					
					if(!(tryMoveReturn(newDestDir, degreeOffset, checks, distDest, distIntervals, nextPoints))) {
						
						// Draw Red line to indicate failed direction
						// rc.setIndicatorLine(curLoc, curLoc.add(newDestDir), 255, 0, 0);
						
//						System.out.println(rightMod + ", " + upMod);
						
						nextPoints = moveAroundTree(curLoc, destDir, curType.bodyRadius);
						// nextPoints is empty if there are no space; otherwise append re-routing path
						if(!(nextPoints.isEmpty())) {
							nextPoints.add(dest); // Add final destination to path
							reRoute = 3;
							return nextPoints; // Return path to exit function
						}
						
						// Check horizontal lines behind unit
						// - Checks up to sensorRadius boundary at intervals of bodyRadius
						for(float vOffset = curType.bodyRadius/2; vOffset < 1*curType.sensorRadius-curType.bodyRadius; vOffset += curType.bodyRadius) {
							nextPoints = viewHorizontal(curLoc, curType.sensorRadius, curType.bodyRadius, vOffset, rightMod, upMod);
							
							// nextPoints is empty if there are no space; otherwise append re-routing path
							if(!(nextPoints.isEmpty())) {
								nextPoints.add(dest); // Add final destination to path
								reRoute = 3;
								return nextPoints; // Return path to exit function
							}
						}
						
						// Now check vertical lines
						for(float hOffset = curType.bodyRadius/2; hOffset < 1*curType.sensorRadius-curType.bodyRadius; hOffset += curType.bodyRadius) {
							nextPoints = viewVertical(curLoc, curType.sensorRadius, curType.bodyRadius, hOffset, rightMod, upMod);
							
							// nextPoints is empty if there are no space; otherwise append re-routing path
							if(!(nextPoints.isEmpty())) {
								nextPoints.add(dest); // Add final destination to path
								reRoute = 3;
								return nextPoints; // Return path to exit function
							}
						}
					}
				}
			}
			
			// Case 2: If direction at angle > 45
			else {
				System.out.println("Angle > 45");
				
				// newAngle stores either 0, PI/2, -PI/2, or PI
				// In other words, it dictates movements in a cardinal direction
				float newAngle = (float)Math.PI/2; // Default is Up
				
				// Check if movement is Down
				if(angleDir < 0) {
					newAngle = (float)(-1*(Math.PI/2)); // Switch to Up
					upMod = -1; // Update modifier
				}
				
				// Update new direction to move to based off cardinal direction
				Direction newDestDir = new Direction(newAngle);
				
				if(!(tryMoveReturn(newDestDir, degreeOffset, checks, distDest, distIntervals, nextPoints))) {
					
					// Draw Red line to indicate failed direction
					// rc.setIndicatorLine(curLoc, curLoc.add(newDestDir), 255, 0, 0);
					
					// Not succesful; set up trying horizontal
					newAngle = 0;
					
					// Check if movement is Left
					if(Math.abs(angleDir) > 90) {
						newAngle = (float)Math.PI; // Switch to Left
						rightMod = -1; // Update modifier
					}
										
					newDestDir = new Direction(newAngle);
					
					// Try moving in that direction
					if(!(tryMoveReturn(newDestDir, degreeOffset, checks, distDest, distIntervals, nextPoints))) {
						
						// Draw Red line to indicate failed direction
						// rc.setIndicatorLine(curLoc, curLoc.add(newDestDir), 255, 0, 0);
						
//						System.out.println(rightMod + ", " + upMod);
						
						nextPoints = moveAroundTree(curLoc, destDir, curType.bodyRadius);
						// nextPoints is empty if there are no space; otherwise append re-routing path
						if(!(nextPoints.isEmpty())) {
							nextPoints.add(dest); // Add final destination to path
							reRoute = 3;
							return nextPoints; // Return path to exit function
						}
						
						// Check vertical lines behind unit
						// - Checks up to sensorRadius boundary at intervals of bodyRadius
						for(float hOffset = curType.bodyRadius/2; hOffset < 1*curType.sensorRadius-curType.bodyRadius; hOffset += curType.bodyRadius) {
							nextPoints = viewVertical(curLoc, curType.sensorRadius, curType.bodyRadius, hOffset, rightMod, upMod);
							
							// nextPoints is empty if there are no space; otherwise append re-routing path
							if(!(nextPoints.isEmpty())) {
								nextPoints.add(dest); // Add final destination to path
								reRoute = 3;
								return nextPoints; // Return path to exit function
							}
						}
						
						// Check horizontal lines behind unit
						// - Checks up to sensorRadius boundary at intervals of bodyRadius
						for(float vOffset = curType.bodyRadius/2; vOffset < 1*curType.sensorRadius-curType.bodyRadius; vOffset += curType.bodyRadius) {
							nextPoints = viewHorizontal(curLoc, curType.sensorRadius, curType.bodyRadius, vOffset, rightMod, upMod);
							
							// nextPoints is empty if there are no space; otherwise append re-routing path
							if(!(nextPoints.isEmpty())) {
								nextPoints.add(dest); // Add final destination to path
								reRoute = 3;
								return nextPoints; // Return path to exit function
							}
						}
						
						// No path found
						return new ArrayList<MapLocation>();
					}
				}
			}
		// Catch for all exceptions
		} catch (Exception e) {
			e.printStackTrace(); // Print exception
			return new ArrayList<MapLocation>(); // Return empty path
		}
		// Movement successful with no edits
		nextPoints.add(dest); // Append final destination to path
		return nextPoints;
	}
	
	/**
	 * Attempts to route around tree blocking path
	 *
	 * @param curLoc Unit's current location
	 * @param dir Unit's intended direction
	 * @param dir Unit's body radius
	 * @return Updated path to follow (i.e. waypoints) on way to goal. If no path, returns empty
	 */
	public static ArrayList<MapLocation> moveAroundTree(MapLocation curLoc, Direction dir, float bodyRadius) {
		System.out.println("Attempt tree routing");		
		
		// Array to store next points (is returned)
		ArrayList<MapLocation> nextPoints = new ArrayList<MapLocation>();
		
		// Lower tolerances for rounding errors
		float bodyRadiusCheck = bodyRadius - (float)0.1;
		
		try {
			
			// Get Unit's intended location
			MapLocation potPoint = curLoc.add(dir, maxDist + bodyRadius);
			System.out.println("Attempted location: " + potPoint);
			
			// Get intended direction angle
			float angleDir = dir.getAngleDegrees();
			System.out.println("Angle: " + angleDir);
			System.out.println("dx: " + dir.getDeltaX(1) + ", dy: " + dir.getDeltaY(1));
			
			// Modifiers to determine direction of intended movement
			int modUp = 1;
			int modRight = 1;
			// Get intended direction X,Y components to set modifiers
			if (dir.getDeltaY(1) < 0) {
				modUp = -1; // Switch to Left
			}
			if (dir.getDeltaX(1) < 0) {
				modRight = -1; // Switch to Down
			}
			
			// Get blocking tree of path; and corresponding location and radius
			TreeInfo blockTree = rc.senseTreeAtLocation(potPoint);

			// If no tree found
			if (blockTree == null) {
				
				// Get very close trees
				TreeInfo[] nearbyTrees = rc.senseNearbyTrees(bodyRadius + (float)0.5);
				for(int i = 0; i < nearbyTrees.length; i++) {
					MapLocation nTreeLoc = nearbyTrees[i].location;
					if (((nTreeLoc.x - curLoc.x)*modRight > 0) && ((nTreeLoc.y - curLoc.y)*modUp > 0)) {
						blockTree = nearbyTrees[i];
						break;
					}
					blockTree = nearbyTrees[0];
				}
			}
			
			MapLocation treeLoc = blockTree.location;
			
			// Draw tree point
			// rc.setIndicatorDot(treeLoc, 0, 255, 0); // Mark point green
			
			float treeRad = blockTree.radius;
			
			// Check if points next to tree is empty
			// Expand in X direction
			MapLocation point1 = new MapLocation(treeLoc.x - modRight * (treeRad + bodyRadius), treeLoc.y);
			// Expand in Y direction
			MapLocation point2 = new MapLocation(treeLoc.x, treeLoc.y - modUp * (treeRad + bodyRadius));
			
			System.out.println("Point 1: " + point1 + ", Point 2: " + point2);
			
			// Case 1: If direction at angle <= 45 relative to horizontal
//			if (((Math.abs(angleDir) % 90) <= 45)||((Math.abs(angleDir) % 90) >= 135)) {
			if ((Math.abs(angleDir) <= 45)||(Math.abs(angleDir) >= 135)) {
				
				// Check if horizontal path is clear
				if(!(rc.isCircleOccupiedExceptByThisRobot(point2,bodyRadiusCheck))) {
					
					// Point is free
					// rc.setIndicatorDot(point2, 0, 0, 255); // Mark point blue
					
					// Append to path
					// Intermediate point along re-routed path
					MapLocation interLoc = new MapLocation(curLoc.x, point2.y);
					
					// Corresponding intermediate direction
					Direction interDir = new Direction(curLoc, interLoc);
					
					// Check if movement is outside movement range
					float maxMove = Math.abs(point2.y - curLoc.y);
					if (maxMove > maxDist) {
						maxMove = maxDist; // Set to max range
					}
					
					// Set move intervals
					distIntervals = maxMove / 3;
					
					// Attempt to move to intermediate point
					tryMoveReturn(interDir, degreeOffset, checks, maxMove, distIntervals, nextPoints);
					
					// Add re-routed path
					nextPoints.add(interLoc);
					nextPoints.add(point2);
					
					return nextPoints;
					
				}
				
				// Check if vertical path is clear
				else if(!(rc.isCircleOccupiedExceptByThisRobot(point1,bodyRadiusCheck))) {
	
					// Point is free (but not previous)
					// rc.setIndicatorDot(point1, 0, 0, 255); // Mark point blue
					// rc.setIndicatorDot(point2, 255, 0, 0); // Mark point red
					
					// Append to path
					// Intermediate point along re-routed path
					MapLocation interLoc = new MapLocation(point1.x, curLoc.y);
					
					// Corresponding intermediate direction
					Direction interDir = new Direction(curLoc, interLoc);
					
					// Check if movement is outside movement range
					float maxMove = Math.abs(point1.x - curLoc.x);
					if (maxMove > maxDist) {
						maxMove = maxDist; // Set to max range
					}
					
					// Set move intervals
					distIntervals = maxMove / 3;
					
					System.out.println(maxMove);
					
					// Attempt to move to intermediate point
					tryMoveReturn(interDir, degreeOffset, checks, maxMove, distIntervals, nextPoints);
					
					// Add re-routed path
					nextPoints.add(interLoc);
					nextPoints.add(point1);
					
					return nextPoints;
				}
				
				// No points are clear
				// rc.setIndicatorDot(point1, 255, 0, 0); // Mark point red
				// rc.setIndicatorDot(point2, 255, 0, 0); // Mark point red
			}
			
			// Case 2: If direction at angle > relative to horizontal
			else {
				// Check if horizontal path is clear
				if(!(rc.isCircleOccupiedExceptByThisRobot(point1,bodyRadiusCheck))) {
					
					// Point is free
					// rc.setIndicatorDot(point1, 0, 0, 255); // Mark point blue
					
					// Append to path
					// Intermediate point along re-routed path
					MapLocation interLoc = new MapLocation(point1.x, curLoc.y);
					
					// Corresponding intermediate direction
					Direction interDir = new Direction(curLoc, interLoc);
					
					// Check if movement is outside movement range
					float maxMove = Math.abs(point1.x - curLoc.x);
					if (maxMove > maxDist) {
						maxMove = maxDist; // Set to max range
					}
					
					// Set move intervals
					distIntervals = maxMove / 3;
					
					// Attempt to move to intermediate point
					tryMoveReturn(interDir, degreeOffset, checks, maxMove, distIntervals, nextPoints);
					
					// Add re-routed path
					nextPoints.add(interLoc);
					nextPoints.add(point1);
					
					return nextPoints;
					
				}
				
				// Check if vertical path is clear
				else if(!(rc.isCircleOccupiedExceptByThisRobot(point1,bodyRadiusCheck))) {
					
					// Point is free (but not previous)
					// rc.setIndicatorDot(point1, 255, 0, 0); // Mark point red
					// rc.setIndicatorDot(point2, 0, 0, 255); // Mark point blue
					
					// Append to path
					// Intermediate point along re-routed path
					MapLocation interLoc = new MapLocation(curLoc.x, point2.y);
					
					// Corresponding intermediate direction
					Direction interDir = new Direction(curLoc, interLoc);
					
					// Check if movement is outside movement range
					float maxMove = Math.abs(point2.y - curLoc.y);
					if (maxMove > maxDist) {
						maxMove = maxDist; // Set to max range
					}
					
					// Set move intervals
					distIntervals = maxMove / 3;
					
					// Attempt to move to intermediate point
					tryMoveReturn(interDir, degreeOffset, checks, maxMove, distIntervals, nextPoints);
					
					// Add re-routed path
					nextPoints.add(interLoc);
					nextPoints.add(point2);
					
					return nextPoints;
	
				}
				
				// No points are clear
				// rc.setIndicatorDot(point1, 255, 0, 0); // Mark point red
				// rc.setIndicatorDot(point2, 255, 0, 0); // Mark point red
			}
		// Catch all exceptions
		} catch (Exception e) {
			e.printStackTrace(); // Print errors
			return new ArrayList<MapLocation>();
		}
		
		// No immediate paths around tree
		return new ArrayList<MapLocation>();
		
	}
	
	/**
	 * Attempts to view potential horizontal paths behind the unit
	 *
	 * @param curLoc Unit's current location
	 * @param radius Unit's sight radius
	 * @param bodyRadius Unit's body radius
	 * @param offsetY How far behind the unit we are searching for
	 * @param rightMod Intended horizontal direction
	 * @param upMod Intended vertical direction
	 * @return Updated path to follow (i.e. waypoints) on way to goal. If no path, returns empty
	 */
	public static ArrayList<MapLocation> viewHorizontal(MapLocation curLoc, float radius, float bodyRadius, float offsetY, int rightMod, int upMod) {
		
		// Array to store next points (is returned)
		ArrayList<MapLocation> nextPoints = new ArrayList<MapLocation>();
		
		// Location of point along potential path
		MapLocation potLoc = new MapLocation(curLoc.x, curLoc.y);
		
		// Try-catch to catch exceptions
		try {
			float vLen = offsetY; // Vertical component of path
			
			// Horizontal max from Pythagoras
			float maxHLen = (float)Math.sqrt((radius * radius ) - (offsetY * offsetY));
			
			// Iterate in increments of body radius up till the calculated max horizontal length
			float hLen = 0;
			for(hLen = 0; hLen < 0.7*maxHLen-bodyRadius; hLen += bodyRadius) {
//				System.out.println("hLen, vLen: " + hLen + ", " + vLen + "; Max: " + maxHLen);
				
				// Update with potential point along path
				potLoc = new MapLocation(rightMod * hLen + curLoc.x, -upMod * vLen + curLoc.y);
				
				// Check if point is not free
				if(rc.isCircleOccupiedExceptByThisRobot(potLoc,bodyRadius)) {
					// rc.setIndicatorDot(potLoc, 153, 0, 0); // Mark point red
					return new ArrayList<MapLocation>(); // Return empty (no path)
				}
				
				// Point is free
				// rc.setIndicatorDot(potLoc, 0, 204, 204); // Mark point blue
			}
			potLoc = new MapLocation(rightMod * hLen/2 + curLoc.x, -upMod * vLen + curLoc.y);
			
			// Intermediate point along re-routed path
			MapLocation interLoc = new MapLocation(curLoc.x, potLoc.y);
			
			// Corresponding intermediate direction
			Direction interDir = new Direction(curLoc, interLoc);
			
			// Check if movement is outside movement range
			float maxMove = offsetY;
			if (offsetY > maxDist) {
				maxMove = maxDist; // Set to max range
			}
			
			// Set move intervals
			distIntervals = maxMove / 3;
			
			// Attempt to move to intermediate point
			tryMoveReturn(interDir, degreeOffset, checks, maxMove, distIntervals, nextPoints);
			
			// Add re-routed path
			nextPoints.add(interLoc);
			nextPoints.add(potLoc);
			
		// Catch all exceptions
		} catch (Exception e) {
			e.printStackTrace(); // Print errors
			return new ArrayList<MapLocation>();
		}
		
		return nextPoints;
		
	}
	
	/**
	 * Attempts to view potential vertical paths behind the unit
	 *
	 * @param curLoc Unit's current location
	 * @param radius Unit's sight radius
	 * @param bodyRadius Unit's body radius
	 * @param offsetY How far behind the unit we are searching for
	 * @param rightMod Intended horizontal direction
	 * @param upMod Intended vertical direction
	 * @return Updated path to follow (i.e. waypoints) on way to goal. If no path, returns empty
	 */	
	public static ArrayList<MapLocation> viewVertical(MapLocation curLoc, float radius, float bodyRadius, float offsetX, int rightMod, int upMod) {
		
		// Array to store next points (is returned)
		ArrayList<MapLocation> nextPoints = new ArrayList<MapLocation>();
		
		// Location of point along potential path
		MapLocation potLoc = new MapLocation(curLoc.x, curLoc.y);
		
		// Try-catch to catch exceptions
		try {
			// Horizontal length
			float hLen = offsetX;
			
			// Vertical max from Pythagoras
			float maxVLen = (float)Math.sqrt((radius * radius ) - (offsetX * offsetX));
			
			// Iterate in increments of body radius up till the calculated max vertical length
			float vLen = 0;
			for(vLen = 0; vLen < 0.7*maxVLen-bodyRadius; vLen += bodyRadius) {
				
				// Update with potential point along path
				potLoc = new MapLocation(-rightMod * hLen + curLoc.x, upMod * vLen + curLoc.y); 
				
				// Check if point is not free
				if(rc.isCircleOccupiedExceptByThisRobot(potLoc,bodyRadius)) {
					// rc.setIndicatorDot(potLoc, 153, 0, 0); // Mark point red
					return new ArrayList<MapLocation>(); // Return empty (no path)
				}
				
				// Point is free
				// rc.setIndicatorDot(potLoc, 0, 204, 204);
			}
			potLoc = new MapLocation(-rightMod * hLen + curLoc.x, upMod * vLen/2 + curLoc.y);
			
			// Intermediate point along re-routed path
			MapLocation interLoc = new MapLocation(potLoc.x, curLoc.y);
			
			// Corresponding intermediate direction
			Direction interDir = new Direction(curLoc, interLoc);
			
			// Check if movement is outside movement range
			float maxMove = offsetX;
			if (potLoc.x > maxDist) {
				maxMove = maxDist; // Set to max range
			}
			
			// Set move intervals
			distIntervals = maxMove / 3;
			
			// Attempt to move to intermediate point
			tryMoveReturn(interDir, degreeOffset, checks, maxMove, distIntervals, nextPoints);
			
			// Add re-routed path
			nextPoints.add(interLoc);
			nextPoints.add(potLoc);
		
		// Catch all exceptions
		} catch (Exception e) {
			e.printStackTrace(); // Print exceptions
			return new ArrayList<MapLocation>();
		}
		
		return nextPoints;
		
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
	
	    // First, try intended direction
	    if (rc.canMove(dir, distance)) {
//	        rc.move(dir, distance);
	        return curLoc.add(dir,distance);
	    }
	    
	    // Intended direction not successful:
	    
	    // Set first distance length to check to be max
	    float distanceCheck = distance;
	    
	    System.out.println("Distance Max: " + distance);
	    
	    // While distance length is above a certain threshold, continue searching
	    while(distanceCheck > 0.0001) {
//	    	System.out.println(distanceCheck);
	    	
	    	// Now try a bunch of similar angles
		    int currentCheck = 1;
		
		    while(currentCheck<=checksPerSide) {
		        // Try the offset of the left side
		        if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck),distanceCheck)) {
		        	System.out.println("Moved on Left Side");
//		            rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck),distanceCheck);
//		            return true;
		        	return curLoc.add(dir.rotateLeftDegrees(degreeOffset*currentCheck),distanceCheck);
		        }
		        // Try the offset on the right side
		        if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck),distanceCheck)) {
		        	System.out.println("Moved on Right Side");
//		            rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck),distanceCheck);
//		            return true;
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