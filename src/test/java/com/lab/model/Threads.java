package com.lab.model;

import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.control.LoopController;
import org.apache.jorphan.collections.ListedHashTree;

public class Threads {

    public static void main(String[] args) {
        org.apache.jmeter.util.JMeterUtils.loadJMeterProperties("C:\\Users\\larisabalc\\Downloads\\apache-jmeter-5.6.3\\apache-jmeter-5.6.3\\bin\\jmeter.properties");
        org.apache.jmeter.util.JMeterUtils.initLocale();

        TestPlan testPlan = new TestPlan();
        testPlan.setName("Simultaneous Threads Test");

        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setName("Simultaneous Thread Group");
        threadGroup.setNumThreads(200000);
        threadGroup.setRampUp(1);
        threadGroup.setSamplerController(new LoopController());

        HTTPSampler httpSampler = new HTTPSampler();
        httpSampler.setName("Simultaneous HTTP Request");
        httpSampler.setDomain("localhost");
        httpSampler.setPort(8080);
        httpSampler.setPath("/login");
        httpSampler.setMethod("GET");

        ListedHashTree threadGroupHashTree = new ListedHashTree();
        threadGroupHashTree.add(threadGroup, httpSampler);

        ListedHashTree testPlanHashTree = new ListedHashTree();
        testPlanHashTree.add(testPlan, threadGroupHashTree);

        StandardJMeterEngine jmeter = new StandardJMeterEngine();
        jmeter.configure(testPlanHashTree);
        jmeter.run();
    }
}
