package textprocessing.topicmodelling.lda.model;

import java.awt.Color;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.CircleBackground;
import com.kennycason.kumo.bg.RectangleBackground;
import com.kennycason.kumo.font.scale.SqrtFontScalar;
import com.kennycason.kumo.image.AngleGenerator;
import com.kennycason.kumo.nlp.FrequencyAnalyzer;
import com.kennycason.kumo.palette.ColorPalette;

import cachingutils.impl.TextFileBasedCache;
import textprocessing.BagOfWords;
import textprocessing.GenericTextprocessingPipelines;
import textprocessing.TextProcessingUtils;

public class LdaMetaAnalysis {

	private final File analysisFolder;
	private final boolean SHOULD_INPUT_TEXT_BE_LEMMATIZED = true;

	private Set<String> overrepresentedTerms;
	private Set<String> esothericTerms;

	public static final Double MIN_FREQ_PER_TEXT_FOR_OVERREPRESENTATION=0.001;
	public static final Double MIN_FREQ_NB_OF_TEXTS_FOR_OVERREPRESENTATION = 0.5;

	private final Supplier<Map<String,String>> inputProvider;

	private LdaMetaAnalysis(File analysisFolder, Supplier<Map<String,String>> inputProvider) {
		this.inputProvider = inputProvider;
		this.analysisFolder = analysisFolder;
		if(!getAnalysisCacheFolder().exists())
			getAnalysisCacheFolder().mkdirs();
		if(!Arrays.asList(getAnalysisCacheFolder().listFiles()).contains(getFileMappingIdToContents()))
			preprocessInputFile();


	}


	private void preprocessInputFile() {
		Map<String, String> labelToText = inputProvider.get();
		Map<String, List<String>> idToListOfRelevantWords = getIdToListOfRelevantWords();

		Map<String, List<String>> idToListOfRelevantWordsF = idToListOfRelevantWords;


		Set<String> toRemove = new HashSet<>();

		overrepresentedTerms = getOverrepresentedTerms();

		toRemove.addAll(overrepresentedTerms);

		Set<String> esothericTerms =
				BagOfWords.getEsothericTermsAcrossACorpus(idToListOfRelevantWords.values());
		this.esothericTerms = esothericTerms;
		toRemove.addAll(esothericTerms);



		Map<String, List<String>> allWordsAfterAllFiltering = 
				idToListOfRelevantWordsF.keySet().stream().collect(Collectors.toMap(Function.identity(),
						x->idToListOfRelevantWordsF.get(x).stream().filter(y->!toRemove.contains(y)).collect(Collectors.toList())
						));


		Map<String, String> clearedString = 
				labelToText.keySet().stream().collect(Collectors.toMap(
						x->{
							String res = 
									//TextProcessingUtils.toListOfWords(x).toString().replaceAll(",", " ").replaceAll("  ", " ");
									x.replaceAll(",", " ").replaceAll("  ", " ");
							//System.out.println(res);
							return res;
						},
						x-> allWordsAfterAllFiltering.get(x)
						.toString().replaceAll(",", " ")
						.replaceAll("  ", " ")));

		String toFile = "id,text"+
				clearedString.keySet().stream().map(x->x.substring(1,x.length()-1)+","+
						clearedString.get(x).substring(1,clearedString.get(x).length()-1)
						)
				.reduce("", (x,y)->x+"\n"+y);


		try {
			Files.writeString(getFileMappingIdToContents().toPath(), toFile);
		} catch (IOException e) {
			e.printStackTrace();
			throw new Error();
		}

	}

	private Map<String, List<String>> getIdToListOfRelevantWords() {
		Map<String, String> labelToText = inputProvider.get();
		if(!SHOULD_INPUT_TEXT_BE_LEMMATIZED)
			return 
			labelToText.keySet().stream().collect(Collectors.toMap(
					Function.identity(),
					x->TextProcessingUtils.toListOfWords(labelToText.get(x)).stream()
					.collect(Collectors.toList())));
		else
			return
			labelToText.keySet().stream().collect(Collectors.toMap(Function.identity(), 
					x->GenericTextprocessingPipelines.getLemmatizedListOfRelevantWords(labelToText.get(x))));
	}


	private File getFileMappingIdToContents() {
		return Paths.get(analysisFolder.getAbsolutePath()+"/cache/preprocessed_texts.txt").toFile();
	}

	public static LdaMetaAnalysis newInstance(File analysisFolder, Supplier<Map<String, String>> ldaInput) {
		return new LdaMetaAnalysis(analysisFolder, ldaInput);
	}

