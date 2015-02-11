Rss filter
==========
A command-line tool to download/filter/export RSS/ATOM feeds.

## Build
In the terminal run:
    mvn package

## Use
 usage: rss-filter [-c <file>] [-h] [-q] [-v] <command> [command2] [command3] ... [commandN]

 Commands:
 get		Iterates over all configured data sources and retrieves the data (usually the RSS feeds)
 tag		attaches the tags to the rss items (actually delegates this to "taggers")
 tagclear	removes all tags attached to rss items (usefull when debugging taggers)
 export		goes through all configured "exportFiles" and write files (usually these are *.atom files in the current dir)

 Options:
 -c <file>   read configuration from the *.yml file (default: config.yml )
 -h          print help
 -q          logging: be extra quiet
 -v          logging: be extra verbose

 Example:
 java -jar target/rss-filter-1.0-SNAPSHOT.jar -v -c config.java-backend.yml get tag export 

