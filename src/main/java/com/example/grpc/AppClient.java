package com.example.grpc;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.TimeUnit;

/**
 * AppClient establishes a connection to a running gRPC server and calls the Greeter service.
 */
public class AppClient {
    // The blocking stub allows synchronous RPC calls (makes remote calls behave like local function calls)
    private final GreeterGrpc.GreeterBlockingStub blockingStub;

    /**
     * Constructor setup that initializes the RPC stub on top of a transport channel.
     * Using the generic Channel interface allows passing mock channels during unit testing.
     */
    public AppClient(Channel channel) {
        blockingStub = GreeterGrpc.newBlockingStub(channel);
    }

    /**
     * Send a greeting request to the server.
     */
    public void greet(String name) {
        System.out.println("Will try to greet " + name + " ...");

        // 1. Build the protobuf request message payload
        MyRequest request = MyRequest.newBuilder().setName(name).build();
        MyResponse response;

        try {
            // 2. Execute the remote call using the blocking stub
            response = blockingStub.sayHello(request);
        } catch (StatusRuntimeException e) {
            // Log gRPC-specific connection/execution error codes
            System.err.println("RPC failed: " + e.getStatus());
            return;
        }

        // 3. Print the response field content
        System.out.println("Greeting response: " + response.getRes());
    }

    /**
     * Core client bootstrap method.
     */
    public static void main(String[] args) throws Exception {
        String user = "Learner";
        String target = "localhost:50051";

        // Create a communication channel to the server. 
        // Channels are thread-safe, reusable, and resource-heavy. Establish once at startup.
        // We usePlaintext() here because SSL/TLS is disabled (fine for development, do not use in production!).
        ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
            .usePlaintext()
            .build();
        
        try {
            // Initialize client and run the greeting logic
            AppClient client = new AppClient(channel);
            client.greet(user);
        } finally {
            // ManagedChannels consume threads and TCP sockets. 
            // Shut them down when the application terminates to prevent resource leaks.
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
