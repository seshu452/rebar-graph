/**
 * Copyright 2018-2019 Rob Schoening
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rebar.graph.core;

import static rebar.graph.neo4j.GraphDriver.GRAPH_PASSWORD;
import static rebar.graph.neo4j.GraphDriver.GRAPH_URL;
import static rebar.graph.neo4j.GraphDriver.GRAPH_USERNAME;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import rebar.graph.neo4j.GraphDriver;
import rebar.graph.neo4j.GraphException;
import rebar.util.EnvConfig;
import rebar.util.RebarException;

public class RebarGraph {

	static org.slf4j.Logger logger = LoggerFactory.getLogger(RebarGraph.class);

	static ApplicationContext applicationContext;
	
	Map<String, Supplier<? extends Scanner>> supplierMap = Maps.newConcurrentMap();

	GraphBuilder graphWriter;

	EnvConfig env = null;

	ScanQueue queue;

	String scannerId = UUID.randomUUID().toString();

	AtomicBoolean running = new AtomicBoolean(true);
	private RebarGraph() {

	}

	public static class Builder {

		GraphBuilder graphDb;

		EnvConfig env = new EnvConfig();

		String scannerId;

		public Builder withEnv(EnvConfig cfg) {
			this.env = cfg.copy();
			return this;
		}

		public Builder withGraphBuilder(GraphBuilder graphWriter) {
			this.graphDb = graphWriter;
			return this;
		}

		public Builder withGraphPassword(String password) {
			env = env.withEnv(GRAPH_PASSWORD, password);
			return this;
		}

		public Builder withGraphUsername(String username) {
			env = env.withEnv(GRAPH_USERNAME, username);
			return this;
		}

		public Builder withGraphUrl(String url) {
			env = env.withEnv(GRAPH_URL, url);
			return (this);
		}

		public Builder withScannerId(String id) {
			this.scannerId = id;
			return this;
		}

		public Builder withInMemoryTinkerGraph() {
			env = env.withEnv(GRAPH_URL, "memory");
			return this;
		}

		public Optional<String> getEnv(String name) {
			return env.get(name);
		}

		public RebarGraph build() {

			RebarGraph rg = new RebarGraph();

			if (graphDb != null) {
				rg.graphWriter = graphDb;
				rg.env = env;

				Neo4jScanQueue queue = new Neo4jScanQueue(graphDb.getNeo4jDriver());
				queue.start();
				rg.queue = queue;

				return rg;
			}

			Optional<String> graphUrl = getEnv(GRAPH_URL);
			if (!graphUrl.isPresent()) {

				graphUrl = Optional.of("bolt://localhost:7687");

				logger.info("GRAPH_URL not set ... defaulting to {}", graphUrl.get());
			}
			logger.info("GRAPH_URL: {}", graphUrl.orElse(""));
			if (graphUrl.isPresent()) {

				GraphDriver.Builder b = new GraphDriver.Builder().withUrl(graphUrl.get());
				if (env.get(GRAPH_USERNAME).isPresent() && env.get(GRAPH_PASSWORD).isPresent()) {
					b = b.withUsername(env.get(GRAPH_USERNAME).get()).withPassword(env.get(GRAPH_PASSWORD).get());
				}
				GraphDriver driver = b.build();
				if (driver.getClass().getName().toLowerCase().contains("neo4j")) {
					GraphBuilder gw = new GraphBuilder((GraphDriver) driver);
					rg.graphWriter = gw;
					rg.env = env;
					Neo4jScanQueue queue = new Neo4jScanQueue((GraphDriver) driver);
					queue.start();
					rg.queue = queue;
					return rg;
				} else {
					throw new GraphException("GRAPH_URL " + graphUrl.get() + " not supported");
				}

			} else {
				throw new GraphException("GRAPH_URL not set");
			}

		}
	}

	public <T extends Scanner> T newScanner(Class<T> clazz) {
		return newScanner(clazz,Maps.newHashMap());
	}
	public <T extends Scanner> T newScanner(Class<T> clazz, Map<String,String> config) {

		try {
			logger.info("creating {}",clazz.getSimpleName());
			T scanner = (T) clazz.newInstance();
			scanner._init(this,config);
			return (T) scanner;
		}
		catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RebarException(e);
		}
	}

	public GraphBuilder getGraphBuilder() {
		return graphWriter;
	}

	public String getScannerId() {
		return scannerId;
	}

	public <T extends Scanner> void registerScanner(Class<T> scannerType, String name, Supplier<T> supplier) {
		String key = scannerType.getName() + ":" + name;
		if (supplierMap.containsKey(key)) {
			throw new IllegalStateException("already exists: " + key);
		}
		supplierMap.put(key, supplier);
	}

	@SuppressWarnings("unchecked")
	public <T extends Scanner> T getScanner(Class<T> scannerType, String name) {
		Supplier<? extends Scanner> supplier = supplierMap.get(scannerType.getName() + ":" + name);
		if (supplier == null) {
			throw new RuntimeException("not found: " + name);
		}
		return (T) supplier.get();
	}

	public ScanQueue getScanQueue() {
		return queue;
	}

	public final EnvConfig getEnvConfig() {
		Preconditions.checkNotNull(env);
		return env;
	}
	
	public boolean isRunning() {
		return running.get();
	}
	
	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}
}
