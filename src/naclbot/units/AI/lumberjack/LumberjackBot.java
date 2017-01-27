// AI for lumber jack

package naclbot.units.AI.lumberjack;
import battlecode.common.*;
import naclbot.units.motion.Move;
import naclbot.units.motion.Todoruno;
import naclbot.units.motion.search.TreeSearch;
import naclbot.units.interact.iFeed;
import naclbot.units.motion.Chirasou;
import naclbot.units.motion.Yuurei;
import naclbot.units.motion.routing.Routing;
import naclbot.variables.BroadcastChannels;
import naclbot.variables.DataVars;
import naclbot.variables.GlobalVars;

import java.util.ArrayList;
import java.util.Arrays;

public class LumberjackBot extends GlobalVars {	
	
	// ------------- GENERAL (IMPORTANT TO SELF) VARS -------------//
	
	// Variable for round number
	private static int roundNumber;
	
	// Variables for self and team recognition
	public static int myID;
	public static boolean iDied = false;
	public static int lumberjackNumber;
	public static int unitNumber;
	private static Team enemy;
	private static Team allies;		
	private static final float strideRadius = battlecode.common.RobotType.LUMBERJACK.strideRadius;
	private static final float bodyRadius = battlecode.common.RobotType.LUMBERJACK.bodyRadius;
	
	// The intial round in which the soldier was constructed
	public static int initRound;
	
	// The total number of soldiers in active service
	private static int currentNumberofLumberjacks;
	
	// Parameters to store locations of self
	public static MapLocation myLocation;	
	
	// Boolean to store whether or not the soldier current has orders to go somewhere....
	private static boolean isCommanded;
	
	private static int trackID;
	
	private static RobotInfo trackedRobot;
	
	// ------------- MOVEMENT VARIABLES -------------//
	
	// Direction at which the soldier traveled last
	private static Direction lastDirection;
	private static MapLocation lastLocation;
	
	// Direction for use each round
	private static Direction myDirection;
    
    // Keep track if it has not moved
    public static int hasNotMoved = 0;
    
    // Location of wanted enemy
    public static Direction enemyDir = null;
    
    // Arraylist to store path for routing....    
//    public static ArrayList<MapLocation> routingPath;
    
    // Maplocation to store a target location when being told to go to a location
    public static MapLocation goalLocation;
    
    // Boolean to store whether or not the robot is tracking something
    public static boolean isTracking;
    
    // Variable to determine after how long soldiers decide that Alan's code is a piece of shit......
    public static final int giveUpOnRouting = 75;
    
    // Variable to store the amount of time currently in routing....
    public static int roundsRouting = 0;
    
    // Variable to determine in which direction the soldier will rotate about a particular robot when tracking it....
    public static boolean rotationDirection;
    
    public static TreeInfo treeToHarvest;
    
    public static int treeID;    
    
    // To store the last known location of a civilian
    public static MapLocation nearestCivilian;  
    
    // Store the maximum distance the lubmerjack is willing to go to get a tree....
    public static final float distanceToSearchTree = battlecode.common.RobotType.LUMBERJACK.sensorRadius;
    
    // Variable to store the number of rounds the lumberjack has been attempting to go to the same tree
    public static int sameTreeRounds;
    
    // variable to store the previous Tree ID of the robot...
    public static int previousTreeID;
    
    
    
	// ------------- TREE VARIABLES ------------------//
	
	// If tree has found a tree with a prize 
    public static boolean foundTree = false;
    
    // Location of tree with a prize 
    public static MapLocation prizeTreeLoc = null;
 
