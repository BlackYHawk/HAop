package com.hawk.aop.internal;

import android.util.Log;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.concurrent.TimeUnit;

/**
 * Created by lan on 2017-12-22.
 */
@Aspect
public class TraceAspect {

    @Pointcut("within(@com.hawk.aop.Trace *)")
    public void withinAnnotionedTraceClass() {}

    @Pointcut("execution(!synthetic * *(..)) && withinAnnotionedTraceClass()")
    public void methodInsideAnnotatedTraceType() {}

    @Pointcut("execution(!synthetic *.new(..)) && withinAnnotionedTraceClass()")
    public void constructorInsideAnnotatedTraceType() {}

    @Pointcut("execution(@com.hawk.aop.Trace * *(..)) || methodInsideAnnotatedTraceType()")
    public void methodTrace() {}

    @Pointcut("execution(@com.hawk.aop.Trace *.new(..)) || constructorInsideAnnotatedTraceType()")
    public void constructorTrace() {}

    @Around("methodTrace() || constructorTrace()")
    public Object traceAndExecutor(ProceedingJoinPoint joinPoint) throws Throwable {
        long startAnosTime = System.nanoTime();
        Object result = joinPoint.proceed();
        long stopAnosTime = System.nanoTime();
        long lengthMilis = TimeUnit.NANOSECONDS.toMillis(stopAnosTime - startAnosTime);

        exitMethod(joinPoint, result, lengthMilis);
        return result;
    }

    private void exitMethod(JoinPoint joinPoint, Object result, long lengthMillis) {
        Signature signature = joinPoint.getSignature();
        Class<?> cls = signature.getDeclaringType();
        String methodName = signature.getName();
        boolean hasReturnType = signature instanceof MethodSignature
                && ((MethodSignature) signature).getReturnType() != void.class;

        StringBuilder builder = new StringBuilder("\u21E0 ")
                .append(methodName)
                .append(" [")
                .append(lengthMillis)
                .append("ms]");

        if (hasReturnType) {
            builder.append(" = ");
            builder.append(Strings.toString(result));
        }

        Log.v(asTag(cls), builder.toString());
    }

    private String asTag(Class<?> cls) {
        if (cls.isAnonymousClass()) {
            return asTag(cls.getEnclosingClass());
        }
        return cls.getSimpleName();
    }
}
