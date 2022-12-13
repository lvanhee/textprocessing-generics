package textprocessing;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TextprocessingStatistics {

	public static Map<String, Double> getTfIdfScores(String corpusSet, Set<String> globalCorpusSet) {
		List<BagOfWords> globalCorpusBag = globalCorpusSet.parallelStream().map(x->BagOfWords.newInstance(x, y->{return true;}, true)).collect(Collectors.toList());
		return getTfIdfScores(BagOfWords.newInstance(corpusSet, x->true, true), globalCorpusBag);
	}

	public static Map<String, Double> getTfIdfScores(BagOfWords corpusBag, List<BagOfWords> globalCorpusBag) {
		Set<String> allWords = corpusBag.getAllWords(corpusBag);
		Map<String, Double> allFrequencies = corpusBag.getFrequencyMap();
		
		Map<String, Double> res = allWords.parallelStream().collect(Collectors.toMap(Function.identity(), x->allFrequencies.get(x)*idf(x,globalCorpusBag)));
		return res;
	}

	private static Double idf(String t, List<BagOfWords> globalCorpusBag) {
		long totalOccurrences = globalCorpusBag.stream().filter(x->x.contains(t)).count();
		double ratio = (double) globalCorpusBag.size()/(double)totalOccurrences;
		return Math.log(ratio);
	}

	public static Set<String> getMostPrevalentScoresAlongTfIds(String string, Set<String> collect) {
		Map<String, Double> idfScores = getTfIdfScores(string, collect);
		Set<String> sortedScores = new TreeSet<String>((x,y)->{
			if(idfScores.get(x)==idfScores.get(y))
				return x.compareTo(y);
			else return idfScores.get(x).compareTo(idfScores.get(y));
			});
		sortedScores.addAll(idfScores.keySet());
		return sortedScores;
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
}
