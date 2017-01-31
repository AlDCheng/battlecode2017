package naclbot.units.AI.scout;

import java.util.ArrayList;
import java.util.Arrays;
import battlecode.common.*;
import naclbot.units.motion.Yuurei;
import naclbot.units.motion.search.EnemyArchonSearch;
import naclbot.units.motion.shoot.Korosenai;
import naclbot.units.interact.iFeed;
import naclbot.units.motion.Chirasou;
import naclbot.units.motion.Todoruno;
import naclbot.units.motion.Todoruno.Rotation;
import naclbot.variables.GlobalVars;
import naclbot.variables.BroadcastChannels;	

/* --------------------------   Overview  -------------------------------
 * 
 * 		AI Controlling the functions of a subclass of scouts that
 * 		scout out the enemy and report early game unit statistics
 *
 *			     ~~ Coded by Illiyia (akimn@#mit.edu)
 *
 *			 Call the init() function to use the file...
 * 
 * 		  Note: Debug statements all begin with SYSTEM CHECK 
 * 
 * 		  Note: These scouts are called RemBot because Rem is the
 * 				BEST best girl there is.. PERIOD.
 * 
 ------------------------------------------------------------------------ */

// Remember.... the first scout gets its own special java file precisely because it is called RemBot!
// Anyone who sincerely doubts that Rem is best girl can look somewhere else - this code is only for those who are true believers.........

public class RemBot extends BestGirlBot {	
	
	// ----------------------------------------------------------------------------------//
	// ------------------------- VARIABLES SPECIFIC TO REMBOT ---------------------------//
	// ----------------------------------------------------------------------------------//	
	
	// Behavior restricting constants..
	private static float explorerDefendDistance = 60;	// Constant to update the distance for which the scout will attempt to defend its archon......
	private static final float keepAwayDistance = (float) 10; // Distance at which the scouts will attempt to stay away from the unit they are investigating
	private static Todoruno.Rotation rotation;	
	private static boolean isStuck; // Represents whether or not the robot couldn't find a move to make the previous turn...
	
	// Store the archon location to go too.....
	private static MapLocation archonLocation;
	
	// Variable to store the enemyData that the RemBot has procured...
	private static EnemyData enemyData = new EnemyData();
	
	// Variable to show whether or not the scout has seen an enemy yet....
	private static boolean toInvestigate = false;
	private static boolean hasBegunInvestigating = false;
	
	// Variables regarding the currently investigated unit....
	private static int investigateID = -1;
	private static RobotInfo investigateRobot = null;	
		
	// ----------------------------------------------------------------------------------//
	// -------------------------------- RUNTIME FUNCTIONS -------------------------------//
	// ----------------------------------------------------------------------------------//	
	
	// Initialization function for RemBot.....
	
	public static void init(){		
		
		// Retrieve the correct corresponding archon...
		archonLocation = EnemyArchonSearch.getCorrespondingArchon();
		
		// SYSTEM CHECK - Draw a line to the target enemy archon location...
		rc.setIndicatorLine(myLocation, archonLocation, 255, 0, 0);
		
		float randomize = (float) Math.random();
		
		if(randomize >= 0.5){
			rotation = new Todoruno.Rotation(true);
		}
		else{
			rotation = new Todoruno.Rotation(false);
		}
		
		// SYSTEM CHECK  Make sure the robot starts its turn
        System.out.println("RemBot succesfully initialized");   
		
		main();
	}

	
	// Initialization follows from regular BestGirlBot behavior
		// When in RemBot mode, BestGirlBots do not shoot, and only observe................
		// RemBots attempt to go to the nearest enemy archon location and calculate how many enemies are nearby.....
	
	// Main function body.......
	
