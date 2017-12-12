//it is a (next hop, total distance) pair used in the distance vector
public class Pair {
	private int nextHop;
	private int distance;
	private int hopCount;
	//next hop of 0 represents the path to itself
	//next hop of -1 represents no path at all
	
	public Pair(int hop, int d, int h) {
		this.nextHop = hop;
		this.distance = d;
		this.hopCount = h;
	}
	
	
	public int getNextHop() {
		return nextHop;
	}
	
	public int getDistance() {
		return distance;
	}
	
	public int gethopCount() {
		return hopCount;
	}
	
	//value shouldn't be changed, create new pair for different distance hop pair
//	public void setNextHop(int a) {
//		nextHop = a;
//	}
//	
//	public void setDistance(int d) {
//		this.distance = d;
//	}
	
	public String toString() {
		//return "(router" + nextHop +"," +  distance + ", Hop Count:" + hopCount +")";
		return nextHop +","+ hopCount;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o == null) {
			return false;
		}
		if (!(o instanceof Pair)) {
			return false;
		}
		Pair other = (Pair)o;
		
		return this.nextHop == other.nextHop && this.distance == other.distance && this.hopCount == other.hopCount;
	}
}
