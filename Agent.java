/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.synapse.unittest;

import javafx.util.Pair;
import org.apache.log4j.Logger;
import org.apache.synapse.config.SynapseConfiguration;

/**
 * Class responsible for receiving test data and and maintaining the test execution flow
 */
public class Agent extends Thread{
    private static Agent agent = null;

    public static synchronized Agent getInstance() {

        if (agent == null) {
            agent = new Agent();
        }
        return agent;
    }

    private static Logger log = Logger.getLogger(Agent.class.getName());
    private TCPServer tcpServer = new TCPServer();
    private SynapseConfiguration synapseConfiguration = new SynapseConfiguration();

    /**
     * Method for initializing the TCPServer instance
     */

    public void initialize() {

        tcpServer.readData(Agent.getInstance());
    }

    /**
     * Method for maintaining the test execution flow
     *
     * @param message
     */

    public void processData(String message) {

        try {

            if (message.startsWith("|") && message.endsWith("|")) {
                String operation = MessageFormatUtils.getOperation(message);

                if (operation.equals("deploy")) {
                    String[] deploymentData = MessageFormatUtils.getDeploymentData(message);
                    String artifact = deploymentData[0];
                    String fileName = deploymentData[1];
                    Pair<SynapseConfiguration, String> pair = new Deployer().deploy(artifact, fileName);
                    synapseConfiguration = pair.getKey();
                    log.info(synapseConfiguration);
                    String key = pair.getValue();

                    if (key.equals(fileName)) {
                        String deploymentResult = "Sequence is deployed successfully";
                        log.info("Sequence deployed successfully");
                        tcpServer.writeData(MessageFormatUtils.generateResultMessage(deploymentResult));

                    } else
                        log.error("Sequence not deployed");

                } else if (operation.equals("executeTest")) {
                    log.info("Test data received unit testing begins");
                    String[] testDataValues = MessageFormatUtils.getTestData(message);
                    String inputXmlPayload = testDataValues[0];
                    String expectedPayload = testDataValues[1];
                    String expectedPropVal = testDataValues[2];

                    boolean mediationResult = new TestExecutor().sequenceMediate(inputXmlPayload, synapseConfiguration, "MySequence");
                    if (mediationResult) {
                        String unitTestResult = new TestExecutor().doAssertions(expectedPayload, expectedPropVal, inputXmlPayload);
                        tcpServer.writeData( MessageFormatUtils.generateResultMessage(unitTestResult));
                    } else {
                        String mediationError = "Sequence cannot be found";
                        tcpServer.writeData(MessageFormatUtils.generateResultMessage(mediationError));
                    }

                } else
                    log.error("Operation not identified");
            } else
                log.error("This is not a valid message");

        } catch (Exception e) {
            String deploymentError = "Exception in deploying the artifact to the synapse engine";
            tcpServer.writeData( MessageFormatUtils.generateResultMessage(deploymentError));
        }
    }
}
