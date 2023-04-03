package ma.enset.service;
import java.util.HashMap;
import java.util.Map;
import io.grpc.stub.StreamObserver;
import ma.enset.stubs.Chat;
import ma.enset.stubs.ChatServiceGrpc;

public class ChatServiceImpl extends ChatServiceGrpc.ChatServiceImplBase {

    private static int lastClientId = 0; // Variable pour stocker le dernier ID généré
    private Map<Integer, StreamObserver<Chat.ChatResponse>> clients = new HashMap<>();

    @Override
    public StreamObserver<Chat.ChatRequest> chat(StreamObserver<Chat.ChatResponse> responseObserver) {

        // Ajouter le client à la liste des clients avec un ID unique

        int clientId = ++lastClientId;// Générer un nouveau ID incrémentiel

        clients.put(clientId, responseObserver);
//        System.out.println(clientId);
        Chat.ChatResponse welcomeMessage = Chat.ChatResponse.newBuilder()
                .setUser("Server")
                .setContent("Bienvenue sur le chat ! Client n° : " + clientId)
                .build();

        // Envoyer le message de bienvenue au client
        responseObserver.onNext(welcomeMessage);

        return new StreamObserver<Chat.ChatRequest>() {
            @Override
            public void onNext(Chat.ChatRequest chatRequest) {
                String message = chatRequest.getContent();
                String sender = chatRequest.getUser();

                // Construire la réponse de chat à envoyer à tous les clients
                Chat.ChatResponse response = Chat.ChatResponse.newBuilder()
                        .setUser(sender)
                        .setContent(message)
                        .build();
                if(message.contains("=>")){
                    String[] arrOfStr = message.split("=>");
                    int id = Integer.parseInt(arrOfStr[0]);
                    String msg = arrOfStr[1];
                    sendMessageToClient(msg, id,sender);
                } else {
                    broadcast(response, clientId);
                }

                // Envoyer la réponse de chat à tous les clients sauf l'expéditeur

                System.out.println(response+""+clientId);
            }

            @Override
            public void onError(Throwable throwable) {
                clients.remove(clientId);
                System.out.println(throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                clients.remove(clientId);
                responseObserver.onCompleted();
            }
        };
    }

    /**
     * Envoie la réponse de chat à tous les clients connectés.
     */
    private void broadcast(Chat.ChatResponse response, int senderId) {
        for (Map.Entry<Integer, StreamObserver<Chat.ChatResponse>> entry : clients.entrySet()) {
            int clientId = entry.getKey();
            StreamObserver<Chat.ChatResponse> observer = entry.getValue();
            if (clientId != senderId) {
                observer.onNext(response);
            }
        }
    }

    /**
     * Envoie la réponse de chat à un client spécifique.
     */
    private void sendMessageToClient(String message, int clientId, String sender) {
        for (Map.Entry<Integer, StreamObserver<Chat.ChatResponse>> entry : clients.entrySet()) {
            int id = entry.getKey();
            StreamObserver<Chat.ChatResponse> observer = entry.getValue();
            if (id == clientId) {
                Chat.ChatResponse response = Chat.ChatResponse.newBuilder()
                        .setUser(sender)
                        .setContent(message)
                        .build();
                observer.onNext(response);
            }
        }
    }
}
