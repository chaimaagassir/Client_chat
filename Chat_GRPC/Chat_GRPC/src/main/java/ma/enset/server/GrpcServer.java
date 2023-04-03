package ma.enset.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import ma.enset.service.ChatServiceImpl;

import java.io.IOException;

public class GrpcServer {
    public static void main(String[] args) throws Exception {
        Server server= ServerBuilder.forPort(600)
                .addService(new ChatServiceImpl())
                .build();
        server.start();
        System.out.println("le serveur est démarreé........");
        server.awaitTermination();

    }
}

