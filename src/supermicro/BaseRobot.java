package supermicro;

import battlecode.common.*;

import java.util.*;
/*Base Robot class for implementing
 * 
 * 
 * 
 * 
 */
public abstract class BaseRobot {
	protected static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
            Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	static int[] tryDirections = {0,-1,1,-2,2};
	protected Stack<Action> moves = null;
	protected RobotController rc;
	protected Team myTeam;
	Random random;
	
	public BaseRobot(RobotController rc){
		this.rc = rc;
		myTeam = rc.getTeam();
		moves = new Stack<Action>();
		random = new Random(rc.getID());
	}
	
	public void initialize(){
		
	}
	
	public void loop(){
		while(true){
			prerun();
			
			try {
				run();
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			postrun();
			
			
		}
	}
	
	private void postrun() {
		Clock.yield();
	}

	public abstract void run() throws GameActionException;

	private void prerun() {
		// TODO Auto-generated method stub
		
	}

	protected void defaultBehavior() {
	        RobotInfo[] ri = rc.senseNearbyRobots();
	        RobotInfo sense = null;
	        for (RobotInfo r : ri) {
	            if (r.team != myTeam) {
	                sense = r;
	                break;
	            }
	        }
	        if (sense != null) {
	            MapLocation l = sense.location;
	            if (rc.canAttackLocation(l) && rc.getType().canAttack() && rc.isWeaponReady() && sense.team != myTeam) {
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
	            Direction d = directions[random.nextInt(8)];
	            if (rc.canMove(d) && rc.isCoreReady()) {
	                try {
	                    rc.move(d);
	                } catch (GameActionException e) {
	                    e.printStackTrace();
	                }
	            }
	        }
	    }
	
	protected boolean tryToRetreat(RobotInfo[] nearbyEnemies) throws GameActionException {
        Direction bestRetreatDir = null;
        RobotInfo currentClosestEnemy = Utility.closest(nearbyEnemies, rc.getLocation());

        int bestDistSq = rc.getLocation().distanceSquaredTo(currentClosestEnemy.location);
        for (Direction dir : Direction.values()) {
            if (!rc.canMove(dir)) continue;

            MapLocation retreatLoc = rc.getLocation().add(dir);

            RobotInfo closestEnemy = Utility.closest(nearbyEnemies, retreatLoc);
            int distSq = retreatLoc.distanceSquaredTo(closestEnemy.location);
            if (distSq > bestDistSq) {
                bestDistSq = distSq;
                bestRetreatDir = dir;
            }
        }

        if (bestRetreatDir != null && rc.isCoreReady()) {
            rc.move(bestRetreatDir);
            return true;
        }
        return false;
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
	            if (d.isDiagonal()) {
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

	public void tryToMove(Direction forward) throws GameActionException {
		if(rc.isCoreReady()){
			for(int deltaD:tryDirections){
				Direction maybeForward = Direction.values()[(forward.ordinal()+deltaD+8)%8];
				if(rc.canMove(maybeForward)){
					rc.move(maybeForward);
					return;
				}
			}
			if(rc.getType().canClearRubble()){
				//failed to move, look to clear rubble
				MapLocation ahead = rc.getLocation().add(forward);
				if(rc.senseRubble(ahead)>=GameConstants.RUBBLE_OBSTRUCTION_THRESH){
					rc.clearRubble(forward);
				}
			}
		}
	}
	
	public Direction randomDirection() {
		return Direction.values()[(int)(random.nextDouble()*8)];
	}

}
