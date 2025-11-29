package com.shun.gateway.util;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Created by chenwenshun on 2022/6/30
 */
@Component
@Scope("prototype")
public class CircleBloomFilter {


    private CopyOnWriteArrayList<BloomFilter<CharSequence>> filters;

    private static final long expectedInsertions = 10000 * 100L;
    private static final double fpp = 0.000001;


    public CircleBloomFilter() {
        this.filters = new CopyOnWriteArrayList<>();
        filters.add(BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), expectedInsertions, fpp));
        doCircle();
    }

    private void doCircle() {
        new ScheduledThreadPoolExecutor(1, runnable -> {
            Thread thread = new Thread(runnable, "CircleBloomFilter");
            thread.setDaemon(true);
            return thread;
        }).scheduleAtFixedRate(() -> {
            this.filters.add(0, BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), expectedInsertions, fpp));
            if(filters.size() > 5){
                this.filters.remove(filters.size() -1);
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    public void put(String key){
        this.filters.get(0).put(key);
    }

    public Boolean exists(String key){
        for (BloomFilter<CharSequence> filter : this.filters) {
            if (filter.mightContain(key)) {
                return true;
            }
        }
        return false;
    }


    public static void main(String[] args) {
//        LinkedList<String> filters = new LinkedList<>();
//        filters.add("bella");
//        filters.add("tina");
//        filters.add("shun");
//
//        System.out.println(filters);
//
//        String poll = filters.poll();
//        System.out.println(poll);
//
//        System.out.println(filters);
        CircleBloomFilter circleBloomFilter = new CircleBloomFilter();
        circleBloomFilter.put("bella");
        circleBloomFilter.put("Tina");
        circleBloomFilter.put("shun");
        System.out.println(circleBloomFilter.exists("bella"));
        System.out.println(circleBloomFilter.exists("bellax"));
        long start = System.currentTimeMillis();
        new Thread(() -> {
            IntStream.range(0, 2000 * 10000).forEach(i -> {
                circleBloomFilter.exists("111eyJraWQiOiJFRjRGMjJDMC01Q0IwLTQzNDgtOTY3Qi0wMjY0OTVFN0VGQzgiLCJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJjb20uemhpaHVpc2h1IiwiYXVkIjoicGMiLCJzdWIiOiJCZWxsYSIsImlhdCI6MTY0MDgzNjM2NSwiZXhwIjoxNjQwODQzNTY1LCJqdGkiOiJkZjE0OTZkNS1kYjM2LTRiNzktYmU5Yy03ZTllYjhhNDE0OTMiLCJ1aWQiOjE4NzUwMTY2NDg5OTk5OTksInJvbGUiOiJhZG1pbiJ9.V3oCWoGbb51lt7kXZLPZrBoLwbRYCDNq6J1Ke8gyyxF-1Ztw2rlpZTRVbIkPOW9WTOihz1iVSdQm0eqaTDQz8w");
//                System.out.println(exists);
//                System.out.println("xxxxx"+ (CurrentTimeMillisClock.getInstance().now().getTime() - start));
            });
        }).start();


        IntStream.range(0, 2000 * 10000).forEach(i ->
                {
                    circleBloomFilter.put(i + "eyJraWQiOiJFRjRGMjJDMC01Q0IwLTQzNDgtOTY3Qi0wMjY0OTVFN0VGQzgiLCJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJjb20uemhpaHVpc2h1IiwiYXVkIjoicGMiLCJzdWIiOiJCZWxsYSIsImlhdCI6MTY0MDgzNjM2NSwiZXhwIjoxNjQwODQzNTY1LCJqdGkiOiJkZjE0OTZkNS1kYjM2LTRiNzktYmU5Yy03ZTllYjhhNDE0OTMiLCJ1aWQiOjE4NzUwMTY2NDg5OTk5OTksInJvbGUiOiJhZG1pbiJ9.V3oCWoGbb51lt7kXZLPZrBoLwbRYCDNq6J1Ke8gyyxF-1Ztw2rlpZTRVbIkPOW9WTOihz1iVSdQm0eqaTDQz8w");
                }
        );

        System.out.println("xxxxx"+ (System.currentTimeMillis() - start));
        System.out.println(circleBloomFilter.exists("111eyJraWQiOiJFRjRGMjJDMC01Q0IwLTQzNDgtOTY3Qi0wMjY0OTVFN0VGQzgiLCJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJjb20uemhpaHVpc2h1IiwiYXVkIjoicGMiLCJzdWIiOiJCZWxsYSIsImlhdCI6MTY0MDgzNjM2NSwiZXhwIjoxNjQwODQzNTY1LCJqdGkiOiJkZjE0OTZkNS1kYjM2LTRiNzktYmU5Yy03ZTllYjhhNDE0OTMiLCJ1aWQiOjE4NzUwMTY2NDg5OTk5OTksInJvbGUiOiJhZG1pbiJ9.V3oCWoGbb51lt7kXZLPZrBoLwbRYCDNq6J1Ke8gyyxF-1Ztw2rlpZTRVbIkPOW9WTOihz1iVSdQm0eqaTDQz8w"));



    }
}
