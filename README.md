# storm-monitor
Tool for monitoring topologies in storm

# How to build
cd storm-monitor && mvn clean install -DskipTests

# Configuration
check sample configuration at storm-monitor/sample-monitor-conf.json. You can configure multiple
storm clusters. In each cluster, multiple topologies can be monitored.

# How to run
java -cp storm-monitor-1.0.0.jar com.inmobi.storm.monitor.MonitorApp sample-monitor-conf.json
