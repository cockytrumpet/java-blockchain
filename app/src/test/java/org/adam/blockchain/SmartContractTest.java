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
    private BlockChain chain;
    private MiningClient client;
    private SmartContract smartContract;

    public SmartContractTest() {
        chain = new BlockChain();
        client = new MiningClient("client", chain);
    }

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
    void testExecution() {
        testLock.lock();
        try {
            Thread.sleep(250);

            SmartContract sc = new SmartContract(client, this::triggerSuccess);
            smartContract = (SmartContract) client.sign(sc);
            chain.send(smartContract);

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
