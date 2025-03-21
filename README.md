# EasyStats

A comprehensive statistics plugin for Minecraft servers that tracks platform usage, country statistics, revenue, campaigns, and session times.

## Features

- Platform Statistics (Java/Bedrock player tracking)
- Country-based Statistics with tier system
- Revenue Tracking
- Campaign Management
- Session Time Tracking
- MySQL and SQLite support
- PlaceholderAPI Integration
- Developer API

## Dependencies

### Required
- Spigot/Paper 1.20+

### Optional
- PlaceholderAPI - For using placeholders
- Floodgate - For accurate Bedrock player detection

## Commands

### Platform Statistics
```
/easystats platform view <platform> [timeframe]
Permission: easystats.platform
Example: /easystats platform view lobby 7d
Output:
=== Platform Statistics for 'lobby' ===
Total Players: 1,234
Java Players: 856 (69.4%)
Bedrock Players: 378 (30.6%)
Timeframe: Last 7 days

/easystats platform compare <platform1> <platform2>
Permission: easystats.platform
Example: /easystats platform compare lobby survival
Output:
=== Platform Comparison ===
Lobby: 1,234 players
Survival: 987 players
Difference: +247 players (25.0% more)
```

### Country Statistics
```
/easystats countries view <platform> [tier] [timeframe]
Permission: easystats.countries
Example: /easystats countries view lobby tier1
Output:
=== Country Statistics (Tier 1) ===
United States: 450 players (Java: 312, Bedrock: 138)
United Kingdom: 235 players (Java: 180, Bedrock: 55)
Germany: 189 players (Java: 145, Bedrock: 44)

/easystats countries compare <platform1> <platform2> [tier]
Permission: easystats.countries
Example: /easystats countries compare lobby survival tier1
Output:
=== Country Comparison (Tier 1) ===
Lobby vs Survival
United States: 450 vs 380 (+70)
United Kingdom: 235 vs 210 (+25)
Germany: 189 vs 165 (+24)
```

### Revenue Tracking
```
/easystats revenue view <platform> [timeframe]
Permission: easystats.revenue
Example: /easystats revenue view lobby 30d
Output:
=== Revenue Statistics ===
Total Revenue (30 days):
USD: $5,234.50
EUR: €4,123.75
GBP: £3,456.20

/easystats revenue add <platform> <amount> <currency>
Permission: easystats.revenue
Example: /easystats revenue add lobby 99.99 USD
Output: Successfully added $99.99 to lobby revenue.
```

### Campaign Management
```
/easystats campaign create <name> <description> <start_date> <end_date> <currency> <cost>
Permission: easystats.campaign
Example: /easystats campaign create summer23 "Summer Sale 2023" 2023-06-01 2023-08-31 USD 5000.00
Output: Campaign 'summer23' created successfully!

/easystats campaign view <name>
Permission: easystats.campaign
Example: /easystats campaign view summer23
Output:
=== Campaign Details ===
Description: Summer Sale 2023
Period: Jun 1, 2023 - Aug 31, 2023
Currency: USD
Cost: $5,000.00
Total Revenue: $15,234.50
ROI: 204.69% (Profit: $10,234.50)
Status: Active
Hostnames:
  - play.server.com
  - hub.server.com

/easystats campaign list
Permission: easystats.campaign
Example: /easystats campaign list
Output:
=== Active Campaigns ===
- summer23 (USD: $15,234.50, ROI: 204.69%)
- halloween23 (EUR: €5,678.90, ROI: 156.32%)
- winter23 (USD: $8,901.23, ROI: 178.02%)

/easystats campaign addhostname <name> <hostname>
Permission: easystats.campaign
Example: /easystats campaign addhostname summer23 play.server.com
Output: Added hostname 'play.server.com' to campaign 'summer23'

/easystats campaign removehostname <name> <hostname>
Permission: easystats.campaign
Example: /easystats campaign removehostname summer23 play.server.com
Output: Removed hostname 'play.server.com' from campaign 'summer23'

/easystats campaign listhostnames <name>
Permission: easystats.campaign
Example: /easystats campaign listhostnames summer23
Output:
=== Campaign Hostnames ===
- play.server.com
- hub.server.com

/easystats campaign end <name>
Permission: easystats.campaign
Example: /easystats campaign end summer23
Output: Campaign 'summer23' ended successfully!
```

### Session Statistics
```
/easystats session view <hostname>
Permission: easystats.session
Example: /easystats session view play.server.com
Output:
=== Session Statistics ===
Average Session Time: 2h 34m
Total Sessions: 1,234
Active Players: 45

/easystats session compare <hostname1> <hostname2>
Permission: easystats.session
Example: /easystats session compare play.server.com hub.server.com
Output:
=== Session Comparison ===
play.server.com: 2h 34m
hub.server.com: 45m
Difference: +1h 49m (243% longer)
```

