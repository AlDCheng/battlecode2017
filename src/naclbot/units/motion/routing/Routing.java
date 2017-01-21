// This hosts the path planning algorithm
package naclbot.units.motion.routing;
import naclbot.units.motion.Move;
import java.util.Arrays;
import java.util.ArrayList;

import battlecode.common.*;
import naclbot.variables.GlobalVars;

// Some shitty path planning is here
public class Routing extends GlobalVars{
	public static Direction prevDir = new Direction(0);
	public static int reRoute = 0; 
	public static boolean newDest = false;
	
	public static MapLocation prevLoc = rc.getLocation();
	public static MapLocation curLoc = new MapLocation(prevLoc.x,prevLoc.y);
	public static MapLocation lastDest = new MapLocation(prevLoc.x,prevLoc.y);
	
	public static ArrayList<MapLocation> path = new ArrayList<MapLocation>();
	
	public static void setRouting(ArrayList<MapLocation> pathNew) {
		prevDir = new Direction(0);
		reRoute = 0; 
		newDest = false;
		
		prevLoc = rc.getLocation();
		curLoc = new MapLocation(prevLoc.x,prevLoc.y);
		lastDest = new MapLocation(prevLoc.x,prevLoc.y);
		
		path = new ArrayList<MapLocation>(pathNew);
	}
	
	public static void routingWrapper() {
		try {
			// Get current location
	    	curLoc = rc.getLocation();
	    	
	//    	System.out.println("LastDest: " + lastDest);
	    	
	    	// Wall following work (incomplete)
	    	// Will be commented on later    	
	    	if(!(lastDest.isWithinDistance(curLoc,1))&&(Routing.newDest)) {
	    		Routing.reRoute--;
	    		if (Routing.reRoute <= 0) {
	    			Routing.reRoute = 0;
	    		}
				Routing.prevDir = new Direction(prevLoc, curLoc);
	    		Routing.newDest = false;
	    	}
	    	
	    	// Update previous location
	    	// This is as moving occurs below this
	    	prevLoc = new MapLocation(curLoc.x,curLoc.y);
	    	
	//    	System.out.println(path.size());
	//    	System.out.println("ReRoute: " + Routing.reRoute);
	    	
	    	// If there is a path to move through
	    	// Then attempt moving to each point one at a time
	    	if(!(path.isEmpty())) {
	    		
	    		// Transfer the first point to nextLoc
	    		// If point isn't reached, it will be re-added to Path later 
	    		MapLocation nextLoc = path.remove(0);
	//    		System.out.println(nextLoc);
	    		
	    		// Calling function to attempt to move to point
	    		ArrayList<MapLocation> newPath = Routing.moveToPoint(nextLoc);
	    		
	    		// If there is a path resulting from the movement...
	    		// - It is only empty when the robot is stuck
	    		if(!(newPath.isEmpty())) {
	//    			System.out.println("New Path: " + newPath);
	    			
	    			// Detect if we have not reached point
	    			if(!(newPath.get(0).isWithinDistance(rc.getLocation(), 1))) {
	//    				System.out.println("Not at point");
	    				
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
	    				
	    				// Set signal that point is found, so new point will be taken
	    				// and store this point that has been cleared
	    				Routing.newDest = true;
	    				lastDest = new MapLocation(newPath.get(0).x, newPath.get(0).y);
	    			}
	    		}
	    		// No continuing path, so it is halted
	    		else {
	    			path = new ArrayList<MapLocation>();
	    		}
	    	}
		} catch (Exception e) {
			e.printStackTrace();
//			return new ArrayList<MapLocation>();
		}
//		return path;
	    	
	}
	
