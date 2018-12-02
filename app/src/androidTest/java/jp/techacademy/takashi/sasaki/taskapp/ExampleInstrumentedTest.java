package jp.techacademy.takashi.sasaki.taskapp;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.Realm;
import io.realm.RealmResults;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("jp.techacademy.takashi.sasaki.taskapp", appContext.getPackageName());

        Realm.init(appContext);
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Task> results = realm.where(Task.class).findAll();
        for (Task task : results) {
            Log.d("JUnit", "" + task.getTitle());
        }
    }
}
