package naclbot.variables;

import battlecode.common.*;
import java.util.ArrayList;

public class GlobalVars {
	public static RobotController rc;
	
	public static int ARCHON_CHANNEL;
	public static int ARCHON_OFFSET;
	public static int ARCHON_LIMIT;
	
	public static int GARDENER_CHANNEL;
	public static int GARDENER_BUILDER_CHANNEL;
	public static int GARDENER_WATERER_CHANNEL;
	
	public static int LUMBERJACK_CHANNEL;	
	
	public static int SCOUT_CHANNEL;
	public static int SCOUT_MESSAGE_OFFSET;
	public static int SCOUT_LIMIT;
	public static int SCOUT_UPDATE_FREQUENCY;
	public static int SCOUT_TRACKING;
	
	public static int TANK_CHANNEL;
	
	
	public static int TREE_DATA_CHANNEL;
	public static int TREE_OFFSET;
	public static int GROUP_CHANNEL;
	public static int GROUP_CHANNEL_OFFSET;
	public static int GROUP_COMMUNICATE_OFFSET;
	public static int GROUP_LEADER_START;
	public static int GROUP_LEADER_OFFSET;
	public static int GROUP_START;
	public static int GROUP_OFFSET;
	public static int GROUP_LIMIT;
	
	// Internal map variables
	public static ArrayList<ArrayList<Integer>> internalMap = new ArrayList<ArrayList<Integer>>();
	public static RobotType unitType;
	public static float robotRadius;
	public static MapLocation centerCoords;
	public static int offsetX, offsetY;
	public static int SOLDIER_CHANNEL;
	
	public static void globalInit(RobotController _RC) {
		rc = _RC;
		
		/* --------------------------------------------------------------------
		 * -------------------------- Internal Map ----------------------------
		-------------------------------------------------------------------- */
		ArrayList<Integer> zeroList = new ArrayList<Integer>();
		zeroList.add(0);
		internalMap.add(zeroList);
		
		unitType = rc.getType();
		robotRadius = unitType.bodyRadius;
		
		centerCoords = rc.getLocation();
		offsetX = 0;
		offsetY = 0;
		
		/* --------------------------------------------------------------------
		 * --------- Broadcast Channel Setup and Unit Organisation -----------
		-------------------------------------------------------------------- */
		
		// Archons
		
		ARCHON_CHANNEL = 0; // Carries number of living Archons
		
		ARCHON_OFFSET = 12;
		// Offset 1: Current X Position
		// OFfset 2: Current Y Position
		
		ARCHON_LIMIT = 3;

		// Gardeners 		
		GARDENER_CHANNEL = 100; // Carries number of living Gardeners
		GARDENER_BUILDER_CHANNEL = 101; // Carries number of living Gardeners designed as unit builders
		GARDENER_WATERER_CHANNEL = 102; // Carries number of living Gardeners designed as waterers
		
		// Scouts
		SCOUT_CHANNEL = 43; // Carries number of scouts
				
		SCOUT_MESSAGE_OFFSET = 11;

		// Offset 1:  Current X Position
		// Offset 2:  Current Y Position
		// Offset 3-8:  Message bits
		// Offset 9: ID Broadcast
		// Offset 10: Message type identifier
			// Type 0: Clear Message only ID and 0 type transmit - means ignore everything  to some functions
			// Type 1: Regular transmission of location/id/nearest 
			// Type 2: Transmission of sudoku - many enemies here
			// Type 3: Transmission of tree data
			// Type 4: Update of tracked object
			// ...
		
		SCOUT_LIMIT= 5; // Limit to number of Scouts

		SCOUT_UPDATE_FREQUENCY = 4; // How often Scouts regularly display that they are alive
		
		SCOUT_TRACKING = SCOUT_CHANNEL + SCOUT_LIMIT * SCOUT_UPDATE_FREQUENCY + 1;
		
		LUMBERJACK_CHANNEL = 140;
		
		/* Scout Channel is the placeholding value foir all scout channels. 
		 * The broadcasts at this number contain only the number of scouts currently available to the team		
		*/
		TANK_CHANNEL = 180;
		
		SOLDIER_CHANNEL = 160;
		
		GROUP_CHANNEL = 200;
		GROUP_COMMUNICATE_OFFSET =2;
		GROUP_CHANNEL_OFFSET = 20;
		GROUP_LIMIT = 5;
		
		GROUP_LEADER_OFFSET = 2;
		GROUP_LEADER_START = GROUP_CHANNEL + GROUP_COMMUNICATE_OFFSET * GROUP_LIMIT;
		GROUP_START = GROUP_LEADER_START + GROUP_LEADER_OFFSET * GROUP_LIMIT;
		GROUP_OFFSET = 20;

		TREE_DATA_CHANNEL = 400; 
		TREE_OFFSET = 4;
		//Offset 0: Tree ID
		//Offset 2: Tree X Position
		//Offset 3: Tree Y Position
		//Offset 4:  Something Else
	}
	
