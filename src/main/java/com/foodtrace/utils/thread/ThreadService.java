package com.foodtrace.utils.thread;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;

@Service
public class ThreadService {

    @Autowired
    private Executor threadPool;

    public void executeAsyncTask(Runnable task) {
        threadPool.execute(task);
    }

}