	public LdaAnalysisReport getReportFor(LdaParameters ldap) {
		File htmlFile = getHtmlOutputFileForReport(ldap);
		htmlFile.getParentFile().mkdirs();

		String total = getStringOutputFromPyLdaLibrary(ldap);
		return getReportFromOutputString(total, htmlFile, getWordPerTopicCsvFile(ldap), getWordCloudFolder(ldap),
				getTopicPerWordFile(ldap),
				getTopicPerDocumentFile(ldap)
				);


	}


	private String getStringOutputFromPyLdaLibrary(LdaParameters ldap) {
		String total = "";
		try {
			String[] commands = {"python", "C:\\Users\\loisv\\Desktop\\git\\textprocessing-generics\\python\\pylda\\apply_lda.py",
					getFileMappingIdToContents().getAbsolutePath().replaceAll("\\\\", "/"),
					getFileIdToBagOfWords().getAbsolutePath().replaceAll("\\\\", "/"),
					ldap.getNbOfTopics()+"",
					ldap.getAlpha(), ldap.getBeta(), ldap.getPasses()+"",
					getHtmlOutputFileForReport(ldap).getAbsolutePath()}; 
			/*Runtime rt = Runtime.getRuntime();

		Process proc = rt.exec(commands);*/

			ProcessBuilder pb = new ProcessBuilder(commands);
			Process proc = pb.start();

			pb.redirectErrorStream(true);


			BufferedReader stdInput = new BufferedReader(new 
					InputStreamReader(proc.getInputStream()));

			/*BufferedReader stdError = new BufferedReader(new 
				InputStreamReader(proc.getErrorStream()));*/



			// Read the output from the command
			//System.out.println("Here is the standard output of the command:\n");
			String s = null;
			StringBuilder builder = new StringBuilder();
			//	System.out.println("Starting the process");
			while ((s = stdInput.readLine()) != null) {
				//	if(builder.toString().isEmpty())
				//			System.out.println("Now receiving input");
				//System.out.println(s);
				builder.append(s+"\n");
			}

			//	System.out.println("Input reception done");

			total+=builder.toString();

			/*// Read any errors from the attempted command
		//System.out.println("Here is the standard error of the command (if any):\n");
		while ((s = stdError.readLine()) != null) {
			System.out.println(s);
		}*/

			proc.waitFor();
			proc.destroy();
			proc.destroyForcibly();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			throw new Error();
		}
		return total;
	}


	private LdaAnalysisReport getReportFromOutputString(String total, File htmlFiles, File wordPerTopicCsvFile, File wordcloudFolder, File topicPerWordFile, File topicPerFocumentFile) {
		String qualityMetrics = total.substring(total.indexOf("<QUALITY_METRICS>"), total.indexOf("</QUALITY_METRICS>"));

		String cvString = qualityMetrics.substring(qualityMetrics.indexOf("\ncv:")+4);
		cvString = cvString.substring(0,cvString.indexOf("\n"));

		double cv = Double.parseDouble(cvString.substring(cvString.indexOf(":")+1));

		String umassString = Arrays.asList(total.split("\n")).stream().filter(x->x.startsWith("umass:")).collect(Collectors.toSet()).iterator().next();
		double umass = Double.parseDouble(umassString.substring(umassString.indexOf(":")+1));

		String perplexityString = Arrays.asList(total.split("\n")).stream().filter(x->x.startsWith("perplexity:")).collect(Collectors.toSet()).iterator().next();
		double perplexity = Double.parseDouble(perplexityString.substring(perplexityString.indexOf(":")+1));

		String consideredSubstring = total.substring(total.indexOf("START MATRIX WORDS PER TOPICS"));
		consideredSubstring = consideredSubstring.substring(consideredSubstring.indexOf("\n")+1);
		consideredSubstring = consideredSubstring.substring(0, consideredSubstring.indexOf("END MATRIX WORDS PER TOPICS")-1);

		String matrixTopicsPerDocument = total.substring(total.indexOf("<MATRIX TOPICS PER DOCUMENT>"), total.indexOf("</MATRIX TOPICS PER DOCUMENT>"));
		matrixTopicsPerDocument = matrixTopicsPerDocument.substring(matrixTopicsPerDocument.indexOf("\n")+1);


		String matrixTopicsPerWord = total.substring(total.indexOf("<MATRIX TOPICS PER WORD>"), total.indexOf("</MATRIX TOPICS PER WORD>"));
		matrixTopicsPerWord = matrixTopicsPerWord.substring(matrixTopicsPerWord.indexOf("\n")+1);

		try {
			Files.writeString(wordPerTopicCsvFile.toPath(), consideredSubstring.replaceAll(" ", ","));

			Files.writeString(topicPerWordFile.toPath(), matrixTopicsPerWord.replaceAll(" ", ","));

			Files.writeString(topicPerFocumentFile.toPath(), matrixTopicsPerDocument.replaceAll(" ", ","));			
		} catch (IOException e) {
			e.printStackTrace();
			throw new Error();
		}

		//exportWordclouds(prevalencePerTopic, wordcloudFolder);

		//TOPIC PER WORD FILE
		//TOPIC PER DOCUMENT FILE




		LdaAnalysisReport report = LdaAnalysisReport.newInstance(
				cv, umass, perplexity, htmlFiles, wordPerTopicCsvFile, wordcloudFolder,topicPerWordFile, topicPerFocumentFile
				);
		return report;
	}


