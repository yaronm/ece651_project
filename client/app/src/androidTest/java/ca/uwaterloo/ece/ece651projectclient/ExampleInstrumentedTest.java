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
import static ca.uwaterloo.ece.ece651projectclient.VisibilityMatrixType.ASSASSIN;
import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("ca.uwaterloo.ece.ece651projectclient", appContext.getPackageName());
        Blackboard blackboard = new ConcreteBlackboard();
        FirebaseCommunication communication = new FirebaseCommunication(blackboard);
        blackboard.userName().set("yyt3");
        blackboard.currentGameId().set("-KyWdawxgdKGfWkhjKwq");
        blackboard.gameState().set(JOINING);/*
        blackboard.numberOfPlayers().set(4);
        Set<String> other_names = new HashSet<>();
        other_names.add("yy");
        other_names.add("ym");
        blackboard.othersNames().set(other_names);
        blackboard.visibilityMatrixType().set(ASSASSIN);
        blackboard.gameState().set(CREATING);
        Blackboard bb = new ConcreteBlackboard();
        FirebaseCommunication communication1 = new FirebaseCommunication(bb);
        bb.userName().set("yy");

        bb.currentGameId().set(blackboard.currentGameId().value());
        bb.gameState().set(JOINING);

        Blackboard bb3 = new ConcreteBlackboard();
        FirebaseCommunication communication2 = new FirebaseCommunication(bb3);

        bb3.currentGameId().set("test_2");
        bb3.numberOfPlayers().set(4);
        bb3.gameState().set(CREATING);

        Blackboard bb4 = new ConcreteBlackboard();
        FirebaseCommunication communication3 = new FirebaseCommunication(bb4);

        bb4.userName().set("yyt");
        bb4.currentGameId().set(bb3.currentGameId().value());
        bb4.gameState().set(JOINING);
        Log.d("done","done");*/
    }
}
