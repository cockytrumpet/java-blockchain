package org.adam.blockchain;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.EventListener;
import java.util.concurrent.atomic.AtomicBoolean;

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
class LotteryClient extends Client implements SmartContractListener {
    public LotteryClient(String name, BlockChain blockChain) {
        super(name, blockChain);
        blockChain.registerSmartContractListener(this);
    }

    @Override
    public void onSmartContractEvent(SmartContractEvent event) {
        // NOTE: how to lottery?
    }
}
