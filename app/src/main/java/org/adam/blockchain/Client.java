package org.adam.blockchain;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.EventListener;
import java.util.concurrent.atomic.AtomicBoolean;

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

interface MinerEventListener extends EventListener {
    void onMinerEvent(MinerEvent event);
}

/**
 * Client that registers for events containing blocks to be mined.
 * starts a background Miner thread when the event is received.
 */
class MiningClient extends Client implements MinerEventListener {
    private AtomicBoolean isMinerEnabled = new AtomicBoolean(true);

    public MiningClient(String name, BlockChain blockChain) {
        super(name, blockChain);
        blockChain.registerListener(this);
    }

    public void stopMiner() {
        blockChain.unregisterListener(this);
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
