// This hosts the path planning algorithm
package naclbot.units.motion.routing;
import java.util.Arrays;
import java.util.ArrayList;

import battlecode.common.*;
import naclbot.variables.GlobalVars;

// Some shitty path planning is here
public class PathPlanning extends GlobalVars{
	// We on purposely switch start and end so that the ending path sequence
	// is from start to end without flipping the order
	public static ArrayList<MapLocation> findPath(MapLocation end, MapLocation start) {
		try {
			System.out.println("Center: " + centerCoords.x + ", " + centerCoords.y);
			System.out.println("Offset: " + offsetX + ", " + offsetY);
				
				
			// Convert start and end to cell coordinates
			CellLocation startCell = new CellLocation();
			CellLocation endCell = new CellLocation();
			startCell.x = (int)((start.x - centerCoords.x)/robotRadius) - offsetX;
			startCell.y = (int)((start.y - centerCoords.y)/robotRadius) - offsetY;
	
			endCell.x = (int)((end.x - centerCoords.x)/robotRadius) - offsetX;
			endCell.y = (int)((end.y - centerCoords.y)/robotRadius) - offsetY;
			
			// Define arrays
			int maxY = internalMap.size();
			int maxX = internalMap.get(0).size();
			
			System.out.println("maxX: " + maxX + ", maxY: " + maxY);
			
			float[][] dist = new float[maxY][maxX];
			
			for (float[] row: dist) {
				Arrays.fill(row, Integer.MAX_VALUE);
			}
	
			CellLocation[][] prev = new CellLocation[maxY][maxX];
			
			System.out.println("Start: " + startCell.x + ", " + startCell.y);
			
			dist[startCell.y][startCell.x] = 0;
			
			// Define queue of cells
			// Format [x][y][dist]
			ArrayList<float[]> queueCells = new ArrayList<float[]>();
	
			CellLocation curCell = startCell;
			
			// Loop to find path
			do {	
				// Mark cell as visited
				//visited[curCell.x][curCell.y] = true;
				
				// Check each neighbor
				for (int i = -1; i < 2; i++) {
					for (int j = -1; j < 2; j++) {
						// Get coords
						int neighborX = curCell.x+i;
						int neighborY = curCell.y+j;
						
						// Check for OOB
						if (((neighborX >= 0) && (neighborX < maxX)) && ((neighborY >= 0) && (neighborY < maxY))) {
							// check for moveable path
							if (internalMap.get(neighborY).get(neighborX) == 0) {
								float cost;
								// Different costs of diagonals
								if ((i == j) || (i == -1*j)) {
									cost = (float)1.4;
								} else {
									cost = 1;
								}
								
								// Add to additional cost
								cost += dist[curCell.y][curCell.x];
								
								// Update dist
								if (cost < dist[neighborY][neighborX]) {
									dist[neighborY][neighborX] = cost;
									
									// Prev
									prev[neighborY][neighborX] = curCell;
											
									// Add next cell to queue
									float[] neighCellInfo = new float[3];
									neighCellInfo[0] = neighborX;
									neighCellInfo[1] = neighborY;
									neighCellInfo[2] = cost;
									queueCells.add(neighCellInfo);
								}
							
							}
						}
					}
				}
				
				// Get next cell
				curCell = nextNode(queueCells);
				
				if (curCell == endCell) {
					break;
				}
				
			} while(queueCells.size() > 1);
			
			// Return path
			ArrayList<MapLocation> path = new ArrayList<MapLocation>();
			CellLocation cur = new CellLocation();
			cur = endCell;
			
			while (prev[cur.y][cur.x] != null) {
				cur = prev[cur.y][cur.x];
				MapLocation pathLoc = new MapLocation(robotRadius * (cur.x + offsetX) + centerCoords.x,
														robotRadius * (cur.y + offsetY) + centerCoords.y);
				path.add(pathLoc);
			}
					
			return path;
		} catch (Exception e) {
			System.out.println("Path planning error");
			e.printStackTrace();
		}
		return null;
	}
	
	// Get next closet node in queue
	public static CellLocation nextNode(ArrayList<float[]> queueCells) {
		float minSize = queueCells.get(0)[2];
		int minIndex = 0;
		
		// Get index of closest node
		for (int i = 1; i < queueCells.size(); i++) {
			float newSize = queueCells.get(i)[2];
			if (newSize < minSize) {
				minSize = newSize;
				minIndex = i;
			}
		}
		
		// Return as MapLocation
		CellLocation nextLoc = new CellLocation();
		nextLoc.x = (int)queueCells.get(minIndex)[0];
		nextLoc.y = (int)queueCells.get(minIndex)[1];
		
		// Remove
		queueCells.remove(minIndex);
		return nextLoc;
	}
	
}

// Cell format class
class CellLocation {
	int x;
	int y;
}