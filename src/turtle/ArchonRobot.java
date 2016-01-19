package turtle;

import java.util.ArrayList;

import battlecode.common.*;

public class ArchonRobot extends BaseRobot{
	private RobotType[] buildRobotTypes = {
			RobotType.SCOUT, 
			RobotType.SOLDIER,
			RobotType.GUARD, 
	};
	private double[] probabilities = {0.1, 0.65, 1.0};
	private double[] probabilitiesZ = {0.0, 0.4, 1.0};

	int heiarchy = -1;

	private int leaderId;
	private MapLocation leaderLocation;
	private boolean sentGoal;
	private ArrayList<MapLocation> neutralBotLocations = new ArrayList<MapLocation>();
	private ArrayList<MapLocation> knownZombieDenLocations = new ArrayList<MapLocation>();
	private int lastSentGoal;
	private boolean foundEnemyArchon;
	private boolean foundZombieDen;
	private MapLocation enemyArchon;
	private double prevHealth = RobotType.ARCHON.maxHealth;
	private ZombieSpawnSchedule zss;

	private boolean suppressSignals;

	public ArchonRobot(RobotController rc){
		super(rc);
		zss = rc.getZombieSpawnSchedule();
	}

	public void getSignals(){
		Signal[] queue = rc.emptySignalQueue();
		for(Signal signal : queue){
			if(signal.getTeam() == myTeam){
				if(signal.getMessage() != null){
					if(signal.getMessage()[0] == 0xdead && signal.getMessage()[1] == 0xbeef){
						heiarchy--;
						continue;
					}
					MessageSignal msgSig= new MessageSignal(signal);
					switch(msgSig.getMessageType()){
					case ROBOT:
						if(msgSig.getPingedTeam() == Team.NEUTRAL){
							rc.setIndicatorString(0,  "Found neutral");
							if(msgSig.getPingedLocation().distanceSquaredTo(rc.getLocation()) < 40){
								goalLocation = msgSig.getPingedLocation();
							}
						}
						if(msgSig.getPingedType() == RobotType.ARCHON && msgSig.getPingedTeam() == myTeam.opponent()){
							rc.setIndicatorString(0, "Found enemy Archon");
							foundEnemyArchon = true;
							sentGoal = false;
							enemyArchon = msgSig.getPingedLocation();
						}
						if(msgSig.getPingedType() == RobotType.ZOMBIEDEN){
							rc.setIndicatorString(2, "Found Zombie Den");
							knownZombieDenLocations.add(msgSig.getPingedLocation());
							foundZombieDen = true;
						}
						break;
					case PARTS:
						break;
					default:
						break;
					}
				}
			}
		}
	}

	@Override
	public void initialize() throws GameActionException{
		if(rc.getRoundNum() != 0){
			return;
		}
		Signal[] signals = rc.emptySignalQueue();
		rc.broadcastSignal(GameConstants.MAP_MAX_HEIGHT*GameConstants.MAP_MAX_HEIGHT);
		for(Signal s : signals){
			if(s.getTeam() == myTeam){
				heiarchy++;
			}
		}
		rc.setIndicatorString(1, "I am the " + heiarchy + ": " + rc.getRoundNum());
		if(heiarchy == -1){
			goalLocation = rc.getLocation();
			rc.broadcastMessageSignal(1337, 0, 100*100);
			leaderId = rc.getID();
			leaderLocation = rc.getLocation();
		}
		else{
			heiarchy-=1;
			for(Signal s : signals){
				if(s.getMessage() != null){
					if(s.getMessage()[0] == 1337){
						leaderId = s.getID();
						leaderLocation = s.getLocation();
						goalLocation = leaderLocation;
						break;
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


		if(!suppressSignals && heiarchy == -1 && (!sentGoal || rc.getRoundNum() - lastSentGoal > 10)){
			MapLocation goal;
			MessageSignal goalDirection = new MessageSignal(rc);
			if(foundZombieDen){
				goal = knownZombieDenLocations.get(knownZombieDenLocations.size()-1);
				goalDirection.setCommand(goal, MessageSignal.CommandType.ATTACK);
			}
			else if(foundEnemyArchon){
				goal = enemyArchon;
				goalDirection.setCommand(goal, MessageSignal.CommandType.ATTACK);
			}
			else{
				goal = rc.getLocation();
				goalDirection.setCommand(goal, MessageSignal.CommandType.MOVE);
			}

			goalDirection.send(30*30);
			sentGoal = true;			
			lastSentGoal = rc.getRoundNum();
			rc.setIndicatorString(2, goal+"");
		}
		else{
			rc.setIndicatorString(2, "");
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
		if(goalLocation != null && rc.getRoundNum() < 150){

			if(rc.canSense(goalLocation)){
				goalLocation = null;
			}
			if(Utility.getClosestRound(zss) - rc.getRoundNum() > 50 && rc.getTeamParts() > 200){
				tryToBuild();
			}
			else if(rc.isCoreReady()){
				BugNav.goTo(goalLocation);
			}
		}

		//try to activate neutral units
		/*MapLocation closestNeutral = Utility.closestLocation(neutralBotLocations, rc.getLocation());
		if (closestNeutral != null) {
			rc.setIndicatorString(2, "Finding Neutal");
			if(rc.canSense(closestNeutral) && (rc.senseRobotAtLocation(closestNeutral) == null || rc.senseRobotAtLocation(closestNeutral).team != Team.NEUTRAL)){
				neutralBotLocations.remove(closestNeutral);
			}
			else if (rc.getLocation().distanceSquaredTo(closestNeutral) < 2) {
				if (rc.isCoreReady()) {
					rc.activate(closestNeutral);
					neutralBotLocations.remove(closestNeutral);
				}
			} else if (rc.isCoreReady() && rc.canMove(rc.getLocation().directionTo(closestNeutral))) {
				tryToMove(rc.getLocation().directionTo(closestNeutral));
			}
		}*/

		RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), RobotType.ARCHON.sensorRadiusSquared);
		if(enemies.length > 3){
			//tryToRetreat(enemies);
		}

		tryToBuild();
		if(enemies.length > 0){
			//tryToRetreat(enemies);
			suppressSignals = true;
			return;
		}else{
			//defaultBehavior();
		}
		suppressSignals = false;
	}

	public void tryToBuild() throws GameActionException{
		//try to build a robot
		double prob = random.nextDouble();
		int index = 0;
		RobotType robot;
		if(rc.getRoundNum() < 50){
			for(Direction d : Direction.values()){
				if(rc.canBuild(d, RobotType.SOLDIER) && rc.isCoreReady()){
					rc.build(d, RobotType.SOLDIER);
				}
			}
		}
		else{
			for(Direction d : Direction.values()){
				if(rc.canBuild(d, RobotType.TURRET) && rc.isCoreReady()){
					rc.build(d, RobotType.TURRET);
				}
			}
		}
		return;
	}
	protected void postrun() throws GameActionException{
		if(heiarchy == 0 && rc.getHealth() < 20 && rc.getHealth() < prevHealth){
			rc.broadcastMessageSignal(0xdead, 0xbeef, 100*100);
		}
		prevHealth = rc.getHealth();
		super.postrun();
	}

}
