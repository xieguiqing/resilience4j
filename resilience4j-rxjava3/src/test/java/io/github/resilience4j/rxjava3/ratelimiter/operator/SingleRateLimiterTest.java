package io.github.resilience4j.rxjava3.ratelimiter.operator;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.rxjava3.ratelimiter.operator.RateLimiterOperator;
import io.github.resilience4j.rxjava3.ratelimiter.operator.SingleRateLimiter;
import io.reactivex.rxjava3.core.Single;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

/**
 * Unit test for {@link SingleRateLimiter}.
 */
public class SingleRateLimiterTest {

    private RateLimiter rateLimiter;

    @Before
    public void setUp() {
        rateLimiter = mock(RateLimiter.class, RETURNS_DEEP_STUBS);
    }

    @Test
    public void shouldEmitEvent() {
        given(rateLimiter.reservePermission()).willReturn(Duration.ofSeconds(0).toNanos());

        Single.just(1)
            .compose(RateLimiterOperator.of(rateLimiter))
            .test()
            .assertResult(1);
    }

    @Test
    public void shouldDelaySubscription() {
        given(rateLimiter.reservePermission()).willReturn(Duration.ofSeconds(1).toNanos());

        Single.just(1)
            .compose(RateLimiterOperator.of(rateLimiter))
            .test()
            .awaitDone(2, TimeUnit.SECONDS);
    }

    @Test
    public void shouldPropagateError() {
        given(rateLimiter.reservePermission()).willReturn(Duration.ofSeconds(0).toNanos());

        Single.error(new IOException("BAM!"))
            .compose(RateLimiterOperator.of(rateLimiter))
            .test()
            .assertError(IOException.class)
            .assertNotComplete();
    }

    @Test
    public void shouldEmitErrorWithRequestNotPermittedException() {
        given(rateLimiter.reservePermission()).willReturn(-1L);

        Single.just(1)
            .compose(RateLimiterOperator.of(rateLimiter))
            .test()
            .assertError(RequestNotPermitted.class)
            .assertNotComplete();
    }

}
