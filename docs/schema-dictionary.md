# HoopsQL Schema Dictionary

This document provides a comprehensive reference for all available fields, data types, and aliases in the HoopsQL database schema. The database contains over 1.6 million player game records with comprehensive NBA statistics.

**Recent Updates**: 
- ✅ ORDER BY functionality fully working! Parser handles both `order by most/least field` and shorthand `most/least field` syntaxes.
- ✅ Games count display: Results now show total matching games at the top (e.g., "(164 games)").

---

## Database Overview

- **Database**: SQLite (76MB)
- **Primary Table**: `player_statistics` (used by HoopsQL as `player_stats`)
- **Records**: 1,627,438 player game statistics
- **Coverage**: Comprehensive NBA game data with detailed box scores

---

## Core Field Categories

### 1. Player Identity
| HoopsQL Field | Database Column | Type | Description | Example |
|---------------|-----------------|------|-------------|---------|
| `player_name` | `firstName + " " + lastName` | String | Full player name | "Kobe Bryant" |
| `first_name` | `firstName` | String | Player's first name | "Kobe" |
| `last_name` | `lastName` | String | Player's last name | "Bryant" |
| `person_id` | `personId` | String | Unique player identifier | "977" |

### 2. Game Context
| HoopsQL Field | Database Column | Type | Description | Example |
|---------------|-----------------|------|-------------|---------|
| `game_id` | `gameId` | String | Unique game identifier | "22100001" |
| `game_date` | `gameDate` | DateTime | Game date and time | "2016-04-13 22:30:00" |
| `team` | `playerteamName` | String | Player's team name | "Lakers" |
| `team_city` | `playerteamCity` | String | Player's team city | "Los Angeles" |
| `opponent` | `opponentteamName` | String | Opposing team name | "Warriors" |
| `opponent_city` | `opponentteamCity` | String | Opposing team city | "Golden State" |
| `win` | `win` | Boolean | Did player's team win? | "1" (W) or "0" (L) |
| `home_away` | `home` | Boolean | Home game for player? | "1" (Home) or "0" (Away) |

### 3. Game Classification
| HoopsQL Field | Database Column | Type | Description | Example |
|---------------|-----------------|------|-------------|---------|
| `game_type` | `gameType` | String | Type of game | "Regular Season" |
| `game_label` | `gameLabel` | String | Game designation | "Game 82" |
| `series_game` | `seriesGameNumber` | String | Playoff series game number | "4" |

### 4. Basic Statistics
| HoopsQL Field | Database Column | Type | Description | Range |
|---------------|-----------------|------|-------------|-------|
| `points` | `points` | Integer | Points scored | 0-81 |
| `rebounds` | `reboundsTotal` | Integer | Total rebounds | 0-30+ |
| `assists` | `assists` | Integer | Assists | 0-20+ |
| `steals` | `steals` | Integer | Steals | 0-10+ |
| `blocks` | `blocks` | Integer | Blocks | 0-10+ |
| `minutes` | `numMinutes` | Float | Minutes played | 0.0-48.0 |
| `turnovers` | `turnovers` | Integer | Turnovers | 0-10+ |
| `fouls` | `foulsPersonal` | Integer | Personal fouls | 0-6 |

### 5. Shooting Statistics - Field Goals
| HoopsQL Field | Database Column | Type | Description | Example |
|---------------|-----------------|------|-------------|---------|
| `field_goals_made` | `fieldGoalsMade` | Integer | Field goals made | 12 |
| `field_goals_attempted` | `fieldGoalsAttempted` | Integer | Field goals attempted | 25 |
| `field_goal_percentage` | `fieldGoalsPercentage` | Float | FG shooting percentage | 0.480 |
| `fg_made` | `fieldGoalsMade` | Integer | Alias for field goals made | 12 |
| `fg_attempted` | `fieldGoalsAttempted` | Integer | Alias for field goals attempted | 25 |
| `fg_percent` | `fieldGoalsPercentage` | Float | Alias for FG percentage | 48.0% |

