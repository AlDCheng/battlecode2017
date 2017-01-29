package naclbot.units.AI.scout;

import java.util.ArrayList;
import java.util.Arrays;
import battlecode.common.*;
import naclbot.units.motion.Yuurei;
import naclbot.units.motion.shoot.Korosenai;
import naclbot.units.interact.iFeed;
import naclbot.units.motion.Chirasou;
import naclbot.units.motion.Todoruno;
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
	
	// Store the archon location to go too.....
	private static MapLocation archonLocation;
	
	// Variable to store the enemyData that the RemBot has procured...
	private static EnemyData enemyData = new EnemyData();
	
	// Variable to show whether or not the scout has seen an enemy yet....
	private static boolean toInvestigate = false;
	
	// Variables regarding the currently investigated unit....
	private static int investigateID = -1;
	private static RobotInfo investigateData = null;	
	
	// ----------------------------------------------------------------------------------//
	// -------------------------------- RUNTIME FUNCTIONS -------------------------------//
	// ----------------------------------------------------------------------------------//	
	
	// Initialization function for RemBot.....
	
	public static void init(){
		
		//archonLocation = rc.getInitialArchonLocations(enemies)[0]; // Utilize the first archonlocation by default.......
		
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
	
	public static void main(){		
		
		// Code to be performed every turn   
		while (true){
			
			// Main actions of the scout.....
			try{
				
				if(rotation != null){
					// SYSTEM CHECK - Print out the rotation orientation of the RemBot....
					System.out.println("Current rotation orientation: " + rotation.printOrientation());
				}
				// SYSTEM CHECK  Make sure the robot starts its turn
                System.out.println("Remember guys Rem is best girl!!!");                 
        		
				// ------------------------ INTERNAL VARIABLES UPDATE ------------------------ //     

            	// Get nearby enemies and allies and bullets for use in other functions            	
            	RobotInfo[] enemyRobots = NearbyUnits(enemies, sensorRadius);
            	RobotInfo[] alliedRobots = NearbyUnits(allies, sensorRadius);
            	TreeInfo[] nearbyTrees = rc.senseNearbyTrees();
            	BulletInfo[] nearbyBullets = rc.senseNearbyBullets();
            	
            	// If there are enemy nearby, get the nearest enemy
            	if(enemyRobots.length > 0){
            		
            		RobotInfo nearestEnemy = Chirasou.getNearestAlly(enemyRobots, myLocation);
            		
            		investigateID = nearestEnemy.ID;
            		investigateData = nearestEnemy;
            		toInvestigate = true;            		
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
		       	rc.setIndicatorLine(myLocation, nearestCivilianLocation, 255, 255, 255);
		       	
		      	// SYSTEM CHECK - Show where the scout is attempting to search....... LIGHT GREEN LINE
		     	// rc.setIndicatorLine(myLocation, archonLocation, 152, 251, 152);
		       	
		       	// ------------------------ BROADCAST UPDATES ------------------------ //
		    	
		       	// Update the nearest enemy and archon locations - If it sees any.......
            	BroadcastChannels.broadcastNearestEnemyLocation(enemyRobots, myLocation, unitNumber, nearestCivilianLocation, onlyRemIsBestGirl);            	
            	BroadcastChannels.broadcastEnemyArchonLocations(enemyRobots);
            	
            	// Update the distress info and retreat to the gardener if necessary..... if necessary            	
            	BroadcastChannels.BroadcastInfo distressInfo = BroadcastChannels.readDistress(myLocation, explorerDefendDistance);
            	
            	// TODO (UPDATE INITIAL UNIT COUNTS)........
            	
            	// If the robot thought it died previously but didn't.... update information...
            	if(believeHasDied){
            		fixAccidentalDeathNotification();
            	}

            	// ------------------------ MOVEMENT FUNCTIONS------------------------ //      
				
				if(investigateID != -1 && investigateData != null){
					
					if(rc.canSenseRobot(investigateID)){
						
						MapLocation desiredMove = Todoruno.rotateAboutEnemy(myLocation, investigateData, strideRadius, keepAwayDistance, rotation);
						
						if(rc.canMove(desiredMove)){
							rc.move(desiredMove);
						}						
					}				
				}
				
				enemyData.update(enemyRobots, nearbyTrees);
				
				
				// ------------------------ ROUND END VARIABLE UPDATES ---------------------- //	
				
				System.out.println(unitNumber);
				System.out.println(myID);
				
				investigateID = -1;
				investigateData = null;
				
			     Clock.yield();
			}
			catch (Exception exception){
				System.out.println("Explorer Scout Exception");
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
    	if (myLocation.distanceTo(targetLocation) >= 5 || !toInvestigate){   	
    		
    		// Attempt to move towards the location
    		return moveTowardsLocation(targetLocation);
    		
    	
    	
    		
    		
    		
    		
    	}
    	
    	return null;
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
			// If there is a robot in the way, attempt to path around it......
			if(rc.senseRobotAtLocation(furthestMove) != null){
				
				// SYSTEM CHECK - Print out that there is an enemy in the way
				System.out.println("There appears to be a robot in my way....");
				
				// Attempt to move randomly in the desired direction......
				MapLocation moveAttempt = Yuurei.tryMoveInDirection(directionTo, 30, 5, strideRadius, myLocation);
				
				// If the moveAttempt was succesful debug lines....
				if(moveAttempt!= null){
					// SYSTEM CHECK - Show where the move is with a CYAN LINE...
					rc.setIndicatorLine(myLocation, moveAttempt, 0, 139, 139);
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
	// ------------------------------- DATA STPRAGE CLASS -------------------------------//
	// ----------------------------------------------------------------------------------//	
    
    private static class EnemyData{
    	
       	private ArrayList<RobotInfo> enemyArchons;
    	private ArrayList<RobotInfo> enemyGardeners;
    	
    	private ArrayList<RobotInfo> enemyLumberjacks;
    	private ArrayList<RobotInfo> enemyScouts;
    	
    	private ArrayList<RobotInfo> enemySoldiers;
    	private ArrayList<RobotInfo> enemyTanks;
    	
    	private ArrayList<TreeInfo> enemyTrees;
    	private ArrayList<TreeInfo> neutralTrees;
    	
    	// Constructor initializes all of the lists as empty
    	EnemyData(){
    		
    		this.enemyArchons = new ArrayList<RobotInfo>();
    		this.enemyGardeners = new ArrayList<RobotInfo>();
    		
    		this.enemyLumberjacks = new ArrayList<RobotInfo>();
    		this.enemyScouts = new ArrayList<RobotInfo>();
    		
    		this.enemySoldiers = new ArrayList<RobotInfo>();
    		this.enemyTanks = new ArrayList<RobotInfo>();
    		
    		this.enemyTrees = new ArrayList<TreeInfo>(); 
    		this.neutralTrees = new ArrayList<TreeInfo>();   
    	}   	
    
    
	    public void update(RobotInfo[] enemyRobots, TreeInfo[] nearbyTrees){
	    	
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
	    	
	    	// SYSTEM CHECK - after the update print out the number of each thing that the RemBot has seen thus far....	    	
	    	printTotals();	    	
	    }
	    
	    
	    // Function to print out the number of unique units stored in each of the array lists.....
	    
	    public void printTotals(){
	    	
	    	System.out.println("Enemy archons seen: " + this.enemyArchons.size());
	    	System.out.println("Enemy gardeners seen: " + this.enemyGardeners.size());
	    	System.out.println("Enemy lumberjacks seen: " + this.enemyLumberjacks.size());
	    	System.out.println("Enemy scouts seen: " + this.enemyScouts.size());
	    	System.out.println("Enemy soldiers seen: " + this.enemySoldiers.size());
	    	System.out.println("Enemy tanks seen: " + this.enemyTanks.size());
	    	System.out.println("Enemy trees seen: " + this.enemyTrees.size());
	    	System.out.println("Neutral trees seen: " + this.neutralTrees.size());
	    	
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
	    			
	    			for(TreeInfo neutralTree: neutralTrees){
	    				
	    				if(neutralTree.ID == tree.ID){
	    					return false;
	    				}	    				
	    			}	    			
	    			// If after iterating through all the trees, tree wasn't found, add it to the list
	    			this.neutralTrees.add(tree);
	    			
	    			// SYSTEM CHECK - Print out that a new neutral tree has been noticed...
		    		System.out.println("New neutral tree has been noticed with ID: " + tree.ID);	    	
		    		
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
    
    
    
}
