module textprocessing.generics {
	exports textprocessing;
	exports textprocessing.languagemodels;
	exports textprocessing.topicmodelling.tfidf;
	exports textprocessing.topicmodelling.lda;
	exports textprocessing.sentimentanalysis;
	exports textprocessing.extraction.pdf;
	requires com.kennycason.kumo.core;
	requires stanford.corenlp;
	requires cachingutils;
	requires java.desktop;
	requires pdfbox.app;
}