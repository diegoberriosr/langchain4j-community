package dev.langchain4j.community.model.zhipu.tokenizer;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonInclude(NON_NULL)
@JsonNaming(SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class TokenizerUsage {

    private Integer promptTokens;
    private Integer imageTokens;
    private Integer videoTokens;
    private Integer totalTokens;

    public TokenizerUsage() {}

    private TokenizerUsage(Builder builder) {
        this.promptTokens = builder.promptTokens;
        this.imageTokens = builder.imageTokens;
        this.videoTokens = builder.videoTokens;
        this.totalTokens = builder.totalTokens;
    }

    public Integer getPromptTokens() {
        return promptTokens;
    }

    public void setPromptTokens(Integer promptTokens) {
        this.promptTokens = promptTokens;
    }

    public Integer getImageTokens() {
        return imageTokens;
    }

    public void setImageTokens(Integer imageTokens) {
        this.imageTokens = imageTokens;
    }

    public Integer getVideoTokens() {
        return videoTokens;
    }

    public void setVideoTokens(Integer videoTokens) {
        this.videoTokens = videoTokens;
    }

    public Integer getTotalTokens() {
        return totalTokens;
    }

    public void setTotalTokens(Integer totalTokens) {
        this.totalTokens = totalTokens;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private Integer promptTokens;
        private Integer imageTokens;
        private Integer videoTokens;
        private Integer totalTokens;

        private Builder() {}

        public Builder promptTokens(Integer promptTokens) {
            this.promptTokens = promptTokens;
            return this;
        }

        public Builder imageTokens(Integer imageTokens) {
            this.imageTokens = imageTokens;
            return this;
        }

        public Builder videoTokens(Integer videoTokens) {
            this.videoTokens = videoTokens;
            return this;
        }

        public Builder totalTokens(Integer totalTokens) {
            this.totalTokens = totalTokens;
            return this;
        }

        public TokenizerUsage build() {
            return new TokenizerUsage(this);
        }
    }
}
