/**
 * Copyright 2018 Rob Schoening
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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import rebar.graph.neo4j.GraphDriver;
import rebar.util.EnvConfig;

public abstract class ScannerModule {

	

	static Logger logger = LoggerFactory.getLogger(ScannerModule.class);
	ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder()
			.setNameFormat("graph-%d").setUncaughtExceptionHandler(this::handleException).build());

	EnvConfig config = new EnvConfig();

	RebarGraph rebarGraph;

	
	public ScannerModule() {

	}



	public RebarGraph getRebarGraph() {
		return rebarGraph;
	}

	public ScheduledExecutorService getExecutor() {
		return executor;
	}

	public EnvConfig getConfig() {
		return config;
	}

	



	
	protected void handleException(Thread thread, Throwable throwable) {
		logger.warn("uncaught exception", throwable);
	}

	protected void registerScannerTarget(String target, String region) {
		Runnable r = new Runnable() {

			@Override
			public void run() {
				try {
				GraphDriver neo4j = getRebarGraph().getGraphDB().getNeo4jDriver();
				
				String r = Strings.isNullOrEmpty(region) ? "undefined" : region;
				neo4j.cypher(
						"merge (a:RebarScannerTarget {type:{type},target:{target},region:{region}}) ON MATCH set a.pingTs=timestamp() ON CREATE set a.pingTs=timestamp(),a.fullScanEnabled=true,a.fullScanIntervalSecs=300")
						.param("type", getScannerType()).param("target", target).param("region", r)
						.exec();
				
				neo4j.cypher("match (s:RebarScanner {id:{id}}),(t:RebarScannerTarget {type:{type},target:{target},region:{region}}) merge (s)-[r:SCANS]->(t) ")
						.param("id",getScannerId())
						.param("type",getScannerType())
						.param("target",target)
						.param("region",r).exec();
				}
				catch (RuntimeException e) {
					logger.warn("unexpected exception",e);
				}
			}

		};
		r.run(); // make sure we have one synchronous execution
		executor.scheduleWithFixedDelay(r,30, 30, TimeUnit.SECONDS);
		
	}
	protected void registerScanner(String type) {

		Runnable r = new Runnable() {

			@Override
			public void run() {
				try {
				GraphDriver neo4j = getRebarGraph().getGraphDB().getNeo4jDriver();
				String hostname = InetAddress.getLocalHost().getCanonicalHostName();
				String ipAddress = InetAddress.getLocalHost().getHostAddress();
				neo4j.cypher(
						"merge (a:RebarScanner {id:{id}}) set a.type={type},a.hostname={hostname},a.ipAddress={ipAddress},a.pingTs=timestamp()")
						.param("hostname", hostname).param("ipAddress", ipAddress).param("id", getRebarGraph().getScannerId()).param("type", type)
						.exec();
				
				}
				catch (UnknownHostException e) {
					logger.warn("unexpected exception",e);
				}
			}

		};
		executor.scheduleWithFixedDelay(r, 0, 30, TimeUnit.SECONDS);

	}

	public String getScannerId() {
		return getRebarGraph().getScannerId();
	}
	
	public String getScannerType() {
		return getClass().getPackage().getName().replace("rebar.graph.", "");
	}
	
	protected abstract  void init();
}
