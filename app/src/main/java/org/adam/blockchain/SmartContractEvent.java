package org.adam.blockchain;

import java.util.EventObject;

/**
 * Event sent to SmartContractListeners. Contains the newly mined block.
 *
 */
public class SmartContractEvent extends EventObject {
    private final Block block;
    public boolean isStopping = false;

    public SmartContractEvent(BlockChain blockChain, Block block) {
        super(blockChain);
        this.block = block;
    }

    public Block getBlock() {
        return block;
    }
}
