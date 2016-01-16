package team184;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;

public class GuardRobot extends BaseRobot {

	Direction d = Direction.EAST;
	public GuardRobot(RobotController rc){
		super(rc);
	}

	@Override
	public void run() throws GameActionException {
			guardCode();
	}

	private void guardCode() throws GameActionException {
		RobotInfo[] enemyArray = rc.senseHostileRobots(rc.getLocation(), 1000000);
		RobotInfo[] allies = rc.senseNearbyRobots(100, myTeam);
		if(enemyArray.length > 2*allies.length){
			tryToRetreat(enemyArray);
		}
		if(enemyArray.length>0){
			if(rc.isWeaponReady()){
				//look for adjacent enemies to attack
				RobotInfo bestTarget = null;
				double lowestHP = 99999;
				for(RobotInfo oneEnemy:enemyArray){
					if(rc.canAttackLocation(oneEnemy.location) && oneEnemy.health < lowestHP){
						bestTarget = oneEnemy;
						lowestHP = oneEnemy.health;
					}
				}
				if(bestTarget != null){
					rc.setIndicatorString(0,"trying to attack");
					rc.attackLocation(bestTarget.location);
				}
			}
			//could not find any enemies adjacent to attack
			//try to move toward them
			if(rc.isCoreReady()){
				MapLocation goal = enemyArray[0].location;
				Direction toEnemy = rc.getLocation().directionTo(goal);
				if(rc.senseRubble(rc.getLocation().add(toEnemy))>=200){
					tryToMove(randomDirection());
				}
				tryToMove(toEnemy);
			}
		}else{//there are no enemies nearby
			//check to see if we are in the way of friends
			//we are obstructing them
			if(rc.isCoreReady()){
				RobotInfo[] nearbyFriends = rc.senseNearbyRobots(2, myTeam);
				if(nearbyFriends.length>3){
					Direction away = randomDirection();
					tryToMove(away);
				}else{//maybe a friend is in need!
					RobotInfo[] alliesToHelp = rc.senseNearbyRobots(1000000,myTeam);
					RobotInfo weakestOneRob = Utility.getRobotWithLowestHP(alliesToHelp);
					if(weakestOneRob!=null){//found a friend most in need
						MapLocation weakestOne = weakestOneRob.location;
						Direction towardFriend = rc.getLocation().directionTo(weakestOne);
						tryToMove(towardFriend);
					}
				}
			}
		}
	}

	public void forward() throws GameActionException{
		RobotInfo[] enemyInfo = rc.senseHostileRobots(rc.getLocation(), rc.getType().attackRadiusSquared);
		if(rc.isWeaponReady()){
			if(enemyInfo.length > 0 && rc.canAttackLocation(enemyInfo[0].location)){
				rc.attackLocation(enemyInfo[0].location);
			}
		}
		if(rc.isCoreReady()){
			if(enemyInfo.length > 0){
				d = rc.getLocation().directionTo(enemyInfo[0].location);
			}
			BugNav.goTo(enemyInfo[0].location);
		}
	}
}
