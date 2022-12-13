package textprocessing;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BagOfWords {

	private final Map<String, Integer> occurrences;

	public BagOfWords(Map<String, Integer> nGrams) {
		this.occurrences = nGrams;
	}

	public static BagOfWords newInstance(String reduce, Predicate<String> filter, boolean mergePlurals) {
		Map<List<String>,Integer> m = TextProcessingUtils.getNGrams(reduce, 1);

		Map<String, Integer> occurrences = 
				m
				.keySet()
				.stream()
				.filter(x->filter.test(x.get(0)))
				.collect(Collectors.toMap(x->x.get(0), x->m.get(x)));
		
		Set<String> words = occurrences.keySet().parallelStream().collect(Collectors.toSet());
		if(mergePlurals)
			for(String s:words)
			{
				if(!occurrences.containsKey(s))continue;
				Set<String> pluralForm = TextProcessingUtils.getDerivedFormsOf(s);
				for(String derivedForm:pluralForm)
					if(occurrences.containsKey(derivedForm))
					{
						int nbOccurrencesOfS= occurrences.get(s);
						int nbOccurrencesOfPlural =
								occurrences.get(derivedForm);
						occurrences.put(s,nbOccurrencesOfS+nbOccurrencesOfPlural);

						occurrences.remove(derivedForm);
					}
				
			}
			

		return new BagOfWords(occurrences);
	}

	public int getNbOccurrences(String s)
	{
		if(!occurrences.containsKey(s))return 0;
		return occurrences.get(s);
	}

	public List<String> getWordsSortedByDecreasingNumberOfOccurrences() {
		SortedSet<String> res = new TreeSet<>((x,y)->
		{
			if(getNbOccurrences(x)>getNbOccurrences(y))return -1;
			if(getNbOccurrences(x)<getNbOccurrences(y))return 1;
			return x.compareTo(y);
		});

		res.addAll(occurrences.keySet());
		return res.stream().collect(Collectors.toList());
	}

	public int hashCode()
	{
		return occurrences.hashCode();
	}

	public boolean equals(Object o)
	{
		return (((BagOfWords)o).occurrences.equals(occurrences));
	}
	
	public String toString() {
		String res = getWordsSortedByDecreasingNumberOfOccurrences().stream().map(x->x+":"+getNbOccurrences(x)).reduce("", (x,y)->x+"\n"+y);
		return res;
	}
	
	public static void main(String[] args) {
		String s = "a b c a";
		System.out.println(BagOfWords.newInstance(s, x->true, false));
	}

	public boolean contains(String string) {
		return getNbOccurrences(string)>0;
	}

	public Map<String, Double> getFrequencyMap() {
		int total = occurrences.values().stream().reduce(0, (x,y)->x+y);
		
		Map<String, Double> res = occurrences.keySet().stream().collect(Collectors.toMap(Function.identity(), 
				x->(double)occurrences.get(x)/(double)total));
		return res;
	}

	public Set<String> getAllWords(BagOfWords corpusBag) {
		return occurrences.keySet();
	}

	public static BagOfWords newInstance(List<String> listOfString) {
		Map<String, Integer> occurrences = new HashMap<>();
		for(String s:listOfString)
			if(!occurrences.containsKey(s))
				occurrences.put(s, 1);
			else occurrences.put(s, occurrences.get(s)+1);
		
		return new BagOfWords(occurrences);
	}

	public static BagOfWords filter(BagOfWords bow, Predicate<String> filter) {
		Map<String, Integer> occurrences = 
				bow.occurrences.keySet().stream().filter(x->filter.test(x))
				.collect(Collectors.toMap(Function.identity(), x->bow.getNbOccurrences(x)));
		return new BagOfWords(occurrences);
	}

	public static BagOfWords mergeAll(Collection<BagOfWords> bows) {
		Map<String, Integer> count = new HashMap<>();
		for(BagOfWords b:bows)
			for(String s: b.occurrences.keySet())
				if(count.containsKey(s))
					count.put(s, count.get(s)+b.occurrences.get(s));
				else count.put(s, b.occurrences.get(s));
		return new BagOfWords(count);
	}

}