	protected static void main(){		
		
		// Code to be performed every turn   
		while (true){
			
			// Main actions of the scout.....
			try{
				
				// After 600 rounds return back to normal scout operations.....
				if(onlyRemIsBestGirl >= 600){
					
					// Call the main function of BestGirlBot.........................
					BestGirlBot.main();
				}				
				
				if(rotation != null){
					
					// SYSTEM CHECK - Print out the rotation orientation of the RemBot....
					System.out.println("Current rotation orientation: " + rotation.printOrientation());
				}      
        		
				// ------------------------ INTERNAL VARIABLES UPDATE ------------------------ //     

            	// Get nearby enemies and allies and bullets for use in other functions            	
            	RobotInfo[] enemyRobots = NearbyUnits(enemies, sensorRadius);
            	RobotInfo[] alliedRobots = NearbyUnits(allies, sensorRadius);
            	TreeInfo[] nearbyTrees = rc.senseNearbyTrees();
            	BulletInfo[] nearbyBullets = rc.senseNearbyBullets();
            	
            	// If there are enemy nearby, get the nearest enemy
            	if(enemyRobots.length > 0){
            		
            		// SYSTEM CHECK - Print out that there is a nearby enemy...
            		System.out.println("Nearby enemies sighted....");            	
            		
            		RobotInfo nearestEnemy = Chirasou.getNearestNonScoutEnemy(enemyRobots, myLocation);   
            		
            		if(nearestEnemy!= null){
		        		investigateID = nearestEnemy.ID;
		        		investigateRobot = nearestEnemy;
		        		toInvestigate = true;        
            		}
            	}
            	
            	// Update game variables
            	teamBullets = rc.getTeamBullets();
            	
            	// Make sure the robot shows appreciation for the one and only best girl in the world.
            	// If you are reading this and you think Emilia is best girl I have no words for you
		        onlyRemIsBestGirl = rc.getRoundNum();
		    	
		    	// Update positional and directional variables
		        myLocation = rc.getLocation();

		    	// See if there is a gardener and update the location of the nearest civilian if there is one....
		    	RobotInfo nearestGardener = Todoruno.getNearestGardener(alliedRobots, myLocation);
		    	// Set the nearest civilian location accordingly...
		       	if (nearestGardener != null){   
		       		
		       		nearestCivilianLocation = nearestGardener.location;	       		 		
		       	}
		       	
		       	// If the robot hadn't moved the previous turn... this value may be null
		       	if (lastDirection == null){
		       		
		       		// Set the direction to go to as away from the last known nearest civilian
		       		lastDirection = nearestCivilianLocation.directionTo(myLocation);
		       	}	
            	
		       	// SYSTEM CHECK - Show where the scout believes its nearest civilian is using a WHITE LINE
		       	// rc.setIndicatorLine(myLocation, nearestCivilianLocation, 255, 255, 255);
		       	
		      	// SYSTEM CHECK - Show where the scout is attempting to search....... LIGHT GREEN LINE
		     	// rc.setIndicatorLine(myLocation, archonLocation, 152, 251, 152);
		       	
		       	// ------------------------ BROADCAST UPDATES ------------------------ //
		    	
		       	// Update the nearest enemy and archon locations - If it sees any.......
            	BroadcastChannels.broadcastNearestEnemyLocation(enemyRobots, myLocation, unitNumber, nearestCivilianLocation, onlyRemIsBestGirl);            	
            	BroadcastChannels.broadcastEnemyArchonLocations(enemyRobots);
            	
            	// Broadcast the round number to the rembot indicator channel.....
            	rc.broadcast(BroadcastChannels.REMBOT_INDICATOR_CHANNEL, onlyRemIsBestGirl);
            	
            	// Update the distress info and retreat to the gardener if necessary..... if necessary (although rembot won't)      	
            	BroadcastChannels.BroadcastInfo distressInfo = BroadcastChannels.readDistress(myLocation, explorerDefendDistance);
            	
            	// TODO (UPDATE INITIAL UNIT COUNTS)........
            	
            	// If the robot thought it died previously but didn't.... update information...
            	if(believeHasDied){
            		isAlive();
            	}
            	
				// Update data stored in the Rembot's unit array.....
				enemyData.update(enemyRobots, nearbyTrees);
				
				// Broadcast the new data
				enemyData.broadcastTotals();
				
				// SYSTEM CHECK - after the update print out the number of each thing that the RemBot has seen thus far....	    	
		    	enemyData.printTotals();	

            	// ------------------------ MOVEMENT FUNCTIONS------------------------ //      
				
    			MapLocation desiredMove = move(enemyRobots, archonLocation);
    			
		       	if(desiredMove != null){
			       	// SYSTEM CHECK - Print out where the desired move is.....
			       	System.out.println("Currently attempting to move to location: " + desiredMove.toString());
		       	}   
		       	
		    	// --------------------------- DODGING ------------------------ //

            	// Call the dodge function
		       	MapLocation dodgeLocation = null;
		       	
		       	// Obtain a dodge location from the dodge function....
		       	dodgeLocation = Yuurei.tryDodge(desiredMove, myLocation, null, nearbyBullets, strideRadius, bodyRadius); 
		       	
		     	// If there is a location that the unit can dodge to..
            	if (dodgeLocation != null){
            		
            		if(!dodgeLocation.equals(desiredMove)){
            			
	            		desiredMove = dodgeLocation;
	            	   	// SYSTEM CHECK - Show desired move after path planning
	        	    	System.out.println("desiredMove altered by dodge to: " + desiredMove.toString());
            		} 
            	}
    			
            	// -------------------- MOVE CORRECTION ---------------------//
            	
            	// If the robot cannot move to the location determined above, call the movement correction function
            	if(!rc.canMove(desiredMove)){
            		desiredMove = movementCorrect(desiredMove, rotation);        		
            	}            	
            	
    			// ------------------------ MOVEMENT EXECUTION  ------------------------//
    			
    			// If the robot can move to the location chosen, do so
				if(rc.canMove(desiredMove)){
					
					// Check to see if the robot will die there
					checkRemDeath(desiredMove);
					
					// Move to the target location
					rc.move(desiredMove);
				}
				else{
					// Check to see if the robot will die there
					checkRemDeath(myLocation);
					
					// SYSTEM CHECK - Print out that the robot did not move this turn....
					System.out.println("RemBot didn't move this turn... Subaru didn't notice her T_T");
				}
				
				// Check to see if the unit can shake a tree....
				Chirasou.attemptInteractWithTree(myLocation, bodyRadius);

				// ------------------------ ROUND END VARIABLE UPDATES ---------------------- //	
				
		        // Make it so that the last direction traveled is the difference between the robot's current and final positions for the round...
                lastPosition =  rc.getLocation();
                lastDirection = new Direction(myLocation, lastPosition);                
					
                // Clear the investigation variables so that RemBot always selects the nearest enemy to investigate
				investigateID = -1;
				investigateRobot = null;
				toInvestigate = false;
				
				// Clear the isStuck value so that it doens't attempt to unstick itself repeatedly
				isStuck = false;
				
	            // SYSTEM CHECK  Make sure the robot finishes its turn
                System.out.println("Turn completed!");
				
				// Yield until the next turn...
			    Clock.yield();
			}
			catch (Exception exception){
				
				System.out.println("RemBot Exception");
                exception.printStackTrace();
			}			
		}	
	}
	
