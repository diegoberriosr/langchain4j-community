package dev.langchain4j.community.model.zhipu.tokenizer;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import dev.langchain4j.community.model.zhipu.chat.Message;
import dev.langchain4j.community.model.zhipu.chat.Tool;
import java.util.List;

@JsonInclude(NON_NULL)
@JsonNaming(SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class TokenizerRequest {

    private final String model;
    private final List<Message> messages;
    private final List<Tool> tools;
    private final String userId;

    private TokenizerRequest(Builder builder) {
        this.model = builder.model;
        this.messages = builder.messages;
        this.tools = builder.tools;
        this.userId = builder.userId;
    }

    public String getModel() {
        return model;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public List<Tool> getTools() {
        return tools;
    }

    public String getUserId() {
        return userId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String model;
        private List<Message> messages;
        private List<Tool> tools;
        private String userId;

        private Builder() {}

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder messages(List<Message> messages) {
            this.messages = messages;
            return this;
        }

        public Builder tools(List<Tool> tools) {
            this.tools = tools;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public TokenizerRequest build() {
            return new TokenizerRequest(this);
        }
    }
}
