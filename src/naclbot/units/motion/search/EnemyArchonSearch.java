package naclbot.units.motion.search;
import battlecode.common.*;
import naclbot.variables.GlobalVars;

import java.util.ArrayList;

public class EnemyArchonSearch extends GlobalVars {
	
	public static MapLocation opposingEnemyArchon(MapLocation myArchon) {
		
		// Initialize arrays
		ArrayList<ArchonPairs> pairs = new ArrayList<ArchonPairs>();
		ArrayList<MapLocation> misfits = new ArrayList<MapLocation>();
		
		// The enemy team's archon positions sorted by increasing x, ties broken by increasing y
		MapLocation[] enemyArchons = rc.getInitialArchonLocations(rc.getTeam().opponent());
		
		// The allied team's archon positions sorted by increasing x, ties broken by increasing y
		MapLocation[] alliedArchons = rc.getInitialArchonLocations(rc.getTeam().opponent());
		
		// If there is just one for each team then we know where the enemy one is
		if (enemyArchons.length == 1) {
			return enemyArchons[0];
		// If there is more than one for each team then go through a more thorough analysis of positioning
		} else if (enemyArchons.length > 1){
			
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
				
				// If there are no pairs, then misfit 
				if (miniPairsX.size() == 0 && miniPairsY.size() == 0) {
					misfits.add(enemyArchon);
				
				// If there is only one pair then add that to pairs
				} else if (miniPairsX.size() == 1 && miniPairsY.size() == 0) {
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
			
			// Check if given archon already has a pair
			int i;
			for (i=0 ; i < pairs.size() ; i++) {
				if (pairs.get(i).getAlly() == myArchon) {
					return pairs.get(i).getEnemy();
				}
			}
			// If it doesn't have a pair then check misfits
			if (misfits.size() == 1) {
				return misfits.get(0);
			}
		} 
		
		// Did not find its pair (SHOULDN'T EVER RETURN NULL)
		return null;
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
	}
	
}