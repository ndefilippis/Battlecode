package turrets;

import battlecode.common.MapLocation;
import battlecode.common.Signal;

public class SignalMessage {
	int[] message;
	Signal signal;
	
	/*	int1: 
	 * 		pos 25-32
	 * 		pos 17-24
	 * 		pos 9-16
	 * 		pos 1-8
	 * 
	 * int2:
	 * 		pos 25-32
	 * 		pos 17-24
	 * 		pos 9-16: 
	 * 		pos 1-8: dx, dy
	 */
	public SignalMessage(){
		message = new int[2];
	}
	public SignalMessage(Signal signal){
		this.signal = signal;
		message = signal.getMessage();
	}
	
	public void setLocation(int dx, int dy){
		if(dx > 32 || dy > 32){
			System.out.println("offset too big");
			return;
		}
		message[1] ^= dx;
		message[1] ^= dy << 4;
	}
	
	public MapLocation getPingedLocation(){
		int dx = message[1] & 0xf;
		int dy = message[1] & 0xf0 >> 4;
		return signal.getLocation().add(dx, dy);
	}
}