### Configuration
```
/easystats reload
Permission: easystats.reload
Example: /easystats reload
Output: Configuration reloaded successfully!
```

### Export Commands
```
/easystats export <name> <timeframe> - Export all statistics data to a JSON file
```

**Permission:** `easystats.export` (default: op)

**Parameters:**
- `name`: The name of the export file (will be saved as `name.json`)
- `timeframe`: Time period to export data for (e.g., 7d, 30d, 90d, 180d, 365d)

**Example:**
```
/easystats export monthly_report 30d
```

This will create a file named `monthly_report.json` in the `plugins/EasyStats/exports` directory containing statistics for the last 30 days.

**Sample Export Data:**
```json
{
    "export_date": "2024-03-20T15:30:00",
    "timeframe": "30d",
    "timeframe_days": "30",
    "platform_stats": {
        "play.example.com": {
            "total": 1500,
            "java": 1000,
            "bedrock": 500
        },
        "mc.example.com": {
            "total": 2000,
            "java": 1500,
            "bedrock": 500
        }
    },
    "revenue_stats": {
        "play.example.com": {
            "USD": 1500.00,
            "EUR": 1200.00
        },
        "mc.example.com": {
            "USD": 2000.00,
            "EUR": 1600.00
        }
    },
    "session_stats": {
        "play.example.com": {
            "average_time_seconds": 3600,
            "average_time_formatted": "1h 0m 0s"
        },
        "mc.example.com": {
            "average_time_seconds": 5400,
            "average_time_formatted": "1h 30m 0s"
        }
    },
    "campaigns": [
        {
            "name": "summer_2024",
            "description": "Summer promotion campaign",
            "start_date": "2024-06-01",
            "end_date": "2024-08-31",
            "currency": "USD",
            "cost": 5000.00,
            "total_revenue": 7500.00,
            "profit": 2500.00,
            "roi": 50.00,
            "status": "active",
            "hostnames": ["play.example.com", "mc.example.com"],
            "join_stats": {
                "total": 3500,
                "java": 2500,
                "bedrock": 1000
            }
        }
    ]
}
```

The export file includes:
- Export metadata (date, timeframe)
- Platform statistics for the specified timeframe (total, Java, and Bedrock joins)
- Revenue statistics by platform and currency for the specified timeframe
- Session statistics (average session time in seconds and formatted)
- Campaign information including:
  - Basic details (name, description, dates, currency)
  - Financial metrics (cost, revenue, profit, ROI)
  - Associated hostnames
  - Join statistics for the specified timeframe (total, Java, and Bedrock joins)

### Player Count Statistics

```
/easystats playercount view <platform> [-all]
Permission: easystats.playercount
Example: /easystats playercount view lobby
Output:
=== Player Count Statistics for 'lobby' ===
Current Players: 45

Average Players:
Last 24h: 38
Last 7d: 42
Last 14d: 40
Last 30d: 39

Peak Players: 156
Peak Time: 2024-03-15 18:30:00

Example: /easystats playercount view -all
Output:
=== Player Count Statistics for 'All Platforms' ===
Current Players: 123

Average Players:
Last 24h: 98
Last 7d: 105
Last 14d: 108
Last 30d: 102

Peak Players: 456
Peak Time: 2024-03-15 18:30:00
```

The player count command shows:
- Current number of players online
- Average player count over different time periods (24h, 7d, 14d, 30d)
- Peak player count and when it occurred

Use the `-all` option to view statistics across all platforms combined.

### Player Count Statistics
```java
// Get platform-specific player count stats
Map<String, Object> stats = api.getPlayerCountStats("lobby");
int current = (int) stats.get("current");
double avg24h = (double) stats.get("avg_24h");
double avg7d = (double) stats.get("avg_7d");
double avg14d = (double) stats.get("avg_14d");
double avg30d = (double) stats.get("avg_30d");
int peak = (int) stats.get("peak_count");
String peakTime = (String) stats.get("peak_time");

// Get global player count stats
Map<String, Object> globalStats = api.getGlobalPlayerCountStats();
int globalCurrent = (int) globalStats.get("current");
double globalAvg24h = (double) globalStats.get("avg_24h");
// ... etc
```

