package textprocessing.sentimentanalysis;

import java.util.Properties;


import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentAnnotatedTree;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

public class SentimentAnalysisUtils {
	private static final Properties properties = new Properties();
	static {properties.setProperty("annotators", "tokenize, ssplit, parse, sentiment");}
	private static final StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);

	public static void getSentimentResult(String text) {
		int sentimentInt;
		String sentimentName; 
		Annotation annotation = pipeline.process(text);
		for(CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class))
		{
			Tree tree = sentence.get(SentimentAnnotatedTree.class);
			sentimentInt = RNNCoreAnnotations.getPredictedClass(tree); 
			sentimentName = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
			System.out.println(sentimentName + "\t" + sentimentInt + "\t" + sentence);
		}
	}
	
	public static void main(String[] args)
	{
		getSentimentResult("hello, this product is very bad!");
	}

}
