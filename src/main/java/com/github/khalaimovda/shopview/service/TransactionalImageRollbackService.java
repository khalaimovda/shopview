package com.github.khalaimovda.shopview.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;


@Service
@RequiredArgsConstructor
public class TransactionalImageRollbackService implements ImageRollbackService {

    private final ImageService imageService;

    /**
     * Register TransactionSynchronization which will remove image if transaction is rolled back
     */
    @Override
    public void registerImageRollback(String imagePath) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                    imageService.deleteImage(imagePath);
                }
            }
            @Override public void suspend() {}
            @Override public void resume() {}
            @Override public void flush() {}
            @Override public void beforeCommit(boolean readOnly) {}
            @Override public void beforeCompletion() {}
        });
    }
}
