import gensim.corpora as corpora
import gensim
import tqdm
import text_preprocessing
from gensim.models import CoherenceModel
import numpy as np
import pandas as pd
import os.path
from os import path
import plotting

#Mallet:
#https://github.com/maria-antoniak/little-mallet-wrapper/blob/master/demo.ipynb


mallet_path = "C:\\Users\\loisv\\Desktop\\Code\\class_code\\ai2\pylda\\resources\\mallet-2.0.8\\bin\\mallet"


def plot_coherence(passes, topics_range, model_results):
    # Show graph
    import matplotlib.pyplot as plt

    for p in passes:
        data_coherence = []
        data_coherence2 = []

        for x in topics_range:
            fitting_result = model_results[(model_results['Topics'] == x) & (model_results['Passes'] == p)][
                'coherence_umass']
            data_coherence.append(sum(fitting_result) / len(fitting_result))

            fitting_result_cv = model_results[((model_results['Topics'] == x) & (model_results['Passes'] == p))][
                'coherence_cv']
            data_coherence2.append(sum(fitting_result_cv) / len(fitting_result_cv))

        plt.plot(topics_range, data_coherence, label="umass " + str(p))

        plt.plot(topics_range, data_coherence2, label="cv " + str(p))

    plt.xlabel("Num Topics")
    plt.ylabel("Coherence score")
    plt.legend(("coherence_values"), loc='best')

    plt.savefig("results/coherence.png")
    plt.show()


def compute_coherence_values_and_lda(corpus, dictionary, k, a, b, passes,input_data):
   # lda_model2 = gensim.models.LdaMulticore(corpus=corpus,
   #                                        id2word=dictionary,
   #                                        num_topics=k,
   #                                        random_state=100,
                                          # chunksize=100,
                                          # passes=10,
   #                                        alpha=a,
   #                                        eta=b)
    lda_model = compute_lda(passes,corpus = corpus, dictionary = dictionary, num_topics=k, a=a, b=b)


    return (lda_model,compute_coherence_values(lda_model,corpus,dictionary,input_data))

def compute_coherence_values(lda_model, corpus, dictionary, input_data):
    #print(lda_model.print_topics())
    # print(lda_model2.print_topics())

    coherence_model_lda_c_v = CoherenceModel(model=lda_model, corpus=corpus, texts=input_data, dictionary=dictionary,
                                             coherence='c_v').get_coherence()
    coherence_model_lda_u_mass = CoherenceModel(model=lda_model, corpus=corpus, coherence='u_mass').get_coherence()

    perplexity = lda_model.log_perplexity(corpus)

    return (coherence_model_lda_c_v, coherence_model_lda_u_mass, perplexity)


def term_frequency_from_text(texts):
    id2word = corpora.Dictionary(texts)
    return [id2word.doc2bow(text) for text in texts]


def jaccard_similarity(topic_1, topic_2):
    """
    Derives the Jaccard similarity of two topics

    Jaccard similarity:
    - A statistic used for comparing the similarity and diversity of sample sets
    - J(A,B) = (A ∩ B)/(A ∪ B)
    - Goal is low Jaccard scores for coverage of the dirverse elements
    """
    intersection = set(topic_1).intersection(set(topic_2))
    union = set(topic_1).union(set(topic_2))

    return float(len(intersection)) / float(len(union))

def average_jaccard_similarity(lda):
    LDA_stability = {}
    for i in range(0, len(lda.num_topics) - 1):
        jaccard_sims = []
        for t1, topic1 in enumerate(lda.LDA_topics[lda.num_topics[i]]):  # pylint: disable=unused-variable
            sims = []
            for t2, topic2 in enumerate(LDA_topics[num_topics[i + 1]]):  # pylint: disable=unused-variable
                sims.append(jaccard_similarity(topic1, topic2))

            jaccard_sims.append(sims)

        LDA_stability[num_topics[i]] = jaccard_sims

    mean_stabilities = [np.array(LDA_stability[i]).mean() for i in num_topics[:-1]]

