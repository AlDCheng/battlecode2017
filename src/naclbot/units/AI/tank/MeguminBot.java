// AI for soldier under normal control
package naclbot.units.AI.tank;
import battlecode.common.*;

import naclbot.variables.BroadcastChannels;
import naclbot.variables.GlobalVars;
import naclbot.units.interact.iFeed;
import naclbot.units.motion.Chirasou;
import naclbot.units.motion.Todoruno;
import naclbot.units.motion.Yuurei;
import naclbot.units.motion.shoot.Korosenai;
import naclbot.units.motion.routing.Routing;
import naclbot.units.motion.search.EnemyArchonSearch;

/* --------------------------   Overview  --------------------------
 * 
 * 			  AI Controlling the functions of the Tank
 *
 *				 ~~ Coded by Illiyia (akimn@#mit.edu)
 *
 *			 Call the init() function to use the file...
 * 
 * 		  Note: Debug statements all begin with SYSTEM CHECK 
 * 
 * 				           MEGUMIN IS BESTGIRL KONOSUBA
 * 
 ------------------------------------------------------------------- */

/* -------------------- LIST OF THINGS TO DO??? --------------------
 * 
 * 			Give MeguminBot more personality............ EXPLOSION!
 * 
 ------------------------------------------------------------------- */


public class MeguminBot extends GlobalVars {
	
	// ----------------------------------------------------------------------------------//
	// ------------------------- VARIABLES FOR USE BY THE ROBOT -------------------------//
	// ----------------------------------------------------------------------------------//	
	
	// ------------- GAME VARIABLES -------------//
	
	// Variable to store the round number
	private static int onlyIdiotsLikeAquaMoreThanMegumin;
	
	// Variables to store the teams currently in the game
	public static Team enemies;
	public static Team allies;
	
	// Gamne-defined robot class related parameters
	private static float strideRadius = battlecode.common.RobotType.TANK.strideRadius;
	private static float bodyRadius = battlecode.common.RobotType.TANK.bodyRadius;
	private static float sensorRadius = battlecode.common.RobotType.TANK.sensorRadius;
	
	// ------------- PERSONAL VARIABLES -------------//
	
	// Self-identifiers...
	public static int myID; // Game-designated ID of the robot
	public static int unitNumber; // Team-generated unit number - represents order in which units were built
	public static int tankNumber; // Team generated number - represents order in which soldiers were built
	
	private static int initRound; // The initial round in which the robot was constructed

	
	// Personal movement variables
	private static MapLocation myLocation; // The current location of the soldier...
	private static MapLocation lastPosition; // The previous location that the soldier was at...
	private static Direction lastDirection; // The direction in which the soldier last traveled

	// ------------- OPERATION VARIABLES -------------//
	
	// Variables related to tracking....
	private static int retardAquaID; // The robot that the soldier is currently tracking....
	private static RobotInfo bakaAqua; // The Robot that the soldier is currently tracking....
	private static boolean isAquaRetarded; // Boolean to show whether or not the tank is currently tracking something or not...

	// Path-planning variables
	private static boolean isCommanded; // Boolean to store whether or not the tank current has orders to go somewhere....
    public static MapLocation goalLocation; // End location of the path planning
    public static int roundsRouting = 0; // FVariable to store the length of time the robot has been in path planning mode....
    
    
    // Routing constants
    public static final int attackFrequency = 0; // Asserts how often robots will attempt to go on the attack after completing a prior attack....
    public static final float attackProbability = (float) 1; // Gives probability of joining an attack at a particular time....
    private static int lastCommanded = attackFrequency; // Int to store the number of rounds since the unit was last in a commanded mode - threshold value
    public static final int giveUpOnRouting = 100; // Variable to determine after how long tanks decide that Alan's code is a piece of shit......
   
    // Enemy data variables....
	private static RobotInfo[] previousRobotData; // Array to store the data of enemy robots from the previous turn.....

	
	// ------------- ADDITIONAL VARIABLES/CONSTANTS -------------//

	// Variables related to operational behavior...
	private static MapLocation nearestCivilianLocation; // Stores for multiple rounds the location of the nearest civilian robot....	
	private static final float separationDistance = sensorRadius - 1; // stores how large of a distance tanks will attempt to keep from nearby units when they engage them...
	private static MapLocation archonLocation; // Stores the location of the archon that the tank is by default sent to attack....	
    // Miscellaneous variables.....
 	private static boolean believeHasDied; // Stores whether or not the robot believes it will die this turn or not.........
 	private static boolean checkArchons = false; // Stores whether or not the robot will attempt to look for archons or not....
 	
  
 	
	// ----------------------------------------------------------------------------------//
	// -------------------------------- RUNTIME FUNCTIONS -------------------------------//
	// ----------------------------------------------------------------------------------//	
 	
    
	// Initialization function - makes the default values for most important parameters
    
    public static void init() throws GameActionException{
    	
        // Important parameters for self
        enemies = rc.getTeam().opponent();
        allies = rc.getTeam();
        myID = rc.getID();      
        
        onlyIdiotsLikeAquaMoreThanMegumin = rc.getRoundNum();
        initRound = onlyIdiotsLikeAquaMoreThanMegumin;

        // Get own scoutNumber  and unitNumber- important for broadcasting 
        tankNumber = rc.readBroadcast(BroadcastChannels.TANK_NUMBER_CHANNEL);        
        unitNumber = rc.readBroadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL);      
          
