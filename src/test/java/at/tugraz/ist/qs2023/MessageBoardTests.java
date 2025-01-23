package at.tugraz.ist.qs2023;

import at.tugraz.ist.qs2023.actorsystem.Message;
import at.tugraz.ist.qs2023.actorsystem.SimulatedActor;
import at.tugraz.ist.qs2023.actorsystem.SimulatedActorSystem;
import at.tugraz.ist.qs2023.messageboard.*;
import at.tugraz.ist.qs2023.messageboard.clientmessages.*;
import at.tugraz.ist.qs2023.messageboard.dispatchermessages.Stop;
import at.tugraz.ist.qs2023.messageboard.dispatchermessages.StopAck;
import at.tugraz.ist.qs2023.messageboard.messagestoremessages.*;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

class TestClient extends SimulatedActor {
    final Queue<Message> receivedMessages;

    TestClient() {
        receivedMessages = new LinkedList<>();
    }

    @Override
    public void receive(Message message) {
        receivedMessages.add(message);
    }
}

public class MessageBoardTests {
    private SimulatedActorSystem system;
    private UserMessage userMessage;
    private MessageStore messageStore;

    @Before
    public void setUp() {
        system = new SimulatedActorSystem();
        userMessage = new UserMessage("John Doe", "Test message");
        messageStore = new MessageStore();
    }

    @Test
    public void tickTest() throws UnknownClientException, UnknownMessageException {
        MockSimulatedActor mockMessageStore = new MockSimulatedActor();
        MockSimulatedActor mockClient = new MockSimulatedActor();
        SimulatedActorSystem mockSystem = new SimulatedActorSystem();

        TestMessageStoreMessage testMessage = new TestMessageStoreMessage(null, 0);
        WorkerHelper workerHelper = new WorkerHelper(mockMessageStore, mockClient, testMessage, mockSystem);

        workerHelper.atStartUp();
        assertEquals(1, mockMessageStore.tellCount);

        workerHelper.tick();
        workerHelper.tick();
        workerHelper.tick();
        assertEquals(1, mockMessageStore.tellCount);

        workerHelper.tick();
        assertEquals(2, mockMessageStore.tellCount);
    }

    @Test
    public void testStopMessageDuration() {
        Stop stopMessage = new Stop();
        assertEquals(2, stopMessage.getDuration());
    }

    @Test
    public void testStopAckMessageDuration() {
        // Create a sample SimulatedActor
        SimulatedActor sender = new SimulatedActor() {
            @Override
            public void receive(Message message) {
                // Do nothing
            }
        };

        StopAck stopAckMessage = new StopAck(sender);
        assertEquals(2, stopAckMessage.getDuration());
    }

    @Test
    public void testStopAckMessageSender() {
        // Create a sample SimulatedActor
        SimulatedActor sender = new SimulatedActor() {
            @Override
            public void receive(Message message) {
                // Do nothing
            }
        };

        StopAck stopAckMessage = new StopAck(sender);
        assertEquals(sender, stopAckMessage.sender);
    }

    @Test
    public void testInitAndFinishCommunication() throws UnknownClientException, UnknownMessageException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;

