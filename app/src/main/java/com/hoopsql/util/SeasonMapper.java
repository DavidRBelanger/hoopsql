package com.hoopsql.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps NBA season names (like "1996-97") to their corresponding date ranges
 */
public class SeasonMapper {
    private static final Map<String, SeasonDates> SEASON_MAP = new HashMap<>();
    
    static {
        // NBA seasons with their approximate start/end dates
        // Regular season typically runs October to April, playoffs through June
        SEASON_MAP.put("1995-96", new SeasonDates("1995-11-03", "1996-06-16"));
        SEASON_MAP.put("1996-97", new SeasonDates("1996-11-01", "1997-06-13"));
        SEASON_MAP.put("1997-98", new SeasonDates("1997-10-31", "1998-06-14"));
        SEASON_MAP.put("1998-99", new SeasonDates("1999-02-05", "1999-06-25")); // Lockout shortened
        SEASON_MAP.put("1999-00", new SeasonDates("1999-11-02", "2000-06-19"));
        SEASON_MAP.put("2000-01", new SeasonDates("2000-10-31", "2001-06-15"));
        SEASON_MAP.put("2001-02", new SeasonDates("2001-10-30", "2002-06-12"));
        SEASON_MAP.put("2002-03", new SeasonDates("2002-10-29", "2003-06-15"));
        SEASON_MAP.put("2003-04", new SeasonDates("2003-10-28", "2004-06-15"));
        SEASON_MAP.put("2004-05", new SeasonDates("2004-11-02", "2005-06-23"));
        SEASON_MAP.put("2005-06", new SeasonDates("2005-11-01", "2006-06-20"));
        SEASON_MAP.put("2006-07", new SeasonDates("2006-10-31", "2007-06-14"));
        SEASON_MAP.put("2007-08", new SeasonDates("2007-10-30", "2008-06-17"));
        SEASON_MAP.put("2008-09", new SeasonDates("2008-10-28", "2009-06-14"));
        SEASON_MAP.put("2009-10", new SeasonDates("2009-10-27", "2010-06-17"));
        SEASON_MAP.put("2010-11", new SeasonDates("2010-10-26", "2011-06-12"));
        SEASON_MAP.put("2011-12", new SeasonDates("2011-12-25", "2012-06-21")); // Lockout shortened
        SEASON_MAP.put("2012-13", new SeasonDates("2012-10-30", "2013-06-20"));
        SEASON_MAP.put("2013-14", new SeasonDates("2013-10-29", "2014-06-15"));
        SEASON_MAP.put("2014-15", new SeasonDates("2014-10-28", "2015-06-16"));
        SEASON_MAP.put("2015-16", new SeasonDates("2015-10-27", "2016-06-19"));
        SEASON_MAP.put("2016-17", new SeasonDates("2016-10-25", "2017-06-12"));
        SEASON_MAP.put("2017-18", new SeasonDates("2017-10-17", "2018-06-08"));
        SEASON_MAP.put("2018-19", new SeasonDates("2018-10-16", "2019-06-13"));
        SEASON_MAP.put("2019-20", new SeasonDates("2019-10-22", "2020-10-11")); // COVID extended
        SEASON_MAP.put("2020-21", new SeasonDates("2020-12-22", "2021-07-20")); // COVID delayed
        SEASON_MAP.put("2021-22", new SeasonDates("2021-10-19", "2022-06-16"));
        SEASON_MAP.put("2022-23", new SeasonDates("2022-10-18", "2023-06-12"));
        SEASON_MAP.put("2023-24", new SeasonDates("2023-10-17", "2024-06-17"));
        SEASON_MAP.put("2024-25", new SeasonDates("2024-10-22", "2025-06-22")); // Current/projected
    }
    
    public static class SeasonDates {
        private final String startDate;
        private final String endDate;
        
        public SeasonDates(String startDate, String endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }
        
        public String getStartDate() { return startDate; }
        public String getEndDate() { return endDate; }
    }
    
    public static SeasonDates getSeasonDates(String season) {
        return SEASON_MAP.get(season);
    }
    
    public static boolean isValidSeason(String season) {
        return SEASON_MAP.containsKey(season);
    }
}
