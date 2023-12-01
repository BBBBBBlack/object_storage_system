package org.example.protocol.Message;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public abstract class Message {
    private static final Map<Integer, Class<?>> messageClasses = new HashMap<>();
    protected static final int AskRequestMessage = 1;
    protected static final int AskResponseMessage = 2;
    protected static final int WriteRequestMessage = 3;
    protected static final int WriteResponseMessage = 4;
    protected static final int CAskRequestMessage = 5;
    protected static final int PullRequestMessage = 6;
    protected static final int PINGMessage = 7;
    protected static final int PONGMessage = 8;
    protected static final int PullColdRequestMessage = 9;
    public static final int DefaultRegionRequestMessage = 10;

    protected String randomStr;

    public abstract Integer getType();

    public abstract Integer getSequenceId();

    public static Class<?> getMessageClass(int messageType) {
        return messageClasses.get(messageType);
    }

    static {
        messageClasses.put(AskRequestMessage, AskRequestMessage.class);
        messageClasses.put(AskResponseMessage, AskResponseMessage.class);
        messageClasses.put(WriteRequestMessage, WriteRequestMessage.class);
        messageClasses.put(WriteResponseMessage, WriteResponseMessage.class);
        messageClasses.put(CAskRequestMessage, CAskRequestMessage.class);
        messageClasses.put(PullRequestMessage, PullRequestMessage.class);
//        messageClasses.put(PINGMessage, PINGMessage.class);
//        messageClasses.put(PONGMessage, PONGMessage.class);
        messageClasses.put(PullColdRequestMessage, PullColdRequestMessage.class);
        messageClasses.put(DefaultRegionRequestMessage, DefaultRegion.class);
    }
}
