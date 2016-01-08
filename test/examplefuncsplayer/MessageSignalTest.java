package examplefuncsplayer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Signal;
import battlecode.common.Team;
import team184.MessageSignal;

public class MessageSignalTest {

	@Test
	public void testSanity() {
		assertEquals(2, 1+1);
	}
	
	@Test
	public void messageTest(){
		MessageSignal.MessageType type = MessageSignal.MessageType.ROBOT;
		int dx = 7;
		int dy = -7;
		Team team = Team.ZOMBIE;
		RobotType rtype = RobotType.ZOMBIEDEN;
		
		RobotInfo ri = new RobotInfo(345, Team.A, RobotType.SCOUT, new MapLocation(0,0), 0, 0, 0, 100, 100, 0, 0);
		MessageSignal send = new MessageSignal(ri);
		send.setMessageType(type);
		send.setPingedLocation(dx, dy);
		send.setPingedTeam(team);
		send.setPingedType(rtype);
		
		Signal sentSignal = new Signal(ri.location, ri.ID, ri.team, send.getMessage()[0], send.getMessage()[1]);
		MessageSignal recv = new MessageSignal(sentSignal);
		assertEquals(type, recv.getMessageType());
		assertEquals(ri.location.add(dx, dy), recv.getPingedLocation());
		assertEquals(team, recv.getPingedTeam());
		assertEquals(rtype, recv.getPingedType());
	}

}
