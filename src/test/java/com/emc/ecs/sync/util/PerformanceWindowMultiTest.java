/*
 * Copyright 2013-2017 EMC Corporation. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.emc.ecs.sync.util;

import org.apache.log4j.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by cwikj on 10/30/2015.
 */
public class PerformanceWindowMultiTest {
    private static PerformanceWindow pw;
    public static void main(String[] args) throws Exception {
        pw = new PerformanceWindow(500, 10);

        // Pattern
        String layoutString = "%d{yyyy-MM-dd HH:mm:ss} %-5p [%t] %c{1}:%L - %m%n";
        PatternLayout layout  = new PatternLayout(layoutString);

        // Appender
        Appender appender = new ConsoleAppender(layout, "System.err");

        LogManager.getRootLogger().addAppender(appender);
        LogManager.getRootLogger().setLevel(Level.TRACE);

        ScheduledExecutorService se = Executors.newScheduledThreadPool(5);

        System.out.println("Adding one @ 1000b/s");

        se.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                pw.increment(1000);
            }
        }, 1, 1, TimeUnit.SECONDS);

        Thread.sleep(10000);

        System.out.println("Adding another @ 32kB/s");

        se.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                pw.increment(32 * 1024);
            }
        }, 1, 1, TimeUnit.SECONDS);

        Thread.sleep(10000);

        System.out.println("Adding a fast one @ 1MB/s");
        se.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                pw.increment(1024);
            }
        }, 1, 1, TimeUnit.MILLISECONDS);

        Thread.sleep(10000);

        System.out.println("Adding another fast one @ 100MB/s");
        se.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                pw.increment(128*1024);
            }
        }, 1, 1, TimeUnit.MILLISECONDS);

        Thread.sleep(100000);

        System.exit(0);
    }
}
