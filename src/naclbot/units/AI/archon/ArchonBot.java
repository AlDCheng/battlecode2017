// AI for Archon
package naclbot.units.AI.archon;
import battlecode.common.*;

import naclbot.variables.GlobalVars;
import naclbot.variables.DataVars;
import naclbot.variables.DataVars.*;

import naclbot.variables.BroadcastChannels;
import naclbot.units.interact.iFeed;
import naclbot.units.motion.*;
import naclbot.units.motion.search.TreeSearch;

import java.util.ArrayList;
import java.util.Arrays;

//~~ Updated by Illiyia~~

public class ArchonBot extends GlobalVars {
	
	// Variable for storing the current round of the game
	public static int currentRound = 0;
	
	// Variables important to self and team recognition
	public static int archonNumber;
	public static int ID;
	private static Team enemy;
	private static Team allies;	
	private static final float strideRadius = battlecode.common.RobotType.ARCHON.strideRadius;
	private static final float bodyRadius = battlecode.common.RobotType.ARCHON.bodyRadius;

	// Variable storing number of additional victory points needed to win
	public static float victoryPointsToWin;
	// Locations to store data on enemy archons....
	public static Tuple[] archonLocations = new Tuple[5];
	public static int[] archonIDs = new int[5];
	
	// Parameters to store location of self (at beginning of round)
	public static MapLocation myLocation;

	// Direction at which the archon traveled last
	private static Direction lastDirection;
	private static MapLocation lastPosition;
	
	// Direction for use each round
	private static Direction myDirection;
	
	
	
	// ------ TO BE CHANGED ------ //
	
	// List of bullet trees
	public static MapLocation treeLoc;
	public static ArrayList<MapLocation> bulletTreeList = new ArrayList<MapLocation>();
	
	
	public static int numBroadcasted;
	public static int currentTreeSize; 
	

	// dummyTree variable for initializing the BST of tree information as well as BST initialization
	public static final basicTreeInfo dummyTree = new basicTreeInfo(-1, -1, -1, -1);
	public static final basicTreeInfo[] dummyTreeInfo = {dummyTree};
	public static binarySearchTree treeList = new binarySearchTree(dummyTreeInfo);
	
	public static int lastCount;
	
	public static int[] givenIDs = new int [TOTAL_TREE_NUMBER];
	
	public static int lastAttackArchon;
	
	// ------ TO BE CHANGED ------ //
	
	
	// Starting game phase
	
	public static void init() throws GameActionException {
		
		// SYSTEM CHECK Initialization start check
		System.out.println("Archon initialized!");
		
		// Initialize unit count		
		rc.broadcast(GARDENER_BUILDER_CHANNEL, 0);
		rc.broadcast(GARDENER_WATERER_CHANNEL, 0);		
		
		// Receive archonNumber from the channel and update
		archonNumber = rc.readBroadcast(BroadcastChannels.ARCHON_NUMBER_CHANNEL);
		rc.broadcast(BroadcastChannels.ARCHON_NUMBER_CHANNEL, archonNumber + 1);
		
		// SYSTEM CHECK Make sure that the archon correctly identifies its number
		System.out.println("my Archon number is: " + archonNumber);
		
		ID = rc.getID();
		enemy = rc.getTeam().opponent();
		allies = rc.getTeam();
		
		Arrays.fill(archonIDs, -1);
		lastAttackArchon = Integer.MIN_VALUE;
		
		numBroadcasted = 0;		
		start();		
	}
	
