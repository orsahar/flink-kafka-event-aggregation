# Event Aggregator Job

This is a demo project that uses Apache Flink to consume events from a Kafka topic, aggregate them based on a set of required keys, and then produce the complete events to another Kafka topic.

## Table of Contents

1. [Introduction](#introduction)
2. [Requirements](#requirements)
3. [Setup](#setup)
4. [Usage](#usage)
5. [Components](#components)
6. [An Example](#an-example)
7. [Future Improvements](#future-improvements)
8. [License](#license)

## Introduction

The `EventAggregatorJob` class in this project demonstrates how to use Apache Flink to perform key-based event aggregation. The job:
1. Consumes events from a Kafka topic.
2. Aggregates these events based on a set of required keys.
3. Produces the complete events to another Kafka topic.

## Requirements

To run this project, you will need:

- Java 8 or later
- Apache Flink
- Apache Kafka
- `flink-streaming-java`, `flink-connector-kafka`, and `jackson-databind` dependencies.

## Setup

1. **Clone the repository:**
   ```bash
   git clone <repository_url>
   cd <repository_directory>
   ```

2. **Install dependencies:**
   Ensure you have the necessary dependencies in your `pom.xml` file.

3. **Kafka setup:**
    - Kafka broker should be up and running.
    - Create the necessary Kafka topics for input and output events.

## Usage

To execute the `EventAggregatorJob`, run the following command:

```bash
flink run -c demo.EventAggregatorJob <path_to_jar> 
    --kafkaBroker <kafka_broker> 
    --inputTopic <input_topic> 
    --outputTopic <output_topic> 
    --requiredKeys <comma_separated_required_keys>
```

- `<path_to_jar>`: Path to the JAR file containing the compiled job.
- `<kafka_broker>`: Address of the Kafka broker.
- `<input_topic>`: Topic name to consume events from.
- `<output_topic>`: Topic name to produce aggregated events to.
- `<comma_separated_required_keys>`: List of keys required for event aggregation, separated by commas.

## Components

### `EventAggregatorJob`
The main class that sets up the Flink execution environment, defines the Kafka consumer and producer, and sets up the data processing pipeline.

### `EventAggregatorFunction`
A custom Flink `KeyedProcessFunction` to aggregate events based on a set of required keys. Once all specified keys are present in the state, a complete event is produced.

### `Event`
A class representing an event structure with a key and a set of attributes.

### `Schema`
A class that defines the schema for required keys.

## An Example

Let's consider an example where you want to aggregate events with required keys 'name' and 'age':

1. Create input and output Kafka topics:

   ```bash
   kafka-topics --create --topic input-events --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
   kafka-topics --create --topic output-events --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
   ```

2. Submit the Flink job:

   ```bash
   flink run -c demo.EventAggregatorJob path_to_jar/EventAggregatorJob.jar --kafkaBroker localhost:9092 --inputTopic input-events --outputTopic output-events --requiredKeys name,age
   ```

3. Produce sample events to `input-events` topic and check the `output-events` topic for aggregated events.

## Future Improvements

- Support for schema evolution.
- Error handling and retries.
- Enhance monitoring and logging.
- Unit tests and integration tests.

## License

This project is licensed under the MIT License.


