package com.alipay.sofa.jraft.example.counter;

import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.option.NodeOptions;

public class CounterServiceBootstrap {
    public static void main(String[] args)throws Exception {

        final String dataPath = "/tmp/raft/";
        final String groupId = "counter";
        final String serverNode = "127.0.0.1:";
        final String serverConf = "127.0.0.1:8001,127.0.0.1:8002,127.0.0.1:8003";

        final NodeOptions nodeOptions = new NodeOptions();
        nodeOptions.setElectionTimeoutMs(1000);
        nodeOptions.setDisableCli(false);
        nodeOptions.setSnapshotIntervalSecs(30);

        for (int i = 8001; i < 8004; i++) {

            final PeerId serverId = new PeerId();

            boolean isParseServerNodeStatus = serverId.parse(serverNode+i);

            final Configuration configuration =  new Configuration();

            boolean isParseConfigurationStatus = configuration.parse(serverConf);

            nodeOptions.setInitialConf(configuration);

            final CounterServer counterServer = new CounterServer(dataPath+i, groupId, serverId, nodeOptions);
            System.out.println("Started counter server at port:"
                    + counterServer.getNode().getNodeId().getPeerId().getPort());
        }
    }
}
