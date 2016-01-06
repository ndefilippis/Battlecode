package team184;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;


public class RobotPlayer {

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    
    public static Stack<Action> pathTo(MapLocation goal, RobotController rc){
    	PriorityQueue<Action> front = new PriorityQueue<Action>();
    	Set<MapLocation> visited = new HashSet<MapLocation>();
    	Action start = new Action(
    		rc.getLocation(), 
    		goal,
    		MyActionType.YIELD, 
    		0
    	);
    	double minCost = start.mannhattan()*40;
    	Action minAction = start;
    	front.add(start);
    	while(!front.isEmpty()){
    		Action curr = front.poll();
    		if(curr.equals(goal)){
    			minAction = curr;
    			break;
    		}
    		double cost = curr.cost;
    		for(Direction d : Direction.values()){
    			Action toAdd;
    			double mult = 1;
    			if(d == Direction.NONE || d == Direction.OMNI){
    				continue;
    			}
    			if(d == Direction.NORTH_EAST || d == Direction.NORTH_EAST || d == Direction.NORTH_EAST || d == Direction.NORTH_EAST){
    				mult = GameConstants.DIAGONAL_DELAY_MULTIPLIER;
    			}
    			MapLocation test = curr.location.add(d);
    			try {
					if(test.distanceSquaredTo(rc.getLocation()) <= rc.getType().attackRadiusSquared && rc.onTheMap(test) && !visited.contains(test) && rc.senseRobotAtLocation(test) == null){
						if(rc.senseRubble(test) >= 100){
							toAdd = new Action(test, goal, MyActionType.DIG, curr.cost+2);
							visited.add(test);
						}
						else{
							toAdd = new Action(test, goal, MyActionType.MOVE, curr.cost+1*mult);
							visited.add(test);
						}
						toAdd.cameFrom = curr;
						if(toAdd.mannhattan() < minCost){
							minCost = toAdd.mannhattan();
							minAction = toAdd;
						}
						front.add(toAdd);
					}
				} catch (GameActionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	}
    	Stack<Action> moves = new Stack<Action>();
    	while(minAction.cameFrom != null){
    		moves.push(minAction);
    		minAction = minAction.cameFrom;
    	}
    	return moves;
    }
    
    public static void move(Action action, RobotController rc){
    	System.out.println(action);
    	if(action.type == MyActionType.DIG){
    		try {
				rc.clearRubble(rc.getLocation().directionTo(action.location));
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	if(action.type == MyActionType.MOVE){
    		try {
    			if(rc.canMove(rc.getLocation().directionTo(action.location)))
					rc.move(rc.getLocation().directionTo(action.location));
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
    public static void run(RobotController rc) {
    	Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
                Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
    	RobotType[] robotTypes = {RobotType.SCOUT, RobotType.SOLDIER, RobotType.SOLDIER, RobotType.SOLDIER,
                RobotType.GUARD, RobotType.GUARD, RobotType.VIPER};
                    
    	Stack<Action> moves = null;
        if (rc.getType() == RobotType.ARCHON) {
            try {
            	
                // Any code here gets executed exactly once at the beginning of the game.
            } catch (Exception e) {
                // Throwing an uncaught exception makes the robot die, so we need to catch exceptions.
                // Caught exceptions will result in a bytecode penalty.
                System.out.println(e.getMessage());
                e.printStackTrace();
            }

            while (true) {
                // This is a loop to prevent the run() method from returning. Because of the Clock.yield()
                // at the end of it, the loop will iterate once per game round.
                try {
                	int i = (int)(7*Math.random());
                	if(rc.canBuild(Direction.NORTH, robotTypes[i])){
                		if(rc.isCoreReady()){
                			rc.build(Direction.NORTH, robotTypes[i]);
                		}
                	}
                    Clock.yield();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        } else if (rc.getType() != RobotType.ARCHON) {
            try {
            	moves = new Stack<Action>();
                // Any code here gets executed exactly once at the beginning of the game.
            } catch (Exception e) {
                // Throwing an uncaught exception makes the robot die, so we need to catch exceptions.
                // Caught exceptions will result in a bytecode penalty.
                System.out.println(e.getMessage());
                e.printStackTrace();
            }

            while (true) {
            	RobotInfo[] ri = rc.senseNearbyRobots();
            	RobotInfo sense = null;
            	for(RobotInfo  r : ri){
            		if(r.team != rc.getTeam()){
            			sense = r;
            			break;
            		}
            	}
            	if(sense != null){
            		MapLocation l = sense.location;
            		if(rc.canAttackLocation(l) && rc.getType().canAttack() && rc.isWeaponReady() && sense.team != rc.getTeam()){
            			try {
							rc.attackLocation(l);
						} catch (GameActionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
            		}
            		else{
            			if(moves.isEmpty() && rc.getType().canMove()){
            				moves = pathTo(l, rc);
            				
            			}
            			else if(rc.isCoreReady()){
                			move(moves.pop(), rc);
            			}
            		}
            	}
            	else{
            		Direction d = directions[(int)(8*Math.random())];
            		if(rc.canMove(d) && rc.isCoreReady()){
            			try {
							rc.move(d);
						} catch (GameActionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
            		}
            	}
                try {
                    Clock.yield();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}
