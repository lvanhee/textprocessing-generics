package textprocessing.topicmodelling.lda.examples;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cachingutils.impl.TextFileBasedCache;
import textprocessing.extraction.pdf.PdfReader;
import textprocessing.topicmodelling.lda.LdaAnalyzerUtils;
import textprocessing.topicmodelling.lda.model.LdaAnalysisReport;
import textprocessing.topicmodelling.lda.model.LdaMetaAnalysis;
import textprocessing.topicmodelling.lda.model.LdaParameters;

public class PdfFolderToLdaPlayground {
	

	public static void main(String[] args) {
		File inputFiles = Paths.get("C:/Users/loisv/Desktop/data").toFile();
		File analysisFolder = Paths.get("C:/Users/loisv/Desktop/lda_tryout/").toFile();
		

		Supplier<Map<String,String>> input = ()->{
			return
					Arrays.asList(inputFiles.listFiles()).parallelStream().collect(
							Collectors.toMap(
									(File x)->x.getName(),
									(File x)->PdfReader.getStringContentsOutOfFile(x, x.getName(), false)));
		};
		
		LdaAnalyzerUtils.performLdaAnalysis(input, analysisFolder);
		
		
	}

}
