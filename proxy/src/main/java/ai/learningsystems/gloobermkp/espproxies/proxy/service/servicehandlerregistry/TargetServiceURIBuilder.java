package ai.learningsystems.gloobermkp.espproxies.proxy.service.servicehandlerregistry;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.springframework.util.Assert;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class TargetServiceURIBuilder
{

    private final String targetServiceBaseURL;
    private final String targetServiceEndPoint;


    public TargetServiceURIBuilder(final String targetServiceBaseURL, final String targetServiceEndPoint) {

        Assert.notNull(targetServiceBaseURL,
                "TargetServiceURIBuilder(targetServiceBaseURL, targetServiceEndPoint) called with null targetServiceBaseURL ! ");
        Assert.hasText(targetServiceBaseURL,
                "TargetServiceURIBuilder(targetServiceBaseURL, targetServiceEndPoint) called with empty or blank  targetServiceBaseURL !");
        Assert.notNull(targetServiceEndPoint,
                "TargetServiceURIBuilder(targetServiceBaseURL, targetServiceEndPoint) called with null targetServiceEndPoint !");
        Assert.hasText(targetServiceEndPoint,
                "TargetServiceURIBuilder(targetServiceBaseURL, targetServiceEndPoint) called with empty or blank targetServiceEndPoint !");

        this.targetServiceBaseURL  = targetServiceBaseURL;
        this.targetServiceEndPoint = targetServiceEndPoint;
    }


    public Optional<URI> build()
    {
        
        // If it exists suppress trailing '/' from baseUrl
        String lTargetServiceBaseURL = this.targetServiceBaseURL;
        if (lTargetServiceBaseURL.endsWith("/")) {
            lTargetServiceBaseURL = lTargetServiceBaseURL.substring(0, lTargetServiceBaseURL.length() - 1);
        }

        // If serviceEndPoint doesn't start with '/' add one
        String lTargetServiceEndPoint = this.targetServiceEndPoint;
        if (!lTargetServiceEndPoint.startsWith("/")) {
            lTargetServiceEndPoint = "/" + lTargetServiceEndPoint;
        }

        String finalUri = lTargetServiceBaseURL + lTargetServiceEndPoint;

        try {
            return Optional.of(new URI(finalUri));
        }
        catch (URISyntaxException uriSyntaxEx) {
            log.error("Service base URL : " + targetServiceBaseURL + " and service endpoint : " + targetServiceEndPoint
                    + "compose a malformed URI : " + finalUri + " !\n" + uriSyntaxEx.getMessage());
            return Optional.empty();
        }

    }

}
