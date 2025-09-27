# HoopsQL Distribution Guide

## Quick Start for End Users

### 1. Prerequisites
- Java 11 or higher installed
- Test with: `java -version`

### 2. Download Files Needed
- `hoopsql-0.1.0-SNAPSHOT.jar` (14MB) - The main application
- `SQLite/hoopsql.db` (76MB) - The NBA statistics database

### 3. Platform Setup

#### Windows (PowerShell)
```powershell
# Create directory and copy files
mkdir C:\HoopsQL
# Copy hoopsql-0.1.0-SNAPSHOT.jar and SQLite\hoopsql.db to C:\HoopsQL\

# Test it works
cd C:\HoopsQL
java -jar hoopsql-0.1.0-SNAPSHOT.jar "Player p = `"Kobe Bryant`" get games where p.points >= 40"

# Add permanent function to PowerShell profile (optional)
if (!(Test-Path $PROFILE)) { New-Item -Path $PROFILE -Type File -Force }
Add-Content $PROFILE 'function hoopsql { java -jar "C:\HoopsQL\hoopsql-0.1.0-SNAPSHOT.jar" $args }'
. $PROFILE

# Now you can use: hoopsql "query here"
```

#### Linux/macOS
```bash
# Create directory and copy files  
mkdir ~/HoopsQL
# Copy hoopsql-0.1.0-SNAPSHOT.jar and SQLite/hoopsql.db to ~/HoopsQL/

# Test it works
cd ~/HoopsQL
java -jar hoopsql-0.1.0-SNAPSHOT.jar "Player p = \"Kobe Bryant\" get games where p.points >= 40"

# Create launcher script (optional)
cat > ~/HoopsQL/hoopsql << 'EOF'
#!/bin/bash
java -jar "$HOME/HoopsQL/hoopsql-0.1.0-SNAPSHOT.jar" "$@"
EOF
chmod +x ~/HoopsQL/hoopsql

# Add to PATH (choose one method)
echo 'export PATH="$HOME/HoopsQL:$PATH"' >> ~/.bashrc && source ~/.bashrc
# OR
sudo ln -s ~/HoopsQL/hoopsql /usr/local/bin/hoopsql

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
HoopsQL/
├── hoopsql-0.1.0-SNAPSHOT.jar    # Main application (14MB)
├── SQLite/
│   └── hoopsql.db                # NBA database (76MB)
└── hoopsql                       # Launcher script (Linux/Mac only)
```

### 6. Expected Output
```
Game Results
============
🏀 2016-04-13 | Kobe Bryant (Lakers vs Jazz) | W | 60.0 pts, 4.0 reb, 4.0 ast
🏀 2009-02-02 | Kobe Bryant (Lakers vs Knicks) | W | 61.0 pts, 7.0 reb, 1.0 ast
🏀 2008-03-28 | Kobe Bryant (Lakers vs Grizzlies) | L | 53.0 pts, 10.0 reb, 8.0 ast
...
```

### 7. Troubleshooting
- **"Java not found"**: Install Java 11+ and ensure it's in PATH
- **"Database not found"**: Ensure SQLite/hoopsql.db is in correct location relative to JAR
- **"Permission denied"**: Run `chmod +x hoopsql` on Linux/Mac
- **PowerShell policy error**: Run `Set-ExecutionPolicy RemoteSigned -Scope CurrentUser`

## Distribution Checklist for Developers

When creating a distribution package:

1. ✅ Build fat JAR: `mvn clean package` 
2. ✅ Include `target/hoopsql-0.1.0-SNAPSHOT.jar` (14MB with dependencies)
3. ✅ Include `SQLite/hoopsql.db` (76MB database)
4. ✅ Include this distribution guide
5. ✅ Test on clean system without Maven/source
6. ✅ Verify both Windows PowerShell and Linux/Mac work
7. ✅ Check interactive mode and file execution

Total distribution size: ~90MB (14MB + 76MB)
