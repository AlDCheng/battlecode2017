// AI for Archon
package naclbot.units.AI.archon;
import battlecode.common.*;



import naclbot.variables.*;
import naclbot.units.motion.*;
import naclbot.units.motion.search.TreeSearch;

import java.util.ArrayList;
import java.util.Arrays;

public class ArchonBot extends ArchonVars {
	public static int current_round = 0;

	public static basicTreeInfo dummyTree = new basicTreeInfo(-1, -1, -1, -1);
	public static basicTreeInfo[] dummyTreeInfo = {dummyTree};
	
	public static BinarySearchTree treeList = new BinarySearchTree(dummyTreeInfo);
	
	public static int archonNumber;
	public static int ID;
	public static Team enemy;
	
	public static Tuple[] archonLocations = new Tuple[3];
	public static int[] archonIDs = new int[3];
	
	// Starting game phase
	public static void entry() throws GameActionException {
		
		System.out.println("Archon initialized!");
		
		// Initialize unit count
		archonVarInit();
		
		rc.broadcast(GARDENER_BUILDER_CHANNEL, 0);
		rc.broadcast(GARDENER_WATERER_CHANNEL, 0);
		
		MapLocation treeLoc = null;
		ArrayList<MapLocation> bulletTreeList = new ArrayList<MapLocation>();
		
		// Receive archonNumber from the channel and update
		archonNumber = rc.readBroadcast(ARCHON_CHANNEL);
		rc.broadcast(ARCHON_CHANNEL, archonNumber + 1);
		
		ID = rc.getID();
		enemy = rc.getTeam().opponent();
		
		Arrays.fill(archonIDs, -1);
		
		
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
            	}
            	
            	detectEnemyGroup();
            	updateEnemyArchonLocations(archonLocations, archonIDs);
            	
            	
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
	
	// FUnction to gain information from scouts regarding the location of trees.
	
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
	private static Tuple[] detectEnemyGroup() throws GameActionException{
		
		Tuple[] coordinates = new Tuple[SCOUT_LIMIT];
		
		for(int i = 0; i < SCOUT_LIMIT; i++){			
			if (rc.readBroadcast(10 + SCOUT_CHANNEL + i * SCOUT_MESSAGE_OFFSET) == 2){
				Tuple coords = new Tuple(rc.readBroadcast(3 + SCOUT_CHANNEL + i * SCOUT_MESSAGE_OFFSET), rc.readBroadcast(4 + SCOUT_CHANNEL + i * SCOUT_MESSAGE_OFFSET));
				coords.printData();	
				coordinates[i] = coords;
			}
			
		}
		return coordinates;	
	}
	private static void updateEnemyArchonLocations(Tuple[] archonLocations, int[] archonIDs) throws GameActionException{			
		
		for(int i = 0; i < SCOUT_LIMIT; i++){			
			if (rc.readBroadcast(10 + SCOUT_CHANNEL + i * SCOUT_MESSAGE_OFFSET) == 6){
				Tuple coords = new Tuple(rc.readBroadcast(6 + SCOUT_CHANNEL + i * SCOUT_MESSAGE_OFFSET), rc.readBroadcast(47 + SCOUT_CHANNEL + i * SCOUT_MESSAGE_OFFSET));
				int foundArchonID = rc.readBroadcast(5 + SCOUT_CHANNEL + i * SCOUT_MESSAGE_OFFSET);
				System.out.println("Recognized that Archon has been found with ID: " + foundArchonID);
				coords.printData();	
				
				int x = arrayContainsIndex(archonIDs, foundArchonID);
				if (x > 0){
					archonLocations[x] = coords;
				}
				else{
					boolean fill = true;
					for(int j =0; j < archonIDs.length; j++){
						if (archonIDs[j] == -1 && fill){
							archonIDs[j] = foundArchonID;
							archonLocations[j] = coords;
							fill = false;
						}
					}
				}
			}
			
		}
		
	}
}