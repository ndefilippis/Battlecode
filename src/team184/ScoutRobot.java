package team184;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class ScoutRobot extends BaseRobot {
	private Map<RobotInfo, Integer> sentRobots;
	private Map<MapLocation, Integer> sentPartsCaches;
	private Map<MapLocation, Integer> sentArchonLocations;
	private Map<Direction, Integer> sentMapEdges;
	private Direction d;

	public void initialize() throws GameActionException{
		d = randomDirection();
		super.initialize();
	}
	public ScoutRobot(RobotController rc){
		super(rc);
		sentRobots = new HashMap<RobotInfo, Integer>();
		sentPartsCaches = new HashMap<MapLocation, Integer>();
		sentMapEdges = new HashMap<Direction, Integer>();
		sentArchonLocations = new HashMap<MapLocation, Integer>();
		nearestArchonLocation = rc.getLocation().add(Direction.SOUTH);
	}

	private void lookForEnemyArchons() throws GameActionException{
		if(rc.getMessageSignalCount() > GameConstants.MESSAGE_SIGNALS_PER_TURN){
			return;
		}
		RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, myTeam.opponent());
		for(RobotInfo ri : enemies){
			if(sentArchonLocations.containsKey(ri.location) && sentArchonLocations.get(ri.location) < 10 || ri.type != RobotType.ARCHON){
				continue;
			}
			MessageSignal archonSignal = new MessageSignal(rc);
			archonSignal.setRobot(ri.location, myTeam.opponent(), ri.type);
			if(archonSignal.send(nearestArchonLocation.distanceSquaredTo(rc.getLocation()))){
				sentArchonLocations.put(ri.location, rc.getRoundNum());
			}
		}
	}

	private void lookForZombieDens() throws GameActionException{
		if(rc.getMessageSignalCount() > GameConstants.MESSAGE_SIGNALS_PER_TURN){
			return;
		}
		RobotInfo[] zombies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, Team.ZOMBIE);
		for(RobotInfo ri : zombies){
			if(sentRobots.containsKey(ri) && rc.getRoundNum() - sentRobots.get(ri) < 10 || ri.type != RobotType.ZOMBIEDEN){
				continue;
			}
			MessageSignal zombieSignal = new MessageSignal(rc);
			zombieSignal.setRobot(ri.location, Team.ZOMBIE, ri.type);
			if(zombieSignal.send(nearestArchonLocation.distanceSquaredTo(rc.getLocation()))){
				sentRobots.put(ri, rc.getRoundNum());
			}
		}
	}

	private void lookForNeutralRobots() throws GameActionException{
		if(rc.getMessageSignalCount() > GameConstants.MESSAGE_SIGNALS_PER_TURN){
			return;
		}
		RobotInfo[] neutralRobots = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, Team.NEUTRAL);
		for(RobotInfo ri : neutralRobots){
			if(sentRobots.containsKey(ri) && rc.getRoundNum() - sentRobots.get(ri) < 10){
				continue;
			}
			MessageSignal neutralSignal = new MessageSignal(rc);
			neutralSignal.setRobot(ri.location, Team.NEUTRAL, ri.type);
			if(neutralSignal.send(nearestArchonLocation.distanceSquaredTo(rc.getLocation()))){
				sentRobots.put(ri, rc.getRoundNum());
			}
		}
	}

	private void lookForPartsCache() throws GameActionException{
		if(rc.getMessageSignalCount() > GameConstants.MESSAGE_SIGNALS_PER_TURN){
			return;
		}
		MapLocation[] partCaches = rc.sensePartLocations(rc.getType().sensorRadiusSquared);
		for(MapLocation ml : partCaches){
			if(rc.senseParts(ml) < 100 && sentPartsCaches.containsKey(ml) && rc.getRoundNum() - sentPartsCaches.get(ml) < 10){
				continue;
			}
			MessageSignal partsSignal = new MessageSignal(rc);
			partsSignal.setParts(ml, rc.senseParts(ml));
			if(partsSignal.send(nearestArchonLocation.distanceSquaredTo(rc.getLocation()))){
				sentPartsCaches.put(ml, rc.getRoundNum());
			}
		}
	}

	@Override
	protected void prerun() throws GameActionException{
		lookForNeutralRobots();
		lookForZombieDens();
		lookForEnemyArchons();

		if(rc.getTeamParts() < 100){
			lookForPartsCache();
		}
	}

	@Override
	public void run() throws GameActionException {
		RobotInfo[] nearbyEnemies = rc.senseHostileRobots(rc.getLocation(), 25);
		if(nearbyEnemies.length > 4){
			if(!rc.isInfected()){
				tryToRetreat(rc.senseHostileRobots(rc.getLocation(), rc.getType().sensorRadiusSquared));
			}
			else{
				tryToMove(rc.getLocation().directionTo(nearbyEnemies[0].location));
			}
		}
		if(rc.isCoreReady()){
			if(rc.isInfected()){
				tryToMove(rc.getLocation().directionTo(nearestArchonLocation).opposite());
			}
			tryToMove(d);
		}
		if(rc.getRoundNum() % 100 == 99){
			d = randomDirection();
		}
	}

	@Override
	protected void postrun() throws GameActionException{
		if(rc.getHealth() < 15){
			rc.broadcastMessageSignal(0x1337, 0xbeef, nearestArchonLocation.distanceSquaredTo(rc.getLocation()));
		}
		super.postrun();
	}
}
