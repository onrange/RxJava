/**
 * Copyright 2013 Netflix, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rx.concurrency;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.subscriptions.BooleanSubscription;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Action1;
import rx.util.functions.Func1;
import rx.util.functions.Func2;

public class TestSchedulers {

    @Test
    public void testComputationThreadPool1() {

        Observable<Integer> o1 = Observable.<Integer> from(1, 2, 3, 4, 5);
        Observable<Integer> o2 = Observable.<Integer> from(6, 7, 8, 9, 10);
        @SuppressWarnings("unchecked")
        Observable<String> o = Observable.<Integer> merge(o1, o2).map(new Func1<Integer, String>() {

            @Override
            public String call(Integer t) {
                assertTrue(Thread.currentThread().getName().startsWith("RxComputationThreadPool"));
                return "Value_" + t + "_Thread_" + Thread.currentThread().getName();
            }
        });

        o.subscribeOn(Schedulers.threadPoolForComputation()).forEach(new Action1<String>() {

            @Override
            public void call(String t) {
                System.out.println("t: " + t);
            }
        });
    }

    @Test
    public void testIOThreadPool1() {

        Observable<Integer> o1 = Observable.<Integer> from(1, 2, 3, 4, 5);
        Observable<Integer> o2 = Observable.<Integer> from(6, 7, 8, 9, 10);
        @SuppressWarnings("unchecked")
        Observable<String> o = Observable.<Integer> merge(o1, o2).map(new Func1<Integer, String>() {

            @Override
            public String call(Integer t) {
                assertTrue(Thread.currentThread().getName().startsWith("RxIOThreadPool"));
                return "Value_" + t + "_Thread_" + Thread.currentThread().getName();
            }
        });

        o.subscribeOn(Schedulers.threadPoolForIO()).forEach(new Action1<String>() {

            @Override
            public void call(String t) {
                System.out.println("t: " + t);
            }
        });
    }

    @Test
    public void testMergeWithoutScheduler1() {

        final String currentThreadName = Thread.currentThread().getName();

        Observable<Integer> o1 = Observable.<Integer> from(1, 2, 3, 4, 5);
        Observable<Integer> o2 = Observable.<Integer> from(6, 7, 8, 9, 10);
        @SuppressWarnings("unchecked")
        Observable<String> o = Observable.<Integer> merge(o1, o2).map(new Func1<Integer, String>() {

            @Override
            public String call(Integer t) {
                assertTrue(Thread.currentThread().getName().equals(currentThreadName));
                return "Value_" + t + "_Thread_" + Thread.currentThread().getName();
            }
        });

        o.forEach(new Action1<String>() {

            @Override
            public void call(String t) {
                System.out.println("t: " + t);
            }
        });
    }

    @Test
    public void testMergeWithImmediateScheduler1() {

        final String currentThreadName = Thread.currentThread().getName();

        Observable<Integer> o1 = Observable.<Integer> from(1, 2, 3, 4, 5);
        Observable<Integer> o2 = Observable.<Integer> from(6, 7, 8, 9, 10);
        @SuppressWarnings("unchecked")
        Observable<String> o = Observable.<Integer> merge(o1, o2).subscribeOn(Schedulers.immediate()).map(new Func1<Integer, String>() {

            @Override
            public String call(Integer t) {
                assertTrue(Thread.currentThread().getName().equals(currentThreadName));
                return "Value_" + t + "_Thread_" + Thread.currentThread().getName();
            }
        });

        o.forEach(new Action1<String>() {

            @Override
            public void call(String t) {
                System.out.println("t: " + t);
            }
        });
    }

    @Test
    public void testMergeWithCurrentThreadScheduler1() {

        final String currentThreadName = Thread.currentThread().getName();

        Observable<Integer> o1 = Observable.<Integer> from(1, 2, 3, 4, 5);
        Observable<Integer> o2 = Observable.<Integer> from(6, 7, 8, 9, 10);
        @SuppressWarnings("unchecked")
        Observable<String> o = Observable.<Integer> merge(o1, o2).subscribeOn(Schedulers.currentThread()).map(new Func1<Integer, String>() {

            @Override
            public String call(Integer t) {
                assertTrue(Thread.currentThread().getName().equals(currentThreadName));
                return "Value_" + t + "_Thread_" + Thread.currentThread().getName();
            }
        });

        o.forEach(new Action1<String>() {

            @Override
            public void call(String t) {
                System.out.println("t: " + t);
            }
        });
    }

    @Test
    public void testMergeWithScheduler1() {

        final String currentThreadName = Thread.currentThread().getName();

        Observable<Integer> o1 = Observable.<Integer> from(1, 2, 3, 4, 5);
        Observable<Integer> o2 = Observable.<Integer> from(6, 7, 8, 9, 10);
        @SuppressWarnings("unchecked")
        Observable<String> o = Observable.<Integer> merge(o1, o2).subscribeOn(Schedulers.threadPoolForComputation()).map(new Func1<Integer, String>() {

            @Override
            public String call(Integer t) {
                assertFalse(Thread.currentThread().getName().equals(currentThreadName));
                assertTrue(Thread.currentThread().getName().startsWith("RxComputationThreadPool"));
                return "Value_" + t + "_Thread_" + Thread.currentThread().getName();
            }
        });

        o.forEach(new Action1<String>() {

            @Override
            public void call(String t) {
                System.out.println("t: " + t);
            }
        });
    }

    @Test
    public void testSubscribeWithScheduler1() throws InterruptedException {

        final AtomicInteger count = new AtomicInteger();

        Observable<Integer> o1 = Observable.<Integer> from(1, 2, 3, 4, 5);

        o1.subscribe(new Action1<Integer>() {

            @Override
            public void call(Integer t) {
                System.out.println("Thread: " + Thread.currentThread().getName());
                System.out.println("t: " + t);
                count.incrementAndGet();
            }
        });

        // the above should be blocking so we should see a count of 5
        assertEquals(5, count.get());

        count.set(0);

        // now we'll subscribe with a scheduler and it should be async

        final String currentThreadName = Thread.currentThread().getName();

        // latches for deterministically controlling the test below across threads
        final CountDownLatch latch = new CountDownLatch(5);
        final CountDownLatch first = new CountDownLatch(1);

        o1.subscribe(new Action1<Integer>() {

            @Override
            public void call(Integer t) {
                try {
                    // we block the first one so we can assert this executes asynchronously with a count
                    first.await(1000, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException("The latch should have released if we are async.", e);
                }
                assertFalse(Thread.currentThread().getName().equals(currentThreadName));
                assertTrue(Thread.currentThread().getName().startsWith("RxComputationThreadPool"));
                System.out.println("Thread: " + Thread.currentThread().getName());
                System.out.println("t: " + t);
                count.incrementAndGet();
                latch.countDown();
            }
        }, Schedulers.threadPoolForComputation());

        // assert we are async
        assertEquals(0, count.get());
        // release the latch so it can go forward
        first.countDown();

        // wait for all 5 responses
        latch.await();
        assertEquals(5, count.get());
    }

    @Test
    public void testRecursiveScheduler1() {
        Observable<Integer> obs = Observable.create(new Func1<Observer<Integer>, Subscription>() {
            @Override
            public Subscription call(final Observer<Integer> observer) {
                return Schedulers.currentThread().schedule(0, new Func2<Scheduler, Integer, Subscription>() {
                    @Override
                    public Subscription call(Scheduler scheduler, Integer i) {
                        if (i > 42) {
                            observer.onCompleted();
                            return Subscriptions.empty();
                        }

                        observer.onNext(i);

                        return scheduler.schedule(i + 1, this);
                    }
                });
            }
        });

        final AtomicInteger lastValue = new AtomicInteger();
        obs.forEach(new Action1<Integer>() {

            @Override
            public void call(Integer v) {
                System.out.println("Value: " + v);
                lastValue.set(v);
            }
        });

        assertEquals(42, lastValue.get());
    }

    @Test
    public void testRecursiveScheduler2() throws InterruptedException {
        // use latches instead of Thread.sleep
        final CountDownLatch latch = new CountDownLatch(10);
        final CountDownLatch completionLatch = new CountDownLatch(1);

        Observable<Integer> obs = Observable.create(new Func1<Observer<Integer>, Subscription>() {
            @Override
            public Subscription call(final Observer<Integer> observer) {

                return Schedulers.threadPoolForComputation().schedule(new BooleanSubscription(), new Func2<Scheduler, BooleanSubscription, Subscription>() {
                    @Override
                    public Subscription call(Scheduler scheduler, BooleanSubscription cancel) {
                        if (cancel.isUnsubscribed()) {
                            observer.onCompleted();
                            completionLatch.countDown();
                            return Subscriptions.empty();
                        }

                        observer.onNext(42);
                        latch.countDown();

                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        scheduler.schedule(cancel, this);

                        return cancel;
                    }
                });
            }
        });

        @SuppressWarnings("unchecked")
        Observer<Integer> o = mock(Observer.class);

        final AtomicInteger count = new AtomicInteger();
        final AtomicBoolean completed = new AtomicBoolean(false);
        Subscription subscribe = obs.subscribe(new Observer<Integer>() {
            @Override
            public void onCompleted() {
                System.out.println("Completed");
                completed.set(true);
            }

            @Override
            public void onError(Exception e) {
                System.out.println("Error");
            }

            @Override
            public void onNext(Integer args) {
                count.incrementAndGet();
                System.out.println(args);
            }
        });

        if (!latch.await(5000, TimeUnit.MILLISECONDS)) {
            fail("Timed out waiting on onNext latch");
        }

        // now unsubscribe and ensure it stops the recursive loop
        subscribe.unsubscribe();
        System.out.println("unsubscribe");

        if (!completionLatch.await(5000, TimeUnit.MILLISECONDS)) {
            fail("Timed out waiting on completion latch");
        }

        assertEquals(10, count.get()); // wondering if this could be 11 in a race condition (which would be okay due to how unsubscribe works ... just it would make this test non-deterministic)
        assertTrue(completed.get());
    }

}
