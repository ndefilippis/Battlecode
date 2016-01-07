package team184;

import battlecode.common.RobotController;

public class GuardRobot extends BaseRobot {
	public GuardRobot(RobotController rc){
		super(rc);
	}

	@Override
	public void run() {
		defaultBehavior();
	}
}