### Player Count Placeholders
```
%easystats_playercount_<platform>_current% - Current player count
Example: %easystats_playercount_lobby_current% → 45

%easystats_playercount_<platform>_24h% - 24-hour average
Example: %easystats_playercount_lobby_24h% → 38

%easystats_playercount_<platform>_7d% - 7-day average
Example: %easystats_playercount_lobby_7d% → 42

%easystats_playercount_<platform>_14d% - 14-day average
Example: %easystats_playercount_lobby_14d% → 40

%easystats_playercount_<platform>_30d% - 30-day average
Example: %easystats_playercount_lobby_30d% → 39

%easystats_playercount_<platform>_peak% - Peak player count
Example: %easystats_playercount_lobby_peak% → 156

%easystats_playercount_<platform>_peak_time% - Peak time
Example: %easystats_playercount_lobby_peak_time% → 2024-03-15 18:30:00

%easystats_globalplayercount_current% - Global current player count
Example: %easystats_globalplayercount_current% → 123

%easystats_globalplayercount_24h% - Global 24-hour average
Example: %easystats_globalplayercount_24h% → 98

%easystats_globalplayercount_7d% - Global 7-day average
Example: %easystats_globalplayercount_7d% → 105

%easystats_globalplayercount_14d% - Global 14-day average
Example: %easystats_globalplayercount_14d% → 108

%easystats_globalplayercount_30d% - Global 30-day average
Example: %easystats_globalplayercount_30d% → 102

%easystats_globalplayercount_peak% - Global peak player count
Example: %easystats_globalplayercount_peak% → 456

%easystats_globalplayercount_peak_time% - Global peak time
Example: %easystats_globalplayercount_peak_time% → 2024-03-15 18:30:00
```

### Player Count Command Usage Examples
```
# View player count for a specific platform
/easystats playercount view lobby
Output:
=== Player Count Statistics for 'lobby' ===
Current Players: 45

Average Players:
Last 24h: 38
Last 7d: 42
Last 14d: 40
Last 30d: 39

Peak Players: 156
Peak Time: 2024-03-15 18:30:00

# View global player count across all platforms
/easystats playercount view -all
Output:
=== Player Count Statistics for 'All Platforms' ===
Current Players: 123

Average Players:
Last 24h: 98
Last 7d: 105
Last 14d: 108
Last 30d: 102

Peak Players: 456
Peak Time: 2024-03-15 18:30:00

# Using with placeholders in a scoreboard
Current Players: %easystats_playercount_lobby_current%
24h Average: %easystats_playercount_lobby_24h%
Global Players: %easystats_globalplayercount_current%
```

## Permissions

```yaml
easystats.*:
  description: Gives access to all EasyStats commands
  children:
    easystats.platform: true
    easystats.countries: true
    easystats.revenue: true
    easystats.campaign: true
    easystats.session: true
    easystats.reload: true
    easystats.export: true

easystats.platform:
  description: Access to platform statistics
  default: op

easystats.countries:
  description: Access to country statistics
  default: op

easystats.revenue:
  description: Access to revenue tracking
  default: op

easystats.campaign:
  description: Access to campaign management
  default: op

easystats.session:
  description: Access to session statistics
  default: op

easystats.reload:
  description: Ability to reload configuration
  default: op

easystats.export:
  description: Access to export commands
  default: op
```

## PlaceholderAPI Integration

### Platform Statistics
```
%easystats_platform_<platform>_total% - Total players
Example: %easystats_platform_lobby_total% → 1,234

%easystats_platform_<platform>_java% - Java players
Example: %easystats_platform_lobby_java% → 856

%easystats_platform_<platform>_bedrock% - Bedrock players
Example: %easystats_platform_lobby_bedrock% → 378

%easystats_platform_<platform>_java_percent% - Java percentage
Example: %easystats_platform_lobby_java_percent% → 69.4%

%easystats_platform_<platform>_bedrock_percent% - Bedrock percentage
Example: %easystats_platform_lobby_bedrock_percent% → 30.6%
```

### Country Statistics
```
%easystats_country_<platform>_<tier>_total% - Total players in tier
Example: %easystats_country_lobby_tier1_total% → 874

%easystats_country_<platform>_<tier>_java% - Java players in tier
Example: %easystats_country_lobby_tier1_java% → 637

%easystats_country_<platform>_<tier>_bedrock% - Bedrock players in tier
Example: %easystats_country_lobby_tier1_bedrock% → 237
```

### Revenue Statistics
```
%easystats_revenue_<platform>_<currency>% - Total revenue in currency
Example: %easystats_revenue_lobby_USD% → $5,234.50
```

