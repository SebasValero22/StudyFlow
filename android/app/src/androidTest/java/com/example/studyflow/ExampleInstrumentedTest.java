package com.example.studyflow;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Test instrumentado, se ejecuta en un dispositivo Android.
 *
 * @see <a href="http://d.android.com/tools/testing">Documentacion de testing</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Contexto de la app bajo prueba
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.studyflow", appContext.getPackageName());
    }
}
