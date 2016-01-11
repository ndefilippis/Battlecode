package scoutsAndSoldiers;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class GuardRobot extends BaseRobot {
	public GuardRobot(RobotController rc){
		super(rc);
	}

	@Override
	public void run() throws GameActionException {
		defaultBehavior();
	}
}
