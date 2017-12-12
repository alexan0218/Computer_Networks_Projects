import java.util.ArrayList;
import java.util.HashMap;

public class Router {
	
	int routerNumber;
	
	Pair[][] table;  //table of distance vectors that stores distance information to all other routers;
	
//	int[] vector;  //distance vector that router would sent out to it's neighbors
	int size;  //overall number of routers
	
	HashMap<Integer, Integer> neighbors;  //neighboring routers with edge dist
	
	HashMap<Integer, Pair[]> vector_from_neighbors;  //information received from its neighbors
	
	public Router(int num, int size) {
		routerNumber = num;
		this.size = size;
		table = new Pair[size][size];
		neighbors = new HashMap<>();
		vector_from_neighbors = new HashMap<>();
	}
	
	
	//to initialize the 
	public void initialize() {
		//initialize the table for this router based only on the edges coming out of it
		for (int router : neighbors.keySet()) {
			int dist = neighbors.get(router);
			Pair p = new Pair(router, dist, 1);
			updateTable(this.routerNumber, router, p);
		}
		//also initialize the distance to itself to 0
		Pair self = new Pair(this.routerNumber,0,0);
		updateTable(this.routerNumber, this.routerNumber, self);
	}
	
	public void edgeUpdate(int r, int dist) {
		//first change the edges in neighbors
		//int oldDist = neighbors.get(r);
		if (dist == -1) {
			//deleting the edge
			neighbors.remove(r);
			
			//modify the table
			//if it pass through r to reach destination, change it to null
//			Pair[] old = table[routerNumber];
//			for (int i = 1; i < old.length; i++) {
//				//if it uses this modified edge to reach to the destination, change it
//				Pair p = old[i];
//				if (p != null && p.getNextHop() == r) {
//					old[i] = null;
//				}
//				
//			}
			
		} else {
			//change the distance attribute
			neighbors.put(r, dist);
			
			//modify the table;
//			Pair[] old = table[routerNumber];
//			for (int i = 1; i < old.length; i++) {
//				//if it uses this modified edge to reach to the destination, change it
//				Pair p = old[i];
//				int changedDist = dist - oldDist;
//				if (p != null && p.getNextHop() == r) {
//					//this router uses r as next hop to reach destination
//					Pair modified = new Pair(r, changedDist + p.getDistance());
//					old[i] = modified;
//				}
//				
//			}
		}

		
	}
	
//	public boolean runPoison() {
//		
//	}
//	
//	public boolean runSplit() {
//		
//	}
	
	public boolean run() {
		//return whether the table for it's own ROW has changed
		//update the table based on information received in vector_from_neighbors;
		//the main distance vector algorithm
		boolean changed = false;  //check whether it's own row is modified
		
		//create a new vector and after updating all the pair, insert it to the window
		Pair[] newVector = new Pair[size];
		Pair[] oldVector = table[routerNumber];
		for (int i = 1; i < size; i++) {
			//r = routerNumber,  dest = i, neighbors = n
			//min(edge to n + c(n,d))
			if (i == routerNumber) {
				//to itself
				newVector[i] = new Pair(this.routerNumber,0, 0);
			} else {
				//to another router
				Pair current = null;
				//compare with current to get the shortest value
				
				for (Integer r: vector_from_neighbors.keySet()) {
					int edge = neighbors.get(r);
					Pair nd = vector_from_neighbors.get(r)[i];   //pair from neighbor to desti
					if (nd != null) {
						if (current == null || current.getDistance() > edge + nd.getDistance()) {
							//update
							current = new Pair(r, nd.getDistance() + edge, nd.gethopCount() + 1);
						}
					}
					//if nd is null, then no update since no path exist from that neighbor	
				}
				//System.out.println("Current: " + current);
				newVector[i] = current;
				
				
			}
		}
		
		//check original vector to see whether update happened
		for (int i = 1; i < size; i++) {
			if ((oldVector[i] == null && newVector[i] != null) || (oldVector[i] != null && newVector[i] == null)) {
				changed = true;
			} else if (oldVector[i] == null && newVector[i] == null) {
				changed = false;
			} else if (!oldVector[i].equals(newVector[i])) {
				changed = true;
			}
		}
		
		for (int index = 1; index < size; index++) {
			Pair[] v = vector_from_neighbors.get(index);
			Pair[] empty = new Pair[size];
			if (v == null) {
				table[index] = empty;
			} else {
				table[index] = v;
			}
		}
		
		table[routerNumber] = newVector;
		
		for (Integer n : vector_from_neighbors.keySet()) {
			table[n] = vector_from_neighbors.get(n);
		}
		
		vector_from_neighbors = new HashMap<Integer, Pair[]>();
		return changed;
	}
	
