package lecturebot;

import java.util.ArrayList;

import battlecode.common.*;

public class RobotPlayer {
	static Direction movingDirection = Direction.NORTH_WEST;
	static RobotController rc;
	static MapLocation archonLocation;
	static int id = -1;
	
	
	@SuppressWarnings("unused")
	public static void run(RobotController rcIn) throws GameActionException {
		rc = rcIn;
		archonLocation = rc.getLocation();
		if(rc.getTeam() == Team.B){
			movingDirection = Direction.SOUTH_EAST;
		}

		while(true){
			try{
				signaling();
				repeat();
				Clock.yield();
				
			}
			catch(GameActionException e){
				e.printStackTrace();
			}
		}
	}
	private static void signaling() throws GameActionException {
		if(rc.getType() == RobotType.ARCHON){
			if(rc.getRoundNum() == 0){
				Signal[] incomingMessages = rc.emptySignalQueue();
				id = incomingMessages.length;
				rc.broadcastMessageSignal(0, 0, 100);
			}
			else{
				if(id == 0){
					sendinstructions();
				}
				else{
					followinstructions();
				}
			}
		}
		else{
			followinstructions();
		}
	}
	private static void sendinstructions() throws GameActionException {
		MapLocation aheadLocation = rc.getLocation().add(movingDirection);
		if(!rc.onTheMap(aheadLocation) || rc.getRoundNum() % 200 == 199){
			movingDirection = randomDirection();
		}
		
		rc.broadcastMessageSignal(rc.getTeam().ordinal(), movingDirection.ordinal(), 1000);
	}
	private static Direction randomDirection() {
		// TODO Auto-generated method stub
		return Direction.values()[(int)(Math.random()*8)];
	}
	private static void followinstructions() {
		Signal[] incomingMessages = rc.emptySignalQueue();
		Signal currentMessage = null;
		for(int messageIndex = 0; messageIndex < incomingMessages.length; messageIndex++){
			if(incomingMessages[messageIndex].getMessage() != null && rc.getTeam().ordinal()==incomingMessages[messageIndex].getMessage()[0]){
				currentMessage = incomingMessages[messageIndex];
				break;
			}
		}
		if(currentMessage == null){
			return;
		}
		archonLocation = currentMessage.getLocation();
		Direction archonDirection = Direction.values()[currentMessage.getMessage()[1]];
		MapLocation goalLocation = archonLocation.add(archonDirection, 5);
		movingDirection = rc.getLocation().directionTo(goalLocation);
	}
	public static void repeat() throws GameActionException{
		RobotInfo[] zombieEnemies = rc.senseNearbyRobots(rc.getType().attackRadiusSquared, Team.ZOMBIE);
		RobotInfo[] normalEnemies = rc.senseNearbyRobots(rc.getType().attackRadiusSquared, rc.getTeam().opponent());
		
		RobotInfo[] opponentEnemies = Utility.joinRobotInfo(zombieEnemies, normalEnemies);
		
		int distToPack = rc.getLocation().distanceSquaredTo(archonLocation);
		
		if(opponentEnemies.length > 0 && rc.getType().canAttack() && distToPack < 36){
			if(rc.isWeaponReady()){
				rc.attackLocation(opponentEnemies[0].location);
			}
		}
		else{
			if(rc.isCoreReady()){
				if(id > 0 && rc.canBuild(movingDirection, RobotType.VIPER)){
					rc.build(movingDirection, RobotType.VIPER);
					return;
				}
				Utility.fowardish(movingDirection);
			}
		}
	}
	
}
