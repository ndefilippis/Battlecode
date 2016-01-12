package swarm;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class RobotPlayer {

	@SuppressWarnings("unused")
	public static void run(RobotController rc) {
		BaseRobot br;
		switch(rc.getType()){
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
		case TTM:
			br = new TurretRobot(rc);
			break;
		default:
			br = null;
			System.out.println("Unknown Robot Type");
			break;
		}
		try {
			br.startLoop();
		} catch (GameActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("You've ended early");
	}
}
