package team184;

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

	public SoldierRobot(RobotController rc){
		super(rc);
	}
	

	@Override
	public void run() throws GameActionException {
		
		RobotInfo[] enemyInfo = rc.senseHostileRobots(rc.getLocation(), rc.getType().sensorRadiusSquared);
		RobotInfo[] allies = rc.senseNearbyRobots(100, myTeam);
		if(enemyInfo.length > 2*allies.length){
			tryToRetreat(enemyInfo);
		}
		
		allies = rc.senseNearbyRobots(9, myTeam);
		boolean nearbyArchon = false;
		for(RobotInfo ri : allies){
			if(ri.type == RobotType.ARCHON){
				nearbyArchon = true;
			}
		}
		int attackingAllies = getNonRangedAllies();
		
		if(enemyInfo.length > 0 && attackingAllies < 1 && !nearbyArchon){
			if(canKite(Utility.closest(enemyInfo, rc.getLocation()))){
				kite();
				rc.setIndicatorString(0, "I am kiting");
			}
			else{
				rc.setIndicatorString(0, "I am not kiting");
				defaultBehavior();
			}
		}
		else{
			defaultBehavior();
		}
	}
	
	private int getNonRangedAllies(){
		int robs = 0;
		RobotInfo[] allies = rc.senseNearbyRobots(100, myTeam);
		for(RobotInfo ri : allies){
			if(ri.type != RobotType.SOLDIER && ri.type != RobotType.VIPER){
				robs++;
			}
		}
		return robs;
	}

	private boolean canKite(RobotInfo closest) {
		if(closest.team == Team.ZOMBIE){
			RobotInfo[] alliesNearMe = rc.senseNearbyRobots(closest.location, 100, myTeam);
			RobotInfo otherGuyTakingZombie = Utility.closest(alliesNearMe, closest.location);
			if(otherGuyTakingZombie == null || otherGuyTakingZombie.location.distanceSquaredTo(closest.location) < rc.getLocation().distanceSquaredTo(closest.location)){
				return closest.type.attackRadiusSquared < rc.getType().attackRadiusSquared && closest.type.movementDelay >= rc.getType().movementDelay;
			}
			else
				return otherGuyTakingZombie.type == RobotType.SOLDIER || otherGuyTakingZombie.location.distanceSquaredTo(closest.location) == rc.getLocation().distanceSquaredTo(closest.location);
		}
		return closest.type.attackRadiusSquared < rc.getType().attackRadiusSquared && closest.type.movementDelay >= rc.getType().movementDelay;
	}


	public void kite() throws GameActionException{
		boolean dig = false;
		RobotInfo[] enemyInfo = rc.senseHostileRobots(rc.getLocation(), rc.getType().attackRadiusSquared);
		if(rc.isWeaponReady()){
			
			//RobotInfo[] zombieInfo = rc.senseNearbyRobots(rc.getType().attackRadiusSquared, Team.ZOMBIE);

			//RobotInfo[] enemyInfo = Utility.combine(opponentInfo, zombieInfo);
			if(enemyInfo.length > 0){
				rc.attackLocation(enemyInfo[0].location);
			}
		}
		if(enemyInfo.length > 0){
			for(Direction q : Direction.values()){
				if(rc.getLocation().add(q).distanceSquaredTo(enemyInfo[0].location) > enemyInfo[0].type.attackRadiusSquared && rc.canMove(q)){
					d = q;
					rc.setIndicatorString(1, d.toString());
				}
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
			if(dig){
				rc.clearRubble(d);
			}
			else if(rc.canMove(d)){
				tryToMove(d);
			}
		}
	}
}
