package neutrals;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class ViperRobot  extends BaseRobot {

	public ViperRobot(RobotController rc){
		super(rc);
	}

	@Override
	public void run() throws GameActionException {
		defaultBehavior();
	}
}
