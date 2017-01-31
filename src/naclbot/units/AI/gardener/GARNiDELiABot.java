// AI for gardener under normal control
package naclbot.units.AI.gardener;

import battlecode.common.*;
import naclbot.units.motion.Move;
import naclbot.units.motion.search.TreeSearch;
import naclbot.variables.GlobalVars;
import naclbot.variables.BroadcastChannels;
import naclbot.units.interact.iFeed;
import naclbot.units.motion.*;
import naclbot.units.motion.Chirasou;
import naclbot.units.motion.search.EnemyArchonSearch;
import naclbot.units.AI.gardener.ReLife;

import java.util.ArrayList;

public class GARNiDELiABot extends GlobalVars {
	
	// Unit counts
	public static int scoutCount;
	public static int soldierCount;
	public static int lumberjackCount;
	public static int tankCount;
	public static int gardenerCount;
	
	// Get enemy info:
	public static int enemyArchons;
	public static int enemyGardeners;
	public static int enemyLumberjacks;
	public static int enemyScouts;
	public static int enemySoldiers;
	public static int enemyTanks;
	public static int enemyTrees;
	public static int neutralTrees;
	public static float neutralTreeArea;
	
	// Archon information
	public static MapLocation myArchon = null;
	public static MapLocation oppositeEnemyArchon;
	public static Direction initDir;
	public static float initDist = 0;
	public static int numArchons;
	
	// Time allocated to dispersing
	private static int disperseTime = 6;
	private static int spaceTime = 21;
	public static int unitStart = 0;
	public static Direction prevMoveDir;
	public static Direction prevMoveVec = new Direction(0);
	
	// Gardener info
	static int unitNumber;
	public static boolean believeHasDied = false;
	
	// Timers and CDs
	public static int buildingLumberjack = 0;
	public static int buildingScout = 0;
	public static int buildingSoldier = 0;
	public static int buildingTank = 0;
	public static int buildingTree = 0;
	
	public static int progressLumberjack = 0;
	public static int progressScout = 0;
	public static int progressSoldier = 0;
	public static int progressTank = 0;
	
	public static int progressLumberjackCD = 0;
	public static int progressScoutCD = 0;
	public static int progressSoldierCD = 0;
	public static int progressTankCD = 0;
	
	public static int unfreeze = 0;
	
	// Build
	public static ArrayList<Object[]> buildOrder = new ArrayList<Object[]>();
	private static float scanInt = (float)5;
	private static int buildRadius = 1;
	private static float scanRad = (float)1;
	
	private static float senseOffset = 4;
	private static float searchRad = 3;
	
	private static float congThresh = (float)0.5;
	
	private static float emptyDensity = 1;
	private static final float emptyDensityThresh = (float)0.65;
	private static final float initDistThresh = (float)40;
	private static final float farDistThresh = (float)60;
	
	public static ArrayList<TreeInfo> alreadyAssignedTrees = new ArrayList<TreeInfo>();
	
