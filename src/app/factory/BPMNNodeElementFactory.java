package app.factory;

import app.model.DiagramElement;
import app.model.ElementType;
import app.model.elements.*;
import app.util.IdGenerator;



public class BPMNNodeElementFactory implements NodeElementFactory {


    @Override
    public DiagramElement createElement(ElementType type, double x, double y) {
        String id = IdGenerator.generateId();
        switch (type) {
            case START_EVENT:        return new StartEventElement(id, x, y, "Start");
            case MESSAGE_START:      return new MessageStartElement(id, x, y, "Msg Start");
            case TIMER_START:        return new TimerStartElement(id, x, y, "Timer Start");
            case CONDITIONAL_START:  return new ConditionalStartElement(id, x, y, "Cond. Start");
            case SIGNAL_START:       return new SignalStartElement(id, x, y, "Signal Start");
            case ERROR_START:        return new ErrorStartElement(id, x, y, "Error Start");
            case INTERMEDIATE_EVENT: return new IntermediateEventElement(id, x, y, "Intermediate");
            case MESSAGE_CATCH:      return new MessageCatchElement(id, x, y, "Msg Catch");
            case TIMER_CATCH:        return new TimerCatchElement(id, x, y, "Timer Catch");
            case CONDITIONAL_CATCH:  return new ConditionalCatchElement(id, x, y, "Cond. Catch");
            case LINK_CATCH:         return new LinkCatchElement(id, x, y, "Link Catch");
            case SIGNAL_CATCH:       return new SignalCatchElement(id, x, y, "Signal Catch");
            case ERROR_CATCH:        return new ErrorCatchElement(id, x, y, "Error Catch");
            case MESSAGE_THROW:      return new MessageThrowElement(id, x, y, "Msg Throw");
            case LINK_THROW:         return new LinkThrowElement(id, x, y, "Link Throw");
            case SIGNAL_THROW:       return new SignalThrowElement(id, x, y, "Signal Throw");
            case END_EVENT:          return new EndEventElement(id, x, y, "End");
            case MESSAGE_END:        return new MessageEndElement(id, x, y, "Msg End");
            case SIGNAL_END:         return new SignalEndElement(id, x, y, "Signal End");
            case ERROR_END:          return new ErrorEndElement(id, x, y, "Error End");
            case TASK:               return new TaskElement(id, x, y, "Task");
            case EXCLUSIVE_GATEWAY:  return new ExclusiveGatewayElement(id, x, y, "XOR");
            default: throw new IllegalArgumentException("Use flow factory for SEQUENCE_FLOW");
        }
    }


    @Override
    public DiagramElement createElementWithStoredId(
            ElementType type, String id, double x, double y, String label) {
        switch (type) {
            case START_EVENT:        return new StartEventElement(id, x, y, label);
            case MESSAGE_START:      return new MessageStartElement(id, x, y, label);
            case TIMER_START:        return new TimerStartElement(id, x, y, label);
            case CONDITIONAL_START:  return new ConditionalStartElement(id, x, y, label);
            case SIGNAL_START:       return new SignalStartElement(id, x, y, label);
            case ERROR_START:        return new ErrorStartElement(id, x, y, label);
            case INTERMEDIATE_EVENT: return new IntermediateEventElement(id, x, y, label);
            case MESSAGE_CATCH:      return new MessageCatchElement(id, x, y, label);
            case TIMER_CATCH:        return new TimerCatchElement(id, x, y, label);
            case CONDITIONAL_CATCH:  return new ConditionalCatchElement(id, x, y, label);
            case LINK_CATCH:         return new LinkCatchElement(id, x, y, label);
            case SIGNAL_CATCH:       return new SignalCatchElement(id, x, y, label);
            case ERROR_CATCH:        return new ErrorCatchElement(id, x, y, label);
            case MESSAGE_THROW:      return new MessageThrowElement(id, x, y, label);
            case LINK_THROW:         return new LinkThrowElement(id, x, y, label);
            case SIGNAL_THROW:       return new SignalThrowElement(id, x, y, label);
            case END_EVENT:          return new EndEventElement(id, x, y, label);
            case MESSAGE_END:        return new MessageEndElement(id, x, y, label);
            case SIGNAL_END:         return new SignalEndElement(id, x, y, label);
            case ERROR_END:          return new ErrorEndElement(id, x, y, label);
            case TASK:               return new TaskElement(id, x, y, label);
            case EXCLUSIVE_GATEWAY:  return new ExclusiveGatewayElement(id, x, y, label);
            default: throw new IllegalArgumentException("Unexpected node type: " + type);
        }
    }
}
