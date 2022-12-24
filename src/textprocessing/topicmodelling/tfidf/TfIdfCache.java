package textprocessing.topicmodelling.tfidf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import textprocessing.BagOfWords;

public class TfIdfCache {
	
	private List<BagOfWords> corpusAsBows=null;
	private Map<String, Double> frequencies = null;
	private Map<String, Double> idfValuePerWord = new HashMap<>();

	public static TfIdfCache newInstance() {
		return new TfIdfCache();
	}

	public void setCorpus(List<BagOfWords> globalCorpusBag) {
		this.corpusAsBows = globalCorpusBag;
	}

	public boolean hasPrecomputedCorpus() {
		// TODO Auto-generated method stub
		return corpusAsBows != null;
	}

	public List<BagOfWords> getPrecomputedCorpus() {
		return corpusAsBows;
	}

	public boolean containsFrequencies() {
		return frequencies!= null;
	}

	public Map<String, Double> getFrequencies() {
		return frequencies;
	}

	public void setFrequencies(Map<String, Double> allFrequencies) {
		this.frequencies = allFrequencies;
	}

	public boolean containsIdfValuefor(String t) {
		return idfValuePerWord.containsKey(t);
	}

	public double getIdfValueFor(String t) {
		return idfValuePerWord.get(t);
	}

	public void putIdfValueFor(String t, double res) {
		idfValuePerWord.put(t, res);
	}

}
