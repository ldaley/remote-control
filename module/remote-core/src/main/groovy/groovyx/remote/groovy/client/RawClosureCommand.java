/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package groovyx.remote.groovy.client;

import groovy.lang.Closure;

import java.util.List;

public class RawClosureCommand {

    private final Closure<?> master;
    private final List<Closure<?>> used;

    public RawClosureCommand(Closure<?> master, List<Closure<?>> used) {
        this.master = master;
        this.used = used;
    }

    public Closure<?> getRoot() {
        return master;
    }

    public List<Closure<?>> getUsed() {
        return used;
    }

    @Override
    public String toString() {
        return "RawClosureCommand{" +
            "master=" + master +
            ", used=" + used +
            '}';
    }
}
