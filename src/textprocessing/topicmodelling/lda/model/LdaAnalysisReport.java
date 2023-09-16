package textprocessing.topicmodelling.lda.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import textprocessing.BagOfWords;
import textprocessing.GenericTextprocessingPipelines;
import textprocessing.TextProcessingUtils;

public class LdaAnalysisReport {
	
	private final double cv;
	private final double umass;
	private final double perplexity;
	private final File htmlFile;
	private final File wordPerTopicCsvFile;
	private final File topicPerWordCsvFile;
	private final File topicPerDocumentCsvFile;
	private final File wordcloudCache;
	private Map<String, Map<Integer,Double>> wordPerTopicId;

	public LdaAnalysisReport(double cv, double umass, double perplexity, File htmlFile, File wordPerTopicCsvFile, File wordcloudCache, File topicPerWordFile, File topicPerFocumentFile) {
		this.cv = cv;
		this.umass = umass;
		this.perplexity = perplexity;
		this.htmlFile = htmlFile;
		this.wordPerTopicCsvFile = wordPerTopicCsvFile;
		this.wordcloudCache = wordcloudCache;
		this.topicPerWordCsvFile = topicPerWordFile;
		this.topicPerDocumentCsvFile = topicPerFocumentFile;
	}


	public static LdaAnalysisReport newInstance(double cv, double umass, double perplexity, File htmlFile, File wordPerTopicCsvFile, File wordcloudCache, File topicPerWordFile, File topicPerFocumentFile) {
		return new LdaAnalysisReport(cv, umass, perplexity,htmlFile,wordPerTopicCsvFile,wordcloudCache,topicPerWordFile, topicPerFocumentFile);
	}

	public String toString() {
		return "CV:"+cv+" UMASS:"+umass+" Perplexity:"+perplexity;
	}
	
	public static String toSerializableString(LdaAnalysisReport rep) {
		return rep.cv+"\t"+rep.umass+"\t"+rep.perplexity+"\t"+rep.htmlFile+"\t"+rep.wordPerTopicCsvFile+"\t"+rep.wordcloudCache+"\t"+rep.topicPerWordCsvFile+"\t"+rep.topicPerDocumentCsvFile;
	}
	
	public static LdaAnalysisReport fromSerializableString(String s) {
		String[] str = s.split("\t");
		return new LdaAnalysisReport(
				Double.parseDouble(str[0]),
				Double.parseDouble(str[1]), 
				Double.parseDouble(str[2]), 
				Paths.get(str[3]).toFile(),
				Paths.get(str[4]).toFile(),
				Paths.get(str[5]).toFile(),
				Paths.get(str[6]).toFile(),
				Paths.get(str[7]).toFile()
				);
	}


	public double getPerplexity() {
		return perplexity;
	}
	
	public int hashCode() 
	{
		return Double.hashCode(cv+umass+perplexity)*htmlFile.hashCode();
	}
	
	public boolean equals(Object o) {
		LdaAnalysisReport rep = (LdaAnalysisReport)o;
		return rep.cv==cv && rep.umass == umass && rep.perplexity == perplexity && rep.htmlFile.equals(htmlFile);
	}


	public File getHtmlOverviewFile() {
		return htmlFile;
	}


	public double getCV() {
		return cv;
	}


	public double getUmass() {
		return umass;
	}


	public Map<String, Map<Integer, Double>> getTopicIdPerWord() {
		if(wordPerTopicId==null)
			try {
				wordPerTopicId = Arrays.asList(Files.readString(topicPerWordCsvFile.toPath()).split("\n"))
				.stream().collect(Collectors.toMap(x->x.split(",")[0],
						x->{
							List<String> values = Arrays.asList(x.split(","));
							Map<Integer,Double> res = new HashMap<>();
							for(int i = 1 ; i < values.size() ; i ++)
								res.put(i-1, Double.parseDouble(values.get(i)));
							return res;
						}
						));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new Error();
			}
		return wordPerTopicId;
	}


	public Map<String, Integer> getMajoritaryTopicPerWord() {
		Map<String, Map<Integer,Double>> weightedTopicPerWord = getWeightedTopicPerWord();
		
		Map<String, Integer>  res = 
				weightedTopicPerWord.keySet().stream()
				.collect(Collectors.toMap(
						Function.identity(), 
						(String x)->
						{
							Comparator<Integer> comp = (Integer y,Integer z)->Double.compare(weightedTopicPerWord.get(x).get(y),weightedTopicPerWord.get(x).get(z));
							return weightedTopicPerWord.get(x).keySet()
							.stream()
									.max(comp).get();
						//return null;
						}
						)
						);
		return res;
	}


	private Map<String, Map<Integer, Double>> getWeightedTopicPerWord() {
		Map<String, Map<Integer, Double>> topicIdPerWord = getTopicIdPerWord();
		
		return topicIdPerWord.keySet().stream().collect(Collectors.toMap(Function.identity(), x->{
			double total = topicIdPerWord.get(x).values().stream().reduce(0d, (z,y)->z+y);
			Map<Integer, Double> res = topicIdPerWord.get(x).keySet().stream().collect(Collectors.toMap(Function.identity(), y->{ 
				return topicIdPerWord.get(x).get(y)/total;})); 
			return res;
		}
		));
		
	}
}
