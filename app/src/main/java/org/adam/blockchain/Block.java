package org.adam.blockchain;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a block in a BlockChain.
 */
class Block {
    int proofThreshold;
    int lastProofThreshold;
    long id;
    long timeStamp;
    long nonce;
    long timeGenerating;
    String hash;
    String previousHash;
    MiningClient miner;
    List<BlockEntry> entryList;

    /**
     * Creates initial block
     */
    Block() {
        proofThreshold = 1;
        lastProofThreshold = 1;
        miner = null;
        id = 0;
        timeStamp = 0;
        nonce = 0;
        timeGenerating = 0;
        hash = "0";
        previousHash = "0";
        entryList = new ArrayList<>();
    }

    /**
     * Creates new block filled with values from the old block
     */
    Block(Block oldBlock) {
        lastProofThreshold = oldBlock.proofThreshold;
        proofThreshold = oldBlock.proofThreshold;
        miner = null;
        id = oldBlock.id + 1;
        timeStamp = 0;
        nonce = 0;
        timeGenerating = 0;
        previousHash = oldBlock.hash;
        hash = "";
        entryList = new ArrayList<>();
    }

    /**
     * Cretes deep copy
     */
    public static Block clone(Block oldBlock) {
        Block newBlock = new Block();

        newBlock.proofThreshold = oldBlock.proofThreshold;
        newBlock.lastProofThreshold = oldBlock.lastProofThreshold;
        newBlock.miner = oldBlock.miner;
        newBlock.id = oldBlock.id;
        newBlock.timeStamp = oldBlock.timeStamp;
        newBlock.nonce = oldBlock.nonce;
        newBlock.timeGenerating = oldBlock.timeGenerating;
        newBlock.hash = oldBlock.hash;
        newBlock.previousHash = oldBlock.previousHash;
        newBlock.entryList = new ArrayList<>();

        for (BlockEntry entry : oldBlock.entryList) {
            newBlock.entryList.add(entry);
        }

        return newBlock;
    }

    @Override
    public String toString() {
        StringBuilder toStringBuilder = new StringBuilder();
        StringBuilder messageBuilder = new StringBuilder();

        for (BlockEntry entry : entryList) {
            messageBuilder.append(entry.toString()).append("\n");
        }

        toStringBuilder.append("\n" + Utils.ColoredOutput.GREEN)
                .append("Block: ").append(id).append("             \n")
                .append("Timestamp: ").append(timeStamp / 1000).append("\n")
                .append("Last block: ").append(previousHash).append("\n")
                .append("This block: ").append(hash).append("\n")
                .append("Nonce: ").append(nonce).append("\n")
                .append("Proof threshold: ").append(proofThreshold).append("\n")
                .append("Time mining: ").append(timeGenerating).append("\n")
                .append("Miner: ").append(miner.getName()).append("\n")
                .append("------------------------------\n")
                .append(messageBuilder)
                .append("------------------------------\n" + Utils.ColoredOutput.RESET);

        return toStringBuilder.toString();
    }
}
