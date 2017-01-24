// AI for gardener under normal control
package naclbot.units.AI.gardener;
import battlecode.common.*;
import naclbot.units.motion.Move;
import naclbot.units.motion.search.TreeSearch;
import naclbot.variables.GlobalVars;
import naclbot.variables.BroadcastChannels;
import naclbot.units.motion.*;

import java.util.ArrayList;

public class GardenerBot extends GlobalVars {
	
	public static int role;
	
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
	
	public static void init() throws GameActionException {
		System.out.println("I'm a gardener!");
		
		//value of initial role, set to planting trees first
		role = 1;
		
		main();
	}
		
		
	public static void main() throws GameActionException {
        // The code you want your robot to perform every round should be in this loop
        while (true) {
        	//System.out.println(role);
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	// Listen for home archon's location, not implemented yet
            	// Check later
                xPos = rc.readBroadcast(0);
                yPos = rc.readBroadcast(1);                
                archonLoc = new MapLocation(xPos,yPos);
                
                // Check number of all units currently in service
                scoutCount = rc.readBroadcast(BroadcastChannels.SCOUT_NUMBER_CHANNEL);                
                soldierCount = rc.readBroadcast(BroadcastChannels.SOLDIER_NUMBER_CHANNEL);
                lumberjackCount = rc.readBroadcast(LUMBERJACK_CHANNEL);
                tankCount = rc.readBroadcast(TANK_CHANNEL);
                
                System.out.println("Scouts: " + scoutCount + ", Soldiers: " + soldierCount + 
                					", Lumberjacks: " + lumberjackCount + ", Tanks: " + tankCount);

                //generates direction of opening after gardeners partially enclose themselves in trees
                dirToOpening = Direction.getEast().rotateLeftDegrees(300);
                
                //generate list of trees that are not full health
                lowHealthTrees = TreeSearch.getNearbyLowTrees();

                //check if there are trees or archons nearby
                nearbyTreesAndArc = Plant.checkNearbyTreesAndArchons(4);
                
                //check if there are nearby allied units
                RobotInfo[] nearbyAllies = rc.senseNearbyRobots(-1,rc.getTeam());
                
                // check for movement to more optimal space
                if (canMove) {
                	if (unitStart < 5) {
                		destination = Chirasou.Disperse(rc.getTeam(),rc.getLocation(), battlecode.common.RobotType.GARDENER.strideRadius);
                		Direction dirToDestination = rc.getLocation().directionTo(destination);
                		if (rc.canMove(dirToDestination)) {
                			 Move.tryMove(dirToDestination);
            			}
                		unitStart++;
                	}
                	else {
	                	destination = Chirasou.Disperse(rc.getTeam(),rc.getLocation(), battlecode.common.RobotType.GARDENER.strideRadius);
	                	// Find optimal location to move
//	                	System.out.println(Clock.getBytecodeNum());
	                	MapLocation destination = Plant.findOptimalSpace(30, (float)rc.getType().sensorRadius-4, (float)rc.getType().sensorRadius-4);
//	                	System.out.println(Clock.getBytecodeNum());
	                	rc.setIndicatorDot(destination, 255, 0, 255);
	                	
	                	// Move to location
	                	Direction dirToDestination = rc.getLocation().directionTo(destination);
	                	if (dirToDestination != null) {
	                		float lengthTo = rc.getLocation().distanceTo(destination);
	                		Move.tryMoveWithDist(dirToDestination, 2, 3, lengthTo);
	                	}
                	}
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
                		buildUnitNew(RobotType.SCOUT, bulletNum);
                    }
                	else if ((rc.getTreeCount() < minTrees1) && (!treeImpossible)) {
                		System.out.println("Trees1");
                    	if (rc.hasTreeBuildRequirements()) {
                    		// To end at 0
                    		Direction plantDirs[] = Plant.scanBuildRadius(5, 1);
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
                    	buildUnitNew(RobotType.SCOUT, bulletNum);
                    }
                	// Toggle with lumberjack
                	else if (soldierCount < minSoldiers1) {
                		System.out.println("Soldiers1");
                    	buildUnitNew(RobotType.SOLDIER, bulletNum);
                    }
                	else {
                		minSat = true;
                	}
                	
                	// End early game
                	//------------------------------------------------
                    if (minSat) {
                    	System.out.println("Satisfication");
                    	Direction plantDirs[] = Plant.scanBuildRadius(5, 1);
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
	
	public static void buildUnitNew(RobotType Unit, float bullets) throws GameActionException {
		if (bullets >= Unit.bulletCost) {
			if (rc.isBuildReady()) {
				Direction plantDirs[] = Plant.scanBuildRadius(5, 1);
				if (plantDirs[1] != null) {
					rc.buildRobot(Unit, plantDirs[1]);
				}
			}
		}		
	}

	
	/*
	public static void completeSurroundTrees(float spacing) throws GameActionException {
		//hexDirArray is an array with 6 Direction objects that point to 0, 60, 120, 180, 240, 300 degrees
		//these are the directions that the gardeners will plant trees in to surround themselves
		
        // plants trees around itself in 6 directions
		if((treeCount < 6) && (rc.hasTreeBuildRequirements())) {
			if (rc.canPlantTree(hexDirArray[0])) {
	        	rc.plantTree(hexDirArray[0]);
	            treeCount++;
	        } else if (rc.canPlantTree(hexDirArray[1])) {
	    		rc.plantTree(hexDirArray[1]);
	            treeCount++;
	        } else if (rc.canPlantTree(hexDirArray[2])) {
	    		rc.plantTree(hexDirArray[2]);
	            treeCount++;
	        } else if (rc.canPlantTree(hexDirArray[3])) {
	    		rc.plantTree(hexDirArray[3]);
	            treeCount++;   
	        } else if (rc.canPlantTree(hexDirArray[4])) {
	    		rc.plantTree(hexDirArray[4]);
	            treeCount++;   
	        } else if (rc.canPlantTree(hexDirArray[5])) {
	    		rc.plantTree(hexDirArray[5]);
	            treeCount++;   
	        }
		}
	}
	
	public static void incompleteSurroundTrees(float spacing) throws GameActionException {
		//hexDirArray is an array with 6 Direction objects that point to 0, 60, 120, 180, 240, 300 degrees
		//these are the directions that the gardeners will plant trees in to surround themselves
		if((treeCount < 6) && (rc.hasTreeBuildRequirements())) {
			// plants trees around itself in 5 directions, leaving one opening
	    	if (rc.canPlantTree(hexDirArray[0])) {
	        	rc.plantTree(hexDirArray[0]);
	            treeCount++;
	        } else if (rc.canPlantTree(hexDirArray[1])) {
	    		rc.plantTree(hexDirArray[1]);
	            treeCount++;
	        } else if (rc.canPlantTree(hexDirArray[2])) {
	    		rc.plantTree(hexDirArray[2]);
	            treeCount++;
	        } else if (rc.canPlantTree(hexDirArray[3])) {
	    		rc.plantTree(hexDirArray[3]);
	            treeCount++;   
	        } else if (rc.canPlantTree(hexDirArray[4])) {
	    		rc.plantTree(hexDirArray[4]);;
	            treeCount++;
	        } else {
	        	buildUnits(dirToOpening);
	        }	
		}
	}*/
	
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