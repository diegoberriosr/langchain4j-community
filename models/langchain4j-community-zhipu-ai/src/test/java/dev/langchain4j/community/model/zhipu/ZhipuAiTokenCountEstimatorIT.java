package dev.langchain4j.community.model.zhipu;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.community.model.zhipu.chat.ChatCompletionModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.CustomMessage;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.PdfFileContent;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.VideoContent;
import dev.langchain4j.model.TokenCountEstimator;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

@EnabledIfEnvironmentVariable(named = "ZHIPU_API_KEY", matches = ".+")
class ZhipuAiTokenCountEstimatorIT {

    private static final ToolSpecification CALCULATOR_TOOL = ToolSpecification.builder()
            .name("calculator")
            .description("returns a sum of two numbers")
            .parameters(JsonObjectSchema.builder()
                    .addIntegerProperty("first")
                    .addIntegerProperty("second")
                    .build())
            .build();

    private static final TokenCountEstimator TOKEN_ESTIMATOR = ZhipuAiTokenCountEstimator.builder()
            .model(ChatCompletionModel.GLM_4_6V.toString())
            .apiKey(System.getenv("ZHIPU_API_KEY"))
            .logRequests(true)
            .logResponses(true)
            .maxRetries(1)
            .build();

    private static final TokenCountEstimator TOKEN_ESTIMATOR_WITH_TOOLS = ZhipuAiTokenCountEstimator.builder()
            .model(ChatCompletionModel.GLM_4_6V.toString())
            .apiKey(System.getenv("ZHIPU_API_KEY"))
            .tools(singletonList(CALCULATOR_TOOL))
            .logRequests(true)
            .logResponses(true)
            .maxRetries(1)
            .build();