    // Location of regular tree
    public static Direction treeDir = null;

    
    // Function to call at the beginning...
    public static void init() throws GameActionException{
    	
    	// SYSTEM CHECK - See if the lumberjack has initialized...    	
    	System.out.println("I'm an lumberjack!");
        
		// Initialize variables important to self and team recognition
		enemy = rc.getTeam().opponent();
        allies = rc.getTeam();        
        
        roundNumber = rc.getRoundNum();
        initRound = roundNumber;
                
        myID = rc.getID();
        myLocation = rc.getLocation();
        lastLocation = rc.getLocation();
        trackID = -1;
        isTracking = false;
        treeID = -1;
        treeToHarvest = null;
        		
        trackedRobot = null;
        
        // Initialize path list and goal location
//       	routingPath = new ArrayList<MapLocation>();    	
//       	Routing.setRouting(routingPath);
       	
    	// Goal location.....
        goalLocation = null;        		
       	
        // Initialize lumberjack so that it does not have any commands initially;
        isCommanded = false;
        
    	
        // Initialize nearest CIvilian to be the stored location of the archon...
        int archonInitialX = rc.readBroadcast(BroadcastChannels.ARCHON_INITIAL_LOCATION_X) / 100;
        int archonInitialY = rc.readBroadcast(BroadcastChannels.ARCHON_INITIAL_LOCATION_Y) / 100;
        
        nearestCivilian = new MapLocation(archonInitialX, archonInitialY);
        
        // Get own lumberjackNumber - important for broadcasting 
        lumberjackNumber = rc.readBroadcast(BroadcastChannels.LUMBERJACK_NUMBER_CHANNEL);
        currentNumberofLumberjacks = lumberjackNumber + 1;
        
        unitNumber = rc.readBroadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL);
        rc.broadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL, unitNumber + 1);
        
        // Update lumberjack number for other units to see.....
        rc.broadcast(BroadcastChannels.LUMBERJACK_NUMBER_CHANNEL, currentNumberofLumberjacks);

        main();
    }
    
    public static void main() throws GameActionException {
 	
        // Actions to be completed every turn by the lumberjack...
        while (true) {

            try {
         	
            	// Get nearby enemies and allies and bullets for use in other functions            	
            	RobotInfo[] enemyRobots = NearbyUnits(enemy, 6);
            	RobotInfo[] alliedRobots = NearbyUnits(allies, 10);
            	BulletInfo[] nearbyBullets = rc.senseNearbyBullets();
    			
            	// Update the nearest enemy and archon locations
            	BroadcastChannels.broadcastNearestEnemyLocation(enemyRobots, myLocation, unitNumber, nearestCivilian, roundNumber);
            	
            	BroadcastChannels.broadcastEnemyArchonLocations(enemyRobots);        
            	
            	// If the robot was just initialized or did not move last turn, set the last direction to point away from anything....
            	
    			if(lastDirection == null){
    		        RobotInfo nearestAlly = Chirasou.getNearestAlly(alliedRobots, myLocation);
    		        
    		        if(nearestAlly != null){
    		        	lastDirection = new Direction(myLocation.directionTo(nearestAlly.location).radians + (float)Math.PI);
    		        }
    		        else{
    		        	lastDirection = Move.randomDirection();
    		        }
    			}    			  
            	
            	// Check if it did not die, and reset the number of gardeners and units
            	if (iDied) {
            		
            		iDied = false;
            		
            		// Get own lumberjackNumber - important for broadcasting 
                    lumberjackNumber = rc.readBroadcast(BroadcastChannels.LUMBERJACK_NUMBER_CHANNEL);
                    currentNumberofLumberjacks = lumberjackNumber + 1;
                    
                    unitNumber = rc.readBroadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL);
                    rc.broadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL, unitNumber + 1);
                    
                    // Update lumberjack number for other lumberjacks to see.....
                    rc.broadcast(BroadcastChannels.LUMBERJACK_NUMBER_CHANNEL, currentNumberofLumberjacks);

            	}
            	
            	// ------------------------- RESET/UPDATE VARIABLES ----------------//      
            	

            	// Update location of self
            	myLocation = rc.getLocation();      
    			

            	TreeInfo[] nearbyTrees = rc.senseNearbyTrees();
            	RobotInfo nearestAlly = Chirasou.getNearestAlly(alliedRobots, myLocation);
            	
            	// If hasn't moved and there is a nearest ally then calculate direction to move away from ally
            	if (lastDirection == null && nearestAlly != null){            
            		Direction directionToAlly = new Direction(myLocation, nearestAlly.location);
            		lastDirection = new Direction(directionToAlly.radians + (float) (Math.PI));
            		
            	}
            	// If hasn't moved and there are no nearby allies then calculate a random direction
            	else if(lastDirection == null && nearestAlly == null){
            		
            		lastDirection = Move.randomDirection();
            	}
            	
            	// Initialize the direction the robot would like to go to at any given round as the direction the robot moved previously....     	
            	myDirection = lastDirection;
            	
            	// Initialize the location the robot would like to go to as the location it is currently at..
            	MapLocation desiredMove = myLocation.add(lastDirection, strideRadius);
            		
            	// ***************** I AM HERE ****************** //
            	MapLocation newDesiredMove = move(enemyRobots, desiredMove, nearbyTrees);
            	
            	desiredMove = newDesiredMove;
            	
            	// SYSTEM CHECK - Show desired move after path planning
    	    	// System.out.println("desiredMove before post-processing: " + desiredMove.toString());
    	    	
            	// Correct desiredMove to within one soldier  stride location of where the robot is right now....
            	if(myLocation.distanceTo(desiredMove) > strideRadius){
            		
	            	Direction desiredDirection = new Direction(myLocation, desiredMove);	
	            	
	            	desiredMove = myLocation.add(desiredDirection, strideRadius);
            	}
            	
               	// SYSTEM CHECK - Show desired move after path planning
    	    	// System.out.println("desiredMove after rescaling: " + desiredMove.toString());
            	
              	
            	// SYSTEM CHECK Make sure the new desired move is in the correct location LIGHT BLUE DOT
            	// rc.setIndicatorDot(desiredMove, 102, 255, 255);
            	
            	// Check to see if the desired move is out of bounds and make it bounce off of the wall if it is...            	
            	if (!rc.canMove(desiredMove)){
            		MapLocation newLocation = Yuurei.correctOutofBoundsError(desiredMove, myLocation, bodyRadius, strideRadius, rotationDirection);
            		
            		myDirection = new Direction(myLocation, newLocation);
            		
            		desiredMove = newLocation;
            		
            	   	// SYSTEM CHECK - Show desired move after path planning
        	    	// System.out.println("desiredMove after out of bounds correction: " + desiredMove.toString());  
            	}

            	// Check if the initial desired move can be completed and wasn't out of bounds/corrected by the above function
            	if(!rc.canMove(desiredMove)){          		
            	
					MapLocation newLocation = Yuurei.correctAllMove(strideRadius, bodyRadius, rotationDirection, allies, myLocation, desiredMove);
					
					// SYSTEM CHECK See if the robot called the attemptRandom Move function or no....
					// System.out.println("Attempted to find a new location to move to randomly...");
					
					desiredMove = newLocation;

	    	       	// SYSTEM CHECK - Show desired move after path planning
	    	    	// System.out.println("desiredMove after collision correcetion " + desiredMove.toString());
            	}
            	
            	if (desiredMove == null){
            		desiredMove = myLocation;
            	}
              	// See whether or not the robot can move to the completely processed desired move, and move if it does
            	if(rc.canMove(desiredMove)){
            		manageBeingAttacked(desiredMove);
            		rc.move(desiredMove);
            	}
            	// If the robot wasn't able to move....
            	else{
            		// SYSTEM CHECK - Make sure that the robot didn't move because it didn't want to....
            		// System.out.println("This robot did not move because it forgot to show Rem appreciation........");
            	}
          	
            	
            	if(trackID != -1){
            		
            		rc.setIndicatorDot(trackedRobot.location, 128, 0, 0);
            		if (myLocation.distanceTo(trackedRobot.location) <= bodyRadius + 1 + trackedRobot.getRadius()){
            			if(myLocation.distanceTo(nearestAlly.location) >= bodyRadius + 1 + nearestAlly.getRadius()){
            				rc.strike();
            			}
            			
            		}
            	}
            	// Double check that the robot can interact with the tree 
				if(rc.canInteractWithTree(treeID)){
					
					// SYSTEM CHECK - Indicate which tree was just interacted with......
					rc.setIndicatorDot(treeToHarvest.location, 128, 0, 0);
					
					if (treeToHarvest.getContainedBullets() > 0){
						rc.shake(treeID);
						
					}
					else{
						rc.chop(treeID);						
					}			
				}
				
				
				// ------------------  Round End Updates --------------------//
            	
            	// At the end of the turn update the round number
                roundNumber += 1;

                // Make it so that the last direction traveled is the difference between the robot's current and final positions for the round...
                lastLocation =  rc.getLocation();
                lastDirection = new Direction(myLocation, lastLocation);
                
                
                // SYSTEM CHECK  Make sure the robot finishes its turn
                System.out.println("Turn completed.....");
                
                if (!isCommanded){
                	//lastCommanded += 1;
                }
                else{
                	roundsRouting += 1;
                }
                
                // If the previously tracked tree was identical to the currently tracked tree
                if(previousTreeID == treeID && treeID != -1){
                	sameTreeRounds += 1;
                }
                // If not, make the number of rounds previously tracked -
                else{
                	sameTreeRounds = 0;
                }
                 
                previousTreeID = treeID;
				
				// Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
				Clock.yield();
		
            } catch (Exception e) {
		
            	System.out.println("Lumberjack Exception");
            	e.printStackTrace();
            }
	
        }
    }
    
    public static void manageBeingAttacked(MapLocation loc) throws GameActionException{
		boolean beingAttacked = iFeed.willBeAttacked(loc);
		if (beingAttacked) {
			boolean willDie = iFeed.willFeed(loc);
			if (willDie) {
				iDied = true;
				// Get own lumberjackNumber - important for broadcasting 
		        lumberjackNumber = rc.readBroadcast(BroadcastChannels.LUMBERJACK_NUMBER_CHANNEL);
		        currentNumberofLumberjacks = lumberjackNumber - 1;
		        
		        unitNumber = rc.readBroadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL);
		        rc.broadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL, unitNumber - 1);
		        
		        // Update lumberjack number for other units to see.....
		        rc.broadcast(BroadcastChannels.LUMBERJACK_NUMBER_CHANNEL, currentNumberofLumberjacks);

			}
		}
	}
    
    private static MapLocation moveTowardsTree(TreeInfo tree, MapLocation desiredMove){
    	
    	float distanceTo = myLocation.distanceTo(tree.location);
    	if (distanceTo > tree.getRadius() + bodyRadius + 0.5){
    		
    		float random = (float)(Math.random() *  Math.PI/3 - Math.PI/6);
    		Direction newDirection = new Direction(myLocation.directionTo(tree.location).radians + random);
    		
    		float remainingDistance = distanceTo - (tree.getRadius() + bodyRadius + (float) 0.5);
    		
    		return myLocation.add(newDirection, remainingDistance);
    	}
    	
		else{
			Direction directionFrom = tree.location.directionTo(myLocation);
			
			for(int i = 0; i <= 9; i++){
				Direction testDirection1 = new Direction(directionFrom.radians - (float)(i * Math.PI/9));
				MapLocation testLocation1 = tree.location.add(testDirection1, tree.getRadius() + (float) 0.5);
				if (rc.canMove(testLocation1)){
					return testLocation1;
				}
				Direction testDirection2 = new Direction(directionFrom.radians - (float)(i * Math.PI/9));
				MapLocation testLocation2 = tree.location.add(testDirection1, tree.getRadius() + (float) 0.5);
				if (rc.canMove(testLocation2)){
					return testLocation2;
				}		
			}    		
    	}
    	return null;    	
    }
    
    
    
    
	/******************************************************************
	******************* Functions for Movement  ***********************
	*******************************************************************/  
    private static MapLocation move(RobotInfo[] enemyRobots, MapLocation desiredMove, TreeInfo[] nearbyTrees) throws GameActionException{
		// If the robot is currently not tracking anything
    	
		if(trackID == -1){ 
			
			
			// See if a robot to be tracked can be found, allow soldier to track any and all units
			
			// Allow lumberjacks to track scouts for the first 400 turns but not afterwards.....
			if (roundNumber <= 400){
				trackedRobot = Todoruno.getNewEnemyToTrack(enemyRobots, myLocation, true, true, true);
			}
			else{
				trackedRobot = Todoruno.getNewEnemyToTrack(enemyRobots, myLocation, true, false, false);
			}

			
			// SYSTEM CHECK - see if the robot recognizes that it is currently not tracking anything
			// System.out.println("Currently not tracking anything");
			
			// Switch over to the move command after getting a new unit to track.... if the unit is currently being told to go somewhere
			if(isCommanded){    		
		   		// If there is a robot
				if (trackedRobot != null){
					// Update the trackID
					trackID = trackedRobot.ID;
					isTracking = true;
					treeID = -1;
				}        		
				return moveTowardsGoalLocation(enemyRobots, desiredMove, nearbyTrees);
			}
			
			// If there is a robot
			if (trackedRobot != null){
				// Update the trackID
				trackID = trackedRobot.ID;
				isTracking = true;
				treeID = -1;
				
				
				// SYSTEM CHECK - Notify what the robot will now track and set an indicator RED DOT on it
				System.out.println("The soldier has noticed the enemy Robot with ID: " + trackID);
				
				rc.setIndicatorDot(trackedRobot.location, 255, 0, 0);        		
				
				// Call move again with the updated information
				return move(enemyRobots, desiredMove, nearbyTrees);    	
			
			} else{ // If there is no viable enemy to track, just continue harvesting trees.....
    
				System.out.println("Seeing if able to harvest....");
				
				if (treeID != -1){
					
					if (rc.canSenseTree(treeID)){
				
						treeToHarvest = rc.senseTree(treeID);
						System.out.println("Can see the tree, will ateempt to harvest");
						return harvest(treeToHarvest, desiredMove, nearbyTrees);	
					}
					else{			
						treeID = -1;
						treeToHarvest = null;
						return move(enemyRobots, desiredMove, nearbyTrees);	
					}
				}
				else{
					// Get the nearest tree
					
	            	TreeInfo nearestNeutralTree = getNearestNonFriendlyTree(nearbyTrees, myLocation); 
	            	
	            	
	            	System.out.println("Finding a neww tree");
	            	if (nearestNeutralTree != null && roundNumber >= initRound + 2){
	            		System.out.println("Found a neww tree");
	            		treeToHarvest = nearestNeutralTree;
	            		treeID = nearestNeutralTree.ID;
	            		return harvest(nearestNeutralTree, desiredMove, nearbyTrees);
	            	}
	            	else{
	            		System.out.println("Didn't find a new tree");
	            	
	            	
	            		desiredMove = myLocation.add(myDirection, (float) (Math.random() * (strideRadius / 2)  + (strideRadius / 2)));
	            		// SYSTEM Check - Set LIGHT GREY LINE indicating where the soldier would wish to go
		        		rc.setIndicatorLine(myLocation, desiredMove, 110, 110, 110);    
	            		
	            		return desiredMove;
	            		
	            	}	
	        		
				}
          

			}
		// If the robot is actually currently tracking something
		} else{
			
			if (!rc.canSenseRobot(trackID)){
				trackID = -1;
				trackedRobot = null;
				return move(enemyRobots,desiredMove,nearbyTrees);
			}
			
			// If the soldier is currently not commanded to go anywhere... follow the robot in question
			else if(!isCommanded){		
				
				trackedRobot = rc.senseRobot(trackID);
				return moveTowardsTargetX(desiredMove, (float)1);
			}
			else{
				// TODO Insert path planning here........
					return moveTowardsGoalLocation(enemyRobots, desiredMove, nearbyTrees);
				}
			}    			
		}
    
    
    // Function to use when moving towards a certain location with a certain target.....
    private static MapLocation moveTowardsGoalLocation(RobotInfo[] enemyRobots, MapLocation desiredMove, TreeInfo[] nearbyTrees) throws GameActionException{
    	
    	// If it can no longer sense the tracked enemy.....
    	if (trackID != -1 && !rc.canSenseRobot(trackID)){
    		trackID = -1;       
        	isTracking = false;
        	trackedRobot = null;
    	}
    	
    	// If the robot has gotten close enough to the goal location, exit the command phase and do something else
    	if (myLocation.distanceTo(goalLocation) < 2 || roundsRouting >= giveUpOnRouting){
    		
    		// Reset the values necessary for switching into a command phase
    		goalLocation = null;
    		isCommanded = false;
    		
    		// Call the move function again...
    		return move(enemyRobots, desiredMove, nearbyTrees);
    	}
    	else{
    		// SYSTEM CHECK - Inform that the robot is currently attempting to following a route to a goal destination.....
    	
	    	System.out.println("Currently attempting to move to a goal location with x: " + goalLocation.x + " and y: " + goalLocation.y);
	    	
	    	// Otherwise, call the routing wrapper to get a new location to go to...
	    	Routing.routingWrapper();
	    	
	    	// Set the desired Move
	    	desiredMove = Routing.path.get(0);
	    	
	    	// SYSTEM CHECK - Show desired move after path planning
	    	System.out.println("desiredMove before post-processing: " + desiredMove.toString());
	    	
	    	// SYSTEM CHECK Print dot from current location to intended move location - GREY DOT
	    	rc.setIndicatorDot(desiredMove, 105, 105, 105);    
	    	
	    	return desiredMove;
    	}
    } 
    
	private static MapLocation harvest(TreeInfo nearestTree, MapLocation desiredMove, TreeInfo[] nearbyTrees) throws GameActionException{
			
			// SYSTEM CHECK - Inform that the scout has entered harvest mode.............
			// System.out.println("Team bullet total insufficient and nearby bullet tree found - entering harvest mode...............");
			
				
			MapLocation testLocation = moveTowardsTree(nearestTree, desiredMove);
			
			if (testLocation != null){
				desiredMove = testLocation;
			}
			else{
				desiredMove = Yuurei.tryMoveInDirection(Move.randomDirection(), strideRadius, myLocation);
				
			}
			rc.setIndicatorLine(myLocation, desiredMove, 0, 255, 0);;
			return desiredMove;
	}
		

  
	/*
    
    private static Direction manageNeutralTrees() throws GameActionException {
	
        // Assumes that nearbyNeutralTrees is not empty
    	TreeInfo nearestTree = nearbyNeutralTrees.get(0);
    	if (nearestTree.getContainedRobot() != null) {
		    
		    // There is a prize! Chop the tree to get it
		    if (rc.canChop(nearestTree.getLocation())) {
				System.out.println("DIGGING FOR PRIZE");
				rc.chop(nearestTree.getLocation());
		        foundTree = true;
				if (nearestTree.getHealth() <= 5) {
				    foundTree = false;
				    System.out.println("NOT DIGGING FOR PRIZE ANYMORE");
				}
				return null;
		
		    } 
		
    	} else if (nearestTree.getContainedBullets() > 0) {
		
    		// If there are bullets and it can shake then SHAKE IT!
    		if (rc.canShake(nearestTree.getLocation())) {
    			foundTree = true;
				System.out.println("SHAKE IT SHAKE IT");
				rc.shake(nearestTree.getLocation());
				return null;
    		}
    		
    	} else {
		
    		// If it can chop the nearest tree then do it
		    if (rc.canChop(nearestTree.getLocation())) {
		    	foundTree = true;
				rc.chop(nearestTree.getLocation());
				System.out.println("CHOP USELESS TREE");
				return null;
		    } 
    	}
    	
    	foundTree = false;
    	Direction theDir = myLocation.directionTo(nearestTree.getLocation());
    	return theDir;
    }
    */

    /******************************************************************
	******************* Miscellaneous Functions************************
	*******************************************************************/   	
	
	// Function to obtain the robot info units in the specified team
	
	private static RobotInfo[] NearbyUnits(Team team, float distance){
	
		return rc.senseNearbyRobots(myLocation, distance, team);
	}
	
	private static RobotInfo getNearestSoldierorTank(RobotInfo[] enemyRobots){
		
		RobotInfo returnRobot = null;
		
		float distance = Integer.MAX_VALUE;
		for(RobotInfo enemyRobot: enemyRobots){
			if (enemyRobot.type == RobotType.SOLDIER || enemyRobot.type == RobotType.TANK){
				if (myLocation.distanceTo(enemyRobot.location) <= distance){
					returnRobot = enemyRobot;
				}
			}
			
		}
		return returnRobot;
	}

	// Function to return the nearest neutral tree to the lumberjack....
	// Makes sure that the lumberjack can actually get to it.....
	
    private static TreeInfo getNearestNonFriendlyTree(TreeInfo[] nearbyTrees, MapLocation myLocation) throws GameActionException{
    	
    	TreeInfo nearestNearbyTree = null;
    	float distance = Integer.MAX_VALUE;
    	
    	float[] distancesToTrees = new float[20];
    	Arrays.fill(distancesToTrees, -1);
    	
    	float randomNumber = (float) Math.random();
    	
    	for(int i = 0; i < 20; i++){
    		
    		Direction directionToSearch;
    		if (randomNumber >= 0.5){
    			directionToSearch = new Direction ((float)(lastDirection.radians + i * Math.PI/10));
    		}
    		else{
    			directionToSearch = new Direction ((float)(lastDirection.radians - i * Math.PI/10));
    		}    		
    		
    		for (float j = bodyRadius; j <= distanceToSearchTree - 1; j += 1){    			
    			
    			MapLocation locationToCheck =myLocation.add(directionToSearch, j);    			

    			
    			if(distancesToTrees[i] < 0 && rc.isLocationOccupiedByTree(locationToCheck)){
    				
    				TreeInfo treeX = rc.senseTreeAtLocation(locationToCheck);
    				if (treeX.team != allies){    					
	    				
	    				distancesToTrees[i] = treeX.location.distanceTo(myLocation);    
	    				rc.setIndicatorDot(locationToCheck, 255, 255, 0);
    				}
    				else{
    					distancesToTrees[i] = 0;
    					rc.setIndicatorDot(locationToCheck, 255, 0, 255);
    				}
    			}
    			else{
    				rc.setIndicatorDot(locationToCheck, 255, 0, 0);
    			}    			
    		}    		
    	}
    	float minimum = Integer.MAX_VALUE;
    	int directionIndex = -1;
    	for (int i = 0; i < 20; i++){
    		
    		if(distancesToTrees[i] < minimum && distancesToTrees[i] > 0){
    			directionIndex = i;
    			minimum = distancesToTrees[i];
    		}    		
    	}
    	
    	if (directionIndex >= 0){
    	   	Direction directionToRead;
    		if (randomNumber >= 0.5){
				directionToRead = new Direction ((float)(lastDirection.radians + directionIndex * Math.PI/10));
			}
			else{
				directionToRead = new Direction ((float)(lastDirection.radians - directionIndex * Math.PI/10));
			}    		
    		rc.setIndicatorDot(myLocation.add(directionToRead, minimum), 255, 255, 255);
    		
    		TreeInfo tree = rc.senseTreeAtLocation(myLocation.add(directionToRead, minimum));
    		return tree;
    	}
    	return null;
    	
    }

   
    public static MapLocation moveTowardsTargetX(
			
    		
		MapLocation desiredMove, // The location that the robot will attempt to go to				
		
		float multiplier // Multiplier off of the original constants here that the robot will track (useful for tracking big robots)
		
		) throws GameActionException{
	
		rc.setIndicatorLine(myLocation, trackedRobot.location, 128, 0, 128);
		
		// Variable to store the distance to the robot currently being tracked
		float gap = myLocation.distanceTo(trackedRobot.location);
		
		// Get the direction to the target enemy
    	Direction dir = myLocation.directionTo(trackedRobot.location);
    	
    	// If the gap is large enough move directly towards the target
    	if (gap < 2 * multiplier){
    		desiredMove = myLocation.add(dir, (float) strideRadius);
    	}
    	
    	// If the gap is slightly smaller, moves so that the approach is not so direct
    	else if (gap < 4.5 * multiplier){	    		
    		// If the object was set to be rotating counterclockwise, go clockwise
    		if (rotationDirection){	    			
    			// Rotate 20 degrees clockwise
    			Direction newDir = new Direction(dir.radians - (float) (Math.PI/9));
    			
    			// Set new move point
    			desiredMove = myLocation.add(newDir, (float) (strideRadius));
    			
    			// Set rotation direction to be clockwise
    			rotationDirection = false;	    			
    		}
    		else{
    			// Rotate 30 degrees counterclockwise
    			Direction newDir = new Direction(dir.radians + (float) (Math.PI/9));
    			
    			// Set new move point
    			desiredMove = myLocation.add(newDir, (float) (strideRadius));
    			
    			// Set rotation direction to be counterclockwise
    			rotationDirection = true;	    				    			
    		}	    		
    	}
    	else{
    		// If the robot was supposed to be going counterclockwise, continue
    		if (rotationDirection){
    			// Calculate the direction from the target that you want to end up at
    			Direction fromDir = new Direction(dir.radians - (float) (2 * Math.PI/3));
    			
    			// Obtain the desired target location
    			desiredMove = trackedRobot.location.add(fromDir, (float) (5* multiplier));
    			
    		} else{
    			// Calculate the direction from the target that you want to end up at
    			Direction fromDir = new Direction(dir.radians + (float) (2 * Math.PI/3));
    			
    			// Obtain the desired target location
    			desiredMove = trackedRobot.location.add(fromDir, (float) (5 * multiplier));	    			
    		}
    	// SYSTEM CHECK Print line from current location to intended move location - light blue green
    	rc.setIndicatorLine(myLocation, desiredMove, 0, 200, 200);   			
    	} 
    	
    	return desiredMove;	
	} 

}