	// Updates map for trees
	// 0 = empty
	// 1 = tree
	// 2 = friendly unit
	// 3 = enemy unit
	
	// need radius
	// treeSpec format [0] x; [1] y; [2]; r
	public static void updateMapTrees(float[][] treeSpecs) {
		
		// Get offset of object position to origin (centerCoords)
		for (int k = 0; k < treeSpecs.length; k++) {
			// Get tree properties from ID
			try {
				
				// Calculate displacement from origin
				float newObjOffsetX = treeSpecs[k][0] - centerCoords.x;
				float newObjOffsetY = treeSpecs[k][1] - centerCoords.y; 
				
				// Convert raw offset to tiles
				// Each tile is the same width as the unit creating this map
				int tileOffsetCenterX = (int)(newObjOffsetX/robotRadius);
				int tileOffsetCenterY = (int)(newObjOffsetY/robotRadius);
				
				// Calculate radius of object in grid
				// We will fill with square hit box for now
				int tileRadius = (int)(treeSpecs[k][2]/robotRadius);
				
				// Loop to fill all tiles covered by radius
				for (int tileOffsetX = tileOffsetCenterX-tileRadius; 
						tileOffsetX <= tileOffsetCenterX+tileRadius; tileOffsetX++) {
					for (int tileOffsetY = tileOffsetCenterY-tileRadius; 
							tileOffsetY <= tileOffsetCenterY+tileRadius; tileOffsetY++) {

						// Insert tree in map by dynamically resizing it
						// i = row; j = col
						// Case 1: X position (extend ArrayList)
						// - Condition 1: left of origin (-offset_x)
						if ((tileOffsetX-offsetX) < 0) {
							// Pad 0s to map for each row
							for (int i = 0; i < internalMap.size(); i++) {
								for (int j = 0; j < (-1*tileOffsetY); j++) {
									internalMap.get(i).add(0, 0);
								}
							}
							// Set offset from original origin
							// i.e. offsetX = -2 means (0 - (-2))=2 gets location of origin
							offsetX += tileOffsetX;
						}
						// - Condition 2: right of internal map boundaries
						// Pad 0s to map for each row
						else if ((tileOffsetX-offsetX) > internalMap.size()) {
							for (int i = 0; i < internalMap.size(); i++) {
								for (int j = 0; j < (-1*tileOffsetY); j++) {
									internalMap.get(i).add(0);
								}
							}
						}
						
						// Case 2: Y position (create new ArrayList)
						// - Condition 1: above the origin (-offset_y)
						ArrayList<Integer> newRow = new ArrayList<Integer>();
						if ((tileOffsetY-offsetY) < 0) {
							// Pad 0s until point
							for (int j = 0; j < internalMap.get(0).size(); j++) {
								newRow.add(0);
							}
							for (int i = 0; i < (-1*(tileOffsetY-offsetY))-1; i++) {
								ArrayList<Integer> newRowUnlinked = new ArrayList<Integer>(newRow);
								internalMap.add(0, newRowUnlinked);
							}
							
							//Add row
							ArrayList<Integer> insertRow = new ArrayList<Integer>(newRow);
							insertRow.set((tileOffsetX-offsetX), 1);
							internalMap.add(0, insertRow);
							
							// Set offset from original origin
							// i.e. offsetY = -2 means (0 - (-2))=2 gets location of origin
							offsetY += (tileOffsetY-offsetY);
						}
						// - Condition 2: below the internal map boundaries
						else if ((tileOffsetY-offsetY) > internalMap.size()-1) {
							// Pad 0s until point
							for (int j = 0; j < internalMap.get(0).size(); j++) {
								newRow.add(0);
							}
							for (int i = 0; i < (-1*(tileOffsetY-offsetY))-1; i++) {
								ArrayList<Integer> newRowUnlinked = new ArrayList<Integer>(newRow);
								internalMap.add(newRow);
							}
							
							//Add row
							ArrayList<Integer> insertRow = new ArrayList<Integer>(newRow);
							insertRow.set((tileOffsetX-offsetX), 1);
							internalMap.add(insertRow);
						}
						// - Condition 3: within internal map boundaries
						else
						{
							internalMap.get(tileOffsetY-offsetY).set((tileOffsetX-offsetX), 1);
						}
						
					}
				}
				
			} catch(Exception e) {
				System.out.println("InternalMapTreeAdd: TreeInfo returns error");
				e.printStackTrace();
			}
		}
	}
	
	
	// Checks to see if an array of integers contains the integer value and outputs true if so; outputs false otherwise
	public static boolean arrayContainsInt(int[] array, int value){
		for (int i = 0; i < array.length; i ++){
			 if (array[i] == value){
				 return true;
			 }
		}
		return false;
	}			
	