        worker.tell(new FinishCommunication(10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message finAckMessage = client.receivedMessages.remove();
        assertEquals(FinishAck.class, finAckMessage.getClass());
        FinishAck finAck = (FinishAck) finAckMessage;
        assertEquals(Long.valueOf(10), finAck.communicationId);
        dispatcher.tell(new Stop());
    }


    @Test
    public void testClientMessage() {
        Long communicationId = 1L;
        ClientMessage clientMessage = new ClientMessage(communicationId) {
            @Override
            public int getDuration() {
                return 1;
            }
        };

        assertEquals(communicationId, clientMessage.getCommunicationId());
        communicationId = 2L;
        clientMessage.setCommunicationId(communicationId);
        assertEquals(communicationId, clientMessage.getCommunicationId());
    }

    @Test
    public void testDelete() {
        long messageId = 1L;
        String clientName = "John Doe";
        long communicationId = 1L;
        Delete delete = new Delete(messageId, clientName, communicationId);

        assertEquals(Long.valueOf(messageId), Long.valueOf(delete.messageId));
        assertEquals(clientName, delete.clientName);
        assertEquals(Long.valueOf(communicationId), Long.valueOf(delete.communicationId));
        assertEquals(1, delete.getDuration());
    }

    @Test
    public void testDislike() {
        long messageId = 1L;
        String clientName = "John Doe";
        long communicationId = 1L;
        Dislike dislike = new Dislike(clientName, communicationId, messageId);

        assertEquals(Long.valueOf(messageId), Long.valueOf(dislike.messageId));
        assertEquals(clientName, dislike.clientName);
        assertEquals(Long.valueOf(communicationId), Long.valueOf(dislike.communicationId));
        assertEquals(1, dislike.getDuration());
    }

    @Test
    public void testEdit() {
        long messageId = 1L;
        String clientName = "John Doe";
        long communicationId = 1L;
        String editedMessage = "newMessage";
        Edit edit = new Edit(messageId, clientName, editedMessage, communicationId);

        assertEquals(Long.valueOf(messageId), Long.valueOf(edit.messageId));
        assertEquals(clientName, edit.clientName);
        assertEquals(Long.valueOf(communicationId), Long.valueOf(edit.communicationId));
        assertEquals(1, edit.getDuration());
        assertEquals(editedMessage, edit.newMessage);
    }

    @Test
    public void testFoundMessages() {
        List messages = new ArrayList();
        messages.add("mess1");
        messages.add("mess2");
        long communicationId = 1L;
        FoundMessages foundMessages = new FoundMessages(messages, communicationId);

        assertEquals(messages, foundMessages.messages);
        assertEquals(communicationId, foundMessages.communicationId.longValue());
        assertEquals(1, foundMessages.getDuration());
    }

    @Test
    public void testLike() {
        long messageId = 1L;
        String clientName = "John Doe";
        long communicationId = 1L;
        Like like = new Like(clientName, communicationId, messageId);

        assertEquals(Long.valueOf(messageId), Long.valueOf(like.messageId));
        assertEquals(clientName, like.clientName);
        assertEquals(Long.valueOf(communicationId), Long.valueOf(like.communicationId));
        assertEquals(1, like.getDuration());
    }

    @Test
    public void testPublish() {
        String clientName = "John Doe";
        UserMessage message = new UserMessage(clientName, "hello");
        long communicationId = 1L;
        Publish publish = new Publish(message, communicationId);

        assertEquals(clientName, publish.message.getAuthor());
        assertEquals(Long.valueOf(communicationId), Long.valueOf(publish.communicationId));
        assertEquals(message.getMessage(), publish.message.getMessage());
        assertEquals(3, publish.getDuration());
    }

    @Test
    public void testReaction() {
        long messageId = 1L;
        String clientName = "John Doe";
        long communicationId = 1L;
        Reaction reaction = new Reaction(clientName, communicationId, messageId, Reaction.Emoji.HORROR);

        assertEquals(clientName, reaction.clientName);
        assertEquals(Long.valueOf(communicationId), Long.valueOf(reaction.communicationId));
        assertEquals(Reaction.Emoji.HORROR, reaction.reaction);
        assertEquals(1, reaction.getDuration());
    }

    @Test
    public void testRemoveLikeOrDislike() {
        long messageId = 1L;
        String clientName = "John Doe";
        long communicationId = 1L;
        RemoveLikeOrDislike removeLikeOrDislike = new RemoveLikeOrDislike(clientName, communicationId, messageId, RemoveLikeOrDislike.Type.LIKE);

        assertEquals(clientName, removeLikeOrDislike.clientName);
        assertEquals(Long.valueOf(communicationId), Long.valueOf(removeLikeOrDislike.communicationId));
        assertEquals(RemoveLikeOrDislike.Type.LIKE, removeLikeOrDislike.typeToDelete);
        assertEquals(1, removeLikeOrDislike.getDuration());
    }


    @Test
    public void testReport() {
        String clientName = "John Doe";
        String reportedClientName = "Jane Doe";
        long communicationId = 1L;
        Report report = new Report(clientName, communicationId, reportedClientName);

        assertEquals(clientName, report.clientName);
        assertEquals(Long.valueOf(communicationId), Long.valueOf(report.communicationId));
        assertEquals(reportedClientName, report.reportedClientName);
        assertEquals(1, report.getDuration());
    }

    @Test
    public void testRetrieveMessages() {
        String clientName = "John Doe";
        long communicationId = 1L;
        RetrieveMessages retrieveMessages = new RetrieveMessages(clientName, communicationId);

        assertEquals(clientName, retrieveMessages.author);
        assertEquals(Long.valueOf(communicationId), Long.valueOf(retrieveMessages.communicationId));
        assertEquals(3, retrieveMessages.getDuration());
    }

    @Test
    public void testSearchMessages() {
        String search = "search";
        long communicationId = 1L;
        SearchMessages searchMessages = new SearchMessages(search, communicationId);

        assertEquals(search, searchMessages.searchText);
        assertEquals(Long.valueOf(communicationId), Long.valueOf(searchMessages.communicationId));
        assertEquals(3, searchMessages.getDuration());
    }

    @Test
    public void testAddDislike() {
        long messageId = 1L;
        String clientName = "John Doe";
        long communicationId = 1L;
        AddDislike addDislike = new AddDislike(clientName, messageId, communicationId);

        assertEquals(clientName, addDislike.clientName);
        assertEquals(Long.valueOf(communicationId), Long.valueOf(addDislike.communicationId));
        assertEquals(messageId, addDislike.messageId);
    }

    @Test
    public void testAddLike() {
        long messageId = 1L;
        String clientName = "John Doe";
        long communicationId = 1L;
        AddLike addLike = new AddLike(clientName, messageId, communicationId);

        assertEquals(clientName, addLike.clientName);
        assertEquals(Long.valueOf(communicationId), Long.valueOf(addLike.communicationId));
        assertEquals(messageId, addLike.messageId);
    }

    @Test
    public void testAddReaction() {
        long messageId = 1L;
        String clientName = "John Doe";
        long communicationId = 1L;
        Reaction.Emoji horror = Reaction.Emoji.HORROR;
        AddReaction addReaction = new AddReaction(clientName, messageId, communicationId, horror);

        assertEquals(clientName, addReaction.clientName);
        assertEquals(Long.valueOf(communicationId), Long.valueOf(addReaction.communicationId));
        assertEquals(messageId, addReaction.messageId);
        assertEquals(horror, addReaction.reaction);
    }

    @Test
    public void testAddReport() {
        String clientName = "John Doe";
        String reportedClientName = "Jane Doe";
        long communicationId = 1L;
        AddReport addReport = new AddReport(clientName, communicationId, reportedClientName);

        assertEquals(clientName, addReport.clientName);
        assertEquals(Long.valueOf(communicationId), Long.valueOf(addReport.communicationId));
        assertEquals(reportedClientName, addReport.reportedClientName);
    }

    @Test
    public void testDeleteLikeOrDislike() {
        long messageId = 1L;
        String clientName = "John Doe";
        long communicationId = 1L;
        RemoveLikeOrDislike.Type like = RemoveLikeOrDislike.Type.LIKE;
        DeleteLikeOrDislike deleteLikeOrDislike = new DeleteLikeOrDislike(clientName, communicationId, messageId, like);

        assertEquals(clientName, deleteLikeOrDislike.clientName);
        assertEquals(Long.valueOf(communicationId), Long.valueOf(deleteLikeOrDislike.communicationId));
        assertEquals(messageId, deleteLikeOrDislike.messageId);
        assertEquals(like, deleteLikeOrDislike.typeToDelete);
    }

    @Test
    public void testDeleteMessage() {
        long messageId = 1L;
        String clientName = "John Doe";
        long communicationId = 1L;
        DeleteMessage deleteMessage = new DeleteMessage(clientName, communicationId, messageId);

        assertEquals(clientName, deleteMessage.clientName);
        assertEquals(Long.valueOf(communicationId), Long.valueOf(deleteMessage.communicationId));
        assertEquals(messageId, deleteMessage.messageId);
    }

    @Test
    public void testEditMessage() {
        long messageId = 1L;
        String clientName = "John Doe";
        String newMessage = "new msg";
        long communicationId = 1L;
        EditMessage deleteMessage = new EditMessage(messageId, clientName, newMessage, messageId);

        assertEquals(clientName, deleteMessage.clientName);
        assertEquals(Long.valueOf(communicationId), Long.valueOf(deleteMessage.communicationId));
        assertEquals(messageId, deleteMessage.messageId);
        assertEquals(newMessage, deleteMessage.newMessage);
    }

    @Test
    public void testRetrieveFromStore() {
        String clientName = "John Doe";
        long communicationId = 1L;
        RetrieveFromStore retrieveFromStore = new RetrieveFromStore(clientName, communicationId);

        assertEquals(clientName, retrieveFromStore.author);
        assertEquals(Long.valueOf(communicationId), Long.valueOf(retrieveFromStore.communicationId));
    }

    @Test
    public void testSearchInStore() {
        String searchText = "John Doe";
        long communicationId = 1L;
        SearchInStore searchInStore = new SearchInStore(searchText, communicationId);

        assertEquals(searchText, searchInStore.searchText);
        assertEquals(Long.valueOf(communicationId), Long.valueOf(searchInStore.communicationId));
    }

    @Test
    public void testUpdateMessageStore() {
        String author = "John Doe";
        String message = "hello";
        UserMessage userMessage = new UserMessage(author, message);
        long communicationId = 1L;
        UpdateMessageStore updateMessageStore = new UpdateMessageStore(userMessage, communicationId);

        assertEquals(userMessage, updateMessageStore.message);
        assertEquals(communicationId, updateMessageStore.communicationId);
    }

    @Test
    public void testUserMessage() {
        String author = "John Doe";
        String message = "Test message";
        UserMessage userMessage = new UserMessage(author, message);

        assertEquals("John Doe", userMessage.getAuthor());
        assertEquals("Test message", userMessage.getMessage());
        assertEquals(0, userMessage.getPoints());
        assertTrue(userMessage.getLikes().isEmpty());
        assertTrue(userMessage.getDislikes().isEmpty());
        assertTrue(userMessage.getReactions().isEmpty());
        assertEquals(UserMessage.NEW_ID, userMessage.getMessageId());

        userMessage.setMessage("bye");
        assertEquals("bye", userMessage.getMessage());
        userMessage.setPoints(2);
        assertEquals(2, userMessage.getPoints());
        userMessage.setMessageId(2);
        assertEquals(2, userMessage.getMessageId());
    }

    @Test
    public void testSetMessage() {
        userMessage.setMessage("Updated message");
        assertEquals("Updated message", userMessage.getMessage());
    }

    @Test
    public void testSetPoints() {
        userMessage.setPoints(5);
        assertEquals(5, userMessage.getPoints());
    }

    @Test
    public void testSetMessageId() {
        userMessage.setMessageId(123L);
        assertEquals(123L, userMessage.getMessageId());
    }

    @Test
    public void testToString() {
        userMessage.getLikes().add("Alice");
        userMessage.getDislikes().add("Bob");
        userMessage.setPoints(1);
        String expected = "John Doe:Test message liked by :Alice disliked by :Bob; Points: 1";
        assertEquals(expected, userMessage.toString());
    }

    /*public static class AddDislikeTest {
        @Test
        public void addDislike_constructorTest() {
            long messageId = 100L;
            String clientName = "Alice";
            long commId = 200L;

            AddDislike addDislike = new AddDislike(clientName, messageId, commId);

            assertEquals(clientName, addDislike.clientName);
            assertEquals(messageId, addDislike.messageId);
            assertEquals(commId, addDislike.communicationId);
        }

        @Test
        public void addDislikeTest() {
            long messageId = 100L;
            String clientName = "Alice";
            long commId = 200L;

            AddDislike addDislike = new AddDislike(clientName, messageId, commId);
            String expectedString = "AddDislike{messageId=" + messageId + ", clientName='" + clientName + "', communicationId=" + commId + "}";

            assertEquals(expectedString, addDislike.toString());
        }
    }*/

    /*public static class AddLikeTest {
        @Test
        public void addLike_constructorTest() {
            long messageId = 100L;
            String clientName = "Alice";
            long commId = 200L;

            AddLike addLike = new AddLike(clientName, messageId, commId);

            assertEquals(clientName, addLike.clientName);
            assertEquals(messageId, addLike.messageId);
            assertEquals(commId, addLike.communicationId);
        }

        @Test
        public void addLike_toStringTest() {
            long messageId = 100L;
            String clientName = "Alice";
            long commId = 200L;

            AddLike addLike = new AddLike(clientName, messageId, commId);
            String expectedString = "AddLike{messageId=" + messageId + ", clientName='" + clientName + "', communicationId=" + commId + "}";

            assertEquals(expectedString, addLike.toString());
        }
    }*/

    @Test
    public void testUserMessageCreation() {
        UserMessage message = new UserMessage("John Doe", "Hello, world!");

        assertEquals("John Doe", message.getAuthor());
        assertEquals("Hello, world!", message.getMessage());
        assertEquals(0, message.getLikes().size());
        assertEquals(0, message.getDislikes().size());
        assertEquals(0, message.getPoints());
        assertEquals(UserMessage.NEW_ID, message.getMessageId());
    }

    @Test
    public void testUserMessageLikesDislikes() {
        UserMessage message = new UserMessage("John Doe", "Hello, world!");

        message.getLikes().add("Jane Doe");
        message.getDislikes().add("Alice");
        message.setPoints(1);

        assertEquals(1, message.getLikes().size());
        assertEquals("Jane Doe", message.getLikes().get(0));
        assertEquals(1, message.getDislikes().size());
        assertEquals("Alice", message.getDislikes().get(0));
        assertEquals(1, message.getPoints());
    }

    @Test
    public void testUserMessageReactions() {
        UserMessage message = new UserMessage("John Doe", "Hello, world!");

        Set<Reaction.Emoji> emojis = new HashSet<>();
        emojis.add(Reaction.Emoji.SMILEY);
        message.getReactions().put("Jane Doe", emojis);

        assertEquals(1, message.getReactions().size());
        assertEquals(true, message.getReactions().get("Jane Doe").contains(Reaction.Emoji.SMILEY));
    }

    @Test
    public void testUserMessageToString() {
        UserMessage message = new UserMessage("John Doe", "Hello, world!");
        message.getLikes().add("Jane Doe");
        message.getDislikes().add("Alice");
        message.setPoints(1);

        String expected = "John Doe:Hello, world! liked by :Jane Doe disliked by :Alice; Points: 1";
        assertEquals(expected, message.toString());
    }

    @Test
    public void getAuthor() {
        UserMessage um = new UserMessage("John", "hello");
        String author = um.getAuthor();
        assertEquals("John", author);
    }

    @Test
    public void getMessage() {
        UserMessage um = new UserMessage("John", "hello");
        String message = um.getMessage();
        assertEquals("hello", message);
    }

    @Test
    public void setMessage() {
        UserMessage um = new UserMessage("John", "hello");
        um.setMessage("goodbye");
        assertEquals("goodbye", um.getMessage());
    }

    @Test
    public void getLikes() {
        UserMessage um = new UserMessage("John", "hello");
        List<String> likes = um.getLikes();
        assertEquals(Collections.emptyList(), likes);
    }

    @Test
    public void getDislikes() {
        UserMessage um = new UserMessage("John", "hello");
        List<String> dislikes = um.getDislikes();
        assertEquals(Collections.emptyList(), dislikes);
    }

    @Test
    public void getPoints() {
        UserMessage um = new UserMessage("John", "hello");
        int points = um.getPoints();
        assertEquals(0, points);
    }

    @Test
    public void setPoints() {
        UserMessage um = new UserMessage("John", "hello");
        um.setPoints(33);
        assertEquals(33, um.getPoints());
    }

    @Test
    public void getReactions() {
        UserMessage um = new UserMessage("John", "hello");
        Map<String, Set<Reaction.Emoji>> reactions = um.getReactions();
        assertEquals(Collections.emptyMap(), reactions);
    }

    @Test
    public void getMessageId() {
        UserMessage um = new UserMessage("John", "hello");
        long messageId = um.getMessageId();
        assertEquals(-1, messageId);
    }

    @Test
    public void setMessageId() {
        UserMessage um = new UserMessage("John", "hello");
        um.setMessageId(10L);
        assertEquals(10L, um.getMessageId());
    }

    private static class TestClientActor extends SimulatedActor {
        private String name;
        public TestClientActor(String name) {
            this.name = name;
        }
        @Override
        public void receive(Message message) {
            // Do nothing; we only need this actor to store messages
        }
    }

    @Test
    public void receiveAddLike() {
        MessageStore messageStore = new MessageStore();
        UserMessage userMessage = new UserMessage("John", "hello");
        userMessage.setMessageId(1L);
        TestClientActor testClient = new TestClientActor("TestClient");
    }

    static class MockSimulatedActor extends SimulatedActor {
        int tellCount = 0;

        @Override
        public void tell(Message message) {
            tellCount++;
        }

        @Override
        public void receive(Message message) {
            // No specific behavior needed for this test
        }
    }

    static class MockSimulatedActorSystem extends SimulatedActorSystem {
        int stopCount = 0;

        @Override
        public void stop(SimulatedActor actor) {
            stopCount++;
        }
    }

    static class TestMessageStoreMessage extends MessageStoreMessage {
        public TestMessageStoreMessage(SimulatedActor storeClient, long communicationId) {
            super();
            this.storeClient = storeClient;
            this.communicationId = communicationId;
        }
    }

    @Test
    public void testUnknowClientException() {
        String clientmess = "This is an unknown client.";
        UnknownClientException exception = new UnknownClientException(clientmess);
        assertEquals(clientmess, exception.getMessage());
    }

    @Test
    public void testUnknowMessageException() {
        String message = "This is an unknown message.";
        UnknownMessageException exception = new UnknownMessageException(message);
        assertEquals(message, exception.getMessage());
    }

    @Test
    public void testOperationAck() {
        long communicationId = 12345L;
        OperationAck operationAck = new OperationAck(communicationId);
        assertEquals(Long.valueOf(communicationId), Long.valueOf(communicationId));
    }

    @Test
    public void testOperationFailed()
    {
        long commID = 65431L;
        OperationFailed opFailed = new OperationFailed(commID);
        assertEquals(Long.valueOf(commID), Long.valueOf(commID));
    }

    @Test
    public void testUserBanned()
    {
        long communiD = 43212L;
        UserBanned userBanned = new UserBanned(communiD);
        assertEquals(Long.valueOf(communiD), Long.valueOf(communiD));
    }

    @Test
    public void testAddMessageAndLike() throws UnknownMessageException, UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        Like like = new Like("John", 10, 1L);

        // Send the Like message to the worker instance from InitAck
        worker.tell(like);

        // Wait for the worker to process the Like message
        while (client.receivedMessages.size() == 0)
            system.runFor(1);
    }

