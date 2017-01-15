// AI for Archon
package naclbot;
import battlecode.common.*;
import java.util.ArrayList;

public class ArchonBot extends ArchonVars {
	public static int current_round = 0;

	public static basicTreeInfo dummyTree = new basicTreeInfo(-1, -1, -1, -1);
	public static basicTreeInfo[] dummyTreeInfo = {dummyTree};
	
	public static BinarySearchTree treeList = new BinarySearchTree(dummyTreeInfo);
	
	// Starting game phase
	public static void entry() throws GameActionException {
		System.out.println("Archon initialized!");
		
		// Initialize unit count
		archonVarInit();
		
		rc.broadcast(GARDENER_BUILDER_CHANNEL, 0);
		rc.broadcast(GARDENER_WATERER_CHANNEL, 0);
		
		MapLocation treeLoc = null;
		ArrayList<MapLocation> bulletTreeList = new ArrayList<MapLocation>();


        // Starting phase loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	// Check for condition to exit Starting Phase
            	current_round = rc.getRoundNum();
            	if(current_round > 100) {
            		break;
            	}
            	
            	// Check for all broadcasts
            	MapLocation[] broadcastLocations = rc.senseBroadcastingRobotLocations();
            	ArrayList<MapLocation> broadcastingEnemyUnits = enemyBroadcasts(broadcastLocations);
            	
            	// Generate a random direction
                Direction dir = Move.randomDirection();
            	
            	// Get number of gardeners
            	int prevNumGard = rc.readBroadcast(GARDENER_CHANNEL);
            	//rc.broadcast(GARDENER_CHANNEL, 0);
            	
            	//System.out.println("Number of gardeners: " + prevNumGard);

                // Spam gardeners at random positions if possible
                if (rc.canHireGardener(dir)) {
                    rc.hireGardener(dir);
                    rc.broadcast(GARDENER_CHANNEL, prevNumGard + 1);
                }
                
                // Move to tree (Algorithm is very stupid. Replace with Dijkstra's or something 
                //System.out.println(treeLoc);
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
    		    		//System.out.println("OOR");
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
	
	public static void mainPhase() throws GameActionException {
		System.out.println("Archon transitioning to Main Phase");
		
		boolean hireGard = false;
		
		// loop for Main Phase
        while (true) {
        	
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	current_round = rc.getRoundNum();
            	
            	if (current_round % SCOUT_UPDATE_FREQUENCY == 3){
            		updateTrees(treeList);
            		treeList.printInOrder(treeList.tree_root);
            	}
            	
            	// Check for all broadcasts - EDIT PLEASE GIVE THIS TO SOMEBODY ELSE TO DO.....
            	/*
       
            	MapLocation[] broadcastLocations = rc.senseBroadcastingRobotLocations();
            	ArrayList<MapLocation> broadcastingEnemyUnits = enemyBroadcasts(broadcastLocations);
				*/
            	
                // Generate a random direction
                Direction dir = Move.randomDirection();
                
	            // Get number of gardeners
            	int prevNumGard = rc.readBroadcast(GARDENER_CHANNEL);
            	rc.broadcast(GARDENER_CHANNEL, 0);

	            /* -------------------------------------------------------
	             * ------------ Tree Data Storage and Shit ---------------
	             --------------------------------------------------------*/
            	

            
            	
            	
                // Try to hire gardeners at 150 turn intervals
            	if ((rc.getRoundNum() % 150 == 0)) {
            		hireGard = false;
            	}
                if (rc.canHireGardener(dir) && !hireGard) {
                    rc.hireGardener(dir);
                    rc.broadcast(GARDENER_CHANNEL, prevNumGard + 1);
                    hireGard = true;
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
	
	private static void updateTrees(BinarySearchTree yahallo) throws GameActionException{
		for(int i = 0; i < SCOUT_LIMIT; i++){
			
			if (rc.readBroadcast(10 + SCOUT_CHANNEL + i * SCOUT_MESSAGE_OFFSET) == 3){
				int sent_number = rc.readBroadcast(9 + SCOUT_CHANNEL + i * SCOUT_MESSAGE_OFFSET);
		
				if (sent_number > 0){
					int ID_1 = rc.readBroadcast(1 + SCOUT_CHANNEL + i * SCOUT_MESSAGE_OFFSET);
					
					int x_1 = rc.readBroadcast(2 + SCOUT_CHANNEL + i * SCOUT_MESSAGE_OFFSET);
					int y_1 = rc.readBroadcast(3 + SCOUT_CHANNEL + i * SCOUT_MESSAGE_OFFSET);
					int radius_1 = rc.readBroadcast(4 + SCOUT_CHANNEL + i * SCOUT_MESSAGE_OFFSET);
					basicTreeInfo tree1 = new basicTreeInfo(ID_1, x_1, y_1, radius_1);
					  System.out.println("First ID:  " + ID_1 + " : "  + x_1 + " : "  + y_1 + " : "  + radius_1);
					
					yahallo.insert(tree1, yahallo.tree_root);
				}
				if (sent_number > 1){
					int ID_2 = rc.readBroadcast(5 + SCOUT_CHANNEL + i * SCOUT_MESSAGE_OFFSET);
					int x_2 = rc.readBroadcast(6+ SCOUT_CHANNEL + i * SCOUT_MESSAGE_OFFSET);
					int y_2 = rc.readBroadcast(7 + SCOUT_CHANNEL + i * SCOUT_MESSAGE_OFFSET);
					int radius_2 = rc.readBroadcast(8 + SCOUT_CHANNEL + i * SCOUT_MESSAGE_OFFSET);
					basicTreeInfo tree2 = new basicTreeInfo(ID_2, x_2, y_2, radius_2);					
					
					yahallo.insert(tree2, yahallo.tree_root);
				}
			

			}
		}
	}
}