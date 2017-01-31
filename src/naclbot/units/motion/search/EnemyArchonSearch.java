package naclbot.units.motion.search;
import battlecode.common.*;
import naclbot.variables.BroadcastChannels;
import naclbot.variables.GlobalVars;

import java.util.ArrayList;
import java.util.Random;

public class EnemyArchonSearch extends GlobalVars {
	
	public static MapLocation myArchon;
	public static MapLocation oppositeEnemyArchon = null;
	
	// FOR GARDENER
	public static MapLocation getCorrespondingArchon(){
		
		// Declare variables
		MapLocation[] archons = rc.getInitialArchonLocations(rc.getTeam());
		MapLocation myLocation = rc.getLocation();
		float minDistance = 100000;
		
		// Determine archon that hired this particular gardener 
		for (MapLocation archon: archons) {
			if (myLocation.distanceTo(archon) < minDistance) {
				myArchon = archon;
				minDistance = myLocation.distanceTo(archon);
			} 
		}
		
		return opposingEnemyArchonFIX(myArchon);	
	}
	
	// FOR REM SCOUT 
	public static MapLocation[] getEnemyArchons(){
		
		// Declare variables
		MapLocation[] archons = rc.getInitialArchonLocations(rc.getTeam());
		MapLocation myLocation = rc.getLocation();
		float minDistance = 100000;
		
		// Determine archon that hired this particular gardener 
		for (MapLocation archon: archons) {
			if (myLocation.distanceTo(archon) < minDistance) {
				myArchon = archon;
				minDistance = myLocation.distanceTo(archon);
			} 
		}
		
		return opposingEnemyArchon2(myArchon);	
	}
	
	public static MapLocation opposingEnemyArchonFIX(MapLocation myArchon) {
		// Initialize variables
		MapLocation realArchon;
		
		// Initialize arrays
		ArrayList<ArchonPairs> pairs = new ArrayList<ArchonPairs>();
		
		// The enemy team's archon positions sorted by increasing x, ties broken by increasing y
		MapLocation[] enemyArchons = rc.getInitialArchonLocations(rc.getTeam().opponent());
		
		// The allied team's archon positions sorted by increasing x, ties broken by increasing y
		// Only used for determining y midpoint
		MapLocation[] alliedArchons = rc.getInitialArchonLocations(rc.getTeam());
		
		// If there is just one for each team then we know where the enemy one is
		if (enemyArchons.length == 1) {
			return enemyArchons[0];
		// If there is more than one for each team then go through a more thorough analysis of positioning
		} else if (enemyArchons.length > 1){
			
			// Find the initial location of your closest archon
			realArchon = findRealArchon(myArchon, alliedArchons);
			
			// Considering who the allied archon is, run through enemy archons
			for (MapLocation enemyArchon: enemyArchons) {
				
				// Enemy archon variables
				float enemyX = enemyArchon.x;
				float enemyY = enemyArchon.y;
				
				ArchonPairs pair;
				
				if (enemyX == realArchon.x) {
					pair = new ArchonPairs(realArchon,enemyArchon);
					pairs.add(pair);
				} else if (enemyY == realArchon.y) {
					pair = new ArchonPairs(realArchon,enemyArchon);
					pairs.add(pair);
				}

			}
		
			// If there is only one pair, then return that enemy
			if (pairs.size() == 1) {
				return pairs.get(0).getEnemy();
				
			// If there are many pairs to be considered then choose the one with the least distance between them 
			// and is also symmetrical.
			} else if (pairs.size() > 1) {
				
				ArrayList<ArchonPairs> reasonablePairs = new ArrayList<ArchonPairs>();
				
				// Variables needed for shortest distance 
				float dist = Float.MAX_VALUE;
				MapLocation enemyLoc = null;
				
				// Determine the x midpoint 
				float midpointX = Math.abs(determineXMidpoint(alliedArchons,enemyArchons));
				
				// Determine the y midpoint
				float midpointY = Math.abs(determineYMidpoint(alliedArchons,enemyArchons));
				
				// Check for symmetry
				int i;
				for (i = 0; i < pairs.size(); i++) {
					
					// X Midpoint variables
					float allyXMidpoint = Math.abs(pairs.get(i).getAlly().x - midpointX);
					float enemyXMidpoint = Math.abs(pairs.get(i).getEnemy().x - midpointX);
					
					// Y Midpoint variables
					float allyYMidpoint = Math.abs(pairs.get(i).getAlly().y - midpointY);
					float enemyYMidpoint = Math.abs(pairs.get(i).getEnemy().y - midpointY);
					
					// Check if symmetric while not being on the same line for both lines
					if (allyXMidpoint == enemyXMidpoint && allyXMidpoint != 0  && enemyXMidpoint != 0) {
						reasonablePairs.add(pairs.get(i));
					} else if (allyYMidpoint == enemyYMidpoint && allyYMidpoint != 0 && enemyYMidpoint != 0) {
						reasonablePairs.add(pairs.get(i));
					}
					
				}
				
				// From remaining options, check for smallest distance 
				int j;
				for (j = 0; j < reasonablePairs.size(); j++) {
					if (reasonablePairs.get(j).getDistanceBetween() < dist) {
						// Update variables
						dist = reasonablePairs.get(j).getDistanceBetween();
						enemyLoc = reasonablePairs.get(j).getEnemy();
					}
				}
				return enemyLoc;
				
			// If there are no pairs then just do distance comparison	
			} else {
				return takeDistances(enemyArchons, alliedArchons, realArchon);
			}
		}
		return enemyArchons[0];
	}
	
