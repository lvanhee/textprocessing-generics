package textprocessing.topicmodelling.lda.model;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LdaPrettyfier {

	public static Map<Integer, Color> getColorPerTopic(int nbOfTopics) {
		Map<Integer, Float> huePerTopic = getHuePerTopic(nbOfTopics);
		return huePerTopic.keySet().stream().collect(Collectors.toMap(Function.identity(), 
				x->new Color(Color.HSBtoRGB(huePerTopic.get(x), .8f, 1f))));
	}

	public static Map<Integer, Float> getHuePerTopic(int nbTopics) {
		Map<Integer, Float> res = new HashMap<>();
		
		for(int i = 0 ;  i < nbTopics ; i++)
			res.put(i,1f/nbTopics*i);
		return res;
	}


}
