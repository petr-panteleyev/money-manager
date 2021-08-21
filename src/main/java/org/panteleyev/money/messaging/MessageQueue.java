/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.messaging;

import javafx.application.Platform;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import static java.lang.Thread.interrupted;

public class MessageQueue {
    private final BlockingQueue<Message> queue = new LinkedBlockingQueue<>();
    private final Map<Class<? extends Message>, List<WeakReference<MessageHandler>>> subscribers = new ConcurrentHashMap<>();
    private final Map<Class<? extends Message>, Lock> locks = new ConcurrentHashMap<>();
    private final Thread processingThread = new Thread(this::messageProcessing);

    private static final Message STOP_MESSAGE = new Message() {
    };

    public MessageQueue start() {
        processingThread.setDaemon(true);
        processingThread.start();
        return this;
    }

    public void stop() {
        processingThread.interrupt();
        send(STOP_MESSAGE);
    }

    public boolean send(Message message) {
        return queue.offer(message);
    }

    /**
     * Adds subscriber for the message class.
     *
     * @param messageClass class of the message
     * @param handler      message handler
     * @return {@code true} if handler was added, {@code false} if handler is a duplicate
     */
    public boolean subscribe(Class<? extends Message> messageClass, MessageHandler handler) {
        var handlers = getHandlers(messageClass);
        return lockOnMessageClass(messageClass, () -> {
            // Prevent duplicates
            for (var ref : handlers) {
                if (ref.refersTo(handler)) {
                    return false;
                }
            }
            handlers.add(new WeakReference<>(handler));
            return true;
        });
    }

    private void messageProcessing() {
        while (!interrupted()) {
            try {
                var message = queue.take();
                if (interrupted()) {
                    break;
                }

                var messageClass = message.getClass();
                var handlers = getHandlers(messageClass);
                lockOnMessageClass(messageClass, () -> {
                    for (var iterator = handlers.iterator(); iterator.hasNext(); ) {
                        var handler = iterator.next().get();
                        if (handler == null) {
                            iterator.remove();
                        } else {
                            // Message handlers are executed in UI thread
                            Platform.runLater(() -> handler.handle(message));
                        }
                    }
                });
            } catch (InterruptedException ex) {
                break;
            }
        }
    }

    private List<WeakReference<MessageHandler>> getHandlers(Class<? extends Message> messageClass) {
        return subscribers.computeIfAbsent(messageClass, k -> new LinkedList<>());
    }

    private Lock getLock(Class<? extends Message> messageClass) {
        return locks.computeIfAbsent(messageClass, k -> new ReentrantLock());
    }

    private <T> T lockOnMessageClass(Class<? extends Message> messageClass, Supplier<T> block) {
        var lock = getLock(messageClass);
        try {
            lock.lock();
            return block.get();
        } finally {
            lock.unlock();
        }
    }

    private void lockOnMessageClass(Class<? extends Message> messageClass, Runnable block) {
        var lock = getLock(messageClass);
        try {
            lock.lock();
            block.run();
        } finally {
            lock.unlock();
        }
    }
}
