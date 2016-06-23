# Kafka Connect Source Connector for Slack

This program is a Kafka Source Connector for inserting Slack messages into a Kafka topic.

This connector is a Slack bot, so it will need to be running *and* invited to the channels of which you want to get the messages.

## Build and run

```
$ sbt assembly
$ export CLASSPATH=~/path/to/kafka-connect-slack-assembly-1.0.jar
$ bin/connect-standalone.sh config/connect-standalone.properties config/connect-slack-source.properties
```

## Configuration

A configuration sample file is can be found at the root of the project. In a nutshell, all you have to do is set the API key for your Slack bot and the destination Kafka topic.

```
name=connect-slack-spurce
connector.class=SlackSourceConnector
kafka.topic=slack-input
slack.apikey=CHANGEME
```

## Output

The messages produced into the destination Kafka topic will have the following (pretty-printed) form:

```
{
  "schema": {
    "type": "struct",
    "fields": [
      {"type": "string","optional": false,"field": "user"},
      {"type": "string","optional": false,"field": "channel"},
      {"type": "string","optional": false,"field": "text"}
    ],
    "optional": false,
    "name": "com.slack.message"
  },
  "payload": {
    "user": "U02PGHJRE",
    "channel": "D1JPAL0PN",
    "text": "Hello, Kafka Connect!"
  }
}
```

The channel name and username are not provided by Slack's RTM API (only their IDs), and I do not consider it to be the Source Connector's job to transform them.
