package com.revature.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExpenseControllerTest {

    @BeforeEach
    public void setUp() throws Exception {
        DummyDataLoader dataLoader = new DummyDataLoader();
        dataLoader.restoreDatabase();
    }

    @Test
    public void dummyTest() {
        // Placeholder test to ensure setup runs
        System.out.println("test");
    }
}
