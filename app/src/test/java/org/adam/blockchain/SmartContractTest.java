package org.adam.blockchain;

import org.junit.jupiter.api.Test;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;

class SmartContractTest {
    private final Lock testLock = new ReentrantLock();
    private final Condition hasSuccess = testLock.newCondition();
    private boolean success = false;

    void triggerSuccess() {
        testLock.lock();
        try {
            success = true;
            hasSuccess.signalAll();
        } finally {
            testLock.unlock();
        }
    }

    @Test
    void testSmartContractExecution() {
        BlockChain blockchain = new BlockChain();
        MiningClient client = new MiningClient("client", blockchain);

        SmartContract smartContract = new SmartContract(this::triggerSuccess);
        smartContract.sourceClient = client;
        smartContract = (SmartContract) client.sign(smartContract);

        testLock.lock();
        try {
            blockchain.send(smartContract);
            long startTime = System.currentTimeMillis();
            long timeout = 10000;

            while (!success) {
                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed >= timeout) {
                    fail("Test timed out.");
                    break;
                }
                hasSuccess.awaitNanos(1_000_000); // 1ms
            }

            assertTrue(success, "Smart contract executed successfully.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Test was interrupted.");
        } finally {
            testLock.unlock();
        }
    }
}
