package com.hawk.aop.internal;

import android.os.Build;
import android.os.Looper;
import android.os.Trace;
import android.util.Log;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;

/**
 * Created by lan on 2017/2/6.
 */
@Aspect
public class LogMethodAspect {

    @Pointcut("within(@com.hawk.aop.LogMethod *)")
    public void withinAnnotatedLogClass() {}

    @Pointcut("execution(!synthetic * *(..)) && withinAnnotatedLogClass()")
    public void methodInsideAnnotatedLogType() {}

    @Pointcut("execution(!synthetic *.new(..)) && withinAnnotatedLogClass()")
    public void constructorInsideAnnotatedLogType() {}

    @Pointcut("execution(@com.hawk.aop.LogMethod * *(..)) || methodInsideAnnotatedLogType()")
    public void methodLog() {}

    @Pointcut("execution(@com.hawk.aop.LogMethod *.new(..)) || constructorInsideAnnotatedLogType()")
    public void constructorLog() {}

    @Around("methodLog() || constructorLog()")
    public Object logAndExecutor(ProceedingJoinPoint joinPoint) throws Throwable {
        enterMethod(joinPoint);
        return joinPoint.proceed();
    }

    private void enterMethod(JoinPoint joinPoint) {
        CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();

        Class<?> cls = codeSignature.getDeclaringType();
        String methodName = codeSignature.getName();
        String[] parameterNames = codeSignature.getParameterNames();
        Object[] parameterValues = joinPoint.getArgs();

        StringBuilder builder = new StringBuilder("\u21E0 ");
        builder.append(methodName).append('(');
        for (int i = 0; i < parameterValues.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(parameterNames[i]).append('=');
            builder.append(Strings.toString(parameterValues[i]));
        }
        builder.append(")");

        if (Looper.myLooper() != Looper.getMainLooper()) {
            builder.append(" [Thread:\"").append(Thread.currentThread().getName()).append("\"]");
        }
        builder.append(" [TIME:\"").append(System.currentTimeMillis()).append("\"]");

        Log.v(asTag(cls), builder.toString());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            final String section = builder.toString().substring(2);
            Trace.beginSection(section);
        }
    }

    private static String asTag(Class<?> cls) {
        if (cls.isAnonymousClass()) {
            return asTag(cls.getEnclosingClass());
        }
        return cls.getSimpleName();
    }

}
