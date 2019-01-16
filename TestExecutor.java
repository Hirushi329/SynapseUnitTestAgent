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

import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.mediators.base.SequenceMediator;

/**
 * Class responsible for building message context and mediating the message context through the deployed sequence and asserting the mediation result
 */
public class TestExecutor {

    /**
     * Create message context from the inputXmlPayload
     */
    public MessageContext createMessageContext(String inputXmlPayload) {

        MessageContext msgCtxt = null;

        try {
            msgCtxt = TestUtils.createLightweightSynapseMessageContext(inputXmlPayload);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return msgCtxt;
    }

    /**
     * Mediate the message through the given sequence
     */
    public boolean sequenceMediate(String inputXmlPayload, SynapseConfiguration synconfig, String key) {

        Mediator sequenceMediator = synconfig.getSequence(key);
        boolean mediationResult = sequenceMediator.mediate(createMessageContext(inputXmlPayload));

        return mediationResult;
    }

    /**
     * Asserting the payload and property values
     */
    public String doAssertions(String expectedPayload, String expectedPropVal, String inputXmlPayload) {

        MessageContext msgCtxt = createMessageContext(inputXmlPayload);

        boolean result1 = (expectedPropVal.equals(msgCtxt.getEnvelope().toString()));

        boolean result2 = (expectedPayload.equals(msgCtxt.getEnvelope().getBody().toString()));

        if (result1 && result2) {
            return "Unit Testing is Successful";
        } else {
            return "Unit testing failed";
        }

    }
}
