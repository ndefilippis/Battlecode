package team184;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Signal;
import battlecode.common.Team;
/*Base Robot class for implementing the types of robots
 * Begins with the startLoop method, which should not exit 
 * 
 * 
 * 
 */
public abstract class BaseRobot {
	protected static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
			Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	static int[] tryDirections = {0,-1,1,-2,2}; //TODO put in navigation class
	protected static RobotController rc;
	protected Team myTeam;
	protected MapLocation goalLocation;
	protected MapLocation nearestArchonLocation;
	protected Random random;

	public BaseRobot(RobotController rc){
		BaseRobot.rc = rc;
		myTeam = rc.getTeam();
		random = new Random(rc.getID());
		int birth = rc.getRoundNum();
	}

	public void initialize() throws GameActionException{
		RobotInfo[] nearbyRobots = rc.senseNearbyRobots(2, myTeam);
		for(RobotInfo ri : nearbyRobots){
			if(ri.type == RobotType.ARCHON){
				nearestArchonLocation = ri.location;
			}
		}
	}

	public void startLoop() throws GameActionException{
		initialize();

		while(true){
			prerun();
			try {
				run();
			} catch (GameActionException e) {
				e.printStackTrace();
			}
			postrun();
		}
	}

	protected void postrun() throws GameActionException{
		Clock.yield();
	}

	public void run() throws GameActionException{}

	protected void prerun() throws GameActionException{
		Signal[] signals = rc.emptySignalQueue();
		for(Signal s : signals){
			if(s.getTeam() == myTeam && s.getMessage() != null){
				MessageSignal ms = new MessageSignal(s);
				if(ms.getMessageType() == MessageSignal.MessageType.COMMAND){
					goalLocation = ms.getPingedLocation();
					if(nearestArchonLocation == null || s.getLocation().distanceSquaredTo(rc.getLocation()) < nearestArchonLocation.distanceSquaredTo(rc.getLocation())){
						nearestArchonLocation = s.getLocation();
					}
					if(rc.getType() != RobotType.ARCHON && rc.getRoundNum() < 75)
						goalLocation = s.getLocation().add(nearestArchonLocation.directionTo(rc.getLocation()), 5);
				}
			}
		}
	}


	/**
	 * Looks for attackable robot and attacks
	 * If it cannot attack, it finds the best path and moves
	 * 
	 * Otherwise, it randomly moves if no robots are in range
	 * 
	 * @throws GameActionException
	 */
	protected void defaultBehavior() throws GameActionException {
		RobotInfo[] ri = rc.senseHostileRobots(rc.getLocation(), rc.getType().sensorRadiusSquared);
		RobotInfo[] attackable = rc.senseHostileRobots(rc.getLocation(), rc.getType().attackRadiusSquared);
		RobotInfo sense = Utility.closest(ri, rc.getLocation());
		if (sense != null) {
			if(rc.isWeaponReady()){
				double lowestHealth_dps = 99999;
				RobotInfo bestTarget = null;
				for(RobotInfo enemy : attackable){
					if(!enemy.type.canAttack() && bestTarget != null){
						continue;
					}
					if (rc.canAttackLocation(enemy.location) && rc.getType().canAttack() && enemy.health/(enemy.attackPower-0.1)*(enemy.weaponDelay) < lowestHealth_dps) {
						lowestHealth_dps = enemy.health/(enemy.attackPower-0.1)*(enemy.weaponDelay);
						bestTarget = enemy;
					}
				}
				if(bestTarget != null){
					rc.attackLocation(bestTarget.location);
				}
			} else {
				if(rc.isCoreReady()){
					BugNav.goTo(sense.location);
				}
			}
		} else {
			Direction d = directions[random.nextInt(8)];
			if (rc.isCoreReady()) {
				if(goalLocation != null && rc.getLocation().distanceSquaredTo(goalLocation) > rc.getType().attackRadiusSquared){
					BugNav.goTo(goalLocation);
					rc.setIndicatorString(1, goalLocation.toString());
				}
				else if(rc.canMove(d)){
					tryToMove(randomDirection());
				}
			}
		}
	}

	/**
	 * Retreats in the direction with the biggest distance from enemies
	 * 	
	 * @param nearbyEnemies Array of nearby robots
	 * @return whether the robot retreated
	 * @throws GameActionException
	 */
	protected boolean tryToRetreat(RobotInfo[] nearbyEnemies) throws GameActionException {
		Direction bestRetreatDir = null;
		RobotInfo currentClosestEnemy = Utility.closest(nearbyEnemies, rc.getLocation());

		int bestDistSq = rc.getLocation().distanceSquaredTo(currentClosestEnemy.location);
		for (Direction dir : Direction.values()) {
			if (!rc.canMove(dir)) continue;

			MapLocation retreatLoc = rc.getLocation().add(dir);

			RobotInfo closestEnemy = Utility.closest(nearbyEnemies, retreatLoc);
			int distSq = retreatLoc.distanceSquaredTo(closestEnemy.location);
			if (distSq > bestDistSq) {
				bestDistSq = distSq;
				bestRetreatDir = dir;
			}
		}

		if (bestRetreatDir != null && rc.isCoreReady()) {
			rc.move(bestRetreatDir);
			return true;
		}
		return false;
	}


	public  int tryToMove(Direction forward) throws GameActionException{
		if(rc.isCoreReady()){
			for(int deltaD:tryDirections){
				Direction maybeForward = Direction.values()[(forward.ordinal()+deltaD+8)%8];
				if(rc.canMove(maybeForward)){
					rc.move(maybeForward);
					return deltaD;
				}
			}
			if(rc.getType().canClearRubble()){
				//failed to move, look to clear rubble
				MapLocation ahead = rc.getLocation().add(forward);
				if(rc.senseRubble(ahead)>=GameConstants.RUBBLE_OBSTRUCTION_THRESH){
					rc.clearRubble(forward);
				}
			}
		}
		return 0;
	}

	public Direction randomDirection() {
		return Direction.values()[(int)(random.nextDouble()*8)];
	}
}
