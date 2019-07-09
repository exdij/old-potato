package com.github.rosjava.android_apps.teleop;
import android.os.Trace;

import org.apache.commons.logging.Log;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;



public class ResponseInterpreter {
    private RobotState robotState;

    private final char CONST_RESPONSE_STATE='S';
    private final char CONST_RESPONSE_DISTANCE_FRONT = 'F';
    private final char CONST_RESPONSE_LIMIT_SWITCHES = 'L';
    private final char CONST_RESPONSE_DISTANCE_READ = 'R';
    private final char CONST_RESPONSE_FLOOR = 'P';
    private final char CONST_RESPONSE_POSITION = 'O';

    final int CONST_STATE_MOVEMENT = 70;
    final int CONST_STATE_STOP = 71;
    final int CONST_STATE_STAIRS = 72;
    final int CONST_STATE_OBSTACLE = 73;


    public volatile boolean Started = FALSE;

    public volatile boolean ObstacleDetected=FALSE;
    public volatile int LimitSwitchesNumber=15;
    public volatile boolean LimitSwtichesState[]={TRUE, TRUE,TRUE,TRUE};
    public volatile int FrontSonarDistance =0;
    public volatile int RearSonarDistance = 0;

    public volatile boolean NoFloorDetected=FALSE;
    public volatile int FloorNumber = 0;
    public boolean FloorSensors[]={TRUE,TRUE,TRUE,TRUE,TRUE,TRUE,TRUE};

    public volatile boolean NeedToRefresh = FALSE;

    public ResponseInterpreter(){
        robotState = new RobotState();
    }
    public void Analyze(String response){
        java.lang.String StateValue = response.substring(1);
        int iStateValue = 0;
        try {
            iStateValue = Integer.parseInt(StateValue);
        }catch (NumberFormatException e){
            iStateValue = -1;
        }
        if(response.charAt(0) == CONST_RESPONSE_STATE){
            if(iStateValue != robotState.State){
                robotState.State = iStateValue;
                NeedToRefresh = TRUE;
            }

            switch (iStateValue) {
                case CONST_STATE_STOP:
                    Started = FALSE;
                    ObstacleDetected = FALSE;
                    NoFloorDetected = FALSE;
                    break;
                case CONST_STATE_MOVEMENT:
                    Started = TRUE;
                    ObstacleDetected = FALSE;
                    NoFloorDetected = FALSE;
                    break;
                case CONST_STATE_OBSTACLE:
                    Started = FALSE;
                    ObstacleDetected = TRUE;
                    NoFloorDetected = FALSE;
                case CONST_STATE_STAIRS:
                    Started = FALSE;
                    ObstacleDetected = FALSE;
                    NoFloorDetected = TRUE;
                default:
                    break;
            }


            }else if(response.charAt(0) == CONST_RESPONSE_DISTANCE_FRONT){
                FrontSonarDistance = iStateValue;
            }else if(response.charAt(0) == CONST_RESPONSE_DISTANCE_READ){
                RearSonarDistance = iStateValue;
            }else if(response.charAt(0) == CONST_RESPONSE_LIMIT_SWITCHES){
                LimitSwitchesNumber = iStateValue;
            }else if(response.charAt(0) == CONST_RESPONSE_FLOOR){
                FloorNumber = iStateValue;

            }
        }

    }


