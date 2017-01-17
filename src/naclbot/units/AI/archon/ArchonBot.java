// AI for Archon
package naclbot.units.AI.archon;
import battlecode.common.*;
import naclbot.variables.ArchonVars;
import naclbot.variables.DataVars.*;
import naclbot.units.motion.*;
import naclbot.units.motion.search.TreeSearch;

import java.util.ArrayList;
import java.util.Arrays;

public class ArchonBot extends ArchonVars {
	public static int current_round = 0;

	public static final basicTreeInfo dummyTree = new basicTreeInfo(-1, -1, -1, -1);
	public static final basicTreeInfo[] dummyTreeInfo = {dummyTree};	

	public static binarySearchTree treeList = new binarySearchTree(dummyTreeInfo);
	
	public static int archonNumber;
	public static int ID;
	public static Team enemy;
	public static Team allies;
	
	public static Tuple[] archonLocations = new Tuple[5];
	public static int[] archonIDs = new int[5];
	public static int lastAttackArchon;
	
	public static MapLocation treeLoc;
	public static ArrayList<MapLocation> bulletTreeList = new ArrayList<MapLocation>();
	
	public static MapLocation myLocation;
	
	// Starting game phase
	
	public static void init() throws GameActionException {
		System.out.println("Archon initialized!");
		
		// Initialize unit count
		archonVarInit();
		
		rc.broadcast(GARDENER_BUILDER_CHANNEL, 0);
		rc.broadcast(GARDENER_WATERER_CHANNEL, 0);
		

		
		
		// Receive archonNumber from the channel and update
		archonNumber = rc.readBroadcast(ARCHON_CHANNEL);
		rc.broadcast(ARCHON_CHANNEL, archonNumber + 1);
		System.out.println("my Archon number is: " + archonNumber);
		
		ID = rc.getID();
		enemy = rc.getTeam().opponent();
		allies = rc.getTeam();
		
		Arrays.fill(archonIDs, -1);
		lastAttackArchon = 0;
		
		start();		
	}
	