	//NO ONE USES THIS
	public static MapLocation opposingEnemyArchon(MapLocation myArchon) {
		
		// Initialize variables
		MapLocation realArchon;
		
		// Initialize arrays
		ArrayList<ArchonPairs> pairs = new ArrayList<ArchonPairs>();
		
		// The enemy team's archon positions sorted by increasing x, ties broken by increasing y
		MapLocation[] enemyArchons = rc.getInitialArchonLocations(rc.getTeam().opponent());
		System.out.print("how many archons:");
		System.out.println(enemyArchons.length);
		
		// The allied team's archon positions sorted by increasing x, ties broken by increasing y
		MapLocation[] alliedArchons = rc.getInitialArchonLocations(rc.getTeam());
		
		// If there is just one for each team then we know where the enemy one is
		if (enemyArchons.length == 1) {
			return enemyArchons[0];
		// If there is more than one for each team then go through a more thorough analysis of positioning
		} else if (enemyArchons.length > 1){
			
			realArchon = findRealArchon(myArchon, alliedArchons);
			System.out.print("archon");
			System.out.println(realArchon);
			
			// Determine the x midpoint 
			float midpointX = Math.abs(determineXMidpoint(alliedArchons,enemyArchons));
			
			// Determine the y midpoint
			float midpointY = Math.abs(determineYMidpoint(alliedArchons,enemyArchons));
			
			// Run through the enemy archons and try to see who their pair is
			for (MapLocation enemyArchon: enemyArchons) {
				
				// Enemy archon variables
				float enemyX = enemyArchon.x;
				float enemyY = enemyArchon.y;
				
				ArrayList<ArchonPairs> miniPairsX = new ArrayList<ArchonPairs>();
				ArrayList<ArchonPairs> miniPairsY = new ArrayList<ArchonPairs>();
				
				ArchonPairs happyX = null;
				ArchonPairs happyY = null;
				
				for (MapLocation allyArchon: alliedArchons) {
					
					// Allied archon variables
					float allyX = allyArchon.x;
					float allyY = allyArchon.y;
					
					// If they have same x or same y then there is a pair 
					if (enemyX == allyX) {
						ArchonPairs pair = new ArchonPairs(allyArchon, enemyArchon);
						miniPairsX.add(pair);
						System.out.print("ally:");
						System.out.println(pair.getAlly());
						System.out.print("enemy:");
						System.out.println(pair.getEnemy());
					} else if (enemyY == allyY) {
						ArchonPairs pair = new ArchonPairs(allyArchon, enemyArchon);
						miniPairsY.add(pair);
						System.out.print("ally:");
						System.out.println(pair.getAlly());
						System.out.print("enemy:");
						System.out.println(pair.getEnemy());
					}
					
				}
				
				// If there is only one pair then add that to pairs
				if (miniPairsX.size() == 1 && miniPairsY.size() == 0) {
					pairs.add(miniPairsX.get(0));
					System.out.println("ONLY ONE PAIR ON X");
					System.out.print("ally:");
					System.out.println(miniPairsX.get(0).getAlly());
					System.out.print("enemy:");
					System.out.println(miniPairsX.get(0).getEnemy());
					continue;
				} else if (miniPairsX.size() == 0 && miniPairsY.size() == 1) {
					pairs.add(miniPairsY.get(0));
					System.out.println("ONLY ONE PAIR ON X");
					System.out.print("ally:");
					System.out.println(miniPairsY.get(0).getAlly());
					System.out.print("enemy:");
					System.out.println(miniPairsY.get(0).getEnemy());
					continue;
					
				// If there is one pair in each then ALWAYS CHOOSE THE ONE WITH LEAST DISTANCE BETWEEN THEM 
				} else if (miniPairsX.size() == 1 && miniPairsY.size() == 1) {
					System.out.println("one from each type of pair");
					if (miniPairsX.get(0).getDistanceBetween() < miniPairsY.get(0).getDistanceBetween()) {
						pairs.add(miniPairsX.get(0));
						System.out.println("LEAST DISTANCE IN X");
						System.out.print("ally:");
						System.out.println(miniPairsX.get(0).getAlly());
						System.out.print("enemy:");
						System.out.println(miniPairsX.get(0).getEnemy());
						continue;
					} else if (miniPairsY.get(0).getDistanceBetween() < miniPairsX.get(0).getDistanceBetween()) {
						System.out.println("LEAST DISTANCE IN Y");
						System.out.print("ally:");
						System.out.println(miniPairsX.get(0).getAlly());
						System.out.print("enemy:");
						System.out.println(miniPairsX.get(0).getEnemy());
						pairs.add(miniPairsY.get(0));
						continue;
					// If they are equidistant then choose randomly
					} else {
						System.out.println("equidistant when one from each");
						double rand = Math.random();
						if (rand < (double) 0.5) {
							pairs.add(miniPairsX.get(0));
							continue;
						} else {
							pairs.add(miniPairsY.get(0));
							continue;
						}
					}
				}
				
				// If there are two or more pairs in X then choose the accurate one using midpoint
				if (miniPairsX.size() > 1) {
					System.out.println("many x pairs");
					int i;
					for (i = 0; i < miniPairsX.size(); i++) {
						float allyDistFromMidpoint = Math.abs(miniPairsX.get(i).getAlly().x - midpointX);
						float enemyDistFromMidpoint = Math.abs(miniPairsX.get(i).getAlly().x - midpointX);
						if (allyDistFromMidpoint == enemyDistFromMidpoint) {
							happyX = miniPairsX.get(i);
							continue;
						}
					}
				}
					
				// If there are two or more pairs in Y then choose the accurate one using midpoint
				if (miniPairsY.size() > 1) {
					System.out.println("many y pairs");
					int j;
					for (j = 0; j < miniPairsY.size(); j++) {
						float allyDistFromMidpoint = Math.abs(miniPairsY.get(j).getAlly().x - midpointY);
						float enemyDistFromMidpoint = Math.abs(miniPairsY.get(j).getAlly().x - midpointY);
						if (allyDistFromMidpoint == enemyDistFromMidpoint) {
							happyY = miniPairsY.get(j);
							continue;
						}
					}
				}
				
				// From the points chosen by midpoint choose the ones that are closest in distance 
				if (happyX != null && happyY == null) {
					System.out.println("choose the x pair, no y pair");
					pairs.add(happyX);
					continue;
				} else if (happyY != null && happyX == null) {
					System.out.println("choose the y pair, no x pair");
					pairs.add(happyY);
					continue;
				} else if (happyX != null && happyY != null){
					System.out.println("equidistant when many of both types of pairs");
					if (happyX.getDistanceBetween() < happyY.getDistanceBetween()) {
						pairs.add(happyX);
						continue;
					} else if (happyY.getDistanceBetween() < happyX.getDistanceBetween()) {
						pairs.add(happyY);
						continue;
					// If they are equidistant then choose randomly
					} else {
						double rand = Math.random();
						if (rand < (double) 0.5) {
							pairs.add(happyX);
							continue;
						} else {
							pairs.add(happyY);
							continue;
						}
					}
				}
			}
			
			// If an archon has been found then return it 
			if (pairs.size() > 0) {
				int i;
				for (i=0 ; i < pairs.size() ; i++) {
					if (pairs.get(i).getAlly() == realArchon) {
						return pairs.get(i).getEnemy();
					}
				}
			// If an archon has not been found then take distances and find shortest one
			} else {
			
				// If there are no pairs then take distances 
				return takeDistances(enemyArchons, alliedArchons, realArchon);
			}
		}
		// If nothing works, just return a random archon
		return enemyArchons[0];
	}
	
