package com.sequenceiq.cloudbreak.blueprint.template;

import java.io.IOException;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;

public class IfTrueHelper implements Helper<Boolean> {

    /**
     * A singleton instance of this helper.
     */
    public static final Helper<Boolean> INSTANCE = new IfTrueHelper();

    /**
     * The helper's name.
     */
    public static final String NAME = "iftrue";

    @Override
    public Object apply(Boolean context, final Options options)
            throws IOException {
        if (context == null) {
            context = false;
        }

        Options.Buffer buffer = options.buffer();
        if (!context) {
            buffer.append(options.inverse());
        } else {
            buffer.append(options.fn());
        }
        return buffer;
    }
}
