package team184;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Signal;
import battlecode.common.Team;

public class MessageSignal {
	private int[] message;
	public static enum MessageType {
		ROBOT (0),
		PARTS (1),
		COMMAND (2),
		RUBBLE (3),
		MAP_EDGE (4);
		
		int n;
		MessageType(int n){
			this.n = n;
		}
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
	 * 		pos 25-32: control bits
	 * 		pos 11-14: robotType
	 * 		pos 9-10:  team
	 * 		pos 1-8:   dx, dy
	 *   parts:
	 *   	number of parts:
	 */
	 
	public MessageSignal(RobotInfo ri){
		message = new int[2];
		if(ri.type == RobotType.SCOUT){
			message[0] ^= 1 << 31;
		}
	}
	public MessageSignal(Signal signal){
		this.signal = signal;
		message = signal.getMessage();
	}
	
	public int[] getMessage(){
		return message;
	}
	
	public void setMessageType(MessageType type){
		message[0] ^= type.n << 28;
	}
	public MessageType getMessageType(){
		return MessageType.values()[message[0] >> 28 & 0x7];
	}
	
	public void setPingedLocation(int dx, int dy){
		if(dx > 8 || dy > 8 || dx < -8 || dy < -8){
			System.out.println("offset too big");
			return;
		}
		message[1] ^= dx+8;
		message[1] ^= (dy+8) << 4;
	}
	public MapLocation getPingedLocation(){
		int dx = message[1] & 0xf - 8;
		int dy = (message[1] >> 4 & 0xf) - 8;
		return signal.getLocation().add(dx, dy);
	}
	
	public void setPingedTeam(Team t){
		for(int i = Team.values().length; --i>=0;){
			if(Team.values()[i] == t){
				message[1] ^= i << 8;
			}
		}
	}
	public Team getPingedTeam(){
		return Team.values()[message[1] >> 8 & 0x3];
	}

	public void setPingedType(RobotType t){
		for(int i = RobotType.values().length; --i>=0;){
			if(RobotType.values()[i] == t){
				message[1] ^= i << 10;
			}
		}
	}
	public RobotType getPingedType(){
		return RobotType.values()[message[1] >> 10 & 0xf];	
	}



	public void setPingedParts(int parts){
		message[1] = parts;
	}
	public int getPingedParts(){
		return message[1];
	}


}
