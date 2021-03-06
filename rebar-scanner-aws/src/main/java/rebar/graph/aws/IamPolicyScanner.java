package rebar.graph.aws;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.identitymanagement.model.GetPolicyRequest;
import com.amazonaws.services.identitymanagement.model.GetPolicyResult;
import com.amazonaws.services.identitymanagement.model.GetUserRequest;
import com.amazonaws.services.identitymanagement.model.GetUserResult;
import com.amazonaws.services.identitymanagement.model.ListPoliciesRequest;
import com.amazonaws.services.identitymanagement.model.ListPoliciesResult;
import com.amazonaws.services.identitymanagement.model.ListUsersRequest;
import com.amazonaws.services.identitymanagement.model.ListUsersResult;
import com.amazonaws.services.identitymanagement.model.ManagedPolicyDetail;
import com.amazonaws.services.identitymanagement.model.NoSuchEntityException;
import com.amazonaws.services.identitymanagement.model.Policy;
import com.amazonaws.services.identitymanagement.model.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.machinezoo.noexception.Exceptions;

import rebar.graph.core.GraphBuilder;
import rebar.graph.core.GraphBuilder.NodeOperation;
import rebar.util.Json;

public class IamPolicyScanner extends AwsEntityScanner<Policy,AmazonIdentityManagementClient> {

	public AmazonIdentityManagementClient getClient() {
		return (AmazonIdentityManagementClient) getClient(AmazonIdentityManagementClientBuilder.class);

	}

	protected void project(Policy policy) {
		JsonNode n = toJson(policy);

		awsGraphNodesWithoutRegion().idKey("arn").properties(n).merge();

	}
	@Override
	protected void doMergeRelationships() {
		mergeAccountOwner();
	}

	@Override
	protected ObjectNode toJson(Policy awsObject) {

		ObjectNode n = super.toJson(awsObject);
		n.remove("region"); // user is not regional
		
		return n;
	}

	@Override
	protected void doScan() {

		long ts = getGraphBuilder().getTimestamp();
		ListPoliciesRequest request = new ListPoliciesRequest();
		do {
			ListPoliciesResult result = getClient().listPolicies(request);
			result.getPolicies().forEach(it -> {
				tryExecute(()->project(it));
			});
			request.setMarker(result.getMarker());
			if (result.isTruncated()) {
				request.setMarker(null);
			}
		} while (!Strings.isNullOrEmpty(request.getMarker()));
		gcWithoutRegion(AwsEntityType.AwsIamPolicy.name(),ts); // do not match on region

		doMergeRelationships();
	}

	@Override
	public void doScan(JsonNode entity) {
		if (isEntityType(entity)) {

		
			String name = entity.path("arn").asText();
		
			doScan(name);
		}

	}

	@Override
	public void doScan(String arn) {
		checkScanArgument(arn);
		try {
			GetPolicyResult result = getClient().getPolicy(new GetPolicyRequest().withPolicyArn(arn));
			project(result.getPolicy());
			doMergeRelationships();
		} catch (NoSuchEntityException e) {
			deleteByArn(arn);
		}
	}

	private void deleteByArn(String arn) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(arn));
		logger.info("deleting AwsIamPolicy arn={}",arn);
		getGraphBuilder().nodes(AwsEntityType.AwsIamPolicy.name()).id("account",getAccount()).id("arn",arn).delete();
	}
	@Override
	public AwsEntityType getEntityType() {
		return AwsEntityType.AwsIamPolicy;
	}

}