	// ----------------------------------------------------------------------------------//
	// ------------------------------- MOVEMENT FUNCTIONS -------------------------------//
	// ----------------------------------------------------------------------------------//	
	
	// General move function.....
    
    private static MapLocation move(RobotInfo[] enemyRobots, MapLocation targetLocation) throws GameActionException{
    	
    	// If the robot has yet to see a robot and it is far away from the initial archon location, move towards it....
    	if (myLocation.distanceTo(targetLocation) >= 20 && !hasBegunInvestigating){   	
    		
    		if(!toInvestigate && investigateRobot == null){
	    		// SYSTEM CHECK - Print out that the robot is far from the location and there is no nearby enemy
	    		System.out.println("No nearby enemies sighted - simply moving towards target destination");
	    		
	    		// Attempt to move towards the location
	    		return moveTowardsLocation(targetLocation);
    		}
    		else{  
    			
	    		// SYSTEM CHECK - Print out that the robot has seen an enemy to investigate and will attempt to pass by it...
	    		System.out.println("Rotating about enemy to get towards target location");
	    		
	    		return Todoruno.passByEnemy(myLocation, targetLocation, investigateRobot, strideRadius, keepAwayDistance, rotation);  		
	    	}
    	}
    	else{    		
    		// If the robot reached the archon Location and no enemies were found.....
    		if(myLocation.distanceTo(targetLocation) <= 2 && !toInvestigate){
    			
    			// Get the locations of the archons
    			MapLocation[] initialLocations = rc.getInitialArchonLocations(enemies);

    			// Randomly select one
    			int randomize = (int) (Math.random() * initialLocations.length);
    			
    			// Set it as the target archon location.....
    			archonLocation = initialLocations[randomize];
    			
    			return moveTowardsLocation(archonLocation);		
    			
    		}
    		else if(toInvestigate && investigateRobot != null){
    		
	    		// SYSTEM CHECK - Print out that the robot is currently rotating about the enemy...
	    		System.out.println("Circling about the enemies at the target location....");
	    		
	    		// Set it so that the robot will continue to attempt to investigate...
	    		hasBegunInvestigating = true;
	    		
	    		return  Todoruno.rotateAboutEnemy(myLocation, investigateRobot, strideRadius, keepAwayDistance, rotation);
    		}    		
    		else{
    			// SYSTEM CHECK - Print out that the robot is far from the location and there is no nearby enemy
	    		System.out.println("No nearby enemies sighted but close to destination - simply moving towards target destination");
	    		
	    		// Attempt to move towards the location
	    		return moveTowardsLocation(targetLocation);    			
    		}
    	} 	
    }

    
    // Function to control how scouts move to a location.... (assumes the location is on the map)
    	// First check to see if they can move completely towards the target location
    	// Then checks to see if there is a robot in the way, and calls the directional move function....., which prioritizes the direction close to the desired
    
