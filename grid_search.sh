#!/bin/bash

TREC_EVAL="/home/ujjayant-kadian/College/trec_eval-9.0.8/trec_eval"
RESULTS_DIR="grid-search-results"
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
echo "Running trec_eval on result files..."

best_map=0.0
best_file=""

for result_file in "$RESULTS_DIR"/*; do
    if [[ -f $result_file ]]; then
        filename=$(basename -- "$result_file")

        output_file="$TREC_EVAL_SCORES_DIR/${filename%.txt}_scores.txt"
        
        $TREC_EVAL "$QRELS_FILE" "$result_file" > "$output_file"

        map_score=$(grep "^map" "$output_file" | awk '{print $3}')
        
        echo "File: $filename, MAP: $map_score"

        if (( $(echo "$map_score > $best_map" | bc -l) )); then
            best_map=$map_score
            best_file=$filename
        fi
    fi
done

echo "##############################################"
echo "Best Results File: $best_file"
echo "Best Mean Average Precision (MAP): $best_map"
