package org.adam.blockchain;

import java.security.PublicKey;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * An entry that can be stored in a block.
 */
abstract class BlockEntry {
    protected static final AtomicInteger nextId = new AtomicInteger(1);
    protected int id;
    protected byte[] signature;
    protected PublicKey verifyWith;
    protected Client sourceClient;

    public BlockEntry() {
        this.id = nextId.getAndIncrement();
    }

    public int getId() {
        return id;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public byte[] getSignature() {
        return signature != null ? signature.clone() : new byte[] { '0' };
    }

    public Client getSourceClient() {
        return sourceClient;
    }

    public PublicKey publicKey() {
        return sourceClient.getPublicKey();
    }

    public boolean isValid() {
        return Utils.verifySignature(this);
    }

    @Override
    public abstract String toString();
}

/**
 * Empty entry.
 */
class EmptyBlockEntry extends BlockEntry {
    public EmptyBlockEntry() {
        signature = new byte[] { '0' };
        sourceClient = null;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public String toString() {
        return "No transactions";
    }
}

/**
 * An entry containing a message.
 */
class Message extends BlockEntry {
    public String text;

    public Message(Client sourceClient, String text) {
        this.sourceClient = sourceClient;
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}

/**
 * An entry containing a transaction.
 */
class Transaction extends BlockEntry {
    public Long amount;
    public Client destinationClient;

    public Transaction(Client sourceClient, long amount, Client destinationClient) {
        this.amount = amount;
        this.sourceClient = sourceClient;
        this.destinationClient = destinationClient;
    }

    public Long getAmount() {
        return amount;
    }

    public Client destinationClient() {
        return destinationClient;
    }

    @Override
    public String toString() {
        return sourceClient.name + " sent " + amount + " 🪙 to " + destinationClient.name;
    }
}

/**
 * An entry containing a command to be executed by miners.
 */
class Command extends Message {
    public Command(Client chainClient, String text) {
        super(chainClient, text);
    }

    @Override
    public String toString() {
        return "<" + text + ">";
    }
}

/**
 * An entry containing a function to be registered with the blockchain.
 *
 * TODO: register function with chain
 * - call register() from processTransactions() to start a client
 * - funds can be sent to SmartContract.getClient()
 */
class SmartContract extends BlockEntry {
    private final Runnable function;
    private Client client;

    public SmartContract(Runnable function) {
        this.function = function;
    }

    public Client getClient() {
        return client;
    }

    public void register(BlockChain blockChain) {
        if (client == null) {
            client = new Client(toString(), blockChain);
        }
    }

    public void execute() {
        if (function != null) {
            function.run();
        }
    }

    @Override
    public String toString() {
        return "SmartContract-" + getId();
    }
}
