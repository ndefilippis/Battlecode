package team184;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;


public class RobotPlayer {

    @SuppressWarnings("unused")
    public static void run(RobotController rc) {
        BaseRobot br;
        switch (rc.getType()) {
            case ARCHON:
                br = new ArchonRobot(rc);
                break;
            case SCOUT:
                br = new ScoutRobot(rc);
                break;
            case SOLDIER:
                br = new SoldierRobot(rc);
                break;
            case GUARD:
                br = new GuardRobot(rc);
                break;
            case VIPER:
                br = new ViperRobot(rc);
                break;
            case TURRET:
                br = new TurretRobot(rc);
                break;
            default:
                br = null;
                System.out.println("I don't think TTMs get passed here");
                break;
        }
        br.loop();
    }
}