	// Checks to see if an array of integers contains the integer value and outputs the index of the value if it exists or -1 if it doesn't
	public static int arrayContainsIndex(int[] array, int value){
		int j = -1;
		for (int i = 0; i < array.length; i ++){
			 if (array[i] == value){
				 j = i;
			 }
		}
		return j;
	}			
	// Heap useful for finding the maximum for minimum negate all input values
	public static class MaxHeap{
		
	    private int[] Heap;
	    private int size;
	    private int maxsize;
	 
	    private static final int root = 1;
	 
	    // data is the intial data to be put in;
	    // maxsize is the maximum desired size of this Heap
	    MaxHeap(int[] data, int maxsize){	    	
	
	        this.maxsize = maxsize;
	        this.size = 0;
	        
	        Heap = new int[this.maxsize + 1];
	        for (int i = 0; i < data.length; i++){
	        	Heap[i+1] = data[i];
	        	size += 1;
	        }
	        
	        for (int j = data.length/2; j>0; j--){
	        	maxHeapify(j);
	        }
	        
	    }
	 
	    private int parent(int child) {
	    	if (child == 1){
	    		return -1;
	    	} else{	    
	    		return child / 2;
	    	}
	    }
	 
	    private int leftChild(int parent) {
	       if (2*parent <= size){
	    	   return 2 * parent;
	       } else{
	    	   return -1;
	       }
	    }
	 
	    private int rightChild(int parent) {
	        if (2 * parent + 1 <= size){
		    	   return 2 * parent + 1;
		       } else{
		    	   return -1;
		       }
	    }
	 
	    // Checks if the current index is the index of a leaf of the heap
	    private boolean isLeaf(int index) {
	        if (index >=  (size / 2)  &&  index <= size) {
	            return true;
	        }
	        return false;
	    }
	    
	    private void increaseKey(int index, int key){
	    	Heap[index] = key;
	    	while (index > 1 && Heap[parent(index)] < Heap[index]){
	    		swap(index, parent(index));
	    		index = parent(index);
	    	}
	    }
	    
	    // swaps to heap entries
	    private void swap(int initial,int fin){
	        int temp = Heap[initial];
	        Heap[initial] = Heap[fin];
	        Heap[fin] = temp;
	    }
	 
	    private void maxHeapify(int index) {
	    	int largest = Integer.MIN_VALUE;
	        if (!isLeaf(index)) { 	        	
	            if (Heap[index] < Heap[leftChild(index)]){
	            	largest = leftChild(index);
	            } else{
	            	largest = index;
	            }
	            if (Heap[largest] < Heap[rightChild(index)]){
	            	largest = rightChild(index);
	            }
	            if (largest != index){
	            	swap(index,largest);
	            	maxHeapify(largest);
	            }
	        }
	    }
	    

	 
	    public void insert(int key) {
	    	size += 1;
	        Heap[size] = Integer.MIN_VALUE;
	        increaseKey(size, key);
	    }	
	 
	    public void print() {
	        for (int i = 1; i <= size / 2; i++ )
	        {
	            System.out.print(" Parent : " + Heap[i] + " Left Child : " + Heap[2*i]
	                  + " Right Child :" + Heap[2 * i  + 1]);
	            System.out.println();
	        }
	    }
	 

	    public int extractMax() {
	        int value = Heap[root];
	        Heap[root] = Heap[size]; 
	        size -= 1;
	        maxHeapify(root);
	        return value;
	    }    
	}
	
	
	public static class basicTreeInfo{
	public int ID;
	public int x;
	public int y;
	public int radius;
	public basicTreeInfo(int identifier, int xpos, int ypos, int fatness){
		this.ID = identifier;
		this.x = xpos;
		this.y = ypos;
		this.radius = fatness;
	}
}


	public static class Node{
		public int key;
		public basicTreeInfo data;
		public Node leftChild = null;
		public Node rightChild = null;
		public Node parent = null;
		
