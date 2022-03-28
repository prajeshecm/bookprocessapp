package com.book;

import software.amazon.awscdk.services.ecr.assets.DockerImageAsset;
import software.amazon.awscdk.services.ecr.assets.DockerImageAssetProps;
import software.constructs.Construct;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.sns.Topic;
import software.amazon.awscdk.services.sns.subscriptions.SqsSubscription;
import software.amazon.awscdk.services.sqs.Queue;

public class BookProcessStack extends Stack {
    public BookProcessStack(final Construct parent, final String id) {
        this(parent, id, null);
    }

    public BookProcessStack(final Construct parent, final String id, final StackProps props) {
        super(parent, id, props);

        /*final Queue queue = Queue.Builder.create(this, "CdkQueue")
                .visibilityTimeout(Duration.seconds(300))
                .build();

        final Topic topic = Topic.Builder.create(this, "CdkTopic")
            .displayName("My First Topic Yeah")
            .build();

        topic.addSubscription(new SqsSubscription(queue));*/

        DockerImageAssetProps imageProps = DockerImageAssetProps.builder().directory("")
                .build();

        DockerImageAsset dockerImageAsset = new DockerImageAsset(this,"",imageProps);




        //vpc-0a6b1fb0b6334ce29

    }
}
