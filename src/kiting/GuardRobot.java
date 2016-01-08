package kiting;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class GuardRobot extends BaseRobot {

	Direction d = Direction.EAST;
	public GuardRobot(RobotController rc){
		super(rc);
	}

	@Override
	public void run() {
		forward();
	}
	
	public void forward(){
		RobotInfo[] enemyInfo = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam().opponent());
			if(rc.isWeaponReady()){
				try {
					if(enemyInfo.length > 0 && rc.canAttackLocation(enemyInfo[0].location))
					rc.attackLocation(enemyInfo[0].location);
				} catch (GameActionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
				if(rc.isCoreReady()){
					try {
						if(enemyInfo.length > 0){
							d = rc.getLocation().directionTo(enemyInfo[0].location);
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