	public static void start() throws GameActionException {		
		
		boolean checkStatus = true;
		
        // Starting phase loop
        while (checkStatus) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	// Check for condition to exit Starting Phase
            	current_round = rc.getRoundNum();
            	if(current_round > 100) {
            		checkStatus = false;
            	}            	
            	// Check for all broadcasts
            	
            	ArrayList<MapLocation> broadcastingEnemyUnits = getEnemyBroadcastLocations();
            	

            	// Generate a random direction
                Direction dir = Move.randomDirection();
            	
                moveToTree();
                
            	// Get number of gardeners
            	int prevNumGard = rc.readBroadcast(GARDENER_CHANNEL);
            	//rc.broadcast(GARDENER_CHANNEL, 0);
            	
            	//System.out.println("Number of gardeners: " + prevNumGard);

                // Spam gardeners at random positions if possible
                if (rc.canHireGardener(dir)) {
                    rc.hireGardener(dir);
                    rc.broadcast(GARDENER_CHANNEL, prevNumGard + 1);
                }               
            		    
                // Broadcast archon's location for other robots on the team to know
    		    broadcastLocation();
                
 
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Archon Exception in Starting Phase");
                e.printStackTrace();
            }
        }
        
        // Move to the mainPhase of operations
        mainPhase();
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
            	// Notify & Create Group
            	if(updateEnemyArchonLocations(archonLocations, archonIDs)){
            		System.out.println("archonIDs updated");  
            		if (lastAttackArchon >= 150){
            			generateCommand(0,archonLocations[0], archonIDs[0]);
                		lastAttackArchon = 0;        
            			
            		}
            	if (lastAttackArchon >= 300){
            		generateCommand(0,archonLocations[0], archonIDs[0]);
            		lastAttackArchon = 0;      
            	}
                        		
            		            		
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
                broadcastLocation();
                lastAttackArchon += 1;
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                
                e.printStackTrace();
            }
        }
		
	}
	
	private static RobotInfo[] senseAlliedUnits(){
		return rc.senseNearbyRobots(-1, allies);		
	}	
	

	private static boolean generateCommand(int kata, Tuple targetLocation, int targetID) throws GameActionException{
		
		// Generates a variety of different group actions
		// kata issues the type of command to be issued
		
		
		// Attack command - gathers nearby armed units to generate a group to attack a target location
		if (kata == 0){
			System.out.println("Archon " + archonNumber + "would like to issue an attack on the target" + targetID + "at location x: " + targetLocation.X + " Y: " + targetLocation.Y);
			
			// to store all members that it wants to call to the group
			int[] groupIDs = new int[GROUP_SIZE_LIMIT];
			int groupCount = 0;
			
			int decidedGroup = -1;
			
			for (int i = 0; i < GROUP_LIMIT; i++){
				if (rc.readBroadcast(GROUP_CHANNEL+i * GROUP_COMMUNICATE_OFFSET) == 0){
					rc.broadcast(GROUP_CHANNEL+i * GROUP_COMMUNICATE_OFFSET, 1);
					rc.broadcast(GROUP_CHANNEL+i * GROUP_COMMUNICATE_OFFSET + 1, rc.getRoundNum());
					
					// Tell which channel the group is in
					rc.broadcast(GROUP_NUMBER_CHANNEL, i);
					decidedGroup = i;			
					
					// Overwrite group if the group is older than 500 turns
				} else if (rc.readBroadcast(GROUP_CHANNEL) + i * GROUP_COMMUNICATE_OFFSET + 1 > rc.getRoundNum() + 500)
					rc.broadcast(GROUP_CHANNEL+i * GROUP_COMMUNICATE_OFFSET, 1);
					rc.broadcast(GROUP_CHANNEL+i * GROUP_COMMUNICATE_OFFSET + 1, rc.getRoundNum());
					
					// Tell which channel the group is in
					rc.broadcast(GROUP_NUMBER_CHANNEL, i);
					decidedGroup = i;		
			}
				// If there is a free group slot
			if (decidedGroup >= 0){
				
			
				RobotInfo[] shounin = senseAlliedUnits();
				for (int j = 0; j < shounin.length; j++){
					if (shounin[j].type == battlecode.common.RobotType.SOLDIER){
						if (groupCount < GROUP_SIZE_LIMIT){
							System.out.println("Archon " + archonNumber + "would like the following soldier to join group " + decidedGroup);;
							System.out.println(shounin[j].ID);
							
							rc.broadcast(GROUP_START + decidedGroup * GROUP_OFFSET + groupCount + 4, shounin[j].ID);
							
							groupIDs[groupCount] = shounin[j].ID;
							groupCount += 1;								
						}
					}						
				}				
			}
			// if a number of soldiers was actually picked
			if(groupCount > 0){
				rc.broadcast(GROUP_START + decidedGroup * GROUP_OFFSET + groupCount + 1, targetID);
				rc.broadcast(GROUP_START + decidedGroup * GROUP_OFFSET + groupCount + 2, (int)targetLocation.X);
				rc.broadcast(GROUP_START + decidedGroup * GROUP_OFFSET + groupCount + 3, (int)targetLocation.Y);
				System.out.println("Group succesfully created");
				
				return true;
				
			}
		}
		return false;
			
	}
	
	
	
	private static void broadcastLocation() throws GameActionException{
		// Regular message
		 myLocation = rc.getLocation();
         rc.broadcast(1 + archonNumber * ARCHON_OFFSET ,(int)myLocation.x);
         rc.broadcast(2 + archonNumber * ARCHON_OFFSET,(int)myLocation.y);
         rc.broadcast(8 + archonNumber * ARCHON_OFFSET, 0);
	}
	
	private static void moveToTree() throws GameActionException{
		
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
	    
	    else {
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
		
	}
	
	
	private static ArrayList<MapLocation> getEnemyBroadcastLocations(){
		
		MapLocation[] broadcastLocations = rc.senseBroadcastingRobotLocations();
    	ArrayList<MapLocation> broadcastingEnemyUnits = enemyBroadcasts(broadcastLocations);
    	return broadcastingEnemyUnits;
	}
	
	private static void updateTrees(binarySearchTree yahallo) throws GameActionException{
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
				System.out.println("received message of OMG");
			}
			
		}
		return coordinates;	
	}
	
	
	private static boolean updateEnemyArchonLocations(Tuple[] archonLocations, int[] archonIDs) throws GameActionException{			
		
		boolean newUpdate = false;
		for(int i = 0; i < SCOUT_LIMIT; i++){			
			if (rc.readBroadcast(10 + SCOUT_CHANNEL + i * SCOUT_MESSAGE_OFFSET) == 5){
				Tuple coords = new Tuple(rc.readBroadcast(6 + SCOUT_CHANNEL + i * SCOUT_MESSAGE_OFFSET), rc.readBroadcast(7 + SCOUT_CHANNEL + i * SCOUT_MESSAGE_OFFSET));
				int foundArchonID = rc.readBroadcast(5 + SCOUT_CHANNEL + i * SCOUT_MESSAGE_OFFSET);
				System.out.println("Recognized that Archon has been found with ID: " + foundArchonID);
				coords.printData();	
				
				int x = arrayContainsIndex(archonIDs, foundArchonID);
				if (x >= 0){
					archonLocations[x] = coords;
				}
				else{
					System.out.println("Oh shit it's a new normie Emilia-loving piece of shit");
					boolean fill = true;
					for(int j =0; j < archonIDs.length; j++){
						if (archonIDs[j] == -1 && fill){
							archonIDs[j] = foundArchonID;
							archonLocations[j] = coords;
							fill = false;
						}
					}
				}
				newUpdate = true;
			}
			
		}
		return newUpdate;
		
	}
}