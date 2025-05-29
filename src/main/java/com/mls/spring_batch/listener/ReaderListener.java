package com.mls.spring_batch.listener;

import com.mls.spring_batch.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class ReaderListener implements ItemReadListener<Product> {

    @Override
    public void beforeRead() {
        ItemReadListener.super.beforeRead();
    }

    @Override
    public void afterRead(Product item) {
        ItemReadListener.super.afterRead(item);
    }

    @Override
    public void onReadError(Exception ex) {
        ItemReadListener.super.onReadError(ex);
    }
}
