package turtle;

import battlecode.common.*;

public class Combat extends BaseRobot{

	public Combat(RobotController rc) {
		super(rc);
		// TODO Auto-generated constructor stub
	}


	public boolean isEnemyStronger(){
		RobotInfo[] enemies = rc.senseNearbyRobots(100, otherTeam);
		RobotInfo[] allies = rc.senseNearbyRobots(100, myTeam);
		int allyScore = 0;
		int enemyScore = 0;
		
		for(RobotInfo ri : enemies){
			if(ri.type.canAttack())
				enemyScore += ri.health*ri.attackPower/ri.weaponDelay;
		}
		for(RobotInfo ri : allies){
			if(ri.type.canAttack())
				allyScore += ri.health*ri.attackPower/ri.weaponDelay;
		}
		return enemyScore > allyScore;
	}
}
