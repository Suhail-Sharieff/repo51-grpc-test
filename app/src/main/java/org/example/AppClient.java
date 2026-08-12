package org.example;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.checkerframework.checker.units.qual.A;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class AppClient {
    private final GreetServiceGrpc.GreetServiceBlockingStub stub;

    public AppClient(ManagedChannel channel) {
        this.stub = GreetServiceGrpc.newBlockingStub(channel);
    }

    private void sendRequest(String toWhom){
        //1> build request
        MyRequest req=MyRequest.newBuilder().setName(toWhom).build();
        MyResponse res=stub.fulfil(req);
        System.out.println("Response received from server: "+res.getRes());
    }

    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel= ManagedChannelBuilder.forTarget("localhost:8080").usePlaintext().build();
        AppClient client=new AppClient(channel);
        try {
            client.sendRequest("suhail");
        } finally {
            channel.shutdownNow().awaitTermination(3000, TimeUnit.MILLISECONDS);

        }
    }
}