def compute_lda(passes, texts_as_word_lists=None, corpus = None, dictionary=None, num_topics=10, a=0.05, b=0.05,
                multicore = True):
    if(corpus ==None):
        corpus = term_frequency_from_text(texts_as_word_lists)
    # Create Dictionary
    if(dictionary==None):
        id2word = corpora.Dictionary(texts_as_word_lists)
    else:
        id2word = dictionary

    num_topics = round(num_topics)
    passes = round(passes)

    if(isinstance(a,str)):
        try:
            a = float(a)
        except ValueError:
            a = a

    # Term Document Frequency
    if(isinstance(b,str)):
        if(b == "None"):
            b = None
        else:
            b = float(b)
    elif(b == None):
        print("")
    elif(np.isnan(b)):
            b = None
    if(multicore):
        lda_model = gensim.models.LdaMulticore(corpus=corpus,
                                               id2word=id2word,
                                               num_topics=num_topics,
                                               random_state=100,
                                               # chunksize=100,
                                               passes=passes,
                                               alpha=a,
                                               eta=b)
    else:
        lda_model = gensim.models.ldamodel.LdaModel(corpus=corpus,
                                               id2word=id2word,
                                               num_topics=num_topics,
                                               random_state=100,
                                               # chunksize=100,
                                               passes=passes,
                                               alpha=a,
                                               eta=b)

    #lda_model = gensim.models.wrappers.LdaMallet(mallet_path, corpus=corpus, num_topics=k, id2word=dictionary)
    return lda_model


