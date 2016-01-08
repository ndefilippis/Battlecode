package kiting;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Random;
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
/*Base Robot class for implementing
 * 
 * 
 * 
 * 
 */
public abstract class BaseRobot {

	protected static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
            Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
    
	protected Stack<Action> moves = null;
	protected RobotController rc;
	Random random;
	
	public BaseRobot(RobotController rc){
		this.rc = rc;
		moves = new Stack<Action>();
		random = new Random(rc.getID()*rc.getRoundNum());
	}
	
	public void loop(){
		while(true){
			prerun();
			
			run();
			
			postrun();
			
			
		}
	}
	
	private void postrun() {
		Clock.yield();
	}

	public abstract void run();

	private void prerun() {
		// TODO Auto-generated method stub
		
	}

	protected void defaultBehavior() {
	        RobotInfo[] ri = rc.senseNearbyRobots();
	        RobotInfo sense = null;
	        for (RobotInfo r : ri) {
	            if (r.team != rc.getTeam()) {
	                sense = r;
	                break;
	            }
	        }
	        if (sense != null) {
	            MapLocation l = sense.location;
	            if (rc.canAttackLocation(l) && rc.getType().canAttack() && rc.isWeaponReady() && sense.team != rc.getTeam()) {
	                try {
	                    rc.attackLocation(l);
	                } catch (GameActionException e) {
	                    e.printStackTrace();
	                }
	            } else {
	                if (moves.isEmpty() && rc.getType().canMove()) {
	                    moves = bestPathTo(l, rc);
	
	                } else if (rc.isCoreReady()) {
	                    move(moves.pop(), rc);
	                }
	            }
	        } else {
	            Direction d = directions[(int) (8 * Math.random())];
	            if (rc.canMove(d) && rc.isCoreReady()) {
	                try {
	                    rc.move(d);
	                } catch (GameActionException e) {
	                    e.printStackTrace();
	                }
	            }
	        }
	    }
	
	
	public static void move(Action action, RobotController rc) {
	    if (action.type == MyActionType.DIG) {
	        try {
	            rc.clearRubble(rc.getLocation().directionTo(action.location));
	        } catch (GameActionException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
	    }
	    if (action.type == MyActionType.MOVE) {
	        try {
	            if (rc.canMove(rc.getLocation().directionTo(action.location)))
	                rc.move(rc.getLocation().directionTo(action.location));
	        } catch (GameActionException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
	    }
	}

	public static Stack<Action> bestPathTo(MapLocation goal, RobotController rc) {
	    PriorityQueue<Action> front = new PriorityQueue<Action>();
	    Set<MapLocation> visited = new HashSet<MapLocation>();
	    Action start = new Action(
	            rc.getLocation(),
	            goal,
	            MyActionType.YIELD,
	            0
	    );
	    double minCost = start.mannhattan() * 40;
	    Action minAction = start;
	    front.add(start);
	    while (!front.isEmpty()) {
	        Action curr = front.poll();
	        if (curr.equals(goal)) {
	            minAction = curr;
	            break;
	        }
	        for (Direction d : Direction.values()) {
	            Action toAdd;
	            double mult = 1;
	            if (d == Direction.NONE || d == Direction.OMNI) {
	                continue;
	            }
	            if (d == Direction.NORTH_EAST || d == Direction.NORTH_EAST || d == Direction.NORTH_EAST || d == Direction.NORTH_EAST) {
	                mult = GameConstants.DIAGONAL_DELAY_MULTIPLIER;
	            }
	            MapLocation test = curr.location.add(d);
	            try {
	                if (test.distanceSquaredTo(rc.getLocation()) <= rc.getType().attackRadiusSquared && rc.onTheMap(test) && !visited.contains(test) && rc.senseRobotAtLocation(test) == null) {
	                    if (rc.senseRubble(test) >= 100) {
	                        toAdd = new Action(test, goal, MyActionType.DIG, curr.cost + 2);
	                        visited.add(test);
	                    } else {
	                        toAdd = new Action(test, goal, MyActionType.MOVE, curr.cost + 1 * mult);
	                        visited.add(test);
	                    }
	                    toAdd.cameFrom = curr;
	                    if (toAdd.mannhattan() < minCost) {
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
	    while (minAction.cameFrom != null) {
	        moves.push(minAction);
	        minAction = minAction.cameFrom;
	    }
	    return moves;
	}
	
	
}
