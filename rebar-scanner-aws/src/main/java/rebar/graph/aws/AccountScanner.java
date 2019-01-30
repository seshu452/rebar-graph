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
package rebar.graph.aws;

import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;

import rebar.graph.core.GraphDB;
import rebar.util.Json;

public class AccountScanner extends AwsEntityScanner {



	@Override
	public void doScan() {
	
		getAwsScanner().getRebarGraph().getGraphDB().nodes("AwsAccount").idKey("account").properties(
				Json.objectNode().put("account", getAwsScanner().getAccount()).put(GraphDB.ENTITY_TYPE, "AwsAccount").put(GraphDB.ENTITY_TYPE,"AwsAccount").put(GraphDB.ENTITY_GROUP,"aws")).merge();
	}

	@Override
	public void doScan(JsonNode entity) {
		scan();
	}
	
	protected Optional<String> toArn(Object awsEntity) {
		return Optional.empty();
	}

	@Override
	public void doScan(String id) {
		checkScanArgument(id);
		scan();	
	}

	@Override
	public AwsEntityType getEntityType() {
		return AwsEntityType.AwsAccount;
	}

	@Override
	protected void doMergeRelationships() {
		
		
	}
	
	

}
