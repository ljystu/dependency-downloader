package com.example.gitprojectsfilter;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

public class TestAll {
//    @Test
//    public void TestALL() {
//        int[] ho = new int[]{1, 2, 3, 4, 5};
//        System.out.println(ho.toString());
//    }


    @Test
    public void TestALL2() throws Exception {
        DependencyCollector.main(new String[]{"-a", "/Users/ljystu/Desktop/neo4j/call-graph-analysis/repo_result_per_halfyear_100_test.json"});
    }

}
