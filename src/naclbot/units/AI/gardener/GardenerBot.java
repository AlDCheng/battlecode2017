// AI for gardener under normal control
package naclbot.units.AI.gardener;
import battlecode.common.*;
import naclbot.units.motion.Move;
import naclbot.units.motion.search.TreeSearch;
import naclbot.variables.GlobalVars;
import naclbot.variables.BroadcastChannels;
import naclbot.units.interact.iFeed;
import naclbot.units.motion.*;
import naclbot.units.motion.search.EnemyArchonSearch;

import java.util.ArrayList;

public class GardenerBot extends GlobalVars {
	
	public static int role;
	
	public static Team enemy;
	
	public static ArrayList<MapLocation> plantedTrees = new ArrayList<MapLocation>();
	public static ArrayList<MapLocation> nearbyLowHealthTrees = new ArrayList<MapLocation>();
	public static int treeCount = 0;
	public static boolean canMove = true;
	
	public static int numGard;
	public static int prevNumBuilder;
	public static int prevNumWaterer;
	
	public static int scoutCount;
	public static int soldierCount;
	public static int lumberjackCount;
	public static int tankCount;
	public static int gardenerCount;
	
	public static Direction[] hexDirArray = Plant.generateHexagonalDirections();
	public static Direction dirToOpening;
	
	public static int xPos;
	public static int yPos;
	public static MapLocation archonLoc;
	
	public static MapLocation destination;
	public static boolean treeImpossible = false;
	
	public static ArrayList<MapLocation> lowHealthTrees;
	
	public static boolean nearbyTreesAndArc;
	
	//-----------Starting game unit amounts-------------
	public static int unitStart = 0;
	
	public static int minScouts1 = 1;
	public static int minScouts2 = 2; // Total
	
	public static int minTrees1 = 2;
	
	public static int minSoldiers1 = 1;
	
//	public static boolean minSat = false;
	public static boolean earlyGame = true;
	//--------------------------------------------------
	public static int scanInt = 5;
	
	static int currentNumberofGardeners;
	
	static int gardenerNumber;
	
	static int unitNumber;
	
	public static float buildDir, prevBuildDir;
	public static Direction buildVec = new Direction(0);
	public static Direction prevBuildVec = new Direction(0);
	//--------------------------------------------------
	public static int buildingLumberjack = 0;
	public static int buildingScout = 0;
	public static int buildingSoldier = 0;
	public static int buildingTank = 0;
	public static int buildingTree = 0;
	
	public static int spawnTime = 20;
	public static int unfreeze = 0;
	public static int treeGiveUp = 5;
	
	// Integer to store the previous health of the gardener
	public static float previousHealth;
	
	public static int syncRefresh = 15;
	public static int syncMin = 500;
	
	public static MapLocation myArchon = null;
	public static MapLocation oppositeEnemyArchon;
	
	// Miscellaneous variables.....
 	private static boolean believeHasDied; // Stores whether or not the robot believes it will die this turn or not.........
 	public static boolean beingAttacked;
	
