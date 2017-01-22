// AI for gardener under normal control
package naclbot.units.AI.gardener;
import battlecode.common.*;
import naclbot.units.motion.Move;
import naclbot.units.motion.search.TreeSearch;
import naclbot.variables.GlobalVars;
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
	
	public static ArrayList<MapLocation> lowHealthTrees;
	
	public static boolean nearbyTreesAndArc;
	
	
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
                xPos = rc.readBroadcast(0);
                yPos = rc.readBroadcast(1);                
                archonLoc = new MapLocation(xPos,yPos);
                
                // Check number of all units currently in service
                scoutCount = rc.readBroadcast(SCOUT_CHANNEL);                
                soldierCount = rc.readBroadcast(SOLDIER_CHANNEL);
                lumberjackCount = rc.readBroadcast(LUMBERJACK_CHANNEL);
                tankCount = rc.readBroadcast(TANK_CHANNEL);

                //generates direction of opening after gardeners partially enclose themselves in trees
                dirToOpening = Direction.getEast().rotateLeftDegrees(300);
                
                //generate list of trees that are not full health
                lowHealthTrees = TreeSearch.getNearbyLowTrees();

                //check if there are trees or archons nearby
                nearbyTreesAndArc = Plant.checkNearbyTreesAndArchons(6);
                
                //default motion
                if (canMove) {
                	if (Chirasou.Disperse(rc.getTeam(),rc.getLocation()) != null) {
                		destination = Chirasou.Disperse(rc.getTeam(),rc.getLocation());
                		Move.tryMove(rc.getLocation().directionTo(destination));
                	}
                }

                //ROLE DESIGNATION
                if (lowHealthTrees.size() > 0) {
                	role = 0;
                } else if (!nearbyTreesAndArc || treeCount > 0) {
                	//if there are no trees nearby role = 0, calls incompleteSurroundTrees()
                	role = 1;
                	canMove = false;
                } else {
                	//if no trees need to be watered, role = 1, calls on buildUnits()
                	role = 2;
                }
                
                //ROLE EXECUTION
                if (role == 0) {
                	waterSurroundingTrees();
                } else if (role == 1) {
                	incompleteSurroundTrees(6);
                } else if (role == 2) {
                   	buildUnits(dirToOpening);
                } else if (role == 3) {
                	//not yet implemented
                	waterSurroundingTrees();
                	completeSurroundTrees(6);
                }
                
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();
            } catch (Exception e) {
                System.out.println("Gardener Exception");
                e.printStackTrace();
            }
        }
	}
	
	
	public static void completeSurroundTrees(float spacing) throws GameActionException {
		
        // plants trees around itself in 6 directions
        if (rc.canPlantTree(hexDirArray[0]) && rc.hasTreeBuildRequirements() && treeCount < 6) {
        	rc.plantTree(hexDirArray[0]);
        	plantedTrees.add(rc.getLocation().add(hexDirArray[0],1+GameConstants.BULLET_TREE_RADIUS));
            treeCount++;
        } else if (rc.canPlantTree(hexDirArray[1]) && rc.hasTreeBuildRequirements() && treeCount < 6) {
    		rc.plantTree(hexDirArray[1]);
    		plantedTrees.add(rc.getLocation().add(hexDirArray[1],1+GameConstants.BULLET_TREE_RADIUS));
            treeCount++;
        } else if (rc.canPlantTree(hexDirArray[2]) && rc.hasTreeBuildRequirements() && treeCount < 6) {
    		rc.plantTree(hexDirArray[2]);
    		plantedTrees.add(rc.getLocation().add(hexDirArray[2],1+GameConstants.BULLET_TREE_RADIUS));
            treeCount++;
        } else if (rc.canPlantTree(hexDirArray[3]) && rc.hasTreeBuildRequirements() && treeCount < 6) {
    		rc.plantTree(hexDirArray[3]);
    		plantedTrees.add(rc.getLocation().add(hexDirArray[3],1+GameConstants.BULLET_TREE_RADIUS));
            treeCount++;   
        } else if (rc.canPlantTree(hexDirArray[4]) && rc.hasTreeBuildRequirements() && treeCount < 6) {
    		rc.plantTree(hexDirArray[4]);
    		plantedTrees.add(rc.getLocation().add(hexDirArray[4],1+GameConstants.BULLET_TREE_RADIUS));
            treeCount++;   
        } else if (rc.canPlantTree(hexDirArray[5]) && rc.hasTreeBuildRequirements() && treeCount < 6) {
    		rc.plantTree(hexDirArray[5]);
    		plantedTrees.add(rc.getLocation().add(hexDirArray[5],1+GameConstants.BULLET_TREE_RADIUS));
            treeCount++;   
        }
	}
	
	public static void incompleteSurroundTrees(float spacing) throws GameActionException {
        
        // plants trees around itself in 5 directions, leaving one opening
    	if (rc.canPlantTree(Direction.getEast()) && rc.hasTreeBuildRequirements() && treeCount < 6) {
        	rc.plantTree(Direction.getEast());
        	plantedTrees.add(rc.getLocation().add(hexDirArray[0],1+GameConstants.BULLET_TREE_RADIUS));
            treeCount++;
        } else if (rc.canPlantTree(Direction.getEast().rotateLeftDegrees(60)) && rc.hasTreeBuildRequirements() && treeCount < 6) {
    		rc.plantTree(Direction.getEast().rotateLeftDegrees(60));
    		plantedTrees.add(rc.getLocation().add(hexDirArray[1],1+GameConstants.BULLET_TREE_RADIUS));
            treeCount++;
        } else if (rc.canPlantTree(Direction.getEast().rotateLeftDegrees(120)) && rc.hasTreeBuildRequirements() && treeCount < 6) {
    		rc.plantTree(Direction.getEast().rotateLeftDegrees(120));
    		plantedTrees.add(rc.getLocation().add(hexDirArray[2],1+GameConstants.BULLET_TREE_RADIUS));
            treeCount++;
        } else if (rc.canPlantTree(Direction.getEast().rotateLeftDegrees(180)) && rc.hasTreeBuildRequirements() && treeCount < 6) {
    		rc.plantTree(Direction.getEast().rotateLeftDegrees(180));
    		plantedTrees.add(rc.getLocation().add(hexDirArray[3],1+GameConstants.BULLET_TREE_RADIUS));
            treeCount++;   
        } else if (rc.canPlantTree(Direction.getEast().rotateLeftDegrees(240)) && rc.hasTreeBuildRequirements() && treeCount < 6) {
    		rc.plantTree(Direction.getEast().rotateLeftDegrees(240));
    		plantedTrees.add(rc.getLocation().add(hexDirArray[4],1+GameConstants.BULLET_TREE_RADIUS));
            treeCount++;
        } else {
        	buildUnits(dirToOpening);
        }
	}
	
	public static void waterSurroundingTrees() throws GameActionException {
		if (lowHealthTrees.size() > 0) {
            if (rc.canWater(lowHealthTrees.get(0))) {
            	rc.water(lowHealthTrees.get(0));
            }
        } 
	}
	
	public static void buildUnits(Direction dirToBuild) throws GameActionException {
		//default moving status
	    //Move.tryMove(Move.randomDirection());
	    
	    //try to build SCOUT, make sure not over SCOUT_LIMIT
	    if (rc.canBuildRobot(RobotType.SCOUT, dirToBuild) && rc.isBuildReady() && scoutCount < SCOUT_LIMIT) {
	        rc.buildRobot(RobotType.SCOUT, dirToBuild);
	        rc.broadcast(SCOUT_CHANNEL, scoutCount+1);
	    }
	    //try to build SOLDIER, make sure soldierCount:lumberjackCount < LUMBERJACK_RATIO
	    else if (rc.canBuildRobot(RobotType.SOLDIER, dirToBuild) && rc.isBuildReady() && soldierCount <= LUMBERJACK_RATIO*lumberjackCount  && lumberjackCount >= START_LUMBERJACK_COUNT) {
	        rc.buildRobot(RobotType.SOLDIER, dirToBuild);
	        rc.broadcast(SOLDIER_CHANNEL, soldierCount+1);
	    } 
	    //try to build LUMBERJACK, "
	    else if (rc.canBuildRobot(RobotType.LUMBERJACK, dirToBuild) && rc.isBuildReady() && (LUMBERJACK_RATIO*lumberjackCount < soldierCount || lumberjackCount < START_LUMBERJACK_COUNT)) {
	        rc.buildRobot(RobotType.LUMBERJACK, dirToBuild);
	        rc.broadcast(LUMBERJACK_CHANNEL, lumberjackCount+1);
	    }
	    //try to build TANK, make sure soldierCount:tankCount < TANK_RATIO
	    /*
	    if (rc.canBuildRobot(RobotType.TANK, dir) && rc.isBuildReady() && TANK_RATIO*tankCount < soldierCount) {
	        rc.buildRobot(RobotType.TANK, dir);
	        rc.broadcast(TANK_CHANNEL, tankCount+1);
	    } 
	    */
	        
	}
	
}