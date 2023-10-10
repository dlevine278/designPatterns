package org.dplevine.patterns.pipeline;


import org.junit.jupiter.api.Test;

public class StageWrapperTest {
    StageWrapper stageWrapper;
    ExecutionContext mContext;
    Stage mStage;


    public static void initClass() {

    }


    public void init() {
        //mContext = mock(ExecutionContext.class);
        //mStage = mock();
    }

    //private ExecutionContext mock(Class<ExecutionContext> executionContextClass) {
    //}

    @Test
    void constructorTest() {

        try {
            //when(mStage.doWork(mContext)).thenReturn(mContext);
            stageWrapper = new StageWrapper("test");
            //assertTrue(mContext == stageWrapper.doWork(mContext), "");

            stageWrapper = new StageWrapper("test", mStage);
            //assertTrue(mContext == stageWrapper.doWork(mContext), "");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
