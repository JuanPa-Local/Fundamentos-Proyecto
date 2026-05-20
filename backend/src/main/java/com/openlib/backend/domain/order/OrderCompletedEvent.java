package com.openlib.backend.domain.order;

import com.openlib.backend.domain.book.Book;
import org.springframework.context.ApplicationEvent;

public class OrderCompletedEvent extends ApplicationEvent {
    private final Book book;

    public OrderCompletedEvent(Object source, Book book) {
        super(source);
        this.book = book;
    }

    public Book getBook() {
        return book;
    }
}
