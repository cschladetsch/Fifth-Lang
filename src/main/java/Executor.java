import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

enum EOperation {
    None,

    Plus,
    Minus,

    Equiv,
    NotEquiv,

    Assert,
    Break,
    Exit,
    Print,

    Resume,
    Suspend,
    Replace,

    Store,
    Get,
    Exists,
}

public class Executor extends ProcessBase {
    private final Map<String, Object> globals = new HashMap<String, Object>();
    private final Stack<Continuation> context = new Stack<>();
    private final Stack<Object> data = new Stack<>();
    private boolean breakFlow;
    private final float FLOAT_EPSLION = 0.00000001f;

    public Executor(ILogger logger) {
        super(logger);
    }

    @Override
    public String toString() {
        return "Executor{" +
                "globals=" + globals +
                ", context=" + context +
                ", data=" + data +
                ", continuation=" + (context.empty() ? "" : context.peek()) +
                '}';
    }

    @Override
    boolean run() {
        return execute(EOperation.Suspend);
    }

    public void run(Continuation continuation) {
        context.push(continuation);
        execute(EOperation.Suspend);
    }

    private boolean execute(Object object) {
        if (object == null) {
            return fail("Unexpected null");
        }

        if (object.getClass() == EOperation.class) {
            return executeOperation((EOperation) object);
        }

        if (object.getClass() == ETokenType.class) {
            return execute(convertToken((ETokenType)object));
        }

        return dataPush(object);
    }

    private Object convertToken(ETokenType token) {
        switch (token) {
            case Plus:
                return EOperation.Plus;
            case Minus:
                return EOperation.Minus;
            case Equiv:
                return EOperation.Equiv;
            case NotEquiv:
                return EOperation.NotEquiv;
            case Assert:
                return EOperation.Assert;
            case Break:
                return EOperation.Break;
        }

        return fail("Couldn't convert token " + token + " to something to do.");
    }

    private boolean executeOperation(EOperation operation) {
        switch (operation) {
            case Plus:
                return doBinaryOp(EOperation.Plus);
            case Equiv:
                return doBinaryOp(EOperation.Equiv);
            case Assert:
                return doUnaryOp(EOperation.Assert);
            case Break:
                return breakFlow = true;
            case Suspend:
                return doSuspend();
        }

        return fail("Unsupported operation " + operation);
    }

    private boolean doSuspend() {
        if (context.empty()) {
            return fail("Context empty.");
        }

        Continuation current = context.peek();
        Optional<Continuation> prev = contextPop();
        context.push(current);
        prev.ifPresent(context::push);

        process(current);

        return true;
    }

    private void process(Continuation current) {
        for (Object next : current.getCode()) {
            if (!execute(next) || hasFailed()) {
                return;
            }

            if (breakFlow) {
                breakFlow = false;
                break;
            }
        }
    }

    public void contextPush(Continuation continuation) {
        context.push(continuation);
    }

    public Optional<Continuation> contextPop() {
        if (context.empty()) {
            return Optional.empty();
        }
        return Optional.of(context.pop());
    }

    private boolean doBinaryOp(EOperation operation) {
        Object second = dataPop();
        Object first = dataPop();

        switch (operation) {
            case Plus:
                return neitherNull(first, second) && doPlus(first, second);
            case Minus:
                return neitherNull(first, second) && doMinus(first, second);
            case Equiv:
                return doEquiv(first, second);
            default:
                return notImplemented();
        }
    }

    private boolean doEquiv(Object first, Object second) {
        if (first == null) {
            return second == null;
        }

        if (second == null) {
            return fail("Cannot compare value to null");
        }

        if (first.getClass() == Float.class || second.getClass() == Float.class) {
            return Math.abs((float)first - (float)second) > FLOAT_EPSLION;
        }

        return dataPush(first.equals(second));
    }

    private boolean neitherNull(Object first, Object second) {
        if (first == null || second == null) {
            return fail("Unexpected null value");
        }

        return true;
    }

    private boolean doMinus(Object first, Object second) {
        if (first.getClass() == Integer.class) {
            return dataPush((int) first - (int) second);
        }

        return notImplemented(first.getClass().getName() + " - " + second.getClass().getName());
    }

    private boolean doPlus(Object first, Object second) {
        if (first.getClass() == Integer.class) {
            return dataPush((int) first + (int) second);
        }

        if (first.getClass() == String.class) {
            return dataPush((String)first + (String)second);
        }

        return notImplemented(first.getClass().getName() + " + " + second.getClass().getName());
    }

    private Object dataPop() {
        if (data.empty()) {
            fail("Empty data stack.");
            return null;
        }

        return data.pop();
    }

    private boolean doUnaryOp(EOperation operation) {
        switch (operation) {
            case Assert: {
                if (!trueEval(dataPop())) {
                    fail("Assertion failed.");
                    return false;
                }

                return true;
            }

            case Print: {
                logger.info(dataPop().toString());
                return true;
            }

            default:
                return notImplemented(operation.toString());
        }
    }

    private boolean dataPush(Object object) {
        data.push(object);
        return true;
    }

    private boolean trueEval(Object object) {
        if (object == null) {
            return false;
        }

        if (object.getClass() == Integer.class) {
            return (int) object != 0;
        }

        if (object.getClass() == Float.class) {
            return Math.abs((float)object) > FLOAT_EPSLION;
        }

        if (object.getClass() == Boolean.class) {
            return (Boolean) object;
        }

        return true;
    }
}
