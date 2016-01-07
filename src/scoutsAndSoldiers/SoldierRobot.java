package scoutsAndSoldiers;

import battlecode.common.RobotController;

public class SoldierRobot extends BaseRobot {

    public SoldierRobot(RobotController rc) {
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
