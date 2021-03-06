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

package com.hazelcast.spi.merge;

import com.hazelcast.spi.impl.merge.AbstractSplitBrainMergePolicy;
import com.hazelcast.spi.impl.merge.SplitBrainDataSerializerHook;

/**
 * Merges data structure entries from source to destination data structure if the source entry
 * has more hits than the destination one.
 *
 * @since 3.10
 */
public class HigherHitsMergePolicy extends AbstractSplitBrainMergePolicy {

    public HigherHitsMergePolicy() {
    }

    @Override
    public <V> V merge(MergingValue<V> mergingValue, MergingValue<V> existingValue) {
        checkInstanceOf(mergingValue, MergingHits.class);
        checkInstanceOf(existingValue, MergingHits.class);
        if (mergingValue == null) {
            return existingValue.getValue();
        }
        if (existingValue == null) {
            return mergingValue.getValue();
        }
        MergingHits merging = (MergingHits) mergingValue;
        MergingHits existing = (MergingHits) existingValue;
        if (merging.getHits() >= existing.getHits()) {
            return mergingValue.getValue();
        }
        return existingValue.getValue();
    }

    @Override
    public int getId() {
        return SplitBrainDataSerializerHook.HIGHER_HITS;
    }
}
