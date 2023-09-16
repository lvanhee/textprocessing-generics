package textprocessing.extraction.pdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import cachingutils.impl.SplittedFileBasedCache;


public class PdfReader {
	
	public static File TXT_OF_PDF_CACHE = Paths.get("data/cache/pdf_to_txt/").toFile();

	private static File getCacheFileFrom(File f)
	{
		String fileName = f.getName();
		File parent = f.getParentFile();
		File res = new File(parent+"_cached_string/"+fileName);
		return res;
	}
	private static File getFailedProcessingFile(File x)
	{
		return new File(x.getAbsolutePath()+"_failed");
	}

	private static SplittedFileBasedCache<String, String> cache= SplittedFileBasedCache.newInstance(x->TXT_OF_PDF_CACHE, Function.identity(), Function.identity());
	
	private static String getStringContentsOutOfFile(File x) {
		return getStringContentsOutOfFile(x,null,false);
	}
	
	public static String getStringContentsOutOfFile(File x, String uniqueID, boolean withCache) {
		if(withCache&& cache.has(uniqueID))return cache.get(uniqueID);
		
		PDFParser parser = null;
		PDDocument pdDoc = null;
		COSDocument cosDoc = null;
		PDFTextStripper pdfStripper;

		
		try {
			parser = new PDFParser(new RandomAccessBufferedFileInputStream(x));
		
		parser.parse();
		cosDoc = parser.getDocument();
		pdfStripper = new PDFTextStripper();
		pdDoc = new PDDocument(cosDoc);
		String parsedText = pdfStripper.getText(pdDoc);
		parsedText = parsedText.replaceAll(""+(char)13, " ");
		while(parsedText.contains("  ")) parsedText = parsedText.replaceAll("  ", " ");
		parsedText = parsedText.replaceAll("- \n", "");
		parsedText = parsedText.replaceAll("-\n", "");
		
		if(withCache)
		{
			cache.add(uniqueID, parsedText);
		}

		return parsedText;
		} catch (IOException e) {
			e.printStackTrace();
			throw new Error();
		}

	}
	
	private static void exportToFile(File cacheFile, String parsedText) {
		 try {
	            FileOutputStream fileOut = new FileOutputStream(cacheFile);
	            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
	            objectOut.writeObject(parsedText);
	            objectOut.close();
	            fileOut.close();
	            
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	}
	
	private static Object importFromFile(File cacheFile) {
		 try {
			 FileInputStream fi = new FileInputStream(cacheFile);
			 ObjectInputStream oi = new ObjectInputStream(fi);
			 Object res = oi.readObject();
			 oi.close();
			 fi.close();
			 return res;	            
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
		 throw new Error();
	}

	public static Map<Reference, String> getStringContentsPerReference(Set<Reference> refs)
	{
		return refs.stream().collect(Collectors.toMap(Function.identity(), 
				x-> PdfReader.getStringContentsOutOfFile(
		BibToPdfMain.getPdfFileFor(x),x.getUniqueId()+"", true)));			
	}

	public static boolean isParsingWorking(File f) {
		if(getCacheFileFrom(f).exists())return true;
		if(getFailedProcessingFile(f).exists())return false;
		PDFParser parser = null;		
		try {
			RandomAccessBufferedFileInputStream input =new RandomAccessBufferedFileInputStream(f); 
			parser = new PDFParser(input);
			parser.parse();
			parser.getDocument().close();
			input.close();
			
			return true;
			
		} catch (IOException e) {
			//e.printStackTrace();
			if(
					e.toString().equals("java.io.IOException: Page tree root must be a dictionary")
					||
			e.toString().equals("java.io.IOException: Missing root object specification in trailer."))
			{
				try {
					getFailedProcessingFile(f).createNewFile();
				} catch (IOException e1) {
					e1.printStackTrace();
					throw new Error();
				}
				return false;
			}
			throw new Error();
		}

	}

	public static Map<Reference, String> getStringContentsFromValidFilesMappedToReferenceFromFile(File fileName) {
		Set<Reference> allReferences = ReferenceDbOperations.referencesFromBibFile(fileName);
		
		allReferences = allReferences.parallelStream()
				.filter(x->BibToPdfMain.isPdfAccessible(x))
				.collect(Collectors.toSet());
		
		return getStringContentsPerReference(allReferences);
	}
	
	
	public static Map<File, String> getStringContentsOutOfFolder(String path, boolean recursively) {
		File f = new File(path);
		if(!f.isDirectory())throw new Error();
		
		Set<File> allFiles = null;
		if(recursively)
			try {
				allFiles = Files.find(Paths.get(path),
				           Integer.MAX_VALUE,
				           (filePath, fileAttr) -> fileAttr.isRegularFile()).map(x->x.toFile()).collect(Collectors.toSet());
			} catch (IOException e) {
				e.printStackTrace();
				throw new Error();
			}
		else allFiles = Arrays.asList(f.listFiles()).stream().collect(Collectors.toSet());
		
		Set<File> allPdfFiles = allFiles.parallelStream().filter(x->x.getName().endsWith(".pdf")).collect(Collectors.toSet());
		System.out.println("Excluded files due to not being PDFs:"+allFiles.stream().filter(x->!allPdfFiles.contains(x)).collect(Collectors.toSet()));
		return allPdfFiles.parallelStream().collect(Collectors.toMap(Function.identity(), x->getStringContentsOutOfFile(x)));
	}
}