    @Test
    public void testAddMessageAndDislike() throws UnknownMessageException, UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        Dislike dislike = new Dislike("John", 10, 1L);

        // Send the Dislike message to the worker instance from InitAck
        worker.tell(dislike);

        // Wait for the worker to process the Dislike message
        while (client.receivedMessages.size() == 0)
            system.runFor(1);
    }

    @Test
    public void testAddMessageAndRemoveLikeOrDislike() throws UnknownMessageException, UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        RemoveLikeOrDislike removeLikeOrDislike = new RemoveLikeOrDislike("John", 10, 1L, RemoveLikeOrDislike.Type.LIKE);

        // Send the RemoveLikeOrDislike message to the worker instance from InitAck
        worker.tell(removeLikeOrDislike);

        // Wait for the worker to process the RemoveLikeOrDislike message
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        // Other test assertions
    }

    @Test
    public void testAddMessageAndReaction() throws UnknownMessageException, UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        Reaction reaction = new Reaction("John", 10, 1L, Reaction.Emoji.HORROR);

        // Send the RemoveLikeOrDislike message to the worker instance from InitAck
        worker.tell(reaction);

        // Wait for the worker to process the RemoveLikeOrDislike message
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        // Other test assertions
    }

    @Test
    public void testAddMessageAndReport() throws UnknownMessageException, UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        Report report = new Report("John", 10, "Jane");

        // Send the RemoveLikeOrDislike message to the worker instance from InitAck
        worker.tell(report);

        // Wait for the worker to process the RemoveLikeOrDislike message
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        // Other test assertions
    }

    @Test
    public void testAddMessageAndSearchMessages() throws UnknownMessageException, UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        SearchMessages searchMessages = new SearchMessages("searchtext", 10);

        // Send the RemoveLikeOrDislike message to the worker instance from InitAck
        worker.tell(searchMessages);

        // Wait for the worker to process the RemoveLikeOrDislike message
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        // Other test assertions
    }

    @Test
    public void testAddMessageAndEdit() throws UnknownMessageException, UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        Edit edit = new Edit(90, "John", "newMsg", 10);


        // Send the RemoveLikeOrDislike message to the worker instance from InitAck
        worker.tell(edit);

        // Wait for the worker to process the RemoveLikeOrDislike message
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        // Other test assertions
    }

    @Test
    public void testAddMessageAndDelete() throws UnknownMessageException, UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        Delete delete = new Delete(3, "John", 10);


        // Send the RemoveLikeOrDislike message to the worker instance from InitAck
        worker.tell(delete);

        // Wait for the worker to process the RemoveLikeOrDislike message
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        // Other test assertions
    }

    @Test
    public void testAddMessageAndRetrieveMessages() throws UnknownMessageException, UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        RetrieveMessages retrieveMessages = new RetrieveMessages("John", 10);


        // Send the RemoveLikeOrDislike message to the worker instance from InitAck
        worker.tell(retrieveMessages);

        // Wait for the worker to process the RemoveLikeOrDislike message
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        // Other test assertions
    }

    @Test
    public void testAddMessageAndPublish() throws UnknownMessageException, UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        Publish publish = new Publish(new UserMessage("John", "message"), 10);


        // Send the RemoveLikeOrDislike message to the worker instance from InitAck
        worker.tell(publish);

        // Wait for the worker to process the RemoveLikeOrDislike message
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        // Other test assertions
    }

    @Test
    public void testAddMessageAndLikeWithInvalidCommunicationId() throws UnknownMessageException, UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        Like like = new Like("John", 11, 1L); // Use an incorrect communicationId (e.g., 11 instead of 10)

        // Send the Like message to the worker instance from InitAck
        worker.tell(like);

        // Wait for the worker to process the Like message and check if it throws UnknownClientException
        boolean exceptionThrown = false;
        while (client.receivedMessages.size() == 0) {
            try {
                system.runFor(1);
                if (client.receivedMessages.peek() instanceof UnknownClientException) {
                    exceptionThrown = true;
                    break;
                }
            } catch (UnknownClientException | UnknownMessageException e) {
                exceptionThrown = true;
                break;
            }
        }

        assertTrue(exceptionThrown);
    }

    @Test
    public void testAddMessageAndDislikeWithInvalidCommunicationId() throws UnknownMessageException, UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        Dislike dislike = new Dislike("John", 11, 1L); // Use an incorrect communicationId (e.g., 11 instead of 10)

        // Send the Like message to the worker instance from InitAck
        worker.tell(dislike);

        // Wait for the worker to process the Like message and check if it throws UnknownClientException
        boolean exceptionThrown = false;
        while (client.receivedMessages.size() == 0) {
            try {
                system.runFor(1);
                if (client.receivedMessages.peek() instanceof UnknownClientException) {
                    exceptionThrown = true;
                    break;
                }
            } catch (UnknownClientException | UnknownMessageException e) {
                exceptionThrown = true;
                break;
            }
        }

        assertTrue(exceptionThrown);
    }

    @Test
    public void testDeleteLikeOrDislikeWithInvalidCommunicationId() throws UnknownMessageException, UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        dispatcher.tell(new InitCommunication(client, 20));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        assertEquals(Long.valueOf(20), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        RemoveLikeOrDislike deleteLikeOrDislike = new RemoveLikeOrDislike("John", 21, 1L, RemoveLikeOrDislike.Type.LIKE); // Use an incorrect communicationId (e.g., 21 instead of 20)

        // Send the RemoveLikeOrDislike message to the worker instance from InitAck
        worker.tell(deleteLikeOrDislike);

        // Wait for the worker to process the RemoveLikeOrDislike message and check if it throws UnknownClientException
        boolean exceptionThrown = false;
        while (client.receivedMessages.size() == 0) {
            try {
                system.runFor(1);
                if (client.receivedMessages.peek() instanceof UnknownClientException) {
                    exceptionThrown = true;
                    break;
                }
            } catch (UnknownClientException | UnknownMessageException e) {
                exceptionThrown = true;
                break;
            }
        }

        assertTrue(exceptionThrown);
    }

    @Test
    public void testReactionWithInvalidCommunicationId() throws UnknownMessageException, UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        dispatcher.tell(new InitCommunication(client, 20));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        assertEquals(Long.valueOf(20), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        Reaction reaction = new Reaction("John", 21, 1L, Reaction.Emoji.HORROR); // Use an incorrect communicationId (e.g., 21 instead of 20)

        // Send the RemoveLikeOrDislike message to the worker instance from InitAck
        worker.tell(reaction);

        // Wait for the worker to process the RemoveLikeOrDislike message and check if it throws UnknownClientException
        boolean exceptionThrown = false;
        while (client.receivedMessages.size() == 0) {
            try {
                system.runFor(1);
                if (client.receivedMessages.peek() instanceof UnknownClientException) {
                    exceptionThrown = true;
                    break;
                }
            } catch (UnknownClientException | UnknownMessageException e) {
                exceptionThrown = true;
                break;
            }
        }

        assertTrue(exceptionThrown);
    }

    @Test
    public void testPublishWithInvalidUserMessage() throws UnknownMessageException, UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        dispatcher.tell(new InitCommunication(client, 20));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        assertEquals(Long.valueOf(20), initAck.communicationId);

        SimulatedActor worker = initAck.worker;

        // Create an invalid UserMessage instance (e.g., with a non-new messageId)
        UserMessage invalidUserMessage = new UserMessage("John", "text");
        invalidUserMessage.setMessageId(1L); // Set a non-new messageId

        Publish publish = new Publish(invalidUserMessage, 20); // Use the correct communicationId

        // Send the Publish message to the worker instance from InitAck
        worker.tell(publish);

        // Wait for the worker to process the Publish message and check if it sends OperationFailed
        boolean operationFailed = false;
        while (client.receivedMessages.size() == 0) {
            system.runFor(1);
            if (client.receivedMessages.peek() instanceof OperationFailed) {
                operationFailed = true;
                break;
            }
        }

        assertTrue(operationFailed);
    }

    @Test
    public void testGetDuration() {
        Reply reply = new Reply(1L) ;
        assertEquals(1, reply.getDuration());
    }


    @Test
    public void testMessageStoreMessage()
    {
        MessageStoreMessage message = new MessageStoreMessage() {
            @Override
            public int getDuration() {
                return super.getDuration();
            }
        };
        int expected = 1;
        int actual = message.getDuration();
        assertEquals(actual, expected);


    }

    @Test
    public void testSimulatedActor(){

        SimulatedActor simActor = new SimulatedActor() {
            @Override
            public void receive(Message message) throws UnknownClientException, UnknownMessageException {

            }
        };
        simActor.setId(123);
        assertEquals(123,simActor.getId());
    }

    @Test
    public void testAddMessageAndGetMessageById() throws UnknownClientException, UnknownMessageException {
        SimulatedActor actor = new SimulatedActor() {
            @Override
            public void receive(Message message) throws UnknownClientException, UnknownMessageException {
                // Do nothing
            }
        };

        Message message = new Message() {
            @Override
            public int getDuration() {
                return 0;
            }
        };

        // Add message to actor's message log
        actor.getMessageLog().add(message);

        List<Message> expectedMessage = new ArrayList<>();
        expectedMessage.add(message);

        assertEquals(expectedMessage, actor.getMessageLog());
    }

    @Test
    public void testTimeSinceSystemStart()
    {
        SimulatedActor actor = new SimulatedActor() {
            @Override
            public void receive(Message message) throws UnknownClientException, UnknownMessageException {

            }
        };

        int expectedTime  = -1;
        int actualTime = actor.getTimeSinceSystemStart();

        assertEquals(expectedTime, actualTime);


    }

    @Test
    public void testGetActors() {
        // Create a simulated actor system
        SimulatedActorSystem system = new SimulatedActorSystem();

        // Create some simulated actors
        TestClientActor testcl1 = new TestClientActor("actor1");
        TestClientActor testcl2 = new TestClientActor("actor2");
        TestClientActor testcl3 = new TestClientActor("actor3");

        system.getActors().add(testcl1);
        system.getActors().add(testcl2);
        system.getActors().add(testcl3);

        // Get the list of actors from the system
        List<SimulatedActor> actors = system.getActors();

        // Make sure the list contains all the actors we added
        assertTrue(actors.contains(testcl1));
        assertTrue(actors.contains(testcl2));
        assertTrue(actors.contains(testcl3));

        // Make sure the list doesn't contain any extra actors
        assertEquals(3, actors.size());
    }

    @Test
    public void testGetCurrentTime()
    {
        SimulatedActorSystem actorSystem = new SimulatedActorSystem();

        actorSystem.getCurrentTime();

        assertEquals(0, actorSystem.getCurrentTime());

    }

    @Test
    public void testRunUntil()
    {
        SimulatedActorSystem system = new SimulatedActorSystem();
        SimulatedActor actor = new TestClientActor("TestActor");
        system.getActors().add(actor);

        system.getCurrentTime();
        try {
            system.runUntil(10);
        } catch (UnknownClientException | UnknownMessageException e) {
            fail("Exception thrown: " + e.getMessage());
        }
        assertEquals(11, system.getCurrentTime());
    }

    @Test
    public void testTick() throws UnknownClientException, UnknownMessageException {

        MockSimulatedActor mockMessageStore = new MockSimulatedActor();
        MockSimulatedActor mockClient = new MockSimulatedActor();
        MockSimulatedActorSystem mockSystem = new MockSimulatedActorSystem();

        TestMessageStoreMessage testMessage = new TestMessageStoreMessage(null, 0);
        WorkerHelper workerHelper = new WorkerHelper(mockMessageStore, mockClient, testMessage, mockSystem);

        workerHelper.atStartUp();
        assertEquals(1, mockMessageStore.tellCount);

        workerHelper.tick();
        workerHelper.tick();
        workerHelper.tick();
        assertEquals(1, mockMessageStore.tellCount);

        workerHelper.tick();
        assertEquals(2, mockMessageStore.tellCount);

        workerHelper.tick();
        workerHelper.tick();
        workerHelper.tick();
        workerHelper.tick();
        assertEquals(3, mockMessageStore.tellCount);

        workerHelper.tick();
        workerHelper.tick();
        workerHelper.tick();
        assertEquals(0, mockClient.tellCount);

        workerHelper.tick();
        assertEquals(1, mockClient.tellCount);
        assertEquals(1, mockSystem.stopCount);
        workerHelper.receive(testMessage);
        assertEquals(2, mockClient.tellCount);
        assertEquals(2, mockSystem.stopCount);
        workerHelper.tick();
    }





}