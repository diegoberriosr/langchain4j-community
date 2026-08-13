package dev.langchain4j.community.model.zhipu;

import static dev.langchain4j.community.model.zhipu.InternalZhipuAiHelper.toTools;
import static dev.langchain4j.community.model.zhipu.InternalZhipuAiHelper.toZhipuAiMessages;
import static dev.langchain4j.internal.RetryUtils.withRetry;
import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.internal.Utils.isNullOrEmpty;
import static dev.langchain4j.internal.ValidationUtils.ensureNotBlank;
import static java.util.Collections.singletonList;
import static java.util.function.Predicate.not;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.community.model.zhipu.chat.Message;
import dev.langchain4j.community.model.zhipu.tokenizer.TokenizerRequest;
import dev.langchain4j.community.model.zhipu.tokenizer.TokenizerResponse;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.TokenCountEstimator;
import java.time.Duration;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * Estimates the count of tokens ZhipuAI models would consume, using ZhipuAI's tokenizer endpoint.
 */
public class ZhipuAiTokenCountEstimator implements TokenCountEstimator {

    private final ZhipuAiClient client;
    private final String model;
    private final String userId;
    private final List<ToolSpecification> tools;
    private final Integer maxRetries;

    private ZhipuAiTokenCountEstimator(Builder builder) {
        this.client = ZhipuAiClient.builder()
                .baseUrl(getOrDefault(builder.baseUrl, "https://open.bigmodel.cn/"))
                .apiKey(builder.apiKey)
                .connectTimeout(getOrDefault(builder.connectTimeout, Duration.ofSeconds(60)))
                .readTimeout(getOrDefault(builder.readTimeout, Duration.ofSeconds(60)))
                .logRequests(getOrDefault(builder.logRequests, false))
                .logResponses(getOrDefault(builder.logResponses, false))
                .build();

        this.model = ensureNotBlank(builder.model, "model");
        this.userId = builder.userId;
        this.tools = builder.tools;
        this.maxRetries = getOrDefault(builder.maxRetries, 3);
    }

    @Override
    public int estimateTokenCountInText(String text) {
        if (isNullOrEmpty(text)) {
            throw new IllegalArgumentException("text cannot be null or empty");
        }

        return estimateTokenCountInMessages(singletonList(UserMessage.from(text)));
    }

    @Override
    public int estimateTokenCountInMessage(ChatMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("message cannot be null");
        }

        return estimateTokenCountInMessages(singletonList(message));
    }

    @Override
    public int estimateTokenCountInMessages(Iterable<ChatMessage> messages) {
        if (messages == null) {
            throw new IllegalArgumentException("messages cannot be null");
        }

        List<ChatMessage> supportedMessages = StreamSupport.stream(messages.spliterator(), false)
                .filter(ZhipuAiTokenCountEstimator::isSupportedByTokenizer)
                .filter(not(ZhipuAiTokenCountEstimator::isAiMessageWithoutContent))
                .toList();

        // An empty message list will cause the API to return an error, even if tools
        // are defined in the request, so we treat this case as an 'empty' conversation equivalent
        if (supportedMessages.isEmpty()) {
            return 0;
        }

        return estimateTokenCount(toZhipuAiMessages(supportedMessages));
    }

    private static boolean isSupportedByTokenizer(ChatMessage message) {
        return switch (message.type()) {
            case SYSTEM, USER, AI -> true;
            default -> false;
        };
    }

    private static boolean isAiMessageWithoutContent(ChatMessage message) {
        return message instanceof AiMessage aiMessage && isNullOrEmpty(aiMessage.text());
    }

    private int estimateTokenCount(List<Message> messages) {
        TokenizerRequest request = TokenizerRequest.builder()
                .model(model)
                .messages(messages)
                .tools(isNullOrEmpty(tools) ? null : toTools(tools))
                .userId(userId)
                .build();

        TokenizerResponse response = withRetry(() -> client.tokenizer(request), maxRetries);

        return response.getUsage().getPromptTokens();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String baseUrl;
        private String apiKey;
        private String model;
        private String userId;
        private List<ToolSpecification> tools;
        private Integer maxRetries;
        private Boolean logRequests;
        private Boolean logResponses;
        private Duration connectTimeout;
        private Duration readTimeout;

        private Builder() {}

        /**
         * Sets the base URL of the ZhipuAI API.
         */
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * Sets the API key used to authenticate with the ZhipuAI API.
         */
        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        /**
         * Sets the name of the model to estimate token counts for.
         */
        public Builder model(String model) {
            this.model = model;
            return this;
        }

        /**
         * Sets the end-user ID to associate with tokenizer requests, for abuse monitoring purposes.
         */
        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        /**
         * Sets the tools the model can call, included in every tokenizer request to account for their token cost.
         */
        public Builder tools(List<ToolSpecification> tools) {
            this.tools = tools;
            return this;
        }

        /**
         * Sets the maximum number of retries on a failed call to the ZhipuAI API.
         */
        public Builder maxRetries(Integer maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        /**
         * Sets whether to log requests sent to the ZhipuAI API.
         */
        public Builder logRequests(Boolean logRequests) {
            this.logRequests = logRequests;
            return this;
        }

        /**
         * Sets whether to log responses received from the ZhipuAI API.
         */
        public Builder logResponses(Boolean logResponses) {
            this.logResponses = logResponses;
            return this;
        }

        /**
         * Sets the timeout for establishing a connection with the ZhipuAI API. Defaults to 60 seconds.
         */
        public Builder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        /**
         * Sets the timeout for reading a response from the ZhipuAI API. Defaults to 60 seconds.
         */
        public Builder readTimeout(Duration readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public ZhipuAiTokenCountEstimator build() {
            return new ZhipuAiTokenCountEstimator(this);
        }
    }
}
