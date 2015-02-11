Rss filter
==========
A command-line tool to download/filter/export RSS/ATOM feeds.

## Overview
The author uses the the default configuration of the rss-filter (see `config.java-backend.yml`, the file is self-explaining) to search for remote programming jobs (in the Java backend area).

However, the tool can be used for anything else. The rss-filter can download RSS/ATOM feeds and filter them based on fulltext search queries (implemented with Lucene).

This means: in case the information you need exists in form of RSS/ATOM feeds - you can use the rss-filter as an RSS aggregator with your own set of (very flexible) filters.
The tool ouptuts the *.atom files - you can browse them in your favorite RSS reader (author uses Liferea).

## Prerequisites
* Java 1.7 or higher
* Maven 3 or higher
* Terminal

To check the prerequisites:

```
$ mvn --version
Apache Maven 3.0.4

$ java -version
java version "1.7.0_07
```

 
## Build
In the terminal run: `  mvn package  `

## Use
```
 java -jar target/rss-filter-1.0-SNAPSHOT.jar [-c <file>] [-h] [-q] [-v] <command> [command2] [command3] ... [commandN]

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
```
