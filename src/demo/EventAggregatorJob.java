package demo;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.util.Collector;
import com.fasterxml.jackson.databind.ObjectMapper;
import  org.apache.flink.api.common.state.MapState;
import  org.apache.flink.api.common.state.MapStateDescriptor;
import java.util.*;
import org.apache.flink.configuration.Configuration;

public class EventAggregatorJob {

    public static void main(String[] args) throws Exception {
        final ParameterTool params = ParameterTool.fromArgs(args);

        final String kafkaBroker = params.getRequired("kafkaBroker");
        final String inputTopic = params.getRequired("inputTopic");
        final String outputTopic = params.getRequired("outputTopic");
        final String requiredKeysStr = params.getRequired("requiredKeys");

        // Convert requiredKeys to Set
        Set<String> requiredKeys = new HashSet<>(Arrays.asList(requiredKeysStr.split(",")));

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Kafka properties
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", kafkaBroker);
        properties.setProperty("group.id", "flink-event-aggregator");



        // Define Kafka consumer
        FlinkKafkaConsumer<String> consumer = new FlinkKafkaConsumer<>(inputTopic, new SimpleStringSchema(), properties);
        consumer.setStartFromEarliest();

        // demo.Schema definition

        Schema schema = new Schema(requiredKeys);

        // Create Kafka producer
        FlinkKafkaProducer<String> producer = new FlinkKafkaProducer<>(
                outputTopic,
                new SimpleStringSchema(),
                properties
        );

        // Define the data stream
        DataStream<String> stream = env.addSource(consumer);

        stream
                .map(value -> {
                    ObjectMapper mapper = new ObjectMapper();

                    return mapper.readValue(value, Event.class);
                })
                .keyBy(event -> event.key)
                .process(new EventAggregatorFunction(schema))
                .map(event -> {
                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.writeValueAsString(event);
                })
                .addSink(producer);

        env.execute("demo.Event Aggregator Job");
    }

    public static class EventAggregatorFunction extends org.apache.flink.streaming.api.functions.KeyedProcessFunction<String, Event, Event> {

        private final Schema schema;
        private transient MapState<String, String> aggregatedState;

        public EventAggregatorFunction(Schema schema) {
            this.schema = schema;
        }

        @Override
        public void open(Configuration parameters) {
            MapStateDescriptor<String, String> descriptor = new MapStateDescriptor<>(
                    "aggregatedState",
                    String.class,
                    String.class
            );
            aggregatedState = getRuntimeContext().getMapState(descriptor);
        }

        @Override
        public void processElement(Event event, Context context, Collector<Event> collector) throws Exception {
            for (Map.Entry<String, String> entry : event.attributes.entrySet()) {
                aggregatedState.put(entry.getKey(), entry.getValue());
            }

            aggregatedState.put("key", event.key);

            boolean isComplete = true;
            for (String key : schema.requiredKeys) {
                if (!aggregatedState.contains(key)) {
                    isComplete = false;
                    break;
                }
            }

            if (isComplete) {
                Map<String, String> completeEventMap = new HashMap<>();
                for (Map.Entry<String, String> entry : aggregatedState.entries()) {
                    completeEventMap.put(entry.getKey(), entry.getValue());
                }
                String key = completeEventMap.get("key");
                completeEventMap.remove("key");
                Event completeEvent = new Event(key, completeEventMap);
                collector.collect(completeEvent);
                aggregatedState.clear();
            }
        }
    }
}