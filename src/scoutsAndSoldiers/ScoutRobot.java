package scoutsAndSoldiers;

import java.util.HashSet;
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
	private Direction d;
	private enum BehaviorState{
		LOOK_FOR_CORNERS,
		RETREAT,
		FIND_ENEMY
	}

	public void initialize(){
		d = randomDirection();
	}
	public ScoutRobot(RobotController rc){
		super(rc);
		sentRobots = new HashSet<RobotInfo>();
		sentPartsCaches = new HashSet<MapLocation>();
	}

	private void lookForZombieDens() throws GameActionException{
		RobotInfo[] zombies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, Team.ZOMBIE);
		for(RobotInfo ri : zombies){
			if(sentRobots.contains(ri) || ri.type != RobotType.ZOMBIEDEN){
				continue;
			}
			MessageSignal neutralSignal = new MessageSignal(rc);
			neutralSignal.setMessageType(MessageSignal.MessageType.ROBOT);
			neutralSignal.setPingedLocation(rc.getLocation().x-ri.location.x, rc.getLocation().y-ri.location.y);
			neutralSignal.setPingedTeam(Team.ZOMBIE);
			neutralSignal.setPingedType(ri.type);
			if(neutralSignal.send(distanceToNearestArchon*distanceToNearestArchon)){
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
			neutralSignal.setMessageType(MessageSignal.MessageType.ROBOT);
			neutralSignal.setPingedLocation(rc.getLocation().x-ri.location.x, rc.getLocation().y-ri.location.y);
			neutralSignal.setPingedTeam(Team.NEUTRAL);
			neutralSignal.setPingedType(ri.type);
			if(neutralSignal.send(distanceToNearestArchon*distanceToNearestArchon)){
				sentRobots.add(ri);
			}
		}
	}

	private void lookForPartsCache() throws GameActionException{
		int senseRadius = (int) Math.sqrt(rc.getType().sensorRadiusSquared);
		MapLocation myLocation = rc.getLocation();
		for(int dx = -senseRadius; dx <= senseRadius; dx++){
			for(int dy = -senseRadius; dy <= senseRadius; dy++){
				if(rc.canSenseLocation(myLocation.add(dx, dy)) && !sentPartsCaches.contains(myLocation.add(dx, dy))){
					if(rc.senseParts(myLocation.add(dx, dy)) > 0){
						MessageSignal partsSignal = new MessageSignal(rc);
						partsSignal.setMessageType(MessageSignal.MessageType.PARTS);
						partsSignal.setPingedLocation(dx, dy);
						partsSignal.setPingedParts((int)rc.senseParts(myLocation.add(dx, dy)));
						if(partsSignal.send(distanceToNearestArchon*distanceToNearestArchon)){
							sentPartsCaches.add(myLocation.add(dx, dy));
						}
					}
				}
			}
		}
	}

	@Override
	public void run() throws GameActionException {
		lookForNeutralRobots();
		lookForPartsCache();


		if(rc.canMove(d)){
			if(rc.isCoreReady()){
				rc.move(d);
			}
		}
		else{
			d.rotateLeft();
		}
	}
}
