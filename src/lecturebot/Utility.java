package lecturebot;

import java.util.ArrayList;

import battlecode.common.*;

public class Utility {
	static int[] possibleDirections = new int[]{0, 1, -1, 2, -2, 3, -3, 4};
	static ArrayList<MapLocation> pastLocations = new ArrayList<MapLocation>();
	static int patient = 10;

	public static void fowardish(Direction ahead) throws GameActionException {
		RobotController rc = RobotPlayer.rc;
		int id = RobotPlayer.id;
		int waitTurns = id == 0 ? 6 : 1;
		if(rc.getRoundNum() % waitTurns == 0){

			for(int i : possibleDirections){
				Direction candidateDirection = Direction.values()[(ahead.ordinal()+i+8)%8];
				MapLocation candidateLocation = rc.getLocation().add(candidateDirection);

				if(patient>0){
					if(rc.canMove(candidateDirection) && !pastLocations.contains(candidateLocation)){
						pastLocations.add(rc.getLocation());
						if(pastLocations.size() > 5){
							pastLocations.remove(0);
						}

						rc.move(candidateDirection);
						patient = Math.min(30, patient+1);
						return;
					}
				}
				else{
					if(rc.canMove(candidateDirection)){
						rc.move(candidateDirection);
						patient = Math.min(30, patient+1);
						return;
					}
					if(rc.senseRubble(candidateLocation) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH){
						rc.clearRubble(candidateDirection);
						return;
					}
				}
			}
			patient -= 5;
		}
	}
	public static RobotInfo[] joinRobotInfo(RobotInfo[] zombieEnemies, RobotInfo[] normalEnemies) {
		RobotInfo[] opponentEnemies = new RobotInfo[zombieEnemies.length + normalEnemies.length];
		int index = 0;
		for(RobotInfo ri : zombieEnemies){
			opponentEnemies[index] = ri;
			index++;
		}
		for(RobotInfo ri : normalEnemies){
			opponentEnemies[index] = ri;
			index++;
		}
		return opponentEnemies;
	}
}
