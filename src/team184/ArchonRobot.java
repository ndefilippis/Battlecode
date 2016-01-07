package team184;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class ArchonRobot extends BaseRobot{

	public ArchonRobot(RobotController rc){
		super(rc);
	}

	@Override
	public void run() {
		int i = (int) (7 * Math.random());
		if (rc.canBuild(Direction.NORTH, robotTypes[i])) {
			if (rc.isCoreReady()) {
				try {
					rc.build(Direction.NORTH, robotTypes[i]);
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
