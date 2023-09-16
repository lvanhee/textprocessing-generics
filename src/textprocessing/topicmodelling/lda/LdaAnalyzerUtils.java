package textprocessing.topicmodelling.lda;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Provider;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cachingutils.impl.TextFileBasedCache;
import textprocessing.topicmodelling.lda.model.LdaAnalysisReport;
import textprocessing.topicmodelling.lda.model.LdaMetaAnalysis;
import textprocessing.topicmodelling.lda.model.LdaParameters;

public class LdaAnalyzerUtils {

	public static<T> void performLdaAnalysis(Supplier<Map<String, String>> texts, File ldaExportFolder) {
		File cacheFolder = Paths.get(ldaExportFolder.getAbsolutePath()+"/cache/").toFile();
		File optimizationResultFile = Paths.get(cacheFolder+"/optimization_results.txt").toFile();
		
		
		TextFileBasedCache<LdaParameters, LdaAnalysisReport> ldaParametersToReportCache = TextFileBasedCache
				.newInstance(optimizationResultFile,
						LdaParameters::toSerializableString, 
						LdaParameters::fromSerializableString,
						LdaAnalysisReport::toSerializableString, 
						LdaAnalysisReport::fromSerializableString, "->");

		
		LdaMetaAnalysis metaAnalysis = LdaMetaAnalysis.newInstance(ldaExportFolder, texts); 

		
		LdaParameters bestParameters = getBestParametersSlowGradientDescent(ldaParametersToReportCache, metaAnalysis);
		
		exportToFolder(metaAnalysis, bestParameters, ldaExportFolder, ldaParametersToReportCache);

		System.out.println(metaAnalysis.getReportString());
	}

	private static void exportToFolder(
			LdaMetaAnalysis metaAnalysis, 
			LdaParameters bestParameters,
			File ldaExportFolder,
			TextFileBasedCache<LdaParameters, LdaAnalysisReport> ldaParametersToReportCache
			) {
		File outputFolder = Paths.get(ldaExportFolder.getAbsoluteFile()+"/best_score_"+bestParameters.getNbOfTopics()+" "+
				String.format("%.3f",ldaParametersToReportCache.get(bestParameters).getPerplexity())+" "
		+String.format("%.3f",ldaParametersToReportCache.get(bestParameters).getCV())+" "
		+String.format("%.3f",ldaParametersToReportCache.get(bestParameters).getUmass())
		+"/").toFile();
		File targetFile = Paths.get(outputFolder.getAbsolutePath()+"/overview.html").toFile();
		File highlightedTextFolder = Paths.get(outputFolder.getAbsolutePath()+"/highlighted_texts/").toFile();
		
		/*if(ldaParametersToReportCache==null)
			ldaParametersToReportCache = metaAnalysis.getTexts();*/
		metaAnalysis.exportHighlightedTexts(bestParameters, ldaParametersToReportCache.get(bestParameters), highlightedTextFolder, metaAnalysis.getTexts());
		

		
		if(!outputFolder.exists())
			outputFolder.mkdirs();

		if(!targetFile.exists())
		{
			try {
				Files.copy(ldaParametersToReportCache.get(bestParameters).getHtmlOverviewFile().toPath(),targetFile.toPath());
			} catch (IOException e) {
				e.printStackTrace();
				throw new Error();
			}}
		
		metaAnalysis.exportWordclouds(bestParameters, outputFolder);
		
	}

	private static LdaParameters getBestParametersGridSearch(TextFileBasedCache<LdaParameters, LdaAnalysisReport> ldaParametersToReportCache, LdaMetaAnalysis metaAnalysis) {

		
		Set<LdaParameters> allConsideredParameters = new HashSet<>();

		for(int nbOfTopics:Arrays.asList(3, 5,6,7, 9,10,11,12,13, 15,17, 20, 21, 22, 
				//23,
				25, 27,29,30,31, 35, 40//,//, 45, 
			//	50//,
			//	70,
			//	100
				))
			for(String alpha: Arrays.asList("symmetric","0.1","0.5","1"))
				for(String beta: Arrays.asList("None","0.1","0.5","1"))
					for(int passes:Arrays.asList(10,20,40))
						allConsideredParameters.add(new LdaParameters(nbOfTopics, alpha, beta, passes));




		AtomicInteger remaining = new AtomicInteger(allConsideredParameters.size());
		//parallelStream can cause the system to break down silently

		Set<LdaParameters> consideredParameters = allConsideredParameters;
		
		AtomicInteger remainingForThisNumber = new AtomicInteger(consideredParameters.size());

		Stream<LdaParameters> str =consideredParameters.parallelStream(); 
		str.forEach(ldap->{
			if(!ldaParametersToReportCache.has(ldap))
				ldaParametersToReportCache.add(ldap, metaAnalysis.getReportFor(ldap));
			System.out.println(ldap+"\t\t"+ldaParametersToReportCache.get(ldap));
			System.out.println("Remaining:"+remainingForThisNumber.decrementAndGet()+" "+ remaining.decrementAndGet());
		});

		LdaParameters bestParameters =
				consideredParameters.stream().max((x,y)->
				compare(x, y,ldaParametersToReportCache)).get();

		System.out.println("BEST:"+bestParameters+" "+ldaParametersToReportCache.get(bestParameters));
		
		return bestParameters;
	}
	
