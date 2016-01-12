package teamGoal;

import java.util.ArrayList;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Signal;
import battlecode.common.Team;

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
	private boolean sentGoal;
	private Direction teamDirection;
	private ArrayList<MapLocation> neutralBotLocations = new ArrayList<MapLocation>();
	private int lastSentGoal;

	public void getSignals(){
		Signal[] queue = rc.emptySignalQueue();
		for(Signal signal : queue){
			if(signal.getTeam() == myTeam){
				if(signal.getMessage() != null){
					MessageSignal msgSig= new MessageSignal(signal);
					switch(msgSig.getMessageType()){
					case ROBOT:
						if(msgSig.getPingedTeam() == Team.NEUTRAL){
							rc.setIndicatorString(0,  "Found neutral");
							destination = msgSig.getPingedLocation();
						}
						break;
					case PARTS:
						destination = msgSig.getPingedLocation();
						break;
					default:
						break;
					}
				}
			}
		}
	}

	public void initialize() throws GameActionException{

		Signal[] signals = rc.emptySignalQueue();
		rc.broadcastSignal(30*30);
		heiarchy = signals.length;
		rc.setIndicatorString(1, "I am the " + heiarchy + ": " + rc.getRoundNum());
		if(heiarchy == 0){
			teamDirection = Direction.EAST;
			rc.broadcastMessageSignal(1337, teamDirection.ordinal(), 30*30);
			leaderId = rc.getID();
			
		}
		else{
			heiarchy-=1;
			for(Signal s : signals){
				if(s.getMessage() != null){
					if(s.getMessage()[0] == 1337){
						leaderId = s.getID();
						leaderLocation = s.getLocation();
						teamDirection = Direction.values()[s.getMessage()[1]];
					}
				}
			}
		}
	}

	@Override
	public void prerun() throws GameActionException{
		if(rc.getRoundNum() % 5 == 0){
			RobotInfo[] robotsNearMe = rc.senseNearbyRobots();
			for(RobotInfo ri : robotsNearMe){
				if(ri.team == Team.NEUTRAL && !neutralBotLocations.contains(ri.location)){
					neutralBotLocations.add(ri.location);
				}
			}
		}
		getSignals();
		
		
		if(heiarchy == 0 && (!sentGoal || rc.getRoundNum() - lastSentGoal > 10)){
			MessageSignal goalDirection = new MessageSignal(rc);
			goalDirection.setMessageType(MessageSignal.MessageType.COMMAND);
			MapLocation goal = rc.getLocation().add(teamDirection, 4);
			goalDirection.setPingedLocation(goal);
			goalDirection.send(30*30);
			sentGoal = true;
			while(!rc.onTheMap(rc.getLocation().add(teamDirection, 4))){
				teamDirection = teamDirection.rotateRight();
				sentGoal = false;
			}
			
			lastSentGoal = rc.getRoundNum();
		}
		else{
			super.prerun();
		}
	}
	
	@Override
	public void run() throws GameActionException {

		//try to heal nearby robots
		RobotInfo[] nearbyAllies = rc.senseNearbyRobots(2, myTeam);
		RobotInfo friendWithLowestHP = Utility.getRobotWithLowestHP(nearbyAllies);
		if(rc.isCoreReady() && friendWithLowestHP != null){
			if(friendWithLowestHP.type != RobotType.ARCHON){
				rc.repair(friendWithLowestHP.location);
			}
		}
		
		MapLocation closestNeutral = Utility.closestLocation(neutralBotLocations, rc.getLocation());
		if (closestNeutral != null) {
			rc.setIndicatorString(2, "Finding Neutal");
			if(rc.canSense(closestNeutral) && rc.senseRobotAtLocation(closestNeutral) == null){
				neutralBotLocations.remove(closestNeutral);
			}
			else if (rc.getLocation().distanceSquaredTo(closestNeutral) < 2) {
				if (rc.isCoreReady()) {
					rc.activate(closestNeutral);
					neutralBotLocations.remove(closestNeutral);
				}
			} else if (rc.isCoreReady() && rc.canMove(rc.getLocation().directionTo(closestNeutral))) {
				try {
					rc.move(rc.getLocation().directionTo(closestNeutral));
				} catch (GameActionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		//try to build a robot
		RobotType robot = buildRobotTypes[(random.nextInt(buildRobotTypes.length))];
		for(Direction d : Direction.values()){
			if (rc.canBuild(d, robot)) {
				if (rc.isCoreReady()) {
					rc.build(d, robot);
					rc.broadcastSignal(2);
				}
			}
		}

		RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), RobotType.ARCHON.sensorRadiusSquared);
		if(enemies.length > 0){
			tryToRetreat(enemies);
		}else{
			defaultBehavior();
		}
	}
}
