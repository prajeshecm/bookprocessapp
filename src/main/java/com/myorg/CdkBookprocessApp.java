package com.myorg;

/*import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;*/

import software.amazon.awscdk.core.App;
import software.amazon.awscdk.core.StackProps;

public class CdkBookprocessApp {
    public static void main(final String[] args) {
        App app = new App();

        new CdkBookprocessStack(app, "CdkBookprocessStack", StackProps.builder()
                .build());
        app.synth();
    }
}
