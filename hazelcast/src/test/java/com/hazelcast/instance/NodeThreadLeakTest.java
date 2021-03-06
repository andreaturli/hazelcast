/*
 * Copyright (c) 2008-2018, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.instance;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Set;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastException;
import com.hazelcast.nio.tcp.TcpIpConnectionManager;
import com.hazelcast.spi.impl.NodeEngineImpl;
import com.hazelcast.spi.impl.eventservice.impl.EventServiceImpl;
import com.hazelcast.spi.impl.executionservice.impl.ExecutionServiceImpl;
import com.hazelcast.spi.impl.operationparker.impl.OperationParkerImpl;
import com.hazelcast.test.annotation.QuickTest;

/**
 * Tests that exception in Node constructor leads to properly finished services.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ DefaultNodeContext.class, NodeEngineImpl.class })
@Category(QuickTest.class)
public class NodeThreadLeakTest {

    private static final HazelcastException HAZELCAST_EXCEPTION = new HazelcastException(
            "Test exception - Emulates service failure.");

    @Test
    public void testLeakWhenCreatingConnectionManager() throws Exception {
        mockConstructorAndTest(TcpIpConnectionManager.class);
    }

    @Test
    public void testCreatingEventServiceImplFails() throws Exception {
        mockConstructorAndTest(EventServiceImpl.class);
    }

    @Test
    public void testCreatingExecutionServiceImplFails() throws Exception {
        mockConstructorAndTest(ExecutionServiceImpl.class);
    }

    @Test
    public void testCreatingOperationParkerImplFails() throws Exception {
        mockConstructorAndTest(OperationParkerImpl.class);
    }

    private void mockConstructorAndTest(Class<?> clazzToMock) throws Exception {
        PowerMockito.whenNew(clazzToMock).withAnyArguments().thenThrow(HAZELCAST_EXCEPTION);
        try {
            Config config = new Config();
            config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
            Hazelcast.newHazelcastInstance(config);
            fail("Starting the member should have failed");
        } catch (HazelcastException expected) {
        }
        Set<Thread> threads = Thread.getAllStackTraces().keySet();
        for (Thread thread : threads) {
            String threadName = thread.getName();
            assertThat(threadName, not(containsString("hz._hzInstance_")));
        }
    }

}
