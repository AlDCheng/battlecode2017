package naclbot.units.motion.search;
import battlecode.common.*;
import naclbot.variables.BroadcastChannels;
import naclbot.variables.GlobalVars;

import java.util.ArrayList;
import java.util.Random;

public class EnemyArchonSearch extends GlobalVars {
	
	public static MapLocation myArchon;
	public static MapLocation oppositeEnemyArchon;
	
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
		
		return opposingEnemyArchon(myArchon);	
	}
	
	public static void manageCorrespondingArchon() throws GameActionException {
		
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
		
		System.out.print("My archon is: ");
		System.out.println(myArchon);
		oppositeEnemyArchon = opposingEnemyArchon(myArchon);
		
		System.out.print("Opposite enemy archon is: ");
		System.out.println(oppositeEnemyArchon);
		rc.setIndicatorDot(oppositeEnemyArchon, 0, 0, 100);
		
		// Broadcast to channel
		if (rc.readBroadcast(BroadcastChannels.OPPOSING_ARCHON_1) == 0) {
			rc.broadcast(BroadcastChannels.OPPOSING_ARCHON_1, 1);
			rc.broadcastFloat(BroadcastChannels.OPPOSING_ARCHON_1 + 1, oppositeEnemyArchon.x);
			rc.broadcastFloat(BroadcastChannels.OPPOSING_ARCHON_1 + 2, oppositeEnemyArchon.y);
		} else if (rc.readBroadcast(BroadcastChannels.OPPOSING_ARCHON_2) == 0) {
			rc.broadcast(BroadcastChannels.OPPOSING_ARCHON_2, 1);
			rc.broadcastFloat(BroadcastChannels.OPPOSING_ARCHON_2 + 1, oppositeEnemyArchon.x);
			rc.broadcastFloat(BroadcastChannels.OPPOSING_ARCHON_2 + 2, oppositeEnemyArchon.y);
		} else if (rc.readBroadcast(BroadcastChannels.OPPOSING_ARCHON_3) == 0) {
			rc.broadcast(BroadcastChannels.OPPOSING_ARCHON_3, 1);
			rc.broadcastFloat(BroadcastChannels.OPPOSING_ARCHON_3 + 1, oppositeEnemyArchon.x);
			rc.broadcastFloat(BroadcastChannels.OPPOSING_ARCHON_3 + 2, oppositeEnemyArchon.y);
		}
	}
	
	public static MapLocation opposingEnemyArchon(MapLocation myArchon) {
		
		// Initialize variables
		MapLocation realArchon;
		
		// Initialize arrays
		ArrayList<ArchonPairs> pairs = new ArrayList<ArchonPairs>();
		
		// The enemy team's archon positions sorted by increasing x, ties broken by increasing y
		MapLocation[] enemyArchons = rc.getInitialArchonLocations(rc.getTeam().opponent());
		
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
				
				for (MapLocation allyArchon: alliedArchons) {
					
					// Allied archon variables
					float allyX = allyArchon.x;
					float allyY = allyArchon.y;
					
					// If they have same x or same y then there is a pair 
					if (enemyX == allyX) {
						ArchonPairs pair = new ArchonPairs(allyArchon, enemyArchon);
						miniPairsX.add(pair);
					} else if (enemyY == allyY) {
						ArchonPairs pair = new ArchonPairs(allyArchon, enemyArchon);
						miniPairsY.add(pair);
					}
					
				}
				
				// If there is only one pair then add that to pairs
				if (miniPairsX.size() == 1 && miniPairsY.size() == 0) {
					pairs.add(miniPairsX.get(0));
				} else if (miniPairsX.size() == 0 && miniPairsY.size() == 1) {
					pairs.add(miniPairsY.get(0));
					
				// If there is one pair in each then ALWAYS CHOOSE THE ONE WITH SAME Y
				} else if (miniPairsX.size() == 1 && miniPairsY.size() == 1) {
					pairs.add(miniPairsY.get(0));
					
				// If there are two or more pairs in X then choose the accurate one using midpoint
				} else if (miniPairsX.size() > 1) {
					int i;
					for (i = 0; i < miniPairsX.size(); i++) {
						float allyDistFromMidpoint = Math.abs(miniPairsX.get(i).getAlly().x - midpointX);
						float enemyDistFromMidpoint = Math.abs(miniPairsX.get(i).getAlly().x - midpointX);
						if (allyDistFromMidpoint == enemyDistFromMidpoint) {
							pairs.add(miniPairsX.get(i));
							break;
						}
					}
					
				// If there are two or more pairs in Y then choose the accurate one using midpoint
				} else if (miniPairsY.size() > 1) {
					int i;
					for (i = 0; i < miniPairsY.size(); i++) {
						float allyDistFromMidpoint = Math.abs(miniPairsY.get(i).getAlly().x - midpointY);
						float enemyDistFromMidpoint = Math.abs(miniPairsY.get(i).getAlly().x - midpointY);
						if (allyDistFromMidpoint == enemyDistFromMidpoint) {
							pairs.add(miniPairsY.get(i));
							break;
						}
					}
				}
				
			}
			
			if (pairs.size() > 0) {
				// Check if given archon already has a pair
				int i;
				for (i=0 ; i < pairs.size() ; i++) {
					if (pairs.get(i).getAlly() == realArchon) {
						return pairs.get(i).getEnemy();
					}
				}
			} else {
			
				// If there are no pairs then take distances 
				return takeDistances(enemyArchons, alliedArchons, realArchon);
			}
		} 
		return null;
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