	public static void init() throws GameActionException {
		System.out.println("I'm a gardener!");
		
		//EnemyArchonSearch.manageCorrespondingArchon();

		//value of initial role, set to planting trees first
		role = 1;
		
		enemy = rc.getTeam().opponent();
		
		 // Get own soldierNumber - important for broadcasting 
        gardenerNumber = rc.readBroadcast(BroadcastChannels.GARDENER_NUMBER_CHANNEL);
        currentNumberofGardeners = gardenerNumber + 1;
        
        unitNumber = rc.readBroadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL);
        rc.broadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL, unitNumber + 1);
        
        // Retrieve the number of active gardeners and increment......
       	int numberOfActiveGardeners = rc.readBroadcast(BroadcastChannels.GARDENERS_ALIVE_CHANNEL);
       	rc.broadcast(BroadcastChannels.GARDENERS_ALIVE_CHANNEL, numberOfActiveGardeners + 1);    
        
        // Update soldier number for other soldiers to see.....
        rc.broadcast(BroadcastChannels.GARDENER_NUMBER_CHANNEL, currentNumberofGardeners);

        
		buildVec = dirAway(rc.getLocation()); 
		main();
	}
		
		
	public static void main() throws GameActionException {
        // The code you want your robot to perform every round should be in this loop
		
		boolean override = false;
		RobotType overrideType = RobotType.SOLDIER;
        float buildJacks = 0;   
		
        while (true) {
        	//System.out.println(role);
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	Win();
            	propagateBuild();
            	boolean syncOK = sync(rc.getRoundNum());
            	
            	MapLocation myLocation = rc.getLocation();
            	
            	RobotInfo[] enemyRobots = NearbyUnits(enemy);
            	
            	System.out.println("Previous health: " + previousHealth);
            	BroadcastChannels.broadcastDistress(previousHealth, enemyRobots, myLocation, unitNumber);
        		int roundNumber = rc.getRoundNum();
        		
            	// Update the nearest enemy and archon locations - If it sees any.......
            	BroadcastChannels.broadcastNearestEnemyLocation(enemyRobots, myLocation, unitNumber, myLocation, roundNumber); 
            	
            	
            	// If the robot thought it died previously but didn't.... update information...
            	if(believeHasDied){
            		fixAccidentalDeathNotification();
            	}
            	
                
                // Check number of all units currently in service
                scoutCount = rc.readBroadcast(BroadcastChannels.SCOUT_NUMBER_CHANNEL);                
                soldierCount = rc.readBroadcast(BroadcastChannels.SOLDIER_NUMBER_CHANNEL);
                lumberjackCount = rc.readBroadcast(BroadcastChannels.LUMBERJACK_NUMBER_CHANNEL);
                tankCount = rc.readBroadcast(BroadcastChannels.TANK_NUMBER_CHANNEL);
                gardenerCount = rc.readBroadcast(BroadcastChannels.GARDENER_NUMBER_CHANNEL);
                
                System.out.println("Scouts: " + scoutCount + ", Soldiers: " + soldierCount + 
                					", Lumberjacks: " + lumberjackCount + ", Tanks: " + tankCount);
                System.out.println("Gardener: " + gardenerCount);

                //generates direction of opening after gardeners partially enclose themselves in trees
                dirToOpening = Direction.getEast().rotateLeftDegrees(300);
                
                //generate list of trees that are not full health
                lowHealthTrees = TreeSearch.getNearbyLowTrees();

                //check if there are trees or archons nearby
                nearbyTreesAndArc = Plant.checkNearbyTreesAndArchons(4);
                
                //check if there are nearby allied units
                RobotInfo[] nearbyAllies = rc.senseNearbyRobots(-1,rc.getTeam());
                
                // Find direction away from others
                
                prevBuildDir = prevBuildVec.opposite().getAngleDegrees();
                buildVec = dirAway(rc.getLocation());
                buildDir = buildVec.getAngleDegrees();
//                float buildDirOpp = dirAway(rc.getLocation()).opposite().getAngleDegrees();
                System.out.println("Build Degrees: " + buildDir);
                
                // check for movement to more optimal space
                if (canMove) {
                	if (unitStart < 10) {
                		destination = Chirasou.Disperse(rc.getTeam(),rc.getLocation(), battlecode.common.RobotType.GARDENER.strideRadius);
                		Direction dirToDestination = rc.getLocation().directionTo(destination);
                		if (dirToDestination != null) {
                			MapLocation newLoc = Yuurei.tryMoveInDirection(dirToDestination, rc.getType().strideRadius, rc.getLocation());
                			if (newLoc != null) {
                				manageBeingAttacked(newLoc);
                				rc.move(newLoc);
                			}
                		}
                		unitStart++;
                	}
                	else {
	                	// Find optimal location to move
//	                	System.out.println(Clock.getBytecodeNum());
	                	MapLocation destination = Plant.findOptimalSpace(30, (float)rc.getType().sensorRadius-4, (float)rc.getType().sensorRadius-4, prevBuildDir);
//	                	System.out.println(Clock.getBytecodeNum());
	                	rc.setIndicatorDot(destination, 255, 0, 255);
	                	
	                	// Move to location
	                	Direction dirToDestination = rc.getLocation().directionTo(destination);
	                	if (dirToDestination != null) {
	                		prevBuildVec = dirToDestination;
	                		float lengthTo = rc.getLocation().distanceTo(destination);
	                		MapLocation newLoc = Yuurei.tryMoveInDirection(dirToDestination, lengthTo, rc.getLocation());
	                		if (newLoc != null) {
	                			manageBeingAttacked(newLoc);
	                			rc.move(newLoc);
	                		}
	                	}
                	}
                }
                
                // If it hasn't moved then check if will be attacked in same loc
                if (!rc.hasMoved()) {
                	manageBeingAttacked(rc.getLocation());
                }
                
                //--------------------------------------------------------------------------------
                // Enemy Sensing
                
                // Check for surrounding enemies
                if (rc.senseNearbyRobots(rc.getLocation(), rc.getType().sensorRadius, rc.getTeam().opponent()).length > 0) {
                	earlyGame = false;
                	override = true;
                	overrideType = RobotType.SOLDIER;
                	
                	/*
                	if (buildJacks >=  Math.random()) {
                		overrideType = RobotType.LUMBERJACK;
                	}*/
                	                	
                }

                //--------------------------------------------------------------------------------
                // Unit Building
                
                float bulletNum = rc.getTeamBullets();
                if (bulletNum > GameConstants.BULLET_TREE_COST) {
                	float newBuildDir = buildDir;
                	
                	float treeCongestion = Plant.congestion;
                	
                	Direction plantDirs[] = Plant.scanBuildRadius(scanInt, buildDir, 4, 1);
                	if (plantDirs[0] != null) {
                		plantDirs = Plant.scanBuildRadius(scanInt, buildDir, 3, 1);
                		if (plantDirs[0] != null) {
                    		plantDirs = Plant.scanBuildRadius(scanInt, buildDir, 2, 1);
                    	}
                	}
                	
                	if (treeCongestion < Plant.congestion) {
                		treeCongestion = Plant.congestion;
                	}
                	
                	if (plantDirs[1] != null) {
                		newBuildDir = plantDirs[1].getAngleDegrees();
                	}
            			
                	
                	//--------------------------------------------------------------------------------
                	System.out.println("Tree Congestion: " + treeCongestion);
                    
                    // Fixed constant values in here
                    // Check for surrouding trees
                    if (Plant.congestion > 0.75) {
                    	System.out.println("Already congested");
                    	buildJacks = 1;
                    }
                    else {
                    	System.out.println("Sense periphery");
                    	TreeInfo[] trees = rc.senseNearbyTrees(rc.getType().sensorRadius, Team.NEUTRAL);
                    	int treeNum = trees.length;
                    	System.out.println("Tree Length: " + treeNum);
                		buildJacks = (float)treeNum/(float)15.0;
                    	for (int i = 0; i < treeNum; i++) {
                    		if (trees[i].getContainedRobot() != null) {
                    			buildJacks = 1;
                    			break;
                    		}
                    	}
                    	
                    	trees = rc.senseNearbyTrees(rc.getType().sensorRadius-3, Team.NEUTRAL);
                    	treeNum = trees.length;
                    	System.out.println("Tree Length (short): " + treeNum);
                		float buildJacksTemp = (float)2.0*(float)treeNum/(float)15.0;
                		buildJacks = Math.max(buildJacksTemp, buildJacks);
                    	for (int i = 0; i < treeNum; i++) {
                    		if (trees[i].getContainedRobot() != null) {
                    			buildJacks = 1;
                    			break;
                    		}
                    	}
                    }
                    
                    System.out.println("Build Jacks: " + buildJacks);
                    //--------------------------------------------------------------------------------
                    if (override) {
                    	System.out.println("Overruled!");
                		override = false;
                		if (bulletNum > 600) {
                			Direction dirToBuild = Plant.scanBuildRadiusTank(scanInt, newBuildDir);
            				if (dirToBuild != null) {
            					rc.buildRobot(RobotType.TANK, dirToBuild);
            				}
                			
                		}
                		buildUnitNew(overrideType, bulletNum, newBuildDir);             		
                	}
                    else if (earlyGame) {
                		if (scoutCount + Math.ceil(buildingScout/20.0) < minScouts1) {
                    		System.out.println("Scouts1");
                    		buildUnitNew(RobotType.SCOUT, bulletNum, newBuildDir);
                        }
                    	else if ((rc.getTreeCount() + Math.ceil(buildingTree/20.0) < minTrees1) && (!treeImpossible)) {
                    		System.out.println("Trees1");
                        	if (rc.hasTreeBuildRequirements()) {
                        		// To end at 0
//                        		Direction plantDirs[] = Plant.scanBuildRadius(scanInt, buildDir);
                        		if (plantDirs[0] != null) {
                        			if(!((rc.senseNearbyTrees((float)2, rc.getTeam()).length > 0) && (canMove))) {
//                                		System.out.println("Empty spaces: " + plantDirs[0] + ", " + plantDirs[1]);
                                    	if (plantDirs[0] != null) {
                                    		rc.plantTree(plantDirs[0]);
                                    		buildingTree += 20;
                                    		canMove = false;
                                    	}
                            		}
                        		}
                        		else {
                        			treeImpossible = true;
                        		}
                        	}
                        	canMove = false;
                        }
                		// Toggle with lumberjack
                    	else if (soldierCount + Math.ceil(buildingSoldier/20.0) + Math.ceil(buildingLumberjack/20.0) < minSoldiers1) {
                    		if (buildJacks >= Math.random()) {
                    			System.out.println("Soldiers1 -> Lumberjacks");
                            	buildUnitNew(RobotType.LUMBERJACK, bulletNum, newBuildDir);
                    		}
                    		else {
                    			System.out.println("Soldiers1");
                            	buildUnitNew(RobotType.SOLDIER, bulletNum, newBuildDir);                    			
                    		}
                        }
                    	/*else if (scoutCount + Math.ceil(buildingScout/20.0) < minScouts2) {
                    		System.out.println("Scouts2");
                        	buildUnitNew(RobotType.SCOUT, bulletNum, newBuildDir);
                        }*/
                    	else {
//                    		minSat = true;
                    		earlyGame = false;
                    	}
                	}
                	
                	// End early game
                	//------------------------------------------------
                	else if (!earlyGame) {
                    	System.out.println("Satisfication");
//                    	Direction plantDirs[] = Plant.scanBuildRadius(scanInt, buildDir);
                    	if (((soldierCount + lumberjackCount) > (rc.getTreeCount())) ||
                    			((Math.floor(rc.getRoundNum()/200)*gardenerCount) > (rc.getTreeCount()))) {
                    		
                    		if(!((rc.senseNearbyTrees((float)2, rc.getTeam()).length > 0) && (canMove))) {
                        		System.out.println("Empty spaces: " + plantDirs[0] + ", " + plantDirs[1]);
                            	if (plantDirs[0] != null) {
                            		rc.plantTree(plantDirs[0]);
                            		buildingTree += 20;
                            		canMove = false;
                            	}
                    		}
                    	}
                    	if ((plantDirs[1] != null) && syncOK) {
//                    		buildUnits(plantDirs[1]);
                    		buildUnitsRemastered(plantDirs[1], bulletNum, buildJacks);
                    	}
                    }                	
                }
                
                //--------------------------------------------------------------------------------
                waterSurroundingTrees();
                
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                
                previousHealth = rc.getHealth();
                
                Clock.yield();
            } catch (Exception e) {
                System.out.println("Gardener Exception");
                e.printStackTrace();
            }
        }
	}
	
	public static void fixAccidentalDeathNotification() throws GameActionException {
		// Reset belief in the robot dying this round....
    	believeHasDied = false;    	

		// Get the current number of soldiers in service
        int numberOfAliveGardeners = rc.readBroadcast(BroadcastChannels.GARDENERS_ALIVE_CHANNEL);
        
        // Update soldier number for other units to see.....
        rc.broadcast(BroadcastChannels.GARDENERS_ALIVE_CHANNEL, numberOfAliveGardeners + 1);
	}
	
	
	
	public static void manageBeingAttacked(MapLocation loc) throws GameActionException{
		beingAttacked = iFeed.willBeAttacked(loc);
		if (beingAttacked) {
			// BIT 0 - GIVES MY ID
			// BIT 1 - GIVES MY X
			// BIT 2 - GIVES MY Y
			// BIT 3 - GIVES NEAREST ENEMY
			RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
			rc.broadcast(BroadcastChannels.GARDENER_DISTRESS_CHANNEL, rc.getID());
			rc.broadcastFloat(BroadcastChannels.GARDENER_DISTRESS_CHANNEL+1, loc.x);
			rc.broadcastFloat(BroadcastChannels.GARDENER_DISTRESS_CHANNEL+2, loc.y);
			if (nearbyEnemies.length > 0) {
				rc.broadcast(BroadcastChannels.GARDENER_DISTRESS_CHANNEL+3, nearbyEnemies[0].getID());
			} else {
				rc.broadcast(BroadcastChannels.GARDENER_DISTRESS_CHANNEL+3, -1);
			}
			boolean willDie = iFeed.willFeed(loc);
			if (willDie) {
				believeHasDied = true;
				
				// Get the current number of soldiers in service
		        int numberOfAliveGardeners = rc.readBroadcast(BroadcastChannels.GARDENERS_ALIVE_CHANNEL);
		        
		        // Update soldier number for other units to see.....
		        rc.broadcast(BroadcastChannels.GARDENERS_ALIVE_CHANNEL, numberOfAliveGardeners - 1);

			}
		}
	}
	
	public static void buildUnitNew(RobotType Unit, float bullets, float dirDeg) throws GameActionException {
		if (bullets >= Unit.bulletCost) {
			if (rc.isBuildReady()) {
				Direction plantDirs[] = Plant.scanBuildRadius(scanInt, dirDeg, 2, 1);
				if (plantDirs[1] != null) {
					rc.buildRobot(Unit, plantDirs[1]);
					
					// Set counters
					
					if (Unit == RobotType.LUMBERJACK) {
						buildingLumberjack += 20;
					}
					else if (Unit == RobotType.SCOUT) {
						buildingScout += 20;
					}
					else if (Unit == RobotType.SOLDIER) {
						buildingSoldier += 20;
					}
					else if (Unit == RobotType.TANK) {
						buildingTank += 20;
					}
				}
			}
		}		
	}
	
	public static Direction dirAway(MapLocation curLoc) {
		RobotInfo[] ourBots = rc.senseNearbyRobots(RobotType.GARDENER.sensorRadius, rc.getTeam());
		Direction opDir;
		
		float dx = 0;
		float dy = 0;
		int size = ourBots.length;
		if(size > 0) {
			for(int i = 1; i < size; i++) {
				if(ourBots[i].type == RobotType.GARDENER) {
					dx += (float)((ourBots[i].location.x - curLoc.x)/size);
					dy += (float)((ourBots[i].location.y - curLoc.y)/size);
				}
				else if(ourBots[i].type == RobotType.ARCHON) {
					dx += 4*(float)((ourBots[i].location.x - curLoc.x)/size);
					dy += 4*(float)((ourBots[i].location.y - curLoc.y)/size);
				}
			}
			opDir = new Direction(dx, dy);
			opDir = opDir.opposite();
			Direction rotLeft = opDir.rotateLeftDegrees(30);
			Direction rotRight= opDir.rotateRightDegrees(30);
			if (rotLeft.radians > 0) {
				return rotLeft;
			}
			else if (rotRight.radians < 0) {
				return rotRight;
			}
			else {
				return opDir;
			}
		}
		else {
			return new Direction(0);
		}
	}
	
	public static void waterSurroundingTrees() throws GameActionException {
		//first checks if there are trees it can water, then waters
		if (lowHealthTrees.size() > 0) {
            if (rc.canWater(lowHealthTrees.get(0))) {
            	rc.water(lowHealthTrees.get(0));
            	canMove = false;
            }
        } 
	}
	
	public static void buildUnits(Direction dirToBuild) throws GameActionException {
		//try to build LUMBERJACK, make sure to build START_LUMBERJACK_COUNT lumberjacks first, then subject to ratio limitations
		if (rc.canBuildRobot(RobotType.SCOUT, dirToBuild) && rc.isBuildReady() && (SCOUT_RATIO*scoutCount < soldierCount || scoutCount < START_SCOUT_COUNT)) {
	        rc.buildRobot(RobotType.SCOUT, dirToBuild);
	        rc.broadcast(SCOUT_CHANNEL, scoutCount+1);
	    }
		else if (rc.canBuildRobot(RobotType.LUMBERJACK, dirToBuild) && rc.isBuildReady() && (LUMBERJACK_RATIO*lumberjackCount < soldierCount || lumberjackCount < START_LUMBERJACK_COUNT)) {
	        rc.buildRobot(RobotType.LUMBERJACK, dirToBuild);
	        rc.broadcast(LUMBERJACK_CHANNEL, lumberjackCount+1);
	    }
	    //try to build SCOUT, make sure to build START_SCOUT_LIMIT scouts first, then subject to ratio limitations
		else if (rc.canBuildRobot(RobotType.SCOUT, dirToBuild) && rc.isBuildReady() && (SCOUT_RATIO*scoutCount < soldierCount || scoutCount < START_SCOUT_COUNT) && lumberjackCount >= START_LUMBERJACK_COUNT) {
	        rc.buildRobot(RobotType.SCOUT, dirToBuild);
	        rc.broadcast(SCOUT_CHANNEL, scoutCount+1);
	    }
	    //try to build SOLDIER, make sure soldierCount:lumberjackCount < LUMBERJACK_RATIO
	    else if (rc.canBuildRobot(RobotType.SOLDIER, dirToBuild) && rc.isBuildReady()) {
	        rc.buildRobot(RobotType.SOLDIER, dirToBuild);
	        rc.broadcast(SOLDIER_CHANNEL, soldierCount+1); 
	    }
	    //try to build TANK, make sure soldierCount:tankCount < TANK_RATIO
	    /*
	    if (rc.canBuildRobot(RobotType.TANK, dir) && rc.isBuildReady() && TANK_RATIO*tankCount < soldierCount) {
	        rc.buildRobot(RobotType.TANK, dir);
	        rc.broadcast(TANK_CHANNEL, tankCount+1);
	    } 
	    */
	        
	}
	
	public static void buildUnitsRemastered(Direction dirToBuild, float bullets, float lumberRatio) throws GameActionException {
		System.out.println("Normal Build: ");
		// Check if ready to build
		if (rc.isBuildReady()) {
			// Prioritize by bullets; Tanks -> infantry -> scouts
			// Try Tank
			if ((bullets >= 500) || ((bullets >= 300) && (TANK_RATIO*tankCount < soldierCount))) {
				System.out.println("Try Tank");
				// Check for spacing
				Direction dir = Plant.scanBuildRadiusTank(scanInt, dirToBuild.getAngleDegrees());
				if (dir != null) {
					rc.buildRobot(RobotType.TANK, dir);
					buildingTank += 20;
					return;
				}
			}
			
			// Try Infantry
			if (bullets >= 100) {
				System.out.println("Try Infantry");
				// Check for enough scouts
//				if (scoutCount > START_SCOUT_COUNT) {
					
				// Pseudo-Random for lumberjacks or soldiers
				if (((0.7*lumberRatio <= Math.random()) && (lumberRatio <= 1.0))
						|| (lumberjackCount > 5*soldierCount)) {
					System.out.println(dirToBuild);
					if (rc.canBuildRobot(RobotType.SOLDIER, dirToBuild)) {
						rc.buildRobot(RobotType.SOLDIER, dirToBuild);
						buildingSoldier += 20;
						return;
					}
				}
				else {
					if (rc.canBuildRobot(RobotType.LUMBERJACK, dirToBuild)) {
						rc.buildRobot(RobotType.LUMBERJACK, dirToBuild);
						buildingLumberjack += 20;
						return;
					}
				}
//				}
			}
			
			// Build scouts if nothing else (change later)
			if (bullets >= 80) {
				System.out.println("Try Scout");
				if (soldierCount > 3*scoutCount) {
					if ((scoutCount < (rc.getRoundNum()/200 + 2)) && (scoutCount < SCOUT_LIMIT)) {
						if (rc.canBuildRobot(RobotType.SCOUT, dirToBuild)) {
					        rc.buildRobot(RobotType.SCOUT, dirToBuild);
					        buildingScout += 20;
					        return;
						}
					}
				}
			}			
		}	        
	}
	
	// Function to obtain the robot info units in the specified team
	
	private static RobotInfo[] NearbyUnits(Team team){

		return rc.senseNearbyRobots((float)10, team);
	}
	
	public static void propagateBuild() {
		buildingLumberjack = Math.max(0, buildingLumberjack - 1);
		buildingScout = Math.max(0, buildingScout - 1);
		buildingSoldier = Math.max(0, buildingSoldier - 1);
		buildingTank = Math.max(0, buildingTank - 1);
		buildingTree = Math.max(0,  buildingTree - 1);
		
		unfreeze = Math.max(0, unfreeze - 1);
	}
	
	public static boolean sync(int roundNum) {
		if(roundNum > syncMin) {
			if ((roundNum % syncRefresh) < 3) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return true;
		}
	}
}