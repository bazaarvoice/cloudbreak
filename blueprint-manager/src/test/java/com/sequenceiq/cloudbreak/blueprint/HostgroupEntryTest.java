package com.sequenceiq.cloudbreak.blueprint;

import static com.sequenceiq.cloudbreak.blueprint.HostgroupEntry.hostgroupEntry;

import org.junit.Assert;
import org.junit.Test;

public class HostgroupEntryTest {

    @Test
    public void hostgroupEntryTestWhenInitialized() {
        HostgroupEntry master = hostgroupEntry("master");
        Assert.assertEquals("master", master.getHostGroup());
    }

}