### Campaign Statistics
```
%easystats_campaign_<platform>_<name>% - Campaign revenue
Example: %easystats_campaign_lobby_summer23% → $15,234.50

%easystats_campaign_<platform>_<name>_cost% - Campaign cost
Example: %easystats_campaign_lobby_summer23_cost% → $5,000.00

%easystats_campaign_<platform>_<name>_roi% - Campaign ROI
Example: %easystats_campaign_lobby_summer23_roi% → 204.69%

%easystats_campaign_<platform>_<name>_profit% - Campaign profit
Example: %easystats_campaign_lobby_summer23_profit% → $10,234.50
```

### Session Statistics
```
%easystats_session_<platform>_avg% - Average session time
Example: %easystats_session_lobby_avg% → 2h 34m

%easystats_session_<platform>_total% - Total session time
Example: %easystats_session_lobby_total% → 3,456h 45m
```

## Developer API

### Getting Started
```java
public class YourPlugin extends JavaPlugin {
    private EasyStatsAPI api;

    @Override
    public void onEnable() {
        // Get EasyStats plugin instance
        Plugin plugin = Bukkit.getPluginManager().getPlugin("EasyStats");
        if (plugin instanceof EasyStats) {
            api = ((EasyStats) plugin).getAPI();
        }
    }
}
```

### Platform Statistics
```java
// Get platform stats
Map<String, Long> stats = api.getPlatformStats("lobby", "7d");
long total = stats.get("total");
long javaPlayers = stats.get("java");
long bedrockPlayers = stats.get("bedrock");

// Compare platforms
Map<String, Long> comparison = api.comparePlatforms("lobby", "survival");
long difference = comparison.get("difference");
long percentDifference = comparison.get("percent_difference");
```

### Country Statistics
```java
// Get country stats
Map<String, Map<String, Long>> stats = api.getCountryStats("lobby", "30d");
Map<String, Long> tier1Stats = stats.get("tier1");
long tier1Total = tier1Stats.get("total");

// Compare countries between platforms
Map<String, Map<String, Long>> comparison = api.compareCountries("lobby", "survival");
```

### Revenue Tracking
```java
// Get revenue stats
Map<String, Double> stats = api.getRevenueStats("lobby", "30d");
double usdRevenue = stats.get("USD");

// Add revenue
api.addRevenue("lobby", 99.99, "USD");

// Compare revenue
Map<String, Double> comparison = api.compareRevenue("lobby", "survival");
```

### Campaign Management
```java
// Create campaign
api.createCampaign("summer23", "Summer Sale 2023", "2023-06-01", "2023-08-31", "USD", 5000.00);

// Get campaign details
Map<String, Object> campaign = api.getCampaign("summer23");
String name = (String) campaign.get("name");
double revenue = (Double) campaign.get("total_revenue");
double cost = (Double) campaign.get("cost");
double roi = (Double) campaign.get("roi");
double profit = (Double) campaign.get("profit");

// Get all campaigns
List<Map<String, Object>> campaigns = api.getAllCampaigns();

// Manage campaign hostnames
api.addHostnameToCampaign("summer23", "play.server.com");
api.removeHostnameFromCampaign("summer23", "play.server.com");
List<String> hostnames = api.getCampaignHostnames("summer23");

// End campaign
api.endCampaign("summer23");
```

### Session Statistics
```java
// Get average session time
double avgTime = api.getAverageSessionTime("play.server.com");

// Compare session times
Map<String, Double> comparison = api.compareSessionTimes("play.server.com", "hub.server.com");
double difference = comparison.get("difference");
double percentDifference = comparison.get("percent_difference");

// Track sessions
UUID playerId = player.getUniqueId();
String hostname = "play.server.com";
api.startSession(playerId, hostname);
api.endSession(playerId, hostname);
```

### Utility Methods
```java
// Format duration
String formatted = api.formatDuration(150000); // "2m 30s"

// Format number
String formatted = api.formatNumber(1234.56); // "1,234.56"

// Format percentage
String formatted = api.formatPercentage(75.5); // "75.50%"
```

## Configuration

### config.yml
```yaml
# Data retention period in days (default: 180 days / 6 months)
retention_days: 180

# Auto-save interval in minutes (default: 120 minutes / 2 hours)
auto_save: 120

# Database Settings
database:
  # Type of database to use (mysql or sqlite)
  type: sqlite
  
  # MySQL Settings (ignored if using sqlite)
  mysql:
    host: localhost
    port: 3306
    database: easystats
    username: root
    password: password
    pool:
      maximum-pool-size: 10
      minimum-idle: 5
      idle-timeout: 300000
      max-lifetime: 600000
      connection-timeout: 5000

  # SQLite Settings (ignored if using mysql)
  sqlite:
    file: database.db
```