package org.adam.blockchain;

import java.util.EventObject;

/**
 * Event sent to registered miners. Contains a block to mine or a termination
 * condition.
 */
public class MinerEvent extends EventObject {
    private boolean terminate = false;
    private final Block block;
    private final MiningClient client;

    /**
     * New block to mine.
     */
    public MinerEvent(BlockChain chain, MiningClient client, Block block) {
        super(chain);
        this.client = client;
        this.block = block;
    }

    /**
     * Termination block
     */
    public MinerEvent(BlockChain chain) {
        super(chain);
        terminate = true;
        block = null;
        client = null;
    }

    public Block getBlock() {
        return block;
    }

    public MiningClient getClient() {
        return client;
    }

    public boolean shouldTerminate() {
        return terminate;
    }
}
