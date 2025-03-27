package com.github.khalaimovda.shopview.service;

public interface ImageRollbackService {
    /**
     * Register logic to rollback image saving in case of problems with other data
     */
    void registerImageRollback(String imagePath);
}
