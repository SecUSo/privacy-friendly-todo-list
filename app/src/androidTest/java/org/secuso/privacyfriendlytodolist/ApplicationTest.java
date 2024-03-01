package org.secuso.privacyfriendlytodolist;

import static junit.framework.TestCase.assertEquals;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4ClassRunner.class)
public class ApplicationTest {
    @Test
    public void testInstrumentationTest() throws Exception {
        assertEquals("org.secuso.privacyfriendlytodolist", InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName());
    }
}