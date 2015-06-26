

import java.util.List;
import java.util.HashMap; 
import java.util.Map;

public class ParticleContainer {
	
	private Map<Integer, List<Particle>> particles = new HashMap<Integer, List<Particle>>();
	
	private static ParticleContainer instance;
	
	public Map<Integer, List<Particle>> getParticles() {
		return particles;
	}

	private ParticleContainer() {		
	}

	public static ParticleContainer getInstance () {
	    if (instance == null) {
	    	instance = new ParticleContainer();
	    }
	    return instance;
	  }
	
}