if __name__ == '__main__':
    import sys
    import pandas as pd
    import re
    import text_preprocessing
    import lda_optimization
    import gensim.corpora as corpora
    import plotting
    import itertools


    if(len(sys.argv)>1):
        path_to_csv_file = sys.argv[1]
        path_to_bow_file = sys.argv[2]
        num_topics = int(sys.argv[3])
        alpha = sys.argv[4]
        beta = sys.argv[5]
        num_passes = int(sys.argv[6])
        html_output = sys.argv[7]
    else:
        path_to_csv_file = "C:/Users/loisv/Desktop/lda_tryout/cache/preprocessed_texts.txt"
        path_to_bow_file = "C:/Users/loisv/Desktop/lda_tryout/cache/bow.txt"
        num_topics = int("5")
        alpha = "symmetric"
        beta = "None"
        num_passes = int("10")
        html_output = "C:/Users/loisv/Desktop/lda_tryout/html.html"

    id_text_dataframe = pd.read_csv(path_to_csv_file)

    id_text_dataframe['processed_text'] = \
    id_text_dataframe['text'].map(lambda x: re.sub('[,\.!?]', '', x))

    texts_as_preprocessed_word_lists = text_preprocessing\
            .text_list_to_preprocessed_word_list(id_text_dataframe.processed_text.values.tolist(),
                                                 path_to_bow_file)

    corpus = lda_optimization.term_frequency_from_text(texts_as_preprocessed_word_lists)

    id2word = corpora.Dictionary(texts_as_preprocessed_word_lists)
    corpus = [id2word.doc2bow(text) for text in texts_as_preprocessed_word_lists]

    lda_model = lda_optimization.compute_lda(num_passes,corpus = corpus, dictionary = id2word, num_topics=num_topics, a=alpha, b=beta, multicore=False)

    (c_v, umass, perplexity) = lda_optimization.compute_coherence_values(lda_model, corpus, id2word, texts_as_preprocessed_word_lists)
    print("<QUALITY_METRICS>")
    print("cv:"+str(c_v))
    print("umass:" + str(umass))
    print("perplexity:" + str(perplexity))
    print("</QUALITY_METRICS>")
    plotting.export_lda_results_as_html(lda_model, corpus, html_output)

#    print(lda_model.show_topics(10))

#    print(lda_model.get_topic_terms(0, topn=10))
#    print(lda_model.get_topic_terms(0, topn=10)[0][0])

    print("START MATRIX WORDS PER TOPICS")

    for i in range(0, len(lda_model.get_topics()[0])):
        to_print = id2word.get(i)
        for j in range(0, len(lda_model.get_topics())):
            to_print = to_print + " " + str(lda_model.get_topics()[j][i])
        print(to_print)

    print("END MATRIX WORDS PER TOPICS")

    print("<MATRIX TOPICS PER DOCUMENT>")

    topics_per_document = plotting.topic_distribution_per_document_to_string(lda_model,
                                                    texts_as_preprocessed_word_lists)
    to_print = ""
    for i in range(0, len(topics_per_document)):
        to_print = "doc_"+str(topics_per_document[i][0])
        for j in range(1, len(topics_per_document[i])):
            to_print = to_print + " " + str(topics_per_document[i][j])
        print(to_print)

    print("</MATRIX TOPICS PER DOCUMENT>")


    print("<MATRIX TOPICS PER WORD>")
    all_postprocessed_words = set(list(itertools.chain.from_iterable(texts_as_preprocessed_word_lists)))
    topics_per_word = plotting.topic_per_word_distribution_to_str(lda_model, all_postprocessed_words)

    for i in range(0, len(topics_per_word)):
        to_print = topics_per_word[i][0]
        for j in range(1, len(topics_per_word[i])):
            to_print = to_print + " " + str(topics_per_word[i][j])

        print(to_print)
    print("</MATRIX TOPICS PER WORD>")

    exit()
