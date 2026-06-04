package com.openlib.backend.domain.book;

import com.openlib.backend.domain.order.OrderCompletedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class BookObserver {

    private final BookRepository bookRepository;

    public BookObserver(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @EventListener
    public void onOrderCompleted(OrderCompletedEvent event) {
        Book book = event.getBook();
        int currentCount = book.getDownloadCount() != null ? book.getDownloadCount() : 0;
        book.setDownloadCount(currentCount + 1);
        bookRepository.save(book);
        System.out.println("Libro descargado: " + book.getTitle() + " - Descargas totales: " + book.getDownloadCount());
    }
}
