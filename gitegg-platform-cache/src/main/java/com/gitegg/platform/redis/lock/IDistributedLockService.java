package com.gitegg.platform.redis.lock;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁接口
 * @author GitEgg
 * @date 2022-4-10
 */
public interface IDistributedLockService {

    /**
     * 加锁
     * @param lockKey key
     */
    void lock(String lockKey);

    /**
     * 释放锁
     *
     * @param lockKey key
     */
    void unlock(String lockKey);

    /**
     * 加锁并设置有效期
     *
     * @param lockKey key
     * @param timeout 有效时间，默认时间单位在实现类传入
     */
    void lock(String lockKey, int timeout);

    /**
     * 加锁并设置有效期指定时间单位
     * @param lockKey key
     * @param timeout 有效时间
     * @param unit    时间单位
     */
    void lock(String lockKey, int timeout, TimeUnit unit);

    /**
     * 尝试获取锁，获取到则持有该锁返回true,未获取到立即返回false
     * @param lockKey
     * @return true-获取锁成功 false-获取锁失败
     */
    boolean tryLock(String lockKey);

    /**
     * 尝试获取锁，获取到则持有该锁leaseTime时间.
     * 若未获取到，在waitTime时间内一直尝试获取，超过watiTime还未获取到则返回false
     * @param lockKey   key
     * @param waitTime  尝试获取时间
     * @param leaseTime 锁持有时间
     * @param unit      时间单位
     * @return true-获取锁成功 false-获取锁失败
     * @throws InterruptedException
     */
    boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit)
            throws InterruptedException;

    /**
     * 锁是否被任意一个线程锁持有
     * @param lockKey
     * @return true-被锁 false-未被锁
     */
    boolean isLocked(String lockKey);
}