	public static void start() throws GameActionException {		
		
		boolean checkStatus = true;
		
        // Starting phase loop
        while (checkStatus) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	
    		    rc.broadcast(TREES_SENT_THIS_TURN, 0);
		        // Broadcast archon's location for other robots on the team to know
			    broadcastLocation();
            	// Check for condition to exit Starting Phase
            	currentRound = rc.getRoundNum();
            	if(currentRound > 100) {
            		checkStatus = false;
            	}            	
          	

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
		// System.out.println("Archon transitioning to Main Phase");
		
		boolean hireGard = false;
		
		// loop for Main Phase
        while (true) {
        	
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	rc.broadcast(TREES_SENT_THIS_TURN, 0);
            	
            	//get current round number
            	currentRound = rc.getRoundNum();
            	
            	//get needed victory points to win
            	victoryPointsToWin = (float) GameConstants.VICTORY_POINTS_TO_WIN - rc.getTeamVictoryPoints();
            	
            	// if enough bullets; win
            	float currentVictoryPointCost = rc.getVictoryPointCost();
            	if (rc.getTeamBullets() >= victoryPointsToWin*currentVictoryPointCost) {
            		rc.donate(rc.getTeamBullets());
            	} /*else if (rc.getTeamBullets() >= currentVictoryPointCost && currentRound >= 700 && currentRound % 2 == 0) {
            		System.out.println(currentVictoryPointCost);
            		rc.donate(currentVictoryPointCost);
            	}*/
            	
            	lastCount = -1;
            	
            	currentTreeSize = treeList.size;
            	
            	currentRound = rc.getRoundNum();
            	
                broadcastLocation();
            	
            	if (currentRound % SCOUT_UPDATE_FREQUENCY == 3){
            		binarySearchTree.archonUpdateTrees(treeList);  
					            		
            		for (int i = 0; i < DataVars.treeMapFormat.size(); i++) {
    					System.out.println("* " + Arrays.toString(DataVars.treeMapFormat.get(i)));
    				}
            		if(DataVars.treeMapFormat.size() > 1) {
    					updateMapTrees(DataVars.treeMapFormat);
    					//System.out.println("end treeAdd");
    				}
            
            	}
            	
            	detectEnemyGroup();
            	// Notify & Create Group
            	if(updateEnemyArchonLocations(archonLocations, archonIDs)){
            		// System.out.println("archonIDs updated");  
            		if (lastAttackArchon >= 50){
            			boolean made = generateCommand(1,archonLocations[0], archonIDs[0]);
            			if (made){
            				lastAttackArchon = 0;        
            			}
            		}
	            	if (lastAttackArchon >= 100){
	            		boolean made = generateCommand(1,archonLocations[0], archonIDs[0]);
	            		if (made){
	        				lastAttackArchon = 0;        
	        			}   
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
	          	                        	            	
                // Try to hire gardeners at 150 turn intervals
            	if ((rc.getRoundNum() % 50 == 0) || (prevNumGard < 3)) {
            		hireGard = false;
            	}
                if (rc.canHireGardener(dir) && !hireGard && prevNumGard < 15) {
                    rc.hireGardener(dir);
                    rc.broadcast(GARDENER_CHANNEL, prevNumGard + 1);
                    hireGard = true;
                }

                // Move randomly
                MapLocation newLoc = Yuurei.tryMoveInDirection(dir, strideRadius, myLocation);
                if (newLoc != null) {
                	rc.move(newLoc);
                	iFeed.willFeed(newLoc);
                }

                // Broadcast archon's location for other robots on the team to know
                
        		if (archonNumber == 0){
        	        
    				if(numBroadcasted<treeList.size-1){
    					
    					Node toSend1 = getUnsentTerm(treeList.tree_root);
    					
        				if (toSend1 != null){
        					
        					givenIDs[numBroadcasted] = toSend1.data.ID;
        					
		                    rc.broadcast(8 + archonNumber * ARCHON_OFFSET, 33);
		                    rc.broadcast(7 + archonNumber * ARCHON_OFFSET, numBroadcasted+1);
		                    		                  
		                    rc.broadcast(TREE_DATA_CHANNEL, numBroadcasted);
		                    
		                    rc.broadcast(1 + TREE_DATA_CHANNEL +  (numBroadcasted % TOTAL_TREE_NUMBER) * TREE_OFFSET, toSend1.data.x);
		                    rc.broadcast(2 + TREE_DATA_CHANNEL +  (numBroadcasted % TOTAL_TREE_NUMBER) * TREE_OFFSET, toSend1.data.y);
		                    rc.broadcast(3 + TREE_DATA_CHANNEL +  (numBroadcasted % TOTAL_TREE_NUMBER) * TREE_OFFSET, toSend1.data.radius);
		                    rc.broadcast(4 + TREE_DATA_CHANNEL +  (numBroadcasted % TOTAL_TREE_NUMBER) * TREE_OFFSET, toSend1.data.ID);
		                    numBroadcasted += 1;		                    
        					}
    				}		
    	
            	}
            
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
		if (kata == 1){
			System.out.println("Archon " + archonNumber + "would like to issue an attack on the target" + targetID + "at location x: " + targetLocation.X + " Y: " + targetLocation.Y);
			
			// Test path planning
//			MapLocation targetLocationML = new MapLocation(targetLocation.X, targetLocation.Y);
//			ArrayList<MapLocation> path = PathPlanning.findPath(rc.getLocation(), targetLocationML);
//			System.out.println(path);
			
			// to store all members that it wants to call to the group
			int[] groupIDs = new int[GROUP_SIZE_LIMIT];
			int groupCount = 0;
			
			int decidedGroup = -1;
			boolean decided = false;
			
			for (int i = 0; i < GROUP_LIMIT; i++){
			
				if (rc.readBroadcast(GROUP_CHANNEL+ i * GROUP_COMMUNICATE_OFFSET) == 0 && !decided){
					rc.broadcast(GROUP_CHANNEL+i * GROUP_COMMUNICATE_OFFSET, 1);
					rc.broadcast(GROUP_CHANNEL+i * GROUP_COMMUNICATE_OFFSET + 1, rc.getRoundNum());
					
					// Tell which channel the group is in
					rc.broadcast(GROUP_NUMBER_CHANNEL, i);
					decidedGroup = i;
					rc.broadcast(3 + archonNumber * ARCHON_OFFSET, 1);
			        rc.broadcast(8 + archonNumber * ARCHON_OFFSET, 1);
			        decided = true;
					
					// Overwrite group if the group is older than 500 turns
				} else if (rc.readBroadcast(GROUP_CHANNEL) + i * GROUP_COMMUNICATE_OFFSET + 1 > rc.getRoundNum() + 500 && !decided){
					rc.broadcast(GROUP_CHANNEL+i * GROUP_COMMUNICATE_OFFSET, 1);
					rc.broadcast(GROUP_CHANNEL+i * GROUP_COMMUNICATE_OFFSET + 1, rc.getRoundNum());
					
					rc.broadcast(3 + archonNumber * ARCHON_OFFSET, 1);
			        rc.broadcast(8 + archonNumber * ARCHON_OFFSET, 1);
					
					// Tell which channel the group is in
					rc.broadcast(GROUP_NUMBER_CHANNEL, i);
					decidedGroup = i;	
					decided = true;
				} else{
					
					//System.out.println("The following group is already occupied: " + i);
						
				}
			}
			
				// If there is a free group slot
			if (decidedGroup >= 0){
				
			
				RobotInfo[] shounin = senseAlliedUnits();
				for (int j = 0; j < shounin.length; j++){
					if (shounin[j].type == battlecode.common.RobotType.SOLDIER || shounin[j].type == battlecode.common.RobotType.TANK || shounin[j].type == battlecode.common.RobotType.LUMBERJACK){
						if (groupCount < GROUP_SIZE_LIMIT){
							//System.out.println("Archon " + archonNumber + "would like the following unit to join group " + decidedGroup);;
							// System.out.println(shounin[j].ID);
							
							rc.broadcast(GROUP_START + decidedGroup * GROUP_OFFSET + groupCount + 5, shounin[j].ID);
							
							groupIDs[groupCount] = shounin[j].ID;
							groupCount += 1;								
						}
					}						
				}				
			}
			// if a number of soldiers was actually picked
			if(groupCount > 0){
				rc.broadcast(GROUP_START + decidedGroup * GROUP_OFFSET + 1, kata);
				rc.broadcast(GROUP_START + decidedGroup * GROUP_OFFSET + 2, targetID);
				rc.broadcast(GROUP_START + decidedGroup * GROUP_OFFSET + 3, (int)targetLocation.X);
				rc.broadcast(GROUP_START + decidedGroup * GROUP_OFFSET + 4, (int)targetLocation.Y);
				//System.out.println("Group creation succesfully initialized");
				
				return true;
				
			}
			
			else{
				// System.out.println("Group creation was unsuccesful - no nearby units to join group");
				
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
	
	private static void moveAwayFromGardeners() throws GameActionException {
		
		
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


	
	private static Tuple[] detectEnemyGroup() throws GameActionException{
		
		Tuple[] coordinates = new Tuple[SCOUT_LIMIT];
	
		
		for(int i = 0; i < SCOUT_LIMIT; i++){			
			if (rc.readBroadcast(10 + SCOUT_CHANNEL + i * SCOUT_MESSAGE_OFFSET) == 2){
				Tuple coords = new Tuple(rc.readBroadcast(1 + SCOUT_CHANNEL + i * SCOUT_MESSAGE_OFFSET), rc.readBroadcast(2 + SCOUT_CHANNEL + i * SCOUT_MESSAGE_OFFSET));
		
				coordinates[i] = coords;
			
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
				//System.out.println("Recognized that Archon has been found with ID: " + foundArchonID);
				coords.printData();	
				
				// Test path planning
//				System.out.println("Start PP");
//				MapLocation targetLocationML = new MapLocation(coords.X, coords.Y);
//				System.out.println("Start (real): " + rc.getLocation() + ", End (real): " + targetLocationML);
//				ArrayList<MapLocation> path = PathPlanning.findPath(rc.getLocation(), targetLocationML);
//				System.out.println(path);
//				System.out.println("End PP");
				
				int x = arrayContainsIndex(archonIDs, foundArchonID);
				if (x >= 0){
					archonLocations[x] = coords;
				}
				else{
					System.out.println("Oh shit it's a new normie piece of shit ---- I bet it thinks Emilia is best girl holy fuck im triggered");
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
	
	public static Node getTerm(Node root, int index){
		
		Node desiredNode = null;
		if (root!=null){
			Node x = getTerm(root.leftChild, index);
			if (x!=null){
				desiredNode = x;	    	
    		}
    		//System.out.print("Node: " + root.key + "Data_x: " + root.data.x + "Data_y: " + root.data.y + "Radius: " + root.data.radius);
    		if (lastCount == index){
    			desiredNode = root;
    		}
    		lastCount += 1;
    		
	        //System.out.println();
    		
    		Node y = getTerm(root.rightChild, index);
			if (y!=null){
				desiredNode = y;	    	
    		}
    	}
		return desiredNode;
		
	}
	
	public static Node getUnsentTerm(Node root){
			
		
			if (root!=null){
				Node x = getUnsentTerm(root.leftChild);
				if (x!=null){
					return x;   	
	    		}
	    		//System.out.print("Node: " + root.key + "Data_x: " + root.data.x + "Data_y: " + root.data.y + "Radius: " + root.data.radius);
	    		if (!arrayContainsInt (givenIDs, root.data.ID)){
	    			return root;
	    		}	    		
		        //System.out.println();
	    		
	    		Node y = getUnsentTerm(root.rightChild);
				if (y!=null){
					return y;    	
	    		}
	    	}
			return null;
		}


}