    private static final String IMAGE_URL =
            "https://upload.wikimedia.org/wikipedia/commons/e/e9/Felis_silvestris_silvestris_small_gradual_decrease_of_quality.png";
    private static final String PDF_URL = "https://upload.wikimedia.org/wikipedia/commons/1/13/Example.pdf";
    private static final String VIDEO_URL = "https://upload.wikimedia.org/wikipedia/commons/c/c0/Big_Buck_Bunny_4K.webm";

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void should_throw_when_instantiating_with_invalid_model_names(String invalidModelName) {

        // given
        ZhipuAiTokenCountEstimator.Builder builder = ZhipuAiTokenCountEstimator.builder()
                .model(invalidModelName)
                .apiKey(System.getenv("ZHIPU_API_KEY"));

        // when - then
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void should_estimate_tokens_in_plain_text() {

        // given - when
        int tokens = TOKEN_ESTIMATOR.estimateTokenCountInText("Hello!");

        // then
        assertThat(tokens).isGreaterThanOrEqualTo(0);
    }

    @Test
    void should_estimate_tokens_in_plain_text_when_tools_are_configured() {

        // given - when
        int tokens = TOKEN_ESTIMATOR_WITH_TOOLS.estimateTokenCountInText("Hello!");

        // then
        assertThat(tokens).isGreaterThanOrEqualTo(0);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {""})
    void should_throw_when_estimating_tokens_in_plain_text_with_invalid_text(String invalidText) {

        // given - when - then
        assertThrows(
                IllegalArgumentException.class,
                () -> TOKEN_ESTIMATOR.estimateTokenCountInText(invalidText));
    }

    @ParameterizedTest
    @MethodSource("chatMessages")
    void should_estimate_tokens_in_message(ChatMessage message) {

        // given - when
        int tokens = TOKEN_ESTIMATOR.estimateTokenCountInMessage(message);

        // then
        assertThat(tokens).isGreaterThanOrEqualTo(0);
    }

    @ParameterizedTest
    @MethodSource("chatMessages")
    void should_estimate_tokens_in_message_when_tools_are_configured(ChatMessage message) {

        // given - when
        int tokens = TOKEN_ESTIMATOR_WITH_TOOLS.estimateTokenCountInMessage(message);

        // then
        assertThat(tokens).isGreaterThanOrEqualTo(0);
    }

    @ParameterizedTest
    @MethodSource("nonSupportedChatMessages")
    void should_skip_estimating_tokens_in_non_supported_message_types(ChatMessage nonSupportedMessage) {

        // given - when
        int tokens = TOKEN_ESTIMATOR.estimateTokenCountInMessage(nonSupportedMessage);

        // then
        assertThat(tokens).isZero();
    }

    @Test
    void should_throw_when_estimating_tokens_in_null_message() {

        // given - when - then
        assertThrows(IllegalArgumentException.class, () -> TOKEN_ESTIMATOR.estimateTokenCountInMessage(null));
    }

    @ParameterizedTest
    @MethodSource("conversations")
    void should_estimate_tokens_in_conversation(List<ChatMessage> conversation) {

        // given - when
        int tokens = TOKEN_ESTIMATOR.estimateTokenCountInMessages(conversation);

        // then
        assertThat(tokens).isGreaterThanOrEqualTo(0);
    }

    @ParameterizedTest
    @MethodSource("conversations")
    void should_estimate_tokens_in_conversation_when_tools_are_configured(List<ChatMessage> conversation) {

        // given - when
        int tokens = TOKEN_ESTIMATOR_WITH_TOOLS.estimateTokenCountInMessages(conversation);

        // then
        assertThat(tokens).isGreaterThanOrEqualTo(0);
    }

    @Test
    void should_skip_estimating_tokens_in_conversation_without_supported_messages() {

        // given
        List<ChatMessage> conversation = nonSupportedChatMessages().toList();

        // when
        int tokens = TOKEN_ESTIMATOR.estimateTokenCountInMessages(conversation);

        // then
        assertThat(tokens).isZero();
    }

    @Test
    void should_skip_estimating_tokens_in_empty_conversation() {

        // given - when
        int tokens = TOKEN_ESTIMATOR.estimateTokenCountInMessages(emptyList());

        // then
        assertThat(tokens).isZero();
    }

    @Test
    void should_throw_when_estimating_tokens_in_null_conversation() {

        // given - when - then
        assertThrows(IllegalArgumentException.class, () -> TOKEN_ESTIMATOR.estimateTokenCountInMessages(null));
    }

    static Stream<ChatMessage> chatMessages() {
        ToolExecutionRequest toolExecutionRequest = ToolExecutionRequest.builder()
                .id("call_1")
                .name("calculator")
                .arguments("{\"first\": 2, \"second\": 2}")
                .build();

        return Stream.of(
                UserMessage.from("Hello!"),
                UserMessage.from(ImageContent.from(IMAGE_URL)),
                UserMessage.from(PdfFileContent.from(PDF_URL)),
                UserMessage.from(VideoContent.from(VIDEO_URL)),
                UserMessage.from(TextContent.from("Hello!"), TextContent.from("How are you doing?")),
                UserMessage.from(
                        TextContent.from("What do you see?"),
                        ImageContent.from(IMAGE_URL),
                        PdfFileContent.from(PDF_URL),
                        VideoContent.from(VIDEO_URL)),
                AiMessage.builder().thinking("The user is greeting me").build(),
                AiMessage.from("Hello! How can I help you?"),
                AiMessage.from(toolExecutionRequest),
                AiMessage.builder()
                        .thinking("The user wants to add two numbers")
                        .text("Let me use the calculator")
                        .toolExecutionRequests(singletonList(toolExecutionRequest))
                        .build(),
                SystemMessage.from("You are a helpful assistant"));
    }

    static Stream<List<ChatMessage>> conversations() {
        ToolExecutionRequest toolExecutionRequest = ToolExecutionRequest.builder()
                .id("call_1")
                .name("calculator")
                .arguments("{\"first\": 2, \"second\": 2}")
                .build();

        return Stream.of(
                List.of(SystemMessage.from("You are a helpful assistant"), UserMessage.from("Hello!")),
                List.of(
                        UserMessage.from("What is 2 + 2?"),
                        AiMessage.from("4"),
                        UserMessage.from("And what about 3 + 3?")),
                List.of(
                        SystemMessage.from("You are a helpful assistant"),
                        UserMessage.from(TextContent.from("What do you see?"), ImageContent.from(IMAGE_URL)),
                        AiMessage.from("A cat.")),
                List.of(
                        UserMessage.from("What is 2 + 2?"),
                        AiMessage.from("Let me use the calculator", singletonList(toolExecutionRequest)),
                        ToolExecutionResultMessage.from(toolExecutionRequest, "4"),
                        AiMessage.from("It is 4.")));
    }

    static Stream<ChatMessage> nonSupportedChatMessages() {
        ToolExecutionRequest toolExecutionRequest = ToolExecutionRequest.builder()
                .id("call_1")
                .name("calculator")
                .arguments("{\"first\": 2, \"second\": 2}")
                .build();

        return Stream.of(
                ToolExecutionResultMessage.from(toolExecutionRequest, "4"),
                ToolExecutionResultMessage.from("call_2", "calculator", "4"),
                ToolExecutionResultMessage.builder()
                        .id("call_3")
                        .toolName("calculator")
                        .contents(TextContent.from("4"), ImageContent.from(IMAGE_URL))
                        .build(),
                ToolExecutionResultMessage.builder()
                        .id("call_4")
                        .toolName("calculator")
                        .text("first and second must be integers")
                        .isError(true)
                        .build(),
                CustomMessage.from(Map.of("content", "test")));
    }

}
