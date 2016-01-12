package swarm;

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
			RobotType.SCOUT, RobotType.SCOUT,
			RobotType.SOLDIER, RobotType.SOLDIER, RobotType.SOLDIER, RobotType.SOLDIER,
			RobotType.GUARD, RobotType.GUARD,
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
	private boolean[][] mapEdgesFound = new boolean[3][3];
	private int[][] mapEdges = new int[3][3];

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
					case MAP_EDGE:
						Direction edge = msgSig.getPingedDirection();
						MapLocation ml = msgSig.getPingedLocation();
						if(edge == Direction.EAST){
							mapEdgesFound[2][1] = true;
							mapEdges[2][1] = ml.x;
						}
						if(edge == Direction.NORTH){
							mapEdgesFound[1][0] = true;
							mapEdges[1][0] = ml.y;
						}
						if(edge == Direction.SOUTH){
							mapEdgesFound[1][2] = true;
							mapEdges[1][2] = ml.y;
						}
						if(edge == Direction.WEST){
							mapEdgesFound[0][1] = true;
							mapEdges[0][1] = ml.x;
						}
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
			teamLocation = rc.getLocation().add(teamDirection);
			rc.broadcastMessageSignal(1337, teamDirection.ordinal(), 50*50);
			leaderId = rc.getID();
			
		}
		else{
			heiarchy-=1;
			for(Signal s : signals){
				if(s.getMessage() != null){
					if(s.getMessage()[0] == 1337){
						leaderId = s.getID();
						teamDirection = Direction.values()[s.getMessage()[1]];
						leaderLocation = s.getLocation();
						teamLocation = s.getLocation().add(teamDirection);
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
			if(rc.getRoundNum() % 250 == 249){
				teamDirection = randomDirection();
			}
			MessageSignal goalDirection = new MessageSignal(rc);
			MapLocation goal = rc.getLocation().add(teamDirection, 4);
			rc.setIndicatorString(0, goal.toString());
			goalDirection.setCommand(goal, MessageSignal.CommandType.MOVE);
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
					MessageSignal teamFirstDirective = new MessageSignal(rc);
					teamFirstDirective.setCommand(teamLocation, MessageSignal.CommandType.MOVE);
					teamFirstDirective.send(2);
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
