package textprocessing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import textprocessing.languagemodels.EnglishStopwords;

public class GenericTextprocessingPipelines {
	
	private static Properties props = new Properties();
	static {props.setProperty("annotators", "tokenize,pos,lemma");}
	
	private static Map<String, String> canonicalForm = new HashMap<>();
	static {
		canonicalForm.put("modeling", "modelling");
	}
	
	private static StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	
	public static List<String> getLemmatizedListOfRelevantWords(String s) {
		CoreDocument document = pipeline.processToCoreDocument(s);		
		
		return document.tokens().stream()
				.filter(x->{
					String tag = x.tag();
					/*if(x.lemma().equals("("))
						System.out.println();*/
				return !x.tag().equals("CD")&&!x.tag().equals("DT")
						&&!x.tag().equals("IN")
						&&!x.tag().equals("HYPH")
						&&!x.tag().equals("TO")
						&&!x.tag().equals("POS")
						&&!x.tag().equals("-RRB-")
						&&!x.tag().equals("-LRB-")
						&&!x.tag().equals("PRP")
						&&!x.tag().equals("NNP")
						&&!x.tag().equals(",")
						&&!x.tag().equals("``")
						&&!x.tag().equals("''")
						&&!x.tag().equals(".");})
				.map(y->y.lemma())
				.filter(x->{
					return !EnglishStopwords.isStopWord(x);
					})
				.filter(x->!x.matches("\\p{Punct}*"))
				.filter(x->!x.equals("“"))
				.filter(x->!x.equals("?"))
				.map(x->{
					if(x.equals("."))
						System.out.println();
					return x;
				})
				.collect(Collectors.toList());
	}
	
	public static List<String> getLemmatizedListOfWords(String s, boolean deleteDashes, boolean canonicalOrthographFixer){
		if(deleteDashes)
			s = s.replaceAll("-", "");
		CoreDocument document = pipeline.processToCoreDocument(s);
		List<String> res = document.tokens().stream()
		.map(y->y.lemma())
		.collect(Collectors.toList());
		
		if(canonicalOrthographFixer)
			res = res.stream().map(x->{if(canonicalForm.containsKey(x)) return canonicalForm.get(x); else return x;}).collect(Collectors.toList());
		
		return res;
	}
	
	public static List<String> getNonLematizedListOfWords(String s) {
		CoreDocument document = pipeline.processToCoreDocument(s);
		List<String> res = document.tokens().stream()
		.map(y->y.originalText())
		.collect(Collectors.toList());
		
		return res;
	}
	
	public static BagOfWords getLemmatizedBowOfRelevantWordsWithoutNames(String s)
	{
		return BagOfWords.newInstance(getLemmatizedListOfRelevantWords(s));
	}


}
