package scoutsAndSoldiers;

import battlecode.common.RobotController;

public class ViperRobot  extends BaseRobot {
	
	public ViperRobot(RobotController rc){
		super(rc);
	}

	@Override
	public void initial() {

	}

	@Override
	public void run() {
		defaultBehavior();
	}
}
