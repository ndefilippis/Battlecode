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
		int i = 0;
		int rcx = 553;
		int rcy = 127;
		int pinglocx = 559;
		int pinglocy = 124;

		int dx = rcx-pinglocx;
		int dy = rcy-pinglocy;
		System.out.println(dx + " " + dy);
		i ^= dx+127;
		System.out.print(i);
		i ^= (dy+127) << 8;
		System.out.println(" "+i);
		
		int ndx = (i & 0xff) - 127;
		int ndy = (i >> 8 & 0xff) - 127;
		System.out.println(ndx + " " + ndy);
		
	}
}
