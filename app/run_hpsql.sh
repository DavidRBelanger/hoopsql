#!/bin/bash
# HoopsQL Runner Script
# Usage: ./run_hpsql.sh my_query.hpsql

if [ $# -eq 0 ]; then
    echo "Usage: ./run_hpsql.sh <query-file.hpsql>"
    echo "Example: ./run_hpsql.sh sample.hpsql"
    exit 1
fi

# Compile if needed
echo "Compiling HoopsQL..."
mvn -q compile

# Run the query
echo "Running HoopsQL query: $1"
echo ""
java -cp target/classes com.hoopsql.cli.HoopsQLRunner "$1"