    private static MapLocation moveTowardsLocation(MapLocation targetLocation) throws GameActionException{    	
    	
    	// Get the direction to the desired location and the furthest move that the scout can make in that direction
    	Direction directionTo = (myLocation.directionTo(targetLocation));
		MapLocation furthestMove = myLocation.add(directionTo, strideRadius);
		
		if(rc.canMove(furthestMove)){    			
			
			// SYSTEM CHECK - Show where the move is with a TURQUOISE...
			rc.setIndicatorLine(myLocation, furthestMove, 64, 224, 208);
			
			// Just return the move that takes the robot closest to the desired location......
			return furthestMove;
		}
		else{
			rc.setIndicatorLine(myLocation, myLocation.add(lastDirection, 20), 255, 255, 255);
			rc.setIndicatorLine(myLocation, targetLocation, 155, 255, 255);
			
			boolean isBlockedByRobot = false;
			
			for(int i = -1; i <= 1; i ++){
				
				Direction directionToCheck = new  Direction((float) (directionTo.radians + Math.PI/6 *i));
				
				MapLocation blockingCheck = myLocation.add(directionToCheck, bodyRadius + strideRadius);
				
				if(rc.senseRobotAtLocation(blockingCheck) != null){
					
					// SYSTEM CHECK - Print out a red dot on the robot that is blocking the way...
					rc.setIndicatorDot(blockingCheck, 200, 0, 0);
					
					isBlockedByRobot = true;					
				}
			}			
			// If there is a robot in the way, attempt to path around it......
			if(isBlockedByRobot){
				
				// SYSTEM CHECK - Print out that there is an enemy in the way
				System.out.println("There appears to be a robot in my way....");
				
				// Attempt to move randomly in the desired direction......
				MapLocation moveAttempt = Yuurei.tryMoveInDirection(lastDirection, 30, 5, strideRadius, myLocation);
				
				// If the moveAttempt was succesful debug lines....
				if(moveAttempt!= null){
					// SYSTEM CHECK - Show where the move is with a BLUE-VIOLET LINE...
					rc.setIndicatorLine(myLocation, moveAttempt, 138, 48, 226);
				}				
				return moveAttempt;
			}
			
			// This shouldn't happen, but if the scout cannot move toward the target, and it is not blocked by a robot, it is probably out of bounds
			else{
				
				// SYSTEM CHECK - Print out that the scout thinks that it is out of bounds....
				System.out.println("Cannot reach desired location and no robot is in the way, will attempt to correct movement....");
				
				// Return a small increment forward in the direction (if the robot cannot go, it will not after corrections....)
				return myLocation.add(directionTo, strideRadius / 5);			
			}			
		}
    }
    
