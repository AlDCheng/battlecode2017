package naclbot.variables;

import battlecode.common.GameActionException;
import naclbot.variables.GlobalVars;
import java.util.ArrayList;

public class DataVars extends GlobalVars{
	public static ArrayList<float[]> treeMapFormat = new ArrayList<float[]>();
	
	// Get number of units originating from given Archon
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
	public static class binarySearchTree{
	    public Node tree_root;
	    public int size;

		 	    // data is the intial data to be put in;
	    // maxsize is the maximum desired size of this Heap
	    public binarySearchTree(basicTreeInfo[] data){	    	
	
	        this.tree_root = new Node(data[0].ID, data[0], null, null, null);
	        
	        for (int i = 1; i < data.length; i++){
	        	insert(data[i], tree_root);
	        }
	        
	 
	    }
	    
	    // returns a node if the value is there; 
	    private  Node search(int key, Node root){
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
	    		//System.out.print("Node: " + root.key + "Data_x: " + root.data.x + "Data_y: " + root.data.y + "Radius: " + root.data.radius);
	    		System.out.println(root.key + "," + root.data.x + "," + root.data.y + "," + root.data.radius);
		        //System.out.println();
		        printInOrder(root.rightChild);	    		
	    	}
	    }
	}	
	


	public static void updateTrees(binarySearchTree yahallo) throws GameActionException{
		// Reset trees to add
		treeMapFormat = new ArrayList<float[]>();
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
					
					// Add to list of treees
					float[] dataRow = new float[3];
					dataRow[0] = x_1;
					dataRow[1] = y_1;
					dataRow[2] = radius_1;
					treeMapFormat.add(dataRow);
				}
				if (sent_number > 1){
					int ID_2 = rc.readBroadcast(5 + SCOUT_CHANNEL + i * SCOUT_MESSAGE_OFFSET);
					int x_2 = rc.readBroadcast(6+ SCOUT_CHANNEL + i * SCOUT_MESSAGE_OFFSET);
					int y_2 = rc.readBroadcast(7 + SCOUT_CHANNEL + i * SCOUT_MESSAGE_OFFSET);
					int radius_2 = rc.readBroadcast(8 + SCOUT_CHANNEL + i * SCOUT_MESSAGE_OFFSET);
					basicTreeInfo tree2 = new basicTreeInfo(ID_2, x_2, y_2, radius_2);
					
					yahallo.insert(tree2, yahallo.tree_root);
					
					// Add to list of treees
					float[] dataRow = new float[3];
					dataRow[0] = x_2;
					dataRow[1] = y_2;
					dataRow[2] = radius_2;
					treeMapFormat.add(dataRow);
				}
			
	
			}
		}
	}
}
