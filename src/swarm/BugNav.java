package swarm;

import battlecode.common.*;

public class BugNav extends BaseRobot{
	public BugNav(RobotController rc) {
		super(rc);
	}

	private static MapLocation dest;

	private enum BugState{
		DIRECT, BUG
	}

	private enum WallSide{
		LEFT, RIGHT
	}

	private static BugState bugState;
	public static WallSide bugWallSide = WallSide.LEFT;
	private static int bugStartDistSq;
	private static Direction bugLastMoveDir;
	private static Direction bugLookStartDir;
	private static int bugRotationCount;
	private static int bugMovesSinceSeenObstacle = 0;

	private static boolean tryMoveDirect() throws GameActionException {
        Direction toDest = rc.getLocation().directionTo(dest);

        if (rc.canMove(toDest)) {
            rc.move(toDest);
            return true;
        }

        Direction[] dirs = new Direction[2];
        Direction dirLeft = toDest.rotateLeft();
        Direction dirRight = toDest.rotateRight();
        if (rc.getLocation().add(dirLeft).distanceSquaredTo(dest) < rc.getLocation().add(dirRight).distanceSquaredTo(dest)) {
            dirs[0] = dirLeft;
            dirs[1] = dirRight;
        } else {
            dirs[0] = dirRight;
            dirs[1] = dirLeft;
        }
        for (Direction dir : dirs) {
            if (rc.canMove(dir)) {
                rc.move(dir);
                return true;
            }
            if(rc.senseRubble(rc.getLocation().add(dir)) < 200 && rc.onTheMap(rc.getLocation().add(dir))){
            	rc.clearRubble(dir);
            	return true;
            }
        }
        return false;
    }
    
	private static void startBug() throws GameActionException {
        bugStartDistSq = rc.getLocation().distanceSquaredTo(dest);
        bugLastMoveDir = rc.getLocation().directionTo(dest);
        bugLookStartDir = rc.getLocation().directionTo(dest);
        bugRotationCount = 0;
        bugMovesSinceSeenObstacle = 0;

        Direction leftTryDir = bugLastMoveDir.rotateLeft();
        for (int i = 0; i < 3; i++) {
            if (!rc.canMove(leftTryDir)) leftTryDir = leftTryDir.rotateLeft();
            else break;
        }
        Direction rightTryDir = bugLastMoveDir.rotateRight();
        for (int i = 0; i < 3; i++) {
            if (!rc.canMove(rightTryDir)) rightTryDir = rightTryDir.rotateRight();
            else break;
        }
        if (dest.distanceSquaredTo(rc.getLocation().add(leftTryDir)) < dest.distanceSquaredTo(rc.getLocation().add(rightTryDir))) {
            bugWallSide = WallSide.RIGHT;
        } else {
            bugWallSide = WallSide.LEFT;
        }
    }

    private static Direction findBugMoveDir() throws GameActionException {
        bugMovesSinceSeenObstacle++;
        Direction dir = bugLookStartDir;
        for (int i = 8; i-- > 0;) {
            if (rc.canMove(dir)) return dir;
            dir = (bugWallSide == WallSide.LEFT ? dir.rotateRight() : dir.rotateLeft());
            bugMovesSinceSeenObstacle = 0;
        }
        return null;
    }
    private static int numRightRotations(Direction start, Direction end) {
        return (end.ordinal() - start.ordinal() + 8) % 8;
    }

    private static int numLeftRotations(Direction start, Direction end) {
        return (-end.ordinal() + start.ordinal() + 8) % 8;
    }
    private static int calculateBugRotation(Direction moveDir) {
        if (bugWallSide == WallSide.LEFT) {
            return numRightRotations(bugLookStartDir, moveDir) - numRightRotations(bugLookStartDir, bugLastMoveDir);
        } else {
            return numLeftRotations(bugLookStartDir, moveDir) - numLeftRotations(bugLookStartDir, bugLastMoveDir);
        }
    }
    private static void bugMove(Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
        	rc.move(dir);
            bugRotationCount += calculateBugRotation(dir);
            bugLastMoveDir = dir;
            if (bugWallSide == WallSide.LEFT) bugLookStartDir = dir.rotateLeft().rotateLeft();
            else bugLookStartDir = dir.rotateRight().rotateRight();
        }
    }
    private static boolean detectBugIntoEdge() throws GameActionException {
        if (bugWallSide == WallSide.LEFT) {
            return !rc.onTheMap(rc.getLocation().add(bugLastMoveDir.rotateLeft()));
        } else {
            return !rc.onTheMap(rc.getLocation().add(bugLastMoveDir.rotateRight()));
        }
    }
    
    private static void reverseBugWallFollowDir() throws GameActionException {
        bugWallSide = (bugWallSide == WallSide.LEFT ? WallSide.RIGHT : WallSide.LEFT);
        startBug();
    }
    private static void bugTurn() throws GameActionException {
        if (detectBugIntoEdge()) {
            reverseBugWallFollowDir();
        }
        Direction dir = findBugMoveDir();
        if (dir != null) {
            bugMove(dir);
        }
    }

    private static boolean canEndBug() {
        if (bugMovesSinceSeenObstacle >= 4) return true;
        return (bugRotationCount <= 0 || bugRotationCount >= 8) && rc.getLocation().distanceSquaredTo(dest) <= bugStartDistSq;
    }

    private static void bugMove() throws GameActionException {
        if (bugState == BugState.BUG) {
            if (canEndBug()) {
                bugState = BugState.DIRECT;
            }
        }
        if (bugState == BugState.DIRECT) {
            if (!tryMoveDirect()) {
                bugState = BugState.BUG;
                startBug();
            }
        }
        if (bugState == BugState.BUG) {
            bugTurn();
        }
    }
    public static void goTo(MapLocation theDest) throws GameActionException {
        if (!theDest.equals(dest)) {
            dest = theDest;
            bugState = BugState.DIRECT;
        }

        if (rc.getLocation().equals(dest)) return;

        bugMove();
    }

	@Override
	public void run() throws GameActionException {
		// TODO Auto-generated method stub

	}
}
