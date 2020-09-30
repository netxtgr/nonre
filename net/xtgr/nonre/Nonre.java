
package net.xtgr.nonre;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Objects;

import redis.clients.jedis.Jedis;

/**
 * 
 * @author Tiger Chang
 *
 */
public class Nonre {
    public static void main(String[] args) {
        Nonre nonre = new Nonre();
        for (int i = 0; i < 100; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int round = 10000;
                    Jedis jedis = new Jedis("127.0.0.1", 6379);
                    jedis.select(1);
                    long xs[] = new long[round];
                    for (int i = 0; i < round; i++) xs[i] = nonre.next();
                    for (int i = 0; i < round; i++) {
                        String key = String.valueOf(xs[i]), value = "";
                        if (0 == jedis.setnx(key, value)) {
                            long rick = 1023 & xs[i];
                            long seri = 4095 & (xs[i] >> 10);
                            long sequ = xs[i] >> 22;
                            System.out.printf("%d-%d-%d \n", sequ, seri, rick);
                        }
                    }
                    jedis.close();
                }
            }).start();
        }

    }

    private static Nonre self;
    public static Nonre instance(int rick) {
        if (Objects.isNull(self)) self = new Nonre(rick);
        return self;
    }

    static void info() {
        Nonre.dspl(MAX_SERIE);
        Nonre.dspl(MAX_INSTA);
        Nonre.dspl(MAX_STAMP);
        Nonre.dspl(MAX_STAMP << (BIT_SERIE + BIT_INSTA) | (MAX_SERIE << BIT_INSTA) | MAX_INSTA);
        Nonre.dspl(Long.MAX_VALUE);
        Nonre.dspl(INVARIANT);
        Nonre.dspl(Nonre.instant());
        Nonre.dspl(Nonre.instant() - INVARIANT);
    }

    static void dspl(long n) {
        System.out.printf("Natural: %16s %20d %64s (%d)bits\n", Long.toHexString(n), n,
                Long.toBinaryString(n), Long.toBinaryString(n).length());
    }

    volatile long lastmsel = instant();
    volatile long series   = 0;
    volatile long instance = 0;

    synchronized long next() {
        if (System.currentTimeMillis() > lastmsel) series = 0;
        else {
            series = (series + 1) & MAX_SERIE;
            if (series == 0) while (System.currentTimeMillis() == lastmsel) {
            }
        }
        lastmsel = System.currentTimeMillis();
        long stamp = lastmsel - INVARIANT;
        if (stamp > MAX_STAMP) throw new IllegalArgumentException(String.valueOf(MAX_STAMP));
        if (instance > MAX_INSTA) throw new IllegalArgumentException(String.valueOf(MAX_INSTA));
        return stamp << (BIT_SERIE + BIT_INSTA) | series << BIT_INSTA | instance;
    }

    static long instant() {
        return System.currentTimeMillis();
    }

    static long invariant() {
        LocalDate d = LocalDate.of(2020, 1, 1);
        LocalTime t = LocalTime.of(0, 0);
        Instant i = LocalDateTime.of(d, t).toInstant(ZoneOffset.UTC);
        return i.toEpochMilli();
    }

    private Nonre(long rick) {
        String e = null;
        if (rick > MAX_INSTA) e = rick + " > " + MAX_INSTA;
        else if (rick < 0) e = rick + " < " + 0;
        if (Objects.nonNull(e)) {
            throw new IllegalArgumentException(e);
        }
        instance = rick;
    }

    private Nonre() {
        this(0);
    }

    final static int  BIT_SERIE = 12;
    final static long MAX_SERIE = -1L ^ (-1L << BIT_SERIE);

    final static int  BIT_INSTA = 10;
    final static long MAX_INSTA = -1L ^ (-1L << BIT_INSTA);

    final static int  BIT_LIMIT = 64;
    final static int  BIT_STAMP = BIT_LIMIT - BIT_INSTA - BIT_SERIE - 1;
    final static long MAX_STAMP = -1L ^ (-1L << BIT_STAMP);

    final static long INVARIANT = invariant();

}
