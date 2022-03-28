package com.book;

import com.book.BookProcessStack;
import software.amazon.awscdk.App;

public final class BookApp {
    public static void main(final String[] args) {
        App app = new App();

        new BookProcessStack(app, "CdkStack");

        app.synth();
    }
}
