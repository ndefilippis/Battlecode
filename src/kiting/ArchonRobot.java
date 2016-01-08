package kiting;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class ArchonRobot extends BaseRobot{
	private RobotType[] buildRobotTypes = {RobotType.SCOUT, RobotType.SOLDIER, RobotType.SOLDIER, RobotType.SOLDIER,
            RobotType.GUARD, RobotType.GUARD, RobotType.VIPER, RobotType.TURRET};
            
	public ArchonRobot(RobotController rc){
		super(rc);
	}

	@Override
	public void run() {
		Direction buildDirection = directions[(int) (8 * Math.random())];
		RobotType robot = buildRobotTypes[(int) (8 * Math.random())];
		if (rc.canBuild(buildDirection, robot)) {
			if (rc.isCoreReady()) {
				try {
					rc.build(buildDirection, robot);
				} catch (GameActionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else if(Math.random() < 0.25){
			Direction d = directions[(int) (8 * Math.random())];
			if (rc.canMove(d) && rc.isCoreReady()) {
				try {
					rc.move(d);
				} catch (GameActionException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
