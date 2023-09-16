package textprocessing;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TextProcessingUtils {

	public static String purgeAllPunctuation(String s)
	{
		s = s.replaceAll("\\.", " ").replaceAll("’", " ").replaceAll("\\?", " ").replaceAll("«", " ").replaceAll("»"," ").
				replaceAll("!"," ").
				replaceAll("…"," ");

		return s;
	}

	public static List<String> toListOfWords(String text) {
		text = clearOfSymbols(text);
		text = text.toLowerCase();
		while(text.startsWith(" "))text = text.substring(1);
		while(text.contains("  "))text = text.replaceAll("  ", " ");
		
		List<String> res =Arrays.asList(text.split(" ")); 
		return res;
	}

	public static String clearOfSymbols(String string) {

		string = string.replaceAll("[^a-zA-Z]", " ");
		/*char c = (char)160;
		string = string.replaceAll(c+"", "");
		string = string.replaceAll(",", " ");
		string = string.replaceAll("%", " ");
		string = string.replaceAll("/", " ");
		string = string.replaceAll("-", " ");
		string = string.replaceAll("^", " ");
		string = string.replaceAll("”", " ");
		string = string.replaceAll("\\.", " ");
		string = string.replaceAll("\"", " ");
		string = string.replaceAll(";", " ");
		string = string.replaceAll("\\(", " ");
		string = string.replaceAll("\\)", " ");
		string = string.replaceAll("\\]", " ");
		string = string.replaceAll("\\[", " ");
		string = string.replaceAll("!", " ");
		string = string.replaceAll("\\|", " ");
		string = string.replaceAll(":", " ");
		string = string.replaceAll("\\?", " ");
		string = string.toLowerCase();
		string = string.replaceAll("\n", " ");
		string = string.replaceAll("–", " ");
		string = string.replaceAll("[0-9]", "");
		string = string.replaceAll("“", " ");
		string = string.replaceAll("•", " ");
		string = string.replaceAll("∈", " ");
		string = string.replaceAll("=", " ");
		string = string.replaceAll("α", " ");
		string = string.replaceAll("∧", " ");
		string = string.replaceAll("φ", " ");
		string = string.replaceAll("&", " ");
		string = string.replaceAll("→", " ");
		string = string.replaceAll("}", " ");
		string = string.replaceAll("\\+", " ");
		string = string.replaceAll("<", " ");
		string = string.replaceAll("≤", " ");
		string = string.replaceAll("δ", " ");
		string = string.replaceAll("<", " ");
		string = string.replaceAll("<", " ");
		string = string.replaceAll("<", " ");
		string = string.replaceAll("<", " ");
		string = string.replaceAll("<", " ");*/

		while(string.contains("  "))
			string = string.replaceAll("  ", " ");

		return string;
	}

	public static Map<List<String>, Integer> getNGrams(String parsedText, int nb) {
		List<String> words = TextProcessingUtils.toListOfWords(parsedText);
		return getNGrams(words, nb);
	}

	public static Map<List<String>, Integer> getNGrams(List<String> words, int nb) {
		Map<List<String>, Integer> allNgrams = new HashMap<>();
		List<String> lastFew = new LinkedList<>();
		for(String s: words)
		{
			lastFew.add(s);
			if(lastFew.size()==nb)
			{
				List<String> copy = lastFew.stream().collect(Collectors.toList());
				if(!allNgrams.containsKey(copy))
					allNgrams.put(copy, 0);
				allNgrams.put(copy, allNgrams.get(copy)+1);
				lastFew.remove(0);				
			}
		}
		return allNgrams;
	}

	public static Set<String> getDerivedFormsOf(String s) {
		if(s.equals("argument"))return 
				Arrays.asList("arguments","argumentation","argumentations")
				.stream().collect(Collectors.toSet());
		if(s.equals("action"))return 
				Arrays.asList("act","actions","acts")
				.stream().collect(Collectors.toSet());
		return Arrays.asList(s+"s").stream().collect(Collectors.toSet());
	}

	public static int countOccurrences(String s, char c) {
		int count = 0;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == c) {
				count++;
			}
		}
		return count;
	}

	public static String toRegex(String s) {
		return java.util.regex.Pattern.quote(s);
	}

	public static int getNumberOfOccurrencesOf(String in, List<String> longTextToSearch) {
		List<String> listOfWord = TextProcessingUtils.toListOfWords(in);
		
		int total = 0;
		while(true)
		{
			int sublist = Collections.indexOfSubList(longTextToSearch, listOfWord);
			if(sublist==-1) break;
			longTextToSearch = longTextToSearch.subList(sublist+1, longTextToSearch.size());
			total++;
		}
		return total;
	}

	public static String delatexify(String string) {
		return string.replaceAll("\\\\`e", "è").replaceAll("\\\\'e", "é")
				.replaceAll("\\\\\"o", "ö");
	}

	public static String conformToWindowsFileFormat(String s) {
		return s.replaceAll(":", "").replaceAll("\\?", "").replaceAll("\\*", "");
	}

}
