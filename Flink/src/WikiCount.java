import org.apache.flink.api.common.functions.FoldFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.wikiedits.WikipediaEditEvent;
import org.apache.flink.streaming.connectors.wikiedits.WikipediaEditsSource;

public class WikiCount {

    public static void main(String[] args) {
        StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<WikipediaEditEvent> edits = environment.addSource(new WikipediaEditsSource());
        KeyedStream<WikipediaEditEvent, String> keyEdits = edits.keyBy(new KeySelector<WikipediaEditEvent, String>() {

            @Override
            public String getKey(WikipediaEditEvent arg0) throws Exception {
                return arg0.getUser();
            }

        });
        DataStream<Tuple2<String, Long>> result = keyEdits.timeWindow(Time.seconds(5)).fold(new Tuple2<>("", 0l),
                new FoldFunction<WikipediaEditEvent, Tuple2<String, Long>>() {

                    @Override
                    public Tuple2<String, Long> fold(Tuple2<String, Long> arg0, WikipediaEditEvent arg1)
                            throws Exception {
                        arg0.f0 = arg1.getUser();
                        arg0.f1 += arg1.getByteDiff();

                        return arg0;
                    }
                });
        result.print();
        try {
            environment.execute();
        } catch (Exception e) {
            // TODO: sameepsinghania. Write exception handling code
        }

    }
}
