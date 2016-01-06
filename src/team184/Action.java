package team184;
import battlecode.common.*;

public class Action implements Comparable{
	MapLocation location;
	MapLocation goal;
	MyActionType type;
	double cost;
	public Action cameFrom;
	
	public Action(MapLocation location, MapLocation goal, MyActionType type, double cost){
		this.location = location;
		this.type = type;
		this.cost = cost;
		this.goal = goal;
	}
	
	public int mannhattan(){
		return Math.abs(location.x-goal.x)+Math.abs(location.y+goal.y);
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
