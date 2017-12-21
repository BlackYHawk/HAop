package com.hawk.aop.internal;

import android.os.Build;
import android.os.Trace;
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
 * Created by lan on 2017/2/6.
 */
@Aspect
public class LogMethodAspect {

    @Pointcut("execution(@LogMethod * *(..))")
    public void method() {}

    @Pointcut("execution(@LogMethod * *(..))")
    public void constructor() {}

    @Around("method() || constructor()")
    public Object logAndExecutor(ProceedingJoinPoint joinPoint) throws Throwable {
        long startAnosTime = System.nanoTime();
        Object result = joinPoint.proceed();
        long stopAnosTime = System.nanoTime();
        long lengthMilis = TimeUnit.NANOSECONDS.toMillis(stopAnosTime - startAnosTime);

        exitMethod(joinPoint, result, lengthMilis);
        return result;
    }

    private static void exitMethod(JoinPoint joinPoint, Object result, long lengthMillis) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Trace.endSection();
        }

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

    private static String asTag(Class<?> cls) {
        if (cls.isAnonymousClass()) {
            return asTag(cls.getEnclosingClass());
        }
        return cls.getSimpleName();
    }

}
