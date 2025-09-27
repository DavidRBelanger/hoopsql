#!/bin/bash
set -e

DB="SQLite/hoopsql.db"
SCHEMA="SQLite/schema.sql"
VIEWS="SQLite/views.sql"
DATA_DIR="data/archive"

# Remove old DB
rm -f "$DB"

# Create schema
sqlite3 "$DB" < "$SCHEMA"

# Import CSVs
sqlite3 "$DB" <<EOF
.mode csv
.import $DATA_DIR/Games.csv games
.import $DATA_DIR/Players.csv players
.import $DATA_DIR/PlayerStatistics.csv player_statistics
.import $DATA_DIR/TeamHistories.csv team_histories
.import $DATA_DIR/TeamStatistics.csv team_statistics
EOF

# Create views
sqlite3 "$DB" < "$VIEWS"

echo "hoopsql.db built successfully!"
