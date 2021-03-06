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
package rebar.graph.neo4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;

import rebar.util.Json;

class Neo4jTemplateImpl extends CypherTemplate  {

	static Logger logger = LoggerFactory.getLogger(Neo4jTemplateImpl.class);

	Driver driver;
	String cypher;
	Map<String, Object> params = new HashMap<>();
	GraphDriver graphDriver;
	
	protected Neo4jTemplateImpl copy() {
		Neo4jTemplateImpl c = new Neo4jTemplateImpl(graphDriver);
		c.resultLimit = this.resultLimit;
		c.attributeMode = this.attributeMode;
		c.cypher = this.cypher;
		c.params = new HashMap<>(params);
		return c;
	}

	Neo4jTemplateImpl(GraphDriver driver) {
		this.graphDriver = driver;
		this.driver = driver.getDriver();
	}

	/* (non-Javadoc)
	 * @see rebar.graph.neo4j.INeo4jTemplate#withAttributeMode(rebar.graph.driver.GraphTemplate.AttributeMode)
	 */
	public Neo4jTemplateImpl withAttributeMode(AttributeMode m) {

		Neo4jTemplateImpl copy = copy();
		copy.attributeMode = m;
		return copy;
	}

	/* (non-Javadoc)
	 * @see rebar.graph.neo4j.INeo4jTemplate#withMaxResults(int)
	 */
	public Neo4jTemplateImpl withMaxResults(int count) {
		Neo4jTemplateImpl copy = copy();
		copy.resultLimit = count;
		return copy;
	}

	/* (non-Javadoc)
	 * @see rebar.graph.neo4j.INeo4jTemplate#cypher(java.lang.String)
	 */
	public Neo4jTemplateImpl cypher(String cypher) {

		Neo4jTemplateImpl copy = copy();
		copy.cypher = cypher;
		return copy;
	}

	public Neo4jTemplateImpl params(Object ...kv) {
		
		if (kv==null) {
			return this;
		}
		if (kv.length %2 != 0) {
			throw new IllegalArgumentException("params must have an even number of arguments");
		}
		Map<String,Object> map = Maps.newHashMap();
		for (int i=0; i<kv.length; i+=2) {
			Object key = kv[i];
			Object val = kv[i+1];
			map.put(kv[i].toString(), val);
		}
		return params(map);
	}
	/* (non-Javadoc)
	 * @see rebar.graph.neo4j.INeo4jTemplate#param(java.lang.String, java.lang.Object)
	 */
	public Neo4jTemplateImpl param(String key, Object val) {
		Neo4jTemplateImpl copy = copy();
		if (val instanceof ObjectNode) {
			val = Json.objectMapper().convertValue(val, Map.class);
		}
		copy.params.put(key, val);
		return copy;
	}

	/* (non-Javadoc)
	 * @see rebar.graph.neo4j.INeo4jTemplate#param(com.fasterxml.jackson.databind.JsonNode)
	 */
	public Neo4jTemplateImpl params(JsonNode n) {
		return params(toParams(n));

	}

	/* (non-Javadoc)
	 * @see rebar.graph.neo4j.INeo4jTemplate#param(java.util.Map)
	 */

	@SuppressWarnings("unchecked")
	public Neo4jTemplateImpl params(Map<?, ?> vals) {
		Neo4jTemplateImpl copy = copy();

		copy.params.putAll((Map<String, Object>) vals);
		return copy;
	}

	/* (non-Javadoc)
	 * @see rebar.graph.neo4j.INeo4jTemplate#stream()
	 */
	@Override
	public Stream<JsonNode> stream() {
		return doExec().asStream();
	}

	/* (non-Javadoc)
	 * @see rebar.graph.neo4j.INeo4jTemplate#list()
	 */
	@Override
	public List<JsonNode> list() {
		return doExec().asList();
	}
	
	/* (non-Javadoc)
	 * @see rebar.graph.neo4j.INeo4jTemplate#exec()
	 */
	@Override
	public void exec() {
		list();
	}

	@SuppressWarnings("unchecked")
	Map<String, Object> toParams(JsonNode n) {
		if (n == null) {
			return new HashMap<>();
		}
		return Neo4jDriverImpl.mapper.convertValue(n, Map.class);
	}

	private Neo4jStatementResultImpl doExec() {
		
		long ts = System.currentTimeMillis();
		try (Session session = driver.session()) {

			
			Neo4jTemplateImpl finalTemplate = copy();

			finalTemplate.params = new HashMap<>(finalTemplate.params);
			if (finalTemplate.params.isEmpty()) {
				finalTemplate.params.put("__params", new HashMap<>());
			}
			else {
				
				finalTemplate.params.put("__params", this.params);
			}
			

			if (logger.isDebugEnabled()) {
				logger.debug("cypher: {} params: {}", finalTemplate.cypher, finalTemplate.params);
			}
			
			StatementResult sr = session.run(finalTemplate.cypher, finalTemplate.params);
			Neo4jStatementResultImpl nsr = new Neo4jStatementResultImpl(finalTemplate);
			nsr.consume(sr);
			long totalTime = System.currentTimeMillis()-ts;
			graphDriver.metrics().recordStatementExecution(finalTemplate.cypher, totalTime);
			return nsr;
		}

	}


	static String toMatchPattern(Object... attrs) {
		StringBuffer sb = new StringBuffer();
		if (!(attrs != null && attrs.length > 0)) {
			throw new IllegalArgumentException("identifying attributes must be set");
		}
		if (attrs != null && attrs.length > 0) {

			int count = 0;
			sb.append("{");
			for (Object attr : attrs) {
				if (count > 0) {
					sb.append(", ");
				}
				sb.append(attr);
				sb.append(":{");
				sb.append(attr);
				sb.append("} ");
				count++;
			}
			sb.append("}");
		}
		return sb.toString();
	}

	@Override
	public Optional<JsonNode> findFirst() {
		return stream().findFirst();
	}

	@Override
	public void forEach(Consumer<JsonNode> c) {
		stream().forEach(c);
		
	}

}
