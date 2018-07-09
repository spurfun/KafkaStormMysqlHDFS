package bolt;

import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

public class WordNormalizserBolt extends BaseBasicBolt {
    public void execute(Tuple input, BasicOutputCollector collector) {
        //tuple的格式：[topic, partition, offset, key, value]
        String sentence = input.getString(4);
        String words[] = sentence.split(" ");

        for (String word : words){
            collector.emit(new Values(word));
        }
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("word"));
    }
}
