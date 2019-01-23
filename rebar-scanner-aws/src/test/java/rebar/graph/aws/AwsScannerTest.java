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

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import rebar.util.Sleep;

public class AwsScannerTest extends AwsIntegrationTest {

	public void testScannerType() {
		Assertions.assertThat(getAwsScanner().getScannerType()).isEqualTo("aws");
	}
	@Test
	public void testIt() {

		Assertions.assertThat(getAwsScanner()).isSameAs(getAwsScanner());

		Assertions.assertThat((Object) getAwsScanner().getClient(AmazonEC2ClientBuilder.class))
				.isSameAs(getAwsScanner().getClient(AmazonEC2ClientBuilder.class));

		
		
	}

	@Test
	public void testX() {

		getAwsScanner().scan("aws", getAwsScanner().getAccount(), getAwsScanner().getRegion().getName(), "ami", "aa");
	}

	void assertScanner(String name, Class type) {
		AwsEntityScanner s = getAwsScanner().getEntityScannerForType(name);
		Assertions.assertThat(s.getClass()).isSameAs(type);
	}

	@Test
	public void testEntityScanners() {
		assertScanner("securitygroup", SecurityGroupScanner.class);
		assertScanner("vpc", VpcScanner.class);
		assertScanner("ami", AmiScanner.class);

	}

	@Test
	public void testAll() {
		getAwsScanner().scan();

		long ts = System.currentTimeMillis();
		getNeo4jDriver().cypher("match (a) where labels(a)[0]=~'Aws.*' return a").stream().forEach(it -> {
			Assertions.assertThat(it.path("graphEntityType").asText()).startsWith("Aws");
			Assertions.assertThat(it.path("graphEntityGroup").asText()).isEqualTo(getAwsScanner().getScannerType());
			Assertions.assertThat(it.path("graphUpdateTs").asLong()).isCloseTo(System.currentTimeMillis(),
					Offset.offset(TimeUnit.MINUTES.toMillis(5)));

			Assertions.assertThat(it.path("graphEntityGroup").asText()).isEqualTo("aws");
			if (ImmutableList.of("AwsAccount", "AwsRegion", "AwsAvailabilityZone")
					.contains(it.path("graphEntityType").asText())) {

			} else {

				String arn = it.path("arn").asText();
				Assertions.assertThat(it.path("arn").asText())
						.as("%s should have an arn %s", it.path("graphEntityType").asText(), it.toString())
						.startsWith("arn:aws:");
				Assertions.assertThat(it.has("region")).as("missing region attribute: %s", arn).isTrue();
				Assertions.assertThat(it.has("account")).as("missing account attribute: %s", arn).isTrue();
				Assertions.assertThat(it.path("region").asText()).isEqualTo(getAwsScanner().getRegion().getName());

				if (!it.path("graphEntityType").asText().equals("AwsAmi")) {
					// account on AMI doesn't necessarily match since AMIs are widely shared cross-account
					Assertions.assertThat(it.path("account").asText()).as(arn).isEqualTo(getAwsScanner().getAccount());
				}
			}
		});

		getNeo4jDriver().cypher(
				"match (a) where labels(a)[0]=~'Aws.*' return labels(a)[0] as label,a.graphEntityType as graphEntityType")
				.forEach(it -> {
					Assertions.assertThat(it.path("graphEntityType").asText()).isEqualTo(it.path("label").asText());
				});

		// This is a bit monolithic, but it is helpful to keep track of all the
		// relationships
		List<String> validRelationships = Lists.newArrayList();
		validRelationships.add("AwsVpc RESIDES_IN AwsRegion");
		validRelationships.add("AwsAccount HAS AwsVpc");
		validRelationships.add("AwsRegion HAS AwsAvailabilityZone");
		validRelationships.add("AwsSubnet RESIDES_IN AwsAvailabilityZone");
		validRelationships.add("AwsEc2Instance USES AwsAmi");
		validRelationships.add("AwsEc2Instance USES AwsSecurityGroup");
		validRelationships.add("AwsEc2Instance RESIDES_IN AwsSubnet");
		validRelationships.add("AwsElb ATTACHED_TO AwsAsg");
		validRelationships.add("AwsElb RESIDES_IN AwsSubnet");
		validRelationships.add("AwsElb DISTRIBUTES_TRAFFIC_TO AwsEc2Instance");
		validRelationships.add("AwsElb DISTRIBUTES_TRAFFIC_TO AwsElbTargetGroup");
		validRelationships.add("AwsElb HAS AwsElbListener");
		validRelationships.add("AwsAsg USES AwsLaunchTemplate");
		validRelationships.add("AwsVpc HAS AwsSubnet");
		validRelationships.add("AwsVpc HAS AwsSecurityGroup");
		validRelationships.add("AwsAsg LAUNCHES_INSTANCES_IN AwsSubnet");
		validRelationships.add("AwsAsg USES AwsLaunchConfig");
		validRelationships.add("AwsAsg HAS AwsEc2Instance");

		validRelationships.add("AwsEksCluster RESIDES_IN AwsSubnet");
		validRelationships.add("AwsEksCluster USES AwsSecurityGroup");
		getNeo4jDriver().cypher(
				"match (a)-[r]->(b) where labels(a)[0]=~'Aws.*' return a.graphEntityType as fromLabel,r,b.graphEntityType as toLabel,type(r) as relType")
				.forEach(it -> {
					String rel = it.path("fromLabel").asText() + " " + it.path("relType").asText() + " "
							+ it.path("toLabel").asText();
					Assertions.assertThat(validRelationships).contains(rel);
				});

	}

}
