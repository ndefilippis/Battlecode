package scoutsAndSoldiers;

import battlecode.common.*;

public class TurretRobot extends BaseRobot {

    public TurretRobot(RobotController rc) {
        super(rc);
    }


    @Override
    public void run() throws GameActionException {
        if (rc.getType() == RobotType.TTM) {
            defaultBehavior();
            return;
        }
        RobotInfo[] ri = rc.senseNearbyRobots();
        RobotInfo sense = null;
        for (RobotInfo r : ri) {
            if (r.team != rc.getTeam()) {
                sense = r;
                break;
            }
        }
        if (sense != null) {
            MapLocation l = sense.location;
            if (rc.canAttackLocation(l) && rc.getType().canAttack() && rc.isWeaponReady() && sense.team != rc.getTeam()) {
                try {
                    rc.attackLocation(l);
                } catch (GameActionException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

