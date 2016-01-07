package org.seedstack.seed.rest.jersey2.internal;

import org.glassfish.jersey.servlet.ServletContainer;
import org.seedstack.seed.web.WebServlet;

@WebServlet("/*")
public class SeedServletContainer extends ServletContainer {

    public SeedServletContainer() {
        super(new SeedApplication());
    }
}