	public boolean runPoison() {
		//return whether the table for it's own ROW has changed
		//update the table based on information received in vector_from_neighbors;
		//the main distance vector algorithm
		boolean changed = false;  //check whether it's own row is modified
		
		//create a new vector and after updating all the pair, insert it to the window
		Pair[] newVector = new Pair[size];
		Pair[] oldVector = table[routerNumber];
		for (int i = 1; i < size; i++) {
			//r = routerNumber,  dest = i, neighbors = n
			//min(edge to n + c(n,d))
			if (i == routerNumber) {
				//to itself
				newVector[i] = new Pair(this.routerNumber,0, 0);
			} else {
				//to another router
				Pair current = null;
				//compare with current to get the shortest value
				
				for (Integer r: vector_from_neighbors.keySet()) {
					int edge = neighbors.get(r);
					Pair nd = vector_from_neighbors.get(r)[i];   //pair from neighbor to desti
					if (nd != null) {
						if ((current == null || current.getDistance() > edge + nd.getDistance()) && (nd.getDistance() != -1)) {
							//update
							current = new Pair(r, nd.getDistance() + edge, nd.gethopCount() + 1);
						}
					}
					//if nd is null, then no update since no path exist from that neighbor	
				}
				newVector[i] = current;
				
				
			}
		}
		
		//check original vector to see whether update happened
		
		for (int i = 1; i < size; i++) {
			if ((oldVector[i] == null && newVector[i] != null) || (oldVector[i] != null && newVector[i] == null)) {
				changed = true;
			} else if (oldVector[i] == null && newVector[i] == null) {
				changed = false;
			} else if (!oldVector[i].equals(newVector[i])) {
				changed = true;
			}
		}
		
		for (int index = 1; index < size; index++) {
			Pair[] v = vector_from_neighbors.get(index);
			Pair[] empty = new Pair[size];
			if (v == null) {
				table[index] = empty;
			} else {
				table[index] = v;
			}
		}
		
		table[routerNumber] = newVector;
		
		for (Integer n : vector_from_neighbors.keySet()) {
			table[n] = vector_from_neighbors.get(n);
		}
		
		vector_from_neighbors = new HashMap<Integer, Pair[]>();
		return changed;
	}
	
	
	public void addNeighbors(int n, int d) {
		//can also be used to update distance of edges
		neighbors.put(n, d);
	}
	
	public HashMap<Integer, Integer> getNeighbors() {
		return neighbors;
	}
	
	
//	public int[] getVector() {
//		return vector;
//	}
//	
//	public void setVector(int[] v) {
//		vector = v;
//	}
	
	public void updateTable(int x, int y, Pair p) {
		//set the table at position x, y
		table[x][y] = p;
	}
	
	public Pair[][] getTable() {
		return table;
	}
	
	
	public void receive(int n, Pair[] vector) {
		vector_from_neighbors.put(n, vector);
	}
	
	//basic protocol
	public Pair[] getBasic(int x) {
		//return the specific vector that this router will send to router x which comes from its local table
		Pair[] result = new Pair[size];
		for (int i = 0; i < result.length;i++) {
			result[i] = table[routerNumber][i];
		}
		return result;
	}
	
	public Pair[] getPoison(int x) {
		Pair[] result = new Pair[size];
		for (int i = 1; i < result.length;i++) {
			if (table[routerNumber][i] == null) {
				result[i] = null;
			} else if(table[routerNumber][i].getNextHop() == x) {
				result[i] = new Pair(-1, -1, -1);
			} else {
				result[i] = table[routerNumber][i];
			}			
		}

		return result;
	}

	public Pair[] getSplit(int x) {
		Pair[] result = new Pair[size];
		for (int i = 1; i < result.length;i++) {
			if (table[routerNumber][i] == null) {
				result[i] = null;
			} else if(table[routerNumber][i].getNextHop() == x) {
				result[i] = null;
			} else {
				result[i] = table[routerNumber][i];
			}
		}

		return result;
	}
	
}
