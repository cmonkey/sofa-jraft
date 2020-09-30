package com.alipay.sofa.jraft.example.counter;

import com.alipay.sofa.jraft.RouteTable;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.error.RemotingException;
import com.alipay.sofa.jraft.example.counter.rpc.IncrementAndGetRequest;
import com.alipay.sofa.jraft.option.CliOptions;
import com.alipay.sofa.jraft.rpc.InvokeCallback;
import com.alipay.sofa.jraft.rpc.impl.cli.CliClientServiceImpl;

import java.util.concurrent.*;

public class CounterClientBootstrap {
    public static void main(String[] args) throws TimeoutException, InterruptedException, RemotingException {

        final String groupId = "counter";
        final String conf = "127.0.0.1:8001,127.0.0.1:8002,127.0.0.1:8003";

        final Configuration configuration = new Configuration();

        boolean isParserConfiguration = configuration.parse(conf);

        RouteTable.getInstance().updateConfiguration(groupId, configuration);

        final CliClientServiceImpl cliClientService = new CliClientServiceImpl();
        cliClientService.init(new CliOptions());

        boolean isLeader = RouteTable.getInstance().refreshLeader(cliClientService, groupId, 1000).isOk();

        final PeerId leader = RouteTable.getInstance().selectLeader(groupId);

        final int n = 1000;

        final CountDownLatch latch = new CountDownLatch(n);

        final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        for(int i = 0; i < n; i++){
            incrementAndGet(cliClientService, leader, i, latch, executor);
        }
        latch.await();

    }

    private static void incrementAndGet(CliClientServiceImpl cliClientService, PeerId leader, int delta, CountDownLatch latch, Executor executor) throws RemotingException, InterruptedException {

        final IncrementAndGetRequest request = new IncrementAndGetRequest();
        request.setDelta(delta);

        cliClientService.getRpcClient().invokeAsync(leader.getEndpoint(), request, new InvokeCallback(){
            @Override
            public void complete(Object result, Throwable err ){
                if(null == err){
                    System.out.println("result = " + result);
                    latch.countDown();
                }else{
                    err.printStackTrace();
                    latch.countDown();
                }
            }

            @Override
            public Executor executor(){
                return executor;
            }
        }, 5000);
    }
}
