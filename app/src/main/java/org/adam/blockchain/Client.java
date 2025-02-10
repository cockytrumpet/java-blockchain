package org.adam.blockchain;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

interface MinerListener extends EventListener {
    void onMinerEvent(MinerEvent event);
}

interface SmartContractListener extends EventListener {
    void onSmartContractEvent(SmartContractEvent event);
}

/*
 * A client can sign and submit messages and currency
 * transfers to BlockChain for processing.
 */
class Client {
    public String name;
    protected BlockChain blockChain;
    private KeyPair keyPair;

    public Client(String name, BlockChain blockChain) {
        this.name = name;
        this.blockChain = blockChain;
        this.keyPair = Utils.generateKeys();
    }

    public boolean sendMessage(String text) {
        Message message = new Message(this, "%s: %s".formatted(name, text));
        return blockChain.send(sign(message));
    }

    public boolean sendCurrency(long amount, Client destinationClient) {
        Transaction transaction = new Transaction(this, amount, destinationClient);
        return blockChain.send(sign(transaction));
    }

    public boolean sendSmartContract(
            BiFunction<String, BlockChain, SmartContractListener> smartContractListenerFactory) {
        SmartContract smartContract = new SmartContract(this, smartContractListenerFactory);
        return blockChain.send(sign(smartContract));
    }

    public BlockEntry sign(BlockEntry blockEntry) {
        return Utils.signBlockEntry(blockEntry, keyPair.getPrivate());
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public Long getBalance() {
        return blockChain.getBalance(this);
    }

    public String getName() {
        return name;
    }
}

/**
 * Client that registers for events containing blocks to be mined.
 * starts a background Miner thread when the event is received.
 */
class MiningClient extends Client implements MinerListener {
    private AtomicBoolean isMinerEnabled = new AtomicBoolean(true);

    public MiningClient(String name, BlockChain blockChain) {
        super(name, blockChain);
        blockChain.registerMinerListener(this);
    }

    public void stopMiner() {
        blockChain.unregisterMinerListener(this);
        isMinerEnabled.set(false);
    }

    public void submitBlock(Block minedBlock) {
        blockChain.addBlock(minedBlock);
        if (minedBlock instanceof TerminationBlock) {
            stopMiner();
        }
    }

    @Override
    public void onMinerEvent(MinerEvent event) {
        if (isMinerEnabled.get()) {
            Thread thread = new Thread(new Miner(event));
            thread.start();
        }
    }
}

/**
 * Simple smart contract client.
 */
// NOTE: needs a stop method
// - send remaining balance to originating client
// - unregister
class LotteryClient extends Client implements SmartContractListener {
    private long trigger;
    private Set<Client> gamblers;

    public LotteryClient(String name, BlockChain blockChain) {
        super(name, blockChain);
        trigger = ThreadLocalRandom.current().nextLong(3L, 11L);
        gamblers = new HashSet<>();
        blockChain.registerSmartContractListener(this);
    }

    @Override
    public void onSmartContractEvent(SmartContractEvent event) {
        event.getBlock().entryList.stream()
                .filter(blockEntry -> blockEntry instanceof Transaction)
                .map(blockEntry -> (Transaction) blockEntry)
                .filter(transaction -> transaction.destinationClient.equals(this))
                .map(transaction -> transaction.sourceClient)
                .forEach(gambler -> gamblers.add(gambler));

        long balance = blockChain.getBalance(this);

        if (balance > trigger) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            List<Client> clients = new ArrayList<>(gamblers);
            Client winner = clients.get(random.nextInt(clients.size()));
            sendCurrency(trigger - 2, winner); // trigger - fee - profit
            gamblers.clear();
            trigger += random.nextLong(3L, 11L);
        }
    }
}
