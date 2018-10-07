package com.microsoft.bot;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.microsoft.aad.adal4j.AuthenticationException;
import com.microsoft.bot.connector.authentication.CredentialProvider;
import com.microsoft.bot.connector.authentication.CredentialProviderImpl;
import com.microsoft.bot.connector.authentication.JwtTokenValidation;
import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;
import com.microsoft.bot.connector.implementation.ConnectorClientImpl;
import com.microsoft.bot.schema.models.Activity;
import com.microsoft.bot.schema.models.ActivityTypes;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {
    private static final Logger LOGGER = Logger.getLogger(Function.class.getName());
    private static String appId = "<-- app id -->";
    private static String appPassword = "<-- app password -->";

    /**
     * This function listens at endpoint "/api/messages".
     */
    @FunctionName("messages")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) 
            HttpRequestMessage<String> request,
            final ExecutionContext context) {

            CredentialProvider credentialProvider = new CredentialProviderImpl(appId, appPassword);

            MessageHandle M=new MessageHandle(credentialProvider);
            HttpResponseMessage RM=M.handle(request);
            return RM;             
    }

    static class MessageHandle  {
        private ObjectMapper objectMapper;
        private CredentialProvider credentialProvider;
        private MicrosoftAppCredentials credentials;

        MessageHandle(CredentialProvider credentialProvider) {
            
            this.objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .findAndRegisterModules();
            this.credentialProvider = credentialProvider;
            this.credentials = new MicrosoftAppCredentials(appId, appPassword);
        }

        public HttpResponseMessage handle(HttpRequestMessage request)  {
            HttpResponseMessage httpResponseMessage=request.createResponseBuilder(HttpStatus.OK).build();

            //if (httpExchange.getRequestMethod().equalsIgnoreCase("POST")) {
                Activity activity = getActivity(request);

                String authHeader = request.getHeaders().get("authorization").toString();
            try {
                JwtTokenValidation.authenticateRequest(activity, authHeader, credentialProvider);

                // send ack to user activity
                    httpResponseMessage=request.createResponseBuilder(HttpStatus.OK).build();
                    // httpResponseMessage.sendResponseHeaders(202, 0);

                    LOGGER.info("ACTIVITY TYPE" + ActivityTypes.MESSAGE);
                    LOGGER.info(activity.text());
                    if (activity.type().equals(ActivityTypes.MESSAGE)) {

                        String activityText = activity.text();

                        ConnectorClientImpl connector = new ConnectorClientImpl(activity.serviceUrl(),this.credentials);
    
                        connector.conversations().sendToConversation(
                                activity.conversation().id(),
                                new Activity().withType(ActivityTypes.MESSAGE)
                                        .withText("You said" + activityText)
                                        .withRecipient(activity.from())
                                        .withFrom(activity.recipient()));
                           
                    }
                } catch (AuthenticationException ex) {
                    httpResponseMessage=request.createResponseBuilder(HttpStatus.UNAUTHORIZED).build();
                    LOGGER.log(Level.WARNING, "Auth failed!", ex);
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "Execution failed", ex);
                    System.out.println("Error");
                    ex.printStackTrace();
                }
            //}
            return httpResponseMessage;
        }


        private Activity getActivity(HttpRequestMessage request) {
            try {
                String body = request.getBody().toString();

                return objectMapper.readValue(body, Activity.class);
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Failed to get activity", ex);
                return null;
            }

        }
    }

}