	// ----------------------------------------------------------------------------------//
	// ------------------------------- MOVEMENT CORRECTION ------------------------------//
	// ----------------------------------------------------------------------------------//	
    
    public static MapLocation movementCorrect(MapLocation desiredLocation, Rotation rotation) throws GameActionException{
    	
    	// First check if the desired move cannot be made because it is out of bounds...
    	if(!rc.onTheMap(desiredLocation)){
    		
    		// SYSTEM CHECK - Print out that the robot's move was out of bounds...
    		System.out.println("Cannot move to desired location - it is out of bounds....");
    		
    		// Get the new corrected location....
    		desiredLocation = Yuurei.correctOutofBoundsError(desiredLocation, myLocation, bodyRadius, bodyRadius);
    		
    		// Switch the direction of rotation so that the scout doesn't try to continuously rotate around....
    		rotation.switchOrientation();
    	}
    	
    	Direction newDirection = myLocation.directionTo(desiredLocation);
    	
    	// If the robot cannot move in the desired direction......
    	if(!rc.canMove(desiredLocation)){
    		
    		// SYSTEM CHECK - Print out that the robot will attempt to move randomly..
    		System.out.println("Will attempt to move randomly - cannot move to initial desired destination");
    		
    		MapLocation testLocation = Yuurei.tryMoveInDirection(newDirection, 20, 9, strideRadius, myLocation);   
    		
    		desiredLocation = testLocation;
    	}
    	
    	// If the robot has resolved the conflict....
    	if(desiredLocation != null){
    		
			// SYSTEM CHECK - Display the corrected move on screen as an orange line
			rc.setIndicatorLine(myLocation, desiredLocation, 255, 165, 0);
    		
    		return desiredLocation;
    	}	
    	else{
    		// SYSTEM CHECK - Print out that the robot couldn't find a location to move to...
    		System.out.println("Cannot move to desired location and cannot find a new location.. will not move this turn...");
    		
    		isStuck = true;
    		
        	return myLocation;    		
    	}
    }
    
    
	// ----------------------------------------------------------------------------------//
	// ------------------------------- DATA STORAGE CLASS -------------------------------//
	// ----------------------------------------------------------------------------------//	
    
    
    // Class to store data about the number of unique enemy units/trees of each type the RemBot has seen thus far....
    
    private static class EnemyData{
    	
    	// Arrays to store the number of civilian units
       	private ArrayList<RobotInfo> enemyArchons;
    	private ArrayList<RobotInfo> enemyGardeners;
    	
    	// Arrays to store the number of combat units
    	private ArrayList<RobotInfo> enemyLumberjacks;
    	private ArrayList<RobotInfo> enemyScouts;    	
    	private ArrayList<RobotInfo> enemySoldiers;
    	private ArrayList<RobotInfo> enemyTanks;
    	
    	// Arrays to store the number of enemy trees seen
    	private ArrayList<TreeInfo> enemyTrees;
    	
    	// Array to store the number of neutral trees seen this turn...
    	private int neutralTrees;
    	private float neutralTreesArea;
    	
    	// Constructor initializes all of the lists as empty
    	EnemyData(){
    		
    		this.enemyArchons = new ArrayList<RobotInfo>();
    		this.enemyGardeners = new ArrayList<RobotInfo>();
    		
    		this.enemyLumberjacks = new ArrayList<RobotInfo>();
    		this.enemyScouts = new ArrayList<RobotInfo>();    		
    		this.enemySoldiers = new ArrayList<RobotInfo>();
    		this.enemyTanks = new ArrayList<RobotInfo>();
    		
    		this.enemyTrees = new ArrayList<TreeInfo>(); 
    		
    		this.neutralTrees = 0;
    		this.neutralTreesArea = 0;
    	}    
    	
