package concurrency.cache;

import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        NonceFinder nonceFinder = new NonceFinder();
        byte[] nonce = nonceFinder.compute("aaa");
        System.out.println(Arrays.toString(nonce));
    }
}
