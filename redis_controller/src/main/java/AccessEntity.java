// package main.java;

import lombok.Setter;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.Getter;

@Getter
@Setter
@RedisHash("access")
public class AccessEntity {
    @Id
    String location;
    int accessCount;

    public AccessEntity(String location) {
        this.location = location;
    }

}