        // Get the current round number......
        onlyIdiotsLikeAquaMoreThanMegumin = rc.getRoundNum();
        initRound = onlyIdiotsLikeAquaMoreThanMegumin;
 
        // Initialize variables important to self
        myLocation = rc.getLocation();
        retardAquaID = -1;
        isAquaRetarded = false;
        bakaAqua = null;
        previousRobotData = null;
    	
    	// In order to get the closest current ally..... obtain data for the nearest allied units and then the gardener if it exists....
     	RobotInfo[] alliedRobots = NearbyUnits(allies, sensorRadius);
       	RobotInfo nearestGardener = Todoruno.getNearestGardener(alliedRobots, myLocation);
       	
       	// If there is a gardener nearby, set the nearest civilian location accordingly...
       	if (nearestGardener != null){       		
       		nearestCivilianLocation = nearestGardener.location;
       	}
       	// Otherwise use the data stored in the broadcast of the initial archon locations...
       	else{           	
       		// Get the locations from the archon broadcasts
            int archonInitialX = rc.readBroadcast(BroadcastChannels.ARCHON_INITIAL_LOCATION_X) / 100;
            int archonInitialY = rc.readBroadcast(BroadcastChannels.ARCHON_INITIAL_LOCATION_Y) / 100;
    		
            // Set the nearestCivilianLocation using the data gained...
            nearestCivilianLocation = new MapLocation(archonInitialX, archonInitialY);       		
       	}
       	
       	// Set the tank to first attempt to move away from the nearest civilian initially....
       	lastDirection = nearestCivilianLocation.directionTo(myLocation);
        
       	// Retrieve the number of active tanks and increment......
       	int numberOfActiveTanks = rc.readBroadcast(BroadcastChannels.TANKS_ALIVE_CHANNEL);
       	rc.broadcast(BroadcastChannels.TANKS_ALIVE_CHANNEL, numberOfActiveTanks + 1);    
        
        // Update the number of tanks so other tanks can know....
        rc.broadcast(BroadcastChannels.TANK_NUMBER_CHANNEL, tankNumber + 1);
        rc.broadcast(BroadcastChannels.UNIT_NUMBER_CHANNEL, unitNumber + 1);
        
		// Retrieve the correct corresponding archon...
		archonLocation = EnemyArchonSearch.getCorrespondingArchon();
		
		// SYSTEM CHECK - Draw a line to the target enemy archon location...
		rc.setIndicatorLine(myLocation, archonLocation, 255, 0, 0);
        
