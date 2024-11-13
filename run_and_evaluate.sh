#!/bin/bash

TREC_EVAL="/home/ujjayant-kadian/College/trec_eval-9.0.8/trec_eval"

RESULTS_DIR="results"
TREC_EVAL_SCORES_DIR="trec_eval_scores"
QRELS_FILE="qrels.assignment2.part1"

echo "##############################################"
if [ -d "$TREC_EVAL_SCORES_DIR" ]; then
    echo "Cleaning up the trec_eval_scores directory..."
    rm -rf "$TREC_EVAL_SCORES_DIR"/*
else
    echo "Creating directory for trec_eval_scores..."
    mkdir -p "$TREC_EVAL_SCORES_DIR"
fi

echo "##############################################"
echo "Running Lucene Search Engine..."
java -jar target/lucene-search-engine-1.2.jar

echo "##############################################"
echo "Running trec_eval on result files..."
for result_file in $RESULTS_DIR/*; do
    if [[ -f $result_file ]]; then
        filename=$(basename -- "$result_file")
        
        scoring_method=$(echo "$filename" | awk -F'_' '{print $(NF-1)}')
        
        technique=$(echo "$filename" | awk -F'_' -v sm="$scoring_method" '{for (i=1; i<NF-1; i++) printf $i "_"; printf $NF;}' | sed 's/_$//')
        

        output_file="$TREC_EVAL_SCORES_DIR/${technique}_${scoring_method}_scores.txt"
        $TREC_EVAL $QRELS_FILE "$result_file" > "$output_file"

        echo "Stored trec_eval scores for $filename in $output_file"
    fi
done

echo "##############################################"
echo "Analyzing trec_eval scores to find the best query formation technique..."
best_map=0.0
best_technique=""

for score_file in $TREC_EVAL_SCORES_DIR/*; do
    if [[ -f $score_file ]]; then
        map_score=$(grep "^map" "$score_file" | awk '{print $3}')
        
        filename=$(basename -- "$score_file")
        scoring_method=$(echo "$filename" | awk -F'_' '{print $(NF-1)}')
        technique=$(echo "$filename" | awk -F'_' -v sm="$scoring_method" '{for (i=1; i<NF-1; i++) printf $i "_"; printf $NF;}' | sed 's/_$//')

        echo "Technique: $technique, Scoring Method: $scoring_method, MAP: $map_score"
        echo "---------------------------------------------------"

        if (( $(echo "$map_score > $best_map" | bc -l) )); then
            best_map=$map_score
            best_technique="${technique}_${scoring_method}"
        fi
    fi
done

echo "##############################################"
echo "Best Query Formation Technique: $best_technique"
echo "Mean Average Precision (MAP): $best_map"
