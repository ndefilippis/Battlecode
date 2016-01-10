package grouping;

import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class ScoutRobot extends BaseRobot {
	private enum BehaviorState{
		LOOK_FOR_CORNERS,
		RETREAT,
		FIND_ENEMY
	}
	public ScoutRobot(RobotController rc){
		super(rc);
	}

	@Override
	public void run() {
		defaultBehavior();
	}
}
