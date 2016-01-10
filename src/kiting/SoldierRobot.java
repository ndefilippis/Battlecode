package kiting;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class SoldierRobot  extends BaseRobot {
	Direction d = Direction.EAST;
	public SoldierRobot(RobotController rc){
		super(rc);
	}

	@Override
	public void run() {
		kite();
	}
	
	public void kite(){
		if(rc.isWeaponReady()){
			RobotInfo[] enemyInfo = rc.senseNearbyRobots(rc.getType().attackRadiusSquared, rc.getTeam().opponent());
			try {
				if(enemyInfo.length > 0)
				rc.attackLocation(enemyInfo[0].location);
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			if(rc.isCoreReady()){
				
				try {
					if(rc.canMove(d))
						rc.move(d);
					else{
						d = d.rotateRight().rotateRight();
					}
				} catch (GameActionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	}
}
