package uk.co.home.push.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    private String username;
    private String accessToken;
    private Date creationTime;
    private Integer numOfNotificationsPushed;
    
    public User() { 	
    }
    
    public User(User template) {
    	this.username = template.username;
    	this.accessToken = template.accessToken;
    	this.creationTime = template.creationTime;
    	this.numOfNotificationsPushed = template.numOfNotificationsPushed;
    }
    
    public String getUsername() {
        return username;
    }

    public String getAccessToken() {
        return accessToken;
    }

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public Date getCreationTime() {
        return creationTime;
    }

    public Integer getNumOfNotificationsPushed() {
        return numOfNotificationsPushed;
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
        private final User object;

        private Builder(final User template) {
        	this.object = template;
        }
        
        public static Builder create() {
            return new Builder(new User());
        }

        public static Builder createFrom(final User from) {
            return new Builder(new User(from));
        }

        public User build() {
            return new User(object);
        }
        
        public Builder withUsername(String username) {
            object.username = username;
            return this;
        }
        
        public Builder withAccessToken(String accessToken) {
        	object.accessToken = accessToken;
        	return this;
        }
        
        public Builder withCreationTime(Date creationTime) {
        	object.creationTime = creationTime;
        	return this;
        }
        
        public Builder withNumOfNotificationsPushed(Integer numOfNotificationsPushed) {
        	object.numOfNotificationsPushed = numOfNotificationsPushed;
        	return this;
        }
    }
}
