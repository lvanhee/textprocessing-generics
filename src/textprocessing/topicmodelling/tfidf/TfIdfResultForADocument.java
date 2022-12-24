package textprocessing.topicmodelling.tfidf;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import textprocessing.BagOfWords;

public class TfIdfResultForADocument {
	
	private final Map<String, Double> m;

	public TfIdfResultForADocument(Map<String, Double> m2) {
		this.m = m2;
	}

	public static TfIdfResultForADocument newInstance(Map<String, Double> d) {
		return new TfIdfResultForADocument(d);
	}
	
	public SortedSet<String> getSortedTfIdsKeywords() {
		SortedSet<String> sortedScores = new TreeSet<String>((x,y)->{
			if(m.get(x).equals(m.get(y)))
				return x.compareTo(y);
			else return m.get(x).compareTo(m.get(y));
			});
		sortedScores.addAll(m.keySet());
		return sortedScores;
	}
	
	public String toStringTop(int nb, boolean showScores)
	{
		String res = "";
		SortedSet<String> sorted = getSortedTfIdsKeywords(); 
		for(int i = 0 ; i < nb; i++)
		{
			String s1 = sorted.last();
			sorted.remove(sorted.last());
			res+=s1;
			if(showScores)
				res+=m.get(s1);
			res+=";";
		}
		
		return res;
	}

	public Set<String> getKeywords() {
		return m.keySet();
	}

	public Double getScoreOf(String s) {
		return m.get(s);
	}

}
