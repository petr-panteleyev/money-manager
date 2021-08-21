/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.messaging;

import javafx.embed.swing.JFXPanel;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class TestMessageQueue {
    private static class MessageSender implements Runnable {
        private final MessageQueue queue;
        private final Supplier<Message> messageSupplier;
        private final int count;

        MessageSender(MessageQueue queue, Supplier<Message> messageSupplier, int count) {
            this.queue = queue;
            this.messageSupplier = messageSupplier;
            this.count = count;
        }

        @Override
        public void run() {
            for (int i = 0; i < count; i++) {
                queue.send(messageSupplier.get());
            }
        }
    }

    @BeforeClass
    public static void initFx() {
        new JFXPanel();
    }

    @BeforeMethod
    public void initCounters() {
        count1.set(0);
        count2.set(0);
        endOfTestQueue.clear();
    }

    private static class TestMessage1 implements Message {
    }

    private static class TestMessage2 implements Message {
    }

    private static class EndOfTestMessage implements Message {
    }

    private final AtomicInteger count1 = new AtomicInteger();
    private final AtomicInteger count2 = new AtomicInteger();

    private final BlockingQueue<Boolean> endOfTestQueue = new ArrayBlockingQueue<>(1);

    private final MessageHandler endMessageHandler = message -> endOfTestQueue.offer(true);

    @Test
    public void testMultipleHandlers() throws Exception {
        var queue = new MessageQueue().start();

        assertTrue(queue.subscribe(TestMessage1.class, message -> count1.incrementAndGet()));
        assertTrue(queue.subscribe(TestMessage1.class, message -> count1.incrementAndGet()));
        assertTrue(queue.subscribe(TestMessage2.class, message -> count2.incrementAndGet()));
        queue.subscribe(EndOfTestMessage.class, endMessageHandler);

        var t1 = new Thread(new MessageSender(queue, TestMessage1::new, 100));
        var t2 = new Thread(new MessageSender(queue, TestMessage2::new, 100));

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        queue.send(new EndOfTestMessage());

        var endOfTest = endOfTestQueue.take();
        assertTrue(endOfTest);

        assertEquals(count1.get(), 200);
        assertEquals(count2.get(), 100);
    }

    @Test
    public void testDuplicateHandlers() throws Exception {
        var queue = new MessageQueue().start();

        MessageHandler handler = message -> count1.incrementAndGet();

        assertTrue(queue.subscribe(TestMessage1.class, handler));
        assertFalse(queue.subscribe(TestMessage1.class, handler));
        queue.subscribe(EndOfTestMessage.class, endMessageHandler);

        var t1 = new Thread(new MessageSender(queue, TestMessage1::new, 100));
        var t2 = new Thread(new MessageSender(queue, TestMessage2::new, 100));

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        queue.send(new EndOfTestMessage());

        var endOfTest = endOfTestQueue.take();
        assertTrue(endOfTest);

        assertEquals(count1.get(), 100);
        assertEquals(count2.get(), 0);
    }
}

