package team184;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class ScoutRobot extends BaseRobot {
	private Set<RobotInfo> sentRobots;
	private int distanceToNearestArchon = 100;
	private Set<MapLocation> sentPartsCaches;
	private Map<MapLocation, Integer> sentArchonLocations;
	private Set<Direction> sentMapEdges;
	private Direction d;

	public void initialize(){
		d = randomDirection();
	}
	public ScoutRobot(RobotController rc){
		super(rc);
		sentRobots = new HashSet<RobotInfo>();
		sentPartsCaches = new HashSet<MapLocation>();
		sentMapEdges = new HashSet<Direction>();
		sentArchonLocations = new HashMap<MapLocation, Integer>();
	}
	
	private void lookForEnemyArchons() throws GameActionException{
		RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, myTeam.opponent());
		for(RobotInfo ri : enemies){
			if(sentArchonLocations.containsKey(ri.location) && sentArchonLocations.get(ri.location) < 10 || ri.type != RobotType.ARCHON){
				continue;
			}
			MessageSignal archonSignal = new MessageSignal(rc);
			archonSignal.setRobot(ri.location, myTeam.opponent(), ri.type);
			if(archonSignal.send(distanceToNearestArchon*distanceToNearestArchon)){
				sentArchonLocations.put(ri.location, rc.getRoundNum());
			}
		}
	}

	private void lookForZombieDens() throws GameActionException{
		RobotInfo[] zombies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, Team.ZOMBIE);
		for(RobotInfo ri : zombies){
			if(sentRobots.contains(ri) || ri.type != RobotType.ZOMBIEDEN){
				continue;
			}
			MessageSignal zombieSignal = new MessageSignal(rc);
			zombieSignal.setRobot(ri.location, Team.ZOMBIE, ri.type);
			if(zombieSignal.send(distanceToNearestArchon*distanceToNearestArchon)){
				sentRobots.add(ri);
			}
		}
	}

	private void lookForNeutralRobots() throws GameActionException{
		RobotInfo[] neutralRobots = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, Team.NEUTRAL);
		for(RobotInfo ri : neutralRobots){
			if(sentRobots.contains(ri)){
				continue;
			}
			MessageSignal neutralSignal = new MessageSignal(rc);
			neutralSignal.setRobot(ri.location, Team.NEUTRAL, ri.type);
			if(neutralSignal.send(distanceToNearestArchon*distanceToNearestArchon)){
				sentRobots.add(ri);
			}
		}
	}

	private void lookForPartsCache() throws GameActionException{
		MapLocation[] partCaches = rc.sensePartLocations(rc.getType().sensorRadiusSquared);
		for(MapLocation ml : partCaches){
			if(rc.senseParts(ml) >= 100 && !sentPartsCaches.contains(ml)){
				MessageSignal partsSignal = new MessageSignal(rc);
				partsSignal.setParts(ml, rc.senseParts(ml));
				if(partsSignal.send(distanceToNearestArchon*distanceToNearestArchon)){
					sentPartsCaches.add(ml);
				}
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
		
		if(rc.senseHostileRobots(rc.getLocation(), 25).length > 4){
			tryToRetreat(rc.senseHostileRobots(rc.getLocation(), rc.getType().sensorRadiusSquared));
		}

		if(rc.isCoreReady()){
			tryToMove(d);
		}
		if(rc.getRoundNum() % 100 == 99){
			d = randomDirection();
		}
		for(Direction d : Direction.values()){
			if(d.isDiagonal() || sentMapEdges.contains(d)) continue;

			int n = 7;
			if(!rc.onTheMap(rc.getLocation().add(d, n))){
				MessageSignal ms = new MessageSignal(rc);
				MapLocation edge = rc.getLocation().add(d, n);
				while(!rc.onTheMap(rc.getLocation().add(d, --n))){
					edge = rc.getLocation().add(d, n);
				}

				ms.setMapEdge(edge, d);
				ms.send(distanceToNearestArchon);
				sentMapEdges.add(d);
			}
		}
	}
	
	@Override
	protected void postrun() throws GameActionException{
		if(rc.getHealth() < 15){
			rc.broadcastMessageSignal(0x1337, 0xbeef, distanceToNearestArchon);
		}
	}
}
