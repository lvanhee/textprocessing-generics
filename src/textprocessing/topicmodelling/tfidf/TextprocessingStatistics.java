package textprocessing.topicmodelling.tfidf;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import textprocessing.BagOfWords;

public class TextprocessingStatistics {



	public static TfIdfResultForADocument getTfIdfScores(String stringToTest, Collection<String> globalCorpusSet, TfIdfCache tfidfCache) {
		
		List<BagOfWords> globalCorpusBag = null;
		if(tfidfCache.hasPrecomputedCorpus())
			globalCorpusBag = tfidfCache.getPrecomputedCorpus();
		else {
			globalCorpusBag = globalCorpusSet.parallelStream().map(x->BagOfWords.newInstance(x, y->{return true;}, true)).collect(Collectors.toList());
			tfidfCache.setCorpus(globalCorpusBag);
		}
		
		return getTfIdfScores(BagOfWords.newInstance(stringToTest, x->true, true), globalCorpusBag, tfidfCache);
	}

	public static TfIdfResultForADocument getTfIdfScores(BagOfWords query, Collection<BagOfWords> globalCorpusBag, TfIdfCache cache) {
		Set<String> allWords = query.getAllWords(query);
		
		Map<String, Double> allFrequenciesForLocalText = null;
		allFrequenciesForLocalText = query.getFrequencyMap();
		
		Map<String, Double> allFrequenciesFinal = allFrequenciesForLocalText;
		
		return TfIdfResultForADocument.newInstance(allWords.stream().collect(Collectors.toMap(Function.identity(), x->
		{
			double frequency = allFrequenciesFinal.get(x);
			double idf = idf(x,globalCorpusBag,cache);
			return frequency * idf;
		})));
	}
	
	public static TfIdfResultForADocument getTfIdfScoresBow(String query, Collection<BagOfWords> globalCorpusBag, TfIdfCache cache) {
		return getTfIdfScores(BagOfWords.newInstance(query, x->true, false), globalCorpusBag, cache);
	}
	
	private static Double idf(String t, Collection<BagOfWords> globalCorpusBag, TfIdfCache cache) {		
		
		if(cache.containsIdfValuefor(t)) {
			return cache.getIdfValueFor(t);
		}
		
		long nbOfDocumentsHoldingStringInCorpus = globalCorpusBag.stream().filter(x->x.contains(t)).count();
		double ratio = (double) globalCorpusBag.size()/(double)nbOfDocumentsHoldingStringInCorpus;
		double res = Math.log(ratio);
		cache.putIdfValueFor(t, res);
		return res;
	}
	
	public static void printNGram(Map<List<String>, Integer> allNGrams) {
		List<List<String>> sorted = allNGrams.keySet().stream().collect(Collectors.toList());
		
		Collections.sort(sorted, (x,y)->{
			if(allNGrams.get(x)>allNGrams.get(y))return -1;
			if(allNGrams.get(x)<allNGrams.get(y))return 1;
			return x.toString().compareTo(y.toString());
		});
		
		for(List<String> l :sorted)
			System.out.println(l+":"+allNGrams.get(l));
		
	}

	public static Map<String, Double> getMostFrequentTfIdfTopics(Collection<TfIdfResultForADocument> values) {
		Map<String, Double> sum = new HashMap<>();
		for(TfIdfResultForADocument tid: values)
		{
			Map<String, Double> localMap = new HashMap<>();
			double totalWeight = tid.getKeywords().stream().map(x->tid.getScoreOf(x)).reduce(0d, (x,y)->x+y);
			double scalingFactor = 1/totalWeight;
			
			for(String s:tid.getKeywords())
				if(!sum.containsKey(s))
					sum.put(s, tid.getScoreOf(s)*scalingFactor);
				else sum.put(s, sum.get(s)+tid.getScoreOf(s)*scalingFactor);
		}
		
		TreeSet<String> sortedTopics = new TreeSet<>((x,y)->{
			if(sum.get(x).equals(sum.get(y))) return x.compareTo(y);
			else return -Double.compare(sum.get(x), sum.get(y));
		});
		sortedTopics.addAll(sum.keySet());
		
		return sum;
	}


	
}
