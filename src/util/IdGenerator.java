package com.hospital.appointment.util;

import java.util.concurrent.atomic.AtomicLong;

public final class IdGenerator {

    private final AtomicLong next;

    public IdGenerator(long seed) {
        this.next = new AtomicLong(seed);
    }

    public long nextId() {
        return next.incrementAndGet();
    }
}
