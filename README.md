# HoopsQL

A powerful SQL-like query language designed specifically for NBA statistics analysis. Query individual games, calculate season averages, and discover insights from over 1.6 million player game records with intuitive, basketball-focused syntax.

## Database Setup

The SQLite database (76MB, 1.6M records) is hosted separately due to GitHub's file size limits.

**Download the database:** [hoopsql.db](https://drive.google.com/uc?export=download&id=1fjq-9XWXE1uUFCQr0eRXkaTzS8HpbuaT)

### Quick Setup:
1. Clone this repository
2. Download the database file (link above) 
3. Place it at: `SQLite/hoopsql.db`
4. Run: `./hoopsql`

### Command Line Setup:
```bash
# Clone and setup
git clone https://github.com/DavidRBelanger/hoopsql.git
cd hoopsql

# Download database
wget -O SQLite/hoopsql.db "https://drive.google.com/uc?export=download&id=1fjq-9XWXE1uUFCQr0eRXkaTzS8HpbuaT"

# Make executable and run
chmod +x hoopsql
./hoopsql 'Player p = "Stephen Curry" get games where p.points >= 30 limit 5'
```

## Features

- **Intuitive Syntax**: Natural basketball terminology (`p.points >= 30`, `p.made_threes >= 8`)
- **Multiple Query Types**: Individual games, season averages, statistical analysis
- **Advanced Sorting**: `order by most points`, `order by least turnovers`
- **Multi-Player Queries**: Automatic same-game detection for teammate analysis
- **High Performance**: 1.6M+ records with optimized SQLite backend
- **Rich Statistics**: Points, rebounds, assists, shooting percentages, and 20+ more fields

## Example Queries

### Individual Game Analysis
```sql
-- Kobe's 40+ point games
Player p = "Kobe Bryant" get games where p.points >= 40 order by most points limit 10

-- LeBron's triple-doubles
Player p = "LeBron James" get games where p.points >= 10 and p.rebounds >= 10 and p.assists >= 10
```

### Season Averages  
```sql
-- Jordan's stats in championship seasons
Player p = "Michael Jordan" get avg(games) where p.team = "Bulls" and p.points >= 25

-- Curry's three-point efficiency
Player p = "Stephen Curry" get avg(games) where p.made_threes >= 5
```

### Multi-Player Analysis
```sql
-- Shaq and Kobe games together
Player p = "Shaquille O'Neal"
Player q = "Kobe Bryant" 
get games where p.played and q.played and p.points >= 25 and q.points >= 25
```

## Documentation

- **[Complete Schema Dictionary](docs/schema-dictionary.md)** - All available fields, aliases, and examples
- **[Setup Guide](docs/spec-doc.md)** - Installation and usage instructions 
- **[Distribution Guide](docs/DISTRIBUTION.md)** - Maven-free setup options
- **[Data Pipeline](docs/data-pipeline.md)** - Database structure and data sources

## Installation

### Prerequisites
- Java 8+ installed
- 76MB free disk space (for database)

### Option 1: Direct Usage (Recommended)
```bash
# Download the pre-built JAR (14MB)
wget <jar-url>
chmod +x hoopsql
./hoopsql
```

### Option 2: Build from Source
```bash
git clone <repo-url>
cd HoopsQL
mvn clean package -f app/pom.xml
java -jar hoopsql-1.0.jar
```

### Windows PowerShell
```powershell
# Download JAR file
java -jar hoopsql-1.0.jar
```

## Available Statistics

| Category | Fields | Examples |
|----------|--------|----------|
| **Scoring** | `points`, `field_goals_made`, `three_pointers_made`, `free_throws_made` | `p.points >= 50` |
| **Playmaking** | `assists`, `turnovers`, `steals` | `p.assists >= 10` |
| **Rebounding** | `rebounds`, `offensive_rebounds`, `defensive_rebounds` | `p.rebounds >= 15` |
| **Defense** | `steals`, `blocks`, `fouls` | `p.blocks >= 5` |
| **Efficiency** | `field_goal_percentage`, `three_point_percentage`, `plus_minus` | `p.fg_percent >= 0.500` |
| **Context** | `team`, `opponent`, `game_date`, `win`, `minutes` | `p.team = "Lakers"` |

## Interactive Mode

```bash
./hoopsql
> Player p = "Magic Johnson" get games where p.assists >= 15
> get avg(games) where p.team = "Lakers" 
> exit
```

## Query Language Features

### Syntax Highlights
- **Player Variables**: `Player p = "Name"` - Clean, readable player assignment
- **Smart Filtering**: `where p.points >= 30 and p.rebounds >= 10` 
- **Flexible Sorting**: `order by most field` or `order by least field`
- **Result Limiting**: `limit 10` for manageable output
- **Scope Selection**: `get games` for individual games, `get avg(games)` for averages

### Advanced Features
- **Automatic Same-Game Detection**: Multi-player queries automatically find games where players played together
- **Smart Field Mapping**: `p.played` creates appropriate game constraints
- **Comprehensive Field Aliases**: `reb`→`rebounds`, `ast`→`assists`, `3pm`→`three_pointers_made`
- **Statistical Aggregations**: Automatic AVG/MIN/MAX calculations with qualified game counts

## Database

- **Size**: 76MB SQLite database
- **Records**: 1,627,438 individual player game statistics
- **Coverage**: Comprehensive NBA historical data
- **Performance**: Optimized indexes for player names and dates

## Contributing

Contributions welcome! Areas for expansion:
- Additional statistical scopes (seasons, career totals)
- Advanced analytics (efficiency ratings, advanced metrics)
- Data visualization integration
- API endpoints for web integration

## License

MIT License - see [LICENSE](LICENSE) file for details.

## Example Output

```
Results:
========
(164 games)

1. 2016-02-27 20:30:00 - Stephen Curry (Warriors vs Thunder) - W
   Stats: 46 pts, 6 reb, 7 ast, 12 to

2. 2015-10-30 22:30:00 - Stephen Curry (Warriors vs Pelicans) - W  
   Stats: 53 pts, 9 reb, 5 ast, 8 to

... (showing first 10 results)
```

---

**HoopsQL** - Where basketball meets data science. Query like you think about the game!
