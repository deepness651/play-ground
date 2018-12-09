package uk.co.home.push.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateUserRequest {
    private String username;
    private String accessToken;

    public CreateUserRequest() { 	
    }

    public CreateUserRequest(CreateUserRequest template) {
    	this.username = template.username;
    	this.accessToken = template.accessToken;
    }

    public String getUsername() {
        return username;
    }

    public String getAccessToken() {
        return accessToken;
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
        private final CreateUserRequest object;

        private Builder(final CreateUserRequest template) {
        	this.object = template;
        }
        
        public static Builder create() {
            return new Builder(new CreateUserRequest());
        }

        public static Builder createFrom(final CreateUserRequest from) {
            return new Builder(new CreateUserRequest(from));
        }

        public CreateUserRequest build() {
            return new CreateUserRequest(object);
        }
        
        public Builder withUsername(String username) {
            object.username = username;
            return this;
        }
        
        public Builder withAccessToken(String accessToken) {
        	object.accessToken = accessToken;
        	return this;
        }
   }
}