	public static MapLocation[] opposingEnemyArchon2(MapLocation myArchon) {
		
		MapLocation[] enemyArchons = rc.getInitialArchonLocations(rc.getTeam().opponent());
		MapLocation opposing = opposingEnemyArchonFIX(myArchon);
		
		// If we find an opposite then make the array and return it 
		if (opposing != null) {
			MapLocation[] locs = new MapLocation[enemyArchons.length];
			locs[0] = oppositeEnemyArchon;
			
			int i = 1;
			for (MapLocation e: enemyArchons) {
				if (e != oppositeEnemyArchon) {
					locs[i] = e;
					i += 1;
				}
			}
			return locs;
			
		// If we do not find an opposite then just return original array
		} else {
			return enemyArchons;
		}
		
	}
	
	
	public static MapLocation takeDistances(MapLocation[] enemyArchons, MapLocation[] alliedArchons, MapLocation archon) {
		
		// Initialize arrays
		ArrayList<ArchonPairs> pairs = new ArrayList<ArchonPairs>();
		
		for (MapLocation enemy: enemyArchons) {
			for (MapLocation ally: alliedArchons) {
				ArchonPairs pair = new ArchonPairs(ally,enemy);
				pairs.add(pair);
			}
		}
		
		float greatestDistance = 0;
		float leastDistance = 10000;
		ArchonPairs greatestDistancePair = null;
		ArchonPairs leastDistancePair = null;
		
		int i;
		for (i=0; i<pairs.size(); i++) {

			float distance = pairs.get(i).getDistanceBetween();

			if (distance > greatestDistance) {
				greatestDistance = distance;
				greatestDistancePair = pairs.get(i);
			} else if (distance < leastDistance) {
				leastDistance = distance;
				leastDistancePair = pairs.get(i);
			}
		}
		
		if (archon == greatestDistancePair.getAlly()) {
			return greatestDistancePair.getEnemy();
		} else if (archon == leastDistancePair.getAlly()) {
			return leastDistancePair.getEnemy();
		// Choose random one gg 
		} else {
			Random rand = new Random();
			int index = rand.nextInt(enemyArchons.length);
			return enemyArchons[index];
		}
		
	}
	