def compute_optimal_hyperparameters(
        texts_as_word_lists,
        path_to_precomputed_optimal_model,
        path_to_optimization_result_csv,
        current_optimal_result_from_search_file,
        pathway_to_intermediate_results,
        topics_range
):

    if os.path.exists(path_to_precomputed_optimal_model):
        return gensim.models.ldamodel.LdaModel.load(path_to_precomputed_optimal_model)

    id2word = corpora.Dictionary(texts_as_word_lists)
    corpus = [id2word.doc2bow(text) for text in texts_as_word_lists]

    num_of_docs = len(corpus)
    corpus_sets = [#gensim.utils.ClippedCorpus(corpus, int(num_of_docs * 0.75)),
                   corpus]
    corpus_title = [#'75% Corpus',
                    '100% Corpus']

    # Alpha parameter
    alpha = list(np.arange(0.05, 1, .3))
    #list(np.arange(0.05, 1, .2))
    #alpha.append('asymmetric')
    alpha.append('symmetric')
    #alpha.append('auto')

    # Beta parameter
    beta = list(np.arange(0.05, 1, .3))
    #list(np.arange(0.05, 1, .2))
    #beta.append('auto')
    beta.append(None)

    passes = list(np.arange(20, 31, 10))


    pbar = tqdm.tqdm(total=(len(beta) * len(alpha) * len(topics_range) * len(corpus_title) * len(passes)))



    model_results = pd.DataFrame.from_dict({'Validation_Set': [],
                     'Topics': [],
                     'Alpha': [],
                     'Beta': [],
                     'Coherence': []
                     })

    max_coherence = None
    best_parameters = None

    if(path.exists(path_to_optimization_result_csv)):
        model_results = pd.read_csv(path_to_optimization_result_csv)
        if(len(model_results.index)>0):
            index_max_coherence = model_results['coherence_umass'].idxmax()
            a = model_results['Alpha'][index_max_coherence]
            try:
                a = float(a)
            except ValueError:
                print("")

            b = model_results['Beta'][index_max_coherence]
            try:
                b = float(b)
            except ValueError:
                print("")

            max_coherence = float(model_results['coherence_umass'][index_max_coherence])

            nt = model_results['Topics'][index_max_coherence]
            best_parameters = gensim.models.LdaModel(corpus=corpus,#it disregards whether it was on a smaller corpus, but whatever
                                                         id2word=id2word,
                                                         num_topics= nt,
                                                         random_state=100,
                                                         #chunksize=100,
                                                         #passes=10,
                                                         alpha=a,
                                                         eta=b
                                                         )




    # iterate through validation corpuses
    for i in range(len(corpus_sets)):
        # iterate through alpha values
        for a in alpha:
            # iterare through beta values
            for b in beta:
                # iterate through number of topics
                for p in passes:
                    # iterate through number of topics
                    for k in topics_range:

                        if (b == None):
                            b_str = "None"
                        else:
                            b_str = str(b)
                        if(len(model_results['Validation_Set'])>0):
                            print(corpus_title[i]+" "+str(k)+" "+str(a)+" "+str(b)+" "+str(k)+" "+str(p))
                            fitting_result = model_results[(model_results['Validation_Set'] == corpus_title[i]) &
                                                             (model_results['Topics'] == k) &
                                                             (model_results['Passes'] == p) &
                                                           ((model_results['Alpha'] == a) | (model_results['Alpha'] == str(a))) &
                                                           ((pd.isna(model_results['Beta']) & (b ==None)) |
                                                            (model_results['Beta'] == b))]
                            list_of_matches = (fitting_result.index.tolist())
                            if 0 < len(list_of_matches):
                                pbar.update(1)
                                continue
                        # get the coherence score for the given parameters
                        (lda_model, (cv, umass, perplexity)) = compute_coherence_values_and_lda(corpus=corpus_sets[i], dictionary=id2word, passes = p,
                                                      k=k, a=a, b=b, input_data=texts_as_word_lists)


                        new_row = pd.DataFrame({'Validation_Set': corpus_title[i], 'Topics': k, 'Alpha': a,
                                                              'Beta': b_str,
                                                'Passes': p, 'coherence_cv': cv,
                                                'perplexity': perplexity,
                                                              'coherence_umass':(umass)}, index=[0])
                        # Save the model results
                        model_results = pd.concat([new_row,model_results.loc[:]]).reset_index(drop=True)
                        pd.DataFrame(model_results).to_csv(path_to_optimization_result_csv#, index=False
                        )

                        print(" "+corpus_title[i]+" k "+str(k)+" a "+str(a)+" b "+str(b)+" p "+str(p)
                              +"->cv:"+str(cv)+" umass:"+str(umass)+" perplexity:"+str(perplexity))

                        plotting.export_lda_results_as_html(lda_model,
                                                            [lda_model.id2word.doc2bow(text)
                                                                              for text in texts_as_word_lists],
                                                            pathway_to_intermediate_results+corpus_title[i]+" k "+str(k)+" a "+str(a)
                                                            +" b "+str(b)+" p "+str(p)+" cv "+str(cv)+" umass "
                                                            +str(umass)+" perplexity "+str(perplexity)+".html")

                        if(max_coherence == None or umass > max_coherence):
                            print("New best!")
                            best_parameters = lda_model
                            best_parameters.save(path_to_precomputed_optimal_model)

                            max_coherence = umass
                            plotting.export_lda_results_as_html(best_parameters, [best_parameters.id2word.doc2bow(text)
                                                                                  for text in texts_as_word_lists],
                                                                current_optimal_result_from_search_file)





                        pbar.update(1)
    pbar.close()


    max_umass = model_results.coherence_umass.max()
    min_umass = model_results.coherence_umass.min()

    max_cv = model_results.coherence_cv.max()
    min_cv = model_results.coherence_cv.min()

    max_perplexity = model_results.perplexity.max()
    min_perplexity = model_results.perplexity.min()

    score_fct = lambda x: (2*(x.coherence_umass-min_umass)/(max_umass-min_umass)+
                           (x.coherence_cv-min_cv)/(max_cv-min_cv)+
                           -1*(x.perplexity-min_perplexity)/(max_perplexity-min_perplexity))

    model_results = model_results[(model_results['Topics'] >= min(topics_range)) & (model_results['Topics'] <= max(topics_range))]


    model_results = model_results.assign(score=score_fct)

    max_line_id = model_results['score'].idxmax()
    max_line = model_results.loc[max_line_id]

    optimal_lda = compute_lda(passes=max_line.Passes,
                corpus=corpus,
                dictionary=id2word,
                num_topics=max_line.Topics,
                a=max_line.Alpha,
                b=max_line.Beta)

    print("Optimal LDA: "+str(max_line.Topics)+" "+str(max_line.Alpha)+" "+str(max_line.Beta)+" "
          +str(max_line.coherence_cv)+"/"+str(min_cv)+"-"+str(max_cv)+"  "
          +str(max_line.coherence_umass)+"/"+str(min_umass)+"-"+str(max_umass)+"  "
          +str(max_line.perplexity)+"/"+str(min_perplexity)+"-"+str(max_perplexity)+"   score:"+str(max_line.score))

    optimal_lda.save(path_to_precomputed_optimal_model)



   #plot_coherence(passes, topics_range, model_results)

    return optimal_lda