### 6. Shooting Statistics - Three Pointers
| HoopsQL Field | Database Column | Type | Description | Example |
|---------------|-----------------|------|-------------|---------|
| `three_pointers_made` | `threePointersMade` | Integer | Three-pointers made | 8 |
| `three_pointers_attempted` | `threePointersAttempted` | Integer | Three-pointers attempted | 15 |
| `three_point_percentage` | `threePointersPercentage` | Float | 3P shooting percentage | 0.533 |
| `made_threes` | `threePointersMade` | Integer | Alias for 3P made | 8 |
| `threes_made` | `threePointersMade` | Integer | Alias for 3P made | 8 |
| `threes_attempted` | `threePointersAttempted` | Integer | Alias for 3P attempted | 15 |
| `three_percent` | `threePointersPercentage` | Float | Alias for 3P percentage | 53.3% |

### 7. Shooting Statistics - Free Throws
| HoopsQL Field | Database Column | Type | Description | Example |
|---------------|-----------------|------|-------------|---------|
| `free_throws_made` | `freeThrowsMade` | Integer | Free throws made | 6 |
| `free_throws_attempted` | `freeThrowsAttempted` | Integer | Free throws attempted | 8 |
| `free_throw_percentage` | `freeThrowsPercentage` | Float | FT shooting percentage | 0.750 |
| `ft_made` | `freeThrowsMade` | Integer | Alias for FT made | 6 |
| `ft_attempted` | `freeThrowsAttempted` | Integer | Alias for FT attempted | 8 |
| `ft_percent` | `freeThrowsPercentage` | Float | Alias for FT percentage | 75.0% |

### 8. Rebounding Statistics
| HoopsQL Field | Database Column | Type | Description | Example |
|---------------|-----------------|------|-------------|---------|
| `rebounds_total` | `reboundsTotal` | Integer | Total rebounds | 12 |
| `rebounds_offensive` | `reboundsOffensive` | Integer | Offensive rebounds | 3 |
| `rebounds_defensive` | `reboundsDefensive` | Integer | Defensive rebounds | 9 |
| `rebounds` | `reboundsTotal` | Integer | Alias for total rebounds | 12 |
| `offensive_rebounds` | `reboundsOffensive` | Integer | Alias for OREB | 3 |
| `defensive_rebounds` | `reboundsDefensive` | Integer | Alias for DREB | 9 |

### 9. Advanced Statistics
| HoopsQL Field | Database Column | Type | Description | Example |
|---------------|-----------------|------|-------------|---------|
| `plus_minus` | `plusMinusPoints` | Integer | Plus/minus rating | +15 |
| `efficiency` | *Calculated* | Float | Player efficiency rating | Calculated field |

---

## Query Scope Field Availability

### Games Scope (`get games where ...`)
**Available**: All fields listed above are available in games scope, representing individual game box scores.

**Most Common Fields**:
- `points`, `rebounds`, `assists` - Basic counting stats
- `field_goals_made/attempted`, `three_pointers_made/attempted`, `free_throws_made/attempted` - Shooting
- `team`, `opponent`, `game_date`, `win` - Game context
- `minutes`, `steals`, `blocks`, `turnovers` - Additional stats

### Aggregation Scope (`get avg(games) where ...`)
**Available**: Statistical aggregations (AVG, MIN, MAX) of all numerical fields from games scope.

**Returns**: 
- Average, minimum, and maximum values for specified conditions
- Qualified games count
- Statistical summary format

### Future Scopes (Planned)
- **Seasons Scope**: Per-season aggregated statistics
- **Career Scope**: Career totals and achievements

---

## Sorting and Ordering

### ORDER BY Syntax
- `order by most <field>` - Sort descending (highest first)  
- `order by least <field>` - Sort ascending (lowest first)

### Sortable Fields
All numerical fields can be used for sorting:
- `points`, `rebounds`, `assists`, `steals`, `blocks`
- `field_goals_made`, `three_pointers_made`, `free_throws_made` 
- `minutes`, `turnovers`, `fouls`, `plus_minus`
- Date fields: `game_date`

