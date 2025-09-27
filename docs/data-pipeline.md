# NBA-QL Data Pipeline

This document outlines the data pipeline for NBA-QL, from data ingestion to query execution.

---

## Overview

The NBA-QL data pipeline consists of three main components:

1. **Data Ingestion**: Annual data refresh after NBA season ends (June/July)
2. **Data Storage**: SQLite database for all queries and operations
3. **Distribution**: Firestore used only for version management and SQLite database distribution

---

## 1. Data Source

- **Dataset**: Alternative NBA dataset with comprehensive player box scores
- **Update Frequency**: Annual (post-season)
- **Format**: SQLite database only
- **Content**: Comprehensive NBA statistics including individual player game box scores, teams, seasons

### Dataset Structure

The dataset contains a pre-built SQLite database (`nba.sqlite`) with comprehensive NBA statistics.

#### SQLite Database Tables
- **game** (65,698 records) - Team-level game statistics (home/away splits)
- **player** (4,815 records) - Player information and status
- **common_player_info** - Detailed player information
- **team** - Team information
- **draft_history** - Draft information
- **play_by_play** - Detailed game events
- **other_stats** - Additional statistics
- And more specialized tables

#### Current Limitations
**Note**: This dataset lacks individual player box scores, which are core to NBA-QL's `games` scope. The current dataset provides:
- Team-level game statistics (home/away splits)
- Player information and career data
- Play-by-play data (not suitable for box score derivation)

**Alternative data sources with player box scores will need to be sourced for full NBA-QL functionality.**

---

## 2. Data Ingestion Pipeline

### 2.1 Annual Data Update Process

Once per year (after NBA season ends in June/July):
- Manually download latest dataset from Kaggle or alternative source
- Process and validate SQLite database integrity
- Upload new SQLite database to Firestore storage
- Update version metadata in Firestore

### 2.2 Data Processing Steps

1. **Source**: Download latest NBA dataset (SQLite database)
2. **Validation**: Check SQLite database integrity and completeness
3. **Enhancement**: Add any missing indexes or computed fields for NBA-QL
4. **Version**: Generate version identifier (timestamp + database hash)
5. **Upload**: Upload SQLite file to Firestore storage bucket
6. **Metadata**: Update version info in Firestore for compiler sync

---

## 3. Firestore Distribution Structure

### 3.1 Storage Organization

Firestore is used ONLY for distribution and version management:

```
nba-ql-distribution/
  ├── metadata/
  │   └── version          # Version information document
  └── storage/
      └── nba-database.db  # SQLite database file (Cloud Storage)
```

### 3.2 Version Management

Version information stored in `metadata/version` document:
```json
{
  "version": "2025-07-15-abc123",
  "timestamp": "2025-07-15T10:00:00Z",
  "database_hash": "abc123456789...",
  "database_size_mb": 45,
  "source": "kaggle-wyattowalsh-basketball-2025",
  "download_url": "gs://nba-ql-storage/nba-database-2025-07-15.db"
}
```

---

## 4. NBA-QL Compiler Database Sync

### 4.1 Local Database Management

The compiler maintains:
- Local SQLite database file (`~/.nba-ql/nba-data.db`)
- Version tracking file (`~/.nba-ql/version.json`)

```json
{
  "local_version": "2025-07-15-abc123",
  "database_path": "~/.nba-ql/nba-data.db",
  "last_sync": "2025-07-15T10:30:00Z",
  "database_size_mb": 45
}
```

### 4.2 Sync Process

When the compiler starts:

1. **Check Version**: Compare local version with Firestore `metadata/version`
2. **Determine Action**:
   - If versions match: Use local SQLite database
   - If Firestore is newer: Download new SQLite database
   - If local database missing: Download fresh copy
3. **Download**: Download SQLite file from Cloud Storage URL
4. **Validate**: Verify database integrity and hash
5. **Replace**: Replace local database file atomically
6. **Update**: Record new version locally

### 4.3 Query Execution

All NBA-QL queries execute directly against the local SQLite database:
- No network calls during query execution
- Fast local SQLite performance
- Full SQL query capabilities available

---

## 5. Implementation Details

### 5.1 Required Technologies

- **Java**: NBA-QL compiler and query execution
- **SQLite JDBC Driver**: Database connectivity for Java
- **Google Cloud SDK**: Firestore interaction for version management
- **HTTP Client**: For downloading SQLite database updates

### 5.2 Java SQLite Integration

The NBA-QL compiler will use standard Java SQLite connectivity:

```java
// SQLite JDBC dependency
implementation 'org.xerial:sqlite-jdbc:3.42.0.0'

// Database connection
String dbPath = System.getProperty("user.home") + "/.nba-ql/nba-data.db";
Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);

// Query execution
PreparedStatement stmt = conn.prepareStatement("SELECT * FROM game WHERE season = ?");
stmt.setString(1, "2023-24");
ResultSet rs = stmt.executeQuery();
```

### 5.3 Configuration Files

#### `config/nba-ql.properties`
```properties
# Firestore configuration
firestore.project_id=nba-ql-project
firestore.database=(default)

# Local storage
local.database_path=~/.nba-ql/nba-data.db
local.version_file=~/.nba-ql/version.json

# Sync settings
sync.check_on_startup=true
sync.timeout_seconds=30
```

### 5.3 Scripts Structure

```
scripts/
├── sync-data.sh           # Main cron job script
├── kaggle-download.py     # Download from Kaggle
├── process-data.py        # Transform and validate
├── firestore-upload.py    # Upload to Firestore
└── cleanup.py             # Maintenance tasks
```

---

## 6. Monitoring and Maintenance

### 6.1 Logging

- All pipeline operations logged with timestamps
- Error tracking and alerting
- Performance metrics (download time, processing time, sync time)

### 6.2 Health Checks

- Daily pipeline success/failure notifications
- Data integrity validation
- Firestore connection monitoring
- Local cache consistency checks

### 6.3 Backup Strategy

- Maintain last N versions in Firestore
- Local backup of processed data
- Rollback capability for bad data updates

---

## 7. Future Enhancements

- Real-time updates during game days
- Multiple data source integration
- Automatic schema evolution
- Distributed caching for multiple compiler instances
- WebSocket notifications for live data updates

---

## 8. Development Setup

### 8.1 Prerequisites

- Java 17+ with SQLite JDBC driver
- Google Cloud project with Firestore enabled
- Sufficient local storage for SQLite database (~50-100MB)

### 8.2 Initial Setup

1. Set up Google Cloud project and Firestore
2. Add SQLite JDBC dependency to Java project
3. Configure local database directory (`~/.nba-ql/`)
4. Run initial database download and sync
5. Verify SQLite connectivity and basic queries