		Node(int value, basicTreeInfo newdata, Node leftC, Node rightC, Node ryoushin){
			this.key = value;
			this.data = newdata;
			this.leftChild = leftC;
			this.rightChild = rightC;
			this.parent = ryoushin;
		}
}


// Only for positive values!
	public static class BinarySearchTree{
	    public Node tree_root;
	    public int size;

		 	    // data is the intial data to be put in;
	    // maxsize is the maximum desired size of this Heap
	    public BinarySearchTree(basicTreeInfo[] data){	    	
	
	        this.tree_root = new Node(data[0].ID, data[0], null, null, null);
	        System.out.print("tree_root_ID: " + tree_root.key);
	        for (int i = 1; i < data.length; i++){
	        	insert(data[i], tree_root);
	        }
	        
	 
	    }
	    
	    // returns a node if the value is there; 
	    private Node search(int key, Node root){
	    	Node done = null;
	    	if (key == root.key){
	    		return root;
	    	} else if (key < root.key){
	    		if (root.leftChild == null){	    
	    		} else{
	    			done = search(key, root.leftChild);
	    		}
	    	} else {
	    		if (root.rightChild == null){
	    		} else{
	    			done = search(key, root.rightChild);
	    		}	    		
	    	}
	    	return done;
	    }
	    
	    public void insert(basicTreeInfo data, Node root){
	    	int key = data.ID;
	    	 System.out.print("inserting: " + data.ID);
	    	
	    	if(search(key, root) == null){
	    		if (key > root.key){
	    			if (root.rightChild != null){
	    				insert(data, root.rightChild);
	    				
	    			} else{
	    				root.rightChild = new Node(key, data, null, null, root);
	
	    				size +=1;
	    			}
	    		}
	    		else if (key < root.key){
	    			if (root.leftChild != null){
	    				insert(data, root.leftChild);
	    				
	    			} else{
	    				root.leftChild = new Node(key, data, null, null, root);

	    				size +=1;
	    			}		
    			
	    		}
	    	}

	    }
	    
	    private Node findMin(Node root){
	    	while (root.leftChild != null){
	    		root = root.leftChild;
	    	}
	    	return root;
	    }
	    

	    
	    
	    //pops element out of tree and returns it
	    public basicTreeInfo delete(int key, Node root){

	    	Node found = search(key, root);

	    	
	    	if (found != null){
	    		if (found.leftChild == null){
	    			
	    			// If node has no children
	    			if (found.rightChild == null){
	    				if (found.key < found.parent.key){
	    					found.parent.leftChild = null;
	    				} else{
	    					found.parent.rightChild = null;
	    				}
	    				return found.data;
	    			}
	    			// If only node's right Child is present
	    			else{
	    				if (found.key < found.parent.key){
	    					found.parent.leftChild = found.rightChild;
	    					found.rightChild.parent = found.parent;
	    	
	    				} else{
	    					found.parent.rightChild = found.rightChild;
	    					found.rightChild.parent = found.parent;
	    					
	    				}
	    				return found.data;
	    			}
	    		}
	    			// If only left child is present
    			if(found.rightChild == null){
    				if (found.key < found.parent.key){
    					found.parent.leftChild = found.leftChild;
    					found.leftChild.parent = found.parent;
  
    				} else{
    					found.parent.rightChild = found.leftChild;
    					found.leftChild.parent = found.parent;
    		
    				}
    				return found.data;
    			}
    			 // If both children are present
    			else{
    				Node right = found.rightChild;
    				Node move = findMin(right);
    				basicTreeInfo temp = move.data;
    				basicTreeInfo store = found.data;
    				delete(move.key, root);
    				
    				
    				found.key = temp.ID;
    				found.data = temp;
    				
    				return store;		   				  					    			
	    		}    		
	      	}
	    	else{
	    		return null;
	    	}
	    }
	 
	 
	 
	    public void printInOrder(Node root){
	    	if (root!=null){
	    		printInOrder(root.leftChild);
	    		System.out.print("Node: " + root.key + "Data_x: " + root.data.x + "Data_y: " + root.data.y + "Radius: " + root.data.radius);
		        System.out.println();
		        printInOrder(root.rightChild);	    		
	    	}
	    }
	}
	
	
	public class Tuple{ 
		public final int X; 
		public final int Y; 
		public Tuple(int x, int y) { 
			this.X = x; 
		    this.Y = y; 
		} 
		public void printData(){
			System.out.print( "X: "+ X + "Y: "+ Y);
			System.out.println();
			
		}
	} 

}





