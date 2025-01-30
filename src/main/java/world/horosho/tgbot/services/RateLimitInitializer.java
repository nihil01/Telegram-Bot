package world.horosho.tgbot.services;
import io.github.bucket4j.Bucket;

import java.time.Duration;

public class RateLimitInitializer {

    public static Bucket getLimitBucket(int token, int tokens, Duration duration){
        return Bucket.builder().addLimit(limit -> limit.capacity(tokens).refillGreedy(token, duration))
                .build();
    }

}
