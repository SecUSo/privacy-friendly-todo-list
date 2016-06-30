package org.secuso.privacyfriendlytodolist.model;


public class Tuple<A, B> {

    public static <P, Q> Tuple<P, Q> makePair(P p, Q q) {
        return new Tuple<P, Q>(p, q);
    }

    private final A a;
    private final B b;

    public Tuple(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public A getLeft() {
        return a;
    }

    public  B getRight() {
        return b;
    }
}

