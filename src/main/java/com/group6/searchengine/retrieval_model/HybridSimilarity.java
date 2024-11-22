package com.group6.searchengine.retrieval_model;

import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.Similarity;

public class HybridSimilarity extends Similarity {

    private final Similarity bm25Similarity;
    private final Similarity vsmSimilarity;
    private final double bm25Weight;
    private final double vsmWeight;

    public HybridSimilarity(Similarity bm25Similarity, Similarity vsmSimilarity, double bm25Weight, double vsmWeight) {
        this.bm25Similarity = bm25Similarity;
        this.vsmSimilarity = vsmSimilarity;
        this.bm25Weight = bm25Weight;
        this.vsmWeight = vsmWeight;
    }

    @Override
    public long computeNorm(FieldInvertState state) {
        return bm25Similarity.computeNorm(state); // Use BM25 norms
    }

    @Override
    public SimScorer scorer(float boost, CollectionStatistics collectionStats, TermStatistics... termStats) {
        SimScorer bm25Scorer = bm25Similarity.scorer(boost, collectionStats, termStats);
        SimScorer vsmScorer = vsmSimilarity.scorer(boost, collectionStats, termStats);

        return new SimScorer() {
            @Override
            public float score(float freq, long norm) {
                double bm25Score = bm25Scorer.score(freq, norm);
                double vsmScore = vsmScorer.score(freq, norm);
                return (float) (bm25Weight * bm25Score + vsmWeight * vsmScore);
            }

            @Override
            public Explanation explain(Explanation freq, long norm) {
                Explanation bm25Explanation = bm25Scorer.explain(freq, norm);
                Explanation vsmExplanation = vsmScorer.explain(freq, norm);

                double bm25Score = bm25Weight * bm25Explanation.getValue().doubleValue();
                double vsmScore = vsmWeight * vsmExplanation.getValue().doubleValue(); 

                return Explanation.match(
                    (float) (bm25Score + vsmScore),
                    "Hybrid score: combined BM25 and VSM scores",
                    bm25Explanation, vsmExplanation
                );
            }
        };
    }
}
