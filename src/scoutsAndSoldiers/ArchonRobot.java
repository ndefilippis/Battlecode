package scoutsAndSoldiers;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class ArchonRobot extends BaseRobot {
    protected static RobotType[] robotTypes = {RobotType.SCOUT, RobotType.GUARD, RobotType.SOLDIER};

    public ArchonRobot(RobotController rc) {
        super(rc);
    }

    @Override
    public void initial() {
        for (Direction dir : Direction.values()) {
            if (rc.canBuild(dir, RobotType.SCOUT)) {
                try {
                    rc.build(dir, RobotType.SCOUT);
                } catch (GameActionException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void run() {
        Direction buildDirection1 = directions[(int) (8 * Math.random())];
        Direction buildDirection2 = directions[(int) (8 * Math.random())];
        while (buildDirection2 == buildDirection1)
            buildDirection2 = directions[(int) (8 * Math.random())];
        if (rc.getRoundNum() < 30) {
            makeScoutsAndGuards(buildDirection1, buildDirection2);
        } else {
            makeGuardsAndSoldiers(buildDirection1, buildDirection2);
        }
    }

    private void makeScoutsAndGuards(Direction buildDirection1, Direction buildDirection2) {
        if (rc.canBuild(buildDirection1, robotTypes[0]) || rc.canBuild(buildDirection2, robotTypes[1])) {
            if (rc.isCoreReady()) {
                try {
                    rc.build(buildDirection1, robotTypes[0]);
                } catch (GameActionException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                try {
                    rc.build(buildDirection2, robotTypes[1]);
                } catch (GameActionException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } else {
            moveArchon(buildDirection1, buildDirection2);
        }
    }

    private void makeGuardsAndSoldiers(Direction buildDirection1, Direction buildDirection2) {
        if (rc.canBuild(buildDirection1, robotTypes[0]) || rc.canBuild(buildDirection2, robotTypes[1])) {
            if (rc.isCoreReady()) {
                try {
                    rc.build(buildDirection1, robotTypes[1]);
                } catch (GameActionException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                try {
                    rc.build(buildDirection2, robotTypes[2]);
                } catch (GameActionException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } else {
            moveArchon(buildDirection1, buildDirection2);
        }
    }

    private void moveArchon(Direction buildDirection1, Direction buildDirection2) {
        Direction d = directions[(int) (8 * Math.random())];
        while (d == buildDirection1 || d == buildDirection2) {
            d = directions[(int) (8 * Math.random())];
        }
        if (rc.canMove(d) && rc.isCoreReady()) {
            try {
                rc.move(d);
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
    }
}
