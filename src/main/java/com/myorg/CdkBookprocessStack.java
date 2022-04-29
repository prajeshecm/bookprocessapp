package com.myorg;

import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.services.ecr.assets.DockerImageAsset;
import software.amazon.awscdk.services.ecr.assets.DockerImageAssetProps;
import software.constructs.Construct;
/*import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;*/
// import software.amazon.awscdk.Duration;
// import software.amazon.awscdk.services.sqs.Queue;

public class CdkBookprocessStack extends Stack {
    public CdkBookprocessStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public CdkBookprocessStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // The code that defines your stack goes here

        // example resource
        // final Queue queue = Queue.Builder.create(this, "CdkBookprocessQueue")
        //         .visibilityTimeout(Duration.seconds(300))
        //         .build();
        DockerImageAssetProps imageAssetProps = DockerImageAssetProps.builder()
                .directory("../../bookprocessapp/")
                .build();
        DockerImageAsset image = new DockerImageAsset(this,"book-processImage", imageAssetProps);

    }
}