	private static LdaParameters getBestParametersSlowGradientDescent(TextFileBasedCache<LdaParameters, LdaAnalysisReport> ldaParametersToReportCache, LdaMetaAnalysis metaAnalysis)
	{
		Set<Integer> consideredNbOfTopics = new HashSet<>();
		for(int i = 2 ; i <= 15 ; i++) {
			consideredNbOfTopics.add(i);
		}
		
		Set<String> consideredAlpha = new HashSet<>();
		
		consideredAlpha.add("symmetric");
		for(float f = 0.2f; f < 1f; f+=0.2f)
			consideredAlpha.add(f+"");
		
		Set<String> consideredBeta = new HashSet<>();
		consideredBeta.add("None");
		for(float f = 0.2f; f < 1f; f+=0.2f)
			consideredBeta.add(f+"");
		
		Set<Integer> passes = new HashSet<>();
		passes.add(10);
		passes.add(20);
		passes.add(40);
		
		LdaParameters best =
				new LdaParameters(consideredNbOfTopics.iterator().next(),
						consideredAlpha.iterator().next(),
						consideredBeta.iterator().next(),
						passes.iterator().next());
		
		if(!ldaParametersToReportCache.has(best))
			ldaParametersToReportCache.add(best, metaAnalysis.getReportFor(best));
		
		int iterationNumber = 1;
		while(true)
		{
			System.out.println("Iteration number:"+iterationNumber);
			

			
			System.out.println("Best so far:"+best+" "+ldaParametersToReportCache.get(best));
			iterationNumber++;
			
			Set<LdaParameters> allConsideredParameters = new HashSet<>();
			
			for(Integer i: consideredNbOfTopics)
				allConsideredParameters.add(new LdaParameters(i, best.getAlpha(), best.getBeta(), best.getPasses()));
			
			for(String alpha: consideredAlpha)
				allConsideredParameters.add(new LdaParameters(best.getNbOfTopics(), alpha, best.getBeta(), best.getPasses()));
			
			for(String beta: consideredBeta)
				allConsideredParameters.add(new LdaParameters(best.getNbOfTopics(), best.getAlpha(), beta, best.getPasses()));
			
			for(Integer pass: passes)
				allConsideredParameters.add(new LdaParameters(best.getNbOfTopics(), best.getAlpha(), best.getBeta(), pass));
			
			LdaParameters localBest = getOptimalParametersFromSet(allConsideredParameters,ldaParametersToReportCache, metaAnalysis);
			if(compare(localBest,best, ldaParametersToReportCache)>0)
				best = localBest;
			else break;
		}
		
		return best;
		
	}

	private static LdaParameters getOptimalParametersFromSet(Set<LdaParameters> allConsideredParameters, 
			TextFileBasedCache<LdaParameters, LdaAnalysisReport> ldaParametersToReportCache,
			LdaMetaAnalysis metaAnalysis) {
		AtomicInteger remaining = new AtomicInteger(allConsideredParameters.size());
		//parallelStream can cause the system to break down silently

		Set<LdaParameters> consideredParameters = allConsideredParameters;
		
		AtomicInteger remainingForThisNumber = new AtomicInteger(consideredParameters.size());

		Stream<LdaParameters> str =consideredParameters.stream(); 
		str.forEach(ldap->{
			if(!ldaParametersToReportCache.has(ldap))
				ldaParametersToReportCache.add(ldap, metaAnalysis.getReportFor(ldap));
		//	System.out.println(ldap+"\t\t"+ldaParametersToReportCache.get(ldap));
		//	System.out.println("Remaining:"+remainingForThisNumber.decrementAndGet()+" "+ remaining.decrementAndGet());
		});

		
		/*LdaParameters best = null;
		for(LdaParameters ldap: consideredParameters)
		{
			if(best==null)
			{
				best = ldap;
				continue;
			}
			else
			{
				if(compare(ldap, best, ldaParametersToReportCache)>0) {
					best = ldap;
				}
			}
		}*/
		LdaParameters bestParameters =
				consideredParameters.stream().max((x,y)->
				{
					return compare(x, y,ldaParametersToReportCache);
				}).get();
		
		return bestParameters;
	}

	private static Integer compare(LdaParameters x, LdaParameters y, TextFileBasedCache<LdaParameters, LdaAnalysisReport> ldaParametersToReportCache) {
		double vx = ldaParametersToReportCache.get(x).getPerplexity();
		double vy = ldaParametersToReportCache.get(y).getPerplexity();
		return -Double.compare(vx,vy);
	}
}
