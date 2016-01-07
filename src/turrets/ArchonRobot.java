package turrets;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class ArchonRobot extends BaseRobot{
	protected static RobotType[] robotTypes = {RobotType.TURRET};
            
	public ArchonRobot(RobotController rc){
		super(rc);
	}

	@Override
	public void run() {
		Direction buildDirection = directions[(int) (8 * Math.random())];
		if (rc.canBuild(buildDirection, robotTypes[0])) {
			if (rc.isCoreReady()) {
				try {
					rc.build(buildDirection, robotTypes[0]);
				} catch (GameActionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else {
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
