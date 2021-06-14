package textprocessing;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
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

}
