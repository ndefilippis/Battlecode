package turtle;

import java.util.ArrayList;

import battlecode.common.*;

public class Utility {


	public static RobotInfo[] combine(RobotInfo[] o1, RobotInfo[] o2){
		RobotInfo[] ret = new RobotInfo[o1.length + o2.length];
		int index = 0;
		for(RobotInfo o : o1){
			ret[index] = o;
			index++;
		}
		for(RobotInfo o : o2){
			ret[index] = o;
			index++;
		}
		return ret;
	}


	//Returns whether the Direction is one of the three Directions in the relatively opposite Direction
	public static boolean oppositeish(Direction d1, Direction d2){
		Direction dTest = d1.opposite();
		return d2 == dTest || d2 == dTest.rotateRight() || d2 == dTest.rotateLeft();
	}

	public static boolean isBlocked(RobotController rc, MapLocation loc) throws GameActionException{
		return !rc.onTheMap(loc) || rc.senseRubble(loc) >= 100 || rc.senseRobotAtLocation(loc) != null;
	}


	public static RobotInfo getRobotWithLowestHP(RobotInfo[] nearbyRobots) {
		RobotInfo lowestRobot = null;
		double lowest = 99999.0;
		for(RobotInfo ri : nearbyRobots){
			if(ri.health < lowest){
				lowest = ri.health;
				lowestRobot = ri;
			}
		}
		return lowestRobot;
	}
	
	public static MapLocation closestLocation(ArrayList<MapLocation> locations, MapLocation location){
		int minDistance = 100000;
		MapLocation lowest = null;
		for(MapLocation loc : locations){
			if(loc.distanceSquaredTo(location) < minDistance){
				minDistance = loc.distanceSquaredTo(location);
				lowest = loc;
			}
		}
		return lowest;
	}
	
	public static int getClosestRound(ZombieSpawnSchedule zss){
		for(int i : zss.getRounds()){
			if(i > BaseRobot.rc.getRoundNum()){
				return i;
			}
		}
		return 3000;
	}

	public static RobotInfo closest(RobotInfo[] robots, MapLocation location) {
		int minDistance = 100000;
		RobotInfo lowest = null;
		for(RobotInfo ri : robots){
			if(ri.location.distanceSquaredTo(location) < minDistance){
				minDistance = ri.location.distanceSquaredTo(location);
				lowest = ri;
			}
		}
		return lowest;
	}
}
