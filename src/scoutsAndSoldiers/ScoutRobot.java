package scoutsAndSoldiers;

import battlecode.common.RobotController;

public class ScoutRobot extends BaseRobot {

	public ScoutRobot(RobotController rc){
		super(rc);
	}

	@Override
	public void run() {
		defaultBehavior();
	}
}
