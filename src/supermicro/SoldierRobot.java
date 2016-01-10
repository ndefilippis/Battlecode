package supermicro;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Signal;
import battlecode.common.Team;

public class SoldierRobot  extends BaseRobot {
	Direction d = Direction.EAST;
	MapLocation nearestArchonLocation;
	
	public void inititalize(){
		RobotInfo[] nearbyRobots = rc.senseNearbyRobots(2, myTeam);
		for(RobotInfo ri : nearbyRobots){
			if(ri.type == RobotType.ARCHON){
				nearestArchonLocation = ri.location;
			}
		}
	}
	public SoldierRobot(RobotController rc){
		super(rc);
	}

	@Override
	public void run() throws GameActionException {
		RobotInfo[] enemyInfo = rc.senseHostileRobots(rc.getLocation(), rc.getType().sensorRadiusSquared);
		RobotInfo[] allies = rc.senseNearbyRobots(9, myTeam);
		boolean nearbyArchon = false;
		for(RobotInfo ri : allies){
			if(ri.type == RobotType.ARCHON){
				nearbyArchon = true;
			}
		}
		if(enemyInfo.length > 0 && allies.length < 2 && !nearbyArchon)
			kite();
		else{
			defaultBehavior();
		}
	}
	
	public void kite() throws GameActionException{
		boolean dig = false;
		if(rc.isWeaponReady()){
			RobotInfo[] enemyInfo = rc.senseHostileRobots(rc.getLocation(), rc.getType().attackRadiusSquared);
			//RobotInfo[] zombieInfo = rc.senseNearbyRobots(rc.getType().attackRadiusSquared, Team.ZOMBIE);
			
			//RobotInfo[] enemyInfo = Utility.combine(opponentInfo, zombieInfo);
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
		boolean toTurn = Utility.isBlocked(rc, rc.getLocation().add(d, 3)) && rc.isCoreReady();
		if(toTurn){
			if(rc.senseRubble((rc.getLocation().add(d, 1))) > 100.0){
				dig = true;
			}
			if(Utility.isBlocked(rc, rc.getLocation().add(d.rotateRight().rotateRight(), 3)))
				d = d.rotateLeft();
			else{
				d = d.rotateRight();
			}
		}
		if(rc.isCoreReady()){
			try {
				if(dig){
					rc.clearRubble(d);
				}
				else if(rc.canMove(d)){
					rc.move(d);
				}
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
