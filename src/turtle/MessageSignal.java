package turtle;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Signal;
import battlecode.common.Team;

//Important!: Make sure to send right after making a signal

public class MessageSignal{
	private int[] message;
	private RobotController rc;
	public static enum MessageType {
		ROBOT,
		PARTS,
		COMMAND,
		INFO,
		RUBBLE,
		MAP_EDGE;
	}
	
	public static enum CommandType{
		MOVE,
		ATTACK,
		DIG,
		MOVE_AWAY
	}
	
	Signal signal;

	/*	int1: 
	 * 		pos 32: 0 if archon, 1 if scout;
	 * 		pos 29-31: message type
	 * 		pos 17-24
	 * 		pos 9-16
	 * 		pos 1-8
	 * 
	 * int2:
	 * 	other robots:
	 * 		pos 24-32: control bits
	 * 		pos 20-23: robotType
	 * 		pos 18-19:  team
	 * 		pos 9-17:  dy
	 * 		pos 1-8:   dx
	 *   parts:
	 *   	pos 28-32: control bits
	 *   	pos 18-27: number of parts
	 *   	pos 9-17: dy
	 *   	pos 1-8: dx
	 *   command:
	 *   	pos 24-32: control bits
	 *   	pos 20-23: command type
	 *   	pos 9-17: dy
	 *   	pos 1-8: dx
	 *   map edge:
	 *   	pos	20-23: edge
	 *   	pos 9-17:  dy
	 *   	pos 1-8:   dx
	 */
	 
	public void setMapEdge(MapLocation ml, Direction edge){
	 	setPingedLocation(ml);
	 	setPingedDirection(edge);
	 }
	 private void setPingedDirection(Direction d){
	 	message[1] ^= d.ordinal() << 19;
	 }
	 public Direction getPingedDirection(){
	 	return Direction.values()[message[1] >> 19 & 0xf];
	 }

	public MessageSignal(RobotController rc){
		message = new int[2];
		if(rc.getType() == RobotType.SCOUT){
			message[0] ^= 1 << 31;
		}
		this.rc = rc;
	}
	public MessageSignal(Signal signal){
		this.signal = signal;
		message = signal.getMessage();
	}
	

	public int[] getMessage(){
		return message;
	}
	
	public void setParts(MapLocation ml, double amount){
		setMessageType(MessageType.PARTS);
		setPingedLocation(ml);
		setPingedParts((int)amount);
	}
	
	public void setRobot(MapLocation ml, Team team, RobotType type){
		setMessageType(MessageType.ROBOT);
		setPingedType(type);
		setPingedTeam(team);
		setPingedLocation(ml);
	}

	public void setCommand(MapLocation ml, CommandType type){
		setMessageType(MessageType.COMMAND);
		setPingedLocation(ml);
		setCommandType(type);
	}
	
	private void setCommandType(CommandType type){
		message[1] ^= type.ordinal() << 19;
	}
	public CommandType getCommandType(){
		return CommandType.values()[message[1] >> 19 & 0xf];
	}

	private void setMessageType(MessageType type){
		message[0] ^= type.ordinal() << 28;
	}
	public MessageType getMessageType(){
		return MessageType.values()[message[0] >> 28 & 0x7];
	}

	private void setPingedLocation(int dx, int dy){
		message[1] ^= dx+127;
		message[1] ^= (dy+127) << 8;
	}
	public MapLocation getPingedLocation(){
		
		int dx = (message[1] & 0xff) - 127;
		int dy = (message[1] >> 8 & 0xff) - 127;
		return signal.getLocation().add(dx, dy);
	}

	private void setPingedTeam(Team t){
		message[1] ^= t.ordinal() << 17;
	}
	public Team getPingedTeam(){
		return Team.values()[message[1] >> 17 & 0x3];
	}

	private void setPingedType(RobotType t){
		message[1] ^= t.ordinal() << 19;
	}
	public RobotType getPingedType(){
		return RobotType.values()[message[1] >> 19 & 0xf];	
	}



	private void setPingedParts(int parts){
		message[1] = Math.min(parts, 1023) << 17;
	}
	public int getPingedParts(){
		return message[1] >> 17 & 0x3ff;
	}

	public boolean send(int radiusSquared) throws GameActionException{
		if(rc.getMessageSignalCount() < GameConstants.MESSAGE_SIGNALS_PER_TURN){
			rc.broadcastMessageSignal(message[0], message[1], radiusSquared);
			return true;
		}
		return false;
	}
	private void setPingedLocation(MapLocation goal) {
		int dx = goal.x - rc.getLocation().x;
		int dy = goal.y - rc.getLocation().y;
		setPingedLocation(dx, dy);
	}
}