### Current Implementation Status
- ✅ **Syntax Parsing**: ORDER BY clauses are correctly parsed (both full and shorthand syntax)
- ✅ **SQL Generation**: ORDER BY SQL is generated in execution plans
- ✅ **Actual Sorting**: Results now properly sorted (parser fix applied)
- ✅ **Complete Functionality**: All ORDER BY operations working correctly

---

## Field Aliases and Shortcuts

### Percentage Fields
- `fg%` → `field_goal_percentage`
- `3p%` → `three_point_percentage` 
- `ft%` → `free_throw_percentage`

### Common Abbreviations
- `reb` → `rebounds`
- `ast` → `assists`
- `stl` → `steals`
- `blk` → `blocks`
- `to` → `turnovers`
- `pf` → `fouls`

### Shooting Shortcuts
- `fgm` → `field_goals_made`
- `fga` → `field_goals_attempted`
- `3pm` → `three_pointers_made`
- `3pa` → `three_pointers_attempted`
- `ftm` → `free_throws_made`
- `fta` → `free_throws_attempted`

---
 
## Data Types and Ranges

### Numerical Ranges (Typical)
- **Points**: 0-81 (Kobe's 81-point game is maximum)
- **Minutes**: 0.0-48.0+ (overtime games can exceed 48)
- **Rebounds**: 0-30+ (exceptional performances)
- **Assists**: 0-20+ (playmaker performances)
- **Shooting Percentages**: 0.000-1.000 (0% to 100%)

### String Values
- **Team Names**: Official team names ("Lakers", "Warriors", "Celtics")
- **Player Names**: Full names with proper capitalization
- **Win/Loss**: "1" for wins, "0" for losses
- **Home/Away**: "1" for home games, "0" for away games

### Date Format
- **Game Dates**: "YYYY-MM-DD HH:MM:SS" (e.g., "2016-04-13 22:30:00")

---

## Query Examples by Field Category

### Basic Stats Query
```sql
Player p = "LeBron James" get games where p.points >= 30 and p.rebounds >= 10 and p.assists >= 10
```

### Shooting Efficiency Query  
```sql
Player p = "Stephen Curry" get games where p.three_pointers_made >= 8 and p.three_point_percentage >= 0.500
```

### Team Performance Query
```sql
Player p = "Michael Jordan" get games where p.team = "Bulls" and p.points >= 40 and p.win = "1"
```

### Statistical Aggregation Query
```sql
Player p = "Kobe Bryant" get avg(games) where p.points >= 50
```

### Sorting Query (ORDER BY)
```sql
Player p = "Kobe Bryant" get games where p.points >= 40 order by least points limit 5
Player p = "Stephen Curry" get games where p.made_threes >= 5 order by most made_threes limit 10
```

**Now Working**: ORDER BY functionality is fully operational! Features include:
- Full syntax: `order by most points` or `order by least points` 
- Shorthand: `most points` or `least points`
- Games count display: Shows total matching games at top of results (e.g., "(135 games)")

---

## Database Schema Notes

### Storage Format
- All numeric values stored as TEXT in SQLite
- HoopsQL automatically converts to appropriate types during query processing
- Boolean fields use "1"/"0" string representation

### Performance Considerations
- Primary queries on `player_name` and `game_date` are optimized
- Multi-player queries use `game_id` joins for same-game detection
- Large result sets automatically limited to first 10 results with indication

### Data Quality
- Comprehensive NBA coverage with official statistics
- All games include complete box score data
- Historical data spans multiple seasons
- Regular season and playoff games included

---

## Field Mapping Reference

For developers extending HoopsQL, here's the complete mapping between HoopsQL field names and database columns:

```java
// Core field mappings used in Planner.java
"player_name" -> "CONCAT(firstName, ' ', lastName)"
"team" -> "playerteamName" 
"opponent" -> "opponentteamName"
"points" -> "CAST(points AS INTEGER)"
"rebounds" -> "CAST(reboundsTotal AS INTEGER)"
"assists" -> "CAST(assists AS INTEGER)"
"game_date" -> "gameDate"
"win" -> "CASE WHEN win = '1' THEN 'W' ELSE 'L' END"
// ... (additional mappings in source code)
```

This schema dictionary serves as the complete reference for building HoopsQL queries and understanding the available data structure.
