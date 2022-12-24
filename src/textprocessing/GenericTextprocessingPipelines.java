package textprocessing;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import textprocessing.languagemodels.EnglishStopwords;

public class GenericTextprocessingPipelines {
	
	private static Properties props = new Properties();
	static {props.setProperty("annotators", "tokenize,pos,lemma");}
	
	private static StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	
	public static List<String> getLemmatizedListOfRelevantWords(String s) {
		CoreDocument document = pipeline.processToCoreDocument(s);
		return document.tokens().stream()
				.filter(x->{
					String tag = x.tag();
					if(x.lemma().equals("("))
						System.out.println();
				return !x.tag().equals("CD")&&!x.tag().equals("DT")
						&&!x.tag().equals("IN")
						&&!x.tag().equals("HYPH")
						&&!x.tag().equals("TO")
						&&!x.tag().equals("POS")
						&&!x.tag().equals("-RRB-")
						&&!x.tag().equals("-LRB-")
						&&!x.tag().equals("PRP")
						&&!x.tag().equals("NNP")//REMOVES CHARACTER NAMES!!!
						&&!x.tag().equals(",")
						&&!x.tag().equals("``")
						&&!x.tag().equals("''")
						&&!x.tag().equals(".");})
				.map(y->y.lemma())
				.filter(x->!EnglishStopwords.isStopWord(x))
				.collect(Collectors.toList());
	}
	
	public static BagOfWords getLemmatizedBowOfRelevantWordsWithoutNames(String s)
	{
		return BagOfWords.newInstance(getLemmatizedListOfRelevantWords(s));
	}
}
