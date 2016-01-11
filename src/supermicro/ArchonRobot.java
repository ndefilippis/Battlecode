package supermicro;

import battlecode.common.*;

import java.util.ArrayList;

public class ArchonRobot extends BaseRobot{
	private RobotType[] buildRobotTypes = {
			RobotType.SCOUT, 
			RobotType.SOLDIER, RobotType.SOLDIER, RobotType.SOLDIER,
			RobotType.GUARD, RobotType.GUARD,
			RobotType.TURRET, RobotType.TURRET
	};

	int heiarchy = -1;
	public ArchonRobot(RobotController rc){
		super(rc);
	}
	private int leaderId;
	private MapLocation destination;
	private MapLocation leaderLocation;
	private ArrayList<MapLocation> neutralBotLocations = new ArrayList<MapLocation>();
	private boolean foundSomething;

	public void getSignals(){
		Signal[] queue = rc.emptySignalQueue();
		for(Signal signal : queue){
			if(signal.getTeam() == myTeam){
				if(signal.getMessage() != null){
					MessageSignal msgSig= new MessageSignal(signal);
					switch(msgSig.getMessageType()){
					case ROBOT:
						if(msgSig.getPingedTeam() == Team.NEUTRAL){
							destination = msgSig.getPingedLocation();
							neutralBotLocations.add(destination);
							foundSomething = true;
						}
						break;
					case PARTS:
						destination = msgSig.getPingedLocation();
						foundSomething = true;
					default:
						break;
					}
				}
			}
		}
	}

	public void initialize(){
		try {
			Signal[] signals = rc.emptySignalQueue();
			rc.broadcastSignal(30*30);
			heiarchy = signals.length;
			rc.setIndicatorString(1, "I am the " + heiarchy + ": " + rc.getRoundNum());
			if(heiarchy == 0){
				rc.broadcastMessageSignal(1337, 1337, 30*30);
			}
			else{
				heiarchy-=1;
				for(Signal s : signals){
					if(s.getMessage() != null){
						if(s.getMessage()[0] == 1337){
							leaderId = s.getID();
							leaderLocation = s.getLocation();
						}
					}
				}
			}
		} catch (GameActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() throws GameActionException {

		getSignals();

		//try to heal nearby robots
		RobotInfo[] nearbyAllies = rc.senseNearbyRobots(2, myTeam);
		RobotInfo friendWithLowestHP = Utility.getRobotWithLowestHP(nearbyAllies);
		if(rc.isCoreReady() && friendWithLowestHP != null){
			try {
				if(friendWithLowestHP.type != RobotType.ARCHON){
					rc.repair(friendWithLowestHP.location);
				}
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		//try to convert neutral bots
		MapLocation closestNeutral = findClosestNeutralBot();
		if (closestNeutral != null) {
		rc.setIndicatorString(2, "Finding Neutal");
			if (rc.isCoreReady()) {
				if (rc.getLocation().distanceSquaredTo(closestNeutral) < 2) {
					rc.activate(closestNeutral);
					neutralBotLocations.remove(closestNeutral);
					rc.broadcastSignal(0);
				} else if (rc.canMove(rc.getLocation().directionTo(closestNeutral))) {
					try {
						rc.move(rc.getLocation().directionTo(closestNeutral));
						rc.broadcastSignal(2);
					} catch (GameActionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		//try to build a robot
		RobotType robot = buildRobotTypes[(random.nextInt(buildRobotTypes.length))];
		for(Direction d : Direction.values()){
			if (rc.canBuild(d, robot)) {
				if (rc.isCoreReady()) {
					try {
						rc.build(d, robot);
						rc.broadcastSignal(2);
					} catch (GameActionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}




		RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), RobotType.ARCHON.sensorRadiusSquared);
		if(enemies.length > 0){
			try {
				tryToRetreat(enemies);
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{
			if(foundSomething){
				defaultBehavior();
			}
		}
	}

	private MapLocation findClosestNeutralBot() {
		int minDistance = 100000;
		MapLocation closest = null;
		for (int i = neutralBotLocations.size()-1; i >= 0; i--) {
			if (minDistance > rc.getLocation().distanceSquaredTo(neutralBotLocations.get(i))) {
				closest = neutralBotLocations.get(i);
				minDistance = rc.getLocation().distanceSquaredTo(neutralBotLocations.get(i));
			}
		}
		return closest;
	}

}