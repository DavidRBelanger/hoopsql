# HoopsQL Specification (Living Doc)

This is the working specification for HoopsQL, a domain-specific query language for NBA basketball statistics. It will be updated in place as design choices are made. It intentionally excludes the exhaustive list of stat aliases and raw CSV field mappings. Those live in a separate Schema Dictionary.

---

## 0. Changelog

- **v1.0**: Initial implementation with full compiler pipeline
- **v1.1**: Added aggregation support with `avg(games)` scope
- **v1.2**: Implemented automatic multi-player same-game detection
- **v1.3**: Added intuitive `p.played` syntax for explicit multi-player queries

---

## 0.1. How to Use HoopsQL

### Installation and Setup

## End User Distribution (No Maven/Source Required)

### Prerequisites
- **Java 11 or higher** (required for all platforms)
- Check your Java version: `java -version`
- If needed, download from: [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://openjdk.org/)

### Download Distribution Package
1. Download the HoopsQL distribution containing:
   - `hoopsql-0.1.0-SNAPSHOT.jar` (14MB standalone executable)
   - `SQLite/hoopsql.db` (76MB NBA statistics database)
   - Platform-specific launcher scripts (optional)

### Platform-Specific Setup

#### Windows (PowerShell)
```powershell
# 1. Create HoopsQL directory
mkdir C:\HoopsQL
cd C:\HoopsQL

# 2. Place downloaded files:
#    - hoopsql-0.1.0-SNAPSHOT.jar
#    - SQLite\hoopsql.db

# 3. Test installation
java -jar hoopsql-0.1.0-SNAPSHOT.jar "Player p = `"Kobe Bryant`" get games where p.points >= 40"

# 4. Create convenient alias (optional - add to PowerShell profile)
function hoopsql { java -jar "C:\HoopsQL\hoopsql-0.1.0-SNAPSHOT.jar" $args }

# 5. Usage examples:
hoopsql  # Interactive mode
hoopsql "Player p = `"Stephen Curry`" get games where p.points >= 30"
```

#### Linux/macOS (Bash/Zsh)
```bash
# 1. Create HoopsQL directory
mkdir ~/HoopsQL
cd ~/HoopsQL

# 2. Place downloaded files:
#    - hoopsql-0.1.0-SNAPSHOT.jar  
#    - SQLite/hoopsql.db

# 3. Test installation
java -jar hoopsql-0.1.0-SNAPSHOT.jar "Player p = \"Kobe Bryant\" get games where p.points >= 40"

# 4. Create launcher script (optional)
cat > hoopsql << 'EOF'
#!/bin/bash
java -jar "$HOME/HoopsQL/hoopsql-0.1.0-SNAPSHOT.jar" "$@"
EOF

chmod +x hoopsql
sudo mv hoopsql /usr/local/bin/  # or add ~/HoopsQL to PATH

# 5. Usage examples:
hoopsql  # Interactive mode
hoopsql "Player p = \"Stephen Curry\" get games where p.points >= 30"
```

### CLI Usage (All Platforms)

#### Direct Java Execution
```bash
# Interactive shell mode
java -jar hoopsql-0.1.0-SNAPSHOT.jar

# Run a single query  
java -jar hoopsql-0.1.0-SNAPSHOT.jar "Player p = \"Kobe Bryant\" get games where p.points >= 40"

# Execute from file
java -jar hoopsql-0.1.0-SNAPSHOT.jar my_query.hpsql
```

#### With Launcher Script (if installed)
```bash
# Interactive mode
hoopsql

# Single query
hoopsql "Player p = \"Kobe Bryant\" get games where p.points >= 40"

# From file
hoopsql my_query.hpsql
```

## Developer Setup (Source Code)
- Java 11 or higher 
- Maven 3.6+ for building from source
- Git clone repository and run `mvn clean package`

### Permanent Setup Tips

#### Windows PowerShell Profile Setup
To make the `hoopsql` function permanent in PowerShell:
```powershell
# 1. Check if profile exists
Test-Path $PROFILE

# 2. Create profile if it doesn't exist
if (!(Test-Path $PROFILE)) { New-Item -Path $PROFILE -Type File -Force }

# 3. Add HoopsQL function to profile
Add-Content $PROFILE 'function hoopsql { java -jar "C:\HoopsQL\hoopsql-0.1.0-SNAPSHOT.jar" $args }'

# 4. Reload profile
. $PROFILE
```

#### Linux/macOS Permanent PATH Setup
```bash
# Add to ~/.bashrc or ~/.zshrc
echo 'export PATH="$HOME/HoopsQL:$PATH"' >> ~/.bashrc
source ~/.bashrc

# Or create symlink (alternative)
ln -s ~/HoopsQL/hoopsql ~/.local/bin/hoopsql
```

### Troubleshooting
- **Java not found**: Ensure Java 11+ is installed and in PATH
- **Database not found**: Ensure `SQLite/hoopsql.db` is in the same directory as the JAR
- **Permission denied (Linux)**: Run `chmod +x hoopsql` on the launcher script
- **PowerShell execution policy**: Run `Set-ExecutionPolicy RemoteSigned -Scope CurrentUser` if needed

### Available Endpoints
1. **Interactive Shell**: Real-time query execution with help system
2. **Direct Query**: Single command execution 
3. **File Execution**: Batch processing of `.hpsql` files

### Example Queries (Actual Working Syntax)
```sql
-- Basic player lookup
Player p = "Kobe Bryant" get games where p.points >= 40

-- Multi-player same game (automatic detection)
Player p = "Kobe Bryant" Player q = "Shaquille O'Neal" get games where p.points >= 30 and q.points >= 20

-- Explicit multi-player with p.played syntax
Player p = "LeBron James" Player q = "Anthony Davis" get games where p.played and q.played and p.points >= 25

-- Aggregation with statistical analysis
Player p = "Stephen Curry" get avg(games) where p.points >= 30

-- Team-based queries  
Player p = "Michael Jordan" get games where p.team = "Bulls" and p.points >= 50

-- Opponent analysis
Player p = "Kobe Bryant" get games where p.opponent = "Celtics" and p.points >= 35
```

### Sample Output Format
```
Results:
========

1. 2016-04-13 22:30:00 - Kobe Bryant (Lakers vs Jazz) - W
   Stats: 60 pts, 4 reb, 4 ast

2. 2009-02-02 19:30:00 - Kobe Bryant (Lakers vs Knicks) - W  
   Stats: 61 pts, 7 reb, 1 ast

3. 2008-03-28 22:30:00 - Kobe Bryant (Lakers vs Grizzlies) - L
   Stats: 53 pts, 10 reb, 8 ast

... (showing first 10 results)
```

For aggregation queries:
```
Statistical Summary:
===================
Player: Stephen Curry
Condition: points >= 30 (Qualified Games: 47)

Points: 34.5 avg, 52 max, 30 min
Rebounds: 5.2 avg, 10 max, 2 min  
Assists: 5.8 avg, 9 max, 3 min
```

### Current Functionality
- ✅ Complete lexer/parser/planner compilation pipeline
- ✅ SQLite database integration with 76MB NBA statistics
- ✅ Multi-player queries with automatic same-game detection
- ✅ Statistical aggregation with `avg(games)` scope
- ✅ Interactive CLI with help system and error handling
- ✅ File-based query execution
- ✅ Comprehensive test suite with 92+ test files

---

## 1. Core ideas

- A query has four parts: declarations, a `get` statement that chooses a scope, a `where` block for filters, and optional `select`, `order by`, and `limit` clauses.
- Declarations introduce typed variables like `Player p` or bind them to canonical objects like `Player p = LeBron_James`.
- Scopes are `games`, `seasons`, or `careers`.
- All equality and binding use `=`. There is no `==`.
- Variables are file wide. Placement is flexible. Declarations can appear before or after `get`, and inside `where`.

---

## 2. Scopes

Exactly one scope per query.

- **games**. One row per player game box score.
- **avg(games)**. Statistical aggregation across multiple games (returns AVG, MIN, MAX).
- **seasons**. One row per player season aggregate (planned).
- **careers**. One row per player career aggregate and accolades (planned).

Examples:

```plaintext
get games where ...
get avg(games) where ...
get seasons where ...    # Future implementation
get careers where ...    # Future implementation
```

**Current Implementation Status**:
- ✅ `games` scope: Fully implemented with complete SQL generation
- ✅ `avg(games)` scope: Statistical aggregation with AVG/MIN/MAX across game results  
- 🚧 `seasons` and `careers` scopes: Planned for future releases

---

## 3. Declarations

### 3.1 Forms

- Generic open variable:
  ```plaintext
  Player p
  Team t
  Opponent o
  ```
  The variable can match any object of that type. Filters constrain it further.

- Bound to canonical object:
  ```plaintext
  Player p = LeBron_James
  Team t = Warriors
  Opponent o = Celtics
  ```

- Relational bindings inside filters:
  ```plaintext
  p.team = t
  p.opponent = o
  p.game = q.game
  ```

### 3.2 Placement and scope rules

- A declaration may appear anywhere in the file. Once declared, the name is available to the rest of the file, including later lines in the same block.
- Inside `where`, a bare `EntityType var` line is treated as a declaration, not a condition.
- Redeclaring a name is an error.
- Variable names: start with a letter. Use letters, digits, underscores.

### 3.3 Canonical object names

- **Current Implementation**: Player names use quoted strings: `"Kobe Bryant"`, `"LeBron James"`, `"Stephen Curry"`
- **Current Implementation**: Team names use quoted strings: `"Lakers"`, `"Warriors"`, `"Bulls"`, `"Celtics"`
- **Future Implementation**: Canonical snake_case names like `LeBron_James`, `Los_Angeles_Lakers` (planned)
- Team historical synonyms will map to correct internal team id in future versions
- Unknown or ambiguous names produce friendly errors with suggestions

---

## 4. Query layout

A file contains any number of declarations plus exactly one `get` query. Minimal skeletons:

```plaintext
# Generic player example
Player p

get games where
    p.points >= 50
```

```plaintext
# Bound player example with inline declarations
get seasons where
    Player p = Stephen_Curry,
    p.points_per_game >= 25
select season, team, points_per_game, 3p%
order by points_per_game
limit 5
```

---

## 5. Where block

### 5.1 Format

- `where` appears after the scope on the same line as `get`.
- Conditions follow on indented lines.
- Separate conditions with commas. A trailing comma is allowed.

```plaintext
get games where
    p.points >= 30,
    p.rebounds >= 10,
    p.assists >= 10,
```

Commas imply logical AND between lines.

### 5.2 Boolean logic

- Operators: `AND`, `OR`, `NOT`. Case insensitive.
- Parentheses are supported for grouping.

```plaintext
get games where
    p.points >= 30 AND (p.rebounds >= 10 OR p.assists >= 10)
```

### 5.3 Comparison operators

- `=` equality or binding by context
- `!=` not equal
- `>`, `<`, `>=`, `<=`
- `between a and b` inclusive
- `in [value1, value2, ...]`

Examples:

```plaintext
p.team = Warriors
p.points between 25 and 35
p.position in [G, F]
p.opponent = o
```

### 5.4 Relationships and joins

- Referencing `p.<field>` in the games scope means the player box score for that game row.
- `p.team` is the player team in that row.
- `p.opponent` is the opposing team in that row.
- **AUTOMATIC BEHAVIOR**: When multiple Player variables are declared, HoopsQL automatically adds same-game constraints (no manual `p.game = q.game` needed)
- Use `p.played` for explicit multi-player queries where you want to ensure both players participated

Examples:

```plaintext
# Automatic same-game detection (recommended)
Player p = "Kobe Bryant"
Player q = "Shaquille O'Neal" 
get games where p.points >= 30 and q.points >= 20

# Explicit syntax when needed
Player p = "LeBron James"
Player q = "Anthony Davis"
get games where p.played and q.played and p.points >= 25

# Legacy syntax (still supported)
Player p
Player q
get games where
    p.points >= 30,
    q.points >= 30,
    p.game = q.game
```

---

## 6. Time and season filters

### 6.1 Literals

- Single season shorthand. A 4 digit year like `2016` means the season that ends in that calendar year. For the NBA that is 2015 to 2016.
- Explicit season identifier. `2015-2016` means the 2015 to 2016 season.
- Exact dates. `YYYY-MM-DD` or `MM-DD-YYYY`.

### 6.2 Fields

- `game_date` in games scope.
- `season` in seasons scope.
- `career_start` and `career_end` in careers scope if available.

### 6.3 Examples

```plaintext
# Christmas 2016
Player p
get games where
    game_date = 2016-12-25

# Entire 2015 to 2016 season
Player p
get games where
    season = 2016

# Date range
Player p
get games where
    game_date between 10-01-2015 and 06-20-2016
```

If both season and date filters are provided, both must match.

---

## 7. Select clause

Controls which fields are shown in the output.

### 7.1 Defaults

If you do not write `select`, each scope returns a basic set.

- Games basic. `game_date`, `season`, `home_or_away`, `team`, `opponent`, `win`, `margin`, plus the declared player headline fields `points`, `rebounds`, `assists` if a `Player` is declared.
- Seasons basic. `season`, `team`, `games_played`, `minutes_per_game`, `points_per_game`, `rebounds_per_game`, `assists_per_game`.
- Careers basic. `teams_played_for`, `seasons_played`, `games_played`, `points_total`, `points_per_game`, `rebounds_total`, `assists_total`.

### 7.2 Adding fields

```plaintext
select game_date, points, rebounds, fg%
```

- Use alias names or raw CSV names. Aliases are preferred for readability.

### 7.3 Replace the basic set

Append `basic=false` to return only the selected fields.

```plaintext
select game_date, points, rebounds basic=false
```

### 7.4 Selecting across variables

Prefix with the variable when needed.

```plaintext
Player p
Opponent o

get games where
    p.opponent = o,
    p.points >= 35
select game_date, p.points, o.name
```

---

## 8. Order by

One sort key in MVP.

- `order by points` means descending.
- `order by most points` is descending.
- `order by least points` is ascending.

Disambiguate with a variable prefix if a name exists on multiple variables.

Examples:

```plaintext
order by points
order by most points
order by least rebounds_per_game
order by p.points
```

---

## 9. Limit

Caps the number of rows returned after sorting.

```plaintext
limit 100
```

Default limit is implementation defined. If omitted, the default applies.

---

## 10. Literals and lists

- Numbers. `30`, `0.45`.
- Canonical identifiers. Unquoted `LeBron_James`, `Los_Angeles_Lakers`.
- Strings. Double quotes for free form text if a field requires it. Example: `"Madison Square Garden"`.
- Booleans. `true` and `false`.
- Lists. Square brackets with comma separated values. Example: `p.team in [Lakers, Celtics, Bulls]`.
- Ranges. `between a and b` is inclusive for numbers and dates.

---

## 11. Operator precedence

From highest to lowest:

1. Parentheses
2. Comparison operators: `=`, `!=`, `>`, `<`, `>=`, `<=`, `between`, `in`
3. `NOT`
4. `AND`
5. `OR`

Commas between lines in `where` are treated as `AND`.

---

## 12. Types and coercion

- Entity types. `Player`, `Team`, `Opponent`.
- Value types. number, string, boolean, date, season id.
- No implicit coercion between incompatible types. Numeric fields cannot be compared to teams or players.
- `=` compares by value for scalars and by identity for entities and canonical names.

---

## 13. Field availability by scope

- Games scope provides box score and game context fields for a player and the game row. Example fields: `points`, `rebounds`, `assists`, `fg%`, `3p%`, `ft%`, `minutes`, `team`, `opponent`, `win`, `margin`, `home_or_away`, `game_date`, `season`.
- Seasons scope provides totals and per game fields, plus season context. Example fields: `points_total`, `points_per_game`, `rebounds_total`, `rebounds_per_game`, `fg%`, `3p%`, `ft%`, `minutes_per_game`, `team`, `season`, `games_played`, `games_started`.
- Careers scope provides career totals and per game fields and accolades. Example fields: `points_total`, `points_per_game`, `rebounds_total`, `rebounds_per_game`, `mvp_wins`, `all_star_selections`, `teams_played_for`, `seasons_played`.

If a field is referenced that does not exist in the active scope, the compiler errors and suggests the correct scope.

---

## 14. Multi variable semantics

### 14.1 One player

```plaintext
Player p
get games where
    p.points >= 50
```

### 14.2 Two players same game (Multiple Options)

```plaintext
# OPTION 1: Automatic (Recommended) - HoopsQL detects multi-player and adds same-game constraint
Player p = "Kobe Bryant"
Player q = "Shaquille O'Neal"
get games where p.points >= 30 and q.points >= 20

# OPTION 2: Explicit syntax when you want to be clear
Player p = "LeBron James" 
Player q = "Anthony Davis"
get games where p.played and q.played and p.points >= 25

# OPTION 3: Legacy manual syntax (still supported)
Player p = "Kobe Bryant"
Player q = "Shaquille O'Neal"
get games where
    p.points >= 30,
    q.points >= 20,
    p.game = q.game
```

### 14.3 Player and opponent team

```plaintext
Player p
Opponent o = Celtics
get games where
    p.opponent = o,
    p.points >= 35
```

If two variables of the same type are used without a relation, the compiler requests a tie such as `p.game = q.game` or an explicit binding.

---

## 15. Output format

- Each result row corresponds to the unit of the scope. Games return player game rows. Seasons return player season rows. Careers return player career rows.
- Default columns are the basic set for that scope unless `basic=false` is present in the `select` clause.
- `select` can add fields or replace the basic set.
- Sorting and limiting apply after shaping the result set.

---

## 16. Comments and whitespace

- `#` starts a comment until end of line.
- Blank lines are ignored.
- Indentation is not semantic. It improves readability.
- A trailing comma after the last condition is allowed.

---

## 17. Errors and diagnostics

Errors must be clear and actionable. Show a 1 based line number, a short description, and an optional suggestion.

**Unknown field**
```
Error on line 6: Unknown field 'ppoints'.
Maybe you meant 'points'?
```

**Unknown name**
```
Error on line 1: Unknown player 'Stepen_Curry'.
Maybe you meant 'Stephen_Curry'?
```

**Field not available in scope**
```
Error on line 5: 'points_per_game' is not available in games scope.
Use seasons scope or careers scope.
```

**Unrelated variables**
```
Error on line 7: 'p' and 'q' are unrelated in games scope.
Tie them with p.game = q.game or bind one variable to a specific player.
```

**Type mismatch**
```
Error on line 8: Cannot compare 'points' (number) with 'Warriors' (team).
```

**Invalid date**
```
Error on line 6: Invalid date '2016/12/25'.
Use YYYY-MM-DD or MM-DD-YYYY.
```

**Ambiguous field**
```
Error on line 10: 'points' is ambiguous across variables.
Use p.points or q.points.
```

---

## 18. Reserved words

Reserved for grammar and cannot be used as variable names or canonical identifiers.

`get`, `where`, `select`, `order`, `by`, `most`, `least`, `between`, `in`, `and`, `or`, `not`, `true`, `false`, `limit`, `games`, `seasons`, `careers`, `Player`, `Team`, `Opponent`, `game_date`, `season`.

---

## 19. Examples (Current Working Implementation)

**A. Any 50 point game**
```sql
Player p get games where p.points >= 50
```

**B. Specific player high-scoring games**
```sql
Player p = "Kobe Bryant" get games where p.points >= 40
```

**C. Multi-player same game (automatic detection)**
```sql
Player p = "Kobe Bryant" Player q = "Shaquille O'Neal" get games where p.points >= 30 and q.points >= 20
```

**D. Statistical aggregation**
```sql
Player p = "Stephen Curry" get avg(games) where p.points >= 30
```

**E. Team-based analysis**
```sql
Player p = "Michael Jordan" get games where p.team = "Bulls" and p.points >= 50
```

**F. Opponent-specific performance**
```sql
Player p = "Kobe Bryant" get games where p.opponent = "Celtics" and p.points >= 35
```

**G. Explicit multi-player syntax**
```sql
Player p = "LeBron James" Player q = "Anthony Davis" get games where p.played and q.played and p.points >= 25
```

**H. Complex multi-condition queries**
```sql
Player p = "Stephen Curry" get games where p.points >= 30 and p.made_threes >= 8 and p.team = "Warriors"
```

## 20. Implementation Architecture

### 20.1 Core Components
- **Lexer** (`com.hoopsql.compiler.Lexer`): Tokenizes HoopsQL source code
- **Parser** (`com.hoopsql.compiler.Parser`): Builds Abstract Syntax Tree (AST)  
- **Planner** (`com.hoopsql.compiler.Planner`): Generates SQL execution plans
- **CLI Runner** (`com.hoopsql.cli.HoopsQLRunner`): Interactive shell and file execution
- **Storage** (`com.hoopsql.storage.SQLiteStorage`): Database connection management

### 20.2 Database Schema
- **Primary Table**: `player_stats` with columns including:
  - `player_name`, `team`, `opponent`, `game_date`
  - `points`, `rebounds`, `assists`, `steals`, `blocks`
  - `field_goals_made/attempted`, `three_pointers_made/attempted`, `free_throws_made/attempted`
  - `win` (boolean), `home_away` indicator
- **Size**: 76MB database with comprehensive NBA statistics

### 20.3 SQL Generation Examples
```sql
-- HoopsQL: Player p = "Kobe Bryant" get games where p.points >= 40
-- Generated SQL:
SELECT * FROM player_stats p_stats 
WHERE p_stats.player_name = ? AND p_stats.points >= ?

-- HoopsQL: Multi-player with automatic same-game detection
-- Generated SQL: 
SELECT * FROM player_stats p1_stats 
JOIN player_stats p2_stats ON p1_stats.game_id = p2_stats.game_id
WHERE p1_stats.player_name = ? AND p2_stats.player_name = ?
AND p1_stats.points >= ? AND p2_stats.points >= ?
```

### 20.4 Error Handling
- **Friendly Syntax Errors**: "Player names must be in quotes. Try: Player p = \"Kobe Bryant\""
- **Database Errors**: Graceful handling with informative messages
- **Interactive Help**: Built-in help system with example queries
