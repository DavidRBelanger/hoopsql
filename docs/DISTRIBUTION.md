# HoopsQL Distribution Guide

## Quick Start for End Users

### 1. Prerequisites
- Java 11 or higher installed
- Test with: `java -version`

### 2. Download Files Needed
- `hoopsql-1.0.jar` (14MB) - The main application (included in repository)
- `SQLite/hoopsql.db` (76MB) - The NBA statistics database

**Download database:** [hoopsql.db](https://drive.google.com/uc?export=download&id=1fjq-9XWXE1uUFCQr0eRXkaTzS8HpbuaT)

### 3. Platform Setup

#### Windows (PowerShell)
```powershell
# Clone repository
git clone https://github.com/DavidRBelanger/hoopsql.git
cd hoopsql

# Download database
Invoke-WebRequest -Uri "https://drive.google.com/uc?export=download&id=1fjq-9XWXE1uUFCQr0eRXkaTzS8HpbuaT" -OutFile "SQLite\hoopsql.db"

# Test it works
java -jar hoopsql-1.0.jar "Player p = `"Kobe Bryant`" get games where p.points >= 40"

# Add permanent function to PowerShell profile (optional)
if (!(Test-Path $PROFILE)) { New-Item -Path $PROFILE -Type File -Force }
Add-Content $PROFILE 'function hoopsql { java -jar "$PWD\hoopsql-1.0.jar" $args }'
. $PROFILE

# Now you can use: hoopsql "query here"
```

#### Linux/macOS
```bash
# Clone repository
git clone https://github.com/DavidRBelanger/hoopsql.git
cd hoopsql

# Download database
wget -O SQLite/hoopsql.db "https://drive.google.com/uc?export=download&id=1fjq-9XWXE1uUFCQr0eRXkaTzS8HpbuaT"

# Test it works
chmod +x hoopsql
./hoopsql "Player p = \"Kobe Bryant\" get games where p.points >= 40"

# Add to PATH (optional)
echo 'export PATH="$(pwd):$PATH"' >> ~/.bashrc && source ~/.bashrc
# OR
sudo ln -s $(pwd)/hoopsql /usr/local/bin/hoopsql

# Now you can use: hoopsql "query here"
```

### 4. Usage Examples

```bash
# Interactive mode
hoopsql

# Single queries
hoopsql "Player p = \"Stephen Curry\" get games where p.points >= 30"
hoopsql "Player p = \"Kobe Bryant\" Player q = \"Shaquille O'Neal\" get games where p.points >= 30 and q.points >= 20"

# From file
echo 'Player p = "LeBron James" get games where p.points >= 35' > query.hpsql
hoopsql query.hpsql
```

### 5. File Structure
```
hoopsql/
├── hoopsql-1.0.jar               # Main application (14MB)
├── SQLite/
│   └── hoopsql.db                # NBA database (76MB) - Download separately
├── hoopsql                       # Launcher script (Linux/Mac)
└── docs/                         # Documentation
```

### 6. Expected Output
```
Results:
========
(26 games)

1. 2016-04-13 22:30:00 - Kobe Bryant (Lakers vs Jazz) - W
   Stats: 60 pts, 4 reb, 4 ast

2. 2009-02-02 19:30:00 - Kobe Bryant (Lakers vs Knicks) - W
   Stats: 61 pts, 0 reb, 3 ast

3. 2008-03-28 22:30:00 - Kobe Bryant (Lakers vs Grizzlies) - L
   Stats: 53 pts, 10 reb, 1 ast
...
```

### 7. Troubleshooting
- **"Java not found"**: Install Java 11+ and ensure it's in PATH
- **"Database not found"**: Ensure SQLite/hoopsql.db is in correct location relative to JAR
- **"Permission denied"**: Run `chmod +x hoopsql` on Linux/Mac
- **PowerShell policy error**: Run `Set-ExecutionPolicy RemoteSigned -Scope CurrentUser`

## Distribution Checklist for Developers

When creating a distribution package:

1. Build fat JAR: `mvn clean package` 
2. Include `target/hoopsql-1.0.jar` (14MB with dependencies)
3. Host `SQLite/hoopsql.db` (76MB database) separately on Google Drive
4. Include this distribution guide with download links
5. Test on clean system without Maven/source
6. Verify both Windows PowerShell and Linux/Mac work
7. Check interactive mode and file execution

Repository size: ~14MB (JAR only, database downloaded separately)
