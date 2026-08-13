package dev.langchain4j.community.model.zhipu.tokenizer;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import dev.langchain4j.community.model.zhipu.shared.ErrorResponse;

@JsonInclude(NON_NULL)
@JsonNaming(SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class TokenizerResponse extends ErrorResponse {

    private String id;
    private TokenizerUsage usage;
    private Long created;
    private String requestId;

    public TokenizerResponse() {}

    private TokenizerResponse(Builder builder) {
        this.id = builder.id;
        this.usage = builder.usage;
        this.created = builder.created;
        this.requestId = builder.requestId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TokenizerUsage getUsage() {
        return usage;
    }

    public void setUsage(TokenizerUsage usage) {
        this.usage = usage;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String id;
        private TokenizerUsage usage;
        private Long created;
        private String requestId;

        private Builder() {}

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder usage(TokenizerUsage usage) {
            this.usage = usage;
            return this;
        }

        public Builder created(Long created) {
            this.created = created;
            return this;
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public TokenizerResponse build() {
            return new TokenizerResponse(this);
        }
    }
}
