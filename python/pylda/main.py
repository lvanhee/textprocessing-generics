#the CVS file should contain a column called "ids" and no column called "text"

if __name__ == '__main__':

    from mmap import mmap

    import pandas as pd
    import os
    import re
    import gensim
    from gensim.utils import simple_preprocess
    import nltk
    nltk.download('stopwords')


    import plotting
    import text_preprocessing
    import lda_optimization
    import post_processing_lda
    import itertools


    #folder = "C:/Users/loisv/My Drive/personnel/hobbies/jdr gn/GN/Miskatonic University/textprocessing/lda/"
    folder = "C:/Users/loisv/My Drive/personnel/hobbies/textprocessing/lda/melania_papers/"
    folder_results = folder + 'results/'
    folder_cache = folder +'cache/'
    preprocessing_cache_file =  folder_cache + 'preprocessed_text_as_lists_of_lemmatized_words.csv'
    global_wordcloud_output_file = folder_results + 'global_wordcloud_preprocessed.png'
    path_to_optimization_result_csv = folder_cache+'optimization_results.csv'
    precomputed_global_optimal_lda_model_file = folder_cache+'precomputed_optimal.obj'
    current_optimal_result_from_search_file = 'current_optimal_result.html'
    pathway_to_intermediate_results = folder_cache+'intermediate_results/'
    csv_file = folder_cache+'input_to_cleaned_text.csv'
    topics_per_document_file = folder_results+'topics_per_document.csv'
    topic_per_word_file = folder_results+'topics_per_word.csv'
    words_per_topic = folder_results+'words_per_topics.csv'

    #preprocessing_cache_file = 'C:/Users/loisv/Desktop/lda_tryout/preprocessed_text_as_lists_of_lemmatized_words.csv'
    #global_wordcloud_output_file = 'C:/Users/loisv/Desktop/lda_tryout/global_wordcloud_preprocessed.png'
    #path_to_optimization_result_csv = 'C:/Users/loisv/Desktop/lda_tryout/optimization_results.csv'
    #path_to_precomputed_optimal_model = 'C:/Users/loisv/Desktop/lda_tryout/precomputed_optimal.obj'
    #current_optimal_result_from_search_file = 'C:/Users/loisv/Desktop/lda_tryout/current_optimal_result.html'
    #pathway_to_intermediate_results = 'C:/Users/loisv/Desktop/lda_tryout/intermediates'
    #csv_file = 'C:/Users/loisv/Desktop/lda_tryout/dataset.csv'

    if not os.path.exists(folder_cache):
        os.makedirs(folder_cache)
    if not os.path.exists(folder_results):
        os.makedirs(folder_results)
    if not os.path.exists(pathway_to_intermediate_results):
        os.makedirs(pathway_to_intermediate_results)

    # Read data into papers
    id_text_dataframe = pd.read_csv(csv_file)

    # Print out the first rows of papers
    print("texts loaded:")
    print(len(id_text_dataframe))
    # Load the regular expression library
    # Remove punctuation
    id_text_dataframe['processed_text'] = \
        id_text_dataframe['text'].map(lambda x: re.sub('[,\.!?]', '', x))

    #################################WORDCLOUD_OF_ALL_THE_TEXT#########################################################
    #print("generating word cloud")
    #long_string = ','.join(list(papers['processed_text'].values))
    #plotting.raw_text_to_wordcloud(long_string, "results/raw_text_wordcloud.png")

    #################################PREPROCESS TEXT IN LISTS OF WORDS##################################################

    print("preprocessing texts")
    texts_as_preprocessed_word_lists = text_preprocessing\
        .text_list_to_preprocessed_word_list(id_text_dataframe.processed_text.values.tolist(),
                                             preprocessing_cache_file)

    all_postprocessed_words = set(list(itertools.chain.from_iterable(texts_as_preprocessed_word_lists)))

    print("Number of different words after post-processing: "+str(len(all_postprocessed_words)))

    print("raw text transformed into processed word lists (tokenization, removed stop words)")
    #print(texts_as_word_lists[0:5])

    corpus = lda_optimization.term_frequency_from_text(texts_as_preprocessed_word_lists)
    print("processed word lists per entry -> term frequencies per entry")
    #print(corpus[0:5])

    if(not os.path.exists(global_wordcloud_output_file)):
        long_string = [item for sublist in texts_as_preprocessed_word_lists for item in sublist]
        long_string2 = ','.join(long_string)
        plotting.raw_text_to_wordcloud(long_string2, global_wordcloud_output_file)
    print("processed word list -> wordcloud file")

    # print(texts_as_word_lists[:1][0][:30])

    #some basic optimization trials, not relevant
    #lda_model = lda_optimization.compute_basic_lda(texts_as_word_lists, 2)
    #plotting.export_lda_results_as_html(lda_model, corpus,"results/preprocessed_2.html")
    #plotting.export_lda_results_as_html(lda_optimization.compute_basic_lda(texts_as_word_lists, 3), corpus,"results/preprocessed_3.html")



    for i in range(5,101,5):
        print("Now computing the optimal distribution for "+str(i)+" topics")
        range_topic = range(i, i+1, 1)

        folder_results_for_i_topics = folder_results + str(i) + ' topics/'
        if not os.path.exists(folder_results_for_i_topics):
            os.makedirs(folder_results_for_i_topics)

        path_to_optimal_model = folder_results_for_i_topics+"precomputed_optimal.obj"

        lda_model = lda_optimization.compute_optimal_hyperparameters(texts_as_preprocessed_word_lists,
                                                                     path_to_optimal_model,
                                                                     path_to_optimization_result_csv,
                                                                     current_optimal_result_from_search_file,
                                                                     pathway_to_intermediate_results, range_topic)

        print(lda_model.print_topics())

        if not os.path.exists(folder_results_for_i_topics+"overview.html"):
            plotting.export_lda_results_as_html(lda_model, [lda_model.id2word.doc2bow(text) for text in texts_as_preprocessed_word_lists],
                                            folder_results_for_i_topics+"overview.html")


        plotting.export_topic_distribution_per_document(lda_model, corpus, texts_as_preprocessed_word_lists, folder_results_for_i_topics+"topics_per_document.csv")

        plotting.export_topic_per_word_distribution(lda_model,all_postprocessed_words,folder_results_for_i_topics+"topic_per_word.csv")

        plotting.export_word_distribution_per_topic(lda_model,folder_results_for_i_topics+"words_per_topic.csv")


        plotting.cluster_test(corpus=corpus, model=lda_model)

        #should print the documents similar to the first document in queue; to be automated to all documents
        plotting.retrieval_test(texts_as_preprocessed_word_lists[0], lda_model, dictionary=lda_model.id2word, corpus=corpus)

        doc_lda = lda_model[lda_optimization.term_frequency_from_text(texts_as_preprocessed_word_lists)]

        #plotting.format_topics_sentences(ldamodel=lda_model, corpus=corpus, texts=texts_as_preprocessed_word_lists)

        #plotting.sentences_chart(lda_model=lda_model, corpus=corpus, texts=texts_as_preprocessed_word_lists)

        # Get the topic distribution for the given document.
        #  text_attempt = ['model', 'model', 'function', 'set']#texts_as_word_lists[:1][0]
        #bow = lda_model.id2word.doc2bow(texts_as_preprocessed_word_lists[0])
    #    doc_topics, word_topics, phi_values = lda_model.get_document_topics(bow, per_word_topics=True)


    lda_model = lda_optimization.compute_optimal_hyperparameters(texts_as_preprocessed_word_lists,
                                                                 path_to_optimal_model,
                                                                 path_to_optimization_result_csv,
                                                                 current_optimal_result_from_search_file,
                                                                 pathway_to_intermediate_results, range(5,101,5))

    if not os.path.exists(current_optimal_result_from_search_file):
        plotting.export_lda_results_as_html(lda_model, [lda_model.id2word.doc2bow(text) for text in
                                                        texts_as_preprocessed_word_lists],
                                            current_optimal_result_from_search_file)

    plotting.export_topic_distribution_per_document(lda_model, corpus, texts_as_preprocessed_word_lists,
                                                    topics_per_document_file)

    plotting.export_topic_per_word_distribution(lda_model, all_postprocessed_words, topic_per_word_file)

    plotting.export_word_distribution_per_topic(lda_model, words_per_topic)

    ##################################TOPICS PER DOCUMENT
    from matplotlib import pyplot as plt




    dominant_topics, topic_percentages = post_processing_lda.\
        dominant_topic_and_topic_distribution_per_document_in_corpus(lda_model=lda_model,
                                                                     texts_as_word_lists=texts_as_preprocessed_word_lists,
                                                                     corpus=corpus)

    # Distribution of Dominant Topics in Each Document
    df = pd.DataFrame(dominant_topics, columns=['Document_Id', 'Dominant_Topic'])
    dominant_topic_in_each_doc = df.groupby('Dominant_Topic').size()
    df_dominant_topic_in_each_doc = dominant_topic_in_each_doc.to_frame(name='count').reset_index()

    # Total Topic Distribution by actual weight
    topic_weightage_by_doc = pd.DataFrame([dict(t) for t in topic_percentages])
    df_topic_weightage_by_doc = topic_weightage_by_doc.sum().to_frame(name='count').reset_index()

    # Top 3 Keywords for each Topic
    topic_top3words = [(i, topic) for i, topics in lda_model.show_topics(formatted=False)
                       for j, (topic, wt) in enumerate(topics) if j < 3]

    df_top3words_stacked = pd.DataFrame(topic_top3words, columns=['topic_id', 'words'])
    df_top3words = df_top3words_stacked.groupby('topic_id').agg(', \n'.join)
    df_top3words.reset_index(level=0, inplace=True)

    from matplotlib.ticker import FuncFormatter

    # Plot
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(10, 4), dpi=120, sharey=True)

    # Topic Distribution by Dominant Topics
    ax1.bar(x='Dominant_Topic', height='count', data=df_dominant_topic_in_each_doc, width=.5, color='firebrick')
    ax1.set_xticks(range(df_dominant_topic_in_each_doc.Dominant_Topic.unique().__len__()))
    tick_formatter = FuncFormatter(
        lambda x, pos: 'Topic ' + str(x) + '\n' + df_top3words.loc[df_top3words.topic_id == x, 'words'].values[0])
    ax1.xaxis.set_major_formatter(tick_formatter)
    ax1.set_title('Number of Documents by Dominant Topic', fontdict=dict(size=10))
    ax1.set_ylabel('Number of Documents')
    ax1.set_ylim(0, 1000)

    # Topic Distribution by Topic Weights
    ax2.bar(x='index', height='count', data=df_topic_weightage_by_doc, width=.5, color='steelblue')
    ax2.set_xticks(range(df_topic_weightage_by_doc.index.unique().__len__()))
    ax2.xaxis.set_major_formatter(tick_formatter)
    ax2.set_title('Number of Documents by Topic Weightage', fontdict=dict(size=10))

    plt.show()

    #  ident = lda_model.id2word.doc2bow(['cat'])
    #  term_id = ident[0][0]
    #  topics_for_term = lda_model.get_term_topics(term_id,0.000001)
    #  print(topics_for_term)

    # Get the most relevant topics to the given word.
    # get_term_topics(word_id, minimum_probability=None)

    # plotting.export_lda_results_as_html(lda_model, [lda_model.id2word.doc2bow(text) for text in texts_as_word_lists],
    #                                    "results/pre-optimization.html")


    ##################### t-SNE clustering chart
    # Get topic weights and dominant topics ------------
    from sklearn.manifold import TSNE
    from bokeh.plotting import figure, output_file, show
    from bokeh.models import Label
    from bokeh.io import output_notebook
    import re, numpy as np, pandas as pd
    import matplotlib.colors as mcolors

    # Get topic weights
    topic_weights = []
    for i, row_list in enumerate(lda_model[corpus]):
        topic_weights.append([w for i, w in row_list])

    # Array of topic weights
    arr = pd.DataFrame(topic_weights).fillna(0).values

    # Keep the well separated points (optional)
    arr = arr[np.amax(arr, axis=1) > 0.35]

    # Dominant topic number in each doc
    topic_num = np.argmax(arr, axis=1)

    # tSNE Dimension Reduction
    tsne_model = TSNE(n_components=2, verbose=1, random_state=0, angle=.99, init='pca')
    tsne_lda = tsne_model.fit_transform(arr)

    # Plot the Topic Clusters using Bokeh
    output_notebook()
    n_topics = 4
    mycolors = np.array([color for name, color in mcolors.TABLEAU_COLORS.items()])
    plot = figure(title="t-SNE Clustering of {} LDA Topics".format(n_topics),
                  plot_width=900, plot_height=700)
    plot.scatter(x=tsne_lda[:,0], y=tsne_lda[:,1], color=mycolors[topic_num])
    show(plot)