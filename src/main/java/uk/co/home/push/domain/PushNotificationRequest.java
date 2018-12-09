package uk.co.home.push.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PushNotificationRequest {
    private String username;
    private String title;
    private String body;

    public PushNotificationRequest() { 	
    }

    public PushNotificationRequest(PushNotificationRequest template) {
    	this.username = template.username;
    	this.title = template.title;
    	this.body = template.body;
    }

    public String getUsername() {
        return username;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    @Override
    public boolean equals(final Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
    
    public static final class Builder {
        private final PushNotificationRequest object;

        private Builder(final PushNotificationRequest template) {
        	this.object = template;
        }
        
        public static Builder create() {
            return new Builder(new PushNotificationRequest());
        }

        public static Builder createFrom(final PushNotificationRequest from) {
            return new Builder(new PushNotificationRequest(from));
        }

        public PushNotificationRequest build() {
            return new PushNotificationRequest(object);
        }
        
        public Builder withUsername(String username) {
            object.username = username;
            return this;
        }
        
        public Builder withTitle(String title) {
        	object.title = title;
        	return this;
        }
        
        public Builder withBody(String body) {
        	object.body = body;
        	return this;
        }
   }
}