    	// Function that takes in all the units that RemBOt has snesed this turn and updates the arrays of data
    
	    public void update(RobotInfo[] enemyRobots, TreeInfo[] nearbyTrees){
	    	
	    	// Reset the values for neutral trees and area....
    		this.neutralTrees = 0;
    		this.neutralTreesArea = 0;
    			
	    	// If there is an enemyRobot nearby...
	    	if(enemyRobots!= null){
	    		
	    		// Iterate over each robot seen and add to the list....
		    	for(RobotInfo enemyRobot: enemyRobots){
		    		addRobot(enemyRobot);
		    	}
	    	}
	    	// If there are trees nearby....
	    	if(nearbyTrees!= null){
	    		
	    		// Iterate over each tree and see if it can be added to the lists....
	    		for(TreeInfo nearbyTree: nearbyTrees){
	    			addTree(nearbyTree);
	    		}	    		
	    	}	    	    	
	    }
	    
	    
	    // Function to print out the number of unique units stored in each of the array lists.....
	    
	    public void printTotals(){
	    	
	    	// Print out the total number of civilian units
	    	System.out.println("Enemy archons seen: " + this.enemyArchons.size());
	    	System.out.println("Enemy gardeners seen: " + this.enemyGardeners.size());
	    	
	    	// Print out the numbers of combat units
	    	System.out.println("Enemy lumberjacks seen: " + this.enemyLumberjacks.size());
	    	System.out.println("Enemy scouts seen: " + this.enemyScouts.size());
	    	System.out.println("Enemy soldiers seen: " + this.enemySoldiers.size());
	    	System.out.println("Enemy tanks seen: " + this.enemyTanks.size());
	    	
	    	// Print out the numbers of enemy trees seen
	    	System.out.println("Enemy trees seen: " + this.enemyTrees.size());
	    	
	    	// Print out the number of neutral trees seen this turn........
	    	System.out.println("Neutral trees seen this turn: " + this.neutralTrees);	
	    	System.out.println("Neutral tree area seen this turn: " + this.neutralTreesArea);
	    }
	    
	    // Function to broadcast the values stored in the arrays of RemBot......
	    
	    public void broadcastTotals() throws GameActionException{
	    	
	       	// Broadcast out the total number of civilian units
	    	rc.broadcast(BroadcastChannels.REMBOT_INFORMATION_CHANNEL_START, this.enemyArchons.size());
	    	rc.broadcast(BroadcastChannels.REMBOT_INFORMATION_CHANNEL_START + 1, this.enemyGardeners.size());
	    	
	    	// Broadcast out the numbers of combat units
	    	rc.broadcast(BroadcastChannels.REMBOT_INFORMATION_CHANNEL_START + 2, this.enemyLumberjacks.size());
	    	rc.broadcast(BroadcastChannels.REMBOT_INFORMATION_CHANNEL_START + 3, this.enemyScouts.size());
	    	rc.broadcast(BroadcastChannels.REMBOT_INFORMATION_CHANNEL_START + 4, this.enemySoldiers.size());
	    	rc.broadcast(BroadcastChannels.REMBOT_INFORMATION_CHANNEL_START + 5, this.enemyTanks.size());
	    	
	    	// Broadcast out the numbers of enemy trees seen
	    	rc.broadcast(BroadcastChannels.REMBOT_INFORMATION_CHANNEL_START + 6, this.enemyTrees.size());
	    	
	     	// Broadcast out the number of neutral trees seen this turn........
	    	rc.broadcast(BroadcastChannels.REMBOT_INFORMATION_CHANNEL_START + 7, this.neutralTrees);
	    	rc.broadcastFloat(BroadcastChannels.REMBOT_INFORMATION_CHANNEL_START + 8, this.neutralTreesArea);	 
	    }
	    
	    
	    // Add a potential tree to the list......
	    
