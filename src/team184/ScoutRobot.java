package team184;

import java.util.HashSet;
import java.util.Set;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;

public class ScoutRobot extends BaseRobot {
	private Set<RobotInfo> sentRobots;
	private int distanceToNearestArchon = 100;
	private Set<MapLocation> sentPartsCaches;
	private enum BehaviorState{
		LOOK_FOR_CORNERS,
		RETREAT,
		FIND_ENEMY
	}
	public ScoutRobot(RobotController rc){
		super(rc);
		sentRobots = new HashSet<RobotInfo>();
		sentPartsCaches = new HashSet<MapLocation>();
	}
	
	private void lookForNeutralRobots(){
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
			try {
				if(neutralSignal.send(distanceToNearestArchon*distanceToNearestArchon)){
					sentRobots.add(ri);
				}
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			};
		}
	}
	
	private void lookForPartsCache(){
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
						try {
							if(partsSignal.send(distanceToNearestArchon*distanceToNearestArchon)){
								sentPartsCaches.add(myLocation.add(dx, dy));
							}
						} catch (GameActionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	@Override
	public void run() {
		lookForNeutralRobots();
		lookForPartsCache();
		
		
		defaultBehavior();
	}
}