	public void exportWordclouds(LdaParameters params, File wordcloudFolder) {
		String total = getStringOutputFromPyLdaLibrary(params);
		String consideredSubstring = total.substring(total.indexOf("START MATRIX WORDS PER TOPICS"));
		consideredSubstring = consideredSubstring.substring(consideredSubstring.indexOf("\n")+1);
		consideredSubstring = consideredSubstring.substring(0, consideredSubstring.indexOf("END MATRIX WORDS PER TOPICS")-1);
		Map<String,Map<Integer, Double>> prevalencePerTopic = Arrays.asList(consideredSubstring.split("\n")).stream().collect(Collectors.toMap(x->x.substring(0,x.indexOf(" ")),
				x->{
					String[] split = x.split(" ");
					Map<Integer, Double> res = new HashMap<>();
					for(int i = 1 ; i < split.length; i++)
						res.put(i-1, Double.parseDouble(split[i]));
					return res;
				}
				));

		exportWordclouds(prevalencePerTopic, wordcloudFolder);
	}

	private void exportWordclouds(Map<String, Map<Integer, Double>> prevalencePerTopic, File wordcloudFolder) {
		int nbTopics = prevalencePerTopic.values().iterator().next().keySet().size();


		Map<Integer, Float> huePerTopic = LdaPrettyfier.getHuePerTopic(nbTopics); 

		for(int i = 0 ; i < nbTopics; i++)
		{
			final int i2 = i;
			Map<String,Double> frequencyForTopic = 
					prevalencePerTopic.keySet().stream().collect(Collectors.toMap(Function.identity(), x->prevalencePerTopic.get(x).get(i2)));

			double maxFrequency = frequencyForTopic.values().stream().max(Double::compare).get();

			List<String> largestFrequencies = frequencyForTopic.keySet().stream().sorted((x,y)->-Double.compare(frequencyForTopic.get(x),frequencyForTopic.get(y))).collect(Collectors.toList());

			List<String> shortlistedLargestFrequencies = largestFrequencies.subList(0, 50);
			double frequencyScale = 100/maxFrequency;

			List<WordFrequency> scaledAndIntegeredFrequencies = shortlistedLargestFrequencies.stream()
					.map(x->new WordFrequency(x, (int)(frequencyForTopic.get(x)*frequencyScale)))
					.collect(Collectors.toList());

			scaledAndIntegeredFrequencies = scaledAndIntegeredFrequencies.stream().filter(x->x.getFrequency()>0).collect(Collectors.toList());


			final Dimension dimension = new Dimension(1000, 1000);
			final WordCloud wordCloud = new WordCloud(dimension, CollisionMode.PIXEL_PERFECT);

			wordCloud.setBackground(new RectangleBackground(new Dimension(1000,1000)));
			wordCloud.setPadding(3);

			float hue = huePerTopic.get(i);
			List<Color> allColors = new ArrayList<>();
			Random r = new Random(1);
			for(int j = 0 ; j < 20; j++)
			{
				float adaptedHue = (float)(hue + r.nextDouble()*.2f-.1f);

				float adaptedBrightness= (float)(.8 + r.nextDouble()*.2f-.1f);
				float adaptedSaturation= (float)(.8 + r.nextDouble()*.2f-.1f);

				allColors.add(new Color(Color.HSBtoRGB(adaptedHue, adaptedSaturation, adaptedBrightness)));
			}

			AngleGenerator ag = new AngleGenerator(0);
			wordCloud.setAngleGenerator(ag);
			wordCloud.setColorPalette(new ColorPalette(allColors));
			wordCloud.setFontScalar(new SqrtFontScalar(5, 150));
			wordCloud.build(scaledAndIntegeredFrequencies);
			if(!wordcloudFolder.exists())
				wordcloudFolder.mkdirs();
			wordCloud.writeToFile(wordcloudFolder.getAbsolutePath()+"/topic "+i+".png");
		}

	}


	private File getHtmlOutputFileForReport(LdaParameters ldap) {
		return Paths.get(getResultFolder(ldap).getAbsolutePath()
				+"/overview.html").toFile();
	}


