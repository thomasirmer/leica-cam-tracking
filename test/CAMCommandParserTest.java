import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Hashtable;

import org.junit.Before;
import org.junit.Test;


public class CAMCommandParserTest {

	CAMCommandParser classToTest;
	
	@Before
	public void setUp() throws Exception {
		classToTest = new CAMCommandParser();
	}

	@Test
	public void command_should_be_splitted_by_slash_and_colon() {
		String camCommand = "/app:matrix /sys:1 /dev:stage /info_for:clientPC /unit:meter /xpos:0,063 /ypos:0,04118 /zpos:-0,0000000204";
		Hashtable<String, String> result = classToTest.parseCAMCommand(camCommand);
		
		Boolean containsApp = result.containsKey("app");
		assertTrue(containsApp);
		String app = result.get("app");
		assertEquals("matrix", app);	
		
		Boolean containsSys = result.containsKey("sys");
		assertTrue(containsSys);
		String sys = result.get("sys");
		assertEquals("1", sys);
		
		Boolean containsDev = result.containsKey("dev");
		assertTrue(containsDev);
		String dev = result.get("dev");
		assertEquals("stage", dev); 
		
		Boolean containsInfo_for = result.containsKey("info_for");
		assertTrue(containsInfo_for);
		String info = result.get("info_for");
		assertEquals("clientPC", info); 
		
		Boolean containsUnit = result.containsKey("unit");
		assertTrue(containsUnit);
		String unit = result.get("unit");
		assertEquals("meter", unit);
		
		Boolean containsX = result.containsKey("xpos");
		assertTrue(containsX);
		String x = result.get("xpos");
		assertEquals("0,063", x);
		
		Boolean containsY = result.containsKey("ypos");
		assertTrue(containsY);
		String y = result.get("ypos");
		assertEquals("0,04118", y);
		
		Boolean containsZ = result.containsKey("zpos");
		assertTrue(containsZ);
		String z = result.get("zpos");
		assertEquals("-0,0000000204", z);
	}
}