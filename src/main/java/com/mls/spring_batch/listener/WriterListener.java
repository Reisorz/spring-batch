package com.mls.spring_batch.listener;

import com.mls.spring_batch.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.Chunk;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class WriterListener implements ItemWriteListener<Product> {

    @Override
    public void beforeWrite(Chunk<? extends Product> items) {
        ItemWriteListener.super.beforeWrite(items);
    }

    @Override
    public void afterWrite(Chunk<? extends Product> items) {
        ItemWriteListener.super.afterWrite(items);
    }

    @Override
    public void onWriteError(Exception exception, Chunk<? extends Product> items) {
        ItemWriteListener.super.onWriteError(exception, items);
    }
}
