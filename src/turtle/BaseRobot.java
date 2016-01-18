package turtle;

import java.util.ArrayList;
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

	static int[] tryDirections = {0,-1,1,-2,2}; //TODO put in navigation class
	public static RobotController rc;
	protected Team myTeam;
	protected Team otherTeam;
	protected RobotType myType;
	protected MapLocation goalLocation;
	protected MapLocation nearestArchonLocation;
	protected int birth;
	protected Random random;
	protected MessageSignal.CommandType currentCommandType;

	public BaseRobot(RobotController rc){
		BaseRobot.rc = rc;
		myTeam = rc.getTeam();
		otherTeam = myTeam.opponent();
		random = new Random(rc.getID());
		birth = rc.getRoundNum();
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
					currentCommandType = ms.getCommandType();
					if(nearestArchonLocation == null || s.getLocation().distanceSquaredTo(rc.getLocation()) < nearestArchonLocation.distanceSquaredTo(rc.getLocation())){
						nearestArchonLocation = s.getLocation();
					}
					
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
			if (rc.isCoreReady()) {
				if(goalLocation != null){
					BugNav.goTo(goalLocation);
					rc.setIndicatorString(1, goalLocation.toString());
					if(currentCommandType == MessageSignal.CommandType.MOVE && rc.getLocation().distanceSquaredTo(goalLocation) <= rc.getType().sensorRadiusSquared){
						goalLocation = null;
						currentCommandType = null;
					}
					if(currentCommandType == MessageSignal.CommandType.ATTACK && rc.getLocation().distanceSquaredTo(goalLocation) <= rc.getType().sensorRadiusSquared && rc.senseRobotAtLocation(goalLocation) == null){
						goalLocation = null;
						currentCommandType = null;
					}
					
				}
				else{
					int[] bestDirection = new int[8];
					RobotInfo[] allies = rc.senseNearbyRobots(100, myTeam);
					for(RobotInfo ally : allies){
						bestDirection[ally.location.directionTo(rc.getLocation()).ordinal()]++;
					}
					int max = 0;
					int ord = 0;
					for(int i = 0; i < bestDirection.length; i++){
						if(bestDirection[i] > max){
							max = bestDirection[i];
							ord = i;
						}
					}
					tryToMove(Direction.values()[ord]);
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
		if(bestRetreatDir == null){
			for(Direction d : Direction.values()){
				if(rc.canMove(d)){
					bestRetreatDir = d;
				}
			}
		}
		if (bestRetreatDir != null && rc.isCoreReady()) {
			tryToMove(bestRetreatDir);
			return true;
		}
		return false;
	}

	private ArrayList<MapLocation> previousMoves = new ArrayList<MapLocation>();
	public int tryToMove(Direction forward) throws GameActionException{
		if(previousMoves.size() > 5){
			previousMoves.remove(0);
		}
		if(rc.isCoreReady()){
			for(int deltaD:tryDirections){
				Direction maybeForward = Direction.values()[(forward.ordinal()+deltaD+8)%8];
				if(rc.canMove(maybeForward) && !previousMoves.contains(rc.getLocation().add(maybeForward))){
					previousMoves.add(rc.getLocation());
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
		return 0xdead;
	}

	public Direction randomDirection() {
		return Direction.values()[(int)(random.nextDouble()*8)];
	}
}
