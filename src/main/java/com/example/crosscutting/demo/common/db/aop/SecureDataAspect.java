package com.example.crosscutting.demo.common.db.aop;

import com.example.crosscutting.demo.common.util.AESUtil;
import com.example.crosscutting.demo.common.db.Secured;
import com.example.crosscutting.demo.config.ApplicationContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 목적: 데이터 영속화 시 특정 컬럼에만 암/복호화 적용
 * 쿼리 실행 시 DAO 의 getter, setter 함수 실행 여부를 판단해
 * 암호화 혹은 복호화 실행
 * */
/**
 * Spring AOP 는 Spring 에서 관리하는 Bean 에만 적용가능하므로,
 * Pojo 객체인 DAO 에는 Spring AOP 를 적용할 수 없기 때문에 Aspectj 라이브러리를 이용해야한다.
 * 동적 생성되는 DAO 객체 적용 AOP 이기 때문에, LTW 방식은 선택한다.
 * Aspectj LTW 활성화 : java jar 실행 시 옵션 "-javaagent:/path/aspectjweaver.jar"
 */
@Slf4j
@Aspect
public class SecureDataAspect {

    private AESUtil aesUtil;

    /**
     *  aspectj 소스에 spring bean 주입
     */
    public SecureDataAspect(){
        this.aesUtil = ApplicationContextProvider.getApplicationContext().getBean(AESUtil.class);
    }

    /**
     * 암호화된 개인정보 read 할 때 복호화
     * 조건 :
     * 1. repository/adapter/ 하위 클래스에서 호출 시
     * 2. Dao getter 호출 시
     */
    @Around("call(* com.example.crosscutting.demo.repository.dao.*.get*(..)) && packageInRepositoryAdapter()" )
    public Object decrypt(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String fieldName = extractFieldName(method.getName());
        Field field = method.getDeclaringClass().getDeclaredField(fieldName);

        // 필드에 @Secured 어노테이션이 있는지 확인
        if (field.isAnnotationPresent(Secured.class)) {
            // Getter 호출
            Object result = joinPoint.proceed();
            //복호화
            if (result instanceof String) {
                String decryptedValue = aesUtil.decrypt((String) result); // 복호화 로직
                log.info("Aspect Secured Decrypt field= {}, original= {}, decrypted= {}", field.getName(), result, decryptedValue);
                return decryptedValue; // 복호화된 값 반환
            }
        }

        return joinPoint.proceed();
    }
    /**
     * 개인정보를 식별할 수 있는 데이터를 저장할 때 암호화
     * 조건 :
     * 1. repository/adapter/ 하위 클래스에서 호출 시
     * 2. Dao setter 호출 시
     * */
    @Around("call(* com.example.crosscutting.demo.repository.dao.*.set*(..)) && packageInRepositoryAdapter()")
    public Object encrypt(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String fieldName = extractFieldName(method.getName());
        Field field = method.getDeclaringClass().getDeclaredField(fieldName);

        // 필드에 @Secured 어노테이션이 있는지 확인
        if (field.isAnnotationPresent(Secured.class)) {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0 && args[0] instanceof String) {
                // 암호화 수행
                String originalValue = (String) args[0];
                if(originalValue!= null){
                    String encryptedValue = aesUtil.encrypt(originalValue); // 암호화 로직
                    args[0] = encryptedValue;
                    log.info("originalValue field= {}, original={}, encrypted={}",field.getName(), originalValue,encryptedValue);
                    return joinPoint.proceed(args);
                }
            }
        }

        return joinPoint.proceed();
    }

    @Pointcut("within(com.example.crosscutting.demo.repository.adapter..*)")
    public void packageInRepositoryAdapter() {}

    /**
     * 메서드 이름에서 필드 이름 추출 getName -> name
     */
    private String extractFieldName(String methodName) {
        return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
    }
}
