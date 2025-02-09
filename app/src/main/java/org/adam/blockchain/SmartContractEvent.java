package org.adam.blockchain;

import java.util.EventObject;

/**
 *
 *
 */
public class SmartContractEvent extends EventObject {
    private final Block block;

    public SmartContractEvent(BlockChain blockChain, Block block) {
        super(blockChain);
        this.block = block;
    }

    public Block getBlock() {
        return block;
    }
}