	public static MapLocation findRealArchon(MapLocation currentArchon, MapLocation[] allies) {
		float distance = 1000;
		MapLocation realArchon = null;
		for (MapLocation ally: allies) {
			if (currentArchon.distanceTo(ally) < distance) {
				realArchon = ally;
				distance = currentArchon.distanceTo(ally);
			}
		}
		return realArchon;
	}
	
	public static float determineXMidpoint(MapLocation[] enemies, MapLocation[] allies) {
		
		// Compare x values to know which one is on which side of the map
		float lowestXEnemy = enemies[0].x;
		float highestXEnemy = enemies[enemies.length - 1].x;
		float lowestXAlly = allies[0].x;
		float highestXAlly = enemies[enemies.length - 1].x;
		
		// All in line, same x coordinate
		if (lowestXEnemy == lowestXAlly) {
			return lowestXEnemy;
		// Enemies are on the left side, so take leftest enemy and rightest ally and average 
		} else if (lowestXEnemy < lowestXAlly) {
			return ((lowestXEnemy + highestXAlly) / 2);
		// Allies are on the right side, so take the leftest ally and rightest enemy and average
		} else {
			return ((lowestXAlly + highestXEnemy) / 2);
		}
		
	}
	
	public static float determineYMidpoint(MapLocation[] enemies, MapLocation[] allies) {
		float lowestYEnemy = enemies[0].y;
		float highestYEnemy = enemies[0].y;
		float lowestYAlly = allies[0].y;
		float highestYAlly = allies[0].y;
		
		// Get lowest and highest for enemies 
		for (MapLocation enemy: enemies) {
			// Update lowest y value
			if (enemy.y < lowestYEnemy) {
				lowestYEnemy = enemy.y;
			} 
			
			// Update highest y value
			if (enemy.y > highestYEnemy) {
				highestYEnemy = enemy.y;
			}
		}
		
		// Get lowest and highest for allies 
		for (MapLocation ally: allies) {
			// Update lowest y value
			if (ally.y < lowestYAlly) {
				lowestYAlly = ally.y;
			}
			
			// Update highest y value
			if (ally.y > highestYAlly) {
				highestYAlly = ally.y;
			}
		}
		
		// All in line, same y coordinate
		if (lowestYEnemy == lowestYAlly) {
			return lowestYEnemy;
		// Enemies are on the bottom, so take bottomest enemy and highest ally and average 
		} else if (lowestYEnemy < lowestYAlly) {
			return ((lowestYEnemy + highestYAlly) / 2);
		// Allies are on the bottom, so take bottomest ally and highest enemy and average
		} else {
			return ((lowestYAlly + highestYEnemy) / 2);
		}
		
	}
	
	public static class ArchonPairs extends GlobalVars{
		
		MapLocation ally;
		MapLocation enemy;
		
		public ArchonPairs(MapLocation allyArchon, MapLocation enemyArchon) {
			this.ally = allyArchon;
			this.enemy = enemyArchon;
		}
		
		public MapLocation getAlly() {
			return ally;
		}
		
		public MapLocation getEnemy() {
			return enemy;
		}
		
		public float getDistanceBetween() {
			return ally.distanceTo(enemy);
		}
	}
	
}