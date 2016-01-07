package scoutsAndSoldiers;

import battlecode.common.*;

public class ScoutRobot extends BaseRobot {

    public ScoutRobot(RobotController rc) {
        super(rc);
    }

    @Override
    public void initial() {

    }

    @Override
    public void run() {
        // defaultBehavior();
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
            if (rc.canAttackLocation(l))
                attackMove(l, sense);
            else
                moveScout(l);
        }
    }

    private void moveScout(MapLocation l) {
        Direction d = directions[(int) (8 * Math.random())];
        if (rc.canMove(d) && rc.isCoreReady()) {
            try {
                rc.move(d);
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
    }

    private void attackMove(MapLocation l, RobotInfo sense) {
        if (rc.getType().canAttack() && rc.isWeaponReady() && sense.team != rc.getTeam()) {
            try {
                rc.attackLocation(l);
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        } else {
            if (moves.isEmpty() && rc.getType().canMove()) {
                moves = bestPathTo(l, rc);
            } else if (rc.isCoreReady()) {
                move(moves.pop(), rc);
            }
        }
    }
}