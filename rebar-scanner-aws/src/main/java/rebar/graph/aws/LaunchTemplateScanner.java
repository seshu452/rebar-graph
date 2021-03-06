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

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.AmazonEC2Exception;
import com.amazonaws.services.ec2.model.DescribeLaunchTemplatesRequest;
import com.amazonaws.services.ec2.model.DescribeLaunchTemplatesResult;
import com.amazonaws.services.ec2.model.LaunchTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;

public class LaunchTemplateScanner extends AwsEntityScanner<LaunchTemplate, AmazonEC2Client> {

	@Override
	public void doScan() {
		scanLaunchTemplates();

	}

	@Override
	public void doScan(JsonNode entity) {
		if (isEntityType(entity, "AwsLaunchTemplate")) {
			scanLaunchTemplateByName(entity.path("name").asText());
		}

	}

	protected Optional<String> toArn(LaunchTemplate awsEntity) {
		return Optional.ofNullable(generateStandardArn("ec2", "launch-template", awsEntity.getLaunchTemplateId()));
	}

	protected ObjectNode toJson(LaunchTemplate lt) {
		ObjectNode x = super.toJson(lt);
		x.set("name", x.path("launchTemplateName"));

		return x;
	}

	protected void project(LaunchTemplate t) {

		ObjectNode n = toJson(t);

		getGraphBuilder().nodes(getEntityTypeName()).idKey("arn").properties(n).merge();
	}

	public void scanLaunchTemplateByName(String name) {
		try {
			AmazonEC2 ec2 = getClient(AmazonEC2ClientBuilder.class);

			DescribeLaunchTemplatesRequest request = new DescribeLaunchTemplatesRequest();
			request.withLaunchTemplateNames(name);
			DescribeLaunchTemplatesResult result = ec2.describeLaunchTemplates(request);

			result.getLaunchTemplates().stream().findFirst().ifPresent(it -> {
				project(it);
			});
		} catch (AmazonEC2Exception e) {
			if (e.getErrorCode().contains("InvalidLaunchTemplate")) {
				getGraphBuilder().nodes(getEntityTypeName())
						.id("name", name, "region", getRegionName(), "account", getAccount()).delete();
			} else {
				throw e;
			}
		}
	}

	public void scanLaunchTemplates() {
		long ts = getGraphBuilder().getTimestamp();

		AmazonEC2 ec2 = getClient(AmazonEC2ClientBuilder.class);

		DescribeLaunchTemplatesRequest request = new DescribeLaunchTemplatesRequest();

		do {
			DescribeLaunchTemplatesResult result = ec2.describeLaunchTemplates(request);
			result.getLaunchTemplates().forEach(it -> {

				project(it);
			});
			request.setNextToken(result.getNextToken());
		} while (!Strings.isNullOrEmpty(request.getNextToken()));

		gc(getEntityTypeName(), ts);
	}

	@Override
	public void doScan(String id) {
		checkScanArgument(id);
		scanLaunchTemplateByName(id);

	}

	@Override
	public AwsEntityType getEntityType() {
		return AwsEntityType.AwsLaunchTemplate;
	}

	@Override
	protected void doMergeRelationships() {
		// TODO Auto-generated method stub

	}

	@Override
	protected AmazonEC2Client getClient() {
		return getClient(AmazonEC2ClientBuilder.class);
	}

}
