package team184;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class SoldierRobot  extends BaseRobot {
	Direction d = Direction.EAST;
	public SoldierRobot(RobotController rc){
		super(rc);
	}

	@Override
	public void run() throws GameActionException {
		kite();
	}
	
	public void kite() throws GameActionException{
		boolean dig = false;
		if(rc.isWeaponReady()){
			RobotInfo[] enemyInfo = rc.senseNearbyRobots(rc.getType().attackRadiusSquared, rc.getTeam().opponent());
			try {
				if(enemyInfo.length > 0){
					rc.attackLocation(enemyInfo[0].location);
					d = rc.getLocation().directionTo(enemyInfo[0].location).opposite();
				}
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		boolean toTurn = !(rc.onTheMap(rc.getLocation().add(d, 3)) || rc.senseRubble((rc.getLocation().add(d, 3))) > 100.0) && rc.isCoreReady();
		if(toTurn){
			if(rc.senseRubble((rc.getLocation().add(d, 3))) > 100.0){
				dig = true;
			}
			d = d.rotateRight();
		}
		if(rc.isCoreReady()){
			try {
				if(dig){
					rc.clearRubble(d);
				}
				if(rc.canMove(d))
					rc.move(d);
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
