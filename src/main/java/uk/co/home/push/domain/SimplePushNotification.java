package uk.co.home.push.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SimplePushNotification {
    private String type = "note";
    private String title;
    private String body;

    public SimplePushNotification() { 	
    }

    public SimplePushNotification(SimplePushNotification template) {
    	this.type = template.type;
    	this.title = template.title;
    	this.body = template.body;
    }

    public String getType() {
        return type;
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
        private final SimplePushNotification object;

        private Builder(final SimplePushNotification template) {
        	this.object = template;
        }
        
        public static Builder create() {
            return new Builder(new SimplePushNotification());
        }

        public static Builder createFrom(final SimplePushNotification from) {
            return new Builder(new SimplePushNotification(from));
        }

        public SimplePushNotification build() {
            return new SimplePushNotification(object);
        }
        
        public Builder withType(String type) {
            object.type = type;
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
