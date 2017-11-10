package ca.uwaterloo.ece.ece651projectclient;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.Set;

import static ca.uwaterloo.ece.ece651projectclient.GameState.CREATING;
import static ca.uwaterloo.ece.ece651projectclient.GameState.JOINING;
import static ca.uwaterloo.ece.ece651projectclient.GameState.OUT;
import static ca.uwaterloo.ece.ece651projectclient.VisibilityMatrixType.ASSASSIN;
import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    private Blackboard blackboard;
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("ca.uwaterloo.ece.ece651projectclient", appContext.getPackageName());
        blackboard = new ConcreteBlackboard();
        FirebaseCommunication communication = new FirebaseCommunication(blackboard);
        blackboard.userName().set("yy");
        blackboard.userTaggedBy().set("yytest");

        blackboard.currentGameId().set("-KyXnaAB8Zcrz0wrwI71");
        blackboard.gameState().set(JOINING);
        /*blackboard.numberOfPlayers().set(4);
        Set<String> other_names = new HashSet<>();
        other_names.add("yy");
        other_names.add("ym");
        blackboard.othersNames().set(other_names);
        blackboard.visibilityMatrixType().set(ASSASSIN);*/
        blackboard.gameState().set(OUT);
        blackboard.userName().set("yy");
        blackboard.currentGameId().set("-Kxsu3Ku8yQ4vmo3Gtn7");
        blackboard.gameState().set(JOINING);
        blackboard.currentGameId().set("test_2");
        blackboard.numberOfPlayers().set(4);
        blackboard.gameState().set(CREATING);
        blackboard.userName().set("yyt");
        blackboard.currentGameId().set("test_2");
        blackboard.gameState().set(JOINING);
        Log.d("done","done");
    }
}
