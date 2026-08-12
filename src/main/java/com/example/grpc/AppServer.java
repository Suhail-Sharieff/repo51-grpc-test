package com.example.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * AppServer starts a gRPC server that listens on port 50051 and handles incoming calls
 * by binding the GreeterImpl service handler.
 */
public class AppServer {
    // The gRPC server instance
    private Server server;

    /**
     * Start the gRPC server.
     */
    private void start() throws IOException {
        int port = 50051;
        // Use ServerBuilder to configure the listening port and mount your service implementation
        server = ServerBuilder.forPort(port)
                .addService(new GreeterImpl())
                .build()
                .start();
        
        System.out.println("Server started, listening on " + port);

        // Add a JVM Shutdown Hook to safely release network ports when the program is killed
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            try {
                AppServer.this.stop();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
            System.err.println("*** server shut down");
        }));
    }

    /**
     * Terminate the gRPC server and release system resources safely.
     */
    private void stop() throws InterruptedException {
        if (server != null) {
            // Initiate graceful shutdown and wait up to 30 seconds for pending calls to finish
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Block the main execution thread so the JVM doesn't terminate immediately.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * The main method that bootstraps and starts the gRPC server.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        final AppServer server = new AppServer();
        server.start();
        server.blockUntilShutdown();
    }

    /**
     * Service implementation class.
     * We extend the auto-generated base class 'GreeterGrpc.GreeterImplBase' and override
     * the methods declared in main.proto.
     */
    static class GreeterImpl extends GreeterGrpc.GreeterImplBase {
        
        /**
         * Implements the SayHello RPC method.
         * 
         * @param req The request object containing the string payload
         * @param responseObserver Observer object used to write responses back to the client
         */
        @Override
        public void sayHello(MyRequest req, StreamObserver<MyResponse> responseObserver) {
            // 1. Read input parameters from the request payload
            String inputName = req.getName();
            System.out.println("Received request: " + inputName);

            // 2. Build the protobuf response message
            MyResponse reply = MyResponse.newBuilder()
                    .setRes("Hello, " + inputName + "! Welcome to gRPC practice!")
                    .build();

            // 3. Send the response message back to the client
            responseObserver.onNext(reply);

            // 4. Signal that the operation is finished and close the transmission channel
            responseObserver.onCompleted();
        }
    }
}