	// Unit entry point
	public static void init() throws GameActionException {
		System.out.println("I'm a gardener!");
		
		// THIS IS OPPOSING ARCHON LOCATION
		MapLocation oppositeEnemyArchon = EnemyArchonSearch.getCorrespondingArchon();
		rc.setIndicatorLine(rc.getLocation(), oppositeEnemyArchon, 255, 255, 255);
		initDist = rc.getLocation().distanceTo(oppositeEnemyArchon);
		initDir = new Direction(rc.getLocation(), oppositeEnemyArchon);
		//manageCorrespondingArchon();
		
		// Get own soldierNumber - important for broadcasting 
        int gardenerNumber = rc.readBroadcast(BroadcastChannels.GARDENERS_ALIVE_CHANNEL);
        int currentNumberofGardeners = gardenerNumber + 1;
        
        // Don't know if neccessary?
        unitNumber = rc.readBroadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL);
        rc.broadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL, unitNumber + 1);
        
        // Retrieve the number of active gardeners and increment......
       	int numberOfActiveGardeners = rc.readBroadcast(BroadcastChannels.GARDENERS_ALIVE_CHANNEL);
       	rc.broadcast(BroadcastChannels.GARDENERS_ALIVE_CHANNEL, numberOfActiveGardeners + 1);
        
        scoutCount = rc.readBroadcast(BroadcastChannels.SCOUTS_ALIVE_CHANNEL);                
        soldierCount = rc.readBroadcast(BroadcastChannels.SOLDIERS_ALIVE_CHANNEL);
        lumberjackCount = rc.readBroadcast(BroadcastChannels.LUMBERJACKS_ALIVE_CHANNEL);
        tankCount = rc.readBroadcast(BroadcastChannels.TANKS_ALIVE_CHANNEL);
        
        buildingScout = rc.readBroadcast(BroadcastChannels.GARDENER_CONSTRUCT_SCOUT);
        progressScout = (int)Math.ceil(buildingScout / 22.0);
        buildingSoldier = rc.readBroadcast(BroadcastChannels.GARDENER_CONSTRUCT_SOLDIER);
        progressSoldier = (int)Math.ceil(buildingSoldier / 22.0);
        buildingLumberjack = rc.readBroadcast(BroadcastChannels.GARDENER_CONSTRUCT_LUMBERJACK);
        progressLumberjack = (int)Math.ceil(buildingLumberjack / 22.0);
        buildingTank = rc.readBroadcast(BroadcastChannels.GARDENER_CONSTRUCT_TANK);
        progressTank = (int)Math.ceil(buildingTank / 22.0);
        
        // Get enemy info:
        
        getDatafromRem(rc.getRoundNum());
        
        // Determine build order
        
        System.out.println("Attempt to find parent");
        int archonNum = determineParent();
        System.out.println("Archon Num: " + archonNum);
        if(archonNum >= 0) {
        	buildOrder = determineBuildOrder(archonNum);
        }
        
		main();
	}
	
	// Unit main function
	public static void main() throws GameActionException {
		
		// Initialization
		float previousHealth = rc.getHealth();
		unitStart = 0;
		Team ourTeam = rc.getTeam();
		Team enemyTeam = ourTeam.opponent();
		float sensorRadius = rc.getType().sensorRadius;
		
		Direction buildDir = initDir;
		Direction buildVec = new Direction(0);
		
		//boolean believeHasDied = false; // this is now further up
		boolean override = false;
		boolean canMove = true;
		
		prevMoveVec = initDir.opposite();
		
		boolean saturated = false;
		
		boolean holdBuild = false;
		
		boolean voted = false;
		
		boolean failedBuild = false;
		
		float newScanInt = scanInt;
		
		while (true) {
			try {
				System.out.println("Clock: " + Clock.getBytecodeNum());
				
				Win();
				
				// Get build timers
				propagateBuild();
				
				updateTargetTrees();
				System.out.println("Update Target Trees: " + Clock.getBytecodeNum());
				
				setNearbyTreeDots();				
				
				int rem = rc.getRoundNum();
				
				boolean hold = false;
				
				// Get current location
				MapLocation myLocation = rc.getLocation();
				
				Chirasou.attemptInteractWithTree(myLocation, rc.getType().bodyRadius);
				
//				initDir = new Direction(myLocation, oppositeEnemyArchon);
				RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, enemyTeam);
				
				BroadcastChannels.broadcastNearestEnemyLocation(enemyRobots, myLocation, unitNumber, myLocation.add(Move.randomDirection(), (float)0.5), rem); 
				System.out.println("Broadcast Enemy Location: " + Clock.getBytecodeNum());
				
				// If the robot thought it died previously but didn't.... update information...
            	if(believeHasDied){
            		fixAccidentalDeathNotification();
            	}
				
				// Get distress location if applicable
				System.out.println("Previous health: " + previousHealth);
            	BroadcastChannels.broadcastDistress(previousHealth, enemyRobots, myLocation, unitNumber);
            	
                // Check number of all units currently in service
                scoutCount = rc.readBroadcast(BroadcastChannels.SCOUTS_ALIVE_CHANNEL);                
                soldierCount = rc.readBroadcast(BroadcastChannels.SOLDIERS_ALIVE_CHANNEL);
                lumberjackCount = rc.readBroadcast(BroadcastChannels.LUMBERJACKS_ALIVE_CHANNEL);
                tankCount = rc.readBroadcast(BroadcastChannels.TANKS_ALIVE_CHANNEL);
                gardenerCount = rc.readBroadcast(BroadcastChannels.GARDENERS_ALIVE_CHANNEL);
                
                buildingScout = rc.readBroadcast(BroadcastChannels.GARDENER_CONSTRUCT_SCOUT);
                progressScout = (int)Math.ceil(buildingScout / 22.0);
                buildingSoldier = rc.readBroadcast(BroadcastChannels.GARDENER_CONSTRUCT_SOLDIER);
                progressSoldier = (int)Math.ceil(buildingSoldier / 22.0);
                buildingLumberjack = rc.readBroadcast(BroadcastChannels.GARDENER_CONSTRUCT_LUMBERJACK);
                progressLumberjack = (int)Math.ceil(buildingLumberjack / 22.0);
                buildingTank = rc.readBroadcast(BroadcastChannels.GARDENER_CONSTRUCT_TANK);
                progressTank = (int)Math.ceil(buildingTank / 22.0);
                
                int nearScouts = 0;
                int nearSoldiers = 0;
                int nearLumberjacks = 0;
                int nearTanks = 0;
                
                RobotInfo[] nearAllies = rc.senseNearbyRobots(-1, ourTeam);
                for (int i = 0; i < nearAllies.length; i++) {
                	if (nearAllies[i].type == RobotType.SCOUT) {
                		nearScouts++;
                	}
                	else if (nearAllies[i].type == RobotType.SOLDIER) {
                		nearSoldiers++;
                	}
                	else if (nearAllies[i].type == RobotType.LUMBERJACK) {
                		nearLumberjacks++;
                	}
                	else if (nearAllies[i].type == RobotType.TANK) {
                		nearTanks++;
                	}                	
                }
                
                
                System.out.println("Broadcasts: " + Clock.getBytecodeNum());
            	// Get enemy info:
                getDatafromRem(rem);
                
                System.out.println("Gardener: " + gardenerCount);
                System.out.println("Scouts: " + scoutCount + ", Soldiers: " + soldierCount + 
                					", Lumberjacks: " + lumberjackCount + ", Tanks: " + tankCount);
                System.out.println();
                System.out.println("In Construction: ");
                System.out.println("Scouts: " + progressScout + ":" + buildingScout + ", Soldiers: " + progressSoldier + ":" + buildingSoldier +
    					", Lumberjacks: " + progressLumberjack + ":" + buildingLumberjack + ", Tanks: " + progressTank + ":" + buildingTank);
                System.out.println();                
                
//                System.out.println("Unit Start: " + unitStart);
                
            	//--------------------------------------------------------------------------------
            	prevMoveDir = prevMoveVec.opposite();
            	
            	System.out.println("Data from Rem: " + Clock.getBytecodeNum());
            	
            	RobotInfo[] ourBots = rc.senseNearbyRobots(sensorRadius, ourTeam);
            	System.out.println("BuildDir: " + buildDir);
            	if(canMove) {
            		buildDir = dirAway(myLocation, ourBots, initDir, sensorRadius);
            	}
                
            	System.out.println("Dir Away: " + Clock.getBytecodeNum());
            	
                // Attempt movement     
                MapLocation destPoint = movement(canMove, myLocation, ourTeam, sensorRadius, prevMoveDir);
                
                System.out.println("Movement: " + Clock.getBytecodeNum());
                
                if(destPoint != null) {
                	checkDeath(destPoint);
                	rc.move(destPoint);
                }
                else {
                	checkDeath(myLocation);
                }
                
                System.out.println("Check Death: " + Clock.getBytecodeNum());
            	
                //--------------------------------------------------------------------------------
                // Override
                
                // Check for enemy tree count
                if (!override && (enemyTrees > rc.getTreeCount()) && (initDist > 50)) {
                	
                	// If present override next build order with tree
                	Object order[] = new Object[2];
                	order[0] = new String("TREE");
                	order[1] = new Integer(1);
                	buildOrder.add(0, order);
                	override = true;
                }
                
                // Check for scout count
                if (!override && (scoutCount+progressScout <= 0) && (rem > 200)) {
                	System.out.println("Override Scout");
                	// If present override next build order with scout
                	Object order[] = new Object[2];
                	order[0] = new String("SCOUT");
                	order[1] = new Integer(1);
                	buildOrder.add(0, order);
                	override = true;
                }

                // Check for surrounding enemies
                if (((!override && rc.senseNearbyRobots(myLocation, sensorRadius, enemyTeam).length > 0) && (nearSoldiers < 1)) ||
                	(!override && (enemySoldiers > 5) && (rc.getTreeCount() > soldierCount))){
                	
                	// If present override next build order with soldier
                	Object order[] = new Object[2];
                	order[0] = new String("SOLDIER");
                	order[1] = new Integer(1);
                	buildOrder.add(0, order);
                	override = true;
                }
                
                // Check for tree density
                if (!override && (((neutralTrees > 20) && (lumberjackCount <= 0) && (soldierCount > 1)) ||
    			((neutralTrees > 20) && (rem > 200) && (saturated) && (lumberjackCount < 10)) ||
    			((neutralTreeArea > (Math.PI*5*5)) && (rem > 200) && (lumberjackCount < 2)))) {
                	
                	Object order[] = new Object[2];
                	order[0] = new String("LUMBERJACK");
                	order[1] = new Integer(1);
                	buildOrder.add(0, order);
                	override = true;
                }

                //--------------------------------------------------------------------------------
                // Unit Building
                
                System.out.println("Build: " + Clock.getBytecodeNum());
                
                // Need build rotations
                // Check if enough bullets
                float bulletNum = rc.getTeamBullets();
                System.out.println("Bullets: " + bulletNum + ", Ready: " + rc.isBuildReady());
                
                if ((bulletNum > GameConstants.BULLET_TREE_COST) && (rc.isBuildReady())) {
                	
                	// Finds availible spots to build units/plant trees
                	System.out.println("Before: " + Clock.getBytecodeNum());
                	
                	Direction buildDirs[];
                	
                	if(failedBuild) {
                		buildDirs = ReLife.scanBuildRadius(newScanInt, 0, buildRadius, scanRad);
                	}
                	else {
                		buildDirs = ReLife.scanBuildRadius(scanInt, buildDir.opposite().getAngleDegrees(), buildRadius, scanRad);
                	}
                	
                	System.out.println("After: " + Clock.getBytecodeNum());
                	
                	if(buildDirs[0] == null) {
                		saturated = true;
                	}
                	else {
                		saturated = false;
                	}
                	
                	if((buildDirs[0] == null) && (buildDirs[1] == null)) {
                		failedBuild = true;
                		if (scanInt > 1) {
                			newScanInt = (float)scanInt/2;
                		}
                	}
                	else {
                		failedBuild = false;
                	}
                	
                	System.out.println("Congestion: " + ReLife.congestion);
                	System.out.println("Congestion (movement): " + ReLife.congestionOptimal);
                	System.out.println("Neutral Trees: " + neutralTrees);
                	
                	//----------------------------------------------------------------------------
                	// Congestion override
                	if((ReLife.congestion >= congThresh) || 
                			(ReLife.congestionOptimal >= congThresh) && (nearLumberjacks < 1)) {
                		ReLife.congestionOptimal = 0;
                		
                		if(buildOrder.size() > 0) {
                			System.out.println("Need lumberjack");
                			
                			// Replace soldier with lumberjack
                			if (((String)buildOrder.get(0)[0]).equals("SOLDIER")) {
                				System.out.println("Replace soldier order");
                				int qty = (Integer)buildOrder.get(0)[1];
                				if(qty > 1) {
                					Object order[] = new Object[2];
                                	order[0] = new String("SOLDIER");
                                	order[1] = qty-1;
                                	buildOrder.set(0, order);
                                	
                                	Object newOrder[] = new Object[2];
                                	newOrder[0] = new String("LUMBERJACK");
                                	newOrder[1] = 1;
                                	buildOrder.add(0, newOrder);
                            	}
                				else if (!((String)buildOrder.get(0)[0]).equals("LUMBERJACK")){
                					Object newOrder[] = new Object[2];
                                	newOrder[0] = new String("LUMBERJACK");
                                	newOrder[1] = 1;
                                	buildOrder.set(0, newOrder);
                				}                            	
                			}
                		}
                		// Else add lumberjack order
                		else {
                			Object newOrder[] = new Object[2];
                        	newOrder[0] = new String("LUMBERJACK");
                        	newOrder[1] = 1;
                        	buildOrder.add(0, newOrder);
                		}
                	}
                	//----------------------------------------------------------------------------
                	
                	System.out.println("Size: " + buildOrder.size());
                	
                	// Override trumps
                	if (buildOrder.size() > 0) {
                		
                		System.out.println("Using build order! Size: " + buildOrder.size());
                		
                		while(rc.isBuildReady() && (buildOrder.size() > 0)) {
                			String nextBuild = (String)buildOrder.get(0)[0];
                    		int nextQty = (Integer)buildOrder.get(0)[1];
                    		
                    		System.out.println("Order: " + nextBuild + ", Qty: " + nextQty);
            				
                    		if(nextBuild == "TREE") {
                    			
                    			System.out.println("Try building Tree");
                    			
                    			if (buildDirs[0] != null) {
                            		rc.plantTree(buildDirs[0]);
                            		buildingTree += 20;
                            		canMove = false;
                            		
                            		nextQty -= 1;
                            		if (nextQty <= 0) {
                            			buildOrder.remove(0);
                            		}
                            		else {
                            			Object order[] = new Object[2];
                            			order[0] = nextBuild;
                                    	order[1] = nextQty;
                            			buildOrder.set(0, order);
                            		}
                    			}
                    			buildOrder.remove(0);
                    		}
                    		else {
                    			RobotType nextType = parseString(nextBuild);
                    			if((nextType == RobotType.SCOUT) && (scoutCount + progressScout > 2)) {
                    				buildOrder.remove(0);
                    			}
                    			else if (((nextType != RobotType.SCOUT) && bulletNum < 100) || ((nextType == RobotType.SCOUT) && (bulletNum < 80))) {
                    				System.out.println("Hold it!");
                    				hold = true;
                    				break;
                    			}
                    			else {
                    				hold = false;
	                    			System.out.println("Try building Unit: " + nextType);
	                				if (nextType != null) {
	                					if (buildDirs[1] != null) {
	                						buildOverride(nextType, bulletNum, buildDirs[1]);
	                					}
	                					else {
	                						break;
	                					}
	                				}
	                				
	                				// Check quantity
	                				nextQty -= 1;
	                				if (nextQty <= 0) {
	                					System.out.println("Removed!");
	                					buildOrder.remove(0);
	                				}
	                				else {
	                        			Object order[] = new Object[2];
	                        			order[0] = nextBuild;
	                                	order[1] = nextQty;
	                        			buildOrder.set(0, order);
	                        		}
                    			}
                    		}
            			}
                		
                		// Reset build order if override
                		if (override && !hold) {
                			buildOrder = new ArrayList<Object[]>();
                			
                			// Turn it off
                    		override = false;	
                		}
                	}
                	
                	// No override/order/it failed
                	if(!hold && rc.isBuildReady()) {
                		if (gardenerCount > 1 && !holdBuild && saturated) {
                			holdBuild = true;
                		}
                		else {
	                		holdBuild = false;
	                		System.out.println("Tree Dir: " + buildDirs[0]);
		                	if ((buildDirs[0] != null) && (unitStart > spaceTime)) {
		                		System.out.println("Try planting tree");
		                		rc.plantTree(buildDirs[0]);
		                		buildingTree += 20;
		                		canMove = false;
		                	}
		                	else if (buildDirs[1] != null) {
		                		buildNextUnit(buildDirs[1], bulletNum, (float)(0.7)*(1-emptyDensity));
		                	}
                		}
                	}
                }
                
                System.out.println();
                System.out.println("Hold: " + hold);           
                System.out.println("In Construction: ");
                System.out.println("Scouts: " + progressScout + ":" + buildingScout + ", Soldiers: " + progressSoldier + ":" + buildingSoldier +
    					", Lumberjacks: " + progressLumberjack + ":" + buildingLumberjack + ", Tanks: " + progressTank + ":" + buildingTank);
                System.out.println();                
                
                System.out.println("Poll: " + Clock.getBytecodeNum());
                
                //--------------------------------------------------------------------------------
                // Generate list of trees that are not full health
                ArrayList<MapLocation> lowHealthTrees = TreeSearch.getNearbyLowTrees();
                waterSurroundingTrees(lowHealthTrees);
                
                // Get health before propagation
            	previousHealth = rc.getHealth();
            	
            	rc.setIndicatorLine(myLocation, myLocation.add(buildDir,1), 255, 0, 0);
            	
            	// If poll, report state
            	int pollState = rc.readBroadcast(BroadcastChannels.GARDENER_POLL);
            	System.out.println("Poll State: " + pollState);
            	
            	System.out.println("Saturated State: " + saturated);
            	
            	if (pollState == 1) {
            		
            		if (saturated) {
            			int fillState = rc.readBroadcast(BroadcastChannels.GARDENER_BUILD_FILL);
            			rc.broadcast(BroadcastChannels.GARDENER_BUILD_FILL, fillState += 1);
            		}
            		else {
            			int fillState = rc.readBroadcast(BroadcastChannels.GARDENER_BUILD_FILL);
            			rc.broadcast(BroadcastChannels.GARDENER_BUILD_FILL, fillState -= 1000);
            		}
            	}
            	
//            	// Poll 2
//            	int pollNumState = rc.readBroadcast(BroadcastChannels.GARDENER_NUMBER_POLL_STATE);
//            	int pollNumGard = rc.readBroadcast(BroadcastChannels.GARDENER_NUMBER_POLL);
//            	
//            	System.out.println("Poll State 2: " + pollNumState);
//            	
//            	if (pollNumState == 1) {
//            		rc.readBroadcast(BroadcastChannels.GARDENER_NUMBER_POLL_STATE);
//            		rc.broadcast(BroadcastChannels.GARDENER_NUMBER_POLL, pollNumGard+1);
////            		voted = true;
//            	}
//            	else {
////            		voted = false;
//            	}
				
            	// End turn
				Clock.yield();
				
			// Catch all exceptions and print error
	        } catch (Exception e) {
	            System.out.println("Gardener Exception");
	            e.printStackTrace();
	        }
		}
	}
	
	//-----------------------------------------------------------------------------
	//-----------------------------------------------------------------------------
	//---------------------------------[Watering]----------------------------------
	
	
	public static void waterSurroundingTrees(ArrayList<MapLocation> lowHealthTrees) throws GameActionException {
		//first checks if there are trees it can water, then waters
		if (lowHealthTrees.size() > 0) {
            if (rc.canWater(lowHealthTrees.get(0))) {
            	rc.water(lowHealthTrees.get(0));
//            	canMove = false;
            }
        } 
	}
	
	//---------------------------------[Movement]----------------------------------
	private static MapLocation movement(boolean canMove, MapLocation curLoc, Team us, float sensorRadius, 
										Direction prevBuildDir) throws GameActionException {
		// Check if not surrounded 
		if (canMove) {
			
			// Increase timer
    		unitStart++;
        	
			// Check if unit was just built. If so, scatter
			if (unitStart < disperseTime) {
        		
				// Generate free point to move to
				MapLocation destination = Chirasou.Disperse(us, curLoc, battlecode.common.RobotType.GARDENER.strideRadius);
        		Direction dirToDestination = curLoc.directionTo(destination);
        		
        		// If point is found, try to move there
        		if (dirToDestination != null) {
        			MapLocation newLoc = Yuurei.tryMoveInDirection(dirToDestination, rc.getType().strideRadius, rc.getLocation());
        			
        			// If can move to location...
        			if (newLoc != null) {
        				
        				// Determine death
//        				checkDeath(newLoc);
        				return newLoc;
        			}
        		}
        	}
			
			// Unit's been hanging around for a while
        	else {
            	// Find optimal location to move
            	MapLocation destination = ReLife.findOptimalSpace(30, (float)sensorRadius-senseOffset, (float)sensorRadius-senseOffset,
            														prevBuildDir.getAngleDegrees(), searchRad);
            	
            	rc.setIndicatorDot(destination, 255, 0, 255);
            	
            	// Move to location
            	Direction dirToDestination = rc.getLocation().directionTo(destination);
            	
            	// If point is found, try to move there
            	if (dirToDestination != null) {
            		prevMoveVec = dirToDestination;
            		float lengthTo = rc.getLocation().distanceTo(destination);
            		MapLocation newLoc = Yuurei.tryMoveInDirection(dirToDestination, lengthTo, rc.getLocation());
            		
            		// If can move to location...
            		if (newLoc != null) {
            			
            			// Determine death
//            			checkDeath(newLoc);
            			return newLoc;
            		}
            	}
        	}
        }
		return null;
	}
	
	public static Direction dirAway(MapLocation curLoc, RobotInfo[] ourBots, Direction prevDir, float sensorRadius) {
		
		Direction opDir;
		float dx = 0;
		float dy = 0;
		int size = ourBots.length;
		if(size > 1) {
			for(int i = 1; i < size; i++) {
				if(ourBots[i].type == RobotType.GARDENER) {
					float dist = curLoc.distanceTo(ourBots[i].location);
					
					dx += (float)(sensorRadius-dist)*((ourBots[i].location.x - curLoc.x)/size);
					dy += (float)(sensorRadius-dist)*((ourBots[i].location.y - curLoc.y)/size);
				}
//				else if(ourBots[i].type == RobotType.ARCHON) {
//					dx += 4*(float)((ourBots[i].location.x - curLoc.x)/size);
//					dy += 4*(float)((ourBots[i].location.y - curLoc.y)/size);
//				}
			}
			if ((dx == 0) && (dy == 0)) {
				return prevDir;
			}
			
			System.out.println("Gardener pull: " + dx + ", " + dy);
			dx -= prevDir.getDeltaX(1);
			dy -= prevDir.getDeltaY(1);
			opDir = new Direction(dx, dy);
			opDir = opDir.opposite();
			return opDir;
		}
		else {
			return prevDir;
		}
	}
	
	//----------------------------------[Distress]----------------------------------
	public static void fixAccidentalDeathNotification() throws GameActionException {
		// Reset belief in the robot dying this round....
    	believeHasDied = false;    	

		// Get the current number of soldiers in service
        int numberOfAliveGardeners = rc.readBroadcast(BroadcastChannels.GARDENERS_ALIVE_CHANNEL);
        
        // Update soldier number for other units to see.....
        rc.broadcast(BroadcastChannels.GARDENERS_ALIVE_CHANNEL, numberOfAliveGardeners + 1);
	}
	
	private static void checkDeath(MapLocation location) throws GameActionException{
		
		System.out.println("Bytecode Start: " + Clock.getBytecodeNum());
		
		// If the lumberjack will lose all of its health from moving to that location....
		boolean willDie = iFeed.willFeed(location);
		
		// If the lumberjack believes that it will die this turn....
		if (willDie) {
			
			// Set the belief variable to true.....
			believeHasDied = true;
			
			// Get the current number of gardeners in service
	        int currentGardenerNumber = rc.readBroadcast(BroadcastChannels.GARDENERS_ALIVE_CHANNEL);
	        
	        // Update gardener number for other units to see.....
	        rc.broadcast(BroadcastChannels.GARDENERS_ALIVE_CHANNEL, currentGardenerNumber - 1);			
		}
		
		System.out.println("Bytecode End: " + Clock.getBytecodeNum());
	}
	
	//-------------------------------[Unit Building]-------------------------------
	private static void propagateBuild() throws GameActionException {		
		buildingLumberjack = Math.max(0, buildingLumberjack - 1);
		buildingScout = Math.max(0, buildingScout - 1);
		buildingSoldier = Math.max(0, buildingSoldier - 1);
		buildingTank = Math.max(0, buildingTank - 1);
		buildingTree = Math.max(0,  buildingTree - 1);
		
		rc.broadcast(BroadcastChannels.GARDENER_CONSTRUCT_LUMBERJACK, buildingLumberjack);
		rc.broadcast(BroadcastChannels.GARDENER_CONSTRUCT_SCOUT, buildingScout);
		rc.broadcast(BroadcastChannels.GARDENER_CONSTRUCT_SOLDIER, buildingSoldier);
		rc.broadcast(BroadcastChannels.GARDENER_CONSTRUCT_TANK, buildingTank);
		
		unfreeze = Math.max(0, unfreeze - 1);
	}
	
	public static void buildNextUnit(Direction dirToBuild, float bullets, float lumberRatio) throws GameActionException {
		// Check if ready to build
		System.out.println("Normal Build");
		if (rc.isBuildReady()) {
			// Prioritize by bullets; Tanks -> infantry -> scouts
			// Try Tank
			if ((bullets >= 700) || ((bullets >= 500) && (TANK_RATIO*tankCount < soldierCount))) {
				System.out.println("Try Tank");
				// Check for spacing
				Direction dir = ReLife.scanBuildRadiusTank(scanInt, dirToBuild.getAngleDegrees());
				if (dir != null) {
					rc.buildRobot(RobotType.TANK, dir);
					buildingTank += 22;
					rc.broadcast(BroadcastChannels.GARDENER_CONSTRUCT_TANK, buildingTank);
					return;
				}
			}
			
			// Try Infantry
			if (bullets >= 100) {
				System.out.println("Try Infantry");
				
				// Pseudo-Random for lumberjacks or soldiers
				/*if ((4*(lumberjackCount+1) <= (soldierCount+1)) && (lumberjackCount < 2)) {
					if (rc.canBuildRobot(RobotType.LUMBERJACK, dirToBuild)) {
						rc.buildRobot(RobotType.LUMBERJACK, dirToBuild);
						buildingLumberjack += 20;
						rc.broadcast(BroadcastChannels.GARDENER_CONSTRUCT_LUMBERJACK, progressLumberjack+1);
						return;
					}
				}
				else if (((lumberRatio <= Math.random()) || (5*lumberjackCount > soldierCount)) 
														&& (lumberRatio <= 1.0)) {*/
				if ((lumberRatio <= Math.random()) && (lumberRatio < 1.0)) {
					// Check for spacing
					if (rc.canBuildRobot(RobotType.SOLDIER, dirToBuild)) {
						rc.buildRobot(RobotType.SOLDIER, dirToBuild);
						buildingSoldier += 22;
						rc.broadcast(BroadcastChannels.GARDENER_CONSTRUCT_SOLDIER, buildingSoldier);
						return;
					}
				}
				else {
					// Check for spacing
					if (rc.canBuildRobot(RobotType.LUMBERJACK, dirToBuild)) {
						rc.buildRobot(RobotType.LUMBERJACK, dirToBuild);
						buildingLumberjack += 22;
						rc.broadcast(BroadcastChannels.GARDENER_CONSTRUCT_LUMBERJACK, buildingLumberjack);
						return;
					}
				}
			}
			
			// Build scouts if nothing else (change later)
			if (bullets >= 80) {
				System.out.println("Try Scout");
				
				// Have scout in commission
				if (scoutCount + progressScout <= 0) {
//				if (soldierCount > 5*scoutCount) {
					if (((scoutCount + progressScout) < Math.ceil(rc.getRoundNum()/500)) && (scoutCount < 2/*SCOUT_LIMIT*/)) {
						if (rc.canBuildRobot(RobotType.SCOUT, dirToBuild)) {
							System.out.println("Building Scout");
					        rc.buildRobot(RobotType.SCOUT, dirToBuild);
					        buildingScout += 22;
					        rc.broadcast(BroadcastChannels.GARDENER_CONSTRUCT_SCOUT, buildingScout);
					        return;
						}
					}
				}
			}			
		}
	}
	
	public static void buildOverride(RobotType Unit, float bullets, Direction buildDir) throws GameActionException {
		if (bullets >= Unit.bulletCost) {
			System.out.println("Build Override");
			if (rc.isBuildReady()) {
				rc.buildRobot(Unit, buildDir);
				
				// Set counters
				if (Unit == RobotType.LUMBERJACK) {
					buildingLumberjack += 22;
					rc.broadcast(BroadcastChannels.GARDENER_CONSTRUCT_LUMBERJACK, buildingLumberjack);
				}
				else if (Unit == RobotType.SCOUT) {
					buildingScout += 22;
					System.out.println("Building Scout");
					rc.broadcast(BroadcastChannels.GARDENER_CONSTRUCT_SCOUT, buildingScout);
				}
				else if (Unit == RobotType.SOLDIER) {
					buildingSoldier += 22;
					rc.broadcast(BroadcastChannels.GARDENER_CONSTRUCT_SOLDIER, buildingSoldier);
				}
				else if (Unit == RobotType.TANK) {
					buildingTank += 22;
					rc.broadcast(BroadcastChannels.GARDENER_CONSTRUCT_TANK, buildingTank);
				}
			}
		}
	}
	
	public static RobotType parseString(String order) {
		if (order.equals("LUMBERJACK")) {
			return RobotType.LUMBERJACK;
		}
		else if (order.equals("SCOUT")) {
			return RobotType.SCOUT;
		}
		else if (order.equals("SOLDIER")) {
			return RobotType.SOLDIER;
		}
		else if (order.equals("TANK")) {
			return RobotType.TANK;
		}
		else {
			return null;
		}
	}
	
	//----------------------------[Init Unit Building]-----------------------------
	public static int determineParent() throws GameActionException {
		RobotInfo[] nearbyBots = rc.senseNearbyRobots(-1, rc.getTeam());
		for (int i = 0; i < nearbyBots.length; i++) {
			if(nearbyBots[i].type == RobotType.ARCHON) {
				for (int j = 0; j < 2; j++) {
					int broadcastStart = BroadcastChannels.ARCHONS_TREE_DENSITY_CHANNEL + 2*j;
					int ID = rc.readBroadcast(broadcastStart);
					if (ID == nearbyBots[i].ID) {
						return j;
					}
				}
			}
		}	
		return -1;
	}
	
	public static ArrayList<Object[]> determineBuildOrder(int archonNum) throws GameActionException {
		ArrayList<Object[]> buildOrder = new ArrayList<Object[]>();
		
		emptyDensity = rc.readBroadcastFloat(BroadcastChannels.ARCHONS_TREE_DENSITY_CHANNEL + 2*archonNum + 1);
		System.out.println("Empty Density: " + emptyDensity);
		
		// Close
		if (initDist < initDistThresh) {
			if (emptyDensity < emptyDensityThresh) {
				buildOrder = getOrder(buildOrder, "SOLDIER", 1, soldierCount);
				buildOrder = getOrder(buildOrder, "LUMBERJACK", 1, lumberjackCount);
				buildOrder = getOrder(buildOrder, "SOLDIER", 1, soldierCount);
			}
			else {
				buildOrder = getOrder(buildOrder, "SOLDIER", 2, soldierCount);
				buildOrder = getOrder(buildOrder, "SCOUT", 1, scoutCount+progressScout);
			}
		}
		
		// Far
		else if (initDist > farDistThresh) {
			if (emptyDensity < emptyDensityThresh) {
				buildOrder = getOrder(buildOrder, "LUMBERJACK", 1, lumberjackCount);
				buildOrder = getOrder(buildOrder, "SCOUT", 1, scoutCount+progressScout);
			}
			else {
				buildOrder = getOrder(buildOrder, "SOLDIER", 1, soldierCount);
				buildOrder = getOrder(buildOrder, "SCOUT", 1, scoutCount+progressScout);
			}
		}
		
		// Middle
		else {
			if (emptyDensity < emptyDensityThresh) {
				buildOrder = getOrder(buildOrder, "LUMBERJACK", 1, lumberjackCount);
				buildOrder = getOrder(buildOrder, "SOLDIER", 1, soldierCount);
				buildOrder = getOrder(buildOrder, "SCOUT", 1, scoutCount+progressScout);	
			}
			
			// Default
			else {
				buildOrder = getOrder(buildOrder, "SOLDIER", 2, soldierCount);
//				buildOrder = getOrder(buildOrder, "LUMBERJACK", 1, lumberjackCount);
				buildOrder = getOrder(buildOrder, "SCOUT", 1, scoutCount+progressScout);	
			}
		}
		
		return buildOrder;
	}
	
	private static ArrayList<Object[]> getOrder(ArrayList<Object[]> build, String name, int qty, int existing) {
		int dif = qty - existing;
		if (dif > 0) {
			Object newOrder[] = new Object[2];
	    	newOrder[0] = name;
	    	newOrder[1] = dif;
	    	
	    	build.add(newOrder);
		}
		
		return build;		
	}
	
	private static void getDatafromRem(int roundNum) throws GameActionException {
		int rem = rc.readBroadcast(BroadcastChannels.REMBOT_INDICATOR_CHANNEL);
		
		if(Math.abs(roundNum - rem) <= 2) {
			// Get enemy info:
	        enemyArchons = rc.readBroadcast(BroadcastChannels.REMBOT_INFORMATION_CHANNEL_START);
	        enemyGardeners = rc.readBroadcast(BroadcastChannels.REMBOT_INFORMATION_CHANNEL_START + 1);
	        enemyLumberjacks = rc.readBroadcast(BroadcastChannels.REMBOT_INFORMATION_CHANNEL_START + 2);
	        enemyScouts = rc.readBroadcast(BroadcastChannels.REMBOT_INFORMATION_CHANNEL_START + 3);
	        enemySoldiers = rc.readBroadcast(BroadcastChannels.REMBOT_INFORMATION_CHANNEL_START + 4);
	        enemyTanks = rc.readBroadcast(BroadcastChannels.REMBOT_INFORMATION_CHANNEL_START + 5);
	        enemyTrees = rc.readBroadcast(BroadcastChannels.REMBOT_INFORMATION_CHANNEL_START + 6);
	        neutralTrees = rc.readBroadcast(BroadcastChannels.REMBOT_INFORMATION_CHANNEL_START + 7);
	        neutralTreeArea = rc.readBroadcastFloat(BroadcastChannels.REMBOT_INFORMATION_CHANNEL_START + 8);
		}
		else {
			// Reset enemy info:
	        enemyArchons = 0;
	        enemyGardeners = 0;
	        enemyLumberjacks = 0;
	        enemyScouts = 0;
	        enemySoldiers = 0;
	        enemyTanks = 0;
	        enemyTrees = 0;
	        neutralTrees = 0;
	        neutralTreeArea = 0;
		}	
	}
	
	//updates broadcast array to hold coordinates of trees around gardeners that lumberjacks should cut down
	public static void updateTargetTrees() throws GameActionException { 
		//initialize variables
		TreeInfo[] nearbyTrees = rc.senseNearbyTrees();
		ArrayList<Integer> emptyChannelOffsets = new ArrayList<Integer>();
		
		//iterate over possible array spots to see if there is an empty space
		for (int i=0; i<10; i++) { 
			if (rc.readBroadcastFloat(BroadcastChannels.LUMBERJACK_TREE_CHANNEL + 2*i) <= 0) { 
				emptyChannelOffsets.add(2*i);
			}
		}
		
		
		if (!emptyChannelOffsets.isEmpty()) {
			
			int totalEmpty = emptyChannelOffsets.size();
			int index = 0;
			System.out.println("this is the length of the arraylist: " + totalEmpty);
			for (TreeInfo tree: nearbyTrees) {
				
				if (totalEmpty == 0) {
					break;
				}
				
				//can be cut down if it is neutral 
				else if (tree.getTeam() == Team.NEUTRAL && !alreadyAssignedTrees.contains(tree)) {

					rc.broadcastFloat(BroadcastChannels.LUMBERJACK_TREE_CHANNEL + emptyChannelOffsets.get(index), tree.getLocation().x);
					rc.broadcastFloat(BroadcastChannels.LUMBERJACK_TREE_CHANNEL + emptyChannelOffsets.get(index) + 1, tree.getLocation().y);
					
					alreadyAssignedTrees.add(tree);
					
					totalEmpty--;
					index++;
					
				}
				
			}
		}
	}
	
	public static void setNearbyTreeDots() throws GameActionException {
		
		for (int i=0; i<10; i++) {
			float x = rc.readBroadcastFloat(BroadcastChannels.LUMBERJACK_TREE_CHANNEL + 2*i);
			float y = rc.readBroadcastFloat(BroadcastChannels.LUMBERJACK_TREE_CHANNEL + 2*i + 1);
			MapLocation loc = new MapLocation(x,y);
			rc.setIndicatorDot(loc, 153, 180, 255);

		}
	}
	
}