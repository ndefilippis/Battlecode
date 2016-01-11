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
    public void initialize() {
        for (Direction dir : Direction.values()) {
            if (rc.canBuild(dir, RobotType.SCOUT) && rc.isCoreReady()) {
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
    public void run() throws GameActionException {
        Direction buildDirection1 = directions[(int) (8 * Math.random())];
        Direction buildDirection2 = directions[(int) (8 * Math.random())];
        while (buildDirection2 == buildDirection1)
            buildDirection2 = directions[(int) (8 * Math.random())];
        if (rc.getRoundNum() < 30) {
            make(buildDirection1, buildDirection2, robotTypes[0], robotTypes[1]);
        } else {
            make(buildDirection1, buildDirection2, robotTypes[1], robotTypes[2]);
        }
    }


    private void make(Direction buildDirection1, Direction buildDirection2, RobotType type1, RobotType type2) throws GameActionException {
    	if(rc.isCoreReady()){
    		if (rc.canBuild(buildDirection1, type1) || rc.canBuild(buildDirection2, type2)) {
    			if(rc.canBuild(buildDirection1, type1)){
    				rc.build(buildDirection1, type1);
    			}
    			else if(rc.canBuild(buildDirection2, type2)){
    				 rc.build(buildDirection2, type2);
    			}
    			else{
    				moveArchon(buildDirection1, buildDirection2);
    			}
    		}
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