	private File getFileIdToBagOfWords() {
		return Paths.get(getAnalysisCacheFolder().getAbsolutePath()+"/id_to_bag_of_words.txt").toFile();
	}


	private File getAnalysisCacheFolder() {
		return Paths.get(analysisFolder.getAbsolutePath()+"/cache/").toFile();
	}


	private File getWordPerTopicCsvFile(LdaParameters ldap) {
		return Paths.get(getAnalysisCacheFolder().getAbsolutePath()+"/"+ldap.toString().replaceAll(":","")+"/word_per_topic.csv").toFile();
	}

	private File getTopicPerWordFile(LdaParameters ldap) {
		return Paths.get(getAnalysisCacheFolder().getAbsolutePath()+"/"+ldap.toString().replaceAll(":","")+"/topic_per_word.csv").toFile();
	}

	private File getTopicPerDocumentFile(LdaParameters ldap) {
		return Paths.get(getAnalysisCacheFolder().getAbsolutePath()+"/"+ldap.toString().replaceAll(":","")+"/topic_per_document.csv").toFile();
	}


	private File getWordCloudFolder(LdaParameters ldap) {
		return getResultFolder(ldap);
	}

	private File getResultFolder(LdaParameters ldap) {
		return Paths.get(getAnalysisCacheFolder().getAbsolutePath()+"/"+ldap.toString().replaceAll(":","")+"/").toFile();
	}




	public String getReportString() {
		return "The texts were processed as follows:\n"+
				"1- Words that are neither verbs or nouns were removed.\n"+
				"2- Words were lemmatized\n"+
				"3- Stopwords were removed\n"+
				"4- Terms that were overrepresented were removed (i.e. when appearing at least "
				+MIN_FREQ_PER_TEXT_FOR_OVERREPRESENTATION*100+"% of the words in at least "+MIN_FREQ_NB_OF_TEXTS_FOR_OVERREPRESENTATION*100+"% of the texts:"+
				getOverrepresentedTerms()+"\n"+
				"5- Esotheric terms (i.e. when appearing in only one document) were removed:"+esothericTerms;
	}




	private Set<String> getOverrepresentedTerms() {
		if(overrepresentedTerms!=null) return overrepresentedTerms;

		Set<String> termsOverrepresentedAcrossTheCorpus = 
				BagOfWords.getTermsOccurringFrequentlyAcrossACorpusStringList(
						getIdToListOfRelevantWords().values(),MIN_FREQ_PER_TEXT_FOR_OVERREPRESENTATION,MIN_FREQ_NB_OF_TEXTS_FOR_OVERREPRESENTATION);
		
		return termsOverrepresentedAcrossTheCorpus;

	}


	public void exportHighlightedTexts(LdaParameters bestParameters, LdaAnalysisReport report, File highlightedTextFolder, Map<String,String> texts) {
		texts.keySet().parallelStream().forEach(s->
		{
			String htmlConvert = toColoredHtml(texts.get(s), bestParameters.getNbOfTopics(), report);
			if(!highlightedTextFolder.exists())
				highlightedTextFolder.mkdirs();
			File highlightedTextFile = Paths.get(highlightedTextFolder.getAbsolutePath()+"/"+
					TextProcessingUtils.conformToWindowsFileFormat(s)
			+".html").toFile();

			try {
				Files.writeString(highlightedTextFile.toPath(), "<html>"+htmlConvert+"</html>");
			} catch (IOException e) {
				e.printStackTrace();
				throw new Error();
			}
		});
	}


	private String toColoredHtml(String text, int nbTopics, LdaAnalysisReport report) {
		List<String> nonLemmatizedSymbols = GenericTextprocessingPipelines.getNonLematizedListOfWords(text);
		List<String> lemmatizedSymbols = GenericTextprocessingPipelines.getLemmatizedListOfWords(text, false, false);
		if(nonLemmatizedSymbols.size()!=lemmatizedSymbols.size())
			throw new Error();


		Map<String, Integer> topicPerWord = report.getMajoritaryTopicPerWord();

		Map<Integer, Color> colorPerTopic = LdaPrettyfier.getColorPerTopic(nbTopics);

		String res = "";
		for(int i = 0; i < nonLemmatizedSymbols.size(); i++)
			if(topicPerWord.containsKey(lemmatizedSymbols.get(i)))
			{
				Color c = colorPerTopic.get(topicPerWord.get(lemmatizedSymbols.get(i)));
				res+="<span style=\"background-color: "+String.format("#%06x", (c.getRGB() & 0x00ffffff))+"\">"+nonLemmatizedSymbols.get(i)+"</span> ";
			}
			else res+=nonLemmatizedSymbols.get(i)+" ";

		return res;
	}


	public Map<String, String> getTexts() {
		return inputProvider.get();
	}



}
