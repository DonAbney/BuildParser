package utils.build.parser.test;

import static org.junit.Assert.*;
import utils.build.parser.BuildParser
import groovy.time.*

class TestBuildParser extends GroovyTestCase {
	
	BuildParser parser
	
	public void setUp() {
		File inputFile = new File("test/testFiles/rssAllCT.xml")
		parser = new BuildParser(inputFile)
	}
	
	public void testParseFile() {
		
		Integer brokenCount = parser.parseFile()
		assertTrue(brokenCount > 0)
	}
	
	public void testCalculateDuration() {
		
		long duration = parser.calculateDuration("2013-02-14T18:17:41Z", "2013-02-14T18:17:41Z")
		assertEquals(0, duration)
	}
	
	public void testCalculateDurationForOverall() {
		
		long duration = parser.calculateDuration("2013-02-14T18:17:41Z", "2013-02-07T19:37:52Z")
		assertEquals(599989000, duration)
	}

	public void testFormatTimeReturnsGoodStringForValidDuration() {
		
		long duration = 599989000
		assertEquals("166 hours, 39 min", parser.formatTime(duration))
	}
}
