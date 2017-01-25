// AI for gardener under normal control
package naclbot.units.AI.gardener;
import battlecode.common.*;
import naclbot.units.motion.Move;
import naclbot.units.motion.search.TreeSearch;
import naclbot.variables.GlobalVars;
import naclbot.variables.BroadcastChannels;
import naclbot.units.interact.iFeed;
import naclbot.units.motion.*;

import java.util.ArrayList;

public class GardenerBot extends GlobalVars {
	
	public static int role;
	public static boolean iDied = false;
	
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
	
	public static boolean minSat = false;
	//--------------------------------------------------
	public static int scanInt = 5;
	
	static int currentNumberofGardeners;
	
	static int gardenerNumber;
	
	static int unitNumber;
	
	public static float buildDir, prevBuildDir;
	public static Direction buildVec = new Direction(0);
	public static Direction prevBuildVec = new Direction(0);
	//--------------------------------------------------
	
	
	
	public static void init() throws GameActionException {
		System.out.println("I'm a gardener!");
		
		//value of initial role, set to planting trees first
		role = 1;
		
		 // Get own soldierNumber - important for broadcasting 
        gardenerNumber = rc.readBroadcast(BroadcastChannels.GARDENER_NUMBER_CHANNEL);
        currentNumberofGardeners = gardenerNumber + 1;
        
        unitNumber = rc.readBroadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL);
        rc.broadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL, unitNumber + 1);
        
        // Update soldier number for other soldiers to see.....
        rc.broadcast(BroadcastChannels.GARDENER_NUMBER_CHANNEL, currentNumberofGardeners);

        
		buildVec = dirAway(rc.getLocation()); 
		main();
	}
		
		
	public static void main() throws GameActionException {
        // The code you want your robot to perform every round should be in this loop
		
        while (true) {
        	//System.out.println(role);
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	
            	// Check if it did not die, and reset the number of gardeners and units
            	if (iDied) {
            		iDied = false;
            		 // Get own soldierNumber - important for broadcasting 
                    gardenerNumber = rc.readBroadcast(BroadcastChannels.GARDENER_NUMBER_CHANNEL);
                    currentNumberofGardeners = gardenerNumber + 1;
                    
                    unitNumber = rc.readBroadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL);
                    rc.broadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL, unitNumber + 1);
                    
                    // Update soldier number for other soldiers to see.....
                    rc.broadcast(BroadcastChannels.GARDENER_NUMBER_CHANNEL, currentNumberofGardeners);
            	}
            	
            	// Listen for home archon's location, not implemented yet
            	// Check later
                xPos = rc.readBroadcast(0);
                yPos = rc.readBroadcast(1);                
                archonLoc = new MapLocation(xPos,yPos);
                
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
                //ROLE DESIGNATION
                
                // Force minimum number of units
                // Scout-Tree-Tree-Scout-Soldier-Trees
//                System.out.println("Tree status: " + treeImpossible + ", Num Tree: " + rc.getTreeCount());
                
                
                float bulletNum = rc.getTeamBullets();
                if (bulletNum > GameConstants.BULLET_TREE_COST) {
                	if (scoutCount < minScouts1) {
                		System.out.println("Scouts1");
                		buildUnitNew(RobotType.SCOUT, bulletNum, buildDir);
                    }
                	else if ((rc.getTreeCount() < minTrees1) && (!treeImpossible)) {
                		System.out.println("Trees1");
                    	if (rc.hasTreeBuildRequirements()) {
                    		// To end at 0
                    		Direction plantDirs[] = Plant.scanBuildRadius(scanInt, buildDir);
                    		if (plantDirs[0] != null) {
                    			rc.plantTree(plantDirs[0]);
                    		}
                    		else {
                    			treeImpossible = true;
                    		}
                    	}
                    	canMove = false;
                    }
                	else if (scoutCount < minScouts2) {
                		System.out.println("Scouts2");
                    	buildUnitNew(RobotType.SCOUT, bulletNum, buildDir);
                    }
                	// Toggle with lumberjack
                	else if (soldierCount < minSoldiers1) {
                		System.out.println("Soldiers1");
                    	buildUnitNew(RobotType.SOLDIER, bulletNum, buildDir);
                    }
                	else {
                		minSat = true;
                	}
                	
                	// End early game
                	//------------------------------------------------
                    if (minSat) {
                    	System.out.println("Satisfication");
                    	Direction plantDirs[] = Plant.scanBuildRadius(scanInt, buildDir);
                    	System.out.println("Empty spaces: " + plantDirs[0] + ", " + plantDirs[1]);
                    	if (plantDirs[0] != null) {
                    		rc.plantTree(plantDirs[0]);
                    		canMove = false;
                    	}
                    	else if (plantDirs[1] != null){
                    		buildUnits(plantDirs[1]);
                    	}
                    }                	
                }
                
                //--------------------------------------------------------------------------------
                waterSurroundingTrees();
                
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();
            } catch (Exception e) {
                System.out.println("Gardener Exception");
                e.printStackTrace();
            }
        }
	}
	
	public static void manageBeingAttacked(MapLocation loc) throws GameActionException{
		boolean beingAttacked = iFeed.willBeAttacked(loc);
		if (beingAttacked) {
			// BIT 0 - GIVES MY ID
			// BIT 1 - GIVES MY X
			// BIT 2 - GIVES MY Y
			// BIT 3 - GIVES NEAREST ENEMY
			RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
			rc.broadcast(BroadcastChannels.GARDENER_DISTRESS_CHANNEL, rc.getID());
			rc.broadcast(BroadcastChannels.GARDENER_DISTRESS_CHANNEL+1, (int) loc.x);
			rc.broadcast(BroadcastChannels.GARDENER_DISTRESS_CHANNEL+2, (int) loc.y);
			if (nearbyEnemies.length > 0) {
				rc.broadcast(BroadcastChannels.GARDENER_DISTRESS_CHANNEL+3, nearbyEnemies[0].getID());
			} else {
				rc.broadcast(BroadcastChannels.GARDENER_DISTRESS_CHANNEL+3, -1);
			}
			boolean willDie = iFeed.willFeed(loc);
			if (willDie) {
				iDied = true;
				// Get own soldierNumber - important for broadcasting 
		        gardenerNumber = rc.readBroadcast(BroadcastChannels.GARDENER_NUMBER_CHANNEL);
		        currentNumberofGardeners = gardenerNumber - 1;
		        
		        unitNumber = rc.readBroadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL);
		        rc.broadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL, unitNumber - 1);
		        
		        // Update soldier number for other soldiers to see.....
		        rc.broadcast(BroadcastChannels.GARDENER_NUMBER_CHANNEL, currentNumberofGardeners);
			}
		}
	}
	
	public static void buildUnitNew(RobotType Unit, float bullets, float dirDeg) throws GameActionException {
		if (bullets >= Unit.bulletCost) {
			if (rc.isBuildReady()) {
				Direction plantDirs[] = Plant.scanBuildRadius(scanInt, dirDeg);
				if (plantDirs[1] != null) {
					rc.buildRobot(Unit, plantDirs[1]);
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
			}
			opDir = new Direction(dx, dy);
			opDir = opDir.opposite();
			return opDir;
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
	
	public static void buildOverwrite() throws GameActionException {
		
	}
}