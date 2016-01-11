package supermicro;

import battlecode.common.*;

import java.util.HashSet;
import java.util.Set;

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
	
	private void lookForZombieDens(){
		RobotInfo[] zombies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, Team.ZOMBIE);
		for(RobotInfo ri : zombies){
			if(sentRobots.contains(ri) || ri.type != RobotType.ZOMBIEDEN){
				continue;
			}
			MessageSignal zombieDenSignal = new MessageSignal(rc);
			zombieDenSignal.setMessageType(MessageSignal.MessageType.ROBOT);
			zombieDenSignal.setPingedLocation(ri.location.x-rc.getLocation().x, ri.location.y-rc.getLocation().y);
			zombieDenSignal.setPingedTeam(Team.ZOMBIE);
			zombieDenSignal.setPingedType(ri.type);
			try {
				if(zombieDenSignal.send(distanceToNearestArchon*distanceToNearestArchon)){
					sentRobots.add(ri);
				}
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void lookForNeutralRobots(){
		RobotInfo[] neutralRobots = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, Team.NEUTRAL);
		for(RobotInfo ri : neutralRobots){
			if(sentRobots.contains(ri)){
				continue;
			}
			MessageSignal neutralSignal = new MessageSignal(rc);
			neutralSignal.setMessageType(MessageSignal.MessageType.ROBOT);
			neutralSignal.setPingedLocation(ri.location.x-rc.getLocation().x, ri.location.y-rc.getLocation().y);
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
		lookForZombieDens();
		lookForNeutralRobots();
		lookForPartsCache();
		
		if(rc.canMove(d)){
			if(rc.isCoreReady()){
				try {
					rc.move(d);
				} catch (GameActionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else{
			d.rotateLeft();
		}
	}
}