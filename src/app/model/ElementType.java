package app.model;

public enum ElementType {
    // Start events
    START_EVENT,
    MESSAGE_START,
    TIMER_START,
    CONDITIONAL_START,
    SIGNAL_START,
    ERROR_START,

    // Intermediate catch events
    MESSAGE_CATCH,
    TIMER_CATCH,
    CONDITIONAL_CATCH,
    LINK_CATCH,
    SIGNAL_CATCH,
    ERROR_CATCH,

    // Intermediate throw events
    MESSAGE_THROW,
    LINK_THROW,
    SIGNAL_THROW,

    // End events
    END_EVENT,
    MESSAGE_END,
    SIGNAL_END,
    ERROR_END,

    // Other
    INTERMEDIATE_EVENT,
    TASK,
    EXCLUSIVE_GATEWAY,
    SEQUENCE_FLOW
}