	    public boolean addTree(TreeInfo tree){
	    	
	    	// Make sure the tree isn't an allied tree
	    	if(tree.team != allies){
	    		
	    		// If the tree is an enemy tree
	    		if(tree.team == enemies){
	    			
	    			for(TreeInfo enemyTree: enemyTrees){
	    				
	    				// If the enemy tree had already been seen before...
	    				if(enemyTree.ID == tree.ID){
	    					
	    					// Return false.....
	    					return false;
	    				}	    				
	    			}
	    			// If after iterating through all the trees, tree wasn't found, add it to the list
	    			this.enemyTrees.add(tree);	    			

		    		// SYSTEM CHECK - Print out that a new enemy tree has been noticed...
		    		System.out.println("New enemy tree has been noticed with ID: " + tree.ID);	    		
	
		    		// Return true....
	    			return true;	    			
	    		}
	    		else{
	    			
	    			// Increment the list of neutral trees seen...	    			
	    			neutralTrees +=1;
	    			
	    			// Increment the area by the area of each neutral tree....
	    			neutralTreesArea += ((tree.radius) * (tree.radius) * Math.PI);
		    		
		    		// Return true.....
	    			return true;	    			
	    		}	    		
	    	}
	    	// If the tree was allied....
	    	else{
		    	return false;
	    	}	    	
	    }
	    
	    
	    // Add a potential robot to the lists.....
	    
	    public boolean addRobot(RobotInfo enemyRobot){
	    	
	    	// If the enemy to be checked is an archon....
	    	if(enemyRobot.type == RobotType.ARCHON){
	    		
	    		for(RobotInfo enemyArchon: this.enemyArchons){
	    			
	    			// If the robot was already put in the list
	    			if (enemyArchon.ID == enemyRobot.ID){
	    				
	    				// Return false....
	    				return false;
	    			}	    			
	    		}
	    		// If after iterating through all stored entries, the object has not been found, add it....!
	    		this.enemyArchons.add(enemyRobot);
	    		
	    		// SYSTEM CHECK - Print out that a new archon has been noticed...
	    		System.out.println("New archon has been noticed with ID: " + enemyRobot.ID);
	    		
	    		return true;
	    	}
	    	// If the enemy is a gardener
	    	else if(enemyRobot.type == RobotType.GARDENER){
	    		
	    		for(RobotInfo enemyGardener: this.enemyGardeners){
	    			
	    			// If the robot was already put in the list
	    			if (enemyGardener.ID == enemyRobot.ID){
	    				
	    				// Return false....
	    				return false;
	    			}	    			
	    		}
	    		// If after iterating through all stored entries, the object has not been found, add it....!
	    		this.enemyGardeners.add(enemyRobot);	    		
	      		
		    	// SYSTEM CHECK - Print out that a new gardener has been noticed...
		    	System.out.println("New gardener has been noticed with ID: " + enemyRobot.ID);
	    		
	    		return true;
	    	}
	    	// If the enemy is a lumberjack
	    	else if(enemyRobot.type == RobotType.LUMBERJACK){
	    		
	    		for(RobotInfo enemyLumberjack: this.enemyLumberjacks){
	    			
	    			// If the robot was already put in the list
	    			if (enemyLumberjack.ID == enemyRobot.ID){
	    				
	    				// Return false....
	    				return false;
	    			}	    			
	    		}
	    		// If after iterating through all stored entries, the object has not been found, add it....!
	    		this.enemyLumberjacks.add(enemyRobot);
	      		
	    		// SYSTEM CHECK - Print out that a new lumberjack has been noticed...
	    		System.out.println("New lumberjack has been noticed with ID: " + enemyRobot.ID);
	    		
	    		return true;
	    	}
		    	// If the enemmy was a scout...
		   	else if(enemyRobot.type == RobotType.SCOUT){
				
				for(RobotInfo enemyScout: this.enemyScouts){
					
					// If the robot was already put in the list
					if (enemyScout.ID == enemyRobot.ID){
						
						// Return false....
						return false;
					}	    			
				}
				// If after iterating through all stored entries, the object has not been found, add it....!
				this.enemyScouts.add(enemyRobot);
	      		
		    	// SYSTEM CHECK - Print out that a new scout has been noticed...
		    	System.out.println("New scout has been noticed with ID: " + enemyRobot.ID);
	    		
				return true;
			}
		   	else if(enemyRobot.type == RobotType.SOLDIER){
				
				for(RobotInfo enemySoldier: this.enemySoldiers){
					
					// If the robot was already put in the list
					if (enemySoldier.ID == enemyRobot.ID){
						
						// Return false....
						return false;
					}	    			
				}
				// If after iterating through all stored entries, the object has not been found, add it....!
				this.enemySoldiers.add(enemyRobot);				
	      		
		    	// SYSTEM CHECK - Print out that a new soldier has been noticed...
		    	System.out.println("New soldier has been noticed with ID: " + enemyRobot.ID);
		    	
				return true;
			}
		   	else if(enemyRobot.type == RobotType.TANK){
				
				for(RobotInfo enemyTank: this.enemyTanks){
					
					// If the robot was already put in the list
					if (enemyTank.ID == enemyRobot.ID){
						
						// Return false....
						return false;
					}	    			
				}
				// If after iterating through all stored entries, the object has not been found, add it....!
				this.enemyTanks.add(enemyRobot);				
	      		
		    	// SYSTEM CHECK - Print out that a new tank has been noticed...
		    	System.out.println("New tank has been noticed with ID: " + enemyRobot.ID);
		    	
				return true;
			}
		    	// This shouldn't happen, but if the function was called for something that wasn't one of the above types, return false
		   	else{
		   		return false;
		   	}  	
	    }    
    } 
    
