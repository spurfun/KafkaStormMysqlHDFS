package bolt;

import org.apache.storm.jdbc.common.Column;
import org.apache.storm.jdbc.common.ConnectionProvider;
import org.apache.storm.jdbc.common.HikariCPConnectionProvider;
import org.apache.storm.jdbc.common.JdbcClient;
import org.apache.storm.shade.com.google.common.collect.Maps;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordCountMysqlBolt implements IRichBolt {
    private OutputCollector collector;  //for emit
    private Map<String,Integer> counters; //for word count
    private JdbcClient jdbcClient;
    private ConnectionProvider connectionProvider;
    public void prepare(Map stormConf, TopologyContext context, OutputCollector outputCollector) {
        collector = outputCollector;
        counters = new HashMap<String, Integer>();

        Map hikariConfigMap = Maps.newHashMap();
        hikariConfigMap.put("dataSourceClassName","com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        hikariConfigMap.put("dataSource.url", "jdbc:mysql://dn5:3306/storm");
        hikariConfigMap.put("dataSource.user","root");
        hikariConfigMap.put("dataSource.password","Root@123");
        connectionProvider = new HikariCPConnectionProvider(hikariConfigMap);

        connectionProvider.prepare();
        jdbcClient = new JdbcClient(connectionProvider, 30);
    }

    public void execute(Tuple input) {
        String word = input.getString(0);

        if (!counters.containsKey(word)) {
            counters.put(word,1);
        } else {
            counters.put(word,counters.get(word)+1);
        }

        ArrayList<Column> list = new ArrayList();
        list.add(new Column("word",word,Types.VARCHAR));

        List<List<Column>> select = jdbcClient.select("select word from wordcount where word = ?", list);
        //计算出查询的条数
        Long n = select.stream().count();
        if (n >= 1) {
            //update
            jdbcClient.executeSql("update wordcount set word_count = " + counters.get(word) + " where word = '" + word + "'");

        } else {
            //insert
            jdbcClient.executeSql("insert into wordcount values( '" + word + "'," + counters.get(word) + ")");
        }
    }

    public void cleanup() {

    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("wordcountmysql"));
    }

    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
