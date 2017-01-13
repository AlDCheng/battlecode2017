// AI for Archon
package naclbot;
import battlecode.common.*;
import java.util.ArrayList;

public class ArchonBot extends ArchonVars {
	
	// Starting game phase
	public static void entry() throws GameActionException {
		System.out.println("Archon initialized!");
		
		// Initialize unit count
		archonVarInit();
		

		// Initialize enemylocation


		MapLocation treeLoc = null;
		ArrayList<MapLocation> bulletTreeList = new ArrayList<MapLocation>();


        // Starting phase loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	// Check for condition to exit Starting Phase
            	if(rc.getRoundNum() > 100) {
            		break;
            	}
            	
            	// Check for all broadcasts
            	MapLocation[] broadcastLocations = rc.senseBroadcastingRobotLocations();
            	ArrayList<MapLocation> broadcastingEnemyUnits = enemyBroadcasts(broadcastLocations);
            	
            	// Generate a random direction
                Direction dir = Move.randomDirection();
            	
            	// Get number of gardeners
            	int prevNumGard = rc.readBroadcast(GARDENER_CHANNEL);
            	rc.broadcast(GARDENER_CHANNEL, 0);

                // Spam gardeners at random positions if possible
                if (rc.canHireGardener(dir)) {
                    rc.hireGardener(dir);
                    rc.broadcast(GARDENER_CHANNEL, prevNumGard + 1);
                }
                
                // Move to tree (Algorithm is very stupid. Replace with Dijkstra's or something 
                System.out.println(treeLoc);
    		    if (treeLoc != null) {
    		    	try {
    		        	if(rc.isLocationOccupiedByTree(treeLoc)) {
    		        		if(rc.senseTreeAtLocation(treeLoc).containedBullets > 0) {
    		        			Move.tryMove(rc.getLocation().directionTo(treeLoc));
    		                	if(rc.canShake(treeLoc)) {
    		                		rc.shake(treeLoc);
    		                	}
    		        		}
    		        		else {
    		        			treeLoc = null;
    		        		}
    		        	}
    		        	else {
    		        		treeLoc = null;
    		        	}
    		    	} catch (GameActionException e) {
    		    		System.out.println("OOR");
    		    		Move.tryMove(rc.getLocation().directionTo(treeLoc));
    		    	}
    		    }
    		    
    		    if (treeLoc == null) {
    		    	bulletTreeList = TreeSearch.getNearbyBulletTrees();
    		    	if (bulletTreeList.size() > 1) {
    		        	treeLoc = TreeSearch.locNearestTree(bulletTreeList);
    		        	Move.tryMove(rc.getLocation().directionTo(treeLoc));
    		        	if(rc.canShake(treeLoc)) {
    		        		rc.shake(treeLoc);
    		        	}
    		    	}
    		    	else {
    		        	// Move randomly
    		            Move.tryMove(Move.randomDirection());
    		        }
    		    }

                // Broadcast archon's location for other robots on the team to know
                MapLocation myLocation = rc.getLocation();
                rc.broadcast(0,(int)myLocation.x);
                rc.broadcast(1,(int)myLocation.y);
                
 
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Archon Exception in Starting Phase");
                e.printStackTrace();
            }
        }
    }
	
	static void mainPhase() throws GameActionException {
		System.out.println("Archon transitioning to Main Phase");
		
		// loop for Main Phase
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	
            	if(rc.getRoundNum() % 500 == 0) {
            		int x = rc.readBroadcast(SCOUT_CHANNEL);
            		if(x>0){
            			rc.broadcast(SCOUT_CHANNEL, x-1);
            		}
            	}
            	// Check for all broadcasts
            	MapLocation[] broadcastLocations = rc.senseBroadcastingRobotLocations();
            	ArrayList<MapLocation> broadcastingEnemyUnits = enemyBroadcasts(broadcastLocations);

                // Generate a random direction
                Direction dir = Move.randomDirection();
                
	            // Get number of gardeners
            	int prevNumGard = rc.readBroadcast(GARDENER_CHANNEL);
            	rc.broadcast(GARDENER_CHANNEL, 0);


                // Randomly attempt to build a gardener in this direction
                if (rc.canHireGardener(dir) && Math.random() < .01) {
                    rc.hireGardener(dir);
                    rc.broadcast(GARDENER_CHANNEL, prevNumGard + 1);
                }

                // Move randomly
                Move.tryMove(Move.randomDirection());

                // Broadcast archon's location for other robots on the team to know
                MapLocation myLocation = rc.getLocation();
                rc.broadcast(0,(int)myLocation.x);
                rc.broadcast(1,(int)myLocation.y);

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Archon Exception in Main Phase");
                e.printStackTrace();
            }
        }
		
	}
}