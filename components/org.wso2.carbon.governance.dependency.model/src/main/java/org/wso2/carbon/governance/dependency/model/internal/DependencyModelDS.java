/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.carbon.governance.dependency.model.internal;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.governance.dependency.model.DependencyModelManager;
import org.wso2.carbon.governance.dependency.model.api.DependencyModelAPI;
import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.msf4j.MicroservicesRunner;

@Component(
        name = "org.wso2.carbon.governance.dependency.model.internal.DependencyModelDS",
        immediate = true
)
public class DependencyModelDS {

    private static final Logger log = Logger.getLogger(DependencyModelDS.class);

    private static final int DEFAULT_DEPENDENCY_MODEL_SERVICE_PORT = 9123;

    /**
     * This is the activation method of DependencyModelDS. This will be called when its references are
     * satisfied.
     *
     * @param bundleContext the bundle context instance of this bundle.
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start(BundleContext bundleContext) throws Exception {
        try {
            int offset = ServiceHolder.getCarbonRuntime().getConfiguration().getPortsConfig().getOffset();
            ServiceHolder.setMicroservicesRunner(new MicroservicesRunner(DEFAULT_DEPENDENCY_MODEL_SERVICE_PORT
                    + offset).deploy(new DependencyModelAPI()));
            ServiceHolder.getMicroservicesRunner().start();
        } catch (Throwable throwable) {
            log.error("Error occured while activating the Dependency Model bundle, and starting API " +
                    "- DependencyModelAPI", throwable);
            throw throwable;
        }
    }


    /**
     * This is the deactivation method of DependencyModelDS. This will be called when this component
     * is being stopped or references are satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {
        ServiceHolder.getMicroservicesRunner().stop();
        if (log.isDebugEnabled()) {
            log.debug("Successfully stopped DependencyModelAPI");
        }
    }

    @Reference(
            name = "carbon.runtime.service",
            service = CarbonRuntime.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetCarbonRuntime"
    )
    protected void setCarbonRuntime(CarbonRuntime carbonRuntime) {
        ServiceHolder.setCarbonRuntime(carbonRuntime);
    }

    protected void unsetCarbonRuntime(CarbonRuntime carbonRuntime) {
        ServiceHolder.setCarbonRuntime(null);
    }
}
