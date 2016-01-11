package scoutsAndSoldiers;
import battlecode.common.*;

public class Action implements Comparable{
	MapLocation location;
	MapLocation goal;
	MyActionType type;
	double cost;
	public Action cameFrom;
	
	public enum MyActionType {
		MOVE,
		DIG,
		YIELD,
	}

	public Action(MapLocation location, MapLocation goal, MyActionType type, double cost){
		this.location = location;
		this.type = type;
		this.cost = cost;
		this.goal = goal;
	}

	public double mannhattan(){
		return Math.sqrt(location.distanceSquaredTo(goal));
	}

	@Override
	public int compareTo(Object o) {
		Action a = (Action)o;
		return (int)((this.cost+this.mannhattan()) - (a.cost+a.mannhattan()));
	}

	public String toString(){
		return location + " " + type + " " + (cost+this.mannhattan());
	}

}
