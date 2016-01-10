package grouping;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Signal;
import battlecode.common.Team;

public class ArchonRobot extends BaseRobot{
	private RobotType[] buildRobotTypes = {RobotType.SCOUT, RobotType.SOLDIER, RobotType.SOLDIER, RobotType.SOLDIER,
            RobotType.GUARD, RobotType.GUARD};
            
	public ArchonRobot(RobotController rc){
		super(rc);
	}
	private MapLocation destination;
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
								foundSomething = true;
							}
							break;
						case PARTS:
							destination = msgSig.getPingedLocation();
							foundSomething = true;
					}
				}
			}
		}
	}

	@Override
	public void run() {
		getSignals();
		if(foundSomething){
			defaultBehavior();
		}
		//try to build a robot
		RobotType robot = buildRobotTypes[(random.nextInt(6))];
		for(Direction d : Direction.values()){
			if (rc.canBuild(d, robot)) {
				if (rc.isCoreReady()) {
					try {
						rc.build(d, robot);
					} catch (GameActionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
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
	}
}