        main();
    }
    
    // Main function of the tank, contains all turn by turn actions....
    
    public static void main() throws GameActionException{
    	
    	// Actions to be completed every turn by the soldier.....,
    	while(true){
    		
    		try{    	
    		    // SYSTEM CHECK  Make sure the robot starts its turn
                System.out.println("MeguminBot recharging!");    
    			
    			// ------------------------- RESET/UPDATE VARIABLES ----------------//        
    			
            	// Get nearby enemies and allies and bullets for use in other functions            	
            	RobotInfo[] enemyRobots = NearbyUnits(enemies, sensorRadius);
            	RobotInfo[] alliedRobots = NearbyUnits(allies, sensorRadius);
            	BulletInfo[] nearbyBullets = rc.senseNearbyBullets();
            	TreeInfo[] nearbyTrees = rc.senseNearbyTrees();
    			
            	// Update global variables.....
            	onlyIdiotsLikeAquaMoreThanMegumin = rc.getRoundNum();
		    	
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
		       		
		       		// SYSTEM CHECK - Print out that the last turn, lastDirection was null....
		       		System.out.println("Last direction was previously null....");
		       		
		       		// Set the direction to go to as away from the last known nearest civilian
		       		lastDirection = nearestCivilianLocation.directionTo(myLocation);
		       	}	
            	
		       	
		       	// SYSTEM CHECK - Show where the tank believes its nearest civilian is using a WHITE LINE
		       	// rc.setIndicatorLine(myLocation, nearestCivilianLocation, 255, 255, 255);		    
            	
            	
            	// ------------ ACTIONS TO BE COMPLETED -------------//
            	
               	
            	// Update the nearest enemy and archon locations
               	BroadcastChannels.broadcastEnemyArchonLocations(enemyRobots);     
            	BroadcastChannels.broadcastNearestEnemyLocation(enemyRobots, myLocation, unitNumber, nearestCivilianLocation, onlyIdiotsLikeAquaMoreThanMegumin);
        	
              	
               	// If the robot thought it died previously but didn't.... update information...
            	if(believeHasDied){
            		fixAccidentalDeathNotification();
            	}
            	
         
            	// ------------------------ MOVEMENT FUNCTIONS------------------------ //      
            	
            	// Placeholder for the location where the robot desires to move - can be modified by dodge
            	MapLocation desiredMove = decideAction(enemyRobots, alliedRobots, nearbyTrees);
            	
            	// New change - sometimes the decideACtion will move the tank.... If this happens do not run following code as desiredMOve has no effect...
            	if(!rc.hasMoved()){
            		
            		// SYSTEM CHECK - Print out that the tank has not yet moved this turn...
            		System.out.println("Tank passed through decideAction without actually moving");
            		
			       	if(desiredMove != null){
				       	// SYSTEM CHECK - Print out where the desired move is.....
				       	System.out.println("Currently attempting to move to location: " + desiredMove.toString());
			       	}        	
	            	
	            	// --------------------------- DODGING ------------------------ //
	
	            	// Call the dodge function
			       	MapLocation dodgeLocation = null;
			       	
			       	if (bakaAqua != null){
			       		
			       		// SYSTEM CHECK - Print out that the dodge function has been called with an enemy nearby....
			       		System.out.println("Calling dodge with a nearby enemy....");
			       		
			       		dodgeLocation = Yuurei.tryDodge(desiredMove, myLocation, bakaAqua.location, nearbyBullets, strideRadius, bodyRadius); 
			       	}
			       	else{		
			       		
			       		// SYSTEM CHECK - Print out that the dodge function has been called with an enemy nearby....
			       		System.out.println("Calling dodge without a nearby enemy....");
			       		
			       		dodgeLocation = Yuurei.tryDodge(desiredMove, myLocation, null, nearbyBullets, strideRadius, bodyRadius); 
					    
			       	}
	            	// If there is a location that the unit can dodge to..
	            	if (dodgeLocation != null){
	            		
	            		if(!dodgeLocation.equals(desiredMove)){
	            			
		            		desiredMove = dodgeLocation;
		            	   	// SYSTEM CHECK - Show desired move after path planning
		        	    	System.out.println("desiredMove altered by dodge to: " + desiredMove.toString());
	            		}            		
	            		else{
	            			// And there is an enemy being shot at.....
	                		if(retardAquaID != -1 && bakaAqua != null){
	                			
	                			// Check to see if the current line of fire is blocked by a tree....
	                			if(Korosenai.isLineBLockedByTree(desiredMove, bakaAqua.location, 1)){
	                				
		                			// SYSTEM CHECK - Print out that the robot will attempt to find a different firing location..
		                			System.out.println("Attempting to find another firing location");
		                			
		                			MapLocation newFiringLocation = Korosenai.findLocationToFireFrom(myLocation, bakaAqua.location, desiredMove, strideRadius);
		                			
		                			if(newFiringLocation != null){
		    	            			// SYSTEM CHECK - Print out that another firing location had been found...
		    	            			System.out.println("New firing Location found.....");
		    	            			desiredMove = newFiringLocation;
		                			}
	                			}            		
	                		} 
	            		}            		
	            	}  
	            	
	               	// SYSTEM CHECK- Print out the amount of bytecode used prior to movecorrect
			       	System.out.println("Bytecode used before move correct: " + Clock.getBytecodeNum());
	            	
	                
	            	// -------------------- MOVE CORRECTION ---------------------//
	            	
			       	// Get the correction from the wrapping correct all move function....
	            	MapLocation correctedMove = Yuurei.correctAllMove(strideRadius, bodyRadius, false, allies, myLocation, desiredMove);            	
			       	
			       	if(correctedMove != null){
			       		
		    	       	// SYSTEM CHECK - Print out where the desired move is.....
				       	System.out.println("Corrected move is: " + correctedMove.toString());	
				       	
				       	// Set the desired location to be the corrected location
				       	desiredMove = correctedMove;
			       	}
			       	// If the robot could not find a location to go to even with the corrected location.....
			       	else{	       		
			       		// SYSTEM CHECK - Print out that the scout never had a place to go to...
			       		System.out.println("No move possible..... will simply remain in place");
			       		
			       		desiredMove = myLocation;		       		
			       	}
			       	
			       	if(desiredMove.equals(myLocation)){
			       		
			       		// Check to see if the robot is in a corner...
			       		int corner = Yuurei.checkIfNearCorner(bodyRadius, strideRadius, desiredMove);
			       		
			       		// If the above function returns a positive integer, the robot is near a corner....
			       		if (corner != 0){
			       			
			       			// SYSTEM CHECK - Print out that the robot is near a corner....
			       			System.out.println("Currently near a corner, will attempt to rectify....");
			       			
			       			desiredMove = Yuurei.moveOutOfCorner(strideRadius, corner, desiredMove);		       	
			       		}		       		
			       	}
			       	          	
			       	// SYSTEM CHECK- Print out the amount of bytecode used after move correct
			       	System.out.println("Bytecode used after move correct: " + Clock.getBytecodeNum());
			       	
			     // ------------------------ Movement Execution  ------------------------//
	
	    	       	// If the robot can move to the location it wishes to go to.....
			       	if(rc.canMove(desiredMove) && desiredMove != myLocation){
			       		
			       		// SYSTEM CHECK - Print out that the robot successfully moved....
			       		System.out.println("Tank succesfully moved to desired location");
			       		
			       		// Check to see if the robot will die there
			       		// checkDeath(desiredMove);
			       		// Move to the target location
			       		rc.move(desiredMove);
			       	}
			       	
			       	// If the robot didn't move along, check if it would die from staying in its current location....
			       	else{
			       		
			    		// SYSTEM CHECK - Print out that the robot did not move
			       		System.out.println("Tank did not move this turn....");
			       				       		
			       		// checkDeath(myLocation);
			       	} 
			       	
			       	// SYSTEM CHECK- Print out the amount of bytecode used prior to shooting.......
			       	System.out.println("Bytecode used prior to shooting: " + Clock.getBytecodeNum());		       	
			       	
			       	// Update the position for the end of the round...
	                lastPosition =  rc.getLocation();
	
	            	// ------------------------ Shooting ------------------------//
	            
	            	// SYSTEM CHECK - Notify that the robot is now attempting to shoot at something........
	            	// System.out.println("Moving on to shooting phase...................");
	            	
	            	boolean hasShot = false;
	            	
	            	if (retardAquaID != -1){
	            		
	            		// SYSTEM CHECK - Show who the robot is aiming at...
	            		System.out.println("Currently shooting at a robot with ID: " + retardAquaID);
	            		
	            		// Get a list of allied trees to avoid shooting..
	            		TreeInfo[] alliedTrees = rc.senseNearbyTrees(-1, allies);
	            		
	            		if(rc.canSenseRobot(retardAquaID)){
	            			hasShot = decideShoot(enemyRobots, alliedRobots, alliedTrees);
	            		}
	            		else{
	            			retardAquaID= -1;
	            			bakaAqua = null;
	            		}
	            	}
	            	
	            	if(hasShot){            		
	            		// SYSTEM CHECK - Inform that the robot has shot something this round....
	            		System.out.println("The robot has fired a shot this round....");
	            	}
	            	else{
	              		// SYSTEM CHECK - Inform that the robot has not shot something this round.......
	            		System.out.println("The robot has not fired a shot this round....");            		
	            	}
            	}
            	else{
            		// SYSTEM CHECK - Print out that the robot has already attempted to move....
            		System.out.println("Robot has already moved, no dodging or move correction will occur......");
            		
            		// If MeguminBot has attacked this turn...
            		if(rc.hasAttacked()){
            			
	            		// SYSTEM CHECK - Print out Megumin quotes..........
	        			printOutMeguminQuotes();
	        			
	        			System.out.println("*MeguminBot Collapses");		
            		}
            	}
				
				// Check to see if the unit can shake a tree....
				Chirasou.attemptInteractWithTree(myLocation, bodyRadius);
            	            	
            	// ------------------  Round End Updates --------------------//

                // Make it so that the last direction traveled is the difference between the robot's current and final positions for the round...
                lastDirection = new Direction(myLocation, lastPosition);
                
                
                // Make sure to reset track data.....
            	retardAquaID = -1;
            	bakaAqua = null;  

                // Store the data for the locations of the enemies previously.....
                previousRobotData = enemyRobots;
                
                // SYSTEM CHECK  Make sure the robot finishes its turn
                System.out.println("Turn completed.....");
                
                // If the robot is not currently in a commanded state, increment
                if (!isCommanded){
                	lastCommanded += 1;
                }
                // The robot was in routing phase, so increment that counter
                else{
                	roundsRouting += 1;
                }  
               

                Clock.yield();    	
            	
	        } catch (Exception e) {
	            System.out.println("MeguminBot Exception");
	            e.printStackTrace();
	        }    	
    	}
    }
    
    
	// ----------------------------------------------------------------------------------//
	// ------------------------------- MOVEMENT FUNCTIONS -------------------------------//
	// ----------------------------------------------------------------------------------//	 
    
	// Function to determine how the robot will act this turn....
	
	private static MapLocation decideAction(RobotInfo[] enemyRobots, RobotInfo nearbyAllies[], TreeInfo[] nearbyTrees) throws GameActionException{
		
		// If the robot currently has orders call the setCommandLocation to see if a new order could be made
		if(!isCommanded){    			
			setCommandLocation(null);
		}
		
		// Call the move function to determine where the robot will actually end up going.....
		return move(enemyRobots, nearbyAllies, nearbyTrees);
	}    
    
    private static MapLocation move(RobotInfo[] enemyRobots, RobotInfo[] nearbyAllies, TreeInfo[] nearbyTrees) throws GameActionException{

		// SYSTEM CHECK - Print out that the robot is searching for nearest enemy to engage
		System.out.println("Searching for the next enemy to engage...."); 
		
		// See if a robot to be tracked can be found, allow tank to track any and all units but archons and scouts
		bakaAqua = Todoruno.getNewEnemyToTrack(enemyRobots, myLocation, true, false, true, false);	

		
		if((onlyIdiotsLikeAquaMoreThanMegumin >= 500 && bakaAqua == null && rc.getTreeCount() > 10) || (bakaAqua == null && onlyIdiotsLikeAquaMoreThanMegumin > 1500)){
			
			// SYSTEM CHECK - Print out that the robot will now attempt to fire at archons...
			System.out.println("Will now attempt to shoot archons....");
			
			// Search again for enemies......
			bakaAqua = Todoruno.getNewEnemyToTrack(enemyRobots, myLocation, true, false, true, true);			
		}
		

		// Switch over to the move command after getting a new unit to track.... if the unit is currently being told to go somewhere
		if(isCommanded && bakaAqua == null || (!isCommanded && goalLocation != null)){
		
			// SYSTEM CHECK - Print out that the tank will attempt to move to a goal location..
			System.out.println("Attempting to move to the goal location at: " + goalLocation.toString());
			
       		// Call the routing function to obtain a location to go to........
			return moveTowardsGoalLocation(enemyRobots, nearbyAllies, nearbyTrees);
    	} 
		
		// If there is a robot to track....
		else if (bakaAqua != null){
			
			// Reset the values necessary for switching into a command phase
    		goalLocation = null;	isCommanded = false;    		
			
			// Update the retardAquaID
			retardAquaID = bakaAqua.ID;	isAquaRetarded = true;
			
			// SYSTEM CHECK - Notify what the robot will now track and set an indicator RED DOT on it
    		System.out.println("The tank has noticed the enemy Robot with ID: " + retardAquaID);

			// Call move again with the updated information - so that the robot will pass into the second bloc of logic
			return engage(enemyRobots, nearbyAllies, nearbyTrees);   	
		
		// If there is no robot to be tracked and the robot is not receiving any orders
		} else{
			
			// Make sure that the tracking variables are reset....
			bakaAqua = null;	retardAquaID = -1;	 isAquaRetarded = false;	
			
    		// SYSTEM CHECK - Notify that nothing to be scouted has been found
    		System.out.println("The tank cannot find anything to engage");    			

    		// Simply add a stride radius away from the initial location if possible.....
    		for (int i = 5; i >= 1; i--){
    			
    			// Get the distance to move away for..... and the resulting map location
    			float testDistance = strideRadius / 5 * i;	            			
    			MapLocation testLocation = myLocation.add(lastDirection, testDistance);
    			
    			// Check if the robot can move to the location and if it can, do so.....
    			if (rc.canMove(testLocation)){	      
    				
    				isCommanded = false;
    				goalLocation = null;
    				
    				// SYSTEM Check - Set LIGHT GREY LINE indicating where the tank would wish to go
        			rc.setIndicatorLine(myLocation, testLocation, 110, 110, 110);   
        			
    				return testLocation;	            			
    			}	            			
    		}    		
    		// If a move in the last direction was not possible, order the robot to go to original archon location
    		
    		if (archonLocation.distanceTo(myLocation) > 4){
    			setCommandLocation(archonLocation);
    		}
    		// If there was a valid point to go to...
    		if(isCommanded){    	
    			
    			// SYSTEM CHECK - Print out that the robot will now attempt to go to a goal location...
    			System.out.println("Attempting to move to last known location of the archon.....");

    			// Tell the robot to go towards the commanded location....		            			
    			return moveTowardsGoalLocation(enemyRobots, nearbyAllies, nearbyTrees);
    		}
    		else{
    			return myLocation;	            		
    		} 	
		}
    }
    
    // Function to follow a unit and approach it..... Similar to scout code but a tank will never stop following the robot..... 
    // A tank bot's job in life is to hunt down and kill what it is tracking... especially if the thing it is tracking likes Emilia
    
	private static MapLocation engage(RobotInfo[] enemyRobots, RobotInfo[] nearbyAllies, TreeInfo[] nearbyTrees) throws GameActionException{
		
		// If the robot can currently sense the robot it was tracking in the previous turn
    	if (rc.canSenseRobot(retardAquaID) && bakaAqua != null){
    		
    		// SYSTEM CHECK - See if the robot identifies that it is actually tracking something
    		System.out.println("Engaging a normie lover with ID: " + retardAquaID);
    		
    		// Update location of tracked robot 
    		bakaAqua = rc.senseRobot(retardAquaID);
    		
    		// If the enemy is a soldier - utilize the more complex engageSoldier function.....
    		if (bakaAqua.type == RobotType.SOLDIER){
    			
    			return Todoruno.chargeSoldier(myLocation, bakaAqua, strideRadius, nearbyAllies, nearbyTrees);
    		}
    		
    		// Otherwise if the enemy is a lumberjack or a tank...
    		else if (bakaAqua.type == RobotType.LUMBERJACK || bakaAqua.type == RobotType.TANK){
    		
	    		// SYSTEM CHECK - Draw a VIOLET LINE between current position and position of robot
	    		rc.setIndicatorLine(myLocation, bakaAqua.location, 150, 0, 200);
	    		
	    		// Attempt to move towards the new location.....
	    		return Todoruno.chargeSoldier(myLocation, bakaAqua, strideRadius, nearbyAllies, nearbyTrees);
	        	
    		}
    		
    		// If the enemy is a scout..... shouldn't happen tanks should not shoot scouts....
    		else if (bakaAqua.type == RobotType.SCOUT){    			
    			
    	 		// SYSTEM CHECK - Draw a FUSCHIA LINE between current position and position of robot - 
	    		rc.setIndicatorLine(myLocation, bakaAqua.location, 255, 0, 255);
	    		
	    		// Attempt to move towards the new location.....
	    		return Todoruno.engageCivilian(myLocation, bakaAqua, strideRadius);
    			
    		}
    		
    		// If the target is a gardener or archon.....
    		else{
    			
    			// SYSTEM CHECK - Draw a INDIGO LINE between current position and position of robot
	    		rc.setIndicatorLine(myLocation, bakaAqua.location, 75, 0, 130);
    			
    			return Todoruno.engageCivilian(myLocation, bakaAqua, strideRadius);  			
    		}
    		
        // If the robot has lost sight of its target....
    	} else {

    		// Reset the track ID and call the move function again to either get a new target or just move on.....
        	retardAquaID = -1;	isAquaRetarded = false;	bakaAqua = null;

        	// SYSTEM CHECK - Notify of target loss
        	System.out.println("Lost sight of target");        	
        	
        	// Simply add a stride radius away from the initial location if possible.....
    		for (int i = 5; i >= 1; i--){
    			
    			// Get the distance to move away for..... and the resulting map location
    			float testDistance = strideRadius / 5 * i;	            			
    			MapLocation testLocation = myLocation.add(lastDirection, testDistance);
    			
    			// Check if the robot can move to the location and if it can, do so.....
    			if (rc.canMove(testLocation)){	       
    				
    				// SYSTEM Check - Set LIGHT GREY LINE indicating where the tank would wish to go
        			rc.setIndicatorLine(myLocation, testLocation, 110, 110, 110);   
        			
    				return testLocation;	            			
    			}	            			
    		}    		
    		// If a move in the last direction was not possible, simply order the robot to remain still...		            		
    		
			// SYSTEM CHECK - Print out that the robot cannot move in its previous direction and will remain still...
			System.out.println("Cannot seem to move in the last direction traveled and no other commands issued.. Unit will not move");
			
			// Return the current location of the robot.......
			return myLocation;	    	
    	}	                		
    }	
	
    // Function to use when moving towards a certain location with a certain target.....
    
    private static MapLocation moveTowardsGoalLocation(RobotInfo[] enemyRobots, RobotInfo[] nearbyAllies, TreeInfo[] nearbyTrees) throws GameActionException{
    	
    	// If the robot has gotten close enough to the goal location, exit the command phase and do something else
    	if (myLocation.distanceTo(goalLocation) < 4 || roundsRouting >= giveUpOnRouting){
    		
    		// SYSTEM CHECK - Print out that the robot has gotten close to the desired location but did not find anything of note...
    		System.out.println("Tank has reached destination/ Failed to do so and given up.....");
    		
    		// Reset the rounds routing counter.....
    		roundsRouting = 0;
    		
    		// Reset the values necessary for switching into a command phase
    		goalLocation = null;
    		isCommanded = false;
    		
    		// Call the move function again...
    		return move(enemyRobots, nearbyAllies, nearbyTrees);
    	}
    	
    	else{
    		// SYSTEM CHECK - Inform that the robot is currently attempting to following a route to a goal destination.....    	
	    	System.out.println("Currently attempting to move to a goal location with x: " + goalLocation.x + " and y: " + goalLocation.y);
	    	
	    	// Otherwise, call the routing wrapper to get a new location to go to...
	    	Routing.routingWrapper();
	    	
	    	// Set the desired Move
	    	MapLocation desiredMove = Routing.path.get(0);
	    	
	    	// SYSTEM CHECK - Show desired move after path planning
	    	System.out.println("desiredMove from path planning: " + desiredMove.toString());
	    	
	    	return desiredMove;
    	}
    }    
    
    
	// ----------------------------------------------------------------------------------//
	// --------------------------- MISCELLANEOUS FUNCTIONS ------------------------------//
	// ----------------------------------------------------------------------------------//	
	
	// Function to obtain the robot info units in the specified team
	
	private static RobotInfo[] NearbyUnits(Team team, float distance){
	
	return rc.senseNearbyRobots(myLocation, distance , team);
	}
	
	
	// Function to set a command location to the location of the nearest archon......
	
	private static void setCommandLocation(MapLocation location) throws GameActionException{	
	
		// Parameters for a successful command initiation............
		// 1. Make sure that the turn number isn't equivalent to the clearing time of the broadcast....
		// 2. Make sure that the robot is not currently being commanded......
		// 3. Make sure that the robot has not been commanded for the last attackFrequency number of turns
		// 4. Make sure that the robot is not yet tracking anything.......
		
    	
		if(location == null){
			
			if (onlyIdiotsLikeAquaMoreThanMegumin % BroadcastChannels.BROADCAST_CLEARING_PERIOD != 1  && lastCommanded >= attackFrequency){
	
	       		// Attempt to read enemy archon data
	           	BroadcastChannels.BroadcastInfo newInfo = null;
	           	
	           	// If the robot is allowed to check for archon locations.....	           
	        	BroadcastChannels.BroadcastInfo archonInfo = BroadcastChannels.readEnemyArchonLocations();
	           	
	        	// Update archon information....
	           	if (archonInfo!= null){
	           		archonLocation = new MapLocation(archonInfo.xPosition, archonInfo.yPosition);
	           	}
	        	
	           	//If the tank is allowed to check for archon information...
	        	if (checkArchons){
	        		newInfo = archonInfo;
	           	}
	           	
	           	// If no archons are left or none have been found, read an enemy location instead...                   	
	           	if(newInfo == null){
	           		newInfo = BroadcastChannels.readEnemyLocations();
	           	}
	      
	           	
	           	// Pseudo random number for joining the attack....
	           	float willJoin = (float) Math.random();
	           	
	           	// If an archon has been seen before and the robot's pseudo random number falls within bounds to join an  attack, create the goal location
	           	// Make sure that the goal location is sufficiently far away - i.e. don't go if it is within sensor Radius....
	           	if (willJoin <= attackProbability && newInfo != null){ 
	           			
	           		// Obtain the location from the broadcast data
	           		MapLocation targetLocation = new MapLocation(newInfo.xPosition, newInfo.yPosition);    
	           		
	           		// Make sure that the robot is somewhat far away....
	           		if(myLocation.distanceTo(targetLocation) >= 3 * strideRadius){
	           		
		           		// The robot now has a command to follow, so will no longer track enemies continuously.....
		           		isCommanded = true;
		           		
		           		// Set the location of the target to go to as the data from the broadcast
		           		goalLocation = targetLocation;           	
		           		
		           		// Append the location to the routing...
		           		Routing.setRouting(goalLocation);           		
		               	
		           		// Reset the lastCommanded since the unit has now received a command
		           		lastCommanded = 0;
		           	}         		
		           	else{
		           		// Calculate the number of archons remaining on the enemy team (that the team has seen)                 
		           		int finishedArchons = rc.readBroadcast(BroadcastChannels.FINISHED_ARCHON_COUNT);
		           		int discoveredArchons = rc.readBroadcast(BroadcastChannels.DISCOVERED_ARCHON_COUNT);                   		
		           		
		           		// IF there are no more enemies to be found....... (as far as the team knows           		
		           		if(finishedArchons == discoveredArchons && rc.getInitialArchonLocations(enemies).length == finishedArchons){
		           			
		           			// SYSTEM CHECK - Print out that the robot will no longer seek archon locations...
		           			System.out.println("Number of archons killed is equivalent to the number seen, the robot will now simply check for nearby enemies....");
		           			
		           			checkArchons = false;
		           			isCommanded = false;
		               		goalLocation = null;          		
		           		}
	           		}
	           	}
	           	else{
		    		// Calculate the number of archons remaining on the enemy team (that the team has seen)                 
		       		int finishedArchons = rc.readBroadcast(BroadcastChannels.FINISHED_ARCHON_COUNT);
		       		int discoveredArchons = rc.readBroadcast(BroadcastChannels.DISCOVERED_ARCHON_COUNT);                   		
		       		
		       		// If it is near the beginning of the game... tell the robot to go to the location of the enemy archon.....
		       		if (discoveredArchons == 0 && onlyIdiotsLikeAquaMoreThanMegumin >= initRound + 2){
		       			
		       			// SYSTEM CHECK - Print out that the tank will be attempting to go to the initial archon location
		       			System.out.println("Attempting to go to the enemy archon location......");
		       			
		       			// The robot now has a command to follow, so will no longer track enemies continuously.....
		           		isCommanded = true;
		           		
		           		// Set the location of the target to go to as the data from the broadcast
		           		goalLocation = archonLocation;           	
		           		
		           		// Append the location to the routing...
		           		Routing.setRouting(goalLocation);           		
		               	
		           		// Reset the lastCommanded since the unit has now received a command
		           		lastCommanded = 0;	       			
		       		}
		       		
		       		
		       		// IF there are no more enemies to be found....... (as far as the team knows           		
		       		if(finishedArchons == discoveredArchons && rc.getInitialArchonLocations(enemies).length == finishedArchons){
		       			
		       			// SYSTEM CHECK - Print out that the robot will no longer seek archon locations...
		       			System.out.println("Number of archons killed is equivalent to the number seen, the robot will now simply check for nearby enemies....");
		       			
		       			checkArchons = false;
		       			isCommanded = false;
		           		goalLocation = null;          		
		       		}
	           	}
	    	}
		}
		// If no target location was inputted, default to the location of the archon initially...
		else{
 			// The robot now has a command to follow, so will no longer track enemies continuously.....
       		isCommanded = true;
       		
       		// Set the location of the target to go to as the data from the broadcast
       		goalLocation = location;     	
       		
       		// Append the location to the routing...
       		Routing.setRouting(goalLocation);           		
           	
       		// Reset the lastCommanded since the unit has now received a command
       		lastCommanded = 0;	       	
		}
	}	
	
	
	// Target selection and actual shooting decision function	
	
	private static boolean decideShoot(RobotInfo[] enemyRobots, RobotInfo[] alliedRobots, TreeInfo[] alliedTrees) throws GameActionException{
		
		// Obtain a location to shoot at
		MapLocation shootingLocation = Korosenai.getFiringLocation(bakaAqua, previousRobotData, lastPosition);
		
		// Return value to store whether or not the has fired this turn or no....
		boolean hasShot;
		
		// If there is more than one enemy nearby, attempt to fire a pentad at the location
		if(enemyRobots.length >= 2 || bakaAqua.type == RobotType.TANK || bakaAqua.type == RobotType.SOLDIER){
			
			// If a pentad can be shot...
			hasShot = Korosenai.tryShootAtEnemy(shootingLocation, lastPosition, 2, alliedRobots, alliedTrees, sensorRadius, bakaAqua);
			
			// If that was not possible, try a triad and then a single shot 
			if(!hasShot){
				hasShot = Korosenai.tryShootAtEnemy(shootingLocation, lastPosition, 1, alliedRobots, alliedTrees, sensorRadius, bakaAqua);
			}			
			if(!hasShot){
				hasShot = Korosenai.tryShootAtEnemy(shootingLocation, lastPosition, 0, alliedRobots, alliedTrees, sensorRadius, bakaAqua);
			}			
		}
		else if (bakaAqua.type != RobotType.ARCHON){
			// If a triad can be shot
			hasShot = Korosenai.tryShootAtEnemy(shootingLocation, lastPosition, 1, alliedRobots, alliedTrees, sensorRadius, bakaAqua);
			
			// If that was not possible, try a single shot 
			if(!hasShot){
				hasShot = Korosenai.tryShootAtEnemy(shootingLocation, lastPosition, 0, alliedRobots, alliedTrees, sensorRadius, bakaAqua);
			}		
		}
		else{
			hasShot = Korosenai.tryShootAtEnemy(shootingLocation, lastPosition,0, alliedRobots, alliedTrees, sensorRadius, bakaAqua);
		}
		
		if(hasShot){
			
			// SYSTEM CHECK - Print out Megumin quotes..........
			printOutMeguminQuotes();
			
			System.out.println("*MeguminBot Collapses");			
		}		
		return hasShot;
	}	
	

	// Function to check if the robot thinks it will die this turn and broadcast if it will.............
	
    public static void checkDeath(MapLocation location) throws GameActionException{
    	
    	// Boollean to store if the robot believes it will be hit if it moves to a certain location......
		boolean beingAttacked = iFeed.willBeAttacked(location);
		
		// If it will get hit from that location....
		if (beingAttacked) {
			
			// SYSTEM CHECK - Print out that the robot thinks it will die this turn....
			System.out.println("Moving to desired location will result in death........");
			
			// If the soldier will lose all of its health from moving to that location....
			boolean willDie = iFeed.willFeed(location);
			
			// If the soldier believes that it will die this turn....
			if (willDie) {
				
				// Set the belief variable to true.....
				believeHasDied = true;
				
				// Get the current number of soldiers in service
		        int currentTankNumber = rc.readBroadcast(BroadcastChannels.TANKS_ALIVE_CHANNEL);
		        
		        // Update soldier number for other units to see.....
		        rc.broadcast(BroadcastChannels.TANKS_ALIVE_CHANNEL, currentTankNumber - 1);

			}
		}
	}
    
    
    // Function to correct an accidental death update*
    
    public static void fixAccidentalDeathNotification() throws GameActionException{
    	
    	// Reset belief in the robot dying this round....
    	believeHasDied = false;    	

		// Get the current number of soldiers in service
        int currentTankNumber = rc.readBroadcast(BroadcastChannels.TANKS_ALIVE_CHANNEL);
        
        // Update soldier number for other units to see.....
        rc.broadcast(BroadcastChannels.TANKS_ALIVE_CHANNEL, currentTankNumber + 1);
    	
    }   	
    
    // Function to quote megumin!
    
    private static void printOutMeguminQuotes(){
    	
    	// Value to decide which quote to use
    	float randomize = (float) Math.random();    	
    	
    	// Standard Explosion quote
    	if(randomize <= 0.7){
    		
    		System.out.println("EXPLOSION!!!!!!");
    	}
    	else if (randomize <= 0.80){
    		
    		// Episode 2 English Translated Quote
    		System.out.println("Darkness blacker than black and darker than dark,");
    		System.out.println("I beseech thee, combine with my deep crimson.");
    		
    		System.out.println("The time of awakening cometh.");
    		
    		System.out.println("Justice, fallen upon the infallible boundary,");
    		System.out.println("appear now as an intangible distortions!");
    		
    		System.out.println("I desire for my torrent of power a destructive force:");
    		
    		System.out.println("a destructive force without equal!");
    		
    		System.out.println("Return all creation to cinders,");
    		System.out.println("and come frome the abyss!");
    		
    		System.out.println("EXPLOSION!");
    	}
    	else if (randomize <= 0.90){
    		
    		// Episode 3 English Translated Quote
    		System.out.println("Oh, blackness shrouded in light,");
    		System.out.println("Frenzied blaze clad in night,");
    		System.out.println("In the name of the crimson demons,");
    		System.out.println("let the collapse of thine origin manifest.");
    		
    		System.out.println("Summon before me the root of thy power hidden within the lands");
    		System.out.println("of the kingdom of demise!");
    		
     		System.out.println("EXPLOSION!");    		
    	}
    	else{
    		
    		// Episode 4 English Translated Quote
    		System.out.println("Crimson-black blaze, king of myriad worlds,");
    		System.out.println("though I promulgate the laws of nature,");
    		System.out.println("I am the alias of destruction incarnate");
    		System.out.println("in accordance with the principles of all creation.");
    		
    		System.out.println("Let the hammer of eternity descend unto me!");    		
    	}
    }	
}	
	
	


