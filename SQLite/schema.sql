-- Games
CREATE TABLE games (
    gameId INTEGER PRIMARY KEY,
    gameDate TEXT,
    gameDuration TEXT,
    hometeamId INTEGER,
    awayteamId INTEGER,
    homeScore INTEGER,
    awayScore INTEGER,
    winner INTEGER,
    arenaId INTEGER,
    attendance INTEGER,
    gameType TEXT,
    gameLabel TEXT,
    seriesGameNumber INTEGER,
    gameSubLabel TEXT
);

-- Players
CREATE TABLE players (
    personId INTEGER PRIMARY KEY,
    firstName TEXT,
    lastName TEXT,
    birthdate TEXT,
    lastAttended TEXT,
    country TEXT,
    height INTEGER,
    bodyWeight INTEGER,
    guard INTEGER,
    forward INTEGER,
    center INTEGER,
    draftYear INTEGER,
    draftRound INTEGER,
    draftNumber INTEGER
);

-- Player statistics
CREATE TABLE player_statistics (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    personId INTEGER NOT NULL,
    gameId INTEGER NOT NULL,
    teamId INTEGER,
    points INTEGER,
    assists INTEGER,
    reboundsTotal INTEGER,
    reboundsDefensive INTEGER,
    reboundsOffensive INTEGER,
    blocks INTEGER,
    steals INTEGER,
    turnovers INTEGER,
    foulsPersonal INTEGER,
    plusMinusPoints INTEGER,
    fieldGoalsAttempted INTEGER,
    fieldGoalsMade INTEGER,
    fieldGoalsPercentage REAL,
    threePointersAttempted INTEGER,
    threePointersMade INTEGER,
    threePointersPercentage REAL,
    freeThrowsAttempted INTEGER,
    freeThrowsMade INTEGER,
    freeThrowsPercentage REAL,
    numMinutes REAL,
    FOREIGN KEY (personId) REFERENCES players(personId),
    FOREIGN KEY (gameId) REFERENCES games(gameId)
);

-- Team histories
CREATE TABLE team_histories (
    teamId INTEGER PRIMARY KEY,
    teamCity TEXT,
    teamName TEXT,
    teamAbbrev TEXT,
    seasonFounded INTEGER,
    seasonActiveTill INTEGER,
    league TEXT
);

-- Team statistics
CREATE TABLE team_statistics (
    teamId INTEGER,
    gameId INTEGER,
    home INTEGER,
    win INTEGER,
    coachId INTEGER,
    points INTEGER,
    assists INTEGER,
    reboundsTotal INTEGER,
    blocks INTEGER,
    steals INTEGER,
    turnovers INTEGER,
    foulsPersonal INTEGER,
    plusMinusPoints INTEGER,
    fieldGoalsAttempted INTEGER,
    fieldGoalsMade INTEGER,
    fieldGoalsPercentage REAL,
    threePointersAttempted INTEGER,
    threePointersMade INTEGER,
    threePointersPercentage REAL,
    freeThrowsAttempted INTEGER,
    freeThrowsMade INTEGER,
    freeThrowsPercentage REAL,
    numMinutes REAL,
    q1Points INTEGER,
    q2Points INTEGER,
    q3Points INTEGER,
    q4Points INTEGER,
    benchPoints INTEGER,
    biggestLead INTEGER,
    biggestScoringRun INTEGER,
    leadChanges INTEGER,
    pointsFastBreak INTEGER,
    pointsFromTurnovers INTEGER,
    pointsInThePaint INTEGER,
    pointsSecondChance INTEGER,
    timesTied INTEGER,
    timeoutsRemaining INTEGER,
    seasonWins INTEGER,
    seasonLosses INTEGER,
    PRIMARY KEY (teamId, gameId),
    FOREIGN KEY (gameId) REFERENCES games(gameId)
);