	// ----------------------------------------------------------------------------------//
	// --------------------------- MISCELANNELOUS FUNCTIONS -----------------------------//
	// ----------------------------------------------------------------------------------//	 
    
	// Function to check if the scout will die if it moves to a certain location
	
    private static void checkRemDeath(MapLocation location) throws GameActionException{    	

		// If the lumberjack will lose all of its health from moving to that location....
		boolean willDie = iFeed.willFeed(location);
		
		// If the lumberjack believes that it will die this turn....
		if (willDie) {
			
			// Set the belief variable to true.....
			believeHasDied = true;
			
			// Get the current number of scouts in service
	        int currentScoutNumber = rc.readBroadcast(BroadcastChannels.SCOUTS_ALIVE_CHANNEL);
	        
	        // Update scout number for other units to see.....
	        rc.broadcast(BroadcastChannels.SCOUTS_ALIVE_CHANNEL, currentScoutNumber - 1);
	        
	        // Get the current number of RemBots in service
	        int currentRemBotNumber = rc.readBroadcast(BroadcastChannels.REMBOT_ALIVE_CHANNEL);
	        
	        rc.broadcast(BroadcastChannels.REMBOT_ALIVE_CHANNEL, currentRemBotNumber - 1);
		
		}
	}
    
    // Function to correct an accidental death update
    
    private static void isAlive() throws GameActionException{
    	
    	// Reset belief in the robot dying this round....
    	believeHasDied = false;    	

		// Get the current number of lumberjacks in service
        int currentScoutNumber = rc.readBroadcast(BroadcastChannels.SCOUTS_ALIVE_CHANNEL);
        
        // Update lumberjack number for other units to see.....
        rc.broadcast(BroadcastChannels.SCOUTS_ALIVE_CHANNEL, currentScoutNumber + 1);        
     
        // Get the current number of RemBots in service
        int currentRemBotNumber = rc.readBroadcast(BroadcastChannels.REMBOT_ALIVE_CHANNEL);
        
        // Broadcast corrected number of RemBots for everyone to see
        rc.broadcast(BroadcastChannels.REMBOT_ALIVE_CHANNEL, currentRemBotNumber + 1);
    	
    }	
}
