package com.neophob.sematrix.cli;

import java.util.Observable;
import java.util.Observer;

import com.neophob.sematrix.core.jmx.PixelControllerStatusMBean;
import com.neophob.sematrix.core.properties.ApplicationConfigurationHelper;
import com.neophob.sematrix.core.properties.ValidCommand;
import com.neophob.sematrix.core.rmi.RmiApi;
import com.neophob.sematrix.osc.model.OscMessage;

public class OscAnswerHandler implements Observer {

    private boolean answerRecieved = false;
    private RmiApi rmi;

    public OscAnswerHandler(RmiApi rmi) {
        this.rmi = rmi;
    }

    public void handleOscMessage(OscMessage msg) {
        System.out.println("got: " + msg);

        ValidCommand command;
        try {
            command = ValidCommand.valueOf(msg.getPattern());
        } catch (Exception e) {
            System.out.println("Unknown message: " + msg.getPattern());
            return;
        }

        switch (command) {
            case GET_VERSION:
                String version = rmi.reassembleObject(msg.getBlob(), String.class);
                System.out.println("PixelController Version: " + version);
                break;

            case GET_JMXSTATISTICS:
                PixelControllerStatusMBean jmxStatistics = rmi.reassembleObject(msg.getBlob(),
                        PixelControllerStatusMBean.class);
                System.out.println(jmxStatistics);
                break;

            case GET_CONFIGURATION:
                ApplicationConfigurationHelper config = rmi.reassembleObject(msg.getBlob(),
                        ApplicationConfigurationHelper.class);
                System.out.println(config);
                break;

            default:
                System.out.println("Unsupported answer: " + command);
                break;
        }

        this.answerRecieved = true;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof OscMessage) {
            OscMessage msg = (OscMessage) arg;
            handleOscMessage(msg);
        } else {
            System.out.println("Ignored notification of unknown type: " + arg);
        }
    }

    public boolean isAnswerRecieved() {
        return answerRecieved;
    }

}