	public static ArrayList<MapLocation> moveToPoint(MapLocation dest) {
		// Array to store next points
		ArrayList<MapLocation> nextPoints = new ArrayList<MapLocation>();
		
		try {			
//			MapLocation curLoc = rc.getLocation();
			
			RobotType curType = rc.getType();			
			
			// Draw orange line for reference
			rc.setIndicatorLine(curLoc, dest, 255, 192, 203);
			
			// Get direction to point
			Direction destDir = new Direction(curLoc, dest);
			
			float degreeOffset = 10;
			
			// Try moving in that direction
			// Offset: 5, Checks: 2
			if(!(Move.tryMove(destDir, degreeOffset, 2))) {
				int rightMod = 1;
				int upMod = 1;
				
				
				// If there is an obstruction:
				
				// Get angle of movement:
				float angleDir = destDir.getAngleDegrees();
				
//				System.out.println("Blocked; Angle: " + angleDir);
				/*
				if (reRoute[0] > 0) {
					if(Move.tryMove(prevDir, degreeOffset, 2)) {
						nextPoints.add(dest);
						return nextPoints;
					}
				}*/
				
				// If direction is in a cardinal direction:
				if ((Math.abs(angleDir) % 90) <= 1) {
//					System.out.println("Angle ~ 0");
					
					// Try moving at 90 degree offsets
					// Right now, it prioritizes moving UR / DL	
					// *Inefficient right now
					if(!(Move.tryMove(destDir, 90, 1))) {
						if((Math.abs(angleDir) < 45) && (Math.abs(angleDir) > 135)) {
							rightMod = 1;
							if(Math.abs(angleDir) > 135) {
								rightMod = -1;
							}
							// Check vertical lines
							for(float hOffset = curType.bodyRadius; hOffset < curType.sensorRadius-curType.bodyRadius; hOffset += curType.bodyRadius) {
								nextPoints = viewVertical(curLoc, curType.sensorRadius, curType.bodyRadius, hOffset, rightMod, 1);
								
								if(!(nextPoints.isEmpty())) {
									nextPoints.add(dest);
									reRoute = 3;
									return nextPoints;
								}
							}
							
							for(float hOffset = curType.bodyRadius; hOffset < curType.sensorRadius-curType.bodyRadius; hOffset += curType.bodyRadius) {
								nextPoints = viewVertical(curLoc, curType.sensorRadius, curType.bodyRadius, hOffset, rightMod, -1);
								
								if(!(nextPoints.isEmpty())) {
									nextPoints.add(dest);
									reRoute = 3;
									return nextPoints;
								}
							}
						}
						else {
							upMod = 1;
							if(angleDir < 0) {
								upMod = -1;
							}
							
							// Check horizontal lines
							for(float vOffset = curType.bodyRadius; vOffset < curType.sensorRadius-curType.bodyRadius; vOffset += curType.bodyRadius) {
								nextPoints = viewHorizontal(curLoc, curType.sensorRadius, curType.bodyRadius, vOffset, 1, upMod);
								if(!(nextPoints.isEmpty())) {
									nextPoints.add(dest);
									reRoute = 3;
									return nextPoints;
								}
							}
							
							for(float vOffset = curType.bodyRadius; vOffset < curType.sensorRadius-curType.bodyRadius; vOffset += curType.bodyRadius) {
								nextPoints = viewHorizontal(curLoc, curType.sensorRadius, curType.bodyRadius, vOffset, -1, upMod);
								if(!(nextPoints.isEmpty())) {
									nextPoints.add(dest);
									reRoute = 3;
									return nextPoints;
								}
							}
							
							
						}
						return new ArrayList<MapLocation>();
						
					}
				}
				
				// If direction at angle <= 45
				else if (((Math.abs(angleDir) % 90) <= 45)||((Math.abs(angleDir) % 90) >= 135)) {
//					System.out.println("Angle <= 45");
					float newAngle = 0;
					
					// To move in the left direction horizontally
					if(Math.abs(angleDir) > 90) {
						newAngle = (float)Math.PI;
						rightMod = -1;
					}
					
					Direction newDestDir = new Direction(newAngle);
					
					// Try moving in that direction
					// Offset: 5, Checks: 2
					if(!(Move.tryMove(newDestDir, degreeOffset, 2))) {
						
						rc.setIndicatorLine(curLoc, curLoc.add(newDestDir), 255, 0, 0);
						
						// Not succesful; set up trying vertical
						newAngle = (float)Math.PI/2;
						
						// To move in the down direction vertically
						if(angleDir < 0) {
							newAngle = (float)(-1*(Math.PI/2));
							upMod = -1;
						}
						
						System.out.println(rightMod + ", " + upMod);
						// Check horizontal lines
						for(float vOffset = curType.bodyRadius; vOffset < 0.5*curType.sensorRadius-curType.bodyRadius; vOffset += curType.bodyRadius) {
//							System.out.println(vOffset);
							nextPoints = viewHorizontal(curLoc, curType.sensorRadius, curType.bodyRadius, vOffset, rightMod, upMod);
							if(!(nextPoints.isEmpty())) {
								nextPoints.add(dest);
								reRoute = 3;
								return nextPoints;
							}
						}
						
						// Now check vertical
						destDir = new Direction(newAngle);
						
						// Try moving in that direction
						// Offset: 5, Checks: 2
						if(!(Move.tryMove(newDestDir, degreeOffset, 2))) {
							
							rc.setIndicatorLine(curLoc, curLoc.add(newDestDir), 255, 0, 0);
							
							// Now check vertical lines
							for(float hOffset = curType.bodyRadius; hOffset < 0.5*curType.sensorRadius-curType.bodyRadius; hOffset += curType.bodyRadius) {
								nextPoints = viewVertical(curLoc, curType.sensorRadius, curType.bodyRadius, hOffset, rightMod, upMod);
								
								if(!(nextPoints.isEmpty())) {
									nextPoints.add(dest);
									reRoute = 3;
									return nextPoints;
								}
							}
							/*
							// Move perpendicular to current angle
							if(!(Move.tryMove(destDir, 90, 1))) {
								return false;
							}*/
						}
					}
					
				}
				
				// If direction at angle > 45
				else {
//					System.out.println("Angle > 45");
					float newAngle = (float)Math.PI/2;
					
					// To move in the down direction vertically
					if(angleDir < 0) {
						newAngle = (float)(-1*(Math.PI/2));
						upMod = -1;
					}
					
					Direction newDestDir = new Direction(newAngle);
					
					// Try moving in that direction
					// Offset: 5, Checks: 2
					if(!(Move.tryMove(newDestDir, degreeOffset, 2))) {
						
						rc.setIndicatorLine(curLoc, curLoc.add(newDestDir), 255, 0, 0);
						
						newAngle = 0;
						
						// To move in the left direction horizontally
						if(Math.abs(angleDir) > 90) {
							newAngle = (float)Math.PI;
							rightMod = -1;
						}
						
//						System.out.println(rightMod + ", " + upMod);
						
						// Check vertical lines
						for(float hOffset = curType.bodyRadius; hOffset < 0.5*curType.sensorRadius-curType.bodyRadius; hOffset += curType.bodyRadius) {
							nextPoints = viewVertical(curLoc, curType.sensorRadius, curType.bodyRadius, hOffset, rightMod, upMod);
							if(!(nextPoints.isEmpty())) {
								nextPoints.add(dest);
								reRoute = 3;
								return nextPoints;
							}
						}
						
						
						destDir = new Direction(newAngle);
						
						// Try moving in that direction
						// Offset: 5, Checks: 2
						if(!(Move.tryMove(newDestDir, degreeOffset, 2))) {
							
							rc.setIndicatorLine(curLoc, curLoc.add(newDestDir), 255, 0, 0);
							
							// Check horizontal lines
							for(float vOffset = curType.bodyRadius; vOffset < 0.5*curType.sensorRadius-curType.bodyRadius; vOffset += curType.bodyRadius) {
								nextPoints = viewHorizontal(curLoc, curType.sensorRadius, curType.bodyRadius, vOffset, rightMod, upMod);
								if(!(nextPoints.isEmpty())) {
									nextPoints.add(dest);
									reRoute = 3;
									return nextPoints;
								}
							}
							
							return new ArrayList<MapLocation>();
							
							/*
							// Move perpendicular to current angle
							if(!(Move.tryMove(destDir, 90, 1))) {
								return false;
							}*/
						}
					}
				}
			}
			else {
				if(reRoute <= 1) {
					reRoute = 0;
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<MapLocation>();
		}
		
		// Movement successful with no edits
		nextPoints.add(dest);
		return nextPoints;
	}
	
	public static ArrayList<MapLocation> viewHorizontal(MapLocation curLoc, float radius, float bodyRadius, float offsetY, int rightMod, int upMod) {
		
		// Array to store next points
		ArrayList<MapLocation> nextPoints = new ArrayList<MapLocation>();
		MapLocation potLoc = new MapLocation(curLoc.x, curLoc.y);
		
		try {
			// Vertical length
			float vLen = offsetY;
			
			// Horizontal max from Pythagoras
			float maxHLen = (float)Math.sqrt((radius * radius ) - (offsetY * offsetY));
			
			// Iterate in increments of body radius
			for(float hLen = 0; hLen < 1*maxHLen-bodyRadius; hLen += bodyRadius) {
//				System.out.println("hLen, vLen: " + hLen + ", " + vLen + "; Max: " + maxHLen);
				potLoc = new MapLocation(rightMod * hLen + curLoc.x, -upMod * vLen + curLoc.y);
				
				// If not free
				if(rc.isCircleOccupiedExceptByThisRobot(potLoc,bodyRadius)) {
					rc.setIndicatorDot(potLoc, 153, 0, 0);
					return new ArrayList<MapLocation>();
				}
				rc.setIndicatorDot(potLoc, 0, 204, 204);
			}
			
			MapLocation interLoc = new MapLocation(curLoc.x, potLoc.y);
			
			Direction interDir = new Direction(curLoc, interLoc);
			Move.tryMove(interDir, 3, 2);
			
			nextPoints.add(interLoc);
			nextPoints.add(potLoc);
			
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<MapLocation>();
		}
		
		return nextPoints;
		
	}
	
	public static ArrayList<MapLocation> viewVertical(MapLocation curLoc, float radius, float bodyRadius, float offsetX, int rightMod, int upMod) {
		
		// Array to store next points
		ArrayList<MapLocation> nextPoints = new ArrayList<MapLocation>();
		MapLocation potLoc = new MapLocation(curLoc.x, curLoc.y);
		
		try {
			// Horizontal length
			float hLen = offsetX;
			
			// Vertical max from Pythagoras
			float maxVLen = (float)Math.sqrt((radius * radius ) - (offsetX * offsetX));
			
			// Iterate in increments of body radius
			for(float vLen = 0; vLen < 1*maxVLen-bodyRadius; vLen += bodyRadius) {
				potLoc = new MapLocation(-rightMod * hLen + curLoc.x, upMod * vLen + curLoc.y); 
				
				// If not free
				if(rc.isCircleOccupiedExceptByThisRobot(potLoc,bodyRadius)) {
					rc.setIndicatorDot(potLoc, 153, 0, 0);
					return new ArrayList<MapLocation>();
				}
				rc.setIndicatorDot(potLoc, 0, 204, 204);
			}
			
			MapLocation interLoc = new MapLocation(potLoc.x, curLoc.y);
			
			Direction interDir = new Direction(curLoc, interLoc);
			Move.tryMove(interDir, 3, 2);
			
			nextPoints.add(interLoc);
			nextPoints.add(potLoc);
			
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<MapLocation>();
		}
		
		return nextPoints;
		